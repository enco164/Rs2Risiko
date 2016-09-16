package com.rs2.risiko.game_logic;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.rs2.risiko.MainActivity;
import com.rs2.risiko.data.GameData;
import com.rs2.risiko.data.Goal;
import com.rs2.risiko.data.Territory;
import com.rs2.risiko.data.User;
import com.rs2.risiko.util.ParcelableUtil;
import com.rs2.risiko.view.JsInterface;
import com.rs2.risiko.view.MapScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

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
    private int armiesToPlace;
    private String attackerTerritoryId;
    private String defenderTerritoryId;

    public Game(Room room, String myId, MainActivity activityWithCallback, List<String> colors) {
        mRoom = room;
        this.myId = myId;
        mCallback = activityWithCallback;
        chooseFirstPlayer(colors);
        activity = activityWithCallback;
        this.armiesToPlace = 0;
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
        List<User> users = new LinkedList<>();
        for (int i = 0; i < ids.size(); ++i) {
            users.add(new User(ids.get(i), colors.get(i), goals.get(i)));
        }

        // mesamo teritorije i dodeljujemo im igrace
        ArrayList<Territory> territories = Territory.getAllTerritories();
        Collections.shuffle(territories);
        for (int i = 0; i < territories.size(); i++) {
            territories.get(i).setUserId(ids.get((i%ids.size())));
            territories.get(i).setArmies(1);
        }

        GameData gd = new GameData(territories, users, GameData.State.INIT_PLACING_ARMIES);
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
                addOneArmyToTerritory(territoryId);

                // ako smo sve postavili saljemo nas objekat na sinhronizaciju
                if (armiesToPlace == 0) {
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
                        gameDataNetworkCopy.setGameState(GameData.State.GAME_TURN_BEGINNING);
                    }
                    mergeGameData();
                    gameData = gameDataNetworkCopy;
                    mCallback.broadcast(gameData.getByteArray());
                }
                break;
            case GAME_PLACING_ARMIES:
                if (!gameData.isMyTerritory(myId, territoryId)) {
                    Log.d(TAG, "Not my territory!");
                    return;
                }
                addOneArmyToTerritory(territoryId);
                mapScreen.lockMap();
                mCallback.broadcast(gameData.getByteArray());
                break;
            case GAME_ATTACK:
                Territory territory = gameData.getTerritory(territoryId);

                // ako je moja teritorija i imam vise od jedne armije cuvam je kao napadacku
                if (territory.getUserId().equals(myId) && territory.getArmies() > 1) {
                    attackerTerritoryId = territoryId;
                    return;
                }

                // u suprotnom je protivnikova

                // ako nije izabrana napadacka teritorija izbacujem gresku
                if (attackerTerritoryId == null) {
                    // TODO napraviti neki sistem za greske
                    Log.d(TAG, "Mora prvo da se selektuje teritorija s koje se napada");
                    return;
                }

                // Ako nisu susedne teritorije izbaciti gresku
                if (!areNeighboringTerritories(territoryId)) {
                    Log.d(TAG, "Nisu susedne teritorije");
                    return;
                }

                // moze da se napada
                defenderTerritoryId = territoryId;
                mapScreen.lockMap();
                doAttack(attackerTerritoryId, defenderTerritoryId);
                if (gameData.getUser(myId).getGoal().isDone()) {
                    gameData.setGameState(GameData.State.END);
                }
                mCallback.broadcast(gameData.getByteArray());
                break;
        }

    }

    private void doAttack(String attackerTerritoryId, String defenderTerritoryId) {
        Territory attackerTerritory = gameData.getTerritory(attackerTerritoryId);
        Territory defenderTerritory = gameData.getTerritory(defenderTerritoryId);

        //generisemo kockice
        int attackerCubesCount = Math.min(attackerTerritory.getArmies(), 3);
        int defenderCubesCount = Math.min(defenderTerritory.getArmies(), 3);
        ArrayList<Integer> attackerCubes = new ArrayList<>();
        ArrayList<Integer> defenderCubes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < attackerCubesCount; i++) {
            attackerCubes.add(random.nextInt(6)+1);
        }
        for (int j = 0; j < defenderCubesCount; j++) {
            defenderCubes.add(random.nextInt(6)+1);
        }

        // sortiramo i vrsimo napad
        Collections.sort(attackerCubes, Collections.<Integer>reverseOrder());
        Collections.sort(defenderCubes, Collections.<Integer>reverseOrder());
        int watchingCubesCount = Math.min(attackerCubesCount, defenderCubesCount);
        for (int k = 0; k < watchingCubesCount; k++) {
            if (attackerCubes.get(k) > defenderCubes.get(k)) {
                defenderTerritory.setArmies(defenderTerritory.getArmies() - 1);
            } else {
                attackerTerritory.setArmies(attackerTerritory.getArmies() -1);
            }
            // ako nema vise sa cime da napada
            if (attackerTerritory.getArmies() == 1) {
                break;
            }

            if (defenderTerritory.getArmies() == 0) {
                defenderTerritory.setUserId(myId);
                defenderTerritory.setArmies(1);
                // TODO Prikazati diskretni slider za prebacivanje armija
//                SeekBar seekBar = new SeekBar(activity);
            }
        }

    }

    private boolean areNeighboringTerritories(String territoryId) {
        // TODO proveriti da li moze sa attackerTerritoryId da se napada territoryId
        return true;
    }

    private void addOneArmyToTerritory(String territoryId) {
        Territory t = gameData.getTerritory(territoryId);
        t.setArmies(t.getArmies() + 1);
        armiesToPlace--;
        Log.d(TAG, t.getName() + ": " + t.getArmies() + " armies");
        Log.d(TAG, "Armies to place: " + armiesToPlace);
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
            armiesToPlace = getInitArmies();

            // Prikazivanje obavestenja korisniku da postavi tenkice
            String dialogText = "Your goal is: " + gameData.getUser(myId).getGoal().getDescription();
            dialogText += "\nPlace " + armiesToPlace + " armies on your territories";
            BaseGameUtils.makeSimpleDialog(activity, dialogText).show();
            return;
        }

        final User user = gameData.getUsers().get(0);

        switch (gameData.getGameState()) {
            case INIT_PLACING_ARMIES:
                gameDataNetworkCopy = gd;
                break;
            case GAME_TURN_BEGINNING:
                mapScreen.lockMap();
                // prikazati ciji je potez ako nije moj
                if (!gameData.getUsers().get(0).getUserId().equals(myId)) {

                    String toastText =  mRoom.getParticipant(user.getUserId()).getDisplayName();
                    toastText += "'s turn!";
                    Toast.makeText(activity, toastText, Toast.LENGTH_LONG).show();
                    return;
                }
                BaseGameUtils.makeSimpleDialog(activity, "Your turn").show();
                armiesToPlace = 0;

                // pitamo korisnika da li zeli da zameni zvezdice
                if (user.getStars() > 0) {
                    new AlertDialog.Builder(activity)
                            .setMessage("Do you want to exchange stars for armies?")
                            .setNegativeButton("Cancel",null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // TODO: user.getStars() zameniti sa brojem armija
                                    armiesToPlace+= user.getStars();
                                    user.setStars(0);
                                }
                            }).show();
                }

                // dodajemo broj tenkica koji treba da se postave
                armiesToPlace += calculateTurnBeginningArmies();

                gameData.setGameState(GameData.State.GAME_PLACING_ARMIES);
                mCallback.broadcast(gameData.getByteArray());
                break;
            case GAME_PLACING_ARMIES:
                gameData = gd;
                mapScreen.lockMap();

                // nije moj potez
                if (!gameData.getUsers().get(0).getUserId().equals(myId)) {
                    return;
                }

                // nemam vise armija za postavljanje
                if (armiesToPlace == 0) {
                    gameData.setGameState(GameData.State.GAME_ATTACK);
                    mCallback.broadcast(gameData.getByteArray());
                    return;
                }

                // inace ostajemo u istom stanju i otkljucavamo mapu
                mapScreen.unlockMap();

                break;
            case GAME_ATTACK:
                gameData = gd;
                mapScreen.lockMap();

                // nije moj potez
                if (!gameData.getUsers().get(0).getUserId().equals(myId)) {
                    return;
                }

                mapScreen.unlockMap();

                break;
        }
    }

    private int calculateTurnBeginningArmies() {
        int territories = 0;
        for (Territory t : gameData.getTerritories()) {
            if (t.getUserId().equals(myId)) {
                territories++;
            }
        }
        //TODO: proveriti kontinente
        return Math.max(territories/3, 3);
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
        int myPlacedArmies = 0;
        for (Territory t : gameData.getTerritories()) {
            if (t.getId().equals(myId)) {
                myPlacedArmies += t.getArmies();
            }
        }

        //vracamo umanjeni broj za onoliko koliko je postavljeno na svim njegovim teritorijama
        return (usersSize == 6 ? 20 :
                usersSize == 5 ? 25 :
                usersSize == 4 ? 30 :
                usersSize == 3 ? 35 : 40) - myPlacedArmies;
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
}
