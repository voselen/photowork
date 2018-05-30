package com.vostrik.elena.photowork.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vostrik.elena.photowork.model.VkPhotoItem;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Elena on 23.04.2018.
 */

public class PhotoContentProvider {
    String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    SimpleDateFormat dateFormat = new SimpleDateFormat(
            pattern
    );

    private static final String TAG = "PhotoContentProvider";

    StreamReaderWriter streamReaderWriter;

    public List<VkPhotoItem> getPhotoItemList() {
        return photoItemList;
    }

    List<VkPhotoItem> photoItemList;

    public PhotoContentProvider(StreamReaderWriter streamReaderWriter) {
        this.streamReaderWriter = streamReaderWriter;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String loadJSONFromAsset() {
        String result = null;
        try (InputStream is = streamReaderWriter.getInputStream()) {

            ObjectMapper mapper = new ObjectMapper();

            photoItemList = mapper.readValue(is, new TypeReference<ArrayList<VkPhotoItem>>() {
            });
            if (photoItemList.size() > 0) result = "OK";
            Log.d(TAG, "photoItemList.size() " + photoItemList.size());

        } catch (FileNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return result;
    }

    public void saveJSONtoFile(List<VkPhotoItem> list) {
        try {
            Log.d(TAG, "saveJSONtoFile photoItemList.size() " + list.size());
            FileOutputStream fos = (FileOutputStream) streamReaderWriter.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(fos, list);
            Log.d(TAG, "after save file");
            //Files.copy(source.toPath(), dest.toPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
