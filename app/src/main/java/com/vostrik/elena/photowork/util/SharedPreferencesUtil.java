package com.vostrik.elena.photowork.util;

import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Сохраняемые значения
 * Created by Elena on 23.04.2018.
 */

public class SharedPreferencesUtil {

    public static String USER_ID_STR="vkUserId";
    public static String PHOTO_COUNT_STR="photoCount";
    public static String LAST_SCAN_DATE_STR ="lastScanDate";

    String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);


    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public String getVkUserId() {
        return vkUserId;
    }

    public void setVkUserId(String vkUserId) {
        this.vkUserId = vkUserId;
    }

    int photoCount;
    String vkUserId;

    public Date getLastScanDate() {
        return lastScanDate;
    }

    public void setLastScanDate(Date lastScanDate) {
        this.lastScanDate = lastScanDate;
    }

    Date lastScanDate;

    SharedPreferences preferences;

    public SharedPreferencesUtil(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void getPreferences(){
        photoCount = preferences.getInt(PHOTO_COUNT_STR, 0);
        vkUserId = preferences.getString(USER_ID_STR, null);
        try {
            lastScanDate = dateFormat.parse(preferences.getString(LAST_SCAN_DATE_STR, dateFormat.format(new Date())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setPreferences(){
        SharedPreferences.Editor ed = preferences.edit();
        ed.putInt(PHOTO_COUNT_STR, photoCount);
        ed.putString(USER_ID_STR, vkUserId);
        ed.putString(LAST_SCAN_DATE_STR, dateFormat.format(lastScanDate));
        ed.commit();
    }
}
