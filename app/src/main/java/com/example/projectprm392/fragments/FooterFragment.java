package com.example.projectprm392.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectprm392.R;

public class FooterFragment extends Fragment {

    private TextView tvCopyright;
    private TextView tvPrivacyPolicy;
    private TextView tvTermsOfService;
    private TextView tvContact;
    private TextView tvSupport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_footer, container, false);
        
        initViews(view);
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        tvCopyright = view.findViewById(R.id.tvCopyright);
        tvPrivacyPolicy = view.findViewById(R.id.tvPrivacyPolicy);
        tvTermsOfService = view.findViewById(R.id.tvTermsOfService);
        tvContact = view.findViewById(R.id.tvContact);
        tvSupport = view.findViewById(R.id.tvSupport);
        
        // Set copyright text
        tvCopyright.setText("© 2025 Việc làm gần tôi. Tất cả quyền được bảo lưu.");
    }

    private void setupListeners() {
        tvPrivacyPolicy.setOnClickListener(v -> {
            // TODO: Open privacy policy page
            openWebPage("https://example.com/privacy-policy");
        });
        
        tvTermsOfService.setOnClickListener(v -> {
            // TODO: Open terms of service page
            openWebPage("https://example.com/terms-of-service");
        });
        
        tvContact.setOnClickListener(v -> {
            // Open email client
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:contact@vieclamgantoi.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Liên hệ từ ứng dụng");
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        });
        
        tvSupport.setOnClickListener(v -> {
            // Open email client for support
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@vieclamgantoi.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Yêu cầu hỗ trợ");
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        });
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
