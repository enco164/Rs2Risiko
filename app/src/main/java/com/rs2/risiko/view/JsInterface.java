package com.rs2.risiko.view;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by enco on 25.8.16..
 */
public class JsInterface {
    Context context;

    public JsInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
