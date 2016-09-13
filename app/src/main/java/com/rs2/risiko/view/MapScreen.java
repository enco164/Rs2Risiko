package com.rs2.risiko.view;

import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.rs2.risiko.MainActivity;
import com.rs2.risiko.R;
import com.rs2.risiko.data.Territory;
import com.rs2.risiko.game_logic.Game;

/**
 * Created by enco on 11.9.16..
 */
public class MapScreen  {

    private static final String TAG = "MapScreen";
    private final MainActivity activity;
    private final WebView webView;

    public MapScreen(MainActivity mainActivity, Game game) {
        activity = mainActivity;
        webView = (WebView) activity.findViewById(R.id.web);
//
//        webView.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//
//                return super.onJsAlert(view, url, message, result);
//            }
//        });
//
//        WebSettings settings = webView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        webView.addJavascriptInterface(new JsInterface(activity, game), "Android");
//
//        settings.setAllowUniversalAccessFromFileURLs(true);
//        settings.setAllowFileAccessFromFileURLs(true);
//        settings.setAllowFileAccess(true);
//
//        webView.loadUrl("file:///android_asset/web/index.html");
    }
//
//    @Override
//    public void updateTerritory(Territory territory) {
//        final String id = territory.getId();
//        final String color = "#" + activity.getResources().getString(territory.getPlayer().getColor()).substring(3);
//        Log.d(TAG, color);
//
//        webView.post(new Runnable() {
//            @Override
//            public void run(){
//                webView.loadUrl("javascript:updateTerritory('" + id + "', {color:'" + color + "'})");
//            }
//        });
//    }
//
//    @Override
//    public void selectTerritory(Territory territory) {
//        final String id = territory.getId();
//        final String color = "#" + activity.getResources().getString(territory.getPlayer().getColor()).substring(3);
//
//        webView.post(new Runnable() {
//            @Override
//            public void run(){
//                webView.loadUrl("javascript:updateTerritory('" + id + "', {select: true})");
//            }
//        });
//    }
}
