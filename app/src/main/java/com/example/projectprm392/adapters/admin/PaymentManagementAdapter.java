package com.example.projectprm392.adapters.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.database.PaymentEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentManagementAdapter extends RecyclerView.Adapter<PaymentManagementAdapter.PaymentViewHolder> {

    private Context context;
    private List<PaymentEntity> allPayments;
    private List<PaymentEntity> filteredPayments;
    private OnPaymentActionListener listener;

    public interface OnPaymentActionListener {
        void onPaymentClick(PaymentEntity payment);
        void onApprovePayment(PaymentEntity payment);
        void onRejectPayment(PaymentEntity payment);
        void onDeletePayment(PaymentEntity payment);
    }

    public PaymentManagementAdapter(Context context, OnPaymentActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.allPayments = new ArrayList<>();
        this.filteredPayments = new ArrayList<>();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_management, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentEntity payment = filteredPayments.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return filteredPayments.size();
    }

    public void updatePayments(List<PaymentEntity> payments) {
        this.allPayments.clear();
        this.allPayments.addAll(payments);
        this.filteredPayments.clear();
        this.filteredPayments.addAll(payments);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredPayments.clear();
        
        if (query.isEmpty()) {
            filteredPayments.addAll(allPayments);
        } else {
            String lowerQuery = query.toLowerCase();
            for (PaymentEntity payment : allPayments) {
                if (payment.getPaymentId().toLowerCase().contains(lowerQuery) ||
                    payment.getUserName().toLowerCase().contains(lowerQuery) ||
                    payment.getUserEmail().toLowerCase().contains(lowerQuery) ||
                    payment.getPackageName().toLowerCase().contains(lowerQuery) ||
                    payment.getTransactionId().toLowerCase().contains(lowerQuery)) {
                    filteredPayments.add(payment);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void applyFilters(String statusFilter, String methodFilter) {
        filteredPayments.clear();
        
        for (PaymentEntity payment : allPayments) {
            boolean matchStatus = "ALL".equals(statusFilter) || payment.getStatus().equals(statusFilter);
            boolean matchMethod = "ALL".equals(methodFilter) || payment.getPaymentMethod().equals(methodFilter);
            
            if (matchStatus && matchMethod) {
                filteredPayments.add(payment);
            }
        }
        notifyDataSetChanged();
    }

    public List<PaymentEntity> getFilteredPayments() {
        return new ArrayList<>(filteredPayments);
    }

    public List<PaymentEntity> getAllPayments() {
        return new ArrayList<>(allPayments);
    }

    class PaymentViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardPayment;
        private ImageView ivStatus;
        private TextView tvPaymentId, tvUserName, tvUserEmail, tvPackage, tvQuota, tvPrice;
        private TextView tvMethod, tvStatus, tvDate, tvTransactionId;
        private MaterialButton btnApprove, btnReject, btnDelete;
        private View layoutActions;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPayment = itemView.findViewById(R.id.cardPayment);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvPaymentId = itemView.findViewById(R.id.tvPaymentId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvPackage = itemView.findViewById(R.id.tvPackage);
            tvQuota = itemView.findViewById(R.id.tvQuota);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvMethod = itemView.findViewById(R.id.tvMethod);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            layoutActions = itemView.findViewById(R.id.layoutActions);
        }

        public void bind(PaymentEntity payment) {
            // Basic info
            tvPaymentId.setText(payment.getPaymentId());
            tvUserName.setText(payment.getUserName());
            tvUserEmail.setText(payment.getUserEmail());
            tvPackage.setText(payment.getPackageName());
            tvQuota.setText(payment.getQuotaAmount() + " quota");
            tvPrice.setText(payment.getFormattedPrice());
            tvMethod.setText(payment.getPaymentMethodText());
            tvStatus.setText(payment.getStatusText());
            tvDate.setText(formatDate(payment.getCreatedAt()));
            
            if (payment.getTransactionId() != null) {
                tvTransactionId.setText(payment.getTransactionId());
                tvTransactionId.setVisibility(View.VISIBLE);
            } else {
                tvTransactionId.setVisibility(View.GONE);
            }

            // Status styling
            setupStatusDisplay(payment);
            
            // Action buttons
            setupActionButtons(payment);

            // Click listener
            cardPayment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentClick(payment);
                }
            });
        }

        private void setupStatusDisplay(PaymentEntity payment) {
            String status = payment.getStatus();
            int statusColor;
            int statusIcon;
            
            switch (status) {
                case "PENDING":
                    statusColor = ContextCompat.getColor(context, R.color.warning);
                    statusIcon = R.drawable.ic_pending;
                    break;
                case "COMPLETED":
                    statusColor = ContextCompat.getColor(context, R.color.success);
                    statusIcon = R.drawable.ic_check_circle;
                    break;
                case "CANCELLED":
                    statusColor = ContextCompat.getColor(context, R.color.error);
                    statusIcon = R.drawable.ic_cancel;
                    break;
                case "FAILED":
                    statusColor = ContextCompat.getColor(context, R.color.error);
                    statusIcon = R.drawable.ic_error;
                    break;
                default:
                    statusColor = ContextCompat.getColor(context, R.color.text_secondary);
                    statusIcon = R.drawable.ic_help;
                    break;
            }
            
            tvStatus.setTextColor(statusColor);
            ivStatus.setImageResource(statusIcon);
            ivStatus.setColorFilter(statusColor);
        }

        private void setupActionButtons(PaymentEntity payment) {
            String status = payment.getStatus();
            
            if ("PENDING".equals(status)) {
                // Show approve and reject buttons for pending payments
                layoutActions.setVisibility(View.VISIBLE);
                btnApprove.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                
                btnApprove.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onApprovePayment(payment);
                    }
                });
                
                btnReject.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRejectPayment(payment);
                    }
                });
                
            } else {
                // Only show delete button for non-pending payments
                layoutActions.setVisibility(View.VISIBLE);
                btnApprove.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
                btnDelete.setVisibility(View.VISIBLE);
            }
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeletePayment(payment);
                }
            });
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
