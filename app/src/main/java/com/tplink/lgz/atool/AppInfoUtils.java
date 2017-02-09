/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AppInfoUtils.java
 *
 * 获取应用信息的工具类
 *
 * Author laiguizhong@tplink.com.cn, Created at 2017-1-25
 *
 * Ver 1.0, 2017-1-25, 赖癸仲, Create file.
 */

package com.tplink.lgz.atool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Trace;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.blankj.utilcode.utils.AppUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 获取应用信息
 */

public class AppInfoUtils {
    /**
     * 获取所有已安装App信息，包括系统应用与第三方应用
     *
     * @param context
     * @return 所有已安装的AppInfo列表，用到的信息有 包名/名称/是否是系统应用
     */
    private static List<AppUtils.AppInfo> getAllAppsInfo(Context context) {
        return AppUtils.getAppsInfo(context);
    }

    /**
     * 获取所有能从桌面启动的应用的包名。包含被禁用的组件，有可能重复，例如：com.google.android.gm
     *
     * @param context
     * @return 所有能从桌面启动的应用的包名列表
     */
    private static List<String> getSystemAppsPackageName(Context context) {
        List<String> appsPackageName = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps =
                pm.queryIntentActivities(mainIntent, PackageManager.MATCH_DISABLED_COMPONENTS);
        for (ResolveInfo app :
                apps) {
            appsPackageName.add(app.activityInfo.packageName);
        }
        return appsPackageName;
    }

    /**
     * 获取所有系统预装应用，包含名称和版本信息
     *
     * {@link #getAllAppsInfo(Context)}
     * {@link #getSystemAppsPackageName(Context)}
     * 依赖于以上两个方法。
     *
     * @param context
     * @return 系统预装应用列表，并按照应用名称排序
     */
    public static List<AppVersionMember> getAppsWithVersionInfo(Context context) {
        List<AppVersionMember> systemAppsVersion = new ArrayList<>();
        List<String> appsPackageName = getSystemAppsPackageName(context);
        List<com.blankj.utilcode.utils.AppUtils.AppInfo> appsInfo = getAllAppsInfo(context);

        for (String appName :
                appsPackageName) {
            for (com.blankj.utilcode.utils.AppUtils.AppInfo appInfo :
                    appsInfo) {
                if (appName.equals(appInfo.getPackageName()) && appInfo.isSystem()) {
                    systemAppsVersion.add(new AppVersionMember(appInfo.getName(), appInfo.getVersionName()));
                    appsInfo.remove(appInfo);
                    break;
                }
            }
        }
        Collections.sort(systemAppsVersion);
        return systemAppsVersion;
    }
    public static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 1;
    public static boolean hasPermission(Context context, String[] permissions) {
        Trace.beginSection("hasPermission");
        try {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } finally {
            Trace.endSection();
        }
    }

    public static void requestPermission(Context context, String[] permissions) {
        if (!(context instanceof Activity)) {
            return;
        }
        Trace.beginSection("requestPermissions");
        try {
            // Construct a list of missing permissions
            final ArrayList<String> unsatisfiedPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    unsatisfiedPermissions.add(permission);
                }
            }
            if (unsatisfiedPermissions.size() == 0) {
                throw new RuntimeException("Request permission activity was called even"
                        + " though all permissions are satisfied.");
            }
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    unsatisfiedPermissions.toArray(new String[unsatisfiedPermissions.size()]),
                    PERMISSIONS_REQUEST_ALL_PERMISSIONS);
        } finally {
            Trace.endSection();
        }
    }


}
