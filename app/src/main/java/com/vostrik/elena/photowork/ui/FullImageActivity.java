package com.vostrik.elena.photowork.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.vostrik.elena.photowork.Application;
import com.vostrik.elena.photowork.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Аквити для отображения фрагментов с увеличенными фотографиями
 * Created by Elena on 21.04.2018.
 */

public class FullImageActivity extends FragmentActivity {
    private static final String TAG = "FullImageActivity";
    private ShareActionProvider shareAction;

    public void setPosition(int position) {
        Log.d(TAG, "after set Activity " + position);
        this.position = position;
    }

    private int position;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image_activity);
        Intent intent = getIntent();
        if (position == 0)
            position = intent.getIntExtra("position", 0);
        Log.d(TAG, "Position " + position);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        final FullImageFragment fullImageFragment = FullImageFragment.newInstance(position);
        ft.add(android.R.id.content, fullImageFragment, TAG);
        ft.commitAllowingStateLoss();//вместо commit(), чтобы избежать java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fullimage, menu);

        // Get the menu item.
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share: {
                onShareItem(Application.bigPhotoMemoryCache.get(Application.photoAdapterPhotos.get(position).id));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Right SDK version " + Build.VERSION.SDK_INT);
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                Log.d(TAG, "requestPermission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do your work
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    //-----------------------------------
    // Can be triggered by a view event such as a button press
    public void onShareItem(Bitmap bitmap) {
        // Get access to the URI for the bitmap
        Uri bmpUri = getLocalBitmapUri(bitmap);
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } else {
            // ...sharing failed, handle error
        }
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
    public Uri getLocalBitmapUri(Bitmap bmp) {
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    // Method when launching drawable within Glide
    public Uri getBitmapFromDrawable(Bitmap bmp) {

        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

            // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            bmpUri = FileProvider.getUriForFile(FullImageActivity.this, "com.codepath.fileprovider", file);  // use this version for API >= 24

            // **Note:** For API < 24, you may use bmpUri = Uri.fromFile(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

}