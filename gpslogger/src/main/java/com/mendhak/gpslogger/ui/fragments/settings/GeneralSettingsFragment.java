/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mendhak.gpslogger.ui.fragments.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.mendhak.gpslogger.ListSelectionActivity;
import com.mendhak.gpslogger.R;
import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.PreferenceNames;
import com.mendhak.gpslogger.common.Strings;
import com.mendhak.gpslogger.common.network.ConscryptProviderInstaller;
import com.mendhak.gpslogger.common.slf4j.Logs;
import com.mendhak.gpslogger.loggers.Files;
import com.mendhak.gpslogger.ui.Dialogs;

import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

import eltos.simpledialogfragment.SimpleDialog;


public class GeneralSettingsFragment extends PreferenceFragmentCompat implements
        SimpleDialog.OnDialogResultListener,
        Preference.OnPreferenceClickListener {

    Logger LOG = Logs.of(GeneralSettingsFragment.class);

    private final ActivityResultLauncher<Intent> listLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                        String key   = result.getData().getStringExtra(ListSelectionActivity.EXTRA_KEY);
                        String value = result.getData().getStringExtra(ListSelectionActivity.EXTRA_VALUE);
                        if (key == null || value == null) return;
                        switch (key) {
                            case "coordinatedisplayformat":
                                PreferenceHelper.getInstance().setDisplayLatLongFormat(
                                    PreferenceNames.DegreesDisplayFormat.valueOf(value));
                                updateCoordSummary();
                                break;
                            case "changelanguage":
                                PreferenceHelper.getInstance().setUserSpecifiedLocale(value);
                                break;
                            case PreferenceNames.APP_THEME_SETTING:
                                PreferenceHelper.getInstance().setAppThemeSetting(value);
                                Dialogs.alert("", getString(R.string.restart_required), getActivity());
                                break;
                        }
                    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findPreference("enableDisableGps").setOnPreferenceClickListener(this);
        findPreference("debuglogtoemail").setOnPreferenceClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            findPreference("resetapp").setOnPreferenceClickListener(this);
        } else {
            findPreference("resetapp").setEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SwitchPreferenceCompat hideNotificationPreference = findPreference("hide_notification_from_status_bar");
            hideNotificationPreference.setEnabled(false);
            hideNotificationPreference.setDefaultValue(false);
            hideNotificationPreference.setChecked(false);
            hideNotificationPreference.setSummary(getString(R.string.hide_notification_from_status_bar_disallowed));
        }

        updateCoordSummary();
        setupCoordFormatClick();
        setupLanguageClick();
        setupThemeClick();

        Preference conscryptPreference = findPreference("install_conscrypt_provider");
        conscryptPreference.setEnabled(ConscryptProviderInstaller.shouldPromptUserForInstallation());
        conscryptPreference.setIntent(ConscryptProviderInstaller.getConscryptInstallationIntent(getActivity()));

        Preference aboutInfo = findPreference("about_version_info");
        try {
            aboutInfo.setTitle("GPSLogger version " + getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_general, rootKey);
    }

    private void updateCoordSummary() {
        String[] samples = {"12° 34' 56.7890\" S", "12° 34.5678' S", "-12.345678"};
        Preference pref = findPreference("coordinatedisplayformat");
        if (pref != null) {
            pref.setSummary(samples[PreferenceHelper.getInstance().getDisplayLatLongFormat().ordinal()]);
        }
    }

    private void setupCoordFormatClick() {
        Preference pref = findPreference("coordinatedisplayformat");
        if (pref == null) return;
        String[] samples = {"12° 34' 56.7890\" S", "12° 34.5678' S", "-12.345678"};
        String[] values  = {
            PreferenceNames.DegreesDisplayFormat.DEGREES_MINUTES_SECONDS.toString(),
            PreferenceNames.DegreesDisplayFormat.DEGREES_DECIMAL_MINUTES.toString(),
            PreferenceNames.DegreesDisplayFormat.DECIMAL_DEGREES.toString()
        };
        pref.setOnPreferenceClickListener(p -> {
            Intent intent = new Intent(getActivity(), ListSelectionActivity.class);
            intent.putExtra(ListSelectionActivity.EXTRA_TITLE, getString(R.string.coordinate_display_format));
            intent.putExtra(ListSelectionActivity.EXTRA_KEY, "coordinatedisplayformat");
            intent.putExtra(ListSelectionActivity.EXTRA_ENTRIES, samples);
            intent.putExtra(ListSelectionActivity.EXTRA_VALUES, values);
            intent.putExtra(ListSelectionActivity.EXTRA_CURRENT_VALUE,
                    PreferenceHelper.getInstance().getDisplayLatLongFormat().toString());
            listLauncher.launch(intent);
            return true;
        });
    }

    private void setupLanguageClick() {
        Preference pref = findPreference("changelanguage");
        if (pref == null) return;
        pref.setOnPreferenceClickListener(p -> {
            Map<String, String> localeMap = Strings.getAvailableLocales(getActivity());
            String[] locales      = localeMap.keySet().toArray(new String[0]);
            String[] displayNames = localeMap.values().toArray(new String[0]);
            Intent intent = new Intent(getActivity(), ListSelectionActivity.class);
            intent.putExtra(ListSelectionActivity.EXTRA_TITLE, getString(R.string.change_language_title));
            intent.putExtra(ListSelectionActivity.EXTRA_KEY, "changelanguage");
            intent.putExtra(ListSelectionActivity.EXTRA_ENTRIES, displayNames);
            intent.putExtra(ListSelectionActivity.EXTRA_VALUES, locales);
            intent.putExtra(ListSelectionActivity.EXTRA_CURRENT_VALUE,
                    PreferenceHelper.getInstance().getUserSpecifiedLocale());
            listLauncher.launch(intent);
            return true;
        });
    }

    private void setupThemeClick() {
        Preference pref = findPreference(PreferenceNames.APP_THEME_SETTING);
        if (pref == null) return;
        pref.setOnPreferenceClickListener(p -> {
            String[] entries = getResources().getStringArray(R.array.app_theme_options);
            String[] values  = getResources().getStringArray(R.array.app_theme_values);
            Intent intent = new Intent(getActivity(), ListSelectionActivity.class);
            intent.putExtra(ListSelectionActivity.EXTRA_TITLE, getString(R.string.app_theme_title));
            intent.putExtra(ListSelectionActivity.EXTRA_KEY, PreferenceNames.APP_THEME_SETTING);
            intent.putExtra(ListSelectionActivity.EXTRA_ENTRIES, entries);
            intent.putExtra(ListSelectionActivity.EXTRA_VALUES, values);
            intent.putExtra(ListSelectionActivity.EXTRA_CURRENT_VALUE,
                    PreferenceHelper.getInstance().getAppThemeSetting());
            listLauncher.launch(intent);
            return true;
        });
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference.getKey().equals("enableDisableGps")) {
            startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            return true;
        }

        if (preference.getKey().equals("resetapp")) {
            SimpleDialog.build()
                    .title(getString(R.string.reset_app_title))
                    .msgHtml(getString(R.string.reset_app_summary))
                    .neg(R.string.cancel)
                    .show(this, "RESET_APP");
            return true;
        }

        if (preference.getKey().equals("debuglogtoemail")) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "GPSLogger Debug Log");

            StringBuilder diagnostics = new StringBuilder();
            diagnostics.append("Android version: ").append(Build.VERSION.SDK_INT).append("\r\n");
            diagnostics.append("OS version: ").append(System.getProperty("os.version")).append("\r\n");
            diagnostics.append("Manufacturer: ").append(Build.MANUFACTURER).append("\r\n");
            diagnostics.append("Model: ").append(Build.MODEL).append("\r\n");
            diagnostics.append("Product: ").append(Build.PRODUCT).append("\r\n");
            diagnostics.append("Brand: ").append(Build.BRAND).append("\r\n");

            intent.putExtra(Intent.EXTRA_TEXT, diagnostics.toString());
            File root = Files.storageFolder(getActivity());
            File file = new File(root, "/debuglog.txt");
            if (file.exists() && file.canRead()) {
                Uri uri = Uri.parse("file://" + file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "Send debug log"));
            } else {
                Toast.makeText(getActivity(), "debuglog.txt not found", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (dialogTag.equalsIgnoreCase("RESET_APP") && which == BUTTON_POSITIVE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ((ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData();
            }
        }
        return false;
    }
}
