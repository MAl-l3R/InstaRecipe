package com.example.instarecipe.ui.recipe.comments;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instarecipe.R;
import com.example.instarecipe.model.Recipe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Context context;
    private List<Comment> commentList;

    public CommentsAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        // Get the comment object
        Comment comment = commentList.get(position);

        // First fetch the username and profile pic then set everything and display
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(comment.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // Set the profile pic, username, comment
                        Glide.with(context).load(documentSnapshot.getString("profilePic")).into(holder.profilePicture);
                        holder.username.setText(documentSnapshot.getString("username"));
                        holder.comment.setText(comment.getComment());

                        // generate relative time span
                        long commentTime = comment.getTimestamp().toDate().getTime();
                        long nowTime = System.currentTimeMillis();
                        CharSequence ago = DateUtils.getRelativeTimeSpanString(commentTime, nowTime, DateUtils.SECOND_IN_MILLIS);
                        holder.timestamp.setText(ago.toString());

                        // Display comment card now
                        holder.commentCard.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {

        CardView commentCard;
        CircleImageView profilePicture;
        TextView comment, username, timestamp;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            commentCard = itemView.findViewById(R.id.comment_card);
            comment = itemView.findViewById(R.id.comment);
            profilePicture = itemView.findViewById(R.id.profile_pic);
            username = itemView.findViewById(R.id.username);
            timestamp = itemView.findViewById(R.id.timestamp);
        }

    }

}
