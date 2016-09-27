package com.rs2.risiko.data;

import android.os.Parcel;
import android.os.Parcelable;


import com.rs2.risiko.util.ParcelableUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameData implements Parcelable{

    public enum State {
        INIT_PLACING_ARMIES,
        GAME_TURN_BEGINNING,
        GAME_PLACING_ARMIES,
        GAME_ATTACK,
        GAME_END_TURN,
        END
    }

    List<Territory> territories;
    List<User> users;
    State gameState;
    int armiesToPlace;
    Map<String, Boolean> isFinishInitPlacingArmies;

    public GameData(List<Territory> territories, List<User> users, State gameState) {
        this.territories = territories;
        this.users = users;
        this.gameState = gameState;
        this.armiesToPlace = 0;
        this.isFinishInitPlacingArmies = new HashMap<>();
        for (User u : users) {
            isFinishInitPlacingArmies.put(u.getUserId(), false);
        }
    }

    public boolean isMyTerritory(String myId, String territoryId) {
        Territory t = getTerritory(territoryId);
        return t.getUserId().equals(myId);
    }

    public List<Territory> getTerritories() {
        return territories;
    }

    public void setTerritories(List<Territory> territories) {
        this.territories = territories;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public State getGameState() {
        return gameState;
    }

    public void setGameState(State gameState) {
        this.gameState = gameState;
    }

    public int getArmiesToPlace() {
        return armiesToPlace;
    }

    public void setArmiesToPlace(int armiesToPlace) {
        this.armiesToPlace = armiesToPlace;
    }

    public Territory getTerritory(String territoryId) {
        for (Territory t : territories) {
            if (t.getId().equals(territoryId)) {
                return t;
            }
        }
        return null;
    }

    public void setIsFinishInitPlacingArmiesFor(String myId) {
        isFinishInitPlacingArmies.put(myId, true);
    }

    public Map<String, Boolean> getIsFinishInitPlacingArmies() {
        return isFinishInitPlacingArmies;
    }

    public User getUser(String myId) {
        for (User u : users) {
            if (u.getUserId().equals(myId)) {
                return u;
            }
        }
        return null;
    }

    public void nextUser() {
        users.add(users.remove(0));
    }

    public Territory getTerritoryById(String territoryId) {
        for (Territory t : this.territories) {
            if (t.getId().equals(territoryId)) {
                return t;
            }
        }
        return null;
    }

    // Parcelling part
    public GameData(Parcel in){

        territories = in.createTypedArrayList(Territory.CREATOR);
        users = in.createTypedArrayList(User.CREATOR);
        gameState = State.valueOf(in.readString());
        armiesToPlace = in.readInt();

        // citanje isFinishMape
        isFinishInitPlacingArmies = new HashMap<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String k = in.readString();
            int v = in.readInt();
            if (v == 1) {
                isFinishInitPlacingArmies.put(k, true);
            } else {
                isFinishInitPlacingArmies.put(k, false);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(territories);
        parcel.writeTypedList(users);
        parcel.writeString(gameState.name());
        parcel.writeInt(armiesToPlace);
        parcel.writeInt(isFinishInitPlacingArmies.size());
        for (Map.Entry<String, Boolean> entry: isFinishInitPlacingArmies.entrySet()) {
            parcel.writeString(entry.getKey());
            if (entry.getValue()) {
                parcel.writeInt(1);
            } else {
                parcel.writeInt(0);
            }
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public GameData createFromParcel(Parcel in) {
            return new GameData(in);
        }

        public GameData[] newArray(int size) {
            return new GameData[size];
        }
    };

    public byte[] getByteArray () {
        return ParcelableUtil.marshall(this);
    }

    @Override
    public String toString() {
        return "GameData{" +
                "gameState=" + gameState +
                ", territories=" + territories +
                ", users=" + users +
                '}';
    }
}
