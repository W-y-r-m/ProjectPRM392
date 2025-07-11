package com.example.projectprm392.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.projectprm392.R;
import com.example.projectprm392.activities.ApplicationActivity;
import com.example.projectprm392.activities.ProfileActivity;
import com.example.projectprm392.controllers.LoginController;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.SessionManager;
import com.example.projectprm392.views.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class HeaderFragment extends Fragment {

    private ImageView ivLogo;
    private SearchView searchView;
    private ImageView ivNotification;
    private ImageView ivProfile;
    private MaterialButton btnLogin;
    private TextView tvUserName;
    private ImageView ivUserMenu;
    private LoginController loginController;
    private SessionManager sessionManager;

    public interface OnSearchListener {
        void onSearchQuery(String query);
    }

    private OnSearchListener onSearchListener;

    public void setOnSearchListener(OnSearchListener listener) {
        this.onSearchListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header, container, false);
        
        initViews(view);
        setupController();
        setupListeners();
        updateUI();
        
        return view;
    }

    private void initViews(View view) {
        ivLogo = view.findViewById(R.id.ivLogo);
        searchView = view.findViewById(R.id.searchView);
        ivNotification = view.findViewById(R.id.ivNotification);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvUserName = view.findViewById(R.id.tvUserName);
        ivUserMenu = view.findViewById(R.id.ivUserMenu);
    }

    private void setupController() {
        loginController = new LoginController(requireContext());
        sessionManager = new SessionManager(requireContext());
    }

    private void setupListeners() {
        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (onSearchListener != null) {
                    onSearchListener.onSearchQuery(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Notification click
        ivNotification.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Thông báo", Toast.LENGTH_SHORT).show();
        });

        // Profile click
        ivProfile.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                showUserMenu(v);
            } else {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Login button
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
        });

        // User menu
        if (ivUserMenu != null) {
            ivUserMenu.setOnClickListener(this::showUserMenu);
        }
    }

    private void showUserMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_application){
                // Navigate to application
                Intent intent = new Intent(requireContext(), ApplicationActivity.class);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.menu_profile) {
                // Navigate to profile
                Intent intent = new Intent(requireContext(), ProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.menu_history) {
                // Navigate to history
                Toast.makeText(requireContext(), "Lịch sử", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    private void logout() {
        loginController.logout();
        updateUI();
        // Navigate to login or refresh main activity
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void updateUI() {
        if (loginController.isLoggedIn()) {
            String fullName = sessionManager.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                btnLogin.setVisibility(View.GONE);
                tvUserName.setVisibility(View.VISIBLE);
                ivUserMenu.setVisibility(View.VISIBLE);
                tvUserName.setText("Xin chào, " + fullName);
            }
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            tvUserName.setVisibility(View.GONE);
            ivUserMenu.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}
