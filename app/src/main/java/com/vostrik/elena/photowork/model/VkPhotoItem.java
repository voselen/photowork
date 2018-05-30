package com.vostrik.elena.photowork.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vostrik.elena.photowork.util.DateSerializer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * VkPhotoItem класс, для сохранения описания картинок с Vk. в Vk.API есть встроенный класс VKApiPhoto, но
 * он слишком много занимает памяти и не дает возможности передать списки таких объектов между активностями
 * сделан Parcelable, чтобы можно было передавать через активити, на данный момент не используется
 * Created by Elena on 12.04.2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VkPhotoItem implements Comparable<VkPhotoItem>, Parcelable {

    String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    SimpleDateFormat dateFormat = new SimpleDateFormat(//"yyyy.MM.dd G 'at' HH:mm:ss z"
            pattern
    );

    @JsonProperty("id")
    public long id;

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("photo_75")
    public String photo_75;
    @JsonProperty("photo_130")
    public String photo_130;
    @JsonProperty("photo_1280")
    public String photo_1280;
    @JsonProperty("width")
    public int width;
    @JsonProperty("height")
    public int height;

    @JsonSerialize(using=DateSerializer.class)
    @JsonProperty("date")
    public Date date;

    @JsonProperty("orderId")
    public int orderId;

    @Override
    public String toString() {
        return "VkPhotoItem{" +
                "id=" + id +
                ", width=" + width +
                ", height=" + height +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VkPhotoItem that = (VkPhotoItem) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int compareTo(@NonNull VkPhotoItem photoItem) {
        return Long.valueOf(this.id).compareTo(photoItem.id);
    }

    public VkPhotoItem(Parcel in) {
        String[] data = new String[8];
        in.readStringArray(data);

        id = new Long(data[0]);

        photo_75 = data[1];
        photo_130 = data[2];
        photo_1280 = data[3];
        width = new Integer(data[4]);
        height = new Integer(data[5]);
        try {
            date = dateFormat.parse(data[6]);
        } catch (ParseException e) {
            date = null;
        }
        orderId = new Integer(data[7]);
    }

    public VkPhotoItem() {

    }

    public VkPhotoItem(long id, String photo_75, String photo_130, String photo_1280, int width, int height, Date date, int orderId) {
        this.id = id;
        this.photo_75 = photo_75;
        this.photo_130 = photo_130;
        this.photo_1280 = photo_1280;
        this.width = width;
        this.height = height;
        this.date = date;
        this.orderId = orderId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{id + "", photo_75, photo_130, photo_1280,
                width + "", height + "", dateFormat.format(date), orderId + ""});
    }

    public static final Parcelable.Creator<VkPhotoItem> CREATOR = new Parcelable.Creator<VkPhotoItem>() {

        @Override
        public VkPhotoItem createFromParcel(Parcel source) {
            return new VkPhotoItem(source);
        }

        @Override
        public VkPhotoItem[] newArray(int size) {
            return new VkPhotoItem[size];
        }
    };

}
