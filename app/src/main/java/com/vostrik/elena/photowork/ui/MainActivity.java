package com.vostrik.elena.photowork.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vostrik.elena.photowork.Application;
import com.vostrik.elena.photowork.R;
import com.vostrik.elena.photowork.asynctask.GetVkPhotoItemsTask;
import com.vostrik.elena.photowork.model.VkPhotoItem;
import com.vostrik.elena.photowork.util.DiskLruCache;
import com.vostrik.elena.photowork.util.PhotoContentProvider;
import com.vostrik.elena.photowork.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static android.os.Environment.isExternalStorageRemovable;

/**
 * Стартовый активити, на котором происходит:
 * - авторизация пользователя в VK
 * - получение общего количества фотографий, на которых отмечен пользователь
 * - запуск фрагмента-галереи для отображения фотографий
 * Created by Elena on 19.04.2018.
 */

public class MainActivity extends FragmentActivity {
    ////
    private final Object mDiskCacheLock = new Object();
    SharedPreferencesUtil sharedPreferencesUtil;
    PhotoContentProvider contentProvider;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    ProgressBar progressBar;
    ////
    public static int SCREEN_HEIGHT = 0;
    public static int SCREEN_WIDTH = 0;
    public static int SIZE_W = 0;
    public static int SIZE_H = 0;

    private void setSize() {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        SCREEN_WIDTH = dm.widthPixels;
        SCREEN_HEIGHT = dm.heightPixels;
        SIZE_W = SIZE_H = SCREEN_WIDTH / 3;
    }

    //Используется для лога
    private static final String TAG = "MainActivity";

    //Количество фотографий, на которых пользователь отмечен в ВК
    private int photoCount;

    //Текущий контекст, используется для передачи в другие компоненты
    Context context;

    int content;

    //Список названий прав доступа, которые необходимы приложению
    private static final String[] sMyScope = new String[]{
            VKScope.PHOTOS
    };

