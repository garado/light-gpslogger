package com.mendhak.gpslogger;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;
import com.mendhak.gpslogger.common.events.CommandEvents;

import de.greenrobot.event.EventBus;

public class SettingsOtherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_settings_other);

        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.header_title)).setText("Other");

        findViewById(R.id.settings_item_exit).setOnClickListener(v -> {
            EventBus.getDefault().post(new CommandEvents.RequestStartStop(false));
            finishAffinity();
        });
    }
}
