package com.example.projectprm392.adapters.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.database.UserEntity;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onUserClick(UserEntity user);
        void onEditUser(UserEntity user);
        void onDeleteUser(UserEntity user);
        void onToggleUserStatus(UserEntity user);
        void onChangeUserPassword(UserEntity user);
    }

    private Context context;
    private List<UserEntity> users;
    private List<UserEntity> filteredUsers;
    private List<Object> displayItems; // Mix of headers and users
    private OnUserActionListener listener;
    private SimpleDateFormat dateFormat;

    public UserManagementAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.users = new ArrayList<>();
        this.filteredUsers = new ArrayList<>();
        this.displayItems = new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserEntity user = (UserEntity) displayItems.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    public void updateUsers(List<UserEntity> newUsers) {
        this.users.clear();
        this.users.addAll(newUsers);
        this.filteredUsers.clear();
        // Filter out admin users from the beginning
        for (UserEntity user : newUsers) {
            if (!"ADMIN".equals(user.getRole())) {
                this.filteredUsers.add(user);
            }
        }
        rebuildDisplayItems();
    }

    public void filter(String query) {
        filteredUsers.clear();
        if (query.isEmpty()) {
            // Add all non-admin users
            for (UserEntity user : users) {
                if (!"ADMIN".equals(user.getRole())) {
                    filteredUsers.add(user);
                }
            }
        } else {
            String lowerQuery = query.toLowerCase();
            for (UserEntity user : users) {
                // Skip admin users and filter by query
                if (!"ADMIN".equals(user.getRole()) &&
                    (user.getFullName().toLowerCase().contains(lowerQuery) ||
                     user.getEmail().toLowerCase().contains(lowerQuery) ||
                     user.getRole().toLowerCase().contains(lowerQuery))) {
                    filteredUsers.add(user);
                }
            }
        }
        rebuildDisplayItems();
    }

    public void filterByRole(String filterType) {
        filteredUsers.clear();
        
        for (UserEntity user : users) {
            // Skip admin users for all filters
            if ("ADMIN".equals(user.getRole())) {
                continue;
            }
            
            boolean shouldInclude = false;
            
            switch (filterType) {
                case "ALL":
                    shouldInclude = true;
                    break;
                case "EMPLOYER":
                case "WORKER":
                    shouldInclude = user.getRole().equals(filterType);
                    break;
                case "LOCKED":
                    shouldInclude = !user.getIsActive(); // Tài khoản đã khóa
                    break;
                case "UNVERIFIED":
                    shouldInclude = !user.getIsVerified(); // Tài khoản chưa xác thực
                    break;
                default:
                    shouldInclude = true;
                    break;
            }
            
            if (shouldInclude) {
                filteredUsers.add(user);
            }
        }
        rebuildDisplayItems();
    }

    private void rebuildDisplayItems() {
        displayItems.clear();
        
        // Add all filtered users (already excluding admins)
        displayItems.addAll(filteredUsers);
        
        notifyDataSetChanged();
    }

    public List<UserEntity> getFilteredUsers() {
        return filteredUsers;
    }

    // User ViewHolder
    class UserViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvUserName, tvUserEmail, tvUserRole, tvVerificationStatus;
        private TextView tvUserPhone, tvUserGender, tvUserCreatedAt, tvUserPostQuota;
        private View viewStatus, layoutExpandedDetails;
        private ImageButton btnUserMenu;
        private MaterialButton btnEditUser, btnToggleStatus, btnDeleteUser;
        private boolean isExpanded = false;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = (CardView) itemView; // The itemView itself is the CardView
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvVerificationStatus = itemView.findViewById(R.id.tvVerificationStatus);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            tvUserGender = itemView.findViewById(R.id.tvUserGender);
            tvUserCreatedAt = itemView.findViewById(R.id.tvUserCreatedAt);
            tvUserPostQuota = itemView.findViewById(R.id.tvUserPostQuota);
            viewStatus = itemView.findViewById(R.id.viewStatus);
            layoutExpandedDetails = itemView.findViewById(R.id.layoutExpandedDetails);
            btnUserMenu = itemView.findViewById(R.id.btnUserMenu);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnToggleStatus = itemView.findViewById(R.id.btnToggleStatus);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }

        public void bind(UserEntity user) {
            // Basic info
            tvUserName.setText(user.getFullName());
            tvUserEmail.setText(user.getEmail());
            
            // Role badge
            tvUserRole.setText(user.getRole());
            switch (user.getRole()) {
                case "ADMIN":
                    tvUserRole.setBackgroundResource(R.drawable.badge_role_admin);
                    break;
                case "EMPLOYER":
                    tvUserRole.setBackgroundResource(R.drawable.badge_role_employer);
                    break;
                case "WORKER":
                default:
                    tvUserRole.setBackgroundResource(R.drawable.badge_role_worker);
                    break;
            }

            // Verification status
            if (user.getIsVerified()) {
                tvVerificationStatus.setText("Đã xác thực");
                tvVerificationStatus.setBackgroundResource(R.drawable.badge_verified);
            } else {
                tvVerificationStatus.setText("Chưa xác thực");
                tvVerificationStatus.setBackgroundResource(R.drawable.badge_unverified);
            }

            // Status indicator
            if (user.getIsActive()) {
                viewStatus.setBackgroundResource(R.drawable.circle_green);
            } else {
                viewStatus.setBackgroundResource(R.drawable.circle_red);
            }

            // Extended details
            tvUserPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa có");
            
            if (user.getGender() != null) {
                tvUserGender.setText(user.getGender() ? "Nam" : "Nữ");
            } else {
                tvUserGender.setText("Chưa xác định");
            }
            
            if (user.getCreatedAt() != null) {
                tvUserCreatedAt.setText(dateFormat.format(user.getCreatedAt()));
            } else {
                tvUserCreatedAt.setText("Không rõ");
            }

            // Post quota (only for employers)
            if ("EMPLOYER".equals(user.getRole())) {
                int postQuota = user.getPostQuota() != null ? user.getPostQuota() : 0;
                tvUserPostQuota.setText("0/" + postQuota);
            } else {
                tvUserPostQuota.setText("N/A");
            }

            // Toggle status button
            if (user.getIsActive()) {
                btnToggleStatus.setText("Khóa");
                btnToggleStatus.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_block));
            } else {
                btnToggleStatus.setText("Mở khóa");
                btnToggleStatus.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_check));
            }

            // Click listeners
            cardView.setOnClickListener(v -> {
                toggleExpanded();
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });

            btnUserMenu.setOnClickListener(v -> showUserMenu(v, user));
            btnEditUser.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditUser(user);
                }
            });
            btnToggleStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleUserStatus(user);
                }
            });
            btnDeleteUser.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUser(user);
                }
            });
        }

        private void toggleExpanded() {
            isExpanded = !isExpanded;
            layoutExpandedDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

        private void showUserMenu(View view, UserEntity user) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.user_item_menu);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit_user) {
                    if (listener != null) listener.onEditUser(user);
                    return true;
                } else if (id == R.id.action_change_password) {
                    if (listener != null) listener.onChangeUserPassword(user);
                    return true;
                } else if (id == R.id.action_toggle_status) {
                    if (listener != null) listener.onToggleUserStatus(user);
                    return true;
                } else if (id == R.id.action_delete_user) {
                    if (listener != null) listener.onDeleteUser(user);
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }
}
