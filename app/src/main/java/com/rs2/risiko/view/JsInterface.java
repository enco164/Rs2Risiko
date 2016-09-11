package com.rs2.risiko.view;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by enco on 11.9.16..
 */
public class JsInterface {
    private Context context;

    public JsInterface(Context context, JsCallbacks game) {
        this.context = context;
        mCallbacks = game;
    }

    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void log(String tag, String msg) { Log.d(tag,msg);}

    @JavascriptInterface
    public void onTerritory(String id) {
        mCallbacks.onTerritoryClick(id);
    }

    @JavascriptInterface
    public void onReady(){
        mCallbacks.onWebviewLoaded();
    }

    /**
     * Interface to communicate to GoogleApiCallbacks
     */
    private static JsCallbacks mCallbacks;


    public interface JsCallbacks {
        void onTerritoryClick(String territoryId);

        void onWebviewLoaded();
    }
}
