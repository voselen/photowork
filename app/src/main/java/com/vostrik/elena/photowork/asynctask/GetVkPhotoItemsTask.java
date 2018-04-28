package com.vostrik.elena.photowork.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vostrik.elena.photowork.Application;
import com.vostrik.elena.photowork.model.VkPhotoItem;
import com.vostrik.elena.photowork.ui.PhotoGridFragment;
import com.vostrik.elena.photowork.util.PhotoContentProvider;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Elena on 24.04.2018.
 */

public class GetVkPhotoItemsTask extends AsyncTask<Integer, Void, Void>{

    int content;
    Context context;
    FragmentManager fragmentManager;
    ProgressBar progressBar;
    PhotoContentProvider contentProvider;
    PhotoGridFragment photoGridFragment;

    public void setStopped(boolean stopped) {
        isStopped = stopped;
        cancel(true);
    }

    boolean isStopped = false;

    public GetVkPhotoItemsTask(int content, Context context, FragmentManager fragmentManager, ProgressBar progressBar, PhotoContentProvider contentProvider, PhotoGridFragment photoGridFragment) {
        this.content = content;
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.progressBar = progressBar;
        this.contentProvider = contentProvider;
        this.photoGridFragment = photoGridFragment;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        int photoCount = integers[0];
        int pagesCount = photoCount > Application.MAX_PHOTO_COUNT  ? (photoCount / Application.MAX_PHOTO_COUNT + 1) : 1;
        int offset = 0;
        for (int i = 0; i < pagesCount; i++) {
            offset = i == 0 ? i : i * Application.MAX_PHOTO_COUNT + 1;
            VKRequest photoWitMeRequest = new VKRequest("photos.getUserPhotos",
                    VKParameters.from(VKApiConst.SORT, 0, VKApiConst.COUNT, Application.MAX_PHOTO_COUNT,
                            VKApiConst.OFFSET, offset));
            photoWitMeRequest.executeSyncWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<VkPhotoItem> list = null;
                    try {
                        list = mapper.readValue(response.json.getJSONObject("response")
                                        .getJSONArray("items").toString(),
                                new TypeReference<ArrayList<VkPhotoItem>>() {
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, e.getMessage());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, "JSONException " + e.getMessage());
                    }
                    Application.vkPhotos.addAll(list);
                    Application.vkPhotos = new ArrayList<VkPhotoItem>(new HashSet<VkPhotoItem>(Application.vkPhotos));
                }

                @Override
                public void onError(VKError error) {
                    Log.e("GetPhotos VKError", "Error code " + error.errorCode + "\tMessage " + error.errorMessage + "\tReason " + error.errorReason);
                }

                @Override
                public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                                       long bytesTotal) {
                    // you can show progress of the request if you want
                    Log.d(TAG, "bytesLoaded " + bytesLoaded + "\tbytesTotal " + bytesTotal);
                }

            });

            progressBar.setVisibility(View.VISIBLE);
        }

        Collections.sort(Application.vkPhotos, new Comparator<VkPhotoItem>() {
            @Override
            public int compare(VkPhotoItem photoItem, VkPhotoItem t1) {
                return t1.date.compareTo(photoItem.date);
            }
        });
        int weight = Application.vkPhotos.size() - 1;
        for (int index = 0; weight >= 0; weight--, index++) {
            Application.vkPhotos.get(index).setOrderId(weight);
        }
        if (Application.vkPhotos != null)
            contentProvider.saveJSONtoFile(Application.vkPhotos);
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(content, photoGridFragment, TAG);
        ft.commitAllowingStateLoss();//вместо commit(), чтобы избежать java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState

        return null;
    }
}
