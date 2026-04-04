package com.mendhak.gpslogger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;

public class SettingsSendersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_settings_senders);

        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.header_title)).setText("Senders");

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
    }

    private void item(int viewId, String fragment) {
        findViewById(viewId).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainPreferenceActivity.class);
            intent.putExtra("preference_fragment", fragment);
            startActivity(intent);
        });
    }
}
