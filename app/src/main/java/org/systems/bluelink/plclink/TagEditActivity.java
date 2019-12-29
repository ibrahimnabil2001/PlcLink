package org.systems.bluelink.plclink;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.systems.bluelink.plclink.data.tagItems.AnalogTag;
import org.systems.bluelink.plclink.data.tagItems.BaseTag;
import org.systems.bluelink.plclink.data.tagItems.BitTag;
import org.systems.bluelink.plclink.data.tagItems.DiscreteTag;

import java.util.ArrayList;

public class TagEditActivity extends AppCompatActivity {

    private boolean editMode = false;
    private int currentTagIndex;

    EditText editTagName;
    EditText editTagAddress;
    RadioButton radioAnalogType;
    RadioButton radioDiscreteType;
    RadioGroup radioGroupTagType;
    ListView bitTagsList;
    View analogTagDetailedView;
    ArrayList<BitTag> bitTags;
    BitTagAdapter tagListAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_edit);

        currentTagIndex = getIntent().getIntExtra("CURRENT TAG", -1);
        editTagName =  findViewById(R.id.edit_tag_name);
        editTagAddress =  findViewById(R.id.edit_tag_address);
        radioAnalogType =  findViewById(R.id.radio_analog);
        radioDiscreteType = findViewById(R.id.radio_discrete);
        radioGroupTagType = findViewById(R.id.radioGroup_tag_type);
        bitTagsList = findViewById(R.id.bit_tag_list);
        analogTagDetailedView =  findViewById(R.id.analog_tag_view);

        if( currentTagIndex == -1){
            setTitle("Add New Tag");
            bitTags = newBitTagsList(16);

        }else{
            editMode = true;
            radioAnalogType.setEnabled(false);
            radioDiscreteType.setEnabled(false);
            populateTag();
        }

        radioAnalogType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((RadioButton)view).isChecked()){
                    bitTagsList.setVisibility(View.GONE);
                    analogTagDetailedView.setVisibility(View.VISIBLE);
                }else{
                    bitTagsList.setVisibility(View.VISIBLE);
                    analogTagDetailedView.setVisibility(View.GONE);
                }
            }
        });

        radioDiscreteType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((RadioButton)view).isChecked()){
                    bitTagsList.setVisibility(View.VISIBLE);
                    analogTagDetailedView.setVisibility(View.GONE);
                    populateBitTagList();
                }else{

                    bitTagsList.setVisibility(View.GONE);
                    analogTagDetailedView.setVisibility(View.VISIBLE);
                }
            }
        });

        //DataKeeper.globalTagList.get(currentTagIndex).getDataType()


    }

    private ArrayList<BitTag> newBitTagsList(int size){
        ArrayList<BitTag> newList = new ArrayList<>();
        for(int i = 0; i < size ; i++){
            BitTag newTag = new BitTag("Element # " + i + "Tag Name", i);
            newList.add(newTag);
        }
        return newList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!editMode){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (editMode) {
                    updateTag();
                    finish();
                }else {
                    insertTag();
                    finish();
                }
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                if (editMode) {
                    deleteTag();
                    finish();
                }else {
                    Toast.makeText(this,"No Tag to delete", Toast.LENGTH_LONG).show();
                    finish();
                }
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertTag(){

        String newTagAddress = editTagAddress.getText().toString();
        String newTagName = editTagName.getText().toString();
        int newTagType = radioAnalogType.isChecked() ? 1 : 0; //to convert boolean to int

        boolean added = false;
        switch (newTagType){
            case BaseTag.DATA_TYPE_DISCRETE:
                tagListAdapter.getUpdatedList();
                DiscreteTag newTag = (new DiscreteTag(newTagAddress,newTagName, 16));
                newTag.setChildList((ArrayList) tagListAdapter.getUpdatedList());
                added = DataKeeper.addToGlobalTagList(newTag);
                break;
            case BaseTag.DATA_TYPE_ANALOG:
                added = DataKeeper.addToGlobalTagList(new AnalogTag(newTagAddress,newTagName));
                break;
        }
        if(added){
           // MainActivity.tagsAdapter.notifyDataSetChanged();
            Toast.makeText(TagEditActivity.this, "tag added successfully", Toast.LENGTH_LONG).show();
        }else{Toast.makeText(TagEditActivity.this, "tag already exists", Toast.LENGTH_LONG).show();}
    }

    private void updateTag(){
        DataKeeper.globalTagList.get(currentTagIndex).setTagName(editTagName.getText().toString());
        DataKeeper.globalTagList.get(currentTagIndex).setTAgAddress(editTagAddress.getText().toString());
    }

    private void populateTag(){
      BaseTag currentTag=  DataKeeper.globalTagList.get(currentTagIndex);
      String tagName = currentTag.getTagName();
      String tagAddress = currentTag.getTagAddress();
      int dataType = currentTag.getDataType();

        editTagAddress.setText(tagAddress);
        editTagName.setText(tagName);
        switch (dataType){
            case BaseTag.DATA_TYPE_ANALOG:
                radioAnalogType.setChecked(true);
                bitTagsList.setVisibility(View.GONE);
                analogTagDetailedView.setVisibility(View.VISIBLE);

                //todo populate Analog Tag
                break;
            case BaseTag.DATA_TYPE_DISCRETE:
                bitTagsList.setVisibility(View.VISIBLE);
                analogTagDetailedView.setVisibility(View.GONE);
                radioDiscreteType.setChecked(true);
                bitTags = ((DiscreteTag) DataKeeper.globalTagList.get(currentTagIndex)).getChildes();
                populateBitTagList();
                break;
        }
    }

    private  void populateBitTagList(){
        tagListAdapter = new BitTagAdapter(this, bitTags);
        bitTagsList.setAdapter(tagListAdapter);
    }

    private void deleteTag(){
        DataKeeper.globalTagList.remove( currentTagIndex);
    }
}
