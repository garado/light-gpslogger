package com.mendhak.gpslogger;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;

public class ListSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE         = "LIST_TITLE";
    public static final String EXTRA_KEY           = "LIST_KEY";
    public static final String EXTRA_ENTRIES       = "LIST_ENTRIES";
    public static final String EXTRA_VALUES        = "LIST_VALUES";
    public static final String EXTRA_CURRENT_VALUE = "LIST_CURRENT";
    public static final String EXTRA_VALUE         = "LIST_VALUE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_list_selection);

        String   title   = getIntent().getStringExtra(EXTRA_TITLE);
        String[] entries = getIntent().getStringArrayExtra(EXTRA_ENTRIES);
        String[] values  = getIntent().getStringArrayExtra(EXTRA_VALUES);
        String   current = getIntent().getStringExtra(EXTRA_CURRENT_VALUE);

        findViewById(R.id.header).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.header_title)).setText(title != null ? title : "");
        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());

        if (entries == null || values == null) return;

        LinearLayout container = findViewById(R.id.content_inner);
        LayoutInflater inflater = LayoutInflater.from(this);
        int marginPx = (int) (15 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < entries.length; i++) {
            TextView btn = (TextView) inflater.inflate(R.layout.component_styled_button, container, false);
            btn.setText(entries[i]);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = marginPx;
            btn.setLayoutParams(params);

            if (values[i].equals(current)) {
                btn.setPaintFlags(btn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }

            final String selected = values[i];
            btn.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra(EXTRA_KEY, getIntent().getStringExtra(EXTRA_KEY));
                result.putExtra(EXTRA_VALUE, selected);
                setResult(Activity.RESULT_OK, result);
                finish();
            });

            container.addView(btn);
        }
    }
}
