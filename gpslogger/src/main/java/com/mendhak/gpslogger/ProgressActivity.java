package com.mendhak.gpslogger;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;

public class ProgressActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "PROGRESS_TITLE";

    private static ProgressActivity instance;

    public static void dismiss() {
        if (instance != null) {
            instance.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_list_selection);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title == null) title = getString(R.string.please_wait);

        findViewById(R.id.header).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.header_title)).setText(title);
        findViewById(R.id.header_back_button).setVisibility(View.INVISIBLE);

        ImageButton hideBtn = findViewById(R.id.header_right_action);
        hideBtn.setImageResource(R.drawable.ic_arrow_back);
        hideBtn.setVisibility(View.VISIBLE);
        hideBtn.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (instance == this) instance = null;
    }
}
