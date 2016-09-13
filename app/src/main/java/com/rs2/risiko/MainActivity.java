package com.rs2.risiko;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.gson.Gson;
import com.rs2.risiko.data.GameData;
import com.rs2.risiko.game_logic.Game;
import com.rs2.risiko.networking.GoogleApiCallbacks;
import com.rs2.risiko.view.MainMenuScreen;
import com.rs2.risiko.view.MapScreen;

import static com.rs2.risiko.util.Constants.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements
         GoogleApiCallbacks.MyCallbacks, Game.GameCallbacks {

    private static final String TAG = "MainActivity";

    MainMenuScreen mainMenuScreen;
    private GoogleApiCallbacks googleApiCallbacks;
    private Room mRoom;
    private Game mGame;
    private MapScreen mMapScreen;

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

        if (requestCode / 1000 > 9)
            googleApiCallbacks.activityResult(requestCode, responseCode, intent);


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
        mRoom = room;
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

    @Override
    public void startGame() {
        Log.d(TAG, "Start Game Called");
        // boje ovde hvatamo zbog konteksta
        List<String> colors = new ArrayList<>();
        for (int PLAYER_COLOR : PLAYER_COLORS) {
            String string = getResources().getString(PLAYER_COLOR);
            colors.add("#" + string.substring(3));
        }
        mGame = new Game(mRoom, getMyId(), this, colors);

    }

    @Override
    public void realTimeMessageReceived(String json) {
        mGame.applyData(new Gson().fromJson(json, GameData.class));
    }

    public String getMyId() {
        return mRoom.getParticipantId(
                Games.Players.getCurrentPlayerId(
                        googleApiCallbacks.googleApiClient()));
    }

    public void onButtonShowWebView() {
//        mGame = new Game(mRoom);
//        mMapScreen = new MapScreen(this, mGame);
//        mGame.attach(mMapScreen);
    }

    @Override
    public void broadcast(String json) {
        try {
            googleApiCallbacks.broadcast(mRoom, json.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
