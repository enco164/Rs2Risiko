package com.rs2.risiko.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by enco on 12.9.16..
 */
public class User implements Parcelable{
    String userId;
    String color;
    Goal goal;
    int stars;

    public User(String userId, String color, Goal goal) {
        this.userId = userId;
        this.color = color;
        this.goal = goal;
        this.stars = 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    // Parcelling part
    public User(Parcel in){
        this.userId = in.readString();
        this.color = in.readString();
        this.goal = in.readParcelable(Goal.class.getClassLoader());
        this.stars = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(userId);
        parcel.writeString(color);
        parcel.writeParcelable(goal, flags);
        parcel.writeInt(stars);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", color='" + color + '\'' +
                ", goal=" + goal +
                '}';
    }

}
