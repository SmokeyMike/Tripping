package com.example.project12.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project12.R;
import com.example.project12.models.Profile;
import com.example.project12.models.Trip;

import java.time.format.DateTimeFormatter;

public class ProfileAdapter extends ListAdapter<Profile, ProfileAdapter.ProfileViewHolder> {

    public interface OnProfileClickListener {
        void onProfileClick(Profile profile);
    }

    private final OnProfileClickListener listener;

    public ProfileAdapter(OnProfileClickListener listener) {
        super(new DiffUtil.ItemCallback<Profile>() {
            @Override
            public boolean areItemsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
                return oldItem.getEmail().equals(newItem.getEmail());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = getItem(position);
        holder.bind(profile);
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvEmail;

        ProfileViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivProfilePhoto);
            tvEmail = itemView.findViewById(R.id.tvProfileEmail);
            itemView.setOnClickListener(v -> listener.onProfileClick(getItem(getAdapterPosition())));
        }

        void bind(Profile profile) {
            tvEmail.setText(profile.getEmail());
            Glide.with(itemView.getContext())
                    .load(profile.getPhotoUrl())
                    .into(ivPhoto);
        }
    }
}
