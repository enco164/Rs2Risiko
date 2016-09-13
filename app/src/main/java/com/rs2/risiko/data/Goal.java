package com.rs2.risiko.data;

import java.util.ArrayList;

/**
 * Created by enco on 27.8.16..
 */
public class Goal {
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
        ArrayList<Goal> goals = new ArrayList<Goal>();

        goals.add(new Goal(0));
        goals.add(new Goal(1));
        goals.add(new Goal(2));
        goals.add(new Goal(3));
        goals.add(new Goal(4));

        return goals;
    }

    final static String[] DESCRIPTIONS = {
            "Unistiti zelenog",
            "Unistiti plavog",
            "Osvojiti ceo svet",
            "Osvoji Severnu Ameriku i Afriku",
            "Osvoji Aziju i Juznu Ameriku"
    };
}
