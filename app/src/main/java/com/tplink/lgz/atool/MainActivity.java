package com.tplink.lgz.atool;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.Utils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity implements PermissionListener {
    public static final String TAG = "ATool";

    private TextView mResult;

    private static final int REQUEST_CODE_PERMISSION_SD = 1;
    private static final int REQUEST_CODE_SETTING = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        AndPermission.with(this)
                .requestCode(REQUEST_CODE_PERMISSION_SD)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .send();
    }


    private void initView() {
        mResult = (TextView) findViewById(R.id.result);
        mResult.setTextSize(16);
        mResult.setLineSpacing(8, 1);
        mResult.setText(R.string.use_steps);
        Button btnChangeLanguage = (Button) findViewById(R.id.changeLanguage);
        btnChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivity(intent);
            }
        });
        Button btnGetSystemAppsInfo = (Button) findViewById(R.id.getAppsInfo);
        btnGetSystemAppsInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ExportAsyncTask().execute();
            }
        });
    }

    private String convertAppsVersionInfoToString(List<AppVersionMember> result) {
        StringBuilder resultString = new StringBuilder();
        Log.i(TAG, "How many system apps? : " + result.size());
        for (AppVersionMember member :
                result) {
            Log.i(TAG, String.format("%-25s: V%s", member.getAppName(), member.getAppVersionName()));
            resultString.append(String.format("%s:\n    %s", member.getAppName(), member.getAppVersionName()));
            resultString.append("\n");
        }
        return resultString.toString();
    }

    private void exportAppsVersionInfoToCSV(List<AppVersionMember> members) {
        StringBuilder csvStringBuffer = new StringBuilder();
        csvStringBuffer.append(getString(R.string.version_info)).append("\r\n");
        for (AppVersionMember member :
                members) {
            csvStringBuffer.append(member.getAppName()).append(",V")
                    .append(member.getAppVersionName()).append("\r\n");
        }
        String csvString = csvStringBuffer.toString();
        String csvFileName = Build.MODEL + getString(R.string.version_info) + ".csv";
        File path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
        try {
            createCSVFile(path.toString(), csvFileName, csvString);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "exportAppsVersionInfoToCSV: error create csv file");
        }
    }

    private void createCSVFile(@NonNull String pathName, String csvFileName, String csvString)
            throws IOException {
        File path = new File(pathName);
        File csvFile = new File(path, csvFileName);
        if (path.mkdirs()) {
            OutputStream os = new FileOutputStream(csvFile);
            byte[] b = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            os.write(b);
            os.write(csvString.getBytes());
            os.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /**
         * 转给AndPermission分析结果。
         *
         * @param requestCode  请求码。
         * @param permissions  权限数组，一个或者多个。
         * @param grantResults 请求结果。
         * @param listener PermissionListener 对象。
         */
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @Override
    public void onSucceed(int requestCode, List<String> grantPermissions) {
        // 请求权限成功 无需反映

    }

    @Override
    public void onFailed(int requestCode, List<String> deniedPermissions) {
        // 用户勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
        if (AndPermission.hasAlwaysDeniedPermission(this, deniedPermissions)) {
            // 自定义的提示语。
            AndPermission.defaultSettingDialog(this, REQUEST_CODE_SETTING)
                    .setTitle("权限申请失败")
                    .setMessage("我们需要的SD卡权限以便导出文件，请您到设置页面手动授权，否则功能无法正常使用！")
                    .setPositiveButton("好，去设置")
                    .show();
        }
    }

    private class ExportAsyncTask extends AsyncTask<Integer, Integer, List<AppVersionMember>> {

        @Override
        protected List<AppVersionMember> doInBackground(Integer... integers) {
            List<AppVersionMember> list = AppInfoUtils.getAppsWithVersionInfo(Utils.getApp());
            exportAppsVersionInfoToCSV(list);
            return list;
        }

        @Override
        protected void onPostExecute(List<AppVersionMember> appVersionMembers) {
            super.onPostExecute(appVersionMembers);
            String resultString = convertAppsVersionInfoToString(appVersionMembers);
            mResult.setText(resultString);
        }
    }
}