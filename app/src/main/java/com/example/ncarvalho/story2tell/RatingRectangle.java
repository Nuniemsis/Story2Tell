package com.example.ncarvalho.story2tell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RatingRectangle extends View {

    private ArrayList<RectF> RectListPaint;
    private ArrayList<RectF> RectListStroke;

    Map<String, Integer> colorRelation;

    ArrayList<String> qualityNames;
    ArrayList<Float> qualityRatios;

    private ArrayList<Paint> FillPaintArrayList;
    private ArrayList<Paint> StrokePaintArrayList;

    private Paint textPaint;

    public RatingRectangle(Context context, UserQualities userQualities) {
        super(context);

        textPaint = new Paint();
        textPaint.setTextSize(48);
        // Initialize a typeface object to draw text on canvas
        Typeface typeface = Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD_ITALIC);

        // Set the paint font
        textPaint.setTypeface(typeface);

        qualityNames = new ArrayList<>();
        qualityRatios = new ArrayList<>();

        RectListPaint = new ArrayList<>();
        RectListStroke = new ArrayList<>();
        // create a rectangle that we'll draw later

        FillPaintArrayList = new ArrayList<>();
        StrokePaintArrayList = new ArrayList<>();

        colorRelation = new HashMap<>();

        colorRelation.put("Shy", Color.MAGENTA);
        colorRelation.put("Polite", Color.RED);
        colorRelation.put("Talkative", Color.BLUE);

        genRectangles(userQualities);
    }

    public void genRectangles(UserQualities userQualities){

        int l = 50;
        int t = 80;
        int r = 50;
        int b = 50;
        int maxScore = 10;
        int diff = 120;

        int width = 600;

        // l, t, r , b
        int i = 0;

        for(Map.Entry<String, Long> entry : userQualities.qualitiesMap.entrySet()){

            int sep = diff*i;
            float ratio = (float) entry.getValue()/maxScore;
            int realWidth = (int) (width*ratio);

            RectF rectanglePaint = new RectF(l , t + sep ,
                    r+ realWidth  , b + sep);
            RectF rectangleStroke = new RectF(l, t + sep , r + width, b + sep);

            RectListPaint.add(rectanglePaint);
            RectListStroke.add(rectangleStroke);

            // Data used to set the text
            qualityNames.add(entry.getKey());
            qualityRatios.add(ratio);

            // create the Paint and set its color
            Paint fillPaint = new Paint();
            fillPaint.setColor(colorRelation.get(entry.getKey()));
            FillPaintArrayList.add(fillPaint);

            // stroke
            Paint strokePaint = new Paint();
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setColor(colorRelation.get(entry.getKey()));
            strokePaint.setStrokeWidth(5);
            StrokePaintArrayList.add(strokePaint);
            i ++;
        }



    }


    @Override
    public void onDraw(Canvas canvas) {
        int textMargin = 20;
        int i = 0;

        for(RectF rect: RectListPaint) {
            canvas.drawRoundRect(rect,5,5, FillPaintArrayList.get(i));
            canvas.drawRoundRect(rect,5,5, StrokePaintArrayList.get(i));
            i++;
        }
        i = 0;
        for(RectF rect: RectListStroke) {
            canvas.drawRoundRect(rect,5,5, StrokePaintArrayList.get(i));
            canvas.drawText(qualityNames.get(i) + ": " +
                            Float.toString(qualityRatios.get(i)*100) +"%",
                    rect.right + textMargin, rect.top, textPaint);
            i++;
        }


    }
}