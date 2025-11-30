 package com.personal.financetracker;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/home")
    public String dashboard(HttpSession session, Model model) {

        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));

        List<Transaction> transactions = transactionRepo.findByUser(user);

        // Totals
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> "income".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Recent transactions (last 5)
        List<Transaction> recentTransactions = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Monthly trend chart data
        Map<String, BigDecimal> monthlyTotals = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            BigDecimal income = transactions.stream()
                    .filter(t -> t.getDate().getMonth() == month && "income".equalsIgnoreCase(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = transactions.stream()
                    .filter(t -> t.getDate().getMonth() == month && "expense".equalsIgnoreCase(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyTotals.put(month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    income.subtract(expense));
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("balance", balance);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("monthlyTotals", monthlyTotals);

        return "dashboard";
    }
} 
