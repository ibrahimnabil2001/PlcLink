package org.systems.bluelink.plclink;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.systems.bluelink.plclink.NewSystemDialogFragment.NoticeDialogListener;

import org.systems.bluelink.plclink.data.tagItems.System;



public class HomeActivity extends AppCompatActivity implements NoticeDialogListener {


     static SystemsAdapter systemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if(DataKeeper.globalSystemList == null){
        DataKeeper.pullFromFile(this, "data.json");}
        systemsAdapter = new SystemsAdapter(this, DataKeeper.globalSystemList);

        ListView listView = findViewById(R.id.projects_list_view);
        listView.setAdapter(systemsAdapter);
        TextView emptyView = findViewById(R.id.empty_list_view);
        listView.setEmptyView(emptyView);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.putExtra("CURRENT SYSTEM", i);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_projects, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_add_project:
                final DialogFragment newSystemDialog = new NewSystemDialogFragment();
                newSystemDialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        systemsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataKeeper.saveToFile(this, "data.json");
        Toast.makeText(this,"home destroyed", Toast.LENGTH_SHORT).show();
        BluetoothService.disconnectBT();
    }

}
