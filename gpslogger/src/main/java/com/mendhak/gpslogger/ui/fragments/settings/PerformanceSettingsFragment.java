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
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.mendhak.gpslogger.R;
import com.mendhak.gpslogger.TextInputActivity;
import com.mendhak.gpslogger.common.AppSettings;
import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.PreferenceNames;

public class PerformanceSettingsFragment
        extends PreferenceFragmentCompat
        implements Preference.OnPreferenceClickListener {

    private final PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();

    private final ActivityResultLauncher<Intent> textLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                        String key   = result.getData().getStringExtra(TextInputActivity.EXTRA_KEY);
                        String value = result.getData().getStringExtra(TextInputActivity.EXTRA_VALUE);
                        if (key == null || value == null) return;
                        try {
                            int intVal = Integer.parseInt(value.trim());
                            switch (key) {
                                case PreferenceNames.MINIMUM_INTERVAL:
                                    preferenceHelper.setMinimumLoggingInterval(intVal);
                                    findPreference(key).setSummary(intVal + getString(R.string.seconds));
                                    break;
                                case PreferenceNames.PASSIVE_FILTER_INTERVAL:
                                    preferenceHelper.setPassiveFilterInterval(intVal);
                                    findPreference(key).setSummary(intVal + getString(R.string.seconds));
                                    break;
                                case PreferenceNames.MINIMUM_DISTANCE:
                                    preferenceHelper.setMinimumDistanceInMeters(intVal);
                                    findPreference(key).setSummary(intVal + getString(R.string.meters));
                                    break;
                                case PreferenceNames.MINIMUM_ACCURACY:
                                    preferenceHelper.setMinimumAccuracy(intVal);
                                    findPreference(key).setSummary(intVal + getString(R.string.meters));
                                    break;
                                case PreferenceNames.LOGGING_RETRY_TIME:
                                    preferenceHelper.setLoggingRetryPeriod(intVal);
                                    findPreference(key).setSummary(intVal + getString(R.string.seconds));
                                    break;
                                case PreferenceNames.ABSOLUTE_TIMEOUT:
                                    preferenceHelper.setAbsoluteTimeoutForAcquiringPosition(intVal);
                                    findPreference(key).setSummary(intVal + getString(R.string.seconds));
                                    break;
                                case PreferenceNames.ALTITUDE_SUBTRACT_OFFSET:
                                    preferenceHelper.setSubtractAltitudeOffset(value.trim());
                                    findPreference(key).setSummary(value.trim() + getString(R.string.meters));
                                    break;
                            }
                        } catch (NumberFormatException ignored) {
                            if (PreferenceNames.ALTITUDE_SUBTRACT_OFFSET.equals(key)) {
                                preferenceHelper.setSubtractAltitudeOffset(value.trim());
                                findPreference(key).setSummary(value.trim() + getString(R.string.meters));
                            }
                        }
                    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        numericPref(PreferenceNames.MINIMUM_INTERVAL,
                preferenceHelper.getMinimumLoggingInterval(), getString(R.string.seconds),
                InputType.TYPE_CLASS_NUMBER);

        numericPref(PreferenceNames.MINIMUM_DISTANCE,
                preferenceHelper.getMinimumDistanceInterval(), getString(R.string.meters),
                InputType.TYPE_CLASS_NUMBER);

        numericPref(PreferenceNames.MINIMUM_ACCURACY,
                preferenceHelper.getMinimumAccuracy(), getString(R.string.meters),
                InputType.TYPE_CLASS_NUMBER);

        numericPref(PreferenceNames.LOGGING_RETRY_TIME,
                preferenceHelper.getLoggingRetryPeriod(), getString(R.string.seconds),
                InputType.TYPE_CLASS_NUMBER);

        numericPref(PreferenceNames.ABSOLUTE_TIMEOUT,
                preferenceHelper.getAbsoluteTimeoutForAcquiringPosition(), getString(R.string.seconds),
                InputType.TYPE_CLASS_NUMBER);

        numericPref(PreferenceNames.PASSIVE_FILTER_INTERVAL,
                preferenceHelper.getPassiveFilterInterval(), getString(R.string.seconds),
                InputType.TYPE_CLASS_NUMBER);

        numericPref(PreferenceNames.ALTITUDE_SUBTRACT_OFFSET,
                preferenceHelper.getSubtractAltitudeOffset(), getString(R.string.meters),
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        SensorManager sensorManager = (SensorManager) AppSettings.getInstance().getSystemService(android.content.Context.SENSOR_SERVICE);
        Sensor significantMotionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        SwitchPreferenceCompat significantMotionSwitch = findPreference(PreferenceNames.ONLY_LOG_IF_SIGNIFICANT_MOTION);
        if (significantMotionSensor == null && significantMotionSwitch != null) {
            significantMotionSwitch.setChecked(false);
            significantMotionSwitch.setEnabled(false);
        }
    }

    private void numericPref(String key, int currentVal, String unit, int inputType) {
        Preference pref = findPreference(key);
        if (pref == null) return;
        pref.setSummary(currentVal + unit);
        pref.setOnPreferenceClickListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_performance, rootKey);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        int inputType = InputType.TYPE_CLASS_NUMBER;
        if (key.equalsIgnoreCase(PreferenceNames.ALTITUDE_SUBTRACT_OFFSET)) {
            inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
        }

        String currentVal = String.valueOf(preferenceHelper.getSubtractAltitudeOffset());
        if (key.equalsIgnoreCase(PreferenceNames.MINIMUM_INTERVAL))
            currentVal = String.valueOf(preferenceHelper.getMinimumLoggingInterval());
        else if (key.equalsIgnoreCase(PreferenceNames.PASSIVE_FILTER_INTERVAL))
            currentVal = String.valueOf(preferenceHelper.getPassiveFilterInterval());
        else if (key.equalsIgnoreCase(PreferenceNames.MINIMUM_DISTANCE))
            currentVal = String.valueOf(preferenceHelper.getMinimumDistanceInterval());
        else if (key.equalsIgnoreCase(PreferenceNames.MINIMUM_ACCURACY))
            currentVal = String.valueOf(preferenceHelper.getMinimumAccuracy());
        else if (key.equalsIgnoreCase(PreferenceNames.LOGGING_RETRY_TIME))
            currentVal = String.valueOf(preferenceHelper.getLoggingRetryPeriod());
        else if (key.equalsIgnoreCase(PreferenceNames.ABSOLUTE_TIMEOUT))
            currentVal = String.valueOf(preferenceHelper.getAbsoluteTimeoutForAcquiringPosition());

        Intent intent = new Intent(getActivity(), TextInputActivity.class);
        intent.putExtra(TextInputActivity.EXTRA_TITLE, preference.getTitle().toString());
        intent.putExtra(TextInputActivity.EXTRA_KEY, key);
        intent.putExtra(TextInputActivity.EXTRA_VALUE, currentVal);
        intent.putExtra(TextInputActivity.EXTRA_INPUT_TYPE, inputType);
        textLauncher.launch(intent);
        return true;
    }
}
