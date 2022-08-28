package com.osamatech.circuitsolver;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements CircuitsAdapter.CircuitItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewCircuitDialog();
            }
        });

        RecyclerView circuitsRecyclerView = findViewById(R.id.rv_circuits);
        RecyclerView favouritesRecyclerView = findViewById(R.id.rv_favourites);

        initCircuitsView(circuitsRecyclerView, 8);
        initCircuitsView(favouritesRecyclerView, 3);
    }

    private void initCircuitsView(RecyclerView recyclerView, int count) {
        recyclerView.setAdapter(new CircuitsAdapter(this, count, this));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public void onCircuitItemClicked(int position) {
        CircuitActivity.startExistingCircuitActivity(position, MainActivity.this);
    }

    private void showNewCircuitDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = getLayoutInflater().inflate(R.layout.new_circuit_dialog,null);
        builder.setTitle(R.string.new_circuit_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.new_circuit_dialog_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText circuitNameEditText = view.findViewById(R.id.et_circuit_name);
                        String circuitName = circuitNameEditText.getText().toString();
                        CircuitActivity.startNewCircuitActivity(circuitName, MainActivity.this);
                    }
                })
                .setNegativeButton(R.string.new_circuit_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
