package com.chacha.matrixrain;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import java.io.File;
import java.util.Random;
import android.os.Handler;

@SuppressLint("ViewConstructor")
public class MatrixRain extends View {

    private static Random random;
    private int width, height;
    private Canvas canvas;
    private Bitmap canvasBmp;
    private float[] txtPosByColumn;
    private Paint paintTxt, paintBg, paintBgBmp, paintInitBg;
    boolean isXposed;
    final Handler handler = new Handler();
    Preferences preferences;
    Context context;

    @SuppressLint("WorldReadableFiles")
    public MatrixRain(Context context) {
        super(context);
        preferences = new Preferences();
        isXposed = false;
        refresh(false);
    }

    @SuppressLint("WorldReadableFiles")
    public MatrixRain(Context context, Preferences preferences) { // Constructor used by module
        super(context);
        this.preferences = preferences;
        isXposed=true;
        refresh(false);
        this.context = context;
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
        handler.postDelayed(this::invalidate, preferences.speed);
    }

    private void drawText() {
            if (preferences.isGradient) {
                paintTxt.setShader(new LinearGradient(0, 0, 0, getHeight(), preferences.choosedColor1, preferences.choosedColor2, Shader.TileMode.MIRROR));
            } else if (!preferences.isRandomColors) {
                paintTxt.setColor(preferences.choosedColor1);
            }

                for (int i = 0; i < txtPosByColumn.length; i++) {
                    if (preferences.isRandomColors)
                        paintTxt.setColor(randomizeColor());
                    for (int j = 0; j < preferences.columnSize; j++)
                        canvas.drawText("" + preferences.text[random.nextInt(preferences.text.length)], (i + j) * preferences.vertLetterSpace * preferences.fontSize, txtPosByColumn[i] * preferences.horLetterSpace * preferences.fontSize, paintTxt);

                    if (txtPosByColumn[i] * preferences.fontSize > height && Math.random() > preferences.offsetEndLines) { //Décalage entre les differentes lignes verticales
                        txtPosByColumn[i] = 0;
                    }
                    txtPosByColumn[i]++;
                }
        }

    private void drawCanvas() {
        canvas.drawRect(0, 0, width, height, paintBg);
        drawText();
    }

    public void refreshFont(){
        Log.e("refreshFont", "refreshFont : " + preferences.fontPath);
        File f = new File(preferences.fontPath);
        if(f.exists())
            paintTxt.setTypeface(Typeface.createFromFile(preferences.fontPath));
    }

    public void refresh(boolean recreateView){
        if(!isXposed) {
            preferences.loadMatrixRainPrefs();
        }
        random = new Random();

        paintTxt = new Paint();
        paintTxt.setStyle(Paint.Style.FILL);
        paintTxt.setTextSize(preferences.fontSize);

        refreshFont();

        paintBg = new Paint();
        paintBg.setColor(preferences.choosedColorBg);
        paintBg.setAlpha(preferences.trailSize);
        paintBg.setStyle(Paint.Style.FILL);

        paintBgBmp = new Paint();
        paintBgBmp.setColor(preferences.choosedColorBg);

        paintInitBg = new Paint();
        paintInitBg.setColor(preferences.choosedColorBg);
        paintInitBg.setAlpha(256);
        paintInitBg.setStyle(Paint.Style.FILL);

        if(recreateView)
            recreateView();
    }

    public void recreateView(){
        canvasBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvasBmp.eraseColor(preferences.choosedColorBg);
        canvas = new Canvas(canvasBmp);

        if(preferences.isInvert)
            canvas.rotate(180, (float) (width / 2), (float) height / 2);

        canvas.drawRect(0, 0, width, height, paintInitBg);
        int matrixWidth = width / preferences.fontSize;

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
