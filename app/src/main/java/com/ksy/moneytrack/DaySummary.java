package com.ksy.moneytrack;

public class DaySummary {
    private int dayOfMonth;
    private double income;
    private double expenses;

    public DaySummary(int dayOfMonth, double income, double expenses) {
        this.dayOfMonth = dayOfMonth;
        this.income = income;
        this.expenses = expenses;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public double getIncome() {
        return income;
    }

    public double getExpenses() {
        return expenses;
    }
}
