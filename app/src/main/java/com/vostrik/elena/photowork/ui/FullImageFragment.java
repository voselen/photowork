package com.vostrik.elena.photowork.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vostrik.elena.photowork.Application;
import com.vostrik.elena.photowork.R;
import com.vostrik.elena.photowork.model.PhotoType;
import com.vostrik.elena.photowork.model.VkPhotoItem;
import com.vostrik.elena.photowork.util.ImageServiceUtil;
import com.vostrik.elena.photowork.util.OnSwipeTouchListener;

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

        v.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
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
                //Toast.makeText(getActivity(), "Up", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeRight() {
                //Toast.makeText(getActivity(), "Right", Toast.LENGTH_SHORT).show();
                if (position - 1 >= 0) {
                    FragmentTransaction fragManager = getActivity().getSupportFragmentManager().beginTransaction();
                    fragManager.replace(android.R.id.content, FullImageFragment.newInstance(position - 1));
                    fragManager.commitAllowingStateLoss();
                }
            }
        });
        return v;
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
//        RefWatcher refWatcher = Application.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

}


