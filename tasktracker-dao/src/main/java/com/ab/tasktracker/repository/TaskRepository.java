package com.ab.tasktracker.repository;

import com.ab.tasktracker.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = "select * from tasktracker_service_task_tx_tbl where task_id =?1 AND created_user_id=?2", nativeQuery = true)
    Task findByIdAndUserId(Long taskId, Long userId);
    @EntityGraph(value = "Task.user", type = EntityGraph.EntityGraphType.LOAD)
    List<Task> findAll();
}
