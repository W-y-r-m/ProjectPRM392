package com.example.projectprm392.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.database.ApplicationEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;

import java.util.List;

public class MyApplicationAdapter extends RecyclerView.Adapter<MyApplicationAdapter.MyApplicationViewHolder> {
    private List<ApplicationEntity> applications;
    private DatabaseHelper databaseHelper;

    public MyApplicationAdapter(DatabaseHelper databaseHelper, List<ApplicationEntity> applications) {
        this.databaseHelper = databaseHelper;
        this.applications = (applications != null) ? applications : new java.util.ArrayList<>();
    }

    @NonNull
    @Override
    public MyApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_application, parent, false);
        return new MyApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyApplicationViewHolder holder, int position) {
        if (applications != null && position < applications.size()) {
            ApplicationEntity application = applications.get(position);
            holder.bind(application);
        }
    }

    public void updateApplications(List<ApplicationEntity> newApplications) {
        this.applications = newApplications;
        notifyDataSetChanged();
    }

    public class MyApplicationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJobName, tvMessage, tvStatus;

        public MyApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobName = itemView.findViewById(R.id.tvJobName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(ApplicationEntity application) {
            if (application == null) {
                tvJobName.setText("Unknown Job");
                tvMessage.setText("");
                tvStatus.setText("");
                return;
            }
            JobEntity job = databaseHelper.getJobById(application.getJobId());
            tvJobName.setText(job != null ? job.getTitle() : "Unknown Job");
            tvMessage.setText(application.getMessage() != null ? application.getMessage() : "");
            tvStatus.setText(application.getStatus() != null ? application.getStatus() : "");
        }
    }

    @Override
    public int getItemCount() {
        return (applications != null) ? applications.size() : 0;
    }
}
