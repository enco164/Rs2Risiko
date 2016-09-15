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

        territories.add(new Territory("RS-00", "Alaska", 0));
        territories.add(new Territory("RS-01", "Alberta", 0));
        territories.add(new Territory("RS-02", "Central America", 0));
        territories.add(new Territory("RS-03", "Dzibuti", 0));
        territories.add(new Territory("RS-04", "Eastern United States", 0));
        territories.add(new Territory("RS-05", "`Greenland", 0));
        territories.add(new Territory("RS-06", "Ontario", 0));
        territories.add(new Territory("RS-07", "Quebec", 0));
        territories.add(new Territory("RS-08", "Western United States", 0));
        territories.add(new Territory("RS-09", "Great Britain", 0));
        territories.add(new Territory("RS-10", "Iceland", 0));
        territories.add(new Territory("RS-11", "Northern Europe", 0));
        territories.add(new Territory("RS-12", "Scandinavia", 0));
        territories.add(new Territory("RS-13", "Southern Europe", 0));
        territories.add(new Territory("RS-14", "Ukraine", 0));
        territories.add(new Territory("RS-15", "Western Europe", 0));
        territories.add(new Territory("RS-16", "Argentina", 0));
        territories.add(new Territory("RS-17", "Brazil", 0));
        territories.add(new Territory("RS-18", "Peru", 0));
        territories.add(new Territory("RS-19", "Venezuela", 0));

        return territories;
    }
}
