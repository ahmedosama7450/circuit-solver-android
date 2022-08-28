package com.osamatech.circuitsolver.circuits;

import android.graphics.Color;
import android.graphics.Paint;

public class Paints {

    public static final float STROKE_SIZE = 11;
    public static final float DEVICE_CIRCLE_STROKE = 10;
    public static final float QUANTITY_TEXT_SIZE = 26;
    public static final float DEVICE_SYMBOL_SIZE = 38;

    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final int ON_COLOR = Color.BLACK;
    public static final int OFF_COLOR = Color.WHITE;
    public static final int ADDING_VOLTAMETER_COLOR = Color.GRAY;

    public Paint defaultPaint;
    public Paint onConnectorPaint;
    public Paint offConnectorPaint;
    public Paint voltameterConnectorPaint;
    public Paint quantityTextPaint;
    public Paint deviceSymbolPaint;
    public Paint deviceCirclePaint;

    public Paints() {
        defaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultPaint.setColor(DEFAULT_COLOR);
        defaultPaint.setStyle(Paint.Style.STROKE);
        defaultPaint.setStrokeWidth(STROKE_SIZE);

        onConnectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        onConnectorPaint.setColor(ON_COLOR);
        onConnectorPaint.setStyle(Paint.Style.FILL);

        offConnectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        offConnectorPaint.setColor(OFF_COLOR);
        offConnectorPaint.setStyle(Paint.Style.FILL);

        voltameterConnectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        voltameterConnectorPaint.setColor(ADDING_VOLTAMETER_COLOR);
        voltameterConnectorPaint.setStyle(Paint.Style.FILL);

        quantityTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        quantityTextPaint.setColor(DEFAULT_COLOR);
        quantityTextPaint.setStyle(Paint.Style.FILL);
        quantityTextPaint.setTextSize(QUANTITY_TEXT_SIZE);

        deviceSymbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        deviceSymbolPaint.setColor(Color.GRAY);
        deviceSymbolPaint.setStyle(Paint.Style.FILL);
        deviceSymbolPaint.setTextSize(DEVICE_SYMBOL_SIZE);
        //Make it bold
        deviceCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        deviceCirclePaint.setStyle(Paint.Style.STROKE);
        deviceCirclePaint.setStrokeWidth(DEVICE_CIRCLE_STROKE);
        deviceCirclePaint.setColor(Color.GRAY);

    }
}
