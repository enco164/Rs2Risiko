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
 * Created by enco on 27.8.16..
 */
public class Screen implements View.OnClickListener {

    private static final String TAG = "Screen";
    int mCurScreen = -1;

    protected MainActivity activity;
    private WebView webView;

    public Screen(MainActivity activity) {
        this.activity = activity;
        // set up a click listener for everything we care about
        for (int id : CLICKABLES) {
            this.activity.findViewById(id).setOnClickListener(this);
        }
        setupWebView();
    }

    public void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            activity.findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        if (activity.getIncomingInvitationId() == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (activity.isMultiplayer()) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
        }
        activity.findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    public void switchToMainMenuScreen() {
        if (activity.getGoogleApiClient() != null && activity.getGoogleApiClient().isConnected()) {
            switchToScreen(R.id.screen_main);
        }
        else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    public void switchToWaitScreen() {
        keepScreenOn();
        this.switchToScreen(R.id.screen_wait);
    }

    public void switchToGameScreen() {
        this.switchToScreen(R.id.screen_game);
    }

    public void switchToWebScreen() {
        webView.loadUrl("javascript:pozivIzJave('Willkommen from Javen')");
        this.switchToScreen(R.id.web);
    }

    public void switchToSingInScreen() {
        this.switchToScreen(R.id.screen_sign_in);
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    public void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(activity.getGoogleApiClient(), room, MIN_PLAYERS);

        // show waiting room UI
        activity.startActivityForResult(i, RC_WAITING_ROOM);
    }

    private void setupWebView() {
        webView = (WebView) activity.findViewById(R.id.web);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d(TAG, message);
                return super.onJsAlert(view, url, message, result);
            }
        });
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JsInterface(activity), "Android");

        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowFileAccess(true);

        webView.loadUrl("file:///android_asset/web/index.html");
    }

    // This array lists everything that's clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
            R.id.button_sign_out, R.id.button_click_me, R.id.button_single_player,
            R.id.button_single_player_2, R.id.button_show_web_view
    };

    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait, R.id.web
    };

    public int getCurScreen() {
        return mCurScreen;
    }

    /*
     * MISC SECTION. Miscellaneous methods.
     */


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

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.button_show_web_view:
                switchToWebScreen();
                break;
            case R.id.button_single_player:
            case R.id.button_single_player_2:
                // play a single-player game
                activity.resetGameVars();
                activity.startGame(false);
                break;
            case R.id.button_sign_in:
                // user wants to sign in
                // Check to see the developer who's running this sample code read the instructions :-)
                // NOTE: this check is here only because this is a sample! Don't include this
                // check in your actual production app.
                if (!BaseGameUtils.verifySampleSetup(activity, R.string.app_id)) {
                    Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
                }

                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                activity.setSignInClicked(true);
                activity.getGoogleApiClient().connect();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                // sign out.
                Log.d(TAG, "Sign-out button clicked");
                activity.setSignInClicked(false);
                Games.signOut(activity.getGoogleApiClient());
                activity.getGoogleApiClient().disconnect();
                switchToSingInScreen();
                break;
            case R.id.button_invite_players:
                // show list of invitable players
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(activity.getGoogleApiClient(), 1, 3);
                switchToWaitScreen();
                activity.startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case R.id.button_see_invitations:
                // show list of pending invitations
                intent = Games.Invitations.getInvitationInboxIntent(activity.getGoogleApiClient());
                switchToWaitScreen();
                activity.startActivityForResult(intent, RC_INVITATION_INBOX);
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                activity.acceptInviteToRoom(activity.getIncomingInvitationId());
                activity.setIncomingInvitationId(null);
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                activity.startQuickGame();
                break;
            case R.id.button_click_me:
                // (gameplay) user clicked the "click me" button
                activity.scoreOnePoint();
                break;
        }
    }
}
