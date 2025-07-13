package com.example.projectprm392.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.database.ApplicationEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;

import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private List<ApplicationEntity> applications;
    private DatabaseHelper databaseHelper;

    public ApplicationAdapter(DatabaseHelper databaseHelper, List<ApplicationEntity> applications) {
        this.databaseHelper = databaseHelper;
        this.applications = applications;
    }

    @NonNull
    @Override
    public ApplicationAdapter.ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ApplicationAdapter.ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationAdapter.ApplicationViewHolder holder, int position) {
        ApplicationEntity application = applications.get(position);
        holder.bind(application);
    }

    public void updateApplications(List<ApplicationEntity> newApplications) {
        this.applications = newApplications;
        notifyDataSetChanged();
    }

    public class ApplicationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJobName, tvUser, tvMessage, tvStatus;
        private Button btnApprove, btnReject;

        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobName = itemView.findViewById(R.id.tvJobName);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        public void bind(ApplicationEntity application) {

            // Lấy tên công việc
            JobEntity job = databaseHelper.getJobById(application.getJobId());
            tvJobName.setText(job != null ? job.getTitle() : "Unknown Job");

            // Lấy tên người nộp đơn
            UserEntity user = databaseHelper.getUserById(application.getUserId());
            tvUser.setText(user != null ? user.getFullName() : "Unknown User");

            tvMessage.setText(application.getMessage());
            tvStatus.setText(application.getStatus());

            btnApprove.setEnabled(!application.getStatus().equalsIgnoreCase("Approved"));
            btnReject.setEnabled(!application.getStatus().equalsIgnoreCase("Rejected"));

            btnApprove.setOnClickListener(v -> {
                int jobId = application.getJobId();
                JobEntity jobFind = databaseHelper.getJobById(jobId);
                int maxUsers = jobFind.getNeededAmount();
                int countApproved = databaseHelper.getApplicationsByJobIdAndStatus(jobId, "Approved");
                if (countApproved >= maxUsers) {
                    btnApprove.setEnabled(false);
                    Toast.makeText(itemView.getContext(), "Vượt quá số lượng người cần tuyển", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                // Chỉ approve nếu chưa vượt quá số lượng
                application.setStatus("Approved");
                databaseHelper.updateApplication(application);
                notifyItemChanged(getAdapterPosition());
            });

            btnReject.setOnClickListener(v -> {
                application.setStatus("Rejected");
                databaseHelper.updateApplication(application);
                notifyItemChanged(getAdapterPosition());
            });
        }
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }
}
