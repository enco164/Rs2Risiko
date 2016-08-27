package com.rs2.risiko.view;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.android.gms.games.Game;
import com.rs2.risiko.game_logic.GameMain;

/**
 * Created by enco on 25.8.16..
 */
public class JsInterface {
    private static String TAG = "JsInterface";
    private String alreadySelected;
    Context context;
    private GameMain gameMain;

    public JsInterface(Context context, GameMain gameMain) {
        this.context = context;
        this.gameMain = gameMain;
        this.alreadySelected = null;
    }

    public void setGameMain(GameMain gameMain) {
        this.gameMain = gameMain;
    }

    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void log(String tag, String msg) { Log.d(tag,msg);}


    @JavascriptInterface
    public void initGameBoard(){
        gameMain.initGameBoard();
    }
    @JavascriptInterface
    public void selectedTeritory(String id){
        Log.d(TAG, "klkinuto: " + id);
        if(!gameMain.checkMyTerritory(id)) return;
        /* ako je na redu napadanje. Treba ovde dosta provera, ovo je samo da vidim da li radi */
        if(alreadySelected == null) {
            Log.d("provera", "vec jeste " + alreadySelected);
            alreadySelected = id;
            gameMain.setAttackSource(id);
        }else{
            if(!id.equals(alreadySelected)) return;
            gameMain.removeAttackSource(alreadySelected);
            alreadySelected = null;
        }
    }


}
