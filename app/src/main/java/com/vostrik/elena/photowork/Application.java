package com.vostrik.elena.photowork;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.vostrik.elena.photowork.model.VkPhotoItem;
import com.vostrik.elena.photowork.ui.MainActivity;
import com.vostrik.elena.photowork.util.DiskLruCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Elena on 12.04.2018.
 */

public class Application extends android.app.Application {

    //ограничение Vk.APi
    public static final int MAX_PHOTO_COUNT = 1000;

    public static int photoCount = 0;
    //Количество фото, загружаемых за раз (примерно равно количество фотографий на одном экране)
    public static final int PHOTO_PER_PAGE = 15;
    //Список идентификаторов фотографий и их URL
    public static List<VkPhotoItem> vkPhotos = new ArrayList<>();
    //Список фотографий, которые будут в адаптере для отображения
    public static List<VkPhotoItem> photoAdapterPhotos;
    //Кэш для больших картинок
    public static LruCache<Long, Bitmap> bigPhotoMemoryCache;
    //Кэш для маленьких картинок
    public static LruCache<Long, Bitmap> preViewPhotoMemoryCache;

    //Дисковый кэш
    public static DiskLruCache mDiskLruCache;
    public static boolean mDiskCacheStarting = true;

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                Toast.makeText(Application.this, "AccessToken invalidated", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Application.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);
        vkPhotos = new ArrayList<>();
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        final int cacheSize = maxMemory / 8;

        bigPhotoMemoryCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

        preViewPhotoMemoryCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

        photoAdapterPhotos = new CopyOnWriteArrayList<VkPhotoItem>();//Collections.synchronizedList(new ArrayList<VkPhotoItem>());
    }
}
