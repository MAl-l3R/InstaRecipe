package com.example.instarecipe.ui.explorePage.accountsTab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instarecipe.R;
import com.example.instarecipe.RecyclerViewInterface;
import com.example.instarecipe.model.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.AccountsViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    ArrayList<User> accountsList;
    Context context;

    public AccountsAdapter(Context context, ArrayList<User> accountsList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.accountsList = accountsList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public ArrayList<User> getAccountsList() {
        return accountsList;
    }

    @NonNull
    @Override
    public AccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_account, parent, false);
        return new AccountsViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountsViewHolder holder, int position) {
        User user = accountsList.get(position);
        Glide.with(context).load(user.getProfilePicUrl()).into(holder.profilePicture);
        holder.username.setText(user.getUsername());
        holder.name.setText(user.getName());
        // Display all together after all info has been fetched
        holder.userCard.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return accountsList.size();
    }

    public static class AccountsViewHolder extends RecyclerView.ViewHolder {

        CardView userCard;
        CircleImageView profilePicture;
        TextView username, name;
        public AccountsViewHolder(View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            userCard = itemView.findViewById(R.id.user_card);
            profilePicture = itemView.findViewById(R.id.profile_pic);
            username = itemView.findViewById(R.id.comment);
            name = itemView.findViewById(R.id.name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && recyclerViewInterface != null) {
                        recyclerViewInterface.onItemClick(pos);
                    }
                }
            });
        }
    }
}

