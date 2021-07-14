package com.example.uploadingfiles.repository;

import com.example.uploadingfiles.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsersRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.login = ?1")
    public User findByUsername(String username);
}
