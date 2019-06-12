package com.example.bizcardandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class AddUserActivity extends AppCompatActivity {

    private String code;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_add:
                    return true;
                case R.id.navigation_profile:
                    finish();
                    return true;
                case R.id.navigation_contacts:
                    Intent intent =
                            new Intent(AddUserActivity.this, ContactView.class);
                    startActivity(intent);
                    finish();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_add);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Intent passedIntent = getIntent();
        code = passedIntent.getStringExtra("code");
    }
}
