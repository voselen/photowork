package com.vostrik.elena.photowork.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vostrik.elena.photowork.Application;
import com.vostrik.elena.photowork.R;
import com.vostrik.elena.photowork.model.PhotoType;
import com.vostrik.elena.photowork.model.VkPhotoItem;
import com.vostrik.elena.photowork.util.ImageServiceUtil;
import com.vostrik.elena.photowork.util.OnSwipeTouchListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Фрагмент, отображающий увеличенное изображение
 * Created by Elena on 21.04.2018.
 */

public class FullImageFragment extends Fragment {
    private static final String VK_PHOTO_IMAGE = VkPhotoItem.class.getName();
    private static final String VK_PHOTO_IMAGE_POSITION = "position";
    private static final String TAG = "FullImageFragment";
    static VkPhotoItem photoItem;
    static int position;
    ImageView imageView;
    ProgressBar progressBar;
    TextView textView;
    Context context;

    //для увеличения размера изображения
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }

    public static FullImageFragment newInstance(int position) {
        final FullImageFragment f = new FullImageFragment();
        final Bundle args = new Bundle();
        photoItem = Application.vkPhotos.get(position);
        args.putParcelable(VK_PHOTO_IMAGE, photoItem);
        args.putInt(VK_PHOTO_IMAGE_POSITION, position);
        Log.d(TAG, "put Int " +position);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoItem = getArguments() != null ? (VkPhotoItem) getArguments().getParcelable(VK_PHOTO_IMAGE) : null;
        position = getArguments() != null ? getArguments().getInt(VK_PHOTO_IMAGE_POSITION) : null;
        Log.d(TAG, "onCreate position " + position);
        context = this.getActivity();
        FullImageActivity activity = (FullImageActivity) getActivity();
        activity.setPosition(position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.full_image_fragment, container, false);

        mScaleGestureDetector = new ScaleGestureDetector(v.getContext(), new ScaleListener());
        Log.d(TAG, "onCreateView position " + position);
        imageView = (ImageView) v.findViewById(R.id.full_image_view);
        progressBar = (ProgressBar) v.findViewById(R.id.bigPhotoProgressBar);
        textView = (TextView) v.findViewById(R.id.full_image_caption);
        textView.setText((position + 1) + " из " + Application.photoCount);
        Button button = (Button)v.findViewById(R.id.back_button);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        // listeners of our two buttons
        View.OnClickListener handler = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.buttonShareImage:
                        shareImage();
                        break;
                }
            }
        };
        v.findViewById(R.id.buttonShareImage).setOnClickListener(handler);

        v.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mScaleGestureDetector.onTouchEvent(motionEvent);
                return super.onTouch(view, motionEvent);
            }

            @Override
            public void onSwipeDown() {

            }

            @Override
            public void onSwipeLeft() {

                if (position + 1 < Application.photoAdapterPhotos .size()) {
                    FragmentTransaction fragManager = getActivity().getSupportFragmentManager().beginTransaction();
                    fragManager.replace(android.R.id.content, FullImageFragment.newInstance(position + 1));
                    fragManager.commitAllowingStateLoss();
                } else if (position + 1 >= Application.photoAdapterPhotos.size() &&
                        position + 1 < Application.photoCount) {
                    //запрашиваем следующую страницу с картинками
                    PhotoGridFragment.nextPage.incrementAndGet();
                    PhotoGridFragment.nextPage();

                    FragmentTransaction fragManager = getActivity().getSupportFragmentManager().beginTransaction();
                    fragManager.replace(android.R.id.content, FullImageFragment.newInstance(position + 1));
                    fragManager.commitAllowingStateLoss();

                }
            }

            @Override
            public void onSwipeUp() {
            }

            @Override
            public void onSwipeRight() {
                if (position - 1 >= 0) {
                    FragmentTransaction fragManager = getActivity().getSupportFragmentManager().beginTransaction();
                    fragManager.replace(android.R.id.content, FullImageFragment.newInstance(position - 1));
                    fragManager.commitAllowingStateLoss();
                }
            }
        });
        return v;
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
    public Uri getLocalBitmapUri(Bitmap bmp) {
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
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
    private void shareImage() {
        Bitmap btm=((BitmapDrawable)imageView.getDrawable()).getBitmap();
        if(btm!=null) {
            Toast.makeText(getActivity(), "Подготовка изображения", Toast.LENGTH_SHORT);
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(btm));
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }
        else
            Toast.makeText(getActivity(), "Дождитесь окончания загрузки изображения", Toast.LENGTH_SHORT);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
       if (FullImageActivity.class.isInstance(getActivity())) {
            ImageServiceUtil.getBitmap(position, imageView, progressBar, PhotoType.BIG, context);
        }

        // Pass clicks on the ImageView to the parent activity to handle
        if (View.OnClickListener.class.isInstance(getActivity())) {
            imageView.setOnClickListener((View.OnClickListener) getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageView != null) {
            imageView.setImageDrawable(null);
        }
    }

}


