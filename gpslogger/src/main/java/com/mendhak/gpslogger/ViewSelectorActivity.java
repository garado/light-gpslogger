package com.mendhak.gpslogger;

import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;

public class ViewSelectorActivity extends AppCompatActivity {

    private static final int[] VIEW_IDS = {
        R.id.option_simple,
        R.id.option_detailed,
        R.id.option_big,
        R.id.option_log,
    };

    private static final int[] VIEW_LABELS = {
        R.string.view_simple,
        R.string.view_detailed,
        R.string.view_big,
        R.string.view_log,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_view_selector);

        ((TextView) findViewById(R.id.header_title)).setText(R.string.view_simple); // reuse or add a "View" string
        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());

        int current = PreferenceHelper.getInstance().getUserSelectedNavigationItem();

        for (int i = 0; i < VIEW_IDS.length; i++) {
            TextView btn = findViewById(VIEW_IDS[i]);
            btn.setText(VIEW_LABELS[i]);

            if (i == current) {
                btn.setPaintFlags(btn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }

            final int position = i;
            btn.setOnClickListener(v -> {
                PreferenceHelper.getInstance().setUserSelectedNavigationItem(position);
                finish();
            });
        }
    }
}
