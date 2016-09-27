package com.rs2.risiko.game_logic;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.rs2.risiko.MainActivity;
import com.rs2.risiko.R;
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
    private int armiesToPlaceOnBeginning;
    private String attackerTerritoryId;
    private String defenderTerritoryId;
    private Button buttonEndTurn;
    private String endTurnTerritoryFrom;
    private boolean shouldGetStars;
    private int brojArmijaZaPrebacivanje;

    public Game(Room room, String myId, MainActivity activityWithCallback, List<String> colors) {
        mRoom = room;
        this.myId = myId;
        mCallback = activityWithCallback;
        activity = activityWithCallback;
        this.armiesToPlaceOnBeginning = 0;
        chooseFirstPlayer(colors);
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
            users.add(new User(ids.get(i), colors.get(i), goals.get(i), mRoom.getParticipant(ids.get(i)).getDisplayName().toString()));
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

    public void applyData(final GameData gd) {
        Log.d(TAG, gd.toString());

        // update podataka
        // ako je gameData null onda je to pocetno stanje koje preuzimamo od "dilera"
        if (gameData == null) {
            applyDataFromDealer(gd);
            return;
        }
        buttonEndTurn.post(new Runnable() {
            @Override
            public void run() {
                buttonEndTurn.setVisibility(View.GONE);
            }
        });

        final User currentUser = gd.getUsers().get(0);

        switch (gd.getGameState()) {
            case INIT_PLACING_ARMIES:
                gameDataNetworkCopy = gd;
                break;

            case GAME_TURN_BEGINNING:
                saveGameDataAndUpdateMap(gd);
                mapScreen.lockMap();
                // prikazati ciji je potez
                if (!isMyTurn()) {

                    String toastText =  mRoom.getParticipant(currentUser.getUserId()).getDisplayName();
                    toastText += "'s turn!";
                    Toast.makeText(activity, toastText, Toast.LENGTH_LONG).show();
                    return;
                }
                mapScreen.unlockMap();

                // dodajemo broj tenkica koji treba da se postave
                // TODO ako je prvi potez ne treba dodavati armije na osnovu teritorija i kontinenata
                gameData.setArmiesToPlace(calculateTurnBeginningArmies());
                gameData.setGameState(GameData.State.GAME_PLACING_ARMIES);
                mCallback.broadcast(gameData.getByteArray());

                // pitamo korisnika da li zeli da zameni zvezdice
                if (currentUser.getStars() > 0) {
                    new AlertDialog.Builder(activity)
                            .setMessage("Do you want to exchange stars for armies?")
                            .setNegativeButton("Cancel",null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    gameData.setArmiesToPlace(
                                            gameData.getArmiesToPlace() +
                                                   calculateArmiesForStars(gameData.getUsers().get(0).getStars()));
                                    gameData.getUsers().get(0).setStars(0);
                                    saveGameDataAndUpdateMap(gameData);
                                    mCallback.broadcast(gameData.getByteArray());
                                }
                            }).show();
                }

                BaseGameUtils.makeSimpleDialog(activity, "Your turn").show();
                break;

            case GAME_PLACING_ARMIES:
                saveGameDataAndUpdateMap(gd);
                mapScreen.lockMap();

                // nije moj potez
                if (!isMyTurn()) {
                    return;
                }

                // nemam vise armija za postavljanje
                if (gameData.getArmiesToPlace() == 0) {
                    gameData.setGameState(GameData.State.GAME_ATTACK);
                    mCallback.broadcast(gameData.getByteArray());
                    return;
                }

                // inace ostajemo u istom stanju i otkljucavamo mapu
                mapScreen.unlockMap();

                break;

            case GAME_ATTACK:
                Log.d(TAG, "applyData(GAME_ATTACK)");
                saveGameDataAndUpdateMap(gd);
                mapScreen.lockMap();
                // nije moj potez
                if (!isMyTurn()) {
                    return;
                }
                buttonEndTurn.post(new Runnable() {
                    @Override
                    public void run() {
                        buttonEndTurn.setVisibility(View.VISIBLE);
                    }
                });
                mapScreen.unlockMap();
                break;

            case GAME_END_TURN:
                saveGameDataAndUpdateMap(gd);
                if (!isMyTurn()) {
                    mapScreen.lockMap();
                    return;
                }
                Log.d(TAG, "Premestanje armija za kraj poteza");
                mapScreen.unlockMap();
                break;
        }
    }

    private void applyDataFromDealer(GameData gd) {
        gameData = gd;
        gameDataNetworkCopy = gd;
        mapScreen = new MapScreen(activity, this);
        mCallback.gameStarted();
        Log.d(TAG, "INIT_PLACING_ARMIES");
        armiesToPlaceOnBeginning = getInitArmies();
        mapScreen.setArmies(armiesToPlaceOnBeginning);
        // Prikazivanje obavestenja korisniku da postavi tenkice
        User myUser = gameData.getUser(myId);
        String dialogText = "Your goal is: " + myUser.getGoal().getDescription();
        dialogText += "\nPlace " + armiesToPlaceOnBeginning + " armies on your territories";
        dialogText += "\nYour color is color of status text";
        mapScreen.setStatusColor(myUser.getColor());
        BaseGameUtils.makeSimpleDialog(activity, dialogText).show();

        // setovanje dugmeta EndTurn
        buttonEndTurn = (Button) activity.findViewById(R.id.button_end_turn);
        buttonEndTurn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "End clicked");
                switch (gameData.getGameState()) {
                    case GAME_ATTACK:
                        if (!isMyTurn()) {
                            return;
                        }
                        gameData.setGameState(GameData.State.GAME_END_TURN);
                        mapScreen.lockMap();
                        mCallback.broadcast(gameData.getByteArray());
                        buttonEndTurn.post(new Runnable() {
                            @Override
                            public void run() {
                                buttonEndTurn.setVisibility(View.GONE);
                            }
                        });
                        break;
                    case GAME_END_TURN:
                        if (!isMyTurn()) {
                            return;
                        }
                        endTurn();
                        break;
                }
            }
        });
    }

    private void saveGameDataAndUpdateMap(GameData gd) {
        gameData = gd;
        mapScreen.updateMap(gd);
        mapScreen.updateStars(myId, gd);
        if (gd.getUsers().get(0).getUserId().equals(myId)) {
            mapScreen.setArmies(gd.getArmiesToPlace());
        } else {
            mapScreen.setArmies(0);
        }
    }

    @Override
    public void onTerritoryClick(String territoryId) {

        // Zakljucavamo za svaki slucaj
        mapScreen.lockMap();

        Territory territory = gameData.getTerritory(territoryId);
        switch (gameData.getGameState()) {
            case INIT_PLACING_ARMIES:
                if (!gameData.isMyTerritory(myId, territoryId)) {
                    makeToast("Not your territory!");
                    mapScreen.unlockMap();
                    return;
                }
                // postavljamo po jednog tenkica
                addOneArmyToTerritory(territoryId);

                // ako smo sve postavili saljemo nas objekat na sinhronizaciju
                if (armiesToPlaceOnBeginning < 1) {
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
                    return;
                }
                mapScreen.unlockMap();
                break;
            case GAME_PLACING_ARMIES:
                mapScreen.unlockMap();
                if (!gameData.isMyTerritory(myId, territoryId)) {
                    makeToast("Not my territory!");
                    return;
                }
                addOneArmyToTerritory(territoryId);
                mapScreen.lockMap();
                mCallback.broadcast(gameData.getByteArray());
                break;
            case GAME_ATTACK:
                mapScreen.unlockMap();

                // ako je moja teritorija
                if (territory.getUserId().equals(myId)) {
                    // i imam vise od jedne armije cuvam je kao napadacku
                    if (territory.getArmies() > 1){
                        makeToast("Selected territory: " + territory.getName());
                        attackerTerritoryId = territoryId;

                        return;
                    }
                    // znaci da nema dovoljno tenkica za napadanje
                    makeToast("Territory " + territory.getName() + " has 1 army");
                    return;
                }

                // u suprotnom je protivnikova

                // ako nije izabrana napadacka teritorija izbacujem gresku
                if (attackerTerritoryId == null) {
                    makeToast("Select territory to attack");
                    return;
                }

                // Ako nisu susedne teritorije izbaciti gresku
                if (!areNeighboringTerritories(attackerTerritoryId, territoryId)) {
                    makeToast("Not neighbouring territories");
                    return;
                }
                mapScreen.lockMap();
                // moze da se napada
                defenderTerritoryId = territoryId;
                Log.d(TAG, "defenderTerritoryId: " + defenderTerritoryId);

                doAttack(attackerTerritoryId, defenderTerritoryId);
                // HAKUJEM OVDE ZESTOKO
//                if (gameData.getUser(myId).getGoal().isDone()) {
//                    // TODO PROVERA DA LI JE ZAVRSENO
//                    gameData.setGameState(GameData.State.END);
//                }
//                mCallback.broadcast(gameData.getByteArray());
                break;
            case GAME_END_TURN:

                mapScreen.unlockMap();
                if (!gameData.isMyTerritory(myId, territoryId)) {
                    makeToast("Choose your territory");
                    return;
                }

                // postavi teritoriju s koje se prebacuje ako nije vec postavljena
                if (endTurnTerritoryFrom == null) {
                    if (territory.getArmies() < 2) {
                        makeToast("Territory has less than 2 armies");
                        return;
                    }
                    endTurnTerritoryFrom = territoryId;
                    mCallback.broadcast(gameData.getByteArray());
                    return;
                }

                // ako je opet kliknuo na istu teritoriju znaci da zeli da je odselektuje
                if (endTurnTerritoryFrom.equals(territoryId)) {
                    endTurnTerritoryFrom = null;
                    return;
                }

                mapScreen.lockMap();

                // znaci da treba da prebacimo na tu teritoriju
                // TODO: Show SeekBar
                // za sada mokovano tako da se uvek prebaci samo jedna armija

                Territory fromTerritory = gameData.getTerritory(endTurnTerritoryFrom);
                createDialogWithSeekBar(fromTerritory.getArmies()-1, fromTerritory, territory, true);

                break;
        }

    }

    private void makeToast(String s) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
    }

    private void endTurn() {
        // dodeljujemo zvezdice
        if (shouldGetStars) {
            Random r = new Random();
            User myUser = gameData.getUsers().get(0);
            myUser.setStars(myUser.getStars() + r.nextInt(2)+1);
        }
        shouldGetStars = false;

        gameData.nextUser();
        gameData.setGameState(GameData.State.GAME_TURN_BEGINNING);
        mCallback.broadcast(gameData.getByteArray());
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
        makeToast("Attacker: " + attackerCubes.toString() + "\nDefender: " + defenderCubes.toString());
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
                // osvojena teritorija
                // postavljamo flag za zvezdice
                shouldGetStars = true;

                defenderTerritory.setUserId(myId);
                defenderTerritory.setArmies(1);
                attackerTerritory.setArmies(attackerTerritory.getArmies() - 1);
                if (attackerTerritory.getArmies() > 1) {
                    createDialogWithSeekBar(attackerTerritory.getArmies() - 1, attackerTerritory, defenderTerritory, false);
                    return;
                }
                break;
            }
        }
        this.attackerTerritoryId = null;
        this.defenderTerritoryId = null;
        if (gameData.getUser(myId).getGoal().isDone()) {
//                    // TODO PROVERA DA LI JE ZAVRSENO
            gameData.setGameState(GameData.State.END);
        }
        mCallback.broadcast(gameData.getByteArray());
    }

    private boolean areNeighboringTerritories(String territoryId1, String territoryId2) {
        return Territory.areNeighboringTeritories(territoryId1, territoryId2);
    }

    private void addOneArmyToTerritory(String territoryId) {
        Territory t = gameData.getTerritory(territoryId);
        t.setArmies(t.getArmies() + 1);
        saveGameDataAndUpdateMap(gameData);
        Log.d(TAG, t.getName() + ": " + t.getArmies() + " armies");
        switch (gameData.getGameState()) {
            case INIT_PLACING_ARMIES:
                armiesToPlaceOnBeginning--;
                mapScreen.setArmies(armiesToPlaceOnBeginning);
                Log.d(TAG, "Armies to place: " + armiesToPlaceOnBeginning);
                break;
            case GAME_TURN_BEGINNING:
            case GAME_PLACING_ARMIES:
                gameData.setArmiesToPlace(gameData.getArmiesToPlace() - 1);
                mapScreen.setArmies(gameData.getArmiesToPlace());
                Log.d(TAG, "Armies to place: " + gameData.getArmiesToPlace());
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
        return Math.max(territories/3, 3) + checkContinents(myId);
    }

    private int calculateArmiesForStars(int stars){

        switch (stars){
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 4;
            case 4:
                return 7;
            case 5:
                return 10;
            case 6:
                return 13;
            case 7:
                return 17;
            case 8:
                return 21;
            case 9:
                return 25;
            default:
                return 30;
        }
    }

    public int checkContinents(String uid){

        int numberOfArmies = 0;

        if(gameData.getTerritoryById("RS-00").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-01").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-02").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-03").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-04").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-05").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-06").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-07").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-12").getUserId().equals(uid)){
            numberOfArmies+=5;
        }
        if(gameData.getTerritoryById("RS-08").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-09").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-10").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-11").getUserId().equals(uid)){
            numberOfArmies+=2;
        }
        if(gameData.getTerritoryById("RS-13").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-14").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-15").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-16").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-17").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-18").getUserId().equals(uid)){
            numberOfArmies+=5;
        }
        if(gameData.getTerritoryById("RS-31").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-32").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-33").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-34").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-35").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-36").getUserId().equals(uid)){
            numberOfArmies+=3;
        }
        if(gameData.getTerritoryById("RS-37").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-38").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-39").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-40").getUserId().equals(uid)){
            numberOfArmies+=2;
        }
        if(gameData.getTerritoryById("RS-19").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-20").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-21").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-22").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-23").getUserId().equals(uid) &&
                gameData.getTerritoryById("RS-24").getUserId().equals(uid)&&
                gameData.getTerritoryById("RS-25").getUserId().equals(uid)&&
                gameData.getTerritoryById("RS-26").getUserId().equals(uid)&&
                gameData.getTerritoryById("RS-27").getUserId().equals(uid)&&
                gameData.getTerritoryById("RS-28").getUserId().equals(uid)&&
                gameData.getTerritoryById("RS-29").getUserId().equals(uid)&&
                gameData.getTerritoryById("RS-30").getUserId().equals(uid)){
            numberOfArmies+=7;
        }

        return numberOfArmies;
    }

    private void mergeGameData() {
        for (Territory t : gameData.getTerritories()) {
            if (t.getUserId().equals(myId)) {
                gameDataNetworkCopy.getTerritory(t.getId()).setArmies(t.getArmies());
            }
        }
    }

    private int getInitArmies() {
        Log.d(TAG, "MyId: " + myId);
        Log.d(TAG, "Users: " + gameData.getUsers().toString());
        int usersSize = gameData.getUsers().size();
        int myPlacedArmies = 0;
        for (Territory t : gameData.getTerritories()) {
            if (t.getUserId().equals(myId)) {
                myPlacedArmies += t.getArmies();
            }
        }

        //vracamo umanjeni broj za onoliko koliko je postavljeno na svim njegovim teritorijama
        int initArmies =  (usersSize == 6 ? 20 :
                usersSize == 5 ? 25 :
                        usersSize == 4 ? 30 :
                                usersSize == 3 ? 35 : 40) ;
        // TODO promeniti ovih 15 na 40 (za dva igraca)

        Log.d(TAG, "initArmies: " + initArmies);
        initArmies -= myPlacedArmies;
        Log.d(TAG, "initArmies-: " + initArmies);
        return initArmies;
    }

    private boolean isMyTurn() {
        return gameData.getUsers().get(0).getUserId().equals(myId);
    }

    private void createDialogWithSeekBar(int max, final Territory fromTerritory, final Territory toTerritory, final boolean shouldEndTurn) {

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.seekbar_dialog);
        dialog.setTitle("Move armies");
        dialog.show();

        brojArmijaZaPrebacivanje = 0;
        final Button buttonSeekDialog = (Button) dialog.findViewById(R.id.button_seek_dialog);
        SeekBar seekbar = (SeekBar) dialog.findViewById(R.id.seekBar);
        final TextView textViewSeekBar = (TextView) dialog.findViewById(R.id.text_desc);
        seekbar.setMax(max);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSeekBar.setText("Move " + progress + " armies");
                brojArmijaZaPrebacivanje = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        buttonSeekDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toTerritory.setArmies(toTerritory.getArmies() + brojArmijaZaPrebacivanje);
                fromTerritory.setArmies(fromTerritory.getArmies() - brojArmijaZaPrebacivanje);
                Log.d(TAG, "brojZaPrebacivanje: "+brojArmijaZaPrebacivanje);
                Log.d(TAG, "toTerritory: "+toTerritory.toString());
                Log.d(TAG, "fromTerritory: "+fromTerritory.toString());
                dialog.dismiss();
                if (shouldEndTurn) {
                    endTurn();
                } else {
                    if (gameData.getUser(myId).getGoal().isDone()) {
                        // TODO PROVERA DA LI JE ZAVRSENO
                        gameData.setGameState(GameData.State.END);
                    }

                    attackerTerritoryId = null;
                    defenderTerritoryId = null;
                    mCallback.broadcast(gameData.getByteArray());
                }
            }
        });
    }

    @Override
    public void onWebviewLoaded() {
        mapScreen.setArmies(armiesToPlaceOnBeginning);
        mapScreen.updateMap(gameData);

    }

    private GameCallbacks mCallback;
    public interface GameCallbacks {
        void broadcast(byte[] data);

        void gameStarted();
    }
}
