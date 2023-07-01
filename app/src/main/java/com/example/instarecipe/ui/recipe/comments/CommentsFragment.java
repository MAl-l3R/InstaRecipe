package com.example.instarecipe.ui.recipe.comments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instarecipe.Database;
import com.example.instarecipe.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsFragment extends Fragment {

    String recipeID, uid;
    EditText addComment;
    ImageView sendBtn;
    CircleImageView profilePic;
    RecyclerView commentsRecyclerView;
    CommentsAdapter adapter;
    List<Comment> commentList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        recipeID = getArguments().getString("recipeID");
        uid = getArguments().getString("uid");

        // Get UI items
        addComment = view.findViewById(R.id.comment);
        sendBtn = view.findViewById(R.id.send_button);
        profilePic = view.findViewById(R.id.profile_pic);
        commentsRecyclerView = view.findViewById(R.id.recyclerview_comments);

        // Setup profile pic
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Glide.with(getContext()).load(user.getPhotoUrl()).into(profilePic);

        // Setup recycler view
        commentList = new ArrayList<>();
        adapter = new CommentsAdapter(getContext(), commentList);
        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(adapter);

        // Get the comments
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recipes").document(recipeID).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            for (DocumentChange documentChange : value.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                    if (documentChange.getDocument() != null) {
                                        // Get the comment
                                        String comment = documentChange.getDocument().getString("comment");
                                        String uid = documentChange.getDocument().getString("uid");
                                        Timestamp timestamp = documentChange.getDocument().getTimestamp("timestamp");

                                        // Add the comment to comment list
                                        commentList.add(new Comment(comment, uid, timestamp));

                                        // Update recycler view
                                        adapter.notifyDataSetChanged();
                                        commentsRecyclerView.scrollToPosition(commentList.size() - 1);
                                        commentsRecyclerView.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    // Update recycler view
                                    adapter.notifyDataSetChanged();
                                    commentsRecyclerView.scrollToPosition(commentList.size() - 1);
                                    commentsRecyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });

        // Send Comment Button
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = addComment.getText().toString();
                if (!comment.isEmpty()) {
                    Database.addComment(comment, recipeID, uid, Timestamp.now());
                    addComment.setText("");
                }
            }
        });

        return view;
    }
}
