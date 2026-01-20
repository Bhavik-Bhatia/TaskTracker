package com.ab.tasktracker.service;

import com.ab.tasktracker.annotation.Log;
import com.ab.tasktracker.client.RestTemplateClient;
import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.ab.tasktracker.dto.TaskDTO;
import com.ab.tasktracker.entity.Task;
import com.ab.tasktracker.entity.User;
import com.ab.tasktracker.exception.AppException;
import com.ab.tasktracker.exception.ErrorCode;
import com.ab.tasktracker.helper.TaskHelper;
import com.ab.tasktracker.util.ModelMapperUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ab.tasktracker.exception.ErrorCode.AUTHENTICATION_REQUIRED;
import static com.ab.tasktracker.exception.ErrorCode.USER_ID_REQUIRED;

@Service
@AllArgsConstructor
public class TaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    private TaskHelper taskHelper;

    private TypesenseService typesenseService;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Log
    public void callML(String taskName, HttpServletRequest httpServletRequest) {
        LOGGER.debug("Call ML Service for initial category");
        RestTemplateClient client = new RestTemplateClient();
//      String initialCategory = mlClient.getCategoryByTaskName(taskName,"");
        String initialCategory = client.callAPIPathVariable("https://8856-49-34-125-139.ngrok-free.app/search/" + taskName, httpServletRequest);
        LOGGER.debug(initialCategory);
    }

    /**
     * This API inserts tasks in DB and Solr. Gets task category from ML service
     * and saves it in DB and Type Sense as well.
     *
     * @param taskDTO TaskDTO from UI is persisted
     * @return TaskDTO
     */
    @Log
    public TaskDTO addTask(TaskDTO taskDTO,HttpServletRequest httpServletRequest) throws AppException {
        Long userId = (Long) httpServletRequest.getAttribute("userId");
        //TODO: Need to user details from Auth Service or task service needs User entity also
        User userEntity = new User();
        boolean isUpdate = false;

        LOGGER.debug("Performing Validation");
        try {
            taskHelper.validateTaskDateTime(taskDTO.getTaskStartDate(), taskDTO.getTaskDueDate(), taskDTO.getTaskCompleteDate());
        } catch (Exception e) {
            LOGGER.error("Error occurred: {}", e.getMessage());
        }

        Set<ConstraintViolation<TaskDTO>> violations = validator.validate(taskDTO);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<TaskDTO> constraintViolation : violations) {
                sb.append(constraintViolation.getMessage());
            }
            throw new ConstraintViolationException("Error occurred: " + sb, violations);
        }

//      Get user details by token and add in TaskDTO
        if (userId != null) {
//          If Task Id present task is being updated not inserted
            if (taskDTO.getTaskId() != null) {
                isUpdate = true;
//                  Get details from Cache
                if (taskHelper.getTaskDetails(taskDTO.getTaskId(), userId) == null) {
                    throw new AppException(ErrorCode.TASK_NOT_FOUND, TaskTrackerConstants.TASK_NOT_EXISTS);
                }
            }
            taskDTO.setUserId(userId);
//          Assignee feature will be added in the future
            if (taskDTO.getAssignee() == null) {
                taskDTO.setAssignee(userId);
            }
        } else {
            throw new AppException(USER_ID_REQUIRED, TaskTrackerConstants.USER_ID_REQUIRED_MESSAGE);
        }

        LOGGER.debug("Call ML Service for initial category");
//      String initialCategory = mlClient.getCategoryByTaskName(taskDTO.getTaskName());
        taskDTO.setUpdatedCategory(taskDTO.getInitialCategory());

        LOGGER.debug("Save task in DB");
        Task task = ModelMapperUtil.getTaskFromTaskDTO(taskDTO, userEntity);

//      We save task in DB/Cache and Cache(Update/Insert)
        if (task != null) {
//            try {
            taskHelper.insertTaskDetails(task);
//            }catch (Exception e) {
//                LOGGER.error("Error while inserting task details: {}", e.getMessage());
//            }

            LOGGER.debug("Save task in TypeSense");
            Map<String, Object> taskMap;
            taskMap = taskHelper.getMapFromTask(task);
            try {
                if (isUpdate) {
                    typesenseService.updateDataToCollection("task", taskMap, String.valueOf(task.getTaskId()));
                } else {
                    typesenseService.insertDataToCollection("task", taskMap);
                }
            } catch (Exception e) {
                LOGGER.error("Exception while inserting into TypeSense {}", e.getMessage());
            }
        }
        return ModelMapperUtil.getTaskDTOFromTask(task);
    }

    /**
     * We get task from userID and taskId, to check that task requested for is of the same user who
     * created it
     *
     * @param taskId to get task details
     * @return TaskDTO task from userID and taskId
     */
    @Log
    public TaskDTO getTask(Long taskId, HttpServletRequest httpServletRequest) throws AppException {
        LOGGER.debug("Get User details by token");
        Long userId = (Long) httpServletRequest.getAttribute("userId");
        if (userId != null) {
            LOGGER.debug("User details found");
//          We get task details from cache/DB
            Task task = taskHelper.getTaskDetails(taskId, userId);
            if (task != null) {
                return ModelMapperUtil.getTaskDTOFromTask(task);
            } else {
                throw new AppException(ErrorCode.TASK_NOT_FOUND, TaskTrackerConstants.TASK_NOT_EXISTS);
            }
        } else {
            throw new AppException(ErrorCode.USER_NOT_FOUND, TaskTrackerConstants.USER_DOES_NOT_EXIST_MESSAGE);
        }
    }

    @Log
    public List<Map<String, Object>> getAllTasks(HttpServletRequest httpServletRequest) throws Exception {
        Long userId = (Long) httpServletRequest.getAttribute("userId");
        if (userId != null) {
            LOGGER.debug("User details found");
//          We get task details from Type Sense
            List<Map<String, Object>> mapList = typesenseService.getAllDocumentsData("task", userId);
            if (!mapList.isEmpty()) {
                return mapList;
            } else {
                throw new AppException(ErrorCode.TASK_NOT_FOUND, TaskTrackerConstants.TASK_NOT_EXISTS);
            }
        }
        throw new AppException(AUTHENTICATION_REQUIRED, TaskTrackerConstants.AUTHENTICATION_REQUIRED);
    }

}
