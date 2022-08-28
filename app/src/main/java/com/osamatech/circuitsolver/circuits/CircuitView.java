package com.osamatech.circuitsolver.circuits;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;

import com.osamatech.circuitsolver.R;
import com.osamatech.circuitsolver.ToastMaker;

import java.util.ArrayList;

public class CircuitView extends View {

    public enum CircuitTool {
        WIRE_TOOL, RESISTOR_TOOL, BATTERY_TOOL,
        CONNECTOR_MOVER, COMPONENT_MOVER, PROPERTY_TOOL,
        NAVIGATOR;

        public boolean isComponentTool() {
            return this == WIRE_TOOL || this == RESISTOR_TOOL || this == BATTERY_TOOL;
        }

        public Component.ComponentType getComponentType() {
            switch (this) {
                case WIRE_TOOL:
                    return Component.ComponentType.WIRE;
                case RESISTOR_TOOL:
                    return Component.ComponentType.RESISTOR;
                case BATTERY_TOOL:
                    return Component.ComponentType.BATTERY;
            }
            throw new RuntimeException("Not a component");
        }

    }

    private static final ArrayList<Component> TEMP_COMPONENTS = new ArrayList<>();

    public static final float SELECTED_COMPONENT_REGION_MULTIPLIER = 4.3f;
    public static final float SELECTED_CONNECTOR_REGION_MULTIPLIER = 1.1f;

    private CircuitTool selectedTool = CircuitTool.WIRE_TOOL;
    private boolean runs = false;
    private boolean showSimulation = true;

    private ArrayList<Component> components = new ArrayList<>();
    private ArrayList<Connector> connectors = new ArrayList<>();
    private ArrayList<Voltameter> voltmeters = new ArrayList<>();

    private final Paints paints;
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1f;
    private float focusX = 0;
    private float focusY = 0;

    private Component componentToDraw = new Component(Component.ComponentType.WIRE);
    private boolean isDrawing = false;

    private Component controlledComponent = null;
    private Connector controlledConnector = null;
    private float lastStartX, lastStartY, lastEndX, lastEndY;

    private Connector voltameterFirstConnector = null;
    private PointF lastPoint = new PointF();

