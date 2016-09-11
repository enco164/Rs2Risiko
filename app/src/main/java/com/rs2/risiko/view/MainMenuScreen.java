package com.rs2.risiko.view;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.rs2.risiko.MainActivity;
import com.rs2.risiko.R;

import static com.rs2.risiko.util.Constants.*;

/**
 * Created by enco on 7.9.16..
 */
public class MainMenuScreen implements View.OnClickListener {
    private static final String TAG = "MainMenuScreen";

    // This array lists everything that's clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            /*R.id.button_accept_popup_invitation,*/ R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
            R.id.button_sign_out,  R.id.button_show_web_view, R.id.button_test
    };

    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait, R.id.web
    };


    private final MainActivity activity;

    int mCurScreen;

    public MainMenuScreen(MainActivity mainActivity, MyCallbacks callbacks) {
        this.activity = mainActivity;
        // set up a click listener for everything we care about
        for (int id : CLICKABLES) {
            this.activity.findViewById(id).setOnClickListener(this);
        }
        mCurScreen = -1;

        mCallbacks = callbacks;
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.button_test:
                Log.d(TAG, "TESTIRANJE");
                mCallbacks.onButtonTest();
//                switchToWebScreen();
                break;
            case R.id.button_show_web_view:
                activity.onButtonShowWebView();
                mCallbacks.onButtonShowWebView();
//                activity.getGame().resetGameVars();
                switchToWebScreen();
                break;
            case R.id.button_sign_in:
                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                mCallbacks.onButtonSignIn();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                // sign out.
                Log.d(TAG, "Sign-out button clicked");
                mCallbacks.onButtonSignOut();
                switchToSingInScreen();
                break;
            case R.id.button_invite_players:
                // show list of invitable players
                switchToWaitScreen();
                mCallbacks.onButtonInvitePlayers();
                break;
            case R.id.button_see_invitations:
                // show list of pending invitations
                switchToWaitScreen();
                mCallbacks.onButtonSeeInvitations();
                break;
//            case R.id.button_accept_popup_invitation:
//                // user wants to accept the invitation shown on the invitation popup
//                // (the one we got through the OnInvitationReceivedListener).
//                activity.acceptInviteToRoom(activity.getIncomingInvitationId());
//                activity.setIncomingInvitationId(null);
//                break;
            case R.id.button_quick_game:
                mCallbacks.onButtonQuickGame();
                // user wants to play against a random opponent right now
                // activity.getGame().startQuickGame();
                break;
        }
    }

    public void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            activity.findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // should we show the invitation popup? TODO: Da li ovako implementiramo pozivnice?
//        boolean showInvPopup;
//        if (activity.getIncomingInvitationId() == null) {
//            // no invitation, so no popup
//            showInvPopup = false;
//        } else if (activity.getGame().isMultiplayer()) {
//            // if in multiplayer, only show invitation on main screen
//            showInvPopup = (mCurScreen == R.id.screen_main);
//        } else {
//            // single-player: show on main screen and gameplay screen
//            showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
//        }
//        activity.findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    public void switchToWebScreen() {
        this.switchToScreen(R.id.web);
    }

    public void switchToMainMenuScreen() {
        if (mCallbacks.isConnected()) {
            switchToScreen(R.id.screen_main);
            return;
        }
        switchToScreen(R.id.screen_sign_in);
    }

    public void switchToWaitScreen() {
        keepScreenOn();
        this.switchToScreen(R.id.screen_wait);
    }

    public void switchToSingInScreen() {
        this.switchToScreen(R.id.screen_sign_in);
    }

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    public void keepScreenOn() {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    public void stopKeepingScreenOn() {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public int getCurrentScreen() {
        return mCurScreen;
    }

    /**
     * Interface to communicate to GoogleApiCallbacks
     */
    private static MyCallbacks mCallbacks;


    public interface MyCallbacks {

        void onButtonQuickGame();

        void onButtonTest();

        void onButtonShowWebView();

        void onButtonSignIn();

        void onButtonSeeInvitations();

        void onButtonInvitePlayers();

        void onButtonSignOut();

        boolean isConnected();
    }
}
