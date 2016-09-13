package com.rs2.risiko.data;

import java.util.List;

/**
 * Created by enco on 12.9.16..
 */
public class User {
    String userId;
    String color;
    Goal goal;
    List<Card> cards;

    public User(String userId, String color, Goal goal, List<Card> cards) {
        this.userId = userId;
        this.color = color;
        this.goal = goal;
        this.cards = cards;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", color='" + color + '\'' +
                ", goal=" + goal +
                ", cards=" + cards +
                '}';
    }
}
