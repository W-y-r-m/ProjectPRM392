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
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.User;

import java.util.List;

public class ApprovedUsersAdapter extends RecyclerView.Adapter<ApprovedUsersAdapter.ViewHolder> {
    private List<UserEntity> userList;
    private int jobId;
    private Context context;

    public ApprovedUsersAdapter(List<UserEntity> userList, int jobId) {
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
            // Hiển thị dialog đánh giá
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_review_user, null);
            builder.setView(dialogView);

            TextView tvUserNameDialog = dialogView.findViewById(R.id.tvUserNameDialog);
            tvUserNameDialog.setText(user.getFullName());
            android.widget.EditText etComment = dialogView.findViewById(R.id.etComment);
            android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
            Button btnSave = dialogView.findViewById(R.id.btnSaveReview);
            Button btnCancel = dialogView.findViewById(R.id.btnCancelReview);

            android.app.AlertDialog dialog = builder.create();

            btnSave.setOnClickListener(view -> {
                String comment = etComment.getText().toString();
                float rating = ratingBar.getRating();
                // Lưu review vào DB
                saveReviewToDb(user.getId(), jobId, comment, rating);
                dialog.dismiss();
            });
            btnCancel.setOnClickListener(view -> dialog.dismiss());

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

    // Hàm lưu review vào DB
    private void saveReviewToDb(int userId, int jobId, String comment, float rating) {
        new Thread(() -> {
            com.example.projectprm392.database.ReviewEntity review = new com.example.projectprm392.database.ReviewEntity(
                    userId, jobId, comment, rating);
            com.example.projectprm392.database.AppDatabase db = com.example.projectprm392.database.AppDatabase
                    .getDatabase(context);
            db.reviewDao().insert(review);
        }).start();
    }
}
