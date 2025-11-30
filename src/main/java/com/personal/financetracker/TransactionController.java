package com.personal.financetracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private UserRepo userRepo;

    // 🟢 List all transactions
    @GetMapping
    public String listTransactions(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        List<User> users = userRepo.findAll();
        model.addAttribute("users", users);

        List<Transaction> transactions = transactionRepo.findAll();

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> "income".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        model.addAttribute("transactions", transactions);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("balance", balance);

        return "transactions";
    }

    // 🟡 Add new transaction
    @PostMapping("/add")
    public String addTransaction(
            @RequestParam("userId") Long userId,
            @RequestParam("category") String category,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("type") String type,
            @RequestParam("date") String date
    ) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + userId));

        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setCategory(category);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setDate(LocalDate.parse(date));

        transactionRepo.save(tx);

        return "redirect:/transactions";
    }

    // 🗑 Delete transaction
    @GetMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable("id") int id) {
        transactionRepo.deleteById((long) id);
        return "redirect:/transactions";
    }

    // ✏️ Edit form (safe version)
    @GetMapping("/edit/{id}")
    public String editTransaction(@PathVariable ("id") int id, Model model) {
        try {
            Transaction tx = transactionRepo.findById((long) id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid transaction Id: " + id));

            List<User> users = userRepo.findAll();
            model.addAttribute("transaction", tx);
            model.addAttribute("users", users);

            return "edit_transaction"; // name must match file name below
        } catch (Exception e) {
            e.printStackTrace(); // log error in console
            model.addAttribute("errorMessage", e.getMessage());
            return "error"; // fallback page
        }
    }

    // 🟢 Update transaction
    @PostMapping("/update/{id}")
    public String updateTransaction(
            @PathVariable ("id") int id,
            @RequestParam("userId") Long userId,
            @RequestParam("category") String category,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("type") String type,
            @RequestParam("date") String date
    ) {
        Transaction tx = transactionRepo.findById((long) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid transaction Id: " + id));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + userId));

        tx.setUser(user);
        tx.setCategory(category);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setDate(LocalDate.parse(date));

        transactionRepo.save(tx);
        return "redirect:/transactions";
    }
}