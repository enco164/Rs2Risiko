package com.rs2.risiko.game_logic;

import android.util.Log;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.rs2.risiko.data.PlayerRisk;
import com.rs2.risiko.data.Territory;
import com.rs2.risiko.view.JsInterface;
import com.rs2.risiko.view.MainMenuScreen;
import com.rs2.risiko.view.MapScreen;

import static com.rs2.risiko.util.Constants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by enco on 10.9.16..
 */
public class Game implements JsInterface.JsCallbacks {

    private static final String TAG = "Game";
    private final Room mRoom;
    private final ArrayList<PlayerRisk> mPlayers;
    private final ArrayList<Territory> mTerritories;

    public Game(Room room) {
        mRoom = room;

        // Kreiramo igrace prema sortiramnoj listi njihovog partivipantId
        // koji se menjaju svaki put kad se zapocne igra, tako da znamo da ce isto biti sortirani
        // na svakom uredjau koji startuje igru

//        List<String> ids = mRoom.getParticipantIds().subList(0, mRoom.getParticipantIds().size());

        // MOCK
        List<String> ids = Arrays.asList(
                "p_1111111",
                "p_2222222",
                "p_3333333",
                "p_4444444"
        );
        Collections.sort(ids);

        mPlayers = new ArrayList<PlayerRisk>();
        for (int i = 0; i < ids.size(); ++i) {
//            mPlayers.add(new PlayerRisk(mRoom.getParticipant(ids.get(i)), PLAYER_COLORS[i], 0));
            mPlayers.add(new PlayerRisk(null, PLAYER_COLORS[i], 0));
        }

        mTerritories = Territory.getAllTerritories();
        // mesamo teritorije
        Collections.shuffle(mTerritories);

        // dodeljujemo teritorije igracima
        int i = 0;
        for (Territory territory: mTerritories) {
            territory.setPlayer(mPlayers.get(i % mPlayers.size()));
            ++i;
        }

    }

    public void updateTerritories() {
        for (Territory territory: mTerritories) {
            mCallback.updateTerritory(territory);
        }
    }

    @Override
    public void onTerritoryClick(String territoryId) {
        if (!isMyTurn()) {
            return;
        }
        Log.d(TAG, "onTerritoryClick: " + territoryId);
        Territory selectedTerritory = null;
        for (Territory t : mTerritories) {
            if (t.getId().equalsIgnoreCase(territoryId)) {
                selectedTerritory = t;
                break;
            }
        }

        if (selectedTerritory != null) {
            mCallback.selectTerritory(selectedTerritory);
        }

    }

    @Override
    public void onWebviewLoaded() {
        updateTerritories();
    }

    private boolean isMyTurn() {
        // TODO: treba uporediti id trenutnog korisnika sa id onog ko je na potezu
        return true;
    }

    public void attach(GameCallbacks callback) {
        mCallback = callback;
    }
    private GameCallbacks mCallback;

    public interface GameCallbacks {

        void updateTerritory(Territory territory);

        void selectTerritory(Territory territory);
    }
}
