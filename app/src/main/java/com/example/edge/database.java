package com.example.edge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class database extends SQLiteOpenHelper {
     public database(Context context) {
        super(context, "image.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         db.execSQL("create table tableimage (imagea string,imageb blob);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL("drop table if exists tableimage");
    }

    public boolean insertdata(byte[] imga,byte[] imgb){
         SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String str=new String(imga);
        contentValues.put("imagea", str);
        contentValues.put("imageb",imgb);
        long ins = MyDB.insert("tableimage",null,contentValues);
        if(ins==-1)return false;
        else return true;
    }
    public boolean imageExist(byte[] imga){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        String str=new String(imga);
        Log.e("TILL HERE ITS WORKING", str);
        //String selectQuery = "Select * from tableimage where imagea="+ str;
        Log.e("TILL HERE ITS WORKING"," YES IT IS ");
        Cursor cursor = MyDB.rawQuery("Select * from tableimage where imagea=?",new String[]{str});


        if(cursor.moveToFirst())
        {
            return true;
        }
        return false;

    }
    public Bitmap getImage(byte[] imga){
         SQLiteDatabase MyDB = this.getWritableDatabase();
        Log.e("TILL HERE ITS WORKING", String.valueOf(imga.length));
        String str=new String(imga);
        Log.e("TILL HERE ITS WORKING", str);
        //String selectQuery = "Select * from tableimage where imagea="+ str;
        Log.e("TILL HERE ITS WORKING"," YES IT IS ");
        Cursor cursor = MyDB.rawQuery("Select * from tableimage where imagea=?",new String[]{str});
         cursor.moveToFirst();
         byte[] bitmap=cursor.getBlob(1);
         Bitmap image = BitmapFactory.decodeByteArray(bitmap,0, bitmap.length);
         return image;
    }
}
