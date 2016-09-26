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

    public static boolean areNeighboringTeritories(String t1, String t2) {
        boolean[][] neighboringTeritories = new boolean[41][41];
        neighboringTeritories[0][2] = true;
        neighboringTeritories[0][12] = true;
        neighboringTeritories[0][24] = true;
        neighboringTeritories[1][2] = true;
        neighboringTeritories[1][3] = true;
        neighboringTeritories[1][4] = true;
        neighboringTeritories[1][5] = true;
        neighboringTeritories[1][6] = true;
        neighboringTeritories[1][12] = true;
        neighboringTeritories[2][0] = true;
        neighboringTeritories[2][1] = true;
        neighboringTeritories[2][4] = true;
        neighboringTeritories[2][12] = true;
        neighboringTeritories[3][1] = true;
        neighboringTeritories[3][4] = true;
        neighboringTeritories[3][5] = true;
        neighboringTeritories[4][1] = true;
        neighboringTeritories[4][2] = true;
        neighboringTeritories[4][3] = true;
        neighboringTeritories[4][13] = true;
        neighboringTeritories[5][1] = true;
        neighboringTeritories[5][3] = true;
        neighboringTeritories[5][6] = true;
        neighboringTeritories[5][7] = true;
        neighboringTeritories[6][1] = true;
        neighboringTeritories[6][5] = true;
        neighboringTeritories[6][7] = true;
        neighboringTeritories[6][12] = true;
        neighboringTeritories[7][5] = true;
        neighboringTeritories[7][6] = true;
        neighboringTeritories[7][8] = true;
        neighboringTeritories[8][7] = true;
        neighboringTeritories[8][9] = true;
        neighboringTeritories[8][11] = true;
        neighboringTeritories[9][8] = true;
        neighboringTeritories[9][10] = true;
        neighboringTeritories[9][11] = true;
        neighboringTeritories[9][31] = true;
        neighboringTeritories[10][9] = true;
        neighboringTeritories[10][11] = true;
        neighboringTeritories[11][8] = true;
        neighboringTeritories[11][9] = true;
        neighboringTeritories[11][10] = true;
        neighboringTeritories[12][0] = true;
        neighboringTeritories[12][1] = true;
        neighboringTeritories[12][2] = true;
        neighboringTeritories[12][6] = true;
        neighboringTeritories[13][4] = true;
        neighboringTeritories[13][14] = true;
        neighboringTeritories[13][15] = true;
        neighboringTeritories[14][13] = true;
        neighboringTeritories[14][15] = true;
        neighboringTeritories[14][16] = true;
        neighboringTeritories[15][13] = true;
        neighboringTeritories[15][14] = true;
        neighboringTeritories[15][17] = true;
        neighboringTeritories[15][18] = true;
        neighboringTeritories[16][14] = true;
        neighboringTeritories[16][17] = true;
        neighboringTeritories[16][31] = true;
        neighboringTeritories[16][32] = true;
        neighboringTeritories[17][15] = true;
        neighboringTeritories[17][16] = true;
        neighboringTeritories[17][18] = true;
        neighboringTeritories[17][28] = true;
        neighboringTeritories[18][15] = true;
        neighboringTeritories[18][17] = true;
        neighboringTeritories[18][19] = true;
        neighboringTeritories[18][25] = true;
        neighboringTeritories[18][28] = true;
        neighboringTeritories[19][18] = true;
        neighboringTeritories[19][20] = true;
        neighboringTeritories[19][25] = true;
        neighboringTeritories[19][26] = true;
        neighboringTeritories[20][19] = true;
        neighboringTeritories[20][21] = true;
        neighboringTeritories[20][22] = true;
        neighboringTeritories[20][23] = true;
        neighboringTeritories[20][26] = true;
        neighboringTeritories[21][20] = true;
        neighboringTeritories[21][22] = true;
        neighboringTeritories[21][24] = true;
        neighboringTeritories[22][20] = true;
        neighboringTeritories[22][21] = true;
        neighboringTeritories[22][23] = true;
        neighboringTeritories[22][24] = true;
        neighboringTeritories[23][20] = true;
        neighboringTeritories[23][22] = true;
        neighboringTeritories[23][24] = true;
        neighboringTeritories[23][26] = true;
        neighboringTeritories[23][27] = true;
        neighboringTeritories[24][21] = true;
        neighboringTeritories[24][22] = true;
        neighboringTeritories[24][23] = true;
        neighboringTeritories[24][27] = true;
        neighboringTeritories[24][0] = true;
        neighboringTeritories[25][18] = true;
        neighboringTeritories[25][19] = true;
        neighboringTeritories[25][26] = true;
        neighboringTeritories[25][28] = true;
        neighboringTeritories[25][29] = true;
        neighboringTeritories[26][19] = true;
        neighboringTeritories[26][20] = true;
        neighboringTeritories[26][23] = true;
        neighboringTeritories[26][25] = true;
        neighboringTeritories[26][29] = true;
        neighboringTeritories[26][30] = true;
        neighboringTeritories[27][23] = true;
        neighboringTeritories[27][24] = true;
        neighboringTeritories[28][17] = true;
        neighboringTeritories[28][18] = true;
        neighboringTeritories[28][25] = true;
        neighboringTeritories[28][29] = true;
        neighboringTeritories[28][32] = true;
        neighboringTeritories[29][25] = true;
        neighboringTeritories[29][26] = true;
        neighboringTeritories[29][28] = true;
        neighboringTeritories[29][30] = true;
        neighboringTeritories[30][26] = true;
        neighboringTeritories[30][29] = true;
        neighboringTeritories[30][37] = true;
        neighboringTeritories[31][9] = true;
        neighboringTeritories[31][16] = true;
        neighboringTeritories[31][32] = true;
        neighboringTeritories[31][33] = true;
        neighboringTeritories[31][34] = true;
        neighboringTeritories[32][16] = true;
        neighboringTeritories[32][28] = true;
        neighboringTeritories[32][31] = true;
        neighboringTeritories[32][33] = true;
        neighboringTeritories[33][31] = true;
        neighboringTeritories[33][32] = true;
        neighboringTeritories[33][34] = true;
        neighboringTeritories[33][35] = true;
        neighboringTeritories[33][36] = true;
        neighboringTeritories[34][31] = true;
        neighboringTeritories[34][33] = true;
        neighboringTeritories[34][35] = true;
        neighboringTeritories[35][33] = true;
        neighboringTeritories[35][34] = true;
        neighboringTeritories[35][36] = true;
        neighboringTeritories[36][33] = true;
        neighboringTeritories[36][35] = true;
        neighboringTeritories[37][30] = true;
        neighboringTeritories[37][38] = true;
        neighboringTeritories[37][39] = true;
        neighboringTeritories[38][37] = true;
        neighboringTeritories[38][40] = true;
        neighboringTeritories[39][37] = true;
        neighboringTeritories[39][40] = true;
        neighboringTeritories[40][38] = true;
        neighboringTeritories[40][39] = true;

        int id1 = Integer.parseInt(t1.substring(3));
        int id2 = Integer.parseInt(t2.substring(3));

        return neighboringTeritories[id1][id2];
    }
}
















