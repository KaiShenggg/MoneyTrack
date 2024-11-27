package com.ksy.moneytrack;

public class Category {

    private final String title;
    private final int iconResId;
    private final int bgResId;

    public Category(String title, int iconResId, int bgResId) {
        this.title = title;
        this.iconResId = iconResId;
        this.bgResId = bgResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getBgResId() {
        return bgResId;
    }

}
