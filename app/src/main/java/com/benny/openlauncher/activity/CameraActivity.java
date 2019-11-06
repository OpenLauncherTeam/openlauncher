package com.benny.openlauncher.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.interfaces.CallbackInterface;
import com.benny.openlauncher.util.Preview;

import java.io.File;

public class CameraActivity extends AppCompatActivity implements CallbackInterface {
    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_STORAGE = 2;
    private static final String TAG = "CameraActivity";
    private TextureView mCameraTextureView;
    private Preview mPreview;
    private Button mNormalAngleButton;
    private Button mWideAngleButton;
    private Button mCameraCaptureButton;
    private Button mCameraDirectionButton;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mNormalAngleButton = findViewById(R.id.normal);
        mWideAngleButton = findViewById(R.id.wide);
        mCameraCaptureButton = findViewById(R.id.capture);
        mCameraDirectionButton = findViewById(R.id.change);

        mCameraTextureView = findViewById(R.id.cameraTextureView);
        mPreview = new Preview(this, mCameraTextureView, mNormalAngleButton, mWideAngleButton, mCameraCaptureButton, mCameraDirectionButton);
        mPreview.setOnCallbackListener(this);

        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CameraActivity.REQUEST_STORAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult()");
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        Log.d(TAG, "CAMERA");
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            mPreview.openCamera();
                        } else {
                            Toast.makeText(this, "Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
            case REQUEST_STORAGE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "REQUEST_STORAGE");
                            mPreview.openCamera();
                        } else {
                            Toast.makeText(this, "Should have storage permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        mPreview.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.onPause();
    }

    @Override
    public void onSave(File filePath) {
        Log.d(TAG, "onSave()");
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(filePath));
        sendBroadcast(intent);
    }
}