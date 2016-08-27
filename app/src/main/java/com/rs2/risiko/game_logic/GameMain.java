package com.rs2.risiko.game_logic;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.rs2.risiko.MainActivity;
import com.rs2.risiko.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.rs2.risiko.util.Constants.*;

/**
 * Created by enco on 27.8.16..
 */
public class GameMain {

    // Current state of the game:
    int mSecondsLeft = -1; // how long until the game ends (seconds)
    int mScore = 0; // user's current score
    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantScore = new HashMap<>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<>();

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    private final MainActivity activity;
    private boolean mMultiplayer;

    // My participant ID in the currently active game
    String mMyId = null;

    public GameMain(MainActivity mainActivity) {
        this.activity = mainActivity;
    }

    public void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 3;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(activity);
        rtmConfigBuilder.setMessageReceivedListener(activity);
        rtmConfigBuilder.setRoomStatusUpdateListener(activity);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        activity.getScreen().switchToWaitScreen();
        resetGameVars();
        Games.RealTimeMultiplayer.create(activity.getGoogleApiClient(), rtmConfigBuilder.build());
    }


    // Reset game variables in preparation for a new game.
    public void resetGameVars() {
        mSecondsLeft = GAME_DURATION;
        mScore = 0;
        mParticipantScore.clear();
        mFinishedParticipants.clear();
    }

    // Start the gameplay phase of the game.
    public void startGame(boolean multiplayer) {
        mMultiplayer = multiplayer;
        activity.getScreen().updateScoreDisplay();
        activity.broadcastScore(false);
        activity.getScreen().switchToGameScreen();

        activity.findViewById(R.id.button_click_me).setVisibility(View.VISIBLE);

        // run the gameTick() method every second to update the game.
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSecondsLeft <= 0)
                    return;
                gameTick();
                h.postDelayed(this, 1000);
            }
        }, 1000);
    }

    // Game tick -- update countdown, check if game ended.
    void gameTick() {
        if (mSecondsLeft > 0)
            --mSecondsLeft;

        // update countdown
        ((TextView) activity.findViewById(R.id.countdown)).setText("0:" +
                (mSecondsLeft < 10 ? "0" : "") + String.valueOf(mSecondsLeft));

        if (mSecondsLeft <= 0) {
            // finish game
            activity.findViewById(R.id.button_click_me).setVisibility(View.GONE);
            activity.broadcastScore(true);
        }
    }

    // indicates the player scored one point
    public void scoreOnePoint() {
        if (mSecondsLeft <= 0)
            return; // too late!
        ++mScore;
        activity.getScreen().updateScoreDisplay();
        activity.getScreen().updatePeerScoresDisplay();

        // broadcast our new score to our peers
        activity.broadcastScore(false);
    }

    public int getScore() {
        return mScore;
    }

    public void setSecondsLeft(int secondsLeft) {
        this.mSecondsLeft = secondsLeft;
    }

    public boolean isMultiplayer() {
        return mMultiplayer;
    }

    public Map<String, Integer> getmParticipantScore() {
        return mParticipantScore;
    }

    public Set<String> getmFinishedParticipants() {
        return mFinishedParticipants;
    }

    public ArrayList<Participant> getParticipants() {
        return mParticipants;
    }

    public void setParticipants(ArrayList<Participant> participants) {
        this.mParticipants = participants;
    }

    public void setMyId(String mMyId) {
        this.mMyId = mMyId;
    }

    public String getMyId() {
        return mMyId;
    }
}