package com.ab.tasktracker.repository;

import com.ab.tasktracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.lang.annotation.Native;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Boolean existsByEmail(String email);
    @Query(value = "SELECT * FROM tasktracker_service_user_ms_tbl WHERE email = ?1 and is_deleted=false",nativeQuery = true)
    User findActiveUserByEmail(@Param("email")String email);

}
