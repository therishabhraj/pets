package com.example.pets;

import androidx.appcompat.app.AppCompatActivity;
import com.example.android.pets.R;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class DeveloperActivity extends AppCompatActivity {

    LinearLayout developer_comment_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        setTitle("About Developer");

        developer_comment_layout = findViewById(R.id.developer_comment_layout);
        developer_comment_layout.setVisibility(View.INVISIBLE);
    }
}