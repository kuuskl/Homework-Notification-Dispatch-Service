package com.example.homework.notification.repository;

import com.example.homework.notification.model.NotificationTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface  NotificationTransactionRepository extends JpaRepository<NotificationTransaction, Long> {

    @Query( "SELECT count(a) FROM NotificationTransaction a WHERE a.target = :target and a.createdAt >= DATEADD(MINUTE, -1, CURRENT_TIMESTAMP)")
    Integer findTargetRecentCount(@Param("target") String target);

    @Query( "SELECT a FROM NotificationTransaction a ORDER BY a.id DESC LIMIT :limit")
    List<NotificationTransaction> getHistory(@Param("limit") int limit);

    @Query( "SELECT count(a) FROM NotificationTransaction a")
    Integer getDispatchCount();

}
