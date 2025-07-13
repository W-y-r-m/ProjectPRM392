package com.example.projectprm392.adapters.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.database.JobEntity;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostManagementAdapter extends RecyclerView.Adapter<PostManagementAdapter.PostViewHolder> {

    private List<JobEntity> posts;
    private List<JobEntity> filteredPosts;
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onPostClick(JobEntity post);
        void onEditPost(JobEntity post);
        void onDeletePost(JobEntity post);
        void onTogglePostStatus(JobEntity post);
    }

    public PostManagementAdapter(OnPostActionListener listener) {
        this.listener = listener;
        this.posts = new ArrayList<>();
        this.filteredPosts = new ArrayList<>();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_management, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        JobEntity post = filteredPosts.get(position);
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return filteredPosts.size();
    }

    public void updatePosts(List<JobEntity> newPosts) {
        this.posts = new ArrayList<>(newPosts);
        // Don't reset filteredPosts here, let the caller apply filter
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredPosts.clear();
        
        if (query.isEmpty()) {
            filteredPosts.addAll(posts);
        } else {
            String lowercaseQuery = query.toLowerCase();
            for (JobEntity post : posts) {
                if (post.getTitle().toLowerCase().contains(lowercaseQuery) ||
                    post.getDescription().toLowerCase().contains(lowercaseQuery) ||
                    post.getLocation().toLowerCase().contains(lowercaseQuery)) {
                    filteredPosts.add(post);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void applyFilter(String filterType) {
        filteredPosts.clear();
        
        for (JobEntity post : posts) {
            boolean include = false;
            
            switch (filterType) {
                case "ALL":
                    include = true;
                    break;
                case "ACTIVE":
                    include = post.getIsActive() != null && post.getIsActive();
                    break;
                case "INACTIVE":
                    include = post.getIsActive() == null || !post.getIsActive();
                    break;
                case "JOB_POSTING":
                    include = "JOB_POSTING".equals(post.getPostType());
                    break;
                case "JOB_SEEKING":
                    include = "JOB_SEEKING".equals(post.getPostType());
                    break;
            }
            
            if (include) {
                filteredPosts.add(post);
            }
        }
        notifyDataSetChanged();
    }

    public List<JobEntity> getFilteredPosts() {
        return new ArrayList<>(filteredPosts);
    }

    public List<JobEntity> getAllPosts() {
        return new ArrayList<>(posts);
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPostTitle, tvPostAuthor, tvPostType, tvPostLocation, 
                        tvPostSalary, tvPostDate, tvPostViews;
        private Chip chipStatus;
        private ImageView ivEdit, ivDelete, ivToggleStatus;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvPostAuthor = itemView.findViewById(R.id.tvPostAuthor);
            tvPostType = itemView.findViewById(R.id.tvPostType);
            tvPostLocation = itemView.findViewById(R.id.tvPostLocation);
            tvPostSalary = itemView.findViewById(R.id.tvPostSalary);
            tvPostDate = itemView.findViewById(R.id.tvPostDate);
            tvPostViews = itemView.findViewById(R.id.tvPostViews);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivToggleStatus = itemView.findViewById(R.id.ivToggleStatus);
        }

        public void bind(JobEntity post, OnPostActionListener listener) {
            // Basic info
            tvPostTitle.setText(post.getTitle());
            tvPostLocation.setText(post.getLocation() != null ? post.getLocation() : "Không xác định");
            tvPostSalary.setText(post.getSalary() != null ? post.getSalary() : "Thỏa thuận");
            
            // Post type
            String postTypeDisplay = "JOB_POSTING".equals(post.getPostType()) ? "Tuyển dụng" : "Tìm việc";
            tvPostType.setText(postTypeDisplay);
            
            // Author (will be retrieved separately if needed)
            tvPostAuthor.setText("ID: " + post.getUserId());
            
            // Date
            if (post.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvPostDate.setText(sdf.format(post.getCreatedAt()));
            } else {
                tvPostDate.setText("Không rõ");
            }
            
            // Views count (placeholder since JobEntity doesn't have this field)
            tvPostViews.setText("0 lượt xem");
            
            // Status
            boolean isActive = post.getIsActive() != null ? post.getIsActive() : false;
            if (isActive) {
                chipStatus.setText("Đang hoạt động");
                chipStatus.setChipBackgroundColorResource(R.color.success);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
                ivToggleStatus.setImageResource(R.drawable.ic_lock);
            } else {
                chipStatus.setText("Đã khóa");
                chipStatus.setChipBackgroundColorResource(R.color.error);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
                ivToggleStatus.setImageResource(R.drawable.ic_unlock);
            }
            
            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
            
            ivEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditPost(post);
                }
            });
            
            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeletePost(post);
                }
            });
            
            ivToggleStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTogglePostStatus(post);
                }
            });
        }
    }
}
