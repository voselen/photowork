package com.vostrik.elena.photowork.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by Elena on 21.04.2018.
 */

public class FullImageActivity extends FragmentActivity {
    private static final String TAG = "FullImageActivity";
    private int position;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        final FullImageFragment fullImageFragment=FullImageFragment.newInstance(position);
        ft.add(android.R.id.content,fullImageFragment, TAG);
        ft.commitAllowingStateLoss();//вместо commit(), чтобы избежать java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    }
}