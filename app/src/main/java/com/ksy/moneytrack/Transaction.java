package com.ksy.moneytrack;

public class Transaction {

    private final int id;
    private final String type;
    private final String category;
    private final double amount;
    private final String memo;
    private final String date;
    private final String created_at;

    public Transaction(int id, String type, String category, double amount, String memo, String date, String created_at) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.memo = memo;
        this.date = date;
        this.created_at = created_at;
    }

    public int getId() {
        return id;
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
