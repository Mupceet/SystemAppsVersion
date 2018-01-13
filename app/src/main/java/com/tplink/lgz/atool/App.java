package com.tplink.lgz.atool;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

/**
 * Created by lgz on 1/13/18.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
