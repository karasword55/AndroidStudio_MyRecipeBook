package com.vektorel.myrecipebook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class AnaMainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> yemekadiarray;
    ArrayList<Integer> idarray;
    ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_ana_main);
        listView=findViewById(R.id.listView);
        yemekadiarray=new ArrayList<String>();
        idarray=new ArrayList<Integer>();
        arrayAdapter=new ArrayAdapter(this, android.R.layout.simple_list_item_1,yemekadiarray);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AnaMainActivity.this,SaveActivity.class);
                intent.putExtra("yemekid",idarray.get(position));
                intent.putExtra("info","old");
                startActivity(intent);
            }
        });
        getData();
    }
    public void getData(){
        try {
            SQLiteDatabase database=this.openOrCreateDatabase("Yemekler",MODE_PRIVATE,null);
            Cursor cursor=database.rawQuery("SELECT * FROM yemekler",null);
            int nameIx=cursor.getColumnIndex("yemekadi");
            int idIx=cursor.getColumnIndex("id");
            while (cursor.moveToNext()){
                yemekadiarray.add(cursor.getString(nameIx));
                idarray.add(cursor.getInt(idIx));
            }
            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void add(View view){
        Intent intent=new Intent(AnaMainActivity.this,SaveActivity.class);
        intent.putExtra("info","new");
        startActivity(intent);
    }
}
