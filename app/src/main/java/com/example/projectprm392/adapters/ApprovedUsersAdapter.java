package com.example.projectprm392.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.database.AppDatabase;
import com.example.projectprm392.database.ReviewEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.User;
import com.example.projectprm392.ui.ReviewDialog;

import java.util.List;

public class ApprovedUsersAdapter extends RecyclerView.Adapter<ApprovedUsersAdapter.ViewHolder> {
    private List<UserEntity> userList;
    private String jobId;
    private Context context;

    public ApprovedUsersAdapter(List<UserEntity> userList, String jobId) {
        this.userList = userList;
        this.jobId = jobId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_approved_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserEntity user = userList.get(position);
        holder.tvUserName.setText(user.getFullName());
        // Xử lý nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            // TODO: Xóa user khỏi job (API hoặc local)
        });
        // Xử lý nút đánh giá
        holder.btnReview.setOnClickListener(v -> {
            ReviewDialog dialog = new ReviewDialog(context, (comment, rating) -> {
                // Lưu review vào database
                ReviewEntity review = new ReviewEntity(
                        user.getId(), // userId
                        Integer.parseInt(jobId), // jobId
                        comment,
                        rating
                );
                AppDatabase db = AppDatabase.getDatabase(context);
                db.reviewDao().insertReview(review);
            });
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        Button btnDelete, btnReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}
