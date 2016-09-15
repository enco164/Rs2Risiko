package com.rs2.risiko.game_logic;

import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.gson.Gson;
import com.rs2.risiko.MainActivity;
import com.rs2.risiko.data.Card;
import com.rs2.risiko.data.GameData;
import com.rs2.risiko.data.Goal;
import com.rs2.risiko.data.PlayerRisk;
import com.rs2.risiko.data.Territory;
import com.rs2.risiko.data.User;
import com.rs2.risiko.util.ParcelableUtil;

import static com.rs2.risiko.util.Constants.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Created by enco on 10.9.16..
 */
public class Game {

    private static final String TAG = "Game";

    private int mCurrentPlayer;
    private Room mRoom;
    private ArrayList<PlayerRisk> mPlayers;
    private ArrayList<Territory> mTerritories;
    private String mMyId;
    private GameData gd;

    public Game(Room room, String myId, MainActivity activityWithCallback, List<String> colors) {
        mRoom = room;
        mMyId = myId;
        mCallback = activityWithCallback;
        chooseFirstPlayer(colors);
    }


    private void chooseFirstPlayer(List<String> colors) {
        List<String> ids = mRoom.getParticipantIds().subList(0, mRoom.getParticipantIds().size());
        Collections.sort(ids);
        if (!Objects.equals(mMyId, ids.get(0))) {
            // nisam "sudija"
            return;
        }

        // mesamo zadatke
        ArrayList<Goal> goals = Goal.getAllGoals();
        Collections.shuffle(goals);

        // mesamo igrace
        Collections.shuffle(ids);

        // mesamo boje
        Collections.shuffle(colors);

        // kreiramo igrace
        List<User> users = new ArrayList<>();
        for (int i = 0; i < ids.size(); ++i) {
            users.add(new User(ids.get(i), colors.get(i), goals.get(i), null));
        }

        // mesamo karte
        ArrayList<Card> cards = Card.getAllCards();
        Collections.shuffle(cards);

        // postavljamo prvog igraca
        String firstPlayer = ids.get(0);

        // mesamo teritorije i dodeljujemo im igrace
        ArrayList<Territory> territories = Territory.getAllTerritories();
        Collections.shuffle(territories);
        for (int i = 0; i < territories.size(); i++) {
            territories.get(i).setUserId(ids.get((i%ids.size())));
            territories.get(i).setArmies(1);
        }

        gd = new GameData(territories, users, cards, firstPlayer, GameData.State.INIT_PLACING_ARMIES);
//        String json = new Gson().toJson(gd);
//        Log.d(TAG, json);
        byte[] data = ParcelableUtil.marshall(gd);
        try {
            Log.d(TAG, new String(data, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mCallback.broadcast(data);
    }

//    public void updateTerritories() {
//        for (Territory territory: mTerritories) {
//            mCallback.updateTerritory(territory);
//        }
//    }
//
//    @Override
//    public void onTerritoryClick(String territoryId) {
//        if (!isMyTurn()) {
//            Log.d(TAG, "Nije moj red za igru");
//            nextPlayer();
//            return;
//        }
//
//        switch (currentState) {
//            case POSTAVLJANJE_PO_3_TENKICA:
//
//                break;
//        }
//
//        Log.d(TAG, "onTerritoryClick: " + territoryId);
//        Territory selectedTerritory = null;
//        for (Territory t : mTerritories) {
//            if (t.getId().equalsIgnoreCase(territoryId)) {
//                selectedTerritory = t;
//                break;
//            }
//        }
//
//        if (selectedTerritory != null) {
//            mCallback.selectTerritory(selectedTerritory);
//        }
//
//    }

//    @Override
//    public void onWebviewLoaded() {
//        updateTerritories();
//    }

//    private boolean isMyTurn() {
//        // TODO: treba uporediti id trenutnog korisnika sa id onog ko je na potezu
//        return myPlayer.getParticipantMock().equalsIgnoreCase(
//                mPlayers.get(mCurrentPlayer).getParticipantMock());
//    }

//    private void nextPlayer(){
//        mCurrentPlayer = (mCurrentPlayer + 1) % mPlayers.size();
//    }

    private GameCallbacks mCallback;

    public interface GameCallbacks {
        void broadcast(byte[] data);
    }

    public void applyData(GameData gd) {

        this.gd = gd;

        switch (gd.getGameState()) {
            case INIT_PLACING_ARMIES:
                Log.d(TAG, "INIT_PLACING_ARMIES");
                Log.d(TAG, gd.toString());
                break;
        }
    }

}
