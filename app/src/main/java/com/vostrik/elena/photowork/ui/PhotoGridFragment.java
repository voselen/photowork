package com.vostrik.elena.photowork.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.vostrik.elena.photowork.Application;
import com.vostrik.elena.photowork.R;
import com.vostrik.elena.photowork.adapter.PhotoAdapter;
import com.vostrik.elena.photowork.model.VkPhotoItem;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Фрагемент, отображающий сетку с превью изображений
 * Created by Elena on 20.04.2018.
 */

public class PhotoGridFragment extends Fragment {
    private static final String TAG = "PhotoGridFragment";
    public static GridView gridView;
    public static PhotoAdapter photoAdapter;
    public static AtomicInteger nextPage = new AtomicInteger(0);
    private static int mPreviousTotal = 0;
    private static final int lastPageId = Application.photoCount / Application.PHOTO_PER_PAGE + 1;
    private static boolean isLastPage = false;
    private static ConcurrentSkipListSet<Integer> pages = new ConcurrentSkipListSet<>();

    public PhotoGridFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate nextPage " + nextPage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.gallery_fragment, container, false);
        gridView = (GridView) v.findViewById(R.id.gridView);

        //Если не загружено еще страниц, загружаем первую
        int size = Application.photoAdapterPhotos.size();
        if (size == 0) {
            setAdapter(Application.vkPhotos.subList(0, Application.PHOTO_PER_PAGE));
        } else {
            setAdapter(Application.photoAdapterPhotos);
        }

        Log.d(TAG, "after");
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
 //       RefWatcher refWatcher = Application.getRefWatcher(getActivity());
 //       refWatcher.watch(this);
    }

    //Привязка Адаптера к GridView
    private void setAdapter(List<VkPhotoItem> list) {

        photoAdapter = new PhotoAdapter(getActivity(), R.layout.photo_image_view, list);
        gridView.setAdapter(photoAdapter);
        photoAdapter.notifyDataSetChanged();
        gridView.invalidateViews();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {
                Intent i = new Intent(getActivity(), FullImageActivity.class);
                i.putExtra("position", position);
                startActivity(i);
            }
        });
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                int currentFirstVisPos = view.getFirstVisiblePosition();

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if ((view.getLastVisiblePosition() + 1) == totalItemCount
                        && totalItemCount > mPreviousTotal
                        ) {
                    mPreviousTotal = totalItemCount;
                    nextPage.incrementAndGet();

                    if (nextPage.get() > lastPageId) {
                        isLastPage = true;
                    }
                    if (pages.add(nextPage.get()) && !isLastPage && totalItemCount < Application.photoCount) {
                        nextPage();
                    }
                } else {
                }
            }

        });
    }


    public static void nextPage() {
        List<VkPhotoItem> sublist = Application.vkPhotos.subList(0, (nextPage.get() + 1) * Application.PHOTO_PER_PAGE);
        Application.photoAdapterPhotos = new CopyOnWriteArrayList<>(sublist);//Collections.synchronizedList(sublist);
        photoAdapter.notifyDataSetChanged();
        gridView.invalidateViews();
    }
}
