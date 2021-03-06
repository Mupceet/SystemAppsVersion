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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;


import com.blankj.utilcode.util.AppUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 获取应用信息
 */

public class AppInfoUtils {

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
     * <p>
     * {@link #getSystemAppsPackageName(Context)}
     * 依赖于以上两个方法。
     *
     * @param context
     * @return 系统预装应用列表，并按照应用名称排序
     */
    public static List<AppVersionMember> getAppsWithVersionInfo(Context context) {
        List<AppVersionMember> systemAppsVersion = new ArrayList<>();
        List<String> appsPackageName = getSystemAppsPackageName(context);
        List<AppUtils.AppInfo> appsInfo = AppUtils.getAppsInfo();

        for (String appName :
                appsPackageName) {
            for (AppUtils.AppInfo appInfo :
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

}
