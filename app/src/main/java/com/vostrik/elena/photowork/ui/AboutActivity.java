package com.vostrik.elena.photowork.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.vostrik.elena.photowork.R;

/**
 * Created by Elena on 27.04.2018.
 */

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TextView textView = (TextView)findViewById(R.id.about_textView);
        textView.setText("Vk photo is an application to get photos from vk.com where user is attached");
    }
}
