package com.mendhak.gpslogger;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.PreferenceNames;
import com.mendhak.gpslogger.common.Systems;

public class SettingsPerformanceActivity extends AppCompatActivity {

    private final PreferenceHelper prefs = PreferenceHelper.getInstance();

    private final ActivityResultLauncher<Intent> textLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
                        String key   = result.getData().getStringExtra(TextInputActivity.EXTRA_KEY);
                        String value = result.getData().getStringExtra(TextInputActivity.EXTRA_VALUE);
                        if (key == null || value == null) return;
                        try {
                            int intVal = Integer.parseInt(value.trim());
                            switch (key) {
                                case PreferenceNames.MINIMUM_INTERVAL:
                                    prefs.setMinimumLoggingInterval(intVal);
                                    refreshValue(R.id.perf_interval, intVal + " sec");
                                    break;
                                case PreferenceNames.PASSIVE_FILTER_INTERVAL:
                                    prefs.setPassiveFilterInterval(intVal);
                                    refreshValue(R.id.perf_passive_interval, intVal + " sec");
                                    break;
                                case PreferenceNames.MINIMUM_DISTANCE:
                                    prefs.setMinimumDistanceInMeters(intVal);
                                    refreshValue(R.id.perf_distance, intVal + " m");
                                    break;
                                case PreferenceNames.MINIMUM_ACCURACY:
                                    prefs.setMinimumAccuracy(intVal);
                                    refreshValue(R.id.perf_accuracy, intVal + " m");
                                    break;
                                case PreferenceNames.LOGGING_RETRY_TIME:
                                    prefs.setLoggingRetryPeriod(intVal);
                                    refreshValue(R.id.perf_retry_time, intVal + " sec");
                                    break;
                                case PreferenceNames.ABSOLUTE_TIMEOUT:
                                    prefs.setAbsoluteTimeoutForAcquiringPosition(intVal);
                                    refreshValue(R.id.perf_timeout, intVal + " sec");
                                    break;
                                case PreferenceNames.ALTITUDE_SUBTRACT_OFFSET:
                                    prefs.setSubtractAltitudeOffset(value.trim());
                                    refreshValue(R.id.perf_altitude_offset, value.trim() + " m");
                                    break;
                            }
                        } catch (NumberFormatException ignored) {
                            if (PreferenceNames.ALTITUDE_SUBTRACT_OFFSET.equals(key)) {
                                prefs.setSubtractAltitudeOffset(value.trim());
                                refreshValue(R.id.perf_altitude_offset, value.trim() + " m");
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(prefs.getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_settings_performance);

        ((TextView) findViewById(R.id.header_title)).setText(R.string.pref_performance_title);
        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());

        numericSelector(R.id.perf_interval,         "Logging interval",       prefs.getMinimumLoggingInterval(),            "sec", PreferenceNames.MINIMUM_INTERVAL);
        numericSelector(R.id.perf_passive_interval,  "Passive interval",       prefs.getPassiveFilterInterval(),             "sec", PreferenceNames.PASSIVE_FILTER_INTERVAL);
        numericSelector(R.id.perf_distance,          "Distance filter",        prefs.getMinimumDistanceInterval(),           "m",   PreferenceNames.MINIMUM_DISTANCE);
        numericSelector(R.id.perf_accuracy,          "Accuracy filter",        prefs.getMinimumAccuracy(),                   "m",   PreferenceNames.MINIMUM_ACCURACY);
        numericSelector(R.id.perf_retry_time,        "Duration to match accuracy", prefs.getLoggingRetryPeriod(),            "sec", PreferenceNames.LOGGING_RETRY_TIME);
        numericSelector(R.id.perf_timeout,           "Absolute time to GPS fix",   prefs.getAbsoluteTimeoutForAcquiringPosition(), "sec", PreferenceNames.ABSOLUTE_TIMEOUT);
        signedSelector(R.id.perf_altitude_offset,    "Subtract altitude offset",   prefs.getSubtractAltitudeOffset(),        "m",   PreferenceNames.ALTITUDE_SUBTRACT_OFFSET);
    }

    private void numericSelector(int rootId, String label, int currentVal, String unit, String key) {
        selector(rootId, label, currentVal + " " + unit, key, InputType.TYPE_CLASS_NUMBER);
    }

    private void signedSelector(int rootId, String label, int currentVal, String unit, String key) {
        selector(rootId, label, currentVal + " " + unit, key,
            InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    private void selector(int rootId, String label, String displayValue, String key, int inputType) {
        View root = findViewById(rootId);
        ((TextView) root.findViewById(R.id.selector_label)).setText(label);
        ((TextView) root.findViewById(R.id.selector_value)).setText(displayValue);
        root.setOnClickListener(v -> {
            Intent intent = new Intent(this, TextInputActivity.class);
            intent.putExtra(TextInputActivity.EXTRA_TITLE, label);
            intent.putExtra(TextInputActivity.EXTRA_KEY, key);
            intent.putExtra(TextInputActivity.EXTRA_VALUE, ((TextView) root.findViewById(R.id.selector_value)).getText().toString().replaceAll("[^\\d-]", ""));
            intent.putExtra(TextInputActivity.EXTRA_INPUT_TYPE, inputType);
            textLauncher.launch(intent);
        });
    }

    private void refreshValue(int rootId, String value) {
        ((TextView) findViewById(rootId).findViewById(R.id.selector_value)).setText(value);
    }
}
