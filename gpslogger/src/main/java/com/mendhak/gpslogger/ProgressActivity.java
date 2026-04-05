package com.mendhak.gpslogger;

import android.os.Bundle;
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
        setContentView(R.layout.activity_progress);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title == null) title = getString(R.string.uploading_data);

        ((TextView) findViewById(R.id.progress_message)).setText(title);
        findViewById(R.id.progress_dismiss).setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (instance == this) instance = null;
    }
}
