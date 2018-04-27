package com.vostrik.elena.photowork.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.vostrik.elena.photowork.Application;
import com.vostrik.elena.photowork.asynctask.DownloadImageTask;
import com.vostrik.elena.photowork.model.PhotoType;
import com.vostrik.elena.photowork.model.VkLoadPhotoItem;
import com.vostrik.elena.photowork.model.VkPhotoItem;
import com.vostrik.elena.photowork.ui.MainActivity;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.vostrik.elena.photowork.Application.mDiskCacheStarting;
import static com.vostrik.elena.photowork.Application.mDiskLruCache;

/**
 * Created by Elena on 15.04.2018.
 * Используется для получения изображений из Кэша и помещения их в кэш
 */
public class ImageServiceUtil {

    private static final Object mDiskCacheLock = new Object();
    private static final int DISK_CACHE_INDEX = 0;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

    private static final String TAG = "ImageServiceUtil";

    /**
     * @param position    позиция в списке фотографий пользователя
     * @param imageView   элемент отображающий фото
     * @param progressBar прогресс бар, который крутится, пока идет загрузка изображения
     * @param photoType   тип изображения, которое нужно загрузить - превью или большое
     * @return изображение
     */
    public static Bitmap getBitmap(int position, ImageView imageView, ProgressBar progressBar, PhotoType photoType, Context context) {
        VkPhotoItem vkPhotoItem = Application.photoAdapterPhotos.get(position);
        VkLoadPhotoItem vkLoadPhotoItem;
        Bitmap bitmap;
        if (photoType == PhotoType.PREVIEW) {
            vkLoadPhotoItem = new VkLoadPhotoItem(PhotoType.PREVIEW, vkPhotoItem, progressBar);
            bitmap = getBitmapFromMemCache(vkPhotoItem.id);
        } else {
            vkLoadPhotoItem = new VkLoadPhotoItem(PhotoType.BIG, vkPhotoItem, progressBar);
            bitmap = getBitmapFromMemCacheBig(vkPhotoItem.id);
        }

        if (bitmap == null) {
            imageView.setTag(vkLoadPhotoItem);
            DownloadImageTask downloadImageTask = new DownloadImageTask(context);
            downloadImageTask.execute(imageView);
        } else {
            imageView.setImageBitmap(bitmap);
            progressBar.setVisibility(View.INVISIBLE);
        }
        return bitmap;
    }

    /**
     * Добавление к кэш большой картинки
     *
     * @param key
     * @param bitmap
     */
    public static void addBitmapToMemoryCacheBig(Long key, Bitmap bitmap) {
        if (getBitmapFromMemCacheBig(key) == null) {
            if (key != null)
                Application.bigPhotoMemoryCache.put(key, bitmap);
        }
    }

    /**
     * Получение из кэша больших картинок изображения по ключу
     *
     * @param key
     * @return
     */
    public static Bitmap getBitmapFromMemCacheBig(Long key) {
        return Application.bigPhotoMemoryCache.get(key);

    }

    public static void addBitmapToMemoryCache(Long key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            if (key != null)
                Application.preViewPhotoMemoryCache.put(key, bitmap);
        }
        // А также на дисковый кэш
    }

    /**
     * from https://developer.android.com/samples/DisplayingBitmaps/project.html
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        Log.d(TAG, "cacheKey " + cacheKey);
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static Bitmap getBitmapFromDiskCache(String data) {
        //BEGIN_INCLUDE(get_bitmap_from_disk_cache)
        final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();
                            // Decode bitmap, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            bitmap = decodeSampledBitmapFromDescriptor(
                                    fd, Integer.MAX_VALUE, Integer.MAX_VALUE);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
            return bitmap;
        }
        //END_INCLUDE(get_bitmap_from_disk_cache)
    }

    /**
     * from https://developer.android.com/samples/DisplayingBitmaps/project.html
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth       The requested width of the resulting bitmap
     * @param reqHeight      The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     * that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    public static void addBitmapToCache(String key, Bitmap bitmap) {
        if (key != null) {
            synchronized (mDiskCacheLock) {
                // Add to disk cache
                if (mDiskLruCache != null) {
                    key = hashKeyForDisk(key);
                    OutputStream out = null;
                    try {
                        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                        if (snapshot == null) {
                            final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                            if (editor != null) {
                                out = editor.newOutputStream(DISK_CACHE_INDEX);
                                bitmap.compress(
                                        DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, out);
                                editor.commit();
                                out.close();
                            }
                        } else {
                            snapshot.getInputStream(DISK_CACHE_INDEX).close();
                        }
                    } catch (final IOException e) {
                        Log.e(TAG, "addBitmapToCache - " + e);
                    } catch (Exception e) {
                        Log.e(TAG, "addBitmapToCache - " + e);
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
    }

    public static Bitmap getBitmapFromMemCache(Long key) {
        return Application.preViewPhotoMemoryCache.get(key);
    }

    public static Bitmap fitPreViewSize(Bitmap bitmap) {
        return resizeImage(bitmap);
    }

    private static Bitmap decodeImage(Bitmap bitmap) {
       // Log.d(TAG, "decodeImage image W " + bitmap.getWidth() + " H " + bitmap.getHeight());
        //bitmap = Bitmap.createScaledBitmap(bitmap, Application.SIZE_W, Application.SIZE_H, true);
        int width = MainActivity.SIZE_W;
        width = width > bitmap.getWidth() ? bitmap.getWidth() : width;
        int height = MainActivity.SIZE_H;
        height = height > bitmap.getHeight() ? bitmap.getHeight() : height;
        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    private static Bitmap resizeImage(Bitmap bitmap) {
        int width = MainActivity.SIZE_W;
        int height = MainActivity.SIZE_H;
        float sizeW = 0;
        //сначала пропорционально растянем картинку до нужнного размера
        if (width > bitmap.getWidth() && height < bitmap.getHeight())
        //нужно подрастянуть ширину
        {
            sizeW = (float) width / bitmap.getWidth();
          //  Log.d(TAG, "size W " + sizeW);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * sizeW), (int) (bitmap.getHeight() * sizeW), true);
        } else if (width < bitmap.getWidth() && height > bitmap.getHeight()) {
            sizeW = (float) height / bitmap.getHeight();
          //  Log.d(TAG, "size H " + sizeW);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * sizeW), (int) (bitmap.getHeight() * sizeW), true);
        } else if (width > bitmap.getWidth() && height > bitmap.getHeight()) {
            sizeW = (float) width / bitmap.getWidth();
          //  Log.d(TAG, "size W " + sizeW);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * sizeW), (int) (bitmap.getHeight() * sizeW), true);
            if (height > bitmap.getHeight()) {
                sizeW = (float) height / bitmap.getHeight();
              //  Log.d(TAG, "size H " + sizeW);
                bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * sizeW), (int) (bitmap.getHeight() * sizeW), true);
            }
        }

        //затем обрезаем картинку до нужного размера
        return decodeImage(bitmap);
    }


    /**
     * from https://developer.android.com/samples/DisplayingBitmaps/project.html
     * Calculate an inSampleSize for use in a {@link android.graphics.BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link android.graphics.BitmapFactory}. This implementation calculates
     * the closest inSampleSize that is a power of 2 and will result in the final decoded bitmap
     * having a width and height equal to or larger than the requested width and height.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (calculate_sample_size)
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / inSampleSize;

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
        // END_INCLUDE (calculate_sample_size)
    }
}
