package com.rs2.risiko.game_logic;

import android.util.Log;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.rs2.risiko.MainActivity;
import com.rs2.risiko.data.Card;
import com.rs2.risiko.data.GameData;
import com.rs2.risiko.data.Goal;
import com.rs2.risiko.data.Territory;
import com.rs2.risiko.data.User;
import com.rs2.risiko.util.ParcelableUtil;
import com.rs2.risiko.view.JsInterface;
import com.rs2.risiko.view.MapScreen;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by enco on 10.9.16..
 */
public class Game implements JsInterface.JsCallbacks {

    private static final String TAG = "Game";
    private MainActivity activity;

    private Room mRoom;
    private GameData gameData;
    private MapScreen mapScreen;
    private String myId;
    private GameData gameDataNetworkCopy;

    public Game(Room room, String myId, MainActivity activityWithCallback, List<String> colors) {
        mRoom = room;
        this.myId = myId;
        mCallback = activityWithCallback;
        chooseFirstPlayer(colors);
        activity = activityWithCallback;
    }


    private void chooseFirstPlayer(List<String> colors) {
        List<String> ids = mRoom.getParticipantIds().subList(0, mRoom.getParticipantIds().size());
        Collections.sort(ids);
        if (!Objects.equals(myId, ids.get(0))) {
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

        GameData gd = new GameData(territories, users, cards, firstPlayer, GameData.State.INIT_PLACING_ARMIES);
        byte[] data = ParcelableUtil.marshall(gd);
        mCallback.broadcast(data);
    }

    @Override
    public void onTerritoryClick(String territoryId) {
        switch (gameData.getGameState()) {
            case INIT_PLACING_ARMIES:
                if (!gameData.isMyTerritory(myId, territoryId)) {
                    Log.d(TAG, "Not my territory!");
                    return;
                }
                // postavljamo po jednog tenkica
                Territory t = gameData.getTerritory(territoryId);
                t.setArmies(t.getArmies() + 1);
                gameData.setArmiesToPlace(gameData.getArmiesToPlace() - 1);
                Log.d(TAG, t.getName() + ": " + t.getArmies() + " armies");
                Log.d(TAG, "Armies to place: " + gameData.getArmiesToPlace());

                // ako smo sve postavili saljemo nas objekat na sinhronizaciju
                if (gameData.getArmiesToPlace() == 0) {
                    gameDataNetworkCopy.setIsFinishInitPlacingArmiesFor(myId);

                    // proveravamo da li su svi zavrsili
                    boolean everyoneFinished = true;
                    for (Map.Entry<String, Boolean> entry
                            : gameDataNetworkCopy.getIsFinishInitPlacingArmies().entrySet()) {
                        if (!entry.getValue()) {
                            everyoneFinished = false;
                            break;
                        }
                    }
                    if (everyoneFinished) {
                        gameDataNetworkCopy.setGameState(GameData.State.GAME);
                    }
                    mergeGameData();
                    gameData = gameDataNetworkCopy;
                    mCallback.broadcast(gameData.getByteArray());
                }
                break;
        }

    }

    @Override
    public void onWebviewLoaded() {
        mapScreen.updateMap(gameData);
    }

    private GameCallbacks mCallback;
    public interface GameCallbacks {
        void broadcast(byte[] data);

        void gameStarted();
    }

    public void applyData(GameData gd) {
        // update podataka
        // ako je gameData null onda je to pocetno stanje koje preuzimamo od "sudije"
        if (gameData == null) {
            gameData = gd;
            gameDataNetworkCopy = gd;
            mapScreen = new MapScreen(activity, this);
            mCallback.gameStarted();
            Log.d(TAG, "INIT_PLACING_ARMIES");
            gameData.setArmiesToPlace(getInitArmies());
            // Prikazivanje obavestenja korisniku da postavi tenkice
            String dialogText = "Your goal is: " + gameData.getUser(myId).getGoal().getDescription();
            dialogText += "\nPlace " + gameData.getArmiesToPlace() + " armies on your territories";
            BaseGameUtils.makeSimpleDialog(activity, dialogText).show();
            return;
        }

        switch (gameData.getGameState()) {
            case INIT_PLACING_ARMIES:
                gameDataNetworkCopy = gd;
                break;
            case GAME:
                String dialogText = "First player is: " + mRoom.getParticipant(gameData.getUsers().get(0).getUserId()).getDisplayName();
                BaseGameUtils.makeSimpleDialog(activity, dialogText).show();

        }
    }

    private void mergeGameData() {
        for (Territory t : gameData.getTerritories()) {
            if (t.getUserId().equals(myId)) {
                gameDataNetworkCopy.getTerritory(t.getId()).setArmies(t.getArmies());
            }
        }
    }

    private int getInitArmies() {
        int usersSize = gameData.getUsers().size();
        int myTerritoriesCount = 0;
        for (Territory t : gameData.getTerritories()) {
            if (t.getId().equals(myId)) {
                myTerritoriesCount++;
            }
        }

        //vracamo umanjeni broj jer je na svakoj teritoriji vec postavljen po jedan tenkic
        return (usersSize == 6 ? 20 :
                usersSize == 5 ? 25 :
                usersSize == 4 ? 30 :
                usersSize == 3 ? 35 : 40) - myTerritoriesCount;
    }

}
