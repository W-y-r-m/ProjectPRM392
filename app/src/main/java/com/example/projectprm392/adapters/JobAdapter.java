package com.example.projectprm392.adapters;

import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<JobEntity> jobs;
    private OnJobClickListener onJobClickListener;
    private Location userLocation;
    private DatabaseHelper databaseHelper;

    public interface OnJobClickListener {
        void onJobClick(JobEntity job);
        void onContactClick(JobEntity job);
        void onSaveClick(JobEntity job);
    }

    public JobAdapter(List<JobEntity> jobs, OnJobClickListener listener, DatabaseHelper databaseHelper) {
        this.jobs = jobs;
        this.onJobClickListener = listener;
        this.databaseHelper = databaseHelper;
    }

    public void updateJobs(List<JobEntity> newJobs) {
        this.jobs = newJobs;
        notifyDataSetChanged();
    }

    public void setUserLocation(Location location) {
        this.userLocation = location;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobEntity job = jobs.get(position);
        holder.bind(job);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    public class JobViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvSalary;
        private TextView tvLocation;
        private TextView tvNeededAmount;
        private TextView tvWorkingTime;
        private TextView tvCompanyName;
        private TextView tvPostTime;
        private ImageView btnView;
        private MaterialButton btnSave;
        private MaterialButton btnContact;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvNeededAmount = itemView.findViewById(R.id.tvNeededAmount);
            tvWorkingTime = itemView.findViewById(R.id.tvWorkingTime);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            btnView = itemView.findViewById(R.id.btnView);
            btnSave = itemView.findViewById(R.id.btnSave);
            btnContact = itemView.findViewById(R.id.btnContact);
        }

        public void bind(JobEntity job) {
            tvTitle.setText(job.getTitle());
            tvDescription.setText(job.getDescription());
            
            // Get company info from database
            if (databaseHelper != null) {
                UserEntity user = databaseHelper.getUserById(job.getUserId());
                if (user != null) {
                    if ("JOB_SEEKING".equals(job.getPostType())) {
                        tvCompanyName.setText("👤 " + user.getFullName() + " (Tìm việc)");
                    } else {
                        tvCompanyName.setText("🏢 " + user.getFullName());
                    }
                } else {
                    tvCompanyName.setText("Không rõ");
                }
            }
            
            // Post time (simplified)
            tvPostTime.setText("Vài giờ trước");
            
            // Format salary
            tvSalary.setText(job.getSalary());
            
            // Location with real distance calculation
            if (job.getLocationLatitude() != null && job.getLocationLongitude() != null && userLocation != null) {
                double jobLat = job.getLocationLatitude();
                double jobLng = job.getLocationLongitude();
                double userLat = userLocation.getLatitude();
                double userLng = userLocation.getLongitude();
                
                // EXPERIMENTAL FIX: Check if coordinates might be swapped
                boolean jobCoordsSwapped = false;
                boolean userCoordsSwapped = false;
                
                if (jobLat > 50 && jobLng < 30) {
                    android.util.Log.w("JobAdapter", "Job coordinates seem swapped! Trying to fix");
                    double temp = jobLat;
                    jobLat = jobLng;
                    jobLng = temp;
                    jobCoordsSwapped = true;
                }
                
                if (userLat > 50 && userLng < 30) {
                    android.util.Log.w("JobAdapter", "User coordinates seem swapped! Trying to fix");
                    double temp = userLat;
                    userLat = userLng;
                    userLng = temp;
                    userCoordsSwapped = true;
                }
                
                Location jobLocation = new Location("job");
                jobLocation.setLatitude(jobLat);
                jobLocation.setLongitude(jobLng);
                
                Location userLocationFixed = new Location("user");
                userLocationFixed.setLatitude(userLat);
                userLocationFixed.setLongitude(userLng);
                
                float distance = userLocationFixed.distanceTo(jobLocation) / 1000; // Convert to km
                
                // Debug logging
                android.util.Log.d("JobAdapter", "=== DISTANCE CALCULATION DEBUG ===");
                android.util.Log.d("JobAdapter", "Job: " + job.getTitle());
                android.util.Log.d("JobAdapter", "User location: " + userLat + ", " + userLng + (userCoordsSwapped ? " (FIXED)" : ""));
                android.util.Log.d("JobAdapter", "Job location: " + jobLat + ", " + jobLng + (jobCoordsSwapped ? " (FIXED)" : ""));
                android.util.Log.d("JobAdapter", "Distance: " + distance + " km");
                
                tvLocation.setText(String.format("Cách bạn ~%.1fkm", distance));
            } else {
                tvLocation.setText(job.getLocationName() != null ? job.getLocationName() : "Vị trí chưa xác định");
            }
            
            // Needed amount
            tvNeededAmount.setText("Cần " + job.getNeededAmount() + " người");
            
            // Working time
            if (job.getWorkingTime() != null && !job.getWorkingTime().isEmpty()) {
                tvWorkingTime.setText(job.getWorkingTime());
            } else {
                tvWorkingTime.setText("Linh hoạt");
            }
            
            // Card click listener
            cardView.setOnClickListener(v -> {
                if (onJobClickListener != null) {
                    onJobClickListener.onJobClick(job);
                }
            });
            
            // View button click listener
            btnView.setOnClickListener(v -> {
                // Navigate to JobDetailActivity
                Intent intent = new Intent(itemView.getContext(), com.example.projectprm392.activities.JobDetailActivity.class);
                intent.putExtra("JOB_ID", job.getId());
                itemView.getContext().startActivity(intent);
            });
            
            // Save button click listener
            btnSave.setOnClickListener(v -> {
                if (onJobClickListener != null) {
                    onJobClickListener.onSaveClick(job);
                }
            });
            
            // Contact button click listener
            btnContact.setOnClickListener(v -> {
                if (onJobClickListener != null) {
                    onJobClickListener.onContactClick(job);
                }
            });
        }
    }
}
