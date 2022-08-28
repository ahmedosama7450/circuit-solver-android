package com.osamatech.circuitsolver.circuits;

import android.graphics.Canvas;

public class Connector implements Drawable {

    public static final float RADIUS = 26f;

    private boolean isOn = true;
    private float x, y;

    public Connector() {
        this(0, 0);
    }

    public Connector(Connector connector) {
        this(connector.getX(), connector.getY());
    }

    public Connector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(Canvas canvas, Paints paints) {
        canvas.drawCircle(x, y, RADIUS, isOn ? paints.onConnectorPaint : paints.offConnectorPaint);
    }

    public void drawForAddingVoltameter(Canvas canvas, Paints paints, Connector firstConnector) {
        if (firstConnector == this) {
            draw(canvas, paints);
        } else {
            canvas.drawCircle(x, y, RADIUS, paints.voltameterConnectorPaint);
        }
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Connector set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public void set(Connector connector) {
        this.x = connector.x;
        this.y = connector.y;
    }

    public void add(float x, float y) {
        this.x += x;
        this.y += y;
    }

}
