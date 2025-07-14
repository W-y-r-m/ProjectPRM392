package com.example.projectprm392.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import androidx.annotation.NonNull;
import com.example.projectprm392.R;

public class ReviewDialog extends Dialog {
    private EditText etComment;
    private RatingBar ratingBar;
    private Button btnSave;
    private OnReviewListener listener;

    public interface OnReviewListener {
        void onReviewSaved(String comment, float rating);
    }

    public ReviewDialog(@NonNull Context context, OnReviewListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_review);
        etComment = findViewById(R.id.etComment);
        ratingBar = findViewById(R.id.ratingBar);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            String comment = etComment.getText().toString();
            float rating = ratingBar.getRating();
            if (listener != null) {
                listener.onReviewSaved(comment, rating);
            }
            dismiss();
        });
    }
}
