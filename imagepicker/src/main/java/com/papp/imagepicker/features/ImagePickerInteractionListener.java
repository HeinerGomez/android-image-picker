package com.papp.imagepicker.features;

import android.content.Intent;

import com.papp.imagepicker.model.Image;

import java.util.List;

public interface ImagePickerInteractionListener {
    void setTitle(String title);
    void cancel();
    // Get this callback by calling an ImagePickerFragment's finishPickImages() method. It
    // removes Images whose files no longer exist.
    void finishPickImages(Intent result);

    /**
     * Called when the user selects or deselects sn image. Also called in onCreateView.
     * May include Images whose files no longer exist.
     */
    void selectionChanged(List<Image> imageList);
}
