package com.rs2.risiko.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GameData {
    public enum State {
        @SerializedName("0")
        FIRST_PLAYER,
        @SerializedName("1")
        INIT_PLACING_ARMIES
    }

    List<Territory> territories;
    List<User> users;
    List<Card> cards;
    String currentUserId;
    State gameState;
    List<Object> parameters;

    public GameData(List<Territory> territories, List<User> users, List<Card> cards, String currentUserId, State gameState, List<Object> parameters) {
        this.territories = territories;
        this.users = users;
        this.cards = cards;
        this.currentUserId = currentUserId;
        this.gameState = gameState;
        this.parameters = parameters;
    }

    public State getGameState() {
        return gameState;
    }

    public void setGameState(State gameState) {
        this.gameState = gameState;
    }

    @Override
    public String toString() {
        return "GameData{" +
                "territories=" + territories +
                ", users=" + users +
                ", cards=" + cards +
                ", currentUserId='" + currentUserId + '\'' +
                ", gameState=" + gameState +
                ", parameters=" + parameters +
                '}';
    }
}
