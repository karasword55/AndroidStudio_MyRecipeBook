package com.vektorel.myrecipebook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
public class SaveActivity extends AppCompatActivity {

    Bitmap selectedImage;
    ImageView imageView2;
    EditText edtxt2,edtxt1;
    Button btnkaydet;
    SQLiteDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_save);
        imageView2=findViewById(R.id.imageView2);
        edtxt2=findViewById(R.id.edtxt2);
        edtxt1=findViewById(R.id.edtxt1);
        btnkaydet=findViewById(R.id.btnkaydet);
        database=this.openOrCreateDatabase("Yemekler",MODE_PRIVATE,null);
        Intent intent=getIntent();
        String info=intent.getStringExtra("info");
        if(info.matches("new")){
            edtxt2.setText("");
            edtxt1.setText("");
            btnkaydet.setVisibility(View.VISIBLE);
            Bitmap selectImage= BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.selectimage);
            imageView2.setImageBitmap(selectImage);
        }else{
            int yemekid=intent.getIntExtra("yemekid",1);
            btnkaydet.setVisibility(View.INVISIBLE);

            try {
                Cursor cursor = database.rawQuery("SELECT * FROM yemekler WHERE id = ?",
                        new String[] {String.valueOf(yemekid)});
                int yemekadiIx=cursor.getColumnIndex("yemekadi");
                int tarifIx=cursor.getColumnIndex("tarif");
                int imageIx=cursor.getColumnIndex("image");
                while(cursor.moveToNext()){
                    edtxt2.setText(cursor.getString(yemekadiIx));
                    edtxt1.setText(cursor.getString(tarifIx));

                    byte[] bytes=cursor.getBlob(imageIx);
                    Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    imageView2.setImageBitmap(bitmap);
                }
                cursor.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }
    public void SelectImage(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intenttoGallery=new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intenttoGallery,2);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intenttoGallery=new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intenttoGallery,2);
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==2 && resultCode==RESULT_OK && data!=null){
            Uri imagedata=data.getData();
            try {

                if(Build.VERSION.SDK_INT>=28){
                    ImageDecoder.Source source=ImageDecoder.createSource(this.getContentResolver(),
                            imagedata);
                    selectedImage=ImageDecoder.decodeBitmap(source);
                    imageView2.setImageBitmap(selectedImage);
                }else{
                    selectedImage=MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                            imagedata);
                    imageView2.setImageBitmap(selectedImage);
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void btnsave(View view){

        String yemekadi=edtxt2.getText().toString();
        String tarif=edtxt1.getText().toString();
        Bitmap smallImage=makesamallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray=outputStream.toByteArray();
        try {
            database=openOrCreateDatabase("Yemekler",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY," +
                    "yemekadi VARCHAR,tarif VARCHAR,image BLOB) ");

            String sqlString="INSERT INTO yemekler(yemekadi,tarif,image) VALUES(?,?,?)";
            SQLiteStatement sqLiteStatement=database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,yemekadi);
            sqLiteStatement.bindString(2,tarif);
            sqLiteStatement.bindBlob(3,byteArray);
            sqLiteStatement.execute();
        }catch(Exception e){
            e.printStackTrace();
        }
        Intent intent=new Intent(SaveActivity.this,AnaMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
    public Bitmap makesamallerImage(Bitmap image,int maxsize){
        int width=image.getWidth();
        int height=image.getHeight();

        float bitmapRatio=(float)width/(float)height;

        if(bitmapRatio>1){
            width=maxsize;
            height=(int)(width/bitmapRatio);
        }else{
            height=maxsize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }
}

