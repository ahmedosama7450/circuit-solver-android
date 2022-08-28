package com.osamatech.circuitsolver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.osamatech.circuitsolver.circuits.CircuitView;

public class CircuitActivity extends AppCompatActivity {

    public static final String CIRCUIT_NAME_KEY = "circuit_name_key";
    public static final String CIRCUIT_ID_KEY = "circuit_id_key";

    private CircuitView circuitView;

    private boolean toolEverSelected = false;
    private int selectedToolId;
    private boolean showsSimulation = true;

    public static void startNewCircuitActivity(String circuitName, Context context) {
        Intent intent = new Intent(context, CircuitActivity.class);
        intent.putExtra(CircuitActivity.CIRCUIT_NAME_KEY, circuitName);
        context.startActivity(intent);
        ToastMaker.showToast(context, "Creating Circuit : " + circuitName, Toast.LENGTH_SHORT);
    }

    public static void startExistingCircuitActivity(int circuitId, Context context) {
        Intent intent = new Intent(context, CircuitActivity.class);
        intent.putExtra(CircuitActivity.CIRCUIT_ID_KEY, circuitId);
        context.startActivity(intent);
        ToastMaker.showToast(context, "Opening Circuit : " + circuitId, Toast.LENGTH_SHORT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circuit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        circuitView = findViewById(R.id.cv_circuit);

        selectTool(R.id.iv_wire);

        Intent intent = getIntent();

        if (intent.hasExtra(CIRCUIT_NAME_KEY)) {

        } else if (intent.hasExtra(CIRCUIT_ID_KEY)) {

        }

    }

    public void onToolSelected(View view) {
        selectTool(view.getId());
    }

    private void selectTool(int id) {
        if (toolEverSelected) {
            ImageView oldToolView = findViewById(selectedToolId);
            oldToolView.setBackgroundColor(ContextCompat.getColor(this, R.color.circuit_tool_background_color));
        }
        ImageView toolView = findViewById(id);
        toolView.setBackgroundColor(ContextCompat.getColor(this, R.color.circuit_tool_background_color_selected));

        selectedToolId = id;
        toolEverSelected = true;

        CircuitView.CircuitTool tool;
        switch (id) {
            case R.id.iv_wire:
                tool = CircuitView.CircuitTool.WIRE_TOOL;
                break;
            case R.id.iv_resistor:
                tool = CircuitView.CircuitTool.RESISTOR_TOOL;
                break;
            case R.id.iv_battery:
                tool = CircuitView.CircuitTool.BATTERY_TOOL;
                break;
            case R.id.iv_move_component:
                tool = CircuitView.CircuitTool.COMPONENT_MOVER;
                break;
            case R.id.iv_move_connector:
                tool = CircuitView.CircuitTool.CONNECTOR_MOVER;
                break;
            case R.id.iv_properties:
                tool = CircuitView.CircuitTool.PROPERTY_TOOL;
                break;
            case R.id.iv_navigate:
                tool = CircuitView.CircuitTool.NAVIGATOR;
                break;
            default:
                throw new RuntimeException("Something went wrong");
        }
        circuitView.selectTool(tool);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_circuit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_run) {
            //circuitView.run(showsSimulation);
        } else if (id == R.id.action_show_simulation) {
            showsSimulation = !item.isChecked();
            item.setChecked(showsSimulation);
        }

        return super.onOptionsItemSelected(item);
    }

}
