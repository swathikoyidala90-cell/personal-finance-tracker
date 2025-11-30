package com.personal.financetracker;

import com.personal.financetracker.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface UserRepo extends JpaRepository<User, Long> {

    User findByUsername(String username);

    Object findByEmail(String email);
    
}
