package com.vostrik.elena.photowork.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.vostrik.elena.photowork.Application;
import com.vostrik.elena.photowork.R;
import com.vostrik.elena.photowork.model.PhotoType;
import com.vostrik.elena.photowork.model.VkPhotoItem;
import com.vostrik.elena.photowork.ui.MainActivity;
import com.vostrik.elena.photowork.util.ImageServiceUtil;

import java.util.List;

/**
 * Created by Elena on 14.04.2018.
 */

public class PhotoAdapter extends ArrayAdapter<VkPhotoItem> {
    private static final String TAG = "PhotoAdapter";
    private LayoutInflater inflater;
    private int layout;
    Context context;

    public PhotoAdapter(Context context, int resource, List<VkPhotoItem> photos) {
        super(context, resource, photos);
        this.context = context;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
       // Application.vkPhotos = photos;
        Application.photoAdapterPhotos  = photos;
        //Log.d(TAG, "Application.photos " + Application.vkPhotos.size());
        Log.d(TAG, "Application.photoAdapterPhotos " + Application.photoAdapterPhotos.size());
    }

    public int getCount() {
        return Application.photoAdapterPhotos == null ? 0 : Application.photoAdapterPhotos.size();
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            viewHolder.imageView.setLayoutParams(new RelativeLayout.LayoutParams(MainActivity.SIZE_W, MainActivity.SIZE_H));
            viewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.imageView.setPadding(1, 1, 1, 0);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imageView.setMinimumWidth(MainActivity.SIZE_W);
        viewHolder.imageView.setMinimumHeight(MainActivity.SIZE_W);
//        Log.d(TAG, "getView position " + position);
        viewHolder.imageView.setImageBitmap(null);
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        ImageServiceUtil.getBitmap(position, viewHolder.imageView, viewHolder.progressBar, PhotoType.PREVIEW, context);

        return convertView;
    }

    private class ViewHolder {
        final ImageView imageView;
        final ProgressBar progressBar;

        ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.photoImageView);
            imageView.setImageResource(android.R.color.transparent);
            progressBar = (ProgressBar) view.findViewById(R.id.photoProgressBar);
        }
    }
}
