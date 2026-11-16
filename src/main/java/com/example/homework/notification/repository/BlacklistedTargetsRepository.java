package com.example.homework.notification.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.homework.notification.model.BlacklistedTargets;

import java.util.List;

public interface BlacklistedTargetsRepository extends JpaRepository<BlacklistedTargets, Long>{

    @Query("SELECT n FROM BlacklistedTargets n WHERE INSTR(LOWER(:target), LOWER(n.target)) > 0")
    List<BlacklistedTargets> isTargetBlacklisted(@Param("target") String target);

}
