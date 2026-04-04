package com.mendhak.gpslogger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;
import com.mendhak.gpslogger.common.events.CommandEvents;

import de.greenrobot.event.EventBus;

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
        item(R.id.settings_item_autosend,    MainPreferenceActivity.PREFERENCE_FRAGMENTS.UPLOAD);
        item(R.id.settings_item_customurl,   MainPreferenceActivity.PREFERENCE_FRAGMENTS.CUSTOMURL);
        item(R.id.settings_item_dropbox,     MainPreferenceActivity.PREFERENCE_FRAGMENTS.DROPBOX);
        item(R.id.settings_item_googledrive, MainPreferenceActivity.PREFERENCE_FRAGMENTS.GOOGLEDRIVE);
        item(R.id.settings_item_sftp,        MainPreferenceActivity.PREFERENCE_FRAGMENTS.SFTP);
        item(R.id.settings_item_opengts,     MainPreferenceActivity.PREFERENCE_FRAGMENTS.OPENGTS);
        item(R.id.settings_item_osm,         MainPreferenceActivity.PREFERENCE_FRAGMENTS.OSM);
        item(R.id.settings_item_email,       MainPreferenceActivity.PREFERENCE_FRAGMENTS.EMAIL);
        item(R.id.settings_item_owncloud,    MainPreferenceActivity.PREFERENCE_FRAGMENTS.OWNCLOUD);
        item(R.id.settings_item_ftp,         MainPreferenceActivity.PREFERENCE_FRAGMENTS.FTP);

        // findViewById(R.id.settings_item_faq).setOnClickListener(v ->
        //         startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://gpslogger.app"))));

        findViewById(R.id.settings_item_exit).setOnClickListener(v -> {
            EventBus.getDefault().post(new CommandEvents.RequestStartStop(false));
            finishAffinity();
        });
    }

    private void item(int viewId, String fragment) {
        findViewById(viewId).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainPreferenceActivity.class);
            intent.putExtra("preference_fragment", fragment);
            startActivity(intent);
        });
    }
}
