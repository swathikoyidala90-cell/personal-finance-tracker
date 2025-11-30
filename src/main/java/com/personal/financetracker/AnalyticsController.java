package com.personal.financetracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AnalyticsController {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private UserRepo userRepo;  // ✅ Corrected

    @GetMapping("/analytics/{id}") // ✅ URL pattern
    public String analyticsPage(@PathVariable("id") Long userId, // ✅ PathVariable fixed
                                Model model,
                                HttpSession session) {

        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + userId));

        List<Transaction> transactions = transactionRepo.findByUser(user);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> "income".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        Map<String, BigDecimal> expenseByCategory = transactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        Map<String, BigDecimal> incomeByDate = transactions.stream()
                .filter(t -> "income".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        t -> t.getDate().toString(),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        Map<String, BigDecimal> expenseByDate = transactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        t -> t.getDate().toString(),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        model.addAttribute("user", user);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("balance", balance);
        model.addAttribute("expenseByCategory", expenseByCategory);
        model.addAttribute("incomeByDate", incomeByDate);
        model.addAttribute("expenseByDate", expenseByDate);

        return "analytics";
    }
}