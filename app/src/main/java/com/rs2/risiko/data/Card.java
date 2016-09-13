package com.rs2.risiko.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by enco on 27.8.16..
 */
public class Card {
    public enum Type {

        @SerializedName("1")
        INFANTRY,

        @SerializedName("2")
        CAVALRY,

        @SerializedName("3")
        ARTILLERY
    };

    private int id;
    private String territoryId;
    private Type type;

    public Card(int id, String territoryId, Type type) {
        this.id = id;
        this.territoryId = territoryId;
        this.type = type;
    }

    public static ArrayList<Card> getAllCards() {
        ArrayList<Card> cards = new ArrayList<>();

        cards.add(new Card(0, "RS-00", Type.INFANTRY));
        cards.add(new Card(1, "RS-01", Type.CAVALRY));
        cards.add(new Card(2, "RS-02", Type.ARTILLERY));
        cards.add(new Card(3, "RS-03", Type.INFANTRY));
        cards.add(new Card(4, "RS-04", Type.CAVALRY));
        cards.add(new Card(5, "RS-05", Type.ARTILLERY));
        cards.add(new Card(6, "RS-06", Type.INFANTRY));
        cards.add(new Card(7, "RS-07", Type.CAVALRY));
        cards.add(new Card(8, "RS-08", Type.ARTILLERY));
        cards.add(new Card(9, "RS-09", Type.INFANTRY));
        cards.add(new Card(10, "RS-10", Type.CAVALRY));

        return cards;
    }

}
