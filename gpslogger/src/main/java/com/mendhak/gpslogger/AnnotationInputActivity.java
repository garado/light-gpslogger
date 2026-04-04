package com.mendhak.gpslogger;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Systems;
import com.mendhak.gpslogger.common.events.CommandEvents;
import com.mendhak.gpslogger.loggers.Files;

import de.greenrobot.event.EventBus;

public class AnnotationInputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_annotation_input);

        ((TextView) findViewById(R.id.header_title)).setText(R.string.add_description);
        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());

        EditText input = findViewById(R.id.text_input_field);
        ImageButton clearBtn = findViewById(R.id.text_input_clear);

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearBtn.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        clearBtn.setOnClickListener(v -> input.setText(""));

        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit(v.getText().toString());
                return true;
            }
            return false;
        });

        // Show keyboard immediately
        input.requestFocus();
        input.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    private void submit(String text) {
        text = text.trim().replaceAll("\\s+", " ");
        if (text.isEmpty()) {
            finish();
            return;
        }
        EventBus.getDefault().post(new CommandEvents.Annotate(text));
        Files.addItemToCacheFile(text, "annotations", this);
        finish();
    }
}
