package com.rs2.risiko.view;

import android.graphics.Color;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.rs2.risiko.MainActivity;
import com.rs2.risiko.R;
import com.rs2.risiko.data.GameData;
import com.rs2.risiko.data.Territory;
import com.rs2.risiko.data.User;
import com.rs2.risiko.game_logic.Game;

import java.util.Iterator;
import java.util.List;

/**
 * Created by enco on 11.9.16..
 */
public class MapScreen  {

    private static final String TAG = "MapScreen";
    private final MainActivity activity;
    private final WebView webView;
    private final TextView statusTextView;

    public MapScreen(MainActivity mainActivity, JsInterface.JsCallbacks jsCallbacks) {
        activity = mainActivity;
        statusTextView = (TextView) activity.findViewById(R.id.map_status_text);
        webView = (WebView) activity.findViewById(R.id.web);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {

                return super.onJsAlert(view, url, message, result);
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        webView.addJavascriptInterface(new JsInterface(activity, jsCallbacks), "Android");

        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowFileAccess(true);

        webView.loadUrl("file:///android_asset/web/index.html");
    }

    public void updateMap(GameData gameData) {
        List<User> users = gameData.getUsers();
        for (final Territory t : gameData.getTerritories()) {
            for (final User u : users) {
                if (u.getUserId().equals(t.getUserId())) {
                    webView.post(new Runnable() {
                        @Override
                        public void run(){
                            webView.loadUrl("javascript:updateTerritory('" + t.getId() + "', " +
                                    "{color:'" + u.getColor() + "', armies:'"+t.getArmies() + " '})");
                        }
                    });
                }
            }
        }
    }

    public void lockMap() {
        webView.post(new Runnable() {
            @Override
            public void run(){
                webView.loadUrl("javascript:setLocked(true)");
            }
        });
    }

    public void unlockMap() {
        webView.post(new Runnable() {
            @Override
            public void run(){
                webView.loadUrl("javascript:setLocked(false)");
            }
        });
    }

    public void setStatusText(final String s) {
        statusTextView.post(new Runnable() {
            @Override
            public void run() {
                statusTextView.setText(s);
            }
        });
    }

    public void setStatusColor(final String color) {
        statusTextView.post(new Runnable() {
            @Override
            public void run() {
                statusTextView.setTextColor(Color.parseColor(color));
            }
        });
    }
}
