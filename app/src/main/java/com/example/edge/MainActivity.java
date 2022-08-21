package com.example.edge;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.CursorWindow;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    database DB;
    Bitmap imgb;
    byte[] byteimga;
    byte[] byteimgb;
    private final int CAMERA_REQ_CODE = 2000;
    private final int GALLERY_REQ_CODE = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            field.set(null, 100 * 1024 * 1024); //the 100MB is the new size
        } catch (Exception e) {
            Log.e("IS THIS RUNNING ",e.toString());
        }
        DB = new database( this);
        Button uploadfromgallery = findViewById(R.id.upload);
        Button uploadfromcamera = findViewById(R.id.camera);
        Button uploadfromurl = findViewById(R.id.fromurl);
        uploadfromgallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iGallery,GALLERY_REQ_CODE);
            }
        });
        uploadfromcamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent iGallery = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(iGallery,CAMERA_REQ_CODE);
            }
        });
        uploadfromurl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            //Your code goes here
                            try {
                                EditText url = (EditText) findViewById(R.id.url);
                                String urlstring = url.getText().toString();
                                URL connection = new URL(urlstring);
                                InputStream input = connection.openStream();
                                Bitmap imageToStore = BitmapFactory.decodeStream(input);
                                imageToStore = Bitmap.createScaledBitmap(imageToStore,1000,1000,true);
                                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                                imageToStore.compress(Bitmap.CompressFormat.PNG,100,byteArray);
                                ImageView realimg=(ImageView)findViewById(R.id.realimg);
                                realimg.setImageBitmap(imageToStore);
                                byteimga = byteArray.toByteArray();
                                if(DB.imageExist(byteimga))
                                { Log.e("IS THIS RUNNING ","IMAGE ALREADY EXIST");
                                    imgb=DB.getImage(byteimga);
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {

                                            // Stuff that updates the UI
                                            ImageView img=(ImageView)findViewById(R.id.img);
                                            img.setImageBitmap(imgb);
                                        }
                                    });

                                    Log.e("IS THIS RUNNING ","IMAGE ALREADY EXIST");
                                }
                                else
                                {Log.e("IS THIS RUNNING ","IMAGE DONT EXIST");
                                    Bitmap edgeDetectedImage=edgeDetectedImage(imageToStore);

                                    ImageView img=(ImageView)findViewById(R.id.img);

//                        img.setImageBitmap(edgeDetectedImage);
                                    ByteArrayOutputStream byteArrayb = new ByteArrayOutputStream();
                                    edgeDetectedImage.compress(Bitmap.CompressFormat.PNG,100,byteArrayb);
                                    byteimgb = byteArrayb.toByteArray();
                                    boolean insert=DB.insertdata(byteimga,byteimgb);
                                    Log.e("ERROR IN REQUEST","INSERTED OK ");
                                    imgb=DB.getImage(byteimga);
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {

                                            // Stuff that updates the UI
                                            ImageView img=(ImageView)findViewById(R.id.img);
                                            img.setImageBitmap(imgb);
                                        }
                                    });
                                }
                            }
                            catch (Exception e) {
                                Log.e("ERROR IN URL ",e.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        Log.e("IS THIS RUNNING","YES IT IS");
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_REQ_CODE){

                  Uri imageFilePath = data.getData();
                try {
                    Bitmap imageToStore = MediaStore.Images.Media.getBitmap(getContentResolver(),imageFilePath);
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                    imageToStore.compress(Bitmap.CompressFormat.PNG,100,byteArray);
                    ImageView realimg=(ImageView)findViewById(R.id.realimg);
                    realimg.setImageBitmap(imageToStore);
                    byteimga = byteArray.toByteArray();
                    if(DB.imageExist(byteimga))
                    { Log.e("IS THIS RUNNING ","IMAGE ALREADY EXIST");
                        imgb=DB.getImage(byteimga);
                        ImageView img=(ImageView)findViewById(R.id.img);
                        img.setImageBitmap(imgb);
                        Log.e("IS THIS RUNNING ","IMAGE ALREADY EXIST");
                    }
                    else
                    {Log.e("IS THIS RUNNING ","IMAGE DONT EXIST");
                        Bitmap edgeDetectedImage=edgeDetectedImage(imageToStore);
                        ImageView img=(ImageView)findViewById(R.id.img);
//                        img.setImageBitmap(edgeDetectedImage);
                        ByteArrayOutputStream byteArrayb = new ByteArrayOutputStream();
                        edgeDetectedImage.compress(Bitmap.CompressFormat.PNG,100,byteArrayb);
                        byteimgb = byteArrayb.toByteArray();
                        boolean insert=DB.insertdata(byteimga,byteimgb);
                        Log.e("ERROR IN REQUEST","INSERTED OK ");
                        imgb=DB.getImage(byteimga);
                        img.setImageBitmap(imgb);
                    }
                } catch (Exception E) {
                    Log.e("ERROR IN REQUEST",E.toString()+" WHAAT ");
                }
            }
            if(requestCode == CAMERA_REQ_CODE){
                try {
                    Bitmap imageToStore = (Bitmap)(data.getExtras().get("data")) ;
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                    imageToStore.compress(Bitmap.CompressFormat.PNG,100,byteArray);
                    ImageView realimg=(ImageView)findViewById(R.id.realimg);
                    realimg.setImageBitmap(imageToStore);
                    byteimga = byteArray.toByteArray();
                    if(DB.imageExist(byteimga))
                    { Log.e("IS THIS RUNNING ","IMAGE ALREADY EXIST");
                        imgb=DB.getImage(byteimga);
                        ImageView img=(ImageView)findViewById(R.id.img);
                        img.setImageBitmap(imgb);
                        Log.e("IS THIS RUNNING ","IMAGE ALREADY EXIST");
                    }
                    else
                    {Log.e("IS THIS RUNNING ","IMAGE DONT EXIST");
                        Bitmap edgeDetectedImage=edgeDetectedImage(imageToStore);
                        ImageView img=(ImageView)findViewById(R.id.img);
//                        img.setImageBitmap(edgeDetectedImage);
                        ByteArrayOutputStream byteArrayb = new ByteArrayOutputStream();
                        edgeDetectedImage.compress(Bitmap.CompressFormat.PNG,100,byteArrayb);
                        byteimgb = byteArrayb.toByteArray();
                        boolean insert=DB.insertdata(byteimga,byteimgb);
                        Log.e("ERROR IN REQUEST","INSERTED OK ");
                        imgb=DB.getImage(byteimga);
                        img.setImageBitmap(imgb);
                    }
                } catch (Exception E) {
                    Log.e("ERROR IN REQUEST",E.toString()+" WHAAT ");
                }
            }
        }
    }
    Mat rgbMat = new Mat();
    Mat grayMat = new Mat();
    Mat bwMat = new Mat();

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    rgbMat = new Mat();
                    grayMat = new Mat();
                    bwMat = new Mat();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    public Bitmap edgeDetectedImage(Bitmap bmp) {
        //compress bitmap
        bmp = getResizedBitmap(bmp, 500);
        Utils.bitmapToMat(bmp, rgbMat);
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(grayMat, grayMat);

        //Imgproc.adaptiveThreshold(grayMat, grayMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 40);
        Imgproc.Canny(grayMat, bwMat, 50, 200, 3, false);

        //find largest contour
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(bwMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

        double maxArea = -1;
        int maxAreaIdx = -1;
        if (contours.size() > 0) {
            MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Mat largest_contour = contours.get(0);
            List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
            for (int idx = 0; idx < contours.size(); idx++) {
                temp_contour = contours.get(idx);
                double contourarea = Imgproc.contourArea(temp_contour);
                //compare this contour to the previous largest contour found
                if (contourarea > maxArea) {
                    //check if this contour is a square
                    MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                    int contourSize = (int)temp_contour.total();
                    Imgproc.approxPolyDP(new_mat, approxCurve, contourSize*0.05, true);
                    if (approxCurve.total() == 4) {
                        maxArea = contourarea;
                        maxAreaIdx = idx;
                        largest_contours.add(temp_contour);
                        largest_contour = temp_contour;
                    }
                }
            }

            if (largest_contours.size() >= 1) {
                MatOfPoint temp_largest = largest_contours.get(largest_contours.size()-1);
                largest_contours = new ArrayList<MatOfPoint>();
                largest_contours.add(temp_largest);
                Imgproc.cvtColor(bwMat, bwMat, Imgproc.COLOR_BayerBG2RGB);
                Imgproc.drawContours(bwMat, largest_contours, -1, new Scalar(0, 255, 0), 1);
            }
        }
        Utils.matToBitmap(bwMat, bmp);
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        bmp = rotateBitmap(bmp,180);
        return bmp;
    }
    Bitmap  rotateBitmap( Bitmap source,int angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true
        );
    }
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}