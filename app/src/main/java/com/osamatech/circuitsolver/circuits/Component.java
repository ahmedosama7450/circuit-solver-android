package com.osamatech.circuitsolver.circuits;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.osamatech.circuitsolver.R;

public class Component extends DoubleConnector implements Drawable {

    public enum ComponentType {
        WIRE, RESISTOR, BATTERY
    }

    private static final Rect TEMP_RECT = new Rect();

    public static final float COMPONENT_MIN_LENGTH = 4.7f * Connector.RADIUS;
    public static final float RESISTOR_WIDTH = 115f;
    public static final float RESISTOR_HEIGHT = 50f;
    public static final float BATTERY_POSITIVE_POLE_LENGTH = 71;
    public static final float BATTERY_NEGATIVE_POLE_LENGTH = 40;
    public static final float BATTERY_POLES_WIDTH = 26.5f;

    public static final String RESISTANCE_UNIT = " Ω";
    public static final String VOLTAGE_UNIT = " V";
    public static final String CURRENT_UNIT = " A";

    private ComponentType type;
    private boolean hasDevice = false;
    private double value = 0;

    private String flowingCurrent;

    public Component(ComponentType type) {
        this.type = type;
    }

    @Override
    public void draw(Canvas canvas, Paints paints) {
        //Component types
        boolean isWire = type == ComponentType.WIRE;
        boolean isResistor = type == ComponentType.RESISTOR;

        //Determine necessary dimensions
        float startX = startConnector.getX();
        float startY = startConnector.getY();
        float endX = endConnector.getX();
        float endY = endConnector.getY();
        float width = calculateLength();
        float height = isWire ? Paints.STROKE_SIZE : (isResistor ? RESISTOR_HEIGHT : BATTERY_POSITIVE_POLE_LENGTH);

        //Prepare paints
        Paint paint = paints.defaultPaint;

        //rotation information
        float xDiff = endX - startX;
        float yDiff = endY - startY;
        float rotationAngle = (float) Math.toDegrees(Math.atan2(yDiff, xDiff));

        //rotating canvas
        canvas.save();
        canvas.rotate(rotationAngle, startX, startY);

        //Drawing component
        if (isWire) {
            canvas.drawLine(startX, startY, startX + width, startY, paint);
        } else if (isResistor) {
            if (width <= RESISTOR_WIDTH) {
                //Draw only resistor
                canvas.drawRect(startX,
                        startY - RESISTOR_HEIGHT / 2,
                        startX + width,
                        startY + RESISTOR_HEIGHT / 2,
                        paint
                );
            } else {
                //Draw wires as well
                float lineLength = (width - RESISTOR_WIDTH) / 2;
                canvas.drawLine(startX,
                        startY,
                        startX + lineLength,
                        startY,
                        paint
                );
                canvas.drawLine(startX + width - lineLength,
                        startY,
                        startX + width,
                        startY,
                        paint
                );
                canvas.drawRect(startX + lineLength,
                        startY - RESISTOR_HEIGHT / 2,
                        startX + width - lineLength,
                        startY + RESISTOR_HEIGHT / 2,
                        paint
                );
            }
        } else {
            if (width <= BATTERY_POLES_WIDTH) {
                //Draw only battery
                float x1 = startX + paint.getStrokeWidth() / 2;
                canvas.drawLine(x1,
                        startY - BATTERY_POSITIVE_POLE_LENGTH / 2,
                        x1,
                        startY + BATTERY_POSITIVE_POLE_LENGTH / 2,
                        paint
                );
                float x2 = startX + BATTERY_POLES_WIDTH - paint.getStrokeWidth() / 2;
                canvas.drawLine(x2,
                        startY - BATTERY_NEGATIVE_POLE_LENGTH / 2,
                        x2,
                        startY + BATTERY_NEGATIVE_POLE_LENGTH / 2,
                        paint
                );
            } else {
                //Draw wires as well
                float lineLength = (width - BATTERY_POLES_WIDTH) / 2;
                canvas.drawLine(startX,
                        startY,
                        startX + lineLength,
                        startY,
                        paint
                );
                canvas.drawLine(startX + width - lineLength,
                        startY,
                        startX + width,
                        startY,
                        paint
                );
                float x1 = startX + lineLength + paint.getStrokeWidth() / 2;
                canvas.drawLine(x1,
                        startY - BATTERY_POSITIVE_POLE_LENGTH / 2,
                        x1,
                        startY + BATTERY_POSITIVE_POLE_LENGTH / 2,
                        paint
                );

                float x2 = startX + lineLength + BATTERY_POLES_WIDTH - paint.getStrokeWidth() / 2;
                canvas.drawLine(x2,
                        startY - BATTERY_NEGATIVE_POLE_LENGTH / 2,
                        x2,
                        startY + BATTERY_NEGATIVE_POLE_LENGTH / 2,
                        paint
                );
            }
        }

        if (hasDevice) {
            //Drawing device if length is enough
            if (width > (2 * Connector.RADIUS + 2 * Device.DEVICE_CIRCLE_RADIUS + 1)) {
                String deviceSymbol = isWire ? Device.AMMETER_SYMBOL : Device.OHMMETER_SYMBOL;
                canvas.drawCircle(startX + width / 2, startY, Device.DEVICE_CIRCLE_RADIUS, paints.deviceCirclePaint);
                paints.deviceSymbolPaint.getTextBounds(deviceSymbol, 0, deviceSymbol.length(), TEMP_RECT);
                canvas.drawText(deviceSymbol, startX + width / 2 - TEMP_RECT.width() / 2f, startY + TEMP_RECT.height() / 2f, paints.deviceSymbolPaint);
            }
        } else {
            //Typing value
            String unit = isWire ? CURRENT_UNIT : (isResistor ? RESISTANCE_UNIT : VOLTAGE_UNIT);
            String valueText = value + unit;
            paints.quantityTextPaint.getTextBounds(valueText, 0, valueText.length() - 1, TEMP_RECT);
            canvas.drawText(valueText, startX + width / 2 - 3 * TEMP_RECT.width() / 4f, startY - height / 2 - TEMP_RECT.height() / 2f, paints.quantityTextPaint);
        }

        //Restore back
        canvas.restore();
    }

