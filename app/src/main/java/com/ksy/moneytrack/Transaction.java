package com.ksy.moneytrack;

public class Transaction {

    private String type;
    private String category;
    private double amount;
    private String memo;
    private String date;
    private String created_at;

    public Transaction(String type, String category, double amount, String memo, String date, String created_at) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.memo = memo;
        this.date = date;
        this.created_at = created_at;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getMemo() {
        return memo;
    }

    public String getDate() {
        return date;
    }

    public String getCreatedAt() {
        return created_at;
    }

}
