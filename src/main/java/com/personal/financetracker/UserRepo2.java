package com.personal.financetracker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo2 extends JpaRepository<User,Integer> {
    User findByEmail(String email);
}
