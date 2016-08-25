package com.rs2.risiko.data;

import android.os.Parcel;
import android.os.Parcelable;

public class GameData implements Parcelable {
    private int myInt;

    public GameData(int myInt) {
        this.myInt = myInt;
    }

    protected GameData(Parcel in) {
        myInt = in.readInt();
    }

    public static final Creator<GameData> CREATOR = new Creator<GameData>() {
        @Override
        public GameData createFromParcel(Parcel in) {
            return new GameData(in);
        }

        @Override
        public GameData[] newArray(int size) {
            return new GameData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeInt(myInt);
    }
}
