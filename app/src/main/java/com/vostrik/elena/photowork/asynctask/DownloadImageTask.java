package com.vostrik.elena.photowork.asynctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.vostrik.elena.photowork.R;
import com.vostrik.elena.photowork.exception.GetImageByUrlException;
import com.vostrik.elena.photowork.exception.GetImageIOException;
import com.vostrik.elena.photowork.model.PhotoType;
import com.vostrik.elena.photowork.model.VkLoadPhotoItem;
import com.vostrik.elena.photowork.util.ImageServiceUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * DownloadImageTask для асинхронной загрузки изображений из интернета и помщения их в нужный кэш в зависимости от типа
 * Created by Elena on 13.04.2018.
 */

public class DownloadImageTask extends AsyncTask<ImageView, Void, Bitmap> {

    private static final String TAG = "DownloadImageTask";
    ImageView imageView = null;
    VkLoadPhotoItem photoItem = null;
    Context context;

    public DownloadImageTask(Context context) {
        this.context = context;
    }

    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {
        this.imageView = imageViews[0];
        Bitmap bitmap = null;
        photoItem = (VkLoadPhotoItem) imageView.getTag();
        if (photoItem.photoType == PhotoType.PREVIEW) {
            bitmap = ImageServiceUtil.getBitmapFromDiskCache(String.valueOf(photoItem.photoItem.id));
            if (bitmap == null) {
                // Проверяем дисковый кэщ в отдельном потоке
                try {
                    bitmap = download_Image(photoItem.photoType == PhotoType.BIG ? photoItem.photoItem.photo_1280 : photoItem.photoItem.photo_75);
                } catch (GetImageByUrlException e) {
                    Log.e(TAG, "GetImageByUrlException on preViewImage: " + e.getMessage());
                    Drawable drawable = context.getResources().getDrawable(R.drawable.empty_photo);
                    bitmap = ((BitmapDrawable) drawable).getBitmap();
                } catch (GetImageIOException e) {
                    Drawable drawable = context.getResources().getDrawable(R.drawable.empty_photo);
                    bitmap = ((BitmapDrawable) drawable).getBitmap();
                }
            } else {
                //добавляем в кэщ памяти
                ImageServiceUtil.addBitmapToMemoryCache(photoItem.photoItem.id, bitmap);
            }
        }

        try {
            bitmap = download_Image(photoItem.photoType == PhotoType.BIG ? photoItem.photoItem.photo_1280 : photoItem.photoItem.photo_75);
        } catch (GetImageByUrlException e) {
            Log.e(TAG, "GetImageByUrlException on BigImage: " + e.getMessage());
            Drawable drawable = context.getResources().getDrawable(R.drawable.empty_photo);
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } catch (GetImageIOException e) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.empty_photo);
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (photoItem.photoType == PhotoType.BIG) {
            if (photoItem.photoItem != null)
                ImageServiceUtil.addBitmapToMemoryCacheBig(photoItem.photoItem.id, result);
        } else {
            //подгоняем изображение под размер экрана
            result = ImageServiceUtil.fitPreViewSize(result);
            if (photoItem.photoItem != null)
                ImageServiceUtil.addBitmapToMemoryCache(photoItem.photoItem.id, result);

            // Добавляем обработанное изображение на дисковый кэш

            if (photoItem.photoItem != null && ImageServiceUtil.getBitmapFromDiskCache(photoItem.photoItem.id + "") == null)
                ImageServiceUtil.addBitmapToCache(photoItem.photoItem.id + "", result);
        }
        imageView.setImageBitmap(result);
        photoItem.progressBar.setVisibility(View.GONE);
    }

    private Bitmap download_Image(String url) throws GetImageByUrlException, GetImageIOException {
        //---------------------------------------------------
        if (url == null) throw new GetImageByUrlException("EmptyUrl");
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("Hub", "IOException getting the image from server : " + e.getMessage().toString() + "\nURL " + url);
            throw new GetImageIOException(e.getMessage());

        } catch (Exception e) {
            Log.e("Hub", "Exception getting the image from server : " + e.getMessage().toString() + "\nURL " + url);
        }
        return bm;
        //---------------------------------------------------
    }
}