    //
    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            Log.d(TAG, "Start InitDiskCacheTask");
            synchronized (mDiskCacheLock) {
                File cacheDir = params[0];
                Application.mDiskLruCache = DiskLruCache.open(cacheDir, DISK_CACHE_SIZE);
                Application.mDiskCacheStarting = false; // Закончили инициализацию
                mDiskCacheLock.notifyAll(); // Будим ожидающие потоки
            }
            Log.d(TAG, "End InitDiskCacheTask");
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        content = android.R.id.content;
        progressBar = (ProgressBar) findViewById(R.id.startProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        setSize();

        sharedPreferencesUtil = new SharedPreferencesUtil(getPreferences(MODE_PRIVATE));
        sharedPreferencesUtil.getPreferences();

        contentProvider = new PhotoContentProvider(context);

        // Запускаем дисковый кэш в отдельном потоке
        File cacheDir = getDiskCacheDir(this, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir);


        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
                @Override
                public void onResult(VKSdk.LoginState res) {
                    switch (res) {
                        case LoggedOut:
                            //попытаться войти в ВК
                            VKSdk.login(MainActivity.this, sMyScope);
                            if (VKSdk.isLoggedIn()) {
                                //запрос фоток пользователя
                                Log.d(TAG, "After Logged in!");
                                getPhotos();

                            }
                            break;
                        case LoggedIn: {
                            //запрос фоток пользователя
                            Log.d(TAG, "isLogged in!");
                            getPhotos();

                            break;
                        }
                        case Pending:
                            //  Toast.makeText(context,
                            //          "Подождите, идет загрузка..", Toast.LENGTH_LONG).show();
                            break;
                        case Unknown:
                            break;
                        default: {
                            Toast.makeText(context,
                                    "Необходимо закрыть приложение и еще раз попытаться войти в ВКонтакте", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onError(VKError error) {
                    Log.e(TAG, "VKError " + error);
                }
            });

        }
        Log.d(TAG, "onCreate in the end");
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context    The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        Log.d(TAG, "getDiskCacheDir " + cachePath + File.separator + uniqueName);
        File file = new File(cachePath + File.separator + uniqueName);
        return file;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferencesUtil.setPreferences();
        Log.d(TAG, "onDestroy");
    }

    void getPhotos() {
        Log.d(TAG, "Start get sharedPref");
        Log.d(TAG, "sharedPreferencesUtil.getVkUserId() " + sharedPreferencesUtil.getVkUserId());
        //если приложение открыто не первый раз
        if (sharedPreferencesUtil.getVkUserId() != null) {
            //если последняя дата обновления данных не больше одного дня
            int days = (int) ((sharedPreferencesUtil.getLastScanDate().getTime() - (new Date()).getTime()) / (24 * 60 * 60 * 1000));
            if (days > 1)
            //если данным больше одного дня, то сделать запрос в ВК
            {
                sharedPreferencesUtil.setLastScanDate(new Date());
                getVkPhotos();
            } else {
                try {
                    Log.d(TAG,
                            contentProvider.loadJSONFromAsset(this));
                    Application.vkPhotos = contentProvider.getPhotoItemList();
                    Collections.sort(Application.vkPhotos, new Comparator<VkPhotoItem>() {
                        @Override
                        public int compare(VkPhotoItem photoItem, VkPhotoItem t1) {
                            return Integer.valueOf(t1.orderId).compareTo(photoItem.orderId);
                        }
                    });
                    if (Application.vkPhotos != null)
                        contentProvider.saveJSONtoFile(Application.vkPhotos);
                    Application.photoCount = Application.vkPhotos.size();
                    final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.add(content, new PhotoGridFragment(), TAG);
                    ft.commitAllowingStateLoss();//вместо commit(), чтобы избежать java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                }
                catch (Exception e)
                        //если не удалось загрузить файл со списком, то запрашиваем новый
                {
                    sharedPreferencesUtil.setLastScanDate(new Date());
                    getVkPhotos();
                }
            }
        } else {
            sharedPreferencesUtil.setLastScanDate(new Date());
            getVkPhotos();
        }
    }


    void getVkPhotoItems(int photoCount) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        GetVkPhotoItemsTask getVkPhotoItemsTask = new GetVkPhotoItemsTask(content, context, fragmentManager, progressBar, contentProvider);
        getVkPhotoItemsTask.execute(photoCount);
    }


    void getVkPhotos() {
        final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "counters"));
        if (request != null) {
            request.unregisterObject();
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    try {
                        JSONArray jsonArray = response.json.getJSONArray("response");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            if (jsonArray.getJSONObject(i).has("id")) {
                                Log.d(TAG, "Ste userID " + jsonArray.getJSONObject(i).getInt("id"));
                                sharedPreferencesUtil.setVkUserId(String.valueOf(jsonArray.getJSONObject(i).getInt("id")));
                            }
                            if (jsonArray.getJSONObject(i).has("counters")) {
                                photoCount = jsonArray.getJSONObject(i).getJSONObject("counters").getInt("user_photos");
                                Application.photoCount = photoCount;
                                Log.d(TAG, "photoCount " + photoCount);
                                sharedPreferencesUtil.setPhotoCount(photoCount);
                                getVkPhotoItems(photoCount);
                                break;
                            }
                        }
                        if (photoCount == 0)
                            Toast.makeText(context,
                                    " У вас нет фотографий, на которых вы отмечены", Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    //при первом открытии приложения будет выполнения авторизация
    // пользователя в ВКонтакте и сохранение его ИД
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
                sharedPreferencesUtil.setVkUserId(res.userId);
                Log.d(TAG, " sharedPreferencesUtil " + sharedPreferencesUtil.getVkUserId());
                getPhotos();

            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Toast.makeText(context,
                        "Ошибка авторизации в ВКонтакте\nError code " + error.errorCode + "\tMessage " + error.errorMessage + "\tReason " + error.errorReason, Toast.LENGTH_LONG).show();
                Log.e("Auth VKError", "Error code " + error.errorCode + "\tMessage " + error.errorMessage + "\tReason " + error.errorReason);


            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);

        }
    }


}
