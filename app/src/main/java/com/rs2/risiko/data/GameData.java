package com.rs2.risiko.data;

import android.os.Parcel;
import android.os.Parcelable;


import java.util.List;

public class GameData implements Parcelable{

    public enum State {
//        @SerializedName("0")
        FIRST_PLAYER,
//        @SerializedName("1")
        INIT_PLACING_ARMIES
    }

    List<Territory> territories;
    List<User> users;
    List<Card> cards;
    String currentUserId;
    State gameState;

    public GameData(List<Territory> territories, List<User> users, List<Card> cards, String currentUserId, State gameState) {
        this.territories = territories;
        this.users = users;
        this.cards = cards;
        this.currentUserId = currentUserId;
        this.gameState = gameState;
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

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public State getGameState() {
        return gameState;
    }

    public void setGameState(State gameState) {
        this.gameState = gameState;
    }

    // Parcelling part
    public GameData(Parcel in){

        territories = in.createTypedArrayList(Territory.CREATOR);
        users = in.createTypedArrayList(User.CREATOR);
        cards = in.createTypedArrayList(Card.CREATOR);
        currentUserId = in.readString();
        gameState = State.valueOf(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(territories);
        parcel.writeTypedList(users);
        parcel.writeTypedList(cards);
        parcel.writeString(currentUserId);
        parcel.writeString(gameState.name());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public GameData createFromParcel(Parcel in) {
            return new GameData(in);
        }

        public GameData[] newArray(int size) {
            return new GameData[size];
        }
    };

    @Override
    public String toString() {
        return "GameData{" +
                "gameState=" + gameState +
                ", territories=" + territories +
                ", users=" + users +
                ", cards=" + cards +
                ", currentUserId='" + currentUserId + '\'' +
                '}';
    }
}
