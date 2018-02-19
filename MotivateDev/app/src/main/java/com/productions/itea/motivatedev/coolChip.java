package com.productions.itea.motivatedev;


import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.tylersuehr.chips.data.Chip;

public class coolChip extends Chip {
    private String coolName;
    private Uri coolPic;

    public coolChip(String coolName, Uri coolPic) {
        this.coolName = coolName;
        this.coolPic = coolPic;
    }

    @Override
    public Object getId() {
        return null;
    }

    @Override
    public String getTitle() {
        return coolName;
    }

    public void setTitle(String coolName) {
        this.coolName = coolName;
    }

    public void setAvatar(Uri coolPic) {
        this.coolPic = coolPic;
    }


    @Nullable
    @Override
    public String getSubtitle() {
        return null;
    }

    @Override
    public Uri getAvatarUri() {
        return coolPic;
    }


    @Nullable
    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }


    // ...other chip methods that are required to implement
}
