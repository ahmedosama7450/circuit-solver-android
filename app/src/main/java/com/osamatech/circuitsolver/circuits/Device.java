package com.osamatech.circuitsolver.circuits;

public interface Device extends Drawable {

    String AMMETER_SYMBOL = "A";
    String OHMMETER_SYMBOL = "Ω";
    String VOLTAMETER_SYMBOL = "V";
    float DEVICE_CIRCLE_RADIUS = Component.RESISTOR_HEIGHT / 2 + 1f;

}
