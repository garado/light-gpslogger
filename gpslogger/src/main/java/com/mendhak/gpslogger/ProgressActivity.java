package com.mendhak.gpslogger;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;

public class ProgressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_list_selection);

        findViewById(R.id.header).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.header_title)).setText(R.string.please_wait);
        findViewById(R.id.header_back_button).setVisibility(View.INVISIBLE);

        // TODO: show progress content in content_inner

        // Hide button — dismisses the screen without cancelling the upload
        findViewById(R.id.header_right_action);
        // TODO: wire up hide button
    }
}
