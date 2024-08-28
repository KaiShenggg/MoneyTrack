package com.ksy.moneytrack;

import java.util.ArrayList;
import java.util.List;

public class Categories {
    private static List<Category> categoryList = new ArrayList<>();

    static {
        // Food
        categoryList.add(new Category("Food", R.drawable.ic_food, R.drawable.circle_elm_bg, "Expenses"));
        categoryList.add(new Category("Breakfast", R.drawable.ic_breakfast, R.drawable.circle_coral_red_bg, "Expenses"));
        categoryList.add(new Category("Lunch", R.drawable.ic_lunch, R.drawable.circle_cornflower_bg, "Expenses"));
        categoryList.add(new Category("Dinner", R.drawable.ic_dinner, R.drawable.circle_carrot_orange_bg, "Expenses"));
        categoryList.add(new Category("Fruit", R.drawable.ic_fruit, R.drawable.circle_apple_bg, "Expenses"));
        categoryList.add(new Category("Dessert", R.drawable.ic_dessert, R.drawable.circle_copper_bg, "Expenses"));

        // Transportation
        categoryList.add(new Category("Transportation", R.drawable.ic_transportation, R.drawable.circle_purple_bg, "Expenses"));
        categoryList.add(new Category("Car", R.drawable.ic_car, R.drawable.circle_copper_bg, "Expenses"));
        categoryList.add(new Category("Bus", R.drawable.ic_bus, R.drawable.circle_apple_bg, "Expenses"));
        categoryList.add(new Category("MRT", R.drawable.ic_mrt, R.drawable.circle_elm_bg, "Expenses"));
        categoryList.add(new Category("Parking", R.drawable.ic_parking, R.drawable.circle_carrot_orange_bg, "Expenses"));
        categoryList.add(new Category("Toll", R.drawable.ic_toll, R.drawable.circle_coral_red_bg, "Expenses"));

        // Shopping
        categoryList.add(new Category("Shopping", R.drawable.ic_shopping, R.drawable.circle_copper_bg, "Expenses"));
        categoryList.add(new Category("Clothing", R.drawable.ic_clothing, R.drawable.circle_elm_bg, "Expenses"));
        categoryList.add(new Category("Electronics", R.drawable.ic_electronics, R.drawable.circle_purple_bg, "Expenses"));

        // Entertainment
        categoryList.add(new Category("Entertainment", R.drawable.ic_entertainment, R.drawable.circle_cornflower_bg, "Expenses"));
        categoryList.add(new Category("Sport", R.drawable.ic_sport, R.drawable.circle_carrot_orange_bg, "Expenses"));
        categoryList.add(new Category("Movie", R.drawable.ic_movie, R.drawable.circle_apple_bg, "Expenses"));

        // Health
        categoryList.add(new Category("Health", R.drawable.ic_health, R.drawable.circle_coral_red_bg, "Expenses"));
        categoryList.add(new Category("Haircut", R.drawable.ic_haircut, R.drawable.circle_elm_bg, "Expenses"));

        // Others Expenses
        categoryList.add(new Category("Gift", R.drawable.ic_gift, R.drawable.circle_carrot_orange_bg, "Expenses"));
        categoryList.add(new Category("Rent", R.drawable.ic_rent, R.drawable.circle_copper_bg, "Expenses"));
        categoryList.add(new Category("Bills", R.drawable.ic_bills, R.drawable.circle_purple_bg, "Expenses"));
        categoryList.add(new Category("Tax", R.drawable.ic_tax, R.drawable.circle_apple_bg, "Expenses"));
        categoryList.add(new Category("Phone Charges", R.drawable.ic_phone_charges, R.drawable.circle_cornflower_bg, "Expenses"));
        categoryList.add(new Category("Insurance", R.drawable.ic_insurance, R.drawable.circle_elm_bg, "Expenses"));
        categoryList.add(new Category("Parent", R.drawable.ic_parent, R.drawable.circle_purple_bg, "Expenses"));
        categoryList.add(new Category("Pet", R.drawable.ic_pet, R.drawable.circle_coral_red_bg, "Expenses"));
        categoryList.add(new Category("Donate", R.drawable.ic_donate, R.drawable.circle_carrot_orange_bg, "Expenses"));
        categoryList.add(new Category("Others", R.drawable.ic_others, R.drawable.circle_cornflower_bg, "Expenses"));

        // Income
        categoryList.add(new Category("Salary", R.drawable.ic_salary, R.drawable.circle_cornflower_bg, "Income"));
        categoryList.add(new Category("Awards", R.drawable.ic_awards, R.drawable.circle_purple_bg, "Income"));
        categoryList.add(new Category("Dividends", R.drawable.ic_dividends, R.drawable.circle_elm_bg, "Income"));
        categoryList.add(new Category("Pocket Money", R.drawable.ic_pocket_money, R.drawable.circle_coral_red_bg, "Income"));
        categoryList.add(new Category("Ang Pao", R.drawable.ic_ang_pao, R.drawable.circle_apple_bg, "Income"));
        categoryList.add(new Category("Balance", R.drawable.ic_balance, R.drawable.circle_carrot_orange_bg, "Income"));
        categoryList.add(new Category("Others", R.drawable.ic_others, R.drawable.circle_copper_bg, "Income"));
    }

    public static Category getCategoryByTitle(String title) {
        for (Category category : categoryList) {
            if (category.getTitle().equals(title)) {
                return category;
            }
        }
        return null;
    }

    public static List<Category> getCategoriesByType(String type) {
        List<Category> filteredCategories = new ArrayList<>();
        for (Category category : categoryList) {
            if (category.getType().equals(type)) {
                filteredCategories.add(category);
            }
        }
        return filteredCategories;
    }
}
