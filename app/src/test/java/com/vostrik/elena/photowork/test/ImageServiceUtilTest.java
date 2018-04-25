package com.vostrik.elena.photowork.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.vostrik.elena.photowork.util.ImageServiceUtil;

import org.junit.Test;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

/**
 * Created by Elena on 22.04.2018.
 */
public class ImageServiceUtilTest {
    @Test
    public void hashKeyForDisk() throws Exception {
        System.out.println(ImageServiceUtil.hashKeyForDisk("1"));

    }

    @Test
    public void decodeSampledBitmapFromDescriptor() throws Exception {
        File file = new File("E:\\Temp\\DSC_1245.JPG");
        FileOutputStream fos1 = new FileOutputStream(file);
        FileDescriptor fd = fos1.getFD();
        FileDescriptor fileDescriptor = fos1.getFD();
        System.out.println(file.getName());
    }

    @Test
    public void fitPreViewSize() throws Exception {
        File file = new File("E:\\Temp\\DSC_1245.JPG");
        FileOutputStream fos1 = new FileOutputStream(file);
        FileDescriptor fd = fos1.getFD();
        FileDescriptor fileDescriptor = fos1.getFD();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);
        ImageServiceUtil.fitPreViewSize(bitmap);
    }

    @Test
    public void calculateInSampleSize() throws Exception {
    }

}