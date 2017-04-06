package com.example.shah.unify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView iv;
    int frontCamId = -1;
    Camera cam = null;
    SharedPreferences preferences;
    public static final String PREFS_FILE = "imagePref";
    public static final String PREFS_NAME = "image_";
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv = (ImageView) findViewById(R.id.imageView);
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0 ; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontCamId = i;
                break;
            }
        }

        preferences  = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        String encodedImage = preferences.getString(PREFS_NAME, "");
        editor = preferences.edit();

        if(!encodedImage.isEmpty()){
            byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            iv.setImageBitmap(bitmap);
        }

        Button btnCam = (Button) findViewById(R.id.button_save);
        btnCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(frontCamId != -1){
                    if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 5);
                        }
                    } else {
                        dispatchTakePictureIntent();
                        /*
                        cam = Camera.open(frontCamId);

                        SurfaceView sv = new SurfaceView(MainActivity.this);
                        try {
                            cam.setPreviewDisplay(sv.getHolder());
                            cam.startPreview();
                            cam.takePicture(null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] bytes, Camera camera) {
                                    //iv.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                    Toast.makeText(MainActivity.this, "Picture taken", Toast.LENGTH_SHORT);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        */
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            iv.setImageBitmap(imageBitmap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();

            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            editor.putString(PREFS_NAME, encodedImage);
            editor.commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 5){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
                /*
                cam = Camera.open(frontCamId);

                SurfaceView sv = new SurfaceView(MainActivity.this);
                try {
                    cam.setPreviewDisplay(sv.getHolder());
                    cam.startPreview();
                    cam.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes, Camera camera) {
                            iv.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
