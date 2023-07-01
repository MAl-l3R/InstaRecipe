package com.example.instarecipe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.instarecipe.model.Recipe;
import com.example.instarecipe.model.User;
import com.example.instarecipe.ui.profilePage.FollowersFollowingFragment;
import com.example.instarecipe.ui.profilePage.ProfileFragment;
import com.example.instarecipe.ui.recipe.RecipeAdapter1;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Database {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public static void saveUserInfo(Context context, FirebaseUser user, Uri profilePicUri, String name, String username, String email, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        DocumentReference userFile = db.collection("users").document(user.getUid());
        userFile.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        Map<String, Object> data = new HashMap<>();
                        data.put("name", name);
                        data.put("username", username.toLowerCase().trim());
                        data.put("email", email);

                        // Check if user's account exists
                        if (snapshot != null && snapshot.exists()) {
                            // Document exists, i.e. existing user
                            if (profilePicUri != null) {

                                // If profile pic was changed, then save it in storage first
                                StorageReference fileRef = storageReference.child("profile_pics").child(user.getUid() + ".jpg");
                                fileRef.putFile(profilePicUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uriFromStorage) {

                                                        // You can save the profile photo in firestore now
                                                        data.put("profilePic", uriFromStorage.toString());
                                                        userFile.update(data)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        // Update user's profile information in Firebase Authentication
                                                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                                .setDisplayName(name)
                                                                                .setPhotoUri(Uri.parse(uriFromStorage.toString()))
                                                                                .build();

                                                                        user.updateProfile(profileUpdates)
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {

                                                                                            // Also update the email in Firebase Authentication
                                                                                            user.updateEmail(email)
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                progressBar.setVisibility(View.GONE);
                                                                                                                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();

                                                                                                                // Send user to main activity
                                                                                                                Intent intent = new Intent(context, MainActivity.class);
                                                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                                                context.startActivity(intent);

                                                                                                            } else {
                                                                                                                progressBar.setVisibility(View.GONE);
                                                                                                                System.out.println(task.getException().getMessage());
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        } else {
                                                                                            progressBar.setVisibility(View.GONE);
                                                                                            System.out.println(task.getException().getMessage());
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Handle the exception if document update fails
                                                                        progressBar.setVisibility(View.GONE);
                                                                        System.out.println(e.getMessage());
                                                                    }
                                                                });
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.GONE);
                                                System.out.println(e.getMessage());
                                            }
                                        });

                            } else {

                                // If profile pic was not changed, then just update the name, email, username
                                userFile.update(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                // Update user's profile information in Firebase Authentication
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name)
                                                        .build();

                                                user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    // Also update the email in Firebase Authentication
                                                                    user.updateEmail(email)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        progressBar.setVisibility(View.GONE);
                                                                                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();

                                                                                        // Send user to main activity
                                                                                        Intent intent = new Intent(context, MainActivity.class);
                                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                        context.startActivity(intent);

                                                                                    } else {
                                                                                        progressBar.setVisibility(View.GONE);
                                                                                        System.out.println(task.getException().getMessage());
                                                                                    }
                                                                                }
                                                                            });
                                                                } else {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    System.out.println(task.getException().getMessage());
                                                                }
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle the exception if document update fails
                                                progressBar.setVisibility(View.GONE);
                                                System.out.println(e.getMessage());
                                            }
                                        });
                            }

                        } else {
                            // Document does not exist, i.e. new user
                            data.put("following", 0);
                            data.put("followers", 0);
                            data.put("recipeCount", 0);

                            // Check if profile pic was set
                            if (profilePicUri != null) {

                                // If profile pic was set, then save it in storage first
                                StorageReference fileRef = storageReference.child("profile_pics").child(user.getUid() + ".jpg");
                                fileRef.putFile(profilePicUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uriFromStorage) {

                                                        // You can save the profile photo in firestore now
                                                        data.put("profilePic", uriFromStorage.toString());
                                                        userFile.set(data)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        // Update user's profile information in Firebase Authentication
                                                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                                .setDisplayName(name)
                                                                                .setPhotoUri(Uri.parse(uriFromStorage.toString()))
                                                                                .build();

                                                                        user.updateProfile(profileUpdates)
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {

                                                                                            // Also update the email in Firebase Authentication
                                                                                            user.updateEmail(email)
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                progressBar.setVisibility(View.GONE);
                                                                                                                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();

                                                                                                                // Send user to main activity
                                                                                                                Intent intent = new Intent(context, MainActivity.class);
                                                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                                                context.startActivity(intent);

                                                                                                            } else {
                                                                                                                progressBar.setVisibility(View.GONE);
                                                                                                                System.out.println(task.getException().getMessage());
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        } else {
                                                                                            progressBar.setVisibility(View.GONE);
                                                                                            System.out.println(task.getException().getMessage());
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Handle the exception if document creation fails
                                                                        progressBar.setVisibility(View.GONE);
                                                                        System.out.println(e.getMessage());
                                                                    }
                                                                });
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.GONE);
                                                System.out.println(e.getMessage());
                                            }
                                        });

                            } else {

                                // If profile pic was not set, then set the default profile pic
                                String defaultProfilePic = "https://firebasestorage.googleapis.com/v0/b/instarecipe-c2b09.appspot.com/o/profile_pics%2FdMb0JMOQR5dg6PbVW5LDLeefkNk1.jpg?alt=media&token=4653f4e1-a21f-4a37-9885-eded0fe276d4&_gl=1*5i7f*_ga*MTQ4NDE5NzU0My4xNjc1OTg3NzI2*_ga_CW55HF8NVT*MTY4NjUxNTQ3NS40MC4xLjE2ODY1MTk0NDcuMC4wLjA.";
                                data.put("profilePic", defaultProfilePic);
                                        userFile.set(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                // Update user's profile information in Firebase Authentication
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name)
                                                        .setPhotoUri(Uri.parse(defaultProfilePic))
                                                        .build();

                                                user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    // Also update the email in Firebase Authentication
                                                                    user.updateEmail(email)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        progressBar.setVisibility(View.GONE);
                                                                                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();

                                                                                        // Send user to main activity
                                                                                        Intent intent = new Intent(context, MainActivity.class);
                                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                        context.startActivity(intent);

                                                                                    } else {
                                                                                        progressBar.setVisibility(View.GONE);
                                                                                        System.out.println(task.getException().getMessage());
                                                                                    }
                                                                                }
                                                                            });
                                                                } else {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    System.out.println(task.getException().getMessage());
                                                                }
                                                            }
                                                        });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle the exception if document creation fails
                                                progressBar.setVisibility(View.GONE);
                                                System.out.println(e.getMessage());
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the exception if document retrieval fails
                        progressBar.setVisibility(View.GONE);
                        System.out.println(e.getMessage());
                    }
                });
    }

    public static void addRecipe(String uid, Uri recipeImageUri, String recipeName, String cookTime, String ingredients, String recipeText, Timestamp time, ProgressBar progressBar, Context context) {

        progressBar.setVisibility(View.VISIBLE);
        String recipeID = recipeName.replaceAll("\\s", "")+"_"+formatTime(time);  // "RecipeName_Month00_00:00:00"
        // Save image in firebase storage first
        StorageReference fileRef = storageReference.child("recipe_images").child(recipeID + ".jpg");
        fileRef.putFile(recipeImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get saved image url
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uriFromStorage) {

                                // Get the user's file
                                DocumentReference userFile = db.collection("users").document(uid);

                                // Increment recipe count
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("recipeCount", FieldValue.increment(1));

                                // Update recipe count and then add recipe to user's file
                                userFile.update(updates)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                // Add the recipe to user file
                                                DocumentReference recipes = userFile.collection("recipes").document(recipeID);

                                                // Add only some recipe data here
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("name", recipeName);
                                                data.put("timestamp", time);

                                                // Save info in recipe folder
                                                recipes.set(data)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                // Add the recipe to recipe file
                                                                DocumentReference recipeFile = db.collection("recipes").document(recipeID);

                                                                // Add your document data here
                                                                Map<String, Object> recipeData = new HashMap<>();
                                                                recipeData.put("imageURL", uriFromStorage.toString());
                                                                recipeData.put("name", recipeName);
                                                                recipeData.put("likes", 0);
                                                                recipeData.put("uid", uid);
                                                                recipeData.put("cookTime", cookTime);
                                                                recipeData.put("ingredients", ingredients);
                                                                recipeData.put("recipe", recipeText);
                                                                recipeData.put("timestamp", time);

                                                                recipeFile.set(recipeData)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                progressBar.setVisibility(View.GONE);
                                                                                // Recipe added successfully
                                                                                Toast.makeText(context, "Recipe Added", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                progressBar.setVisibility(View.GONE);
                                                                                System.out.println(e.getMessage());
                                                                            }
                                                                        });

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressBar.setVisibility(View.GONE);
                                                                System.out.println(e.getMessage());
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.GONE);
                                                // Handle the exception if document creation fails
                                                System.out.println(e.getMessage());
                                            }
                                        });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        System.out.println(e.getMessage());
                    }
                });
    }

    public static void deleteRecipe(String uid, String recipeId, Context context, FragmentManager fragmentManager) {
        // Get the reference to the recipe image in Firebase Storage
        StorageReference imageRef = storageReference.child("recipe_images").child(recipeId + ".jpg");

        // Delete the recipe image from storage
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Remove the recipe from user's file
                        DocumentReference userFile = db.collection("users").document(uid);
                        userFile.collection("recipes").document(recipeId)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Decrement the recipe count in user's file
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("recipeCount", FieldValue.increment(-1));
                                        userFile.update(updates)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Remove the recipe from recipe file
                                                        DocumentReference recipeFile = db.collection("recipes").document(recipeId);
                                                        recipeFile.delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        // Recipe removed successfully
                                                                        // Create an instance of the destination fragment
                                                                        ProfileFragment profileFragment = new ProfileFragment();
                                                                        // Start a new FragmentTransaction
                                                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                                        // Replace the current fragment with the destination fragment
                                                                        fragmentTransaction.replace(R.id.frameLayout, profileFragment);
                                                                        // Commit the transaction
                                                                        fragmentTransaction.commit();
                                                                        Toast.makeText(context, "Recipe Deleted", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Handle the exception if recipe removal fails
                                                                        System.out.println(e.getMessage());
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle the exception if recipe count update fails
                                                        System.out.println(e.getMessage());
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle the exception if recipe removal fails
                                        System.out.println(e.getMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the exception if recipe image deletion fails
                        System.out.println(e.getMessage());
                    }
                });
    }


    public static void addComment(String comment, String recipeID, String uid, Timestamp time) {
        // Add the comment to the recipe file
        String commentID = uid+"_"+formatTime(time);  // "UID_Month00_00:00:00"
        DocumentReference commentsFile = db.collection("recipes").document(recipeID).collection("comments").document(commentID);

        // Add document data here
        Map<String, Object> data = new HashMap<>();
        data.put("comment", comment);
        data.put("uid", uid);
        data.put("timestamp", time);

        // Save the comment in the recipe's comments file
        commentsFile.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Comment added successfully
                        System.out.println("Comment Added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
    }

    public static void addFollower(String currentUserUid, String currentUserName,String uid, String name) {

        // Get this user's file
        DocumentReference userFile = db.collection("users").document(uid);

        // Increment their followers count
        Map<String, Object> updates = new HashMap<>();
        updates.put("followers", FieldValue.increment(1));

        // Update followers count and then add current user to this user's followers list
        userFile.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Add current user to this user's followers collection
                        DocumentReference followers = userFile.collection("followers").document(currentUserUid);

                        // Add some user info here
                        Map<String, Object> data = new HashMap<>();
                        data.put("name", currentUserName);

                        // Save current user's info in this user's followers collection
                        followers.set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        // Get the current user's file
                                        DocumentReference currUserFile = db.collection("users").document(currentUserUid);

                                        // Increment their following count
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("following", FieldValue.increment(1));

                                        // Update following count and then add this user to current user's following list
                                        currUserFile.update(updates)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        // Add this user to current user's following collection
                                                        DocumentReference following = currUserFile.collection("following").document(uid);

                                                        // Add some user info here
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", name);

                                                        // Save this user's info in current user's following collection
                                                        following.set(data)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        System.out.println("Follower added");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        System.out.println(e.getMessage());
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle the exception if document creation fails
                                                        System.out.println(e.getMessage());
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the exception if document creation fails
                        System.out.println(e.getMessage());
                    }
                });
    }

    public static void removeFollower(String currentUserUid,String uid) {

        // Get this user's file
        DocumentReference userFile = db.collection("users").document(uid);

        // Decrement their followers count
        Map<String, Object> updates = new HashMap<>();
        updates.put("followers", FieldValue.increment(-1));

        // Update followers count and then remove current user from this user's followers list
        userFile.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Remove current user from this user's followers collection
                        DocumentReference followers = userFile.collection("followers").document(currentUserUid);
                        followers.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        // Get the current user's file
                                        DocumentReference currUserFile = db.collection("users").document(currentUserUid);

                                        // Decrement their following count
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("following", FieldValue.increment(-1));

                                        // Update following count and then remove this user from current user's following list
                                        currUserFile.update(updates)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        // Remove this user from current user's following collection
                                                        DocumentReference following = currUserFile.collection("following").document(uid);
                                                        following.delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        System.out.println("Follower removed");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        System.out.println(e.getMessage());
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle the exception if document creation fails
                                                        System.out.println(e.getMessage());
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the exception if document creation fails
                        System.out.println(e.getMessage());
                    }
                });
    }

    public static void incrementLikes(String uid, String recipeID, Timestamp timeLiked) {

        // Get the recipe file
        DocumentReference recipeFile = db.collection("recipes").document(recipeID);

        // Increment its likes count
        Map<String, Object> updates = new HashMap<>();
        updates.put("likes", FieldValue.increment(1));

        // Update likes count and then add the recipe to user's liked recipes list
        recipeFile.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Add the recipe to user's liked recipes list
                        DocumentReference likedRecipes = db.collection("users").document(uid).collection("likedRecipes").document(recipeID);

                        // Add the liked time
                        Map<String, Object> data = new HashMap<>();
                        data.put("timeLiked", timeLiked);

                        // Save the recipe in user's liked recipes collection
                        likedRecipes.set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        System.out.println("Recipe Liked");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the exception if document creation fails
                        System.out.println(e.getMessage());
                    }
                });
    }

    public static void decrementLikes(String uid, String recipeID) {

        // Get the recipe file
        DocumentReference recipeFile = db.collection("recipes").document(recipeID);

        // Decrement its likes count
        Map<String, Object> updates = new HashMap<>();
        updates.put("likes", FieldValue.increment(-1));

        // Update likes count and then delete the recipe from user's liked recipes list
        recipeFile.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Delete the recipe from user's liked recipes list
                        DocumentReference likedRecipes = db.collection("users").document(uid).collection("likedRecipes").document(recipeID);
                        likedRecipes.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        System.out.println("Recipe Unliked");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the exception if document creation fails
                        System.out.println(e.getMessage());
                    }
                });
    }

    public static void searchForUsers(String input, String currentUserUid, OnSuccessListener<ArrayList<User>> successListener, OnFailureListener failureListener) {

        db.collection("users")
                .whereGreaterThanOrEqualTo("username", input)
                .whereLessThan("username", input + "\uf8ff")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        ArrayList<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String uid = document.getId();
                            if (!uid.equals(currentUserUid)) {
                                String name = document.getString("name");
                                String username = document.getString("username");
                                String email = document.getString("email");
                                String profilePic = document.getString("profilePic");
                                int following = document.getLong("following").intValue();
                                int followers = document.getLong("followers").intValue();
                                int recipeCount = document.getLong("recipeCount").intValue();
                                ArrayList<Recipe> recipes = new ArrayList<>();

                                // Create user instance and add user to user list
                                User user = new User(uid, name, username, email, profilePic, followers, following, recipeCount, recipes);
                                userList.add(user);
                            }

                        }

                        successListener.onSuccess(userList);
                    }
                })
                .addOnFailureListener(failureListener);
    }

    public static void searchForFollowersFollowing(String resultsFor, String uid, String input, OnSuccessListener<ArrayList<User>> successListener, OnFailureListener failureListener) {

        db.collection("users").document(uid).collection(resultsFor)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        final int[] documentsProcessed = {0};
                        ArrayList<User> userList = new ArrayList<>();
                        // For each follower/following, access their profile
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userUid = document.getId();
                            db.collection("users").document(userUid)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            String username = documentSnapshot.getString("username");
                                            if (username.startsWith(input)) {
                                                String name = documentSnapshot.getString("name");
                                                String email = documentSnapshot.getString("email");
                                                String profilePic = documentSnapshot.getString("profilePic");
                                                int followers = documentSnapshot.getLong("followers").intValue();
                                                int following = documentSnapshot.getLong("following").intValue();
                                                int recipeCount = documentSnapshot.getLong("recipeCount").intValue();
                                                ArrayList<Recipe> recipes = new ArrayList<>();

                                                // Create user instance and add user to user list
                                                User user = new User(userUid, name, username, email, profilePic, followers, following, recipeCount, recipes);
                                                userList.add(user);
                                            }

                                            // Increment the counter for each document processed
                                            documentsProcessed[0]++;

                                            // This is another way to handle asynchronous threads...
                                            // Check if this is the last document retrieval
                                            if (documentsProcessed[0] == queryDocumentSnapshots.size()) {
                                                // All documents have been retrieved and processed
                                                successListener.onSuccess(userList);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    });
                        }

                    }
                })
                .addOnFailureListener(failureListener);
    }

    public static void getRecipes(OnSuccessListener<ArrayList<Recipe>> successListener, OnFailureListener failureListener) {

        db.collection("recipes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        ArrayList<Recipe> recipeList = new ArrayList<>();
                        // Iterate through all recipes
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            // Access recipe fields
                            String recipeID = document.getId();
                            String recipeImageURL = document.getString("imageURL");
                            String recipeName = document.getString("name");
                            String uid = document.getString("uid");
                            int likes = document.getLong("likes").intValue();
                            String cookTime = document.getString("cookTime");
                            String ingredients = document.getString("ingredients");
                            String recipeText = document.getString("recipe");
                            Timestamp time = document.getTimestamp("timestamp");
                            // Add recipe to list
                            Recipe recipe = new Recipe(recipeID, recipeName, uid, likes, cookTime, ingredients, recipeText, recipeImageURL, time);
                            recipeList.add(recipe);
                        }

                        successListener.onSuccess(recipeList);
                    }
                })
                .addOnFailureListener(failureListener);
    }

    public static void getUsersRecipes(String uid, OnSuccessListener<ArrayList<Recipe>> successListener, OnFailureListener failureListener) {

        // Get the user's recipes
        // Create a query to search for recipes created by the user
        Query userRecipes = db.collection("recipes").whereEqualTo("uid", uid);
        userRecipes.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        // Initialize list of user's recipes
                        ArrayList<Recipe> recipeList = new ArrayList<>();
                        // Get the matching documents
                        List<DocumentSnapshot> matchingDocuments = querySnapshot.getDocuments();
                        // Iterate through the matching documents
                        for (DocumentSnapshot document : matchingDocuments) {
                            // Access recipe fields
                            String recipeID = document.getId();
                            String recipeImageURL = document.getString("imageURL");
                            String recipeName = document.getString("name");
                            String uid = document.getString("uid");
                            int likes = document.getLong("likes").intValue();
                            String cookTime = document.getString("cookTime");
                            String ingredients = document.getString("ingredients");
                            String recipeText = document.getString("recipe");
                            Timestamp time = document.getTimestamp("timestamp");
                            // Add recipe to list
                            Recipe recipe = new Recipe(recipeID, recipeName, uid, likes, cookTime, ingredients, recipeText, recipeImageURL, time);
                            recipeList.add(recipe);
                        }

                        // Sort the recipeList in descending order based on timestamp
                        Collections.sort(recipeList, new Comparator<Recipe>() {
                            @Override
                            public int compare(Recipe recipe1, Recipe recipe2) {
                                return recipe2.getTimestamp().compareTo(recipe1.getTimestamp());
                            }
                        });

                        successListener.onSuccess(recipeList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the exception if the retrieval fails
                        // This could occur due to permission issues or network problems
                        // Note: This failure listener is not triggered when the collection does not exist
                        System.out.println(e.getMessage());
                    }
                });
    }

    public static void getLikedRecipes(String uid, OnSuccessListener<ArrayList<Recipe>> successListener, OnFailureListener failureListener) {
        db.collection("users").document(uid).collection("likedRecipes")
                .orderBy("timeLiked", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Get the list of liked recipe IDs
                        ArrayList<String> recipeIDs = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            recipeIDs.add(document.getId());
                        }

                        // Create a list to store the sorted recipes
                        ArrayList<Recipe> recipeList = new ArrayList<>();

                        // Check if user liked any recipes
                        if (!recipeIDs.isEmpty()) {
                            // Now get the liked recipes
                            db.collection("recipes")
                                    .whereIn(FieldPath.documentId(), recipeIDs)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            // Create a map to store the recipes by ID
                                            Map<String, Recipe> recipeMap = new HashMap<>();

                                            // Iterate through all recipes and add them to the map
                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                String recipeID = document.getId();
                                                String userUid = document.getString("uid");
                                                String recipeImageURL = document.getString("imageURL");
                                                String recipeName = document.getString("name");
                                                int likes = document.getLong("likes").intValue();
                                                String cookTime = document.getString("cookTime");
                                                String ingredients = document.getString("ingredients");
                                                String recipeText = document.getString("recipe");
                                                Timestamp time = document.getTimestamp("timestamp");

                                                Recipe recipe = new Recipe(recipeID, recipeName, userUid, likes, cookTime, ingredients, recipeText, recipeImageURL, time);
                                                recipeMap.put(recipeID, recipe);
                                            }

                                            // Iterate through the recipeIDs in the order of liked recipes
                                            for (String recipeID : recipeIDs) {
                                                if (recipeMap.containsKey(recipeID)) {
                                                    Recipe recipe = recipeMap.get(recipeID);
                                                    recipeList.add(recipe);
                                                }
                                            }

                                            successListener.onSuccess(recipeList);
                                        }
                                    })
                                    .addOnFailureListener(failureListener);
                        } else {
                            // If no liked recipes, then return empty list
                            successListener.onSuccess(recipeList);
                        }
                    }
                })
                .addOnFailureListener(failureListener);
    }




    public static void getFollowingRecipes(String uid, OnSuccessListener<ArrayList<Recipe>> successListener, OnFailureListener failureListener) {
        db.collection("users").document(uid).collection("following")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Get the list of users followed
                        ArrayList<String> usersFollowed = new ArrayList<>();
                        ArrayList<String> usersDisplayed = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            usersFollowed.add(document.getId());
                        }

                        // Now get their recipes
                        ArrayList<Recipe> recipeList = new ArrayList<>();
                        db.collection("recipes")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        // Iterate through all recipes
                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                            String userUid = document.getString("uid");
                                            // If the recipe belongs to a person the user follows,
                                            if (usersFollowed.contains(userUid) && !usersDisplayed.contains(userUid)) {
                                                // Access their recipe
                                                String recipeID = document.getId();
                                                String recipeImageURL = document.getString("imageURL");
                                                String recipeName = document.getString("name");
                                                int likes = document.getLong("likes").intValue();
                                                String cookTime = document.getString("cookTime");
                                                String ingredients = document.getString("ingredients");
                                                String recipeText = document.getString("recipe");
                                                Timestamp time = document.getTimestamp("timestamp");
                                                // Add their recipe to list
                                                Recipe recipe = new Recipe(recipeID, recipeName, userUid, likes, cookTime, ingredients, recipeText, recipeImageURL, time);
                                                recipeList.add(recipe);
                                                usersDisplayed.add(userUid);
                                            }
                                        }
                                        successListener.onSuccess(recipeList);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(failureListener);
    }



    private static String formatTime(Timestamp timestamp) {

        // Convert Firebase Timestamp to Java Date
        Date date = timestamp.toDate();

        // Define the desired date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMdd_HH:mm:ss", Locale.CANADA);

        // Format the date to the desired format
        String formattedTimestamp = dateFormat.format(date);

        return formattedTimestamp;
    }

}
