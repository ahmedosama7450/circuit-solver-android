package com.osamatech.circuitsolver.circuits;

import android.graphics.Canvas;
import android.graphics.Rect;

public class Voltameter extends DoubleConnector implements Device {

    private static final Rect TEMP_RECT = new Rect();

    public static final float VOLTAMETER_DEFAULT_HEIGHT = 100f;

    private boolean positionReversed = false;

    public Voltameter(Connector startConnector, Connector endConnector) {
        this.startConnector = startConnector;
        this.endConnector = endConnector;
    }

    @Override
    public void draw(Canvas canvas, Paints paints) {
        if (startConnector == null || endConnector == null) return;

        Connector connector1;
        Connector connector2;
        if(positionReversed) {
            connector1 = this.endConnector;
            connector2 = this.startConnector;
        } else {
            connector1 = this.startConnector;
            connector2 = this.endConnector;
        }

        //Determine necessary dimensions
        float startX = connector1.getX() - Paints.STROKE_SIZE / 2;
        float startY = connector1.getY();
        float endX = connector2.getX();
        float endY = connector2.getY();

        //rotation information
        float xDiff = endX - startX;
        float yDiff = endY - startY;
        float rotationAngle = (float) Math.toDegrees(Math.atan2(yDiff, xDiff));
        float length = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);

        //rotating canvas
        canvas.save();
        canvas.rotate(rotationAngle, startX, startY);

        //draw lines
        float upperY = startY + VOLTAMETER_DEFAULT_HEIGHT;
        float newEndX = startX + length;
        float halfLength = length / 2 - Device.DEVICE_CIRCLE_RADIUS;

        canvas.drawLine(startX, startY, startX, upperY, paints.defaultPaint);
        canvas.drawLine(startX, upperY, startX + halfLength, upperY, paints.defaultPaint);
        canvas.drawLine(newEndX, startY, newEndX, upperY, paints.defaultPaint);
        canvas.drawLine(newEndX, upperY, newEndX - halfLength, upperY, paints.defaultPaint);

        //draw circle
        canvas.drawCircle(startX + length / 2, upperY, Device.DEVICE_CIRCLE_RADIUS, paints.deviceCirclePaint);

        //draw letter
        String deviceSymbol = Device.VOLTAMETER_SYMBOL;
        paints.deviceSymbolPaint.getTextBounds(deviceSymbol, 0, deviceSymbol.length(), TEMP_RECT);
        canvas.drawText(deviceSymbol, startX + length / 2 - TEMP_RECT.width() / 2f, upperY + TEMP_RECT.height() / 2f, paints.deviceSymbolPaint);

        canvas.restore();
    }

    public float findLength() {
        float xDiff = startConnector.getX() - startConnector.getX();
        float yDiff = endConnector.getY() - endConnector.getY();
        float rotationAngle = (float) Math.toDegrees(Math.atan2(yDiff, xDiff));
        return  (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public boolean isPositionReversed() {
        return positionReversed;
    }

    public void setPositionReversed(boolean positionReversed) {
        this.positionReversed = positionReversed;
    }

}
