package com.ksy.moneytrack;

public class DaySummary {
    private final String dayOfMonth;
    private final double income;
    private final double expenses;

    public DaySummary(String dayOfMonth, double income, double expenses) {
        this.dayOfMonth = dayOfMonth;
        this.income = income;
        this.expenses = expenses;
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public double getIncome() {
        return income;
    }

    public double getExpenses() {
        return expenses;
    }
}
