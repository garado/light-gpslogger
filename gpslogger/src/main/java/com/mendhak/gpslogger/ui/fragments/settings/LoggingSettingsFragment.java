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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.text.InputType;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.codekidlabs.storagechooser.StorageChooser;
import com.mendhak.gpslogger.BuildConfig;
import com.mendhak.gpslogger.ListSelectionActivity;
import com.mendhak.gpslogger.MainPreferenceActivity;
import com.mendhak.gpslogger.R;
import com.mendhak.gpslogger.TextInputActivity;
import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.PreferenceNames;
import com.mendhak.gpslogger.common.Strings;
import com.mendhak.gpslogger.common.slf4j.Logs;
import com.mendhak.gpslogger.loggers.Files;
import com.mendhak.gpslogger.ui.Dialogs;
import com.mendhak.gpslogger.ui.components.SwitchPlusClickPreference;

import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import eltos.simpledialogfragment.SimpleDialog;

public class LoggingSettingsFragment extends PreferenceFragmentCompat
        implements
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener,
        SimpleDialog.OnDialogResultListener
{

    private static final Logger LOG = Logs.of(LoggingSettingsFragment.class);
    private static PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();

    private final ActivityResultLauncher<Intent> listLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                        String value = result.getData().getStringExtra(ListSelectionActivity.EXTRA_VALUE);
                        if (value == null) return;
                        preferenceHelper.setNewFileCreationMode(value);
                        findPreference(PreferenceNames.NEW_FILE_CREATION_MODE)
                                .setSummary(getFileCreationLabelFromValue(value));
                        setPreferencesEnabledDisabled();
                    });

    private final ActivityResultLauncher<Intent> textLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                        String key   = result.getData().getStringExtra(TextInputActivity.EXTRA_KEY);
                        String value = result.getData().getStringExtra(TextInputActivity.EXTRA_VALUE);
                        if (key == null || value == null) return;
                        switch (key) {
                            case PreferenceNames.CUSTOM_FILE_NAME:
                                preferenceHelper.setCustomFileName(value);
                                findPreference(PreferenceNames.CUSTOM_FILE_NAME).setSummary(value);
                                break;
                            case PreferenceNames.LOG_TO_CSV_DELIMITER:
                                String delim = value.length() > 0 ? value.substring(0, 1) : ",";
                                preferenceHelper.setCSVDelimiter(delim);
                                setPreferenceCsvSummary(delim, preferenceHelper.shouldCSVUseCommaInsteadOfPoint());
                                break;
                        }
                    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Preference gpsloggerFolder = findPreference(PreferenceNames.GPSLOGGER_FOLDER);

        String gpsLoggerFolderPath = preferenceHelper.getGpsLoggerFolder();
        gpsloggerFolder.setDefaultValue(gpsLoggerFolderPath);
        gpsloggerFolder.setSummary(gpsLoggerFolderPath);
        gpsloggerFolder.setOnPreferenceClickListener(this);

        if(!(new File(gpsLoggerFolderPath)).canWrite()){
            gpsloggerFolder.setSummary(Html.fromHtml("<font color='red'>" + gpsLoggerFolderPath + "</font>"));
        }


        SwitchPreferenceCompat logGpx = findPreference(PreferenceNames.LOG_TO_GPX);
        SwitchPreferenceCompat logGpx11 = findPreference(PreferenceNames.LOG_AS_GPX_11);
        logGpx11.setTitle("      " + logGpx11.getTitle());
        logGpx11.setSummary("      " + logGpx11.getSummary());
        logGpx.setOnPreferenceChangeListener(this);
        logGpx11.setEnabled(logGpx.isChecked());


        Preference newFilePref = findPreference(PreferenceNames.NEW_FILE_CREATION_MODE);
        newFilePref.setOnPreferenceClickListener(this);
        newFilePref.setSummary(getFileCreationLabelFromValue(preferenceHelper.getNewFileCreationMode()));


        SwitchPreferenceCompat chkfile_prefix_serial = findPreference(PreferenceNames.PREFIX_SERIAL_TO_FILENAME);
        if (Strings.isNullOrEmpty(Strings.getBuildSerial())) {
            chkfile_prefix_serial.setEnabled(false);
            chkfile_prefix_serial.setSummary("This option not available on older phones or if a serial id is not present");
        } else {
            chkfile_prefix_serial.setEnabled(true);
            chkfile_prefix_serial.setSummary(chkfile_prefix_serial.getSummary().toString() + "(" + Strings.getBuildSerial() + ")");
        }


        findPreference(PreferenceNames.CUSTOM_FILE_NAME).setOnPreferenceClickListener(this);
        if(!Strings.isNullOrEmpty(preferenceHelper.getCustomFileName())){
            findPreference(PreferenceNames.CUSTOM_FILE_NAME).setSummary(preferenceHelper.getCustomFileName());
        }

        ((SwitchPlusClickPreference)findPreference(PreferenceNames.LOG_TO_URL))
                .setSwitchClickListener(new SwitchPlusClickPreference.SwitchPlusClickListener() {

                    @Override
                    public void onCheckedChanged(SwitchCompat buttonView, boolean isChecked) {
                        // No need to do anything, the value gets propagated.
                    }

                    @Override
                    public void onClick(View view) {

                        Intent targetActivity = new Intent(getActivity(), MainPreferenceActivity.class);
                        targetActivity.putExtra("preference_fragment", MainPreferenceActivity.PREFERENCE_FRAGMENTS.CUSTOMURL);
                        startActivity(targetActivity);
                    }
                });

        findPreference(PreferenceNames.LOGGING_WRITE_TIME_WITH_OFFSET).setOnPreferenceChangeListener(this);
        setPreferenceTimeZoneOffsetSummary(preferenceHelper.shouldWriteTimeWithOffset());

        findPreference("log_plain_text_csv_advanced").setOnPreferenceClickListener(this);
        setPreferenceCsvSummary(preferenceHelper.getCSVDelimiter(), preferenceHelper.shouldCSVUseCommaInsteadOfPoint());

        findPreference("delete_files").setOnPreferenceClickListener(this);

    }

    private void setPreferenceCsvSummary(String delimiter, Boolean useComma){
        String sample = "lorem,ipsum,";
        String number = "12.345";
        sample = sample.replaceAll(",", delimiter);
        if(useComma){
            number = number.replace(".", ",");
        }
        findPreference("log_plain_text_csv_advanced").setSummary(sample+number);

    }

    private void setPreferenceTimeZoneOffsetSummary(boolean shouldIncludeOffset){
        String dateTimeString = Strings.getIsoDateTime(new Date());
        if(shouldIncludeOffset){
            dateTimeString = Strings.getIsoDateTimeWithOffset(new Date());
        }

        findPreference(PreferenceNames.LOGGING_WRITE_TIME_WITH_OFFSET).setSummary(getString(R.string.file_logging_log_time_with_offset_summary) + " " + dateTimeString);
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_logging, rootKey);
    }


    @Override
    public void onResume() {
        super.onResume();

        setPreferencesEnabledDisabled();
    }



    @Override
    public boolean onPreferenceClick(Preference preference) {

        if(preference.getKey().equalsIgnoreCase(PreferenceNames.NEW_FILE_CREATION_MODE)){
            String[] entries = getResources().getStringArray(R.array.filecreation_entries);
            String[] values  = getResources().getStringArray(R.array.filecreation_values);
            Intent intent = new Intent(getActivity(), ListSelectionActivity.class);
            intent.putExtra(ListSelectionActivity.EXTRA_TITLE, getString(R.string.new_file_creation_title));
            intent.putExtra(ListSelectionActivity.EXTRA_KEY, PreferenceNames.NEW_FILE_CREATION_MODE);
            intent.putExtra(ListSelectionActivity.EXTRA_ENTRIES, entries);
            intent.putExtra(ListSelectionActivity.EXTRA_VALUES, values);
            intent.putExtra(ListSelectionActivity.EXTRA_CURRENT_VALUE, preferenceHelper.getNewFileCreationMode());
            listLauncher.launch(intent);
            return true;
        }

        if(preference.getKey().equalsIgnoreCase("log_plain_text_csv_advanced")){
            Intent intent = new Intent(getActivity(), TextInputActivity.class);
            intent.putExtra(TextInputActivity.EXTRA_TITLE, getString(R.string.log_plain_text_csv_advanced_title));
            intent.putExtra(TextInputActivity.EXTRA_KEY, PreferenceNames.LOG_TO_CSV_DELIMITER);
            intent.putExtra(TextInputActivity.EXTRA_VALUE, preferenceHelper.getCSVDelimiter());
            intent.putExtra(TextInputActivity.EXTRA_INPUT_TYPE, InputType.TYPE_CLASS_TEXT);
            textLauncher.launch(intent);
            return true;
        }

        if(preference.getKey().equalsIgnoreCase(PreferenceNames.GPSLOGGER_FOLDER)){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                SimpleDialog.build()
                        .title(R.string.error)
                        .msg(R.string.gpslogger_custom_path_need_permission)
                        .show(this, "FILE_PERMISSIONS_REQUIRED");

                return false;
            }

            StorageChooser chooser = Dialogs.directoryChooser(getActivity());
            chooser.setOnSelectListener(path -> {
                LOG.debug(path);
                if(Strings.isNullOrEmpty(path)) {
                    path = Files.storageFolder(getActivity()).getAbsolutePath();
                }
                File testFile = new File(path, "testfile.txt");
                try {
                    testFile.createNewFile();
                    if(testFile.exists()){
                        testFile.delete();
                        LOG.debug("Test file successfully created and deleted.");
                    }
                } catch (Exception ex) {
                    LOG.error("Could not create a test file in the chosen directory.", ex);
                    path = preferenceHelper.getGpsLoggerFolder();
                    Dialogs.alert(getString(R.string.error), getString(R.string.pref_logging_file_no_permissions), getActivity());
                }

                findPreference(PreferenceNames.GPSLOGGER_FOLDER).setSummary(path);
                preferenceHelper.setGpsLoggerFolder(path);

            });
            chooser.show();
        }

        if(preference.getKey().equalsIgnoreCase(PreferenceNames.CUSTOM_FILE_NAME)){
            Intent intent = new Intent(getActivity(), TextInputActivity.class);
            intent.putExtra(TextInputActivity.EXTRA_TITLE, getString(R.string.new_file_custom_title));
            intent.putExtra(TextInputActivity.EXTRA_KEY, PreferenceNames.CUSTOM_FILE_NAME);
            intent.putExtra(TextInputActivity.EXTRA_VALUE, preferenceHelper.getCustomFileName());
            intent.putExtra(TextInputActivity.EXTRA_INPUT_TYPE, InputType.TYPE_CLASS_TEXT);
            textLauncher.launch(intent);
            return true;
        }

        if(preference.getKey().equalsIgnoreCase("delete_files")){
            StorageChooser chooser = Dialogs.multiFilePicker(getActivity(), PreferenceHelper.getInstance().getGpsLoggerFolder());
            chooser.setOnMultipleSelectListener(selectedFilePaths -> {
                Dialogs.progress(getActivity(), getString(R.string.please_wait));
                for(String filePath : selectedFilePaths){
                    File f = new File(filePath);
                    f.delete();
                    LOG.warn("Deleted file: " + f.getName());
                }
                Dialogs.hideProgress();
            });

            chooser.show();
        }

        return false;
    }


    @Override
    public boolean onPreferenceChange(final Preference preference, Object newValue) {

        if(preference.getKey().equalsIgnoreCase(PreferenceNames.LOG_TO_GPX)){
            SwitchPreferenceCompat logGpx11 = findPreference(PreferenceNames.LOG_AS_GPX_11);
            logGpx11.setEnabled((Boolean)newValue);
            return true;
        }


        if(preference.getKey().equalsIgnoreCase(PreferenceNames.LOGGING_WRITE_TIME_WITH_OFFSET)){
            setPreferenceTimeZoneOffsetSummary(Boolean.valueOf(newValue.toString()));
            return true;
        }

        if (preference.getKey().equalsIgnoreCase(PreferenceNames.LOG_TO_OPENGTS)) {

            if(!((SwitchPreferenceCompat) preference).isChecked() && (Boolean)newValue  ) {

                Intent targetActivity = new Intent(getActivity(), MainPreferenceActivity.class);
                targetActivity.putExtra("preference_fragment", MainPreferenceActivity.PREFERENCE_FRAGMENTS.OPENGTS);
                startActivity(targetActivity);
            }

            return true;
        }

        return false;
    }


    private void setPreferencesEnabledDisabled() {

        Preference prefFileCustomName = findPreference(PreferenceNames.CUSTOM_FILE_NAME);
        Preference prefAskEachTime = findPreference(PreferenceNames.ASK_CUSTOM_FILE_NAME);
        Preference prefSerialPrefix = findPreference(PreferenceNames.PREFIX_SERIAL_TO_FILENAME);
        Preference prefDynamicFileName = findPreference(PreferenceNames.CUSTOM_FILE_NAME_KEEP_CHANGING);

        prefFileCustomName.setEnabled(preferenceHelper.shouldCreateCustomFile());
        prefAskEachTime.setEnabled(preferenceHelper.shouldCreateCustomFile());
        prefSerialPrefix.setEnabled(!preferenceHelper.shouldCreateCustomFile());
        prefDynamicFileName.setEnabled(preferenceHelper.shouldCreateCustomFile());
    }


    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if(which != BUTTON_POSITIVE){ return true; }

        if(dialogTag.equalsIgnoreCase("FILE_PERMISSIONS_REQUIRED")){
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            getActivity().startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));
            return true;
        }

        return false;
    }

    private String getFileCreationLabelFromValue(String value){
        String[] values = getResources().getStringArray(R.array.filecreation_values);
        ArrayList<String> valuesArray = new ArrayList<>(Arrays.asList(values));
        int chosenIndex = valuesArray.indexOf(value);
        String[] labels = getResources().getStringArray(R.array.filecreation_entries);
        String chosenLabel = labels[chosenIndex];
        return chosenLabel;
    }

}
