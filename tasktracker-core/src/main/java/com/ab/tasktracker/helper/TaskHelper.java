package com.ab.tasktracker.helper;

import com.ab.cache_service.service.CacheService;
import com.ab.tasktracker.actuator.metric.TaskTrackerMetrics;
import com.ab.tasktracker.annotation.Log;
import com.ab.tasktracker.entity.Task;
import com.ab.tasktracker.exception.AppException;
import com.ab.tasktracker.exception.ErrorCode;
import com.ab.tasktracker.repository.TaskRepository;
import com.ab.tasktracker.service.GlobalHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.ab.tasktracker.constants.TaskTrackerConstants.CACHE_TASK_DETAILS;

/**
 * Helper class for Task Service
 */
@Component
@AllArgsConstructor
public class TaskHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskHelper.class);

    private TaskRepository taskRepository;

    private CacheService cacheService;

    private GlobalHelper globalHelper;

    private TaskTrackerMetrics taskTrackerMetrics;

    /**
     * Insert task details into DB and Cache
     *
     * @param task to get details with
     * @return Task
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @Log
    public Task insertTaskDetails(Task task) {
        Task taskEntity = null;
        taskEntity = taskRepository.save(task);
        taskTrackerMetrics.taskInsertCounterIncrement();
        Map<String, Object> map = new HashMap<>();
        map.put(taskEntity.getTaskId() + CACHE_TASK_DETAILS, taskEntity);
        cacheService.cacheOps(map, CacheService.CacheOperation.INSERT);
        return taskEntity;
    }

    /**
     * Get task details from cache/DB
     *
     * @param taskId to get details
     * @param userId to get details
     * @return Task
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @Log
    public Task getTaskDetails(Long taskId, Long userId) {
        Task taskEntity = null;
        try {
//          Get task details via cache
            Map<String, Object> map = new HashMap<>();
            map.put(taskId + CACHE_TASK_DETAILS, null);
            Task task = (Task) cacheService.cacheOps(map, CacheService.CacheOperation.FETCH).getFirst();
            if (task != null) {
                taskEntity = task;
            } else {
//              Either from DB
                taskEntity = taskRepository.findByIdAndUserId(taskId, userId);
            }
        } catch (Exception e) {
            LOGGER.error("Error while getting user details: {}", e.getMessage());
        }
        return taskEntity;
    }

    /**
     * Validates Date Time related to Insert/Update task APIS
     *
     * @param taskStartDate
     * @param taskDueDate
     * @param taskCompleteDate
     * @return
     */
    @Log
    public boolean validateTaskDateTime(ZonedDateTime taskStartDate, ZonedDateTime taskDueDate, ZonedDateTime taskCompleteDate) throws AppException {
//      First we compare Start Date and Due Date
        if (taskStartDate != null && taskDueDate != null) {
            if (globalHelper.compareDate(taskStartDate, taskDueDate))
                throw new AppException(ErrorCode.DATE_EXCEPTION, "Due date is larger than Start Date");
//          Then we compare Start Date and Completed Date
            if (taskCompleteDate != null) {
                if (!globalHelper.compareDate(taskCompleteDate, taskStartDate)) {
                    throw new AppException(ErrorCode.DATE_EXCEPTION, "Completion date is larger than Start Date");
                }
            }
        }
        return true;
    }

    public Map<String, Object> getMapFromTask(Task task) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Map<String, Object> taskMap = mapper.convertValue(task, new TypeReference<Map<String, Object>>() {
        });
//      Updating usage of assignee and userId from Map to their Long values as we removed foreign key relationship
//        Map assigneeUserMap = (Map) taskMap.get("assignee");
//        Map userIdUserMap = (Map) taskMap.get("userId");
        taskMap.put("assignee", taskMap.get("assignee").toString());
        taskMap.put("userId", taskMap.get("userId").toString());
        taskMap.put("parentTaskId", taskMap.get("parentTaskId").toString());
        taskMap.put("taskId", taskMap.get("taskId").toString());
        return taskMap;
    }

}
