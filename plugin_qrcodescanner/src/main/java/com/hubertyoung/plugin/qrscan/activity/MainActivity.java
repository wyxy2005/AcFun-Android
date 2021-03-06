/*
 * Copyright (C) 2018 Jenly Yu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubertyoung.plugin.qrscan.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hubertyoung.plugin.qrscan.CaptureActivity;
import com.hubertyoung.plugin.qrscan.Intents;
import com.hubertyoung.plugin.qrscan.R;
import com.hubertyoung.plugin.qrscan.util.CodeUtils;
import com.hubertyoung.plugin.qrscan.util.UriUtils;
import com.wlqq.phantom.communication.PhantomUtils;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_TITLE = "key_title";
    public static final String KEY_IS_QR_CODE = "key_code";
    public static final String KEY_IS_CONTINUOUS = "key_continuous_scan";

    public static final int REQUEST_CODE_SCAN = 0X01;
    public static final int REQUEST_CODE_PHOTO = 0X02;

    public static final int RC_CAMERA = 0X01;

    public static final int RC_READ_PHOTO = 0X02;

    private Class<?> cls;
    private String title;
    private boolean isContinuousScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null){
            switch (requestCode){
                case REQUEST_CODE_SCAN:
                    String result = data.getStringExtra( Intents.Scan.RESULT);
                    Toast.makeText(this,result,Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_CODE_PHOTO:
                    parsePhoto(data);
                    break;
            }

        }
    }

    private void parsePhoto(Intent data){
        final String path = UriUtils.getInstance().getImagePath(this,data);
        Log.d("Jenly","path:" + path);
        if(TextUtils.isEmpty(path)){
            return;
        }
        //异步解析
        asyncThread(new Runnable() {
            @Override
            public void run() {
                final String result = CodeUtils.parseCode(path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Jenly","result:" + result);
                        Toast.makeText(getContext(),result,Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    private Context getContext(){
        return this;
    }

    /**
     * 检测拍摄权限
     */
//    @AfterPermissionGranted(RC_CAMERA)
    private void checkCameraPermissions(){
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission( Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }else{
                startScan(cls,title);
            }
        }
//        String[] perms = {Manifest.permission.CAMERA};
//        if (EasyPermissions.hasPermissions(this, perms)) {//有权限

//        } else {
//            // Do not have permissions, request them now
//            EasyPermissions.requestPermissions(this, getString(R.string.permission_camera),
//                    RC_CAMERA, perms);
//        }
    }

    private void asyncThread(Runnable runnable){
        new Thread(runnable).start();
    }

    /**
     * 扫码
     * @param cls
     * @param title
     */
    private void startScan(Class<?> cls,String title){
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeCustomAnimation(this,R.anim.in,R.anim.out);
        Intent intent = new Intent(this, cls);
        intent.putExtra(KEY_TITLE,title);
        intent.putExtra(KEY_IS_CONTINUOUS,isContinuousScan);
//        ActivityCompat.startActivityForResult(this,intent,REQUEST_CODE_SCAN,optionsCompat.toBundle());
        PhantomUtils.startActivity(this,intent);
    }

    /**
     * 生成二维码/条形码
     * @param isQRCode
     */
    private void startCode(boolean isQRCode){
        Intent intent = new Intent(this,CodeActivity.class);
        intent.putExtra(KEY_IS_QR_CODE,isQRCode);
        intent.putExtra(KEY_TITLE,isQRCode ? getString(R.string.qr_code) : getString(R.string.bar_code));
        startActivity(intent);
    }

    private void startPhotoCode(){
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);
    }

//    @AfterPermissionGranted(RC_READ_PHOTO)
    private void checkExternalStoragePermissions(){
//        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            startPhotoCode();
//        }else{
//            EasyPermissions.requestPermissions(this, getString(R.string.permission_external_storage),
//                    RC_READ_PHOTO, perms);
//        }
    }

    public void OnClick(View v){
        isContinuousScan = false;
        switch (v.getId()){
            case R.id.btn0:
                this.cls = CustomCaptureActivity.class;
                this.title = ((Button)v).getText().toString();
                isContinuousScan = true;
                checkCameraPermissions();
                break;
            case R.id.btn1:
                this.cls = CaptureActivity.class;
                this.title = ((Button)v).getText().toString();
                checkCameraPermissions();
                break;
            case R.id.btn2:
                this.cls = EasyCaptureActivity.class;
                this.title = ((Button)v).getText().toString();
                checkCameraPermissions();
                break;
            case R.id.btn3:
                this.cls = CustomCaptureActivity.class;
                this.title = ((Button)v).getText().toString();
                checkCameraPermissions();
                break;
            case R.id.btn4:
                startCode(false);
                break;
            case R.id.btn5:
                startCode(true);
                break;
            case R.id.btn6:
                checkExternalStoragePermissions();
                break;
        }

    }
}
