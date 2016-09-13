package com.rs2.risiko.data;

import java.util.ArrayList;

/**
 * Created by enco on 27.8.16..
 */
public class Territory {
    private String id;
    private String name;
    private int armies;
    private String userId;
    private PlayerRisk player;

    public Territory(String name, String id, int armies, PlayerRisk player){
        this.name = name;
        this.id = id;
        this.armies = armies;
        this.player = player;
    }

    public Territory(String name, String id, int armies){
        this.name = name;
        this.id = id;
        this.armies = armies;
        this.player = new PlayerRisk(null, 0, 0, 0);

    }

    public String getId() {
        return id;
    }

    public int getArmies() {
        return armies;
    }

    public PlayerRisk getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayer(PlayerRisk player) {
        this.player = player;
    }

    public void setArmies(int armies) {
        this.armies = armies;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public static ArrayList<Territory> getAllTerritories(){
        /* za sad hardkodovano koja je cija, ali to moze lako seterima da se popuni. Svakako treba puna lista */

        ArrayList<Territory>  territories = new ArrayList<Territory>();
    //        public PlayerRisk(Participant participant, int color, int goalId, int id) {

        territories.add(new Territory("Alaska", "RS-00", 0));
        territories.add(new Territory("Alberta", "RS-01", 0));
        territories.add(new Territory("Central America", "RS-02", 0));
        territories.add(new Territory("Dzibuti", "RS-03", 0));
        territories.add(new Territory("Eastern United States", "RS-04", 0));
        territories.add(new Territory("`Greenland", "RS-05", 0));
        territories.add(new Territory("Ontario", "RS-06", 0));
        territories.add(new Territory("Quebec", "RS-07", 0));
        territories.add(new Territory("Western United States", "RS-08", 0));
        territories.add(new Territory("Great Britain", "RS-09", 0));
        territories.add(new Territory("Iceland", "RS-10", 0));
        territories.add(new Territory("Northern Europe", "RS-11", 0));
        territories.add(new Territory("Scandinavia", "RS-12", 0));
        territories.add(new Territory("Southern Europe", "RS-13", 0));
        territories.add(new Territory("Ukraine", "RS-14", 0));
        territories.add(new Territory("Western Europe", "RS-15", 0));
        territories.add(new Territory("Argentina", "RS-16", 0));
        territories.add(new Territory("Brazil", "RS-17", 0));
        territories.add(new Territory("Peru", "RS-18", 0));
        territories.add(new Territory("Venezuela", "RS-19", 0));

        return territories;
    }
}
