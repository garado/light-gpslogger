package com.mendhak.gpslogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;

public class UploadActivity extends AppCompatActivity {

    public static final String EXTRA_ACTION = "UPLOAD_ACTION";

    public static final String ACTION_AUTO_SEND_NOW = "AUTO_SEND_NOW";
    public static final String ACTION_CUSTOM_URL    = "CUSTOM_URL";
    public static final String ACTION_DROPBOX       = "DROPBOX";
    public static final String ACTION_GOOGLE_DRIVE  = "GOOGLE_DRIVE";
    public static final String ACTION_SFTP          = "SFTP";
    public static final String ACTION_OPEN_GTS      = "OPEN_GTS";
    public static final String ACTION_OSM           = "OSM";
    public static final String ACTION_EMAIL         = "EMAIL";
    public static final String ACTION_OWN_CLOUD     = "OWN_CLOUD";
    public static final String ACTION_FTP           = "FTP";

    private static final String[] LABELS  = {
        "Auto Send Now",
        "Custom URL",
        "Dropbox",
        "Google Drive",
        "SFTP",
        "OpenGTS",
        "OpenStreetMap",
        "Email",
        "ownCloud",
        "FTP",
    };

    private static final String[] ACTIONS = {
        ACTION_AUTO_SEND_NOW,
        ACTION_CUSTOM_URL,
        ACTION_DROPBOX,
        ACTION_GOOGLE_DRIVE,
        ACTION_SFTP,
        ACTION_OPEN_GTS,
        ACTION_OSM,
        ACTION_EMAIL,
        ACTION_OWN_CLOUD,
        ACTION_FTP,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_upload);

        ((TextView) findViewById(R.id.header_title)).setText("Upload");
        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());

        LinearLayout container = findViewById(R.id.content_inner);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < LABELS.length; i++) {
            TextView btn = (TextView) inflater.inflate(R.layout.component_styled_button, container, false);
            btn.setText(LABELS[i]);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = (int) (15 * getResources().getDisplayMetrics().density);
            btn.setLayoutParams(params);

            final String action = ACTIONS[i];
            btn.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra(EXTRA_ACTION, action);
                setResult(Activity.RESULT_OK, result);
                finish();
            });

            container.addView(btn);
        }
    }
}
