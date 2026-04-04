package com.mendhak.gpslogger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;

public class SettingsMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_settings_menu);

        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.header_title)).setText(R.string.settings_screen_name);

        setupItems();
    }

    private void setupItems() {
        item(R.id.settings_item_general,     MainPreferenceActivity.PREFERENCE_FRAGMENTS.GENERAL);
        item(R.id.settings_item_logging,     MainPreferenceActivity.PREFERENCE_FRAGMENTS.LOGGING);
        item(R.id.settings_item_performance, MainPreferenceActivity.PREFERENCE_FRAGMENTS.PERFORMANCE);

        findViewById(R.id.settings_item_senders).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsSendersActivity.class)));
        findViewById(R.id.settings_item_other).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsOtherActivity.class)));
    }

    private void item(int viewId, String fragment) {
        findViewById(viewId).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainPreferenceActivity.class);
            intent.putExtra("preference_fragment", fragment);
            startActivity(intent);
        });
    }
}
