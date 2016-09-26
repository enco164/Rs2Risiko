package com.rs2.risiko.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by enco on 27.8.16..
 */
public class Territory implements Parcelable{
    private String id;
    private String name;
    private String userId;
    private int armies;

    public Territory(String id, String name, int armies){
        this.id = id;
        this.name = name;
        this.armies = armies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getArmies() {
        return armies;
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

    // Parcelling part
    public Territory(Parcel in){
        this.id = in.readString();
        this.name = in.readString();
        this.userId = in.readString();
        this.armies = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(userId);
        parcel.writeInt(armies);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Territory createFromParcel(Parcel in) {
            return new Territory(in);
        }

        public Territory[] newArray(int size) {
            return new Territory[size];
        }
    };

    @Override
    public String toString() {
        return "Territory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", armies=" + armies +
                '}';
    }

    public static ArrayList<Territory> getAllTerritories(){
        /* za sad hardkodovano koja je cija, ali to moze lako seterima da se popuni. Svakako treba puna lista */

        ArrayList<Territory>  territories = new ArrayList<>();

        territories.add(new Territory("RS-00", "Alaska", 1));
        territories.add(new Territory("RS-01", "Ontario", 1));
        territories.add(new Territory("RS-02", "Northern Territory", 1));
        territories.add(new Territory("RS-03", "Quebec", 1));
        territories.add(new Territory("RS-04", "Greenland", 0));
        territories.add(new Territory("RS-05", "Eastern United States", 1));
        territories.add(new Territory("RS-06", "Western United States", 1));
        territories.add(new Territory("RS-07", "Central America", 1));
        territories.add(new Territory("RS-08", "Venezuela", 0));
        territories.add(new Territory("RS-09", "Brazil", 1));
        territories.add(new Territory("RS-10", "Argentina", 1));
        territories.add(new Territory("RS-11", "Peru", 1));
        territories.add(new Territory("RS-12", "Alberta", 1));
        territories.add(new Territory("RS-13", "Island", 1));
        territories.add(new Territory("RS-14", "Great Britain", 1));
        territories.add(new Territory("RS-15", "Scandinavia", 1));
        territories.add(new Territory("RS-16", "Western Europe", 1));
        territories.add(new Territory("RS-17", "Central Europe", 1));
        territories.add(new Territory("RS-18", "Ukraine", 1));
        territories.add(new Territory("RS-19", "Ural", 1));
        territories.add(new Territory("RS-20", "Siberia", 1));
        territories.add(new Territory("RS-21", "Jakutz", 1));
        territories.add(new Territory("RS-22", "Irkutz", 1));
        territories.add(new Territory("RS-23", "Mongolia", 1));
        territories.add(new Territory("RS-24", "Kamchatka", 1));
        territories.add(new Territory("RS-25", "Afghanistan", 1));
        territories.add(new Territory("RS-26", "China", 1));
        territories.add(new Territory("RS-27", "Japan", 1));
        territories.add(new Territory("RS-28", "Middle East", 1));
        territories.add(new Territory("RS-29", "India", 1));
        territories.add(new Territory("RS-30", "Siam", 1));
        territories.add(new Territory("RS-31", "North Africa", 1));
        territories.add(new Territory("RS-32", "Egypt", 1));
        territories.add(new Territory("RS-33", "East Africa", 1));
        territories.add(new Territory("RS-34", "Congo", 1));
        territories.add(new Territory("RS-35", "South Africa", 1));
        territories.add(new Territory("RS-36", "Madagascar", 1));
        territories.add(new Territory("RS-37", "Indonesia", 1));
        territories.add(new Territory("RS-38", "New Guinea", 1));
        territories.add(new Territory("RS-39", "Western Australia", 1));
        territories.add(new Territory("RS-40", "Eastern Australia", 1));




        return territories;

    }
}
