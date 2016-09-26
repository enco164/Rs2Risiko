package com.rs2.risiko.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by enco on 27.8.16..
 */
public class Goal implements Parcelable {
    private int id;

    public Goal(int id) {
        this.id = id;
    }

    public boolean isDone() {
        return false; //TODO: Implementirati proveru zadatka
    }

    public String getDescription() {
        return DESCRIPTIONS[id];
    }

    public int getId() {
        return id;
    }

    public static ArrayList<Goal> getAllGoals() {
        ArrayList<Goal> goals = new ArrayList<>();

        goals.add(new Goal(0));
        goals.add(new Goal(1));
        goals.add(new Goal(2));
        goals.add(new Goal(3));
        goals.add(new Goal(4));
        goals.add(new Goal(5));
        goals.add(new Goal(6));
        goals.add(new Goal(7));
        goals.add(new Goal(8));
        goals.add(new Goal(9));
        goals.add(new Goal(10));
        goals.add(new Goal(11));
        goals.add(new Goal(12));
        goals.add(new Goal(13));

        return goals;
    }

    final static String[] DESCRIPTIONS = {
            "Capture Europe, Australia and one other continent",
            "Capture Europe, South America and one other continent",
            "Capture North America and Africa",
            "Capture North America and Australia",
            "Capture Asia and South America",
            "Capture Asia and Africa",
            "Capture 24 territories",
            "Capture 18 territories and occupy each with two troops",
            "Destroy dark blue  or — if you are dark blue or dark blue is not in game — capture 24 territories",
            "Destroy lite blue  or — if you are lite blue or lite blue is not in game— capture 24 territories",
            "Destroy red  or — if you are red or red is not in game— capture 24 territories",
            "Destroy black or — if you are black or black is not in game— capture 24 territories",
            "Destroy yellow  or — if you are yellow or yellow is not in game— capture 24 territories",
            "Destroy purple  or — if you are purple or purple is not in game— capture 24 territories"
    };

    // Parcelling part
    public Goal(Parcel in){
        this.id = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Goal createFromParcel(Parcel in) {
            return new Goal(in);
        }

        public Goal[] newArray(int size) {
            return new Goal[size];
        }
    };

}
