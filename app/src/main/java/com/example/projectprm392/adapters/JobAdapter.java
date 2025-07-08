package com.example.projectprm392.adapters;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.models.Job;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobs;
    private OnJobClickListener onJobClickListener;
    private Location userLocation;

    public interface OnJobClickListener {
        void onJobClick(Job job);
    }

    public JobAdapter(List<Job> jobs, OnJobClickListener listener) {
        this.jobs = jobs;
        this.onJobClickListener = listener;
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
        Job job = jobs.get(position);
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
        private MaterialButton btnApply;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvNeededAmount = itemView.findViewById(R.id.tvNeededAmount);
            tvWorkingTime = itemView.findViewById(R.id.tvWorkingTime);
            btnApply = itemView.findViewById(R.id.btnApply);
        }

        public void bind(Job job) {
            tvTitle.setText(job.getTitle());
            tvDescription.setText(job.getDescription());
            
            // Format salary
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String salaryText = currencyFormat.format(job.getSalary()) + "/" + getSalaryUnitText(job.getSalaryUnit());
            tvSalary.setText(salaryText);
            
            // Location with real distance calculation
            if (job.getLocationLatitude() != null && job.getLocationLongitude() != null && userLocation != null) {
                Location jobLocation = new Location("job");
                jobLocation.setLatitude(job.getLocationLatitude());
                jobLocation.setLongitude(job.getLocationLongitude());
                
                float distance = userLocation.distanceTo(jobLocation) / 1000; // Convert to km
                tvLocation.setText(String.format("Cách bạn ~%.1fkm", distance));
            } else {
                tvLocation.setText("Vị trí chưa xác định");
            }
            
            // Needed amount
            tvNeededAmount.setText("Cần " + job.getNeededAmount() + " người");
            
            // Working time
            if (job.getStartTime() != null && job.getEndTime() != null) {
                tvWorkingTime.setText(job.getStartTime() + " - " + job.getEndTime());
            } else {
                tvWorkingTime.setText("Linh hoạt");
            }
            
            // Card click listener
            cardView.setOnClickListener(v -> {
                if (onJobClickListener != null) {
                    onJobClickListener.onJobClick(job);
                }
            });
            
            // Apply button click listener
            btnApply.setOnClickListener(v -> {
                // TODO: Implement apply logic
                btnApply.setText("Đã ứng tuyển");
                btnApply.setEnabled(false);
            });
        }
        
        private String getSalaryUnitText(Job.SalaryUnit unit) {
            switch (unit) {
                case HOUR:
                    return "giờ";
                case DAY:
                    return "ngày";
                case PACKAGE:
                    return "gói";
                default:
                    return "giờ";
            }
        }
    }
}
