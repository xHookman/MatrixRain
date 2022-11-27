package com.chacha.matrixrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.coniy.fileprefs.FileSharedPreferences;

public class Dev extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dev);

        try {
            //noinspection deprecation
            sharedPreferences = this.getSharedPreferences("Settings", Context.MODE_WORLD_READABLE);
        } catch (SecurityException ignored) {
            sharedPreferences = this.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        }
        FileSharedPreferences.makeWorldReadable(getPackageName(), "Settings");
        editor = sharedPreferences.edit();

        EditText layoutHook = findViewById(R.id.layoutHook);
        EditText layoutType = findViewById(R.id.layoutType);
        EditText objectId = findViewById(R.id.objectId);
        EditText index = findViewById(R.id.index);
        CheckBox overrideIsPaused = findViewById(R.id.overrideIsPaused);
        Button leaveAndSave = findViewById(R.id.leaveAndSave);

        layoutHook.setText(sharedPreferences.getString("layoutHook", ""));
        layoutType.setText(sharedPreferences.getString("layoutType", ""));
        objectId.setText(sharedPreferences.getString("objectId", ""));
        index.setText(sharedPreferences.getString("index", ""));
        overrideIsPaused.setChecked(sharedPreferences.getBoolean("isPausedDev", false));

        overrideIsPaused.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("isPausedDev", b);
            editor.commit();
            FileSharedPreferences.makeWorldReadable(getPackageName(), "Settings");
        });

        leaveAndSave.setOnClickListener(view1 -> {
            editor.putString("layoutHook", layoutHook.getText().toString());
            editor.putString("layoutType", layoutType.getText().toString());
            editor.putString("objectId", objectId.getText().toString());
            editor.putString("index", index.getText().toString());
            editor.commit();
            FileSharedPreferences.makeWorldReadable(getPackageName(), "Settings");
            finish();
        });
    }
}
