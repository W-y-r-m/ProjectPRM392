package com.example.projectprm392.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectprm392.R;
import com.example.projectprm392.database.JobEntity;
import java.util.List;

public class PostHistoryAdapter extends RecyclerView.Adapter<PostHistoryAdapter.ViewHolder> {
    private List<JobEntity> jobList;

    public PostHistoryAdapter(List<JobEntity> jobList) {
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JobEntity job = jobList.get(position);
        holder.tvTitle.setText(job.getTitle());
        holder.tvDate.setText(job.getCreatedAt() != null ? job.getCreatedAt().toString() : "");
        holder.tvContent.setText(job.getDescription());

        holder.itemView.setOnClickListener(v -> {
            android.content.Context context = v.getContext();
            android.content.Intent intent = new android.content.Intent(context,
                    com.example.projectprm392.activities.ApprovedUsersActivity.class);
            intent.putExtra("jobId", job.getId()); // Đảm bảo JobEntity có getId()
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPostTitle);
            tvDate = itemView.findViewById(R.id.tvPostDate);
            tvContent = itemView.findViewById(R.id.tvPostContent);
        }
    }
}
