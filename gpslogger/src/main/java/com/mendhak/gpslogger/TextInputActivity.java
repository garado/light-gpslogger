package com.mendhak.gpslogger;

import android.app.Activity;
import android.content.Intent;
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

public class TextInputActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE      = "TEXT_INPUT_TITLE";
    public static final String EXTRA_KEY        = "TEXT_INPUT_KEY";
    public static final String EXTRA_VALUE      = "TEXT_INPUT_VALUE";
    public static final String EXTRA_INPUT_TYPE = "TEXT_INPUT_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(), getBaseContext(), getResources());
        setContentView(R.layout.activity_text_input);

        String title   = getIntent().getStringExtra(EXTRA_TITLE);
        String current = getIntent().getStringExtra(EXTRA_VALUE);
        int inputType  = getIntent().getIntExtra(EXTRA_INPUT_TYPE, EditorInfo.TYPE_CLASS_TEXT);

        ((TextView) findViewById(R.id.header_title)).setText(title != null ? title : "");
        findViewById(R.id.header_back_button).setOnClickListener(v -> finish());

        EditText    input = findViewById(R.id.text_input_field);
        ImageButton clear = findViewById(R.id.text_input_clear);

        input.setInputType(inputType);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if (current != null) {
            input.setText(current);
            input.setSelection(current.length());
        }

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        clear.setVisibility(current != null && !current.isEmpty() ? View.VISIBLE : View.GONE);
        clear.setOnClickListener(v -> input.setText(""));

        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirm(input.getText().toString());
                return true;
            }
            return false;
        });

        input.requestFocus();
        input.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    private void confirm(String value) {
        Intent result = new Intent();
        result.putExtra(EXTRA_KEY, getIntent().getStringExtra(EXTRA_KEY));
        result.putExtra(EXTRA_VALUE, value);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
