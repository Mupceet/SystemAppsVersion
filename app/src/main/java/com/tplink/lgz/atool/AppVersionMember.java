/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AppVersionMember.java
 *
 * 应用信息Bean
 *
 * Author laiguizhong@tplink.com.cn, Created at 2017-1-25
 *
 * Ver 1.0, 2017-1-25, 赖癸仲, Create file.
 */
package com.tplink.lgz.atool;

/**
 * 应用成员，包括名称和版本信息，可根据名称排序。
 */

public class AppVersionMember implements Comparable<AppVersionMember>{
    private String appName;
    private String appVersionName;

    public AppVersionMember(String name, String versionName) {
        this.appName = name;
        this.appVersionName = versionName;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    @Override
    public int compareTo(AppVersionMember o) {
        return this.appName.compareTo(o.getAppName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppVersionMember that = (AppVersionMember) o;

        if (!appName.equals(that.appName)) return false;
        return appVersionName.equals(that.appVersionName);

    }

    @Override
    public int hashCode() {
        int result = appName.hashCode();
        result = 31 * result + appVersionName.hashCode();
        return result;
    }
}
