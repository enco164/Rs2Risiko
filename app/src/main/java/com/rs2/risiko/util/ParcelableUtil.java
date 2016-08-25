package com.rs2.risiko.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by enco on 24.8.16..
 *
 * Unmarshalling (with CREATOR)
 *
 * byte[] bytes = …
 * MyClass myclass = ParcelableUtil.unmarshall(bytes, MyClass.CREATOR);
 *
 *
 * Unmarshalling (without CREATOR)
 *
 * byte[] bytes = …
 * Parcel parcel = ParcelableUtil.unmarshall(bytes);
 * MyClass myclass = new MyClass(parcel); // Or MyClass.CREATOR.createFromParcel(parcel).
 *
 *
 * Marshalling
 *
 * MyClass myclass = …
 * byte[] bytes = ParcelableUtil.marshall(myclass);
 */
public class ParcelableUtil {
    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // This is extremely important!
        return parcel;
    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }
}
