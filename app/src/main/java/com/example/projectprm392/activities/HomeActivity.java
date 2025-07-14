package com.example.projectprm392.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.projectprm392.R;
import com.example.projectprm392.fragments.FooterFragment;
import com.example.projectprm392.fragments.HeaderFragment;
import com.example.projectprm392.fragments.HomeBodyFragment;

public class HomeActivity extends AppCompatActivity implements HeaderFragment.OnSearchListener {

    private HeaderFragment headerFragment;
    private HomeBodyFragment homeBodyFragment;
    private FooterFragment footerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupFragments();
    }

    private void setupFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Header Fragment
        headerFragment = new HeaderFragment();
        headerFragment.setOnSearchListener(this);
        transaction.add(R.id.fragmentHeader, headerFragment);

        // Home Body Fragment
        homeBodyFragment = new HomeBodyFragment();
        transaction.add(R.id.fragmentHomeBody, homeBodyFragment);

        // Footer Fragment
        footerFragment = new FooterFragment();
        transaction.add(R.id.fragmentFooter, footerFragment);

        transaction.commit();
    }

    @Override
    public void onSearchQuery(String query) {
        if (homeBodyFragment != null) {
            homeBodyFragment.performSearch(query);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (headerFragment != null) {
            headerFragment.updateUI();
        }
        
        // Refresh home body data when returning to this activity
        if (homeBodyFragment != null) {
            homeBodyFragment.refreshData();
        }
    }
}
