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
        settings.setDisplayZoomControls(false);
        webView.addJavascriptInterface(new JsInterface(activity, jsCallbacks), "Android");

        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowFileAccess(true);

        webView.loadUrl("file:///android_asset/web/index.html");
    }

    public void updateMap(final GameData gameData) {


        List<User> users = gameData.getUsers();
        int id = 1;
        final int availArmies = gameData.getArmiesToPlace();

        for (final User u : users) {
            setPlayerColor(String.valueOf(id++), u.getName(), u.getColor());

        }

        setCurrentPlayer(gameData.getUsers().get(0).getName());
        Log.d("BOJE", "TRENUTNA: " + gameData.getUsers().get(0).getName());

        for (final Territory t : gameData.getTerritories()) {
            for (final User u : users) {

                if (u.getUserId().equals(t.getUserId())) {
                    webView.post(new Runnable() {
                        @Override
                        public void run(){
                            webView.loadUrl("javascript:updateTerritory('" + t.getId() + "', " +
                                    "{color:'" + u.getColor() + "', armies:'"+t.getArmies() +
                                    "', stars:'" + u.getStars() + "', availArmies:'" + availArmies + "'})");
                        }
                    });
                }
            }
        }
    }

    public void updateStars(final String userId, final GameData gd) {
        for (final User u : gd.getUsers()) {
            if (u.getUserId().equals(userId)) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:setNumOfStars("+u.getStars()+")");
                    }
                });
            }
        }
    }

    public void setArmies(final int armies) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:setNumOfAvailableArmies("+armies+")");
            }
        });
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
//        statusTextView.post(new Runnable() {
//            @Override
//            public void run() {
//                statusTextView.setText(s);
//            }
//        });
    }



    public void setStatusColor(final String color) {
        statusTextView.post(new Runnable() {
            @Override
            public void run() {
                statusTextView.setTextColor(Color.parseColor(color));
            }
        });
    }

    public void setPlayerColor(final String  id, final  String name, final String color){
        webView.post(new Runnable() {
            @Override
            public void run(){
                webView.loadUrl("javascript:setPlayerColor(" + id + ", '" + name + "','" + color + "')");
            }
        });
    }

    public void setCurrentPlayer(final String name){
        Log.d("BOJE", "javascript:setCurrentPlayer('" + name + "')");
        webView.post(new Runnable() {
            @Override
            public void run(){
                webView.loadUrl("javascript:setCurrentPlayer('" + name + "')");
            }
        });
    }

    public void setAvailableTanks(final String num){
        webView.post(new Runnable() {
            @Override
            public void run(){
                webView.loadUrl("javascript:setNumOfAvailableTanks(" + num + ")");
            }
        });
    }
}
