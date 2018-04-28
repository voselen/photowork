package com.vostrik.elena.photowork.test;

import com.vostrik.elena.photowork.model.VkPhotoItem;
import com.vostrik.elena.photowork.util.PhotoContentProvider;
import com.vostrik.elena.photowork.util.StreamReaderWriter;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by Elena on 28.04.2018.
 */
public class PhotoContentProviderTest {
    PhotoContentProvider photoContentProvider;

    @Before
    public void setUp() throws Exception {
        final File initialFile = new File("E:\\Temp\\photoVkItems.json");
        StreamReaderWriter streamReaderWriter = new StreamReaderWriter() {
            @Override
            public InputStream getInputStream() throws FileNotFoundException {
                return new FileInputStream(initialFile);
            }

            @Override
            public OutputStream getOutputStream() throws FileNotFoundException {
                return new FileOutputStream(initialFile);
            }
        };
        photoContentProvider = new PhotoContentProvider(streamReaderWriter);
        List<VkPhotoItem> vkPhotoItemList = new ArrayList<>();
        vkPhotoItemList.add(new VkPhotoItem(0, "photo_75", "photo_130", "photo_1280", 111, 10, new Date(),  1));
        vkPhotoItemList.add(new VkPhotoItem(1, "photo_75", "photo_130", "photo_1280", 111, 10, new Date(),  3));
        vkPhotoItemList.add(new VkPhotoItem(2, "photo_75", "photo_130", "photo_1280", 111, 10, new Date(),  4));
        vkPhotoItemList.add(new VkPhotoItem(3, "photo_75", "photo_130", "photo_1280", 111, 10, new Date(),  5));
        vkPhotoItemList.add(new VkPhotoItem(4, "photo_75", "photo_130", "photo_1280", 111, 10, new Date(),  2));
        photoContentProvider.saveJSONtoFile(vkPhotoItemList);

    }

    @Test
    public void getPhotoItemList() throws Exception {
        assertNotNull(photoContentProvider.loadJSONFromAsset());
        List<VkPhotoItem> vkPhotoItemList = photoContentProvider.getPhotoItemList();
        assertEquals(vkPhotoItemList.size(), 5);
    }

}