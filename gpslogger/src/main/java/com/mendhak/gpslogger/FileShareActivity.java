package com.mendhak.gpslogger;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;
import com.mendhak.gpslogger.loggers.Files;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class FileShareActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED = "FILE_SHARE_SELECTED";
    // Optional: pass a pre-filtered file list + sender for the upload flow
    public static final String EXTRA_FILES    = "FILE_SHARE_FILES";
    public static final String EXTRA_SENDER   = "FILE_SHARE_SENDER";

    private final Set<String> selectedFiles = new LinkedHashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_list_selection);

        findViewById(R.id.header).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.header_title)).setText(R.string.osm_pick_file);
        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());

        boolean isUploadMode = getIntent().hasExtra(EXTRA_FILES);

        ImageButton rightAction = findViewById(R.id.header_right_action);
        rightAction.setImageResource(isUploadMode ? R.drawable.upload : R.drawable.share);
        rightAction.setVisibility(View.VISIBLE);
        rightAction.setOnClickListener(v -> confirm());

        if (isUploadMode) {
            loadFromExtras();
        } else {
            loadAllFiles();
        }
    }

    private void loadAllFiles() {
        LinearLayout container = findViewById(R.id.content_inner);
        LayoutInflater inflater = LayoutInflater.from(this);
        int marginPx = marginPx();

        addButton(container, inflater, getString(R.string.sharing_location_only), marginPx);

        File gpxFolder = new File(PreferenceHelper.getInstance().getGpsLoggerFolder());
        if (gpxFolder.exists()) {
            File[] files = Files.fromFolder(gpxFolder);
            Arrays.sort(files, (f1, f2) -> -Long.compare(f1.lastModified(), f2.lastModified()));
            for (File f : files) {
                addButton(container, inflater, f.getName(), marginPx);
            }
        }
    }

    private void loadFromExtras() {
        String[] files = getIntent().getStringArrayExtra(EXTRA_FILES);
        if (files == null) { finish(); return; }

        LinearLayout container = findViewById(R.id.content_inner);
        LayoutInflater inflater = LayoutInflater.from(this);
        int marginPx = marginPx();

        for (String name : files) {
            addButton(container, inflater, name, marginPx);
        }
    }

    private void addButton(LinearLayout container, LayoutInflater inflater, String name, int marginPx) {
        TextView btn = (TextView) inflater.inflate(R.layout.component_styled_button, container, false);
        btn.setText(name);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = marginPx;
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            if (selectedFiles.contains(name)) {
                selectedFiles.remove(name);
                btn.setPaintFlags(btn.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            } else {
                selectedFiles.add(name);
                btn.setPaintFlags(btn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        });

        container.addView(btn);
    }

    private void confirm() {
        if (selectedFiles.isEmpty()) { finish(); return; }
        Intent result = new Intent();
        result.putStringArrayListExtra(EXTRA_SELECTED, new ArrayList<>(selectedFiles));
        if (getIntent().hasExtra(EXTRA_SENDER)) {
            result.putExtra(EXTRA_SENDER, getIntent().getStringExtra(EXTRA_SENDER));
        }
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private int marginPx() {
        return (int) (15 * getResources().getDisplayMetrics().density);
    }
}
