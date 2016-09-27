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

    public void applyData(GameData gd) {
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
                                    // TODO: user.getStars() zameniti sa brojem armija, napravljena f-ja calculateArmiesForStars
                                    gameData.setArmiesToPlace(
                                            gameData.getArmiesToPlace() +
                                                    gameData.getUsers().get(0).getStars());
                                    gameData.getUsers().get(0).setStars(0);
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
    }

    @Override
    public void onTerritoryClick(String territoryId) {

        // Zakljucavamo za svaki slucaj
        mapScreen.lockMap();

        Territory territory = gameData.getTerritory(territoryId);
        switch (gameData.getGameState()) {
            case INIT_PLACING_ARMIES:
                if (!gameData.isMyTerritory(myId, territoryId)) {
                    mapScreen.setStatusText("Not your territory");
                    Log.d(TAG, "Not my territory!");
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
                    Log.d(TAG, "Not my territory!");
                    return;
                }
                addOneArmyToTerritory(territoryId);
                mapScreen.lockMap();
                mCallback.broadcast(gameData.getByteArray());
                break;
            case GAME_ATTACK:
                mapScreen.unlockMap();
                Log.d(TAG, "MyId: " + myId);
                Log.d(TAG, territory.toString());

                // ako je moja teritorija
                if (territory.getUserId().equals(myId)) {
                    // i imam vise od jedne armije cuvam je kao napadacku
                    if (territory.getArmies() > 1){
                        Log.d(TAG, "Selected territory: " + territory.getName());
                        attackerTerritoryId = territoryId;

                        return;
                    }
                    // znaci da nema dovoljno tenkica za napadanje
                    Log.d(TAG, "Territory " + territory.getName() + " has 1 army");
                    return;
                }

                // u suprotnom je protivnikova

                // ako nije izabrana napadacka teritorija izbacujem gresku
                if (attackerTerritoryId == null) {
                    Log.d(TAG, "Mora prvo da se selektuje teritorija s koje se napada");
                    return;
                }

                // Ako nisu susedne teritorije izbaciti gresku
                if (!areNeighboringTerritories(attackerTerritoryId, territoryId)) {
                    Log.d(TAG, "Nisu susedne teritorije");
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
                    Log.d(TAG, "Izaberi svoju teritoriju");
                    return;
                }

                // postavi teritoriju s koje se prebacuje ako nije vec postavljena
                if (endTurnTerritoryFrom == null) {
                    if (territory.getArmies() < 2) {
                        Log.d(TAG, "Nema dovoljno armija za prebacivanje");
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

                territory.setArmies(territory.getArmies() + 1);
                Territory t = gameData.getTerritory(endTurnTerritoryFrom);
                createDialogWithSeekBar(t.getArmies()-1, t, territory, true);

                break;
        }

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
        Log.d(TAG, "Attacker: " + attackerCubes.toString() + "; armies: " + attackerTerritory.getArmies());
        Log.d(TAG, "Defender: " + defenderCubes.toString() + "; armies: " + defenderTerritory.getArmies());
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
        // TODO proveriti da li moze sa attackerTerritoryId da se napada territoryId
        return true;
    }

    private void addOneArmyToTerritory(String territoryId) {
        Territory t = gameData.getTerritory(territoryId);
        t.setArmies(t.getArmies() + 1);
        saveGameDataAndUpdateMap(gameData);
        Log.d(TAG, t.getName() + ": " + t.getArmies() + " armies");
        switch (gameData.getGameState()) {
            case INIT_PLACING_ARMIES:
                armiesToPlaceOnBeginning--;

                Log.d(TAG, "Armies to place: " + armiesToPlaceOnBeginning);
                break;
            case GAME_TURN_BEGINNING:
            case GAME_PLACING_ARMIES:
                gameData.setArmiesToPlace(gameData.getArmiesToPlace() - 1);
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
        //TODO: proveriti kontinente
        return Math.max(territories/3, 3);
    }

    private int calculateArmiesForStars(int stars){

        switch (stars){
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
            case 10:
                return 30;
            default:
                return 0;
        }
    }

    public int checkContinents(String uid){

        int numberOfArmies = 0;
        List<Territory> teritorije = gameData.getTerritories();
        if(teritorije.get(0).getUserId().equals(uid) &&
                teritorije.get(1).getUserId().equals(uid) &&
                teritorije.get(2).getUserId().equals(uid) &&
                teritorije.get(3).getUserId().equals(uid) &&
                teritorije.get(4).getUserId().equals(uid) &&
                teritorije.get(5).getUserId().equals(uid) &&
                teritorije.get(6).getUserId().equals(uid) &&
                teritorije.get(7).getUserId().equals(uid) &&
                teritorije.get(12).getUserId().equals(uid)){
            numberOfArmies+=5;
        }
        if(teritorije.get(8).getUserId().equals(uid) &&
                teritorije.get(9).getUserId().equals(uid) &&
                teritorije.get(10).getUserId().equals(uid) &&
                teritorije.get(11).getUserId().equals(uid)){
            numberOfArmies+=2;
        }
        if(teritorije.get(13).getUserId().equals(uid) &&
                teritorije.get(14).getUserId().equals(uid) &&
                teritorije.get(15).getUserId().equals(uid) &&
                teritorije.get(16).getUserId().equals(uid) &&
                teritorije.get(17).getUserId().equals(uid) &&
                teritorije.get(18).getUserId().equals(uid)){
            numberOfArmies+=5;
        }
        if(teritorije.get(31).getUserId().equals(uid) &&
                teritorije.get(32).getUserId().equals(uid) &&
                teritorije.get(33).getUserId().equals(uid) &&
                teritorije.get(34).getUserId().equals(uid) &&
                teritorije.get(35).getUserId().equals(uid) &&
                teritorije.get(36).getUserId().equals(uid)){
            numberOfArmies+=3;
        }
        if(teritorije.get(37).getUserId().equals(uid) &&
                teritorije.get(38).getUserId().equals(uid) &&
                teritorije.get(39).getUserId().equals(uid) &&
                teritorije.get(40).getUserId().equals(uid)){
            numberOfArmies+=2;
        }
        if(teritorije.get(19).getUserId().equals(uid) &&
                teritorije.get(20).getUserId().equals(uid) &&
                teritorije.get(21).getUserId().equals(uid) &&
                teritorije.get(22).getUserId().equals(uid) &&
                teritorije.get(23).getUserId().equals(uid) &&
                teritorije.get(24).getUserId().equals(uid)&&
                teritorije.get(25).getUserId().equals(uid)&&
                teritorije.get(26).getUserId().equals(uid)&&
                teritorije.get(27).getUserId().equals(uid)&&
                teritorije.get(28).getUserId().equals(uid)&&
                teritorije.get(29).getUserId().equals(uid)&&
                teritorije.get(30).getUserId().equals(uid)){
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
        dialog.setTitle("Prebacivanje armija");
        dialog.show();

        brojArmijaZaPrebacivanje = 1;
        final Button buttonSeekDialog = (Button) dialog.findViewById(R.id.button_seek_dialog);
        SeekBar seekbar = (SeekBar) dialog.findViewById(R.id.seekBar);
        final TextView textViewSeekBar = (TextView) dialog.findViewById(R.id.text_desc);
        seekbar.setMax(max-1);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSeekBar.setText("Prebaci " + (progress + 1) + " armija");
                brojArmijaZaPrebacivanje = progress + 1;
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
        mapScreen.updateMap(gameData);
    }

    private GameCallbacks mCallback;
    public interface GameCallbacks {
        void broadcast(byte[] data);

        void gameStarted();
    }
}
