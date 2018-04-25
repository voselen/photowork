package com.vostrik.elena.photowork.model;

import android.widget.ProgressBar;

/**
 * Created by Elena on 18.04.2018.
 */

public class VkLoadPhotoItem {
    public PhotoType photoType;
    public VkPhotoItem photoItem;
    public ProgressBar progressBar;

    public VkLoadPhotoItem(PhotoType photoType, VkPhotoItem photoItem, ProgressBar progressBar) {
        this.photoType = photoType;
        this.photoItem = photoItem;
        this.progressBar = progressBar;
    }
}
