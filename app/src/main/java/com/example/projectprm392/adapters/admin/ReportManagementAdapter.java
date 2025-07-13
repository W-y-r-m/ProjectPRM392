package com.example.projectprm392.adapters.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.database.ReportEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportManagementAdapter extends RecyclerView.Adapter<ReportManagementAdapter.ReportViewHolder> {

    private List<ReportEntity> allReports = new ArrayList<>();
    private List<ReportEntity> filteredReports = new ArrayList<>();
    private OnReportActionListener listener;
    private DatabaseHelper databaseHelper;

    public interface OnReportActionListener {
        void onReportClick(ReportEntity report);
        void onResolveReport(ReportEntity report);
        void onRejectReport(ReportEntity report);
        void onDeleteReport(ReportEntity report);
    }

    public ReportManagementAdapter(OnReportActionListener listener) {
        this.listener = listener;
        this.databaseHelper = new DatabaseHelper(((android.content.Context) listener));
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_management, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportEntity report = filteredReports.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return filteredReports.size();
    }

    public void updateReports(List<ReportEntity> reports) {
        this.allReports.clear();
        this.allReports.addAll(reports);
        this.filteredReports.clear();
        this.filteredReports.addAll(reports);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredReports.clear();
        if (query.isEmpty()) {
            filteredReports.addAll(allReports);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (ReportEntity report : allReports) {
                // Search in content, reporter name, or target user name
                UserEntity reporter = databaseHelper.getUserById(report.reporterId);
                UserEntity targetUser = databaseHelper.getUserById(report.targetUserId);
                
                String reporterName = reporter != null ? reporter.getFullName().toLowerCase() : "";
                String targetName = targetUser != null ? targetUser.getFullName().toLowerCase() : "";
                String content = report.content != null ? report.content.toLowerCase() : "";
                
                if (content.contains(lowerCaseQuery) || 
                    reporterName.contains(lowerCaseQuery) || 
                    targetName.contains(lowerCaseQuery)) {
                    filteredReports.add(report);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void applyFilter(String filterType) {
        filteredReports.clear();
        for (ReportEntity report : allReports) {
            switch (filterType) {
                case "ALL":
                    filteredReports.add(report);
                    break;
                case "PENDING":
                    if ("pending".equals(report.status)) {
                        filteredReports.add(report);
                    }
                    break;
                case "RESOLVED":
                    if ("resolved".equals(report.status)) {
                        filteredReports.add(report);
                    }
                    break;
                case "REJECTED":
                    if ("rejected".equals(report.status)) {
                        filteredReports.add(report);
                    }
                    break;
            }
        }
        notifyDataSetChanged();
    }

    public List<ReportEntity> getFilteredReports() {
        return new ArrayList<>(filteredReports);
    }

    public List<ReportEntity> getAllReports() {
        return new ArrayList<>(allReports);
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvReporterName, tvTargetName, tvContent, tvCreatedAt, tvStatus;
        private ImageView ivStatusIcon;
        private MaterialButton btnResolve, btnReject, btnDelete;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvReporterName = itemView.findViewById(R.id.tvReporterName);
            tvTargetName = itemView.findViewById(R.id.tvTargetName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            btnResolve = itemView.findViewById(R.id.btnResolve);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(ReportEntity report) {
            // Get user info
            UserEntity reporter = databaseHelper.getUserById(report.reporterId);
            UserEntity targetUser = databaseHelper.getUserById(report.targetUserId);

            String reporterName = reporter != null ? reporter.getFullName() : "Không xác định";
            String targetName = targetUser != null ? targetUser.getFullName() : "Không xác định";

            tvReporterName.setText("Người báo cáo: " + reporterName);
            tvTargetName.setText("Người bị báo cáo: " + targetName);
            
            // Show truncated content
            String content = report.content;
            if (content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            tvContent.setText(content);

            // Format date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvCreatedAt.setText(sdf.format(new Date(report.createdAt)));

            // Set status and icon
            setStatusDisplay(report.status);

            // Set button visibility based on status
            if ("pending".equals(report.status)) {
                btnResolve.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
            } else {
                btnResolve.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
            }

            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReportClick(report);
                }
            });

            btnResolve.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onResolveReport(report);
                }
            });

            btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectReport(report);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteReport(report);
                }
            });
        }

        private void setStatusDisplay(String status) {
            switch (status) {
                case "pending":
                    tvStatus.setText("Chờ xử lý");
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.warning));
                    ivStatusIcon.setImageResource(R.drawable.ic_pending);
                    ivStatusIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.warning));
                    break;
                case "resolved":
                    tvStatus.setText("Đã giải quyết");
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.success));
                    ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                    ivStatusIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.success));
                    break;
                case "rejected":
                    tvStatus.setText("Đã từ chối");
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.error));
                    ivStatusIcon.setImageResource(R.drawable.ic_cancel);
                    ivStatusIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.error));
                    break;
                default:
                    tvStatus.setText("Không xác định");
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary));
                    ivStatusIcon.setImageResource(R.drawable.ic_help);
                    ivStatusIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.text_secondary));
                    break;
            }
        }
    }
}
