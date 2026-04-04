package com.mendhak.gpslogger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.PreferenceNames;
import com.mendhak.gpslogger.common.Strings;
import com.mendhak.gpslogger.common.Systems;
import com.mendhak.gpslogger.ui.Dialogs;

import java.util.Map;

public class SettingsGeneralActivity extends AppCompatActivity {

    private final PreferenceHelper prefs = PreferenceHelper.getInstance();

    private final ActivityResultLauncher<Intent> listLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
                        String key   = result.getData().getStringExtra(ListSelectionActivity.EXTRA_KEY);
                        String value = result.getData().getStringExtra(ListSelectionActivity.EXTRA_VALUE);
                        if (key == null || value == null) return;

                        switch (key) {
                            case PreferenceNames.LATLONG_DISPLAY_FORMAT:
                                prefs.setDisplayLatLongFormat(PreferenceNames.DegreesDisplayFormat.valueOf(value));
                                refreshSelector(R.id.gen_coord_format, coordLabel(value));
                                break;
                            case PreferenceNames.USER_SPECIFIED_LANGUAGE:
                                prefs.setUserSpecifiedLocale(value);
                                refreshSelector(R.id.gen_language, value);
                                break;
                            case PreferenceNames.APP_THEME_SETTING:
                                prefs.setAppThemeSetting(value);
                                refreshSelector(R.id.gen_theme, value);
                                Dialogs.alert("", getString(R.string.restart_required), this);
                                break;
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(prefs.getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_settings_general);

        ((TextView) findViewById(R.id.header_title)).setText(R.string.pref_general_title);
        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());

        setupCoordFormat();
        setupLanguage();
        setupTheme();
    }

    private void setupCoordFormat() {
        String current = prefs.getDisplayLatLongFormat().toString();
        setupListSelector(R.id.gen_coord_format, "Coordinate format",
            coordLabel(current),
            PreferenceNames.LATLONG_DISPLAY_FORMAT,
            new String[]{"12° 34' 56.7890\" S", "12° 34.5678' S", "-12.345678"},
            new String[]{
                PreferenceNames.DegreesDisplayFormat.DEGREES_MINUTES_SECONDS.toString(),
                PreferenceNames.DegreesDisplayFormat.DEGREES_DECIMAL_MINUTES.toString(),
                PreferenceNames.DegreesDisplayFormat.DECIMAL_DEGREES.toString()
            },
            current);
    }

    private void setupLanguage() {
        Map<String, String> localeMap = Strings.getAvailableLocales(this);
        String[] values  = localeMap.keySet().toArray(new String[0]);
        String[] entries = localeMap.values().toArray(new String[0]);
        String current   = prefs.getUserSpecifiedLocale();
        String display   = localeMap.containsKey(current) ? localeMap.get(current) : current;
        setupListSelector(R.id.gen_language, "Language", display,
            PreferenceNames.USER_SPECIFIED_LANGUAGE, entries, values, current);
    }

    private void setupTheme() {
        String[] entries = getResources().getStringArray(R.array.app_theme_options);
        String[] values  = getResources().getStringArray(R.array.app_theme_values);
        String current   = prefs.getAppThemeSetting();
        setupListSelector(R.id.gen_theme, "App theme", current,
            PreferenceNames.APP_THEME_SETTING, entries, values, current);
    }

    private void setupListSelector(int rootId, String label, String displayValue,
                                   String key, String[] entries, String[] values, String current) {
        View root = findViewById(rootId);
        ((TextView) root.findViewById(R.id.selector_label)).setText(label);
        ((TextView) root.findViewById(R.id.selector_value)).setText(displayValue);
        root.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListSelectionActivity.class);
            intent.putExtra(ListSelectionActivity.EXTRA_TITLE, label);
            intent.putExtra(ListSelectionActivity.EXTRA_KEY, key);
            intent.putExtra(ListSelectionActivity.EXTRA_ENTRIES, entries);
            intent.putExtra(ListSelectionActivity.EXTRA_VALUES, values);
            intent.putExtra(ListSelectionActivity.EXTRA_CURRENT_VALUE, current);
            listLauncher.launch(intent);
        });
    }

    private void refreshSelector(int rootId, String value) {
        ((TextView) findViewById(rootId).findViewById(R.id.selector_value)).setText(value);
    }

    private String coordLabel(String value) {
        switch (value) {
            case "DEGREES_MINUTES_SECONDS": return "12° 34' 56.7890\" S";
            case "DEGREES_DECIMAL_MINUTES": return "12° 34.5678' S";
            default:                        return "-12.345678";
        }
    }
}
