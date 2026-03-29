package com.personal.financetracker;
import com.personal.financetracker.*;
import java.util.ArrayList;

public class FinanceService {

    private ArrayList<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public void showAllTransactions() {
        for (Transaction t : transactions) {
            System.out.println(
                t.getType() + " | " +
                t.getAmount() + " | " +
                t.getCategory() + " | " +
                t.getDate()
            );
        }
    }

    public double calculateBalance() {
        double balance = 0;

        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("income")) {
                balance += t.getAmount();
            } else {
                balance -= t.getAmount();
            }
        }

        return balance;
    }

    public void showByCategory(String category) {
        for (Transaction t : transactions) {
            if (t.getCategory().equalsIgnoreCase(category)) {
                System.out.println(
                    t.getType() + " | " +
                    t.getAmount() + " | " +
                    t.getCategory()
                );
            }
        }
    }
}
