package com.application.letschat.repository.user;

import com.application.letschat.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword%")
    public List<User> findByKeyword(@Param("keyword") String keyword);

    User findByEmail(String email);

}
