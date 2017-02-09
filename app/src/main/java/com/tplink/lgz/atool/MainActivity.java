package com.tplink.lgz.atool;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.utils.ToastUtils;
import com.blankj.utilcode.utils.Utils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity implements PermissionListener {
    public static final String TAG = "ATool";

    private TextView mResult;
    private Button mBtnGetSystemAppsInfo;
    private Button mBtnChangeLanguage;
    private String mResultString = null;
    List<AppVersionMember> appVersionMemberList = null;
    private static final int EVENT_EXPORT_SUCCED = 100;
    private static final int EVENT_EXPORT_FAILED = 404;

    private static final int REQUEST_CODE_PERMISSION_SD = 1;
    private static final int REQUEST_CODE_SETTING = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.init(this);
        initView();
    }



    private void initView() {
        mResult = (TextView) findViewById(R.id.result);
        mResult.setTextSize(16);
        mResult.setLineSpacing(8,1);
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
                        if (mResultString != null) {
                            Message message = Message.obtain(mHandler, EVENT_EXPORT_SUCCED);
                            mHandler.sendMessage(message);
                            AndPermission.with(MainActivity.this)
                                    .requestCode(REQUEST_CODE_PERMISSION_SD)
                                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    .send();
                        } else {
                            Message message = Message.obtain(mHandler, EVENT_EXPORT_FAILED);
                            mHandler.sendMessage(message);
                        }
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
            resultString.append(String.format("%s:\n    %s", member.getAppName(), member.getAppVersionName()));
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
        File path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
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
                case EVENT_EXPORT_SUCCED:
                    mResult.setText(mResultString);
                    break;
                case EVENT_EXPORT_FAILED:
                    ToastUtils.showLongToastSafe(R.string.export_failed);
                    break;
                default:
                    break;
            }
        }
    };

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
        exportAppsVersionInfoToCSV(appVersionMemberList);
        ToastUtils.showLongToastSafe(R.string.export_succeed);
    }

    @Override
    public void onFailed(int requestCode, List<String> deniedPermissions) {
        ToastUtils.showLongToastSafe(R.string.export_lack_permission);
        // 用户勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
        if (AndPermission.hasAlwaysDeniedPermission(this, deniedPermissions)) {
            // 自定义的提示语。
            AndPermission.defaultSettingDialog(this, REQUEST_CODE_SETTING)
                    .setTitle("权限申请失败")
                    .setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！")
                    .setPositiveButton("好，去设置")
                    .show();
        }
    }
}