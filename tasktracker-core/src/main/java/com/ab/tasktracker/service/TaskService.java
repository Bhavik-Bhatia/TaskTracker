package com.ab.tasktracker.service;

import com.ab.tasktracker.client.MLClient;
import com.ab.tasktracker.client.RestTemplateClient;
import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.ab.tasktracker.dto.TaskDTO;
import com.ab.tasktracker.entity.Task;
import com.ab.tasktracker.entity.User;
import com.ab.tasktracker.helper.TaskHelper;
import com.ab.tasktracker.helper.UserHelper;
import com.ab.tasktracker.repository.TaskRepository;
import com.ab.tasktracker.util.ModelMapperUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private MLClient mlClient;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private UserHelper userHelper;
    @Autowired
    private TaskHelper taskHelper;
    @Autowired
    private TypesenseService typesenseService;


    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

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
    public TaskDTO addTask(TaskDTO taskDTO) {
        LOGGER.debug("Enter in TaskService.addTask()");
        User userEntity = null;
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
        LOGGER.debug("Get User details by token");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String email = authentication.getPrincipal().toString();
            if (!email.isBlank()) {
//              Get user details via cache
                userEntity = userHelper.getUserDetails(email);
//              If Task Id present task is being updated not inserted
                if (taskDTO.getTaskId() != null && userEntity.getUserId() != null) {
                    isUpdate = true;
//                  Get details from Cache
                    if (taskHelper.getTaskDetails(taskDTO.getTaskId(), userEntity.getUserId()) == null) {
                        throw new RuntimeException(TaskTrackerConstants.TASK_NOT_EXISTS);
                    }
                }
                if (userEntity != null) {
                    taskDTO.setUserId(userEntity.getUserId());
//                  Assignee feature will be added in the future
                    if (taskDTO.getAssignee() == null) {
                        taskDTO.setAssignee(userEntity.getUserId());
                    }
                } else {
                    throw new RuntimeException(TaskTrackerConstants.USER_DOES_NOT_EXIST_MESSAGE);
                }
                LOGGER.debug("User details found");
            }
        } else {
            throw new RuntimeException(TaskTrackerConstants.AUTHENTICATION_REQUIRED);
        }

        LOGGER.debug("Call ML Service for initial category");
//      String initialCategory = mlClient.getCategoryByTaskName(taskDTO.getTaskName());
        taskDTO.setUpdatedCategory(taskDTO.getInitialCategory());

        LOGGER.debug("Save task in DB");
        Task task = ModelMapperUtil.getTaskFromTaskDTO(taskDTO, userEntity);

//      We save task in DB/Cache and Cache(Update/Insert)
        if (task != null) {
            taskHelper.insertTaskDetails(task);

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
        LOGGER.debug("Exit in TaskService.addTask()");
        return ModelMapperUtil.getTaskDTOFromTask(task);
    }

    /**
     * We get task from userID and taskId, to check that task requested for is of the same user who
     * created it
     *
     * @param taskId to get task details
     * @return TaskDTO task from userID and taskId
     */
    public TaskDTO getTask(Long taskId) {
        LOGGER.debug("Get User details by token");
//      Get user by context holder and get task from user and task ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String email = authentication.getPrincipal().toString();
            if (!email.isBlank()) {
                User userEntity = userHelper.getUserDetails(email);
                if (userEntity != null) {
                    LOGGER.debug("User details found");
//                  We get task details from cache/DB
                    Task task = taskHelper.getTaskDetails(taskId, userEntity.getUserId());
                    if (task != null) {
                        return ModelMapperUtil.getTaskDTOFromTask(task);
                    } else {
                        throw new RuntimeException(TaskTrackerConstants.TASK_NOT_EXISTS);
                    }
                } else {
                    throw new RuntimeException(TaskTrackerConstants.USER_DOES_NOT_EXIST_MESSAGE);
                }

            }
        }
        throw new RuntimeException(TaskTrackerConstants.AUTHENTICATION_REQUIRED);
    }

    public List<Map<String, Object>> getAllTasks() throws Exception {
        LOGGER.debug("Get User details by token");
//      Get user by context holder and get task from user and task ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String email = authentication.getPrincipal().toString();
            if (!email.isBlank()) {
                User userEntity = userHelper.getUserDetails(email);
                if (userEntity != null) {
                    LOGGER.debug("User details found");
//                  We get task details from Type Sense
                    List<Map<String, Object>> mapList = typesenseService.getAllDocumentsData("task", userEntity.getUserId());
                    if (!mapList.isEmpty()) {
                        return mapList;
                    } else {
                        throw new RuntimeException(TaskTrackerConstants.TASK_NOT_EXISTS);
                    }
                } else {
                    throw new RuntimeException(TaskTrackerConstants.USER_DOES_NOT_EXIST_MESSAGE);
                }

            }
        }
        throw new RuntimeException(TaskTrackerConstants.AUTHENTICATION_REQUIRED);
    }

}
