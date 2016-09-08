package com.rs2.risiko;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.rs2.risiko.networking.GoogleApiCallbacks;
import com.rs2.risiko.view.MainMenuScreen;


import static com.rs2.risiko.util.Constants.*;

public class MainActivity extends Activity implements
         GoogleApiCallbacks.MyCallbacks {

    private static final String TAG = "MainActivity";

    MainMenuScreen mainMenuScreen;
    private GoogleApiCallbacks googleApiCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the Google Api Client with access to Games
        googleApiCallbacks = new GoogleApiCallbacks(this);

        mainMenuScreen = new MainMenuScreen(this, googleApiCallbacks);
    }

    // Activity just got to the foreground. We switch to the wait screen because we will now
    // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
    // foreground we go through the sign-in flow -- but if the user is already authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {
        mainMenuScreen.switchToWaitScreen();
        if (googleApiCallbacks.googleApiClient() != null &&
                googleApiCallbacks.googleApiClient().isConnected()) {
            Log.w(TAG,
                    "GameHelper: client was already connected on onStart()");
             mainMenuScreen.switchToMainMenuScreen();
        } else {
            Log.d(TAG,"Connecting client.");
            googleApiCallbacks.googleApiClient().connect();
        }
        super.onStart();
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        googleApiCallbacks.leaveRoom();

        // stop trying to keep the screen on
        mainMenuScreen.stopKeepingScreenOn();

        if (googleApiCallbacks.googleApiClient() == null ||
                !googleApiCallbacks.googleApiClient().isConnected())
        {
            mainMenuScreen.switchToSingInScreen();
        }
        else {
            mainMenuScreen.switchToWaitScreen();
        }
        super.onStop();
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public void onBackPressed() {
        if (mainMenuScreen.getCurrentScreen() == R.id.screen_game ||
                mainMenuScreen.getCurrentScreen() == R.id.web) {
            googleApiCallbacks.leaveRoom();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                googleApiCallbacks.handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                googleApiCallbacks.handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
//                    game.startGame(true);
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    googleApiCallbacks.leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    googleApiCallbacks.leaveRoom();
                }
                break;
            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                googleApiCallbacks.setSignInClicked(false);
                googleApiCallbacks.setResolvingConnectionFailure(false);
                if (responseCode == RESULT_OK) {
                    googleApiCallbacks.googleApiClient().connect();
                } else {
                    BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    @Override
    public void connected() {
        mainMenuScreen.switchToMainMenuScreen();
    }

    @Override
    public void connectionFailed() {
        mainMenuScreen.switchToSingInScreen();
    }

    @Override
    public void leftRoom() {
        mainMenuScreen.switchToMainMenuScreen();
    }

    @Override
    public void roomConnected(Room room) {

        //updateRoom(room);
    }

    @Override
    public void leaveRoom(boolean isInRoom) {
        mainMenuScreen.stopKeepingScreenOn();
        if (isInRoom) {
            mainMenuScreen.switchToWaitScreen();
        } else {
            mainMenuScreen.switchToMainMenuScreen();
        }
    }

    @Override
    public void selectPlayerResult(boolean isError) {
        if (isError) {
            mainMenuScreen.switchToMainMenuScreen();
            return;
        }
        mainMenuScreen.switchToWaitScreen();
    }

    @Override
    public void showMainMenu() {
        mainMenuScreen.switchToMainMenuScreen();
    }

    @Override
    public void invitationInboxResult(boolean isError) {
        if (isError) {
            mainMenuScreen.switchToMainMenuScreen();
            return;
        }
        mainMenuScreen.switchToWaitScreen();
//        game.resetGameVars();
    }

}
