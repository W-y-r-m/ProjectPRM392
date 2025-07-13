package com.example.projectprm392.adapters.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.database.JobEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JobManagementAdapter extends RecyclerView.Adapter<JobManagementAdapter.JobViewHolder> {

    public interface OnJobActionListener {
        void onJobClick(JobEntity job);
        void onEditJob(JobEntity job);
        void onDeleteJob(JobEntity job);
        void onToggleJobStatus(JobEntity job);
    }

    private List<JobEntity> allJobs;
    private List<JobEntity> filteredJobs;
    private OnJobActionListener listener;
    private SimpleDateFormat dateFormat;

    public JobManagementAdapter(OnJobActionListener listener) {
        this.listener = listener;
        this.allJobs = new ArrayList<>();
        this.filteredJobs = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job_management, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobEntity job = filteredJobs.get(position);
        
        holder.tvJobTitle.setText(job.getTitle());
        holder.tvJobDescription.setText(job.getDescription());
        holder.tvJobSalary.setText(job.getSalary() != null ? job.getSalary() : "Thỏa thuận");
        holder.tvJobLocation.setText(job.getLocation());
        holder.tvJobType.setText(getJobTypeDisplay(job.getPostType()));
        holder.tvJobCreatedAt.setText(job.getCreatedAt() != null ? 
            dateFormat.format(job.getCreatedAt()) : "N/A");
        
        // Set status
        boolean isActive = job.getIsActive() != null ? job.getIsActive() : false;
        holder.tvJobStatus.setText(isActive ? "Đang hoạt động" : "Đã khóa");
        holder.tvJobStatus.setTextColor(holder.itemView.getContext().getResources()
            .getColor(isActive ? R.color.green : R.color.red));
        
        // Set status icon
        holder.ivJobStatus.setImageResource(isActive ? 
            R.drawable.ic_check_circle : R.drawable.ic_lock);
        holder.ivJobStatus.setColorFilter(holder.itemView.getContext().getResources()
            .getColor(isActive ? R.color.green : R.color.red));
        
        // Set post type icon
        boolean isJobPosting = "JOB_POSTING".equals(job.getPostType());
        holder.ivJobType.setImageResource(isJobPosting ? 
            R.drawable.ic_work : R.drawable.ic_person_search);
        
        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onJobClick(job);
            }
        });
        
        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditJob(job);
            }
        });
        
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteJob(job);
            }
        });
        
        holder.ivToggleStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleJobStatus(job);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredJobs.size();
    }

    public void updateJobs(List<JobEntity> jobs) {
        this.allJobs.clear();
        this.allJobs.addAll(jobs);
        this.filteredJobs.clear();
        this.filteredJobs.addAll(jobs);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredJobs.clear();
        if (query.isEmpty()) {
            filteredJobs.addAll(allJobs);
        } else {
            String lowercaseQuery = query.toLowerCase();
            for (JobEntity job : allJobs) {
                if (job.getTitle().toLowerCase().contains(lowercaseQuery) ||
                    job.getDescription().toLowerCase().contains(lowercaseQuery) ||
                    job.getLocation().toLowerCase().contains(lowercaseQuery)) {
                    filteredJobs.add(job);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void applyFilter(String filterType) {
        filteredJobs.clear();
        
        switch (filterType) {
            case "ALL":
                filteredJobs.addAll(allJobs);
                break;
            case "ACTIVE":
                for (JobEntity job : allJobs) {
                    if (job.getIsActive() != null && job.getIsActive()) {
                        filteredJobs.add(job);
                    }
                }
                break;
            case "INACTIVE":
                for (JobEntity job : allJobs) {
                    if (job.getIsActive() == null || !job.getIsActive()) {
                        filteredJobs.add(job);
                    }
                }
                break;
            case "JOB_SEEKING":
                for (JobEntity job : allJobs) {
                    if ("JOB_SEEKING".equals(job.getPostType())) {
                        filteredJobs.add(job);
                    }
                }
                break;
            case "JOB_POSTING":
                for (JobEntity job : allJobs) {
                    if ("JOB_POSTING".equals(job.getPostType())) {
                        filteredJobs.add(job);
                    }
                }
                break;
        }
        notifyDataSetChanged();
    }

    public List<JobEntity> getFilteredJobs() {
        return new ArrayList<>(filteredJobs);
    }

    private String getJobTypeDisplay(String postType) {
        if ("JOB_POSTING".equals(postType)) {
            return "Tuyển dụng";
        } else if ("JOB_SEEKING".equals(postType)) {
            return "Tìm việc";
        }
        return "Không xác định";
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvJobDescription, tvJobSalary, tvJobLocation, 
                 tvJobType, tvJobStatus, tvJobCreatedAt;
        ImageView ivJobType, ivJobStatus, ivEdit, ivDelete, ivToggleStatus;

        JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvJobDescription = itemView.findViewById(R.id.tvJobDescription);
            tvJobSalary = itemView.findViewById(R.id.tvJobSalary);
            tvJobLocation = itemView.findViewById(R.id.tvJobLocation);
            tvJobType = itemView.findViewById(R.id.tvJobType);
            tvJobStatus = itemView.findViewById(R.id.tvJobStatus);
            tvJobCreatedAt = itemView.findViewById(R.id.tvJobCreatedAt);
            
            ivJobType = itemView.findViewById(R.id.ivJobType);
            ivJobStatus = itemView.findViewById(R.id.ivJobStatus);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivToggleStatus = itemView.findViewById(R.id.ivToggleStatus);
        }
    }
}