    public CircuitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paints = new Paints();
        scaleGestureDetector = new ScaleGestureDetector(context, new NavigationListener());
        componentToDraw.setStartConnector(new Connector());
        componentToDraw.setEndConnector(new Connector());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(scaleFactor, scaleFactor, focusX, focusY);
        for (Component component : components) {
            component.draw(canvas, paints);
        }
        for (Voltameter voltameter : voltmeters) {
            voltameter.draw(canvas, paints);
        }
        for (Connector connector : connectors) {
            if (voltameterFirstConnector == null)
                connector.draw(canvas, paints);
            else
                connector.drawForAddingVoltameter(canvas, paints, voltameterFirstConnector);
        }
        if (isDrawing) {
            componentToDraw.draw(canvas, paints);
        }
        canvas.restore();
    }

    public void selectTool(CircuitTool newSelectedTool) {
        this.selectedTool = newSelectedTool;
        voltameterFirstConnector = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (runs) return true;
        if (selectedTool == CircuitTool.NAVIGATOR) {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        }

        int touchAction = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (touchAction) {
            case MotionEvent.ACTION_DOWN:
                if (selectedTool.isComponentTool()) {
                    componentToDraw.setType(selectedTool.getComponentType());
                    componentToDraw.getStartConnector().set(x, y);
                    componentToDraw.getEndConnector().set(x, y);
                    isDrawing = true;
                } else if (selectedTool == CircuitTool.COMPONENT_MOVER) {
                    controlledComponent = findComponent(x, y);
                    if (controlledComponent != null) {
                        lastStartX = controlledComponent.getStartConnector().getX();
                        lastStartY = controlledComponent.getStartConnector().getY();
                        lastEndX = controlledComponent.getEndConnector().getX();
                        lastEndY = controlledComponent.getEndConnector().getY();
                    }
                } else if (selectedTool == CircuitTool.CONNECTOR_MOVER) {
                    controlledConnector = findConnector(x, y);
                    if (controlledConnector != null) {
                        lastStartX = controlledConnector.getX();
                        lastStartY = controlledConnector.getY();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDrawing) {
                    componentToDraw.getEndConnector().set(x, y);
                    invalidate();
                } else if (controlledComponent != null) {
                    float xDiff = x - lastPoint.x;
                    float yDiff = y - lastPoint.y;
                    controlledComponent.getStartConnector().add(xDiff, yDiff);
                    controlledComponent.getEndConnector().add(xDiff, yDiff);
                    invalidate();
                } else if (controlledConnector != null) {
                    float xDiff = x - lastPoint.x;
                    float yDiff = y - lastPoint.y;
                    controlledConnector.add(xDiff, yDiff);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isDrawing) {
                    if (componentToDraw.isLongEnough()) {
                        //find closest connectors if possible
                        Connector startConnector = findClosestConnector(componentToDraw.getStartConnector().getX(), componentToDraw.getStartConnector().getY());
                        Connector endConnector = findClosestConnector(componentToDraw.getEndConnector().getX(), componentToDraw.getEndConnector().getY());
                        boolean newConnectorCreated = false;
                        if (startConnector == null) {
                            startConnector = new Connector(componentToDraw.getStartConnector());
                            connectors.add(startConnector);
                            newConnectorCreated = true;
                        }
                        if (endConnector == null) {
                            endConnector = new Connector(componentToDraw.getEndConnector());
                            connectors.add(endConnector);
                            newConnectorCreated = true;
                        }

                        //Check if component exists already, if so replace it, otherwise create one
                        Component foundComponent = null;
                        if (!newConnectorCreated) {
                            for (Component component : components) {
                                if (hasConnector(component, startConnector) && hasConnector(component, endConnector)) {
                                    foundComponent = component;
                                    break;
                                }
                            }
                        }

                        //execution
                        if (foundComponent == null) {
                            //create
                            Component component = new Component(componentToDraw.getType());
                            component.setStartConnector(startConnector);
                            component.setEndConnector(endConnector);
                            components.add(component);
                        } else {
                            //replace
                            foundComponent.setType(componentToDraw.getType());
                        }
                    }
                    isDrawing = false;
                    invalidate();
                } else if (controlledComponent != null) {
                    //Check if all components connected are long enough, otherwise undo this
                    ArrayList<Component> components = getComponentsConnectedTo(controlledComponent, TEMP_COMPONENTS);
                    for (Component component : components) {
                        if (!component.isLongEnough()) {
                            //Undo
                            controlledComponent.getStartConnector().set(lastStartX, lastStartY);
                            controlledComponent.getEndConnector().set(lastEndX, lastEndY);
                            break;
                        }
                    }

                    //check if any of the component's two connectors are close to another connector
                    Connector componentConnector1 = controlledComponent.getStartConnector();
                    Connector connector1 = findClosestConnector(componentConnector1.getX(), componentConnector1.getY());
                    if (connector1 != null) {
                        mergeConnectors(componentConnector1, getComponentsConnectedTo(componentConnector1, TEMP_COMPONENTS), connector1);
                    }
                    Connector componentConnector2 = controlledComponent.getEndConnector();
                    Connector connector2 = findClosestConnector(componentConnector2.getX(), componentConnector2.getY());
                    if (connector2 != null) {
                        mergeConnectors(componentConnector2, getComponentsConnectedTo(componentConnector2, TEMP_COMPONENTS), connector2);
                    }

                    controlledComponent = null;
                    invalidate();
                } else if (controlledConnector != null) {
                    //Check if all components connected are long enough, otherwise undo this
                    ArrayList<Component> components = getComponentsConnectedTo(controlledConnector, TEMP_COMPONENTS);
                    for (Component component : components) {
                        if (!component.isLongEnough()) {
                            //Undo
                            controlledConnector.set(lastStartX, lastStartY);
                            break;
                        }
                    }

                    //Checking if the connector is too close to another, if so , remove this one, connect its components with the other
                    Connector anotherConnector = findClosestConnector(controlledConnector.getX(), controlledConnector.getY());
                    if (anotherConnector != null) {
                        mergeConnectors(controlledConnector, components, anotherConnector);
                    }

                    controlledConnector = null;
                    invalidate();
                } else if (selectedTool == CircuitTool.PROPERTY_TOOL) {
                    if (voltameterFirstConnector != null) {
                        Connector connector = findConnector(x, y);
                        if (connector != null && getVoltameter(voltameterFirstConnector, connector) == null) {
                            Voltameter voltameter = new Voltameter(voltameterFirstConnector, connector);
                            voltmeters.add(voltameter);
                            voltameterFirstConnector = null;
                        }
                        invalidate();
                    } else {
                        showPropertiesMenu(x, y);
                    }
                }
                break;
        }

        lastPoint.set(x, y);
        invalidate();//TODO: REMOVABLE
        return true;
    }

    private void mergeConnectors(Connector removedConnector, ArrayList<Component> components, Connector connector) {
        connectors.remove(removedConnector);
        for (Component component : components) {
            if (component.getStartConnector() == removedConnector) {
                component.setStartConnector(connector);
            } else if (component.getEndConnector() == removedConnector) {
                component.setEndConnector(connector);
            }
        }
    }

    private Connector findClosestConnector(float x, float y) {
        Connector closestConnector = null;
        float closestSquaredDis = 0;
        for (Connector connector : connectors) {
            float xDiff = connector.getX() - x;
            float yDiff = connector.getY() - y;
            float squaredDis = xDiff * xDiff + yDiff * yDiff;
            if (squaredDis == 0) continue;
            if (squaredDis <= 4 * Connector.RADIUS * Connector.RADIUS) {
                if (closestConnector == null || squaredDis < closestSquaredDis) {
                    closestConnector = connector;
                    closestSquaredDis = squaredDis;
                }
            }
        }
        return closestConnector;
    }

    private Connector findConnector(float x, float y) {
        for (int i = connectors.size() - 1; i >= 0; i--) {
            Connector connector = connectors.get(i);
            float xDiff = connector.getX() - x;
            float yDiff = connector.getY() - y;
            float squaredDis = xDiff * xDiff + yDiff * yDiff;
            float squaredRadius = Connector.RADIUS * Connector.RADIUS * SELECTED_CONNECTOR_REGION_MULTIPLIER * SELECTED_CONNECTOR_REGION_MULTIPLIER;
            if (squaredDis <= squaredRadius) {
                return connector;
            }
        }
        return null;
    }

    private Component findComponent(float x, float y) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component component = components.get(i);
            float lineX1 = component.getStartConnector().getX();
            float lineY1 = component.getStartConnector().getY();
            float lineX2 = component.getEndConnector().getX();
            float lineY2 = component.getEndConnector().getY();

            float xDiff = lineX2 - lineX1;
            float yDiff = lineY2 - lineY1;
            float rectWidth = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
            float sin = -yDiff / rectWidth;
            float cos = xDiff / rectWidth;

            float rectHeight = SELECTED_COMPONENT_REGION_MULTIPLIER * Paints.STROKE_SIZE;
            float rectX = rotateX(lineX1, lineY1, sin, cos);
            float rectY = rotateY(lineX1, lineY1, sin, cos) - rectHeight / 2;
            float newX = rotateX(x, y, sin, cos);
            float newY = rotateY(x, y, sin, cos);

            if (newX >= rectX && newX <= rectX + rectWidth && newY >= rectY && newY <= rectY + rectHeight) {
                return component;
            }
        }
        return null;
    }

    private Voltameter findVoltameter(float x, float y) {
        for (Voltameter voltameter : voltmeters) {
            float lineX1 = voltameter.getStartConnector().getX();
            float lineY1 = voltameter.getStartConnector().getY();
            float lineX2 = voltameter.getEndConnector().getX();
            float lineY2 = voltameter.getEndConnector().getY();

            float xDiff = lineX2 - lineX1;
            float yDiff = lineY2 - lineY1;
            float rectWidth = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
            float sin = -yDiff / rectWidth;
            float cos = xDiff / rectWidth;

            float rectHeight = Voltameter.VOLTAMETER_DEFAULT_HEIGHT;
            float rectX = rotateX(lineX1, lineY1, sin, cos);
            float rectY = rotateY(lineX1, lineY1, sin, cos) - rectHeight / 2f;
            float newX = rotateX(x, y, sin, cos);
            float newY = rotateY(x, y, sin, cos);

            float centerX = rectX + rectWidth / 2;
            float centerY = rectY  + Voltameter.VOLTAMETER_DEFAULT_HEIGHT / 2f;

            float xDif = centerX - newX;
            float yDif = centerY - newY;

            if (xDif * xDif + yDif * yDif < Device.DEVICE_CIRCLE_RADIUS  * Device.DEVICE_CIRCLE_RADIUS) {
                return voltameter;
            }
        }
        return null;
    }

    private float rotateX(float x, float y, float sin, float cos) {
        return x * cos - y * sin;
    }

    private float rotateY(float x, float y, float sin, float cos) {
        return x * sin + y * cos;
    }

    private ArrayList<Component> getComponentsConnectedTo(Connector connector, ArrayList<Component> returnedList) {
        for (Component component : components) {
            if (component.getStartConnector() == connector || component.getEndConnector() == connector) {
                returnedList.add(component);
            }
        }
        return returnedList;
    }

    private ArrayList<Component> getComponentsConnectedTo(Component givenComponent, ArrayList<Component> returnedList) {
        for (Component component : components) {
            if (hasConnector(component, givenComponent.getStartConnector()) || hasConnector(component, givenComponent.getEndConnector())) {
                returnedList.add(component);
            }
        }
        return returnedList;
    }

    private boolean hasComponentsConnected(Connector connector) {
        for (Component component : components) {
            if (component.getStartConnector() == connector || component.getEndConnector() == connector) {
                return true;
            }
        }
        return false;
    }

    private boolean hasConnector(DoubleConnector doubleConnector, Connector connector) {
        return doubleConnector.getStartConnector() == connector || doubleConnector.getEndConnector() == connector;
    }

    private Voltameter getVoltameter(Connector connector1, Connector connector2) {
        for (Voltameter voltameter : voltmeters) {
            if (hasConnector(voltameter, connector1) && hasConnector(voltameter, connector2)) {
                return voltameter;
            }
        }
        return null;
    }

    private void showListDialog(String title, String[] items, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setItems(items, listener);
        builder.create().show();
    }

    private void showPropertiesMenu(float x, float y) {
        final Connector selectedConnector = findConnector(x, y);
        if (selectedConnector != null) {
            //Preparing items
            String[] items = new String[2];
            if (selectedConnector.isOn())
                items[0] = getContext().getString(R.string.circuit_view_properties_menu_turn_off);
            else
                items[0] = getContext().getString(R.string.circuit_view_properties_menu_turn_on);
            items[1] = getContext().getString(R.string.circuit_view_properties_menu_add_voltameter);
            //Creating the dialog
            showListDialog(getContext().getString(R.string.circuit_view_properties_menu_connector_title), items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            //Change state
                            selectedConnector.setOn(!selectedConnector.isOn());
                            invalidate();
                            break;
                        case 1:
                            //add voltameter
                            voltameterFirstConnector = selectedConnector;
                            invalidate();
                            break;
                    }
                }
            });
            return;
        }

        final Voltameter selectedVoltameter = findVoltameter(x, y);
        if (selectedVoltameter != null) {
            //Preparing items
            String[] items = new String[2];
            items[0] = getContext().getString(R.string.circuit_view_properties_menu_remove_voltameter);
            items[1] = getContext().getString(R.string.circuit_view_properties_menu_reverse_voltameter);
            //Creating the dialog
            showListDialog(getContext().getString(R.string.circuit_view_properties_menu_voltameter_title), items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            //remove voltameter
                            voltmeters.remove(selectedVoltameter);
                            invalidate();
                            break;
                        case 1:
                            //reverse voltameter
                            selectedVoltameter.setPositionReversed(!selectedVoltameter.isPositionReversed());
                            invalidate();
                            break;
                    }
                }
            });
            return;
        }

        final Component selectedComponent = findComponent(x, y);
        if (selectedComponent != null) {
            //Preparing items
            boolean isBattery = selectedComponent.getType() == Component.ComponentType.BATTERY;
            boolean isWire = selectedComponent.getType() == Component.ComponentType.WIRE;

            String[] items = new String[isBattery ? 2 : 3];
            items[0] = getContext().getString(R.string.circuit_view_properties_menu_value);
            items[1] = getContext().getString(R.string.circuit_view_properties_menu_remove);
            if (!isBattery) {
                if (isWire) {
                    items[2] = getContext().getString(R.string.circuit_view_properties_menu_add_remove_ammeter);
                } else {
                    items[2] = getContext().getString(R.string.circuit_view_properties_menu_add_remove_ohmmeter);
                }
            }

            //Creating the dialog
            String title;
            if (isBattery)
                title = getContext().getString(R.string.circuit_view_properties_menu_battery_title);
            else if (isWire)
                title = getContext().getString(R.string.circuit_view_properties_menu_wire_title);
            else
                title = getContext().getString(R.string.circuit_view_properties_menu_resistor_title);

            showListDialog(title, items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            //Assign value
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            final View view = LayoutInflater.from(getContext()).inflate(R.layout.assign_value_dialog, null);
                            builder.setTitle(R.string.assign_value_dialog_title)
                                    .setView(view)
                                    .setPositiveButton(R.string.assign_value_dialog_create, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            EditText valueEditText = view.findViewById(R.id.et_value);
                                            double value = Double.valueOf(valueEditText.getText().toString());
                                            if (selectedComponent.setValue(value)) {
                                                invalidate();
                                            } else {
                                                ToastMaker.shortToast(getContext(), "Input Error");
                                            }
                                        }
                                    })
                                    .setNegativeButton(R.string.assign_value_dialog_cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.create().show();
                            break;
                        case 1:
                            //Remove component
                            components.remove(selectedComponent);
                            //Remove its connectors if possible
                            boolean connectorRemoved = false;
                            if (!hasComponentsConnected(selectedComponent.getStartConnector())) {
                                connectors.remove(selectedComponent.getStartConnector());
                                connectorRemoved = true;
                            }
                            if (!hasComponentsConnected(selectedComponent.getEndConnector())) {
                                connectors.remove(selectedComponent.getEndConnector());
                                connectorRemoved = true;
                            }
                            //Remove voltameters if possible
                            if (connectorRemoved) {
                                Voltameter voltameter = getVoltameter(selectedComponent.getStartConnector(), selectedComponent.getEndConnector());
                                if (voltameter != null) {
                                    voltmeters.remove(voltameter);
                                }
                            }
                            invalidate();
                            break;
                        case 2:
                            //Add device
                            selectedComponent.setHasDevice(!selectedComponent.hasDevice());
                            invalidate();
                            break;
                    }
                }
            });

        }
    }

    public Paints getPaints() {
        return paints;
    }

    public CircuitTool getSelectedTool() {
        return selectedTool;
    }

    private class NavigationListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            // Don't let the object get too small or too large.
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            invalidate();
            return true;
        }

    }


}
