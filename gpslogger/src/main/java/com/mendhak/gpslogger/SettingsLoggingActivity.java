package com.mendhak.gpslogger;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.codekidlabs.storagechooser.StorageChooser;
import com.mendhak.gpslogger.BuildConfig;
import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.PreferenceNames;
import com.mendhak.gpslogger.common.Strings;
import com.mendhak.gpslogger.common.Systems;
import com.mendhak.gpslogger.common.slf4j.Logs;
import com.mendhak.gpslogger.loggers.Files;
import com.mendhak.gpslogger.ui.Dialogs;

import org.slf4j.Logger;

import java.io.File;

public class SettingsLoggingActivity extends AppCompatActivity {

    private static final Logger LOG = Logs.of(SettingsLoggingActivity.class);
    private final PreferenceHelper prefs = PreferenceHelper.getInstance();

    private final ActivityResultLauncher<Intent> textLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
                        String key   = result.getData().getStringExtra(TextInputActivity.EXTRA_KEY);
                        String value = result.getData().getStringExtra(TextInputActivity.EXTRA_VALUE);
                        if (key == null || value == null) return;
                        switch (key) {
                            case PreferenceNames.CUSTOM_FILE_NAME:
                                prefs.setCustomFileName(value);
                                refreshValue(R.id.log_filename, value);
                                break;
                            case PreferenceNames.LOG_TO_CSV_DELIMITER:
                                String delim = value.length() > 0 ? value.substring(0, 1) : ",";
                                prefs.setCSVDelimiter(delim);
                                refreshValue(R.id.log_csv_delimiter, delim);
                                break;
                        }
                    });

    private final ActivityResultLauncher<Intent> listLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
                        String value = result.getData().getStringExtra(ListSelectionActivity.EXTRA_VALUE);
                        if (value == null) return;
                        prefs.setNewFileCreationMode(value);
                        refreshValue(R.id.log_file_creation, fileCreationLabel(value));
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(prefs.getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_settings_logging);

        ((TextView) findViewById(R.id.header_title)).setText(R.string.pref_logging_title);
        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());

        setupFolder();
        setupFileCreation();
        setupFilename();
        setupCsvDelimiter();
    }

    private void setupFolder() {
        selector(R.id.log_folder, "Log folder", prefs.getGpsLoggerFolder(),
            v -> openFolderChooser());
    }

    private void setupFileCreation() {
        selector(R.id.log_file_creation, "New file creation",
            fileCreationLabel(prefs.getNewFileCreationMode()),
            v -> {
                String[] entries = getResources().getStringArray(R.array.filecreation_entries);
                String[] values  = getResources().getStringArray(R.array.filecreation_values);
                Intent intent = new Intent(this, ListSelectionActivity.class);
                intent.putExtra(ListSelectionActivity.EXTRA_TITLE, "New file creation");
                intent.putExtra(ListSelectionActivity.EXTRA_KEY, PreferenceNames.NEW_FILE_CREATION_MODE);
                intent.putExtra(ListSelectionActivity.EXTRA_ENTRIES, entries);
                intent.putExtra(ListSelectionActivity.EXTRA_VALUES, values);
                intent.putExtra(ListSelectionActivity.EXTRA_CURRENT_VALUE, prefs.getNewFileCreationMode());
                listLauncher.launch(intent);
            });
    }

    private void setupFilename() {
        selector(R.id.log_filename, "Custom filename",
            Strings.isNullOrEmpty(prefs.getCustomFileName()) ? "gpslogger" : prefs.getCustomFileName(),
            v -> {
                Intent intent = new Intent(this, TextInputActivity.class);
                intent.putExtra(TextInputActivity.EXTRA_TITLE, "Custom filename");
                intent.putExtra(TextInputActivity.EXTRA_KEY, PreferenceNames.CUSTOM_FILE_NAME);
                intent.putExtra(TextInputActivity.EXTRA_VALUE, prefs.getCustomFileName());
                intent.putExtra(TextInputActivity.EXTRA_INPUT_TYPE, InputType.TYPE_CLASS_TEXT);
                textLauncher.launch(intent);
            });
    }

    private void setupCsvDelimiter() {
        selector(R.id.log_csv_delimiter, "CSV delimiter", prefs.getCSVDelimiter(),
            v -> {
                Intent intent = new Intent(this, TextInputActivity.class);
                intent.putExtra(TextInputActivity.EXTRA_TITLE, "CSV delimiter");
                intent.putExtra(TextInputActivity.EXTRA_KEY, PreferenceNames.LOG_TO_CSV_DELIMITER);
                intent.putExtra(TextInputActivity.EXTRA_VALUE, prefs.getCSVDelimiter());
                intent.putExtra(TextInputActivity.EXTRA_INPUT_TYPE, InputType.TYPE_CLASS_TEXT);
                textLauncher.launch(intent);
            });
    }

    private void openFolderChooser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));
            return;
        }
        StorageChooser chooser = Dialogs.directoryChooser(this);
        chooser.setOnSelectListener(path -> {
            if (Strings.isNullOrEmpty(path)) path = Files.storageFolder(this).getAbsolutePath();
            File test = new File(path, "testfile.txt");
            try {
                test.createNewFile();
                if (test.exists()) test.delete();
            } catch (Exception ex) {
                LOG.error("Cannot write to chosen directory", ex);
                path = prefs.getGpsLoggerFolder();
                Dialogs.alert(getString(R.string.error), getString(R.string.pref_logging_file_no_permissions), this);
            }
            prefs.setGpsLoggerFolder(path);
            refreshValue(R.id.log_folder, path);
        });
        chooser.show();
    }

    private void selector(int rootId, String label, String value, View.OnClickListener onClick) {
        View root = findViewById(rootId);
        ((TextView) root.findViewById(R.id.selector_label)).setText(label);
        ((TextView) root.findViewById(R.id.selector_value)).setText(value);
        root.setOnClickListener(onClick);
    }

    private void refreshValue(int rootId, String value) {
        ((TextView) findViewById(rootId).findViewById(R.id.selector_value)).setText(value);
    }

    private String fileCreationLabel(String value) {
        String[] values  = getResources().getStringArray(R.array.filecreation_values);
        String[] entries = getResources().getStringArray(R.array.filecreation_entries);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) return entries[i];
        }
        return value;
    }
}
