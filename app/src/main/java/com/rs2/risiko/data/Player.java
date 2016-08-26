package com.rs2.risiko.data;

import com.google.android.gms.games.multiplayer.Participant;

/**
 * Created by enco on 27.8.16..
 */
public class Player {
    private Participant participant;
    private int color;
    private Goal goal;

    public Player(Participant participant, int color, int goalId) {
        this.participant = participant;
        this.color = color;
        this.goal = new Goal(goalId);
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Goal getGoal() {
        return goal;
    }
}
