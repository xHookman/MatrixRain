package com.chacha.matrixrain;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import com.coniy.fileprefs.FileSharedPreferences;
import java.io.File;
import java.util.Random;
import android.os.Handler;
import android.widget.Toast;

@SuppressLint("ViewConstructor")
public class MatrixRain extends View {

    SharedPreferences sharedPreferences;
    private static Random random;
    private int width, height;
    private Canvas canvas;
    private Bitmap canvasBmp;
    private int fontSize, columnSize, speed;
    private int verticalLetterSpace, horizontalLetterSpace;
    private float offsEndLines;
    private char[] chars;
    private float[] txtPosByColumn;
    private Paint paintTxt, paintBg, paintBgBmp, paintInitBg;
    private int choosedColor1, choosedColor2, choosedColorBg, trailSize;
    private String choosedFont;
    boolean gradient, randomColors, isInvert, xposed, isPausedDev=false;
    final Handler handler = new Handler();
    static String sharedPrefsFile = "Settings";

    @SuppressLint("WorldReadableFiles")
    public MatrixRain(Context context, String rndText, int color1, int color2, int colorBg, int trail, String font, int speedS, int size, int columnSizex, int vertLetterSpace, int horLetterSpace, float offsetEndLines, boolean isgradient, boolean isRandomColors, boolean isInvertVal, boolean xposedModule) {
        super(context);
        try {
            //noinspection deprecation
            sharedPreferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_WORLD_READABLE);
        } catch (SecurityException ignored) {
            sharedPreferences = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE);
        }
        FileSharedPreferences.makeWorldReadable(context.getPackageName(), sharedPrefsFile);
        xposed = xposedModule;
        if(xposedModule) {
            choosedColor1 = color1;
            choosedColor2 = color2;
            choosedColorBg = colorBg;
            trailSize = trail;
            chars = rndText.toCharArray();
            choosedFont = font;
            fontSize = size;
            speed = speedS;
            gradient = isgradient;
            randomColors = isRandomColors;
            isInvert = isInvertVal;
            columnSize = columnSizex;
            verticalLetterSpace = vertLetterSpace;
            horizontalLetterSpace = horLetterSpace;
            offsEndLines = offsetEndLines;

            if(offsEndLines==9)
                offsEndLines=0.965f;
            else if(offsEndLines==10)
                offsEndLines=0.995f;
            else
                offsEndLines/=10;
        }

        refresh(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
            width = w;
            height = h;
            if(width!=0 && height!=0) //For low dpi (0dp causing crashes)
                recreateView();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBmp, 0, 0, paintBgBmp);
        drawCanvas();
        handler.postDelayed(this::invalidate, speed);
    }

    private void drawText() {
            if (gradient) {
                paintTxt.setShader(new LinearGradient(0, 0, 0, getHeight(), choosedColor1, choosedColor2, Shader.TileMode.MIRROR));
            } else if (!randomColors) {
                paintTxt.setColor(choosedColor1);
            }

                for (int i = 0; i < txtPosByColumn.length; i++) {
                    if (randomColors)
                        paintTxt.setColor(randomizeColor());
                    for (int j = 0; j < columnSize; j++)
                        canvas.drawText("" + chars[random.nextInt(chars.length)], (i + j) * verticalLetterSpace * fontSize, txtPosByColumn[i] * horizontalLetterSpace * fontSize, paintTxt);

                    if (txtPosByColumn[i] * fontSize > height && Math.random() > offsEndLines) { //Décalage entre les differentes lignes verticales
                        txtPosByColumn[i] = 0;
                    }
                    txtPosByColumn[i]++;
                }
        }

    private void drawCanvas() {
        canvas.drawRect(0, 0, width, height, paintBg);
        drawText();
    }

    public void nonXposedPrefs(){
        choosedColor1 = sharedPreferences.getInt("color1", Color.GREEN);
        choosedColor2 = sharedPreferences.getInt("color2", Color.GREEN);
        choosedColorBg = sharedPreferences.getInt("colorBg", Color.BLACK);
        trailSize = sharedPreferences.getInt("trailSize", 10);
        chars = sharedPreferences.getString("rndText", getResources().getString(R.string.default_rndtext)).toCharArray();
        choosedFont = sharedPreferences.getString("fontPath", "");
        fontSize = sharedPreferences.getInt("size", 20);
        speed = sharedPreferences.getInt("speed", 20);
        gradient = sharedPreferences.getBoolean("isGradient", false);
        randomColors = sharedPreferences.getBoolean("isRandomColors", false);
        isInvert = sharedPreferences.getBoolean("isInvert", false);
        columnSize = sharedPreferences.getInt("columnSize", 1);
        verticalLetterSpace = sharedPreferences.getInt("vertLetterSpace", 1);
        horizontalLetterSpace = sharedPreferences.getInt("horLetterSpace", 1);
        offsEndLines = sharedPreferences.getInt("offsetEndLines", 9);
        isPausedDev = sharedPreferences.getBoolean("isPausedDev", false);

        if(offsEndLines==9)
            offsEndLines=0.965f;
        else if(offsEndLines==10)
            offsEndLines=0.995f;
        else
            offsEndLines/=10;
    }

    public void refreshFont(){
        File f = new File(choosedFont);
        if(f.exists())
            paintTxt.setTypeface(Typeface.createFromFile(choosedFont));
    }

    public void refresh(boolean recreateView){
        if(!xposed) {
            nonXposedPrefs();
        }
        random = new Random();

        paintTxt = new Paint();
        paintTxt.setStyle(Paint.Style.FILL);
        paintTxt.setTextSize(fontSize);

        refreshFont();

        paintBg = new Paint();
        paintBg.setColor(choosedColorBg);
        paintBg.setAlpha(trailSize);
        paintBg.setStyle(Paint.Style.FILL);

        paintBgBmp = new Paint();
        paintBgBmp.setColor(choosedColorBg);

        paintInitBg = new Paint();
        paintInitBg.setColor(choosedColorBg);
        paintInitBg.setAlpha(256);
        paintInitBg.setStyle(Paint.Style.FILL);

        if(recreateView)
            recreateView();
    }

    public void recreateView(){
        canvasBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvasBmp.eraseColor(choosedColorBg);
        canvas = new Canvas(canvasBmp);

        if(isInvert)
            canvas.rotate(180, (float) (width / 2), (float) height / 2);

        canvas.drawRect(0, 0, width, height, paintInitBg);
        int matrixWidth = width / fontSize;

        txtPosByColumn = new float[matrixWidth + 1];

        for (int x = 0; x < matrixWidth; x++) {
            txtPosByColumn[x] = random.nextInt(height / 2) + 1;
        }
    }

    int randomizeColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }
}
