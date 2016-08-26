package com.rs2.risiko.data;

/**
 * Created by enco on 27.8.16..
 */
public class Goal {
    private int goalId;

    public Goal(int goalId) {
        this.goalId = goalId;
    }

    public boolean isDone() {
        return false; //TODO: Implementirati proveru zadatka
    }

    public String getDescription() {
        return DESCRIPTIONS[goalId];
    }

    public int getGoalId() {
        return goalId;
    }

    final static String[] DESCRIPTIONS = {
            "Unistiti crnog",
            "Unistiti plavog",
            "Osvojiti ceo svet"
    };
}
