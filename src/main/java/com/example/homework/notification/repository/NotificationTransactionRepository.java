package com.example.homework.notification.repository;

import com.example.homework.notification.model.NotificationTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface  NotificationTransactionRepository extends JpaRepository<NotificationTransaction, Long> {

    @Query( "SELECT a FROM NotificationTransaction a WHERE a.id = :id")
    Optional<NotificationTransaction> findById(@Param("id") long id);

    @Query( "SELECT count(a) FROM NotificationTransaction a WHERE a.target = :target and a.createdAt >= DATEADD(MINUTE, -1, CURRENT_TIMESTAMP)")
    Integer findTargetRecentCount(@Param("target") String target);
}