    public boolean isLongEnough() {
        float xDiff = endConnector.getX() - startConnector.getX();
        float yDiff = endConnector.getY() - startConnector.getY();
        return xDiff * xDiff + yDiff * yDiff >= COMPONENT_MIN_LENGTH * COMPONENT_MIN_LENGTH;
    }

    public float calculateLength() {
        float xDiff = endConnector.getX() - startConnector.getX();
        float yDiff = endConnector.getY() - startConnector.getY();
        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public boolean isFlowingCurrentDefined() {
        return type == ComponentType.WIRE && !hasDevice;
    }

    public String getFlowingCurrent() {
        //If it's already been assigned, just return it
        if (flowingCurrent != null) return flowingCurrent;

        //Try to figure it out
        if (isFlowingCurrentDefined()) {
            flowingCurrent = String.valueOf(value);
        }

        //return it
        return flowingCurrent;
    }

    public void setFlowingCurrent(String flowingCurrent) {
        this.flowingCurrent = flowingCurrent;
    }

    public double getValue() {
        return value;
    }

    public boolean setValue(double value) {
        if (type != ComponentType.WIRE && value <= 0) return false;
        this.hasDevice = false;
        this.value = value;
        return true;
    }

    public boolean hasDevice() {
        return hasDevice;
    }

    public void setHasDevice(boolean hasDevice) {
        if (type != ComponentType.BATTERY) this.hasDevice = hasDevice;
    }

    public ComponentType getType() {
        return type;
    }

    public void setType(ComponentType type) {
        this.type = type;
    }

}
