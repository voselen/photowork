package com.vostrik.elena.photowork.util;

import android.content.Context;
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
import java.util.Arrays;
import java.util.List;

/**
 * Created by Elena on 23.04.2018.
 */

public class PhotoContentProvider {
    private static final String TAG = "PhotoContentProvider";
    private final String DATA_FILE_NAME = "photoVkItems.json";

    String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

    //File logFile;
    Context context;

    public List<VkPhotoItem> getPhotoItemList() {
        return photoItemList;
    }

    List<VkPhotoItem> photoItemList;

    public PhotoContentProvider(Context context) {
        this.context = context;
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.openFileInput(DATA_FILE_NAME);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            mapper.setDateFormat(dateFormat);
            photoItemList = mapper.readValue(json,
                    new TypeReference<ArrayList<VkPhotoItem>>() {
                    });
            Log.d(TAG, "photoItemList.size() " + photoItemList.size());

        } catch (FileNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void saveJSONtoFile(List<VkPhotoItem> list) {
        try {
            FileOutputStream fos = context.openFileOutput(DATA_FILE_NAME, Context.MODE_PRIVATE);
            ObjectMapper mapper = new ObjectMapper();
            //mapper.setDateFormat(dateFormat);
            // Writing to a file
            Log.d(TAG, Arrays.toString(list.toArray()));
            //mapper.writeValue(fos, list);
            //mapper.writeValueAsString(list);
            mapper.writer(dateFormat);
            mapper.writeValue(fos, list);
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
