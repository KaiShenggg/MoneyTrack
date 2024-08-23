package com.ksy.moneytrack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionUtils {

    // Method to group transactions by date
    public static Map<String, List<Transaction>> groupTransactionsByDate(List<Transaction> transactions) {
        Map<String, List<Transaction>> groupedTransactions = new LinkedHashMap<>();

        for (Transaction transaction : transactions) {
            String date = transaction.getDate();
            if (!groupedTransactions.containsKey(date)) {
                groupedTransactions.put(date, new ArrayList<>());
            }
            groupedTransactions.get(date).add(transaction);
        }

        return groupedTransactions;
    }

}
