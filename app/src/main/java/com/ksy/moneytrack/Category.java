package com.ksy.moneytrack;

public class Category {

    private String title;
    private int iconResId;
    private int bgResId;
    private String type;

    public Category(String title, int iconResId, int bgResId, String type) {
        this.title = title;
        this.iconResId = iconResId;
        this.bgResId = bgResId;
        this.type = type;
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

    public String getType() {
        return type;
    }

}
