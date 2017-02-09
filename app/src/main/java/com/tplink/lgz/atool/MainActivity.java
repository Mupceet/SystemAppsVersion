package com.tplink.lgz.atool;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "ATool";

    private TextView mResult;
    private Button mBtnGetSystemAppsInfo;
    private Button mBtnChangeLanguage;
    private String mResultString = null;
    private boolean mHasWriteExternalStoragePermission = true;
    List<AppVersionMember> appVersionMemberList = null;
    private static final int EVENT_COMPLETE = 100;
    private static final int EVENT_EXPORT_COMPLETE = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.init(this);
        if (!AppInfoUtils.hasPermission(MainActivity.this, REQUIRED_PERMISSIONS)) {
            mHasWriteExternalStoragePermission = false;
            AppInfoUtils.requestPermission(MainActivity.this, REQUIRED_PERMISSIONS);
        }
        initView();

    }



    private void initView() {
        mResult = (TextView) findViewById(R.id.result);
        mResult.setText(R.string.use_steps);
        mBtnChangeLanguage = (Button) findViewById(R.id.changeLanguage);
        mBtnChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivity(intent);
            }
        });
        mBtnGetSystemAppsInfo = (Button) findViewById(R.id.getAppsInfo);
        mBtnGetSystemAppsInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        appVersionMemberList = AppInfoUtils.getAppsWithVersionInfo(MainActivity.this);
                        mResultString = convertAppsVersionInfoToString(appVersionMemberList);
                        if (mHasWriteExternalStoragePermission) {
                            exportAppsVersionInfoToCSV(appVersionMemberList);
                            Message message = Message.obtain(mHandler, EVENT_EXPORT_COMPLETE);
                            mHandler.sendMessage(message);
                        }
                        Message message = Message.obtain(mHandler, EVENT_COMPLETE);
                        mHandler.sendMessage(message);
                    }
                }).start();
            }
        });
    }

    private String convertAppsVersionInfoToString(List<AppVersionMember> result) {
        StringBuilder resultString = new StringBuilder();
        Log.i(TAG, "How many system apps? : " + result.size());
        for (AppVersionMember member :
                result) {
            Log.i(TAG, String.format("%-25s: V%s", member.getAppName(), member.getAppVersionName()));
            resultString.append(String.format("%s:\n\t\t%s", member.getAppName(), member.getAppVersionName()));
            resultString.append("\n");
        }
        return resultString.toString();
    }

    private void exportAppsVersionInfoToCSV(List<AppVersionMember> members) {
        StringBuffer csvStringBuffer = new StringBuffer();
        csvStringBuffer.append(getString(R.string.version_info) + "\r\n");
        for (AppVersionMember member :
                members) {
            csvStringBuffer.append(member.getAppName() + ",V" + member.getAppVersionName() + "\r\n");
        }
        String csvString = csvStringBuffer.toString();
        String csvFileName = Build.MODEL + getString(R.string.version_info) + ".csv";
        File path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        File csvFile = new File(path, csvFileName);
        try {
            path.mkdirs();// Make sure the directory exists.
            OutputStream os = new FileOutputStream(csvFile);
            byte b[] = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
            os.write(b);
            os.write(csvString.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "exportAppsVersionInfoToCSV: error create csv file");
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_COMPLETE:
                    mResult.setText(mResultString);
                    break;
                case EVENT_EXPORT_COMPLETE:
                    Toast.makeText(MainActivity.this, "导出成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppInfoUtils.PERMISSIONS_REQUEST_ALL_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mHasWriteExternalStoragePermission = true;

                } else {
                    Toast.makeText(this, "无权限可导出CSV文件！", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    mHasWriteExternalStoragePermission = false;
                }
                return;
            }
        }
    }
}