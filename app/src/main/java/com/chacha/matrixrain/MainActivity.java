package com.chacha.matrixrain;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK;
import static android.os.Build.VERSION.SDK_INT;
import static com.chacha.matrixrain.CopyAssetsFiles.copyAssetFolder;
import static com.chacha.matrixrain.Donation.openDonationLink;
import static com.chacha.matrixrain.Donation.remindDonation;
import static com.chacha.matrixrain.Utils.checkXposed;
import static com.chacha.matrixrain.Utils.killSystemUi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.coniy.fileprefs.FileSharedPreferences;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ColorPickerDialog colorPickerDialog;
    SharedPreferences sharedPreferences;
    static String sharedPrefsFile = "Settings";
    static String fontsFolder = "MatrixRain";

    int color1, color2, colorBg;
    MatrixRain matrixRain;
    int ItemsNumber, fontChoice;
    FileListClass FileList = new FileListClass();
    RealPathClass realPath = new RealPathClass();
    File file = new File(Environment.getExternalStorageDirectory(), fontsFolder);
    Spinner fontDropdown;
    ImageView arrow1, arrow2;

    @SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences= Preferences.loadPreferences(this);

        colorPickerDialog = ColorPickerDialog.createColorPickerDialog(this, ColorPickerDialog.DARK_THEME);

        fontDropdown = findViewById(R.id.fontDropdown);

        Button color1Button = findViewById(R.id.buttonColor1);
        Button color2Button = findViewById(R.id.buttonColor2);
        Button color3Button = findViewById(R.id.buttonColor3);
        Button save = findViewById(R.id.saveBtn);

        CheckBox gradient = findViewById(R.id.gradientCheckbox);
        CheckBox randomize = findViewById(R.id.randomizeCheckbox);
        CheckBox invert = findViewById(R.id.invertCheckbox);

        SeekBar speed = findViewById(R.id.speedSeekbar);
        SeekBar size = findViewById(R.id.sizeSeekbar);
        SeekBar columnSize = findViewById(R.id.columnSizeSeekbar);
        SeekBar vertLetterSpace = findViewById(R.id.vertLettersSpaceSeekbar);
        SeekBar horLetterSpace = findViewById(R.id.horLettersSpaceSeekbar);
        SeekBar offsetEndLines = findViewById(R.id.offsetEndLinesSeekbar);
        SeekBar trail = findViewById(R.id.trailSeekbar);
        SeekBar position = findViewById(R.id.positionSeekbar);

        EditText rndText = findViewById(R.id.rndText);

        TextView speedTextProgress = findViewById(R.id.speedTextProgress);
        TextView sizeTextProgress = findViewById(R.id.sizeTextProgress);
        TextView columnSizeTextProgress = findViewById(R.id.columnSizeTextView);
        TextView vertLetterTextProgress = findViewById(R.id.vertLettersTextProgress);
        TextView horLetterTextProgress = findViewById(R.id.horLettersTextProgress);
        TextView offsetEndLinesTextProgress = findViewById(R.id.offsetEndLinesTextProgress);
        TextView trailTextProgress = findViewById(R.id.trailTextProgress);
        TextView positionTextProgress = findViewById(R.id.positionTextProgress);

        ConstraintLayout expandSettings = findViewById(R.id.expand_settings);
        ScrollView seekbarsLayout = findViewById(R.id.seekbars_layout);
        arrow1 = findViewById(R.id.arrow1);
        arrow2 = findViewById(R.id.arrow2);

        try { //Since v1.3 for opacity a conversion is needed (String to int)
            color1 = sharedPreferences.getInt("color1", Color.GREEN);
            color2 = sharedPreferences.getInt("color2", Color.GREEN);
            colorBg = sharedPreferences.getInt("colorBg", Color.BLACK);
        } catch (ClassCastException e){
            int c1, c2, c3;
            c1 = Color.parseColor(sharedPreferences.getString("color1", "#ff00ff00"));
            c2 = Color.parseColor(sharedPreferences.getString("color2", "#ff00ff00"));
            c3 = Color.parseColor(sharedPreferences.getString("colorBg", "#ff000000"));

            Preferences.getEditor().putInt("color1", c1);
            Preferences.getEditor().putInt("color2", c2);
            Preferences.getEditor().putInt("colorBg", c3);
            Preferences.getEditor().apply();
            FileSharedPreferences.makeWorldReadable(getPackageName(), sharedPrefsFile);
            color1 = c1;
            color2 = c2;
            colorBg = c3;
        }

        gradient.setChecked(sharedPreferences.getBoolean("isGradient", false));
        randomize.setChecked(sharedPreferences.getBoolean("isRandomColors", false));
        invert.setChecked(sharedPreferences.getBoolean("isInvert", false));

        speed.setProgress(sharedPreferences.getInt("speed", 20));
        size.setProgress(sharedPreferences.getInt("size", 20)-1);
        columnSize.setProgress(sharedPreferences.getInt("columnSize", 1));
        vertLetterSpace.setProgress(sharedPreferences.getInt("vertLetterSpace", 1)-1);
        horLetterSpace.setProgress(sharedPreferences.getInt("horLetterSpace", 1)-1);
        offsetEndLines.setProgress(sharedPreferences.getInt("offsetEndLines", 9));
        trail.setProgress(sharedPreferences.getInt("trailSize", 10));
        position.setProgress(sharedPreferences.getInt("position", 8));

        fontChoice = sharedPreferences.getInt("fontChoice", 0);
        rndText.setText(sharedPreferences.getString("rndText", "ABCDEFGHIJKLMNOPQRSTUVWSYZabcdefghijklmnopqrstuvwyz"));

        speed.setMax(149); //1 de moins car i+1
        size.setMax(98); //1 de moins car i+2 dans la fonction seekbarFunction
        columnSize.setMax(10);
        vertLetterSpace.setMax(14);
        horLetterSpace.setMax(14);
        offsetEndLines.setMax(10);
        position.setMax(10);
        trail.setMax(255);

        matrixRain = new MatrixRain(this);
        LinearLayout linearLayout = findViewById(R.id.mainLayout);
        linearLayout.addView(matrixRain);

        color1Button.setTextColor(color1);
        color2Button.setTextColor(color2);
        color3Button.setTextColor(colorBg);

        checkXposed(this);
        checkPermission();
        remindDonation(this);

        try {
            ItemsNumber = FileList.GetFiles(Environment.getExternalStorageDirectory().getPath() + "/" + fontsFolder + "/").length;
            try {
               refreshFontDropdown();
            } catch (Exception ignored){
            }
            ItemsNumber = FileList.GetFiles(Environment.getExternalStorageDirectory().getPath() + "/" + fontsFolder + "/").length;

        } catch (Exception ignored){}
        fontDropdown.setSelection(fontChoice);

        fontDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String fontPath = "";
                ItemsNumber = FileList.GetFiles(Environment.getExternalStorageDirectory().getPath() + "/" + fontsFolder + "/").length;
                        if (position == 0)
                            Preferences.getEditor().putString("fontPath", "");
                        else if (position == ItemsNumber+1)
                            getFont();
                        else
                            fontPath = Environment.getExternalStorageDirectory().getPath() + "/" + fontsFolder + "/" + parentView.getItemAtPosition(position).toString();
                Preferences.getEditor().putString("fontPath", fontPath);
                Preferences.getEditor().putInt("fontChoice", position);
                Preferences.getEditor().commit();
                    FileSharedPreferences.makeWorldReadable(getPackageName(), sharedPrefsFile);
                    File f = new File(fontPath);

                    if(f.exists())
                        rndText.setTypeface(Typeface.createFromFile(fontPath));
                    else
                        rndText.setTypeface(Typeface.DEFAULT);

                matrixRain.refresh(false);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        save.setOnClickListener(view -> {
            Preferences.getEditor().putString("rndText", String.valueOf(rndText.getText()));
            Preferences.getEditor().apply();
            matrixRain.refresh(false);
        });

        expandSettings.setOnClickListener(v -> {
            Animation animation;

            if (seekbarsLayout.getVisibility()==View.GONE) {
                seekbarsLayout.setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
                seekbarsLayout.setAnimation(animation);
                seekbarsLayout.startAnimation(animation);
                arrow1.setRotation(180);
                arrow2.setRotation(180);
            } else {
                seekbarsLayout.setVisibility(View.GONE);
                seekbarsLayout.clearAnimation();
                arrow1.setRotation(360);
                arrow2.setRotation(360);
            }
        });

        color1Button.setOnClickListener(view -> colorChoose(color1Button, "color1"));
        color2Button.setOnClickListener(view -> colorChoose(color2Button, "color2"));
        color3Button.setOnClickListener(view -> colorChoose(color3Button, "colorBg"));

        gradient.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
                randomize.setChecked(false);

            Preferences.getEditor().putBoolean("isGradient", b);
            Preferences.getEditor().apply();
            matrixRain.refresh(false);
        });

        randomize.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
                gradient.setChecked(false);

            Preferences.getEditor().putBoolean("isRandomColors", b);
            Preferences.getEditor().apply();
            matrixRain.refresh(false);
        });

        invert.setOnCheckedChangeListener((compoundButton, b) -> {
            Preferences.getEditor().putBoolean("isInvert", b);
            Preferences.getEditor().apply();
            matrixRain.refresh(true);
        });

        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekbarsFunc(speedTextProgress, getResources().getString(R.string.speed), "speed", i+1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekbarsFunc(sizeTextProgress, getResources().getString(R.string.size), "size", i+2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        columnSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekbarsFunc(columnSizeTextProgress, getResources().getString(R.string.columns_size), "columnSize", i+1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        vertLetterSpace.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekbarsFunc(vertLetterTextProgress, getResources().getString(R.string.space_between_letters_vertical), "vertLetterSpace", i+1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        horLetterSpace.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekbarsFunc(horLetterTextProgress, getResources().getString(R.string.space_between_letters_horizontal), "horLetterSpace", i+1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        offsetEndLines.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekbarsFunc(offsetEndLinesTextProgress, getResources().getString(R.string.offset_end_of_lines), "offsetEndLines", i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        trail.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekbarsFunc(trailTextProgress, getResources().getString(R.string.trail_size), "trailSize", i); //Min is 0
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekbarsFunc(positionTextProgress, getResources().getString(R.string.position), "position", i); //Min is 0
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void getFont() {
        // To open up a gallery browser
        Intent intent = new Intent();
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            intent.setType("font/ttf");
        } else {
            intent.setType("application/octet-stream");
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Font (TTF)"), 2);
        // To handle when an image is selected from the browser, add the following to your Activity
    }
    

    public void seekbarsFunc(TextView textProgress, String text, String prefName, int i){
        textProgress.setText(text + " : " + i);
        Preferences.getEditor().putInt(prefName, i);
        Preferences.getEditor().apply();
        matrixRain.refresh(true);
    }

    public void killSystemUI(View view) {
        killSystemUi(this);
    }

    public void colorChoose(Button button, String sharedPref){
        colorPickerDialog.setInitialColor(button.getCurrentTextColor());
        colorPickerDialog.setOnColorPickedListener((color, hexVal) -> {
            Preferences.getEditor().putInt(sharedPref, color);
            Preferences.getEditor().commit();
            button.setTextColor(color);
            matrixRain.refresh(false);
        });
        colorPickerDialog.show();
    }

    public void devBtn(View view) {
        Intent intent = new Intent(this, Dev.class);
        startActivity(intent);
    }

    public void donate(View view) {
       openDonationLink(this);
    }

    public void deleteItem(View view){
        String DropdownSelectedItem = fontDropdown.getSelectedItem().toString();
        final File filetoremove = new File(Environment.getExternalStorageDirectory().getPath() + "/" + fontsFolder + "/" + DropdownSelectedItem); // path only
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setMessage("Are you sure you want to delete "+ fontDropdown.getSelectedItem() + " file definitely ?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    try {
                        FileUtils.forceDelete(filetoremove);
                        Toast.makeText(this, "The file was removed with success !", Toast.LENGTH_LONG).show();
                        refreshFontDropdown();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "An error occurred to remove the file :/", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                createFiles();
            } else {
                requestPermission();
            }
        } else {
            int result = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            if (result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED){
                createFiles();
            } else {
                requestPermission();
            }
        }
    }

    public void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 3);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 3);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 10);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                try {
                    createFiles();
                } catch (Exception ignored) {
                    // Checkpermission();
                    //Toast.makeText(ctx, "An error occured !", Toast.LENGTH_LONG).show();
                }
                break;
            case 2:
                try{
                if (Objects.requireNonNull(Objects.requireNonNull(data.getData()).getPath()).contains(".ttf")) {
                    File srcFile = new File(realPath.getPathFromURI(this, data.getData()));  // path + filename
                    Log.e("PATH : ", realPath.getPathFromURI(this, data.getData()));
                    File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/MatrixRain"); // path only
                    try {
                        FileUtils.copyFileToDirectory(srcFile, destDir);
                        refreshFontDropdown();
                    } catch (IOException e) {
                        Toast.makeText(this, "CAN NOT COPY FONT FILE, CHECK THE APP PERMISSIONS !", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Please select a .ttf font file", Toast.LENGTH_LONG).show();
                }
    } catch (Exception e) {
        Toast.makeText(this, R.string.filepickererror, Toast.LENGTH_LONG).show();
        e.printStackTrace();
    }
                break;
                
            case 3:
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // perform action when allow permission success
                        createFiles();
                    } else {
                        showPermissionError();
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createFiles();
                // permission was granted
            } else {
                showPermissionError();
            }
        }
    }

    private void createFiles() {
        if (!file.exists()) {
            file.mkdirs();
        }
        if(file.list().length==0){
            copyAssetFolder(getAssets(), "fonts", file.getPath());
        }
        refreshFontDropdown();
    }

    private void showPermissionError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, THEME_DEVICE_DEFAULT_DARK);
        builder.setMessage("Store permission was not granted. This permission is necessary to store custom fonts on the device.")
                .setPositiveButton("Ask again", (dialog, id) -> checkPermission())
                .setNegativeButton("Go to settings", (dialog, id) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    public void refreshFontDropdown(){
        try {
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, FileList.getFileNames(FileList.GetFiles(Environment.getExternalStorageDirectory() + "/MatrixRain")));
            spinnerArrayAdapter.add("--Add a font--");
            spinnerArrayAdapter.insert("Default", 0);
            fontDropdown.setAdapter(spinnerArrayAdapter);
            fontDropdown.setSelection(sharedPreferences.getInt("fontChoice", 0));
        } catch (Exception ignored){}
        ItemsNumber = FileList.GetFiles(Environment.getExternalStorageDirectory().getPath() + "/" + fontsFolder + "/").length;

    }

    public void visibilityRedText(View view) {
        Animation animation;
        view.setVisibility(View.GONE);
        animation = AnimationUtils.loadAnimation(this, R.anim.bounce_end);
        view.setAnimation(animation);
        view.startAnimation(animation);
    }
}