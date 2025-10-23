package com.ab.tasktracker.rest;

import com.ab.tasktracker.constants.TaskTrackerURI;
import com.ab.tasktracker.dto.TaskDTO;
import com.ab.tasktracker.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//TODO 6: Improve your HTTP Methods like POST, GET, DELETE, PUT. Use them properly as per their standards. Return URI in POST if a new resource is created.
@RestController
@RequestMapping(TaskTrackerURI.TASK_URI)
@CrossOrigin("*")
@AllArgsConstructor
public class TaskResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskResources.class);

    private TaskService taskService;

    /**
     * This API inserts tasks in DB and Solr. Gets task category from ML service
     * and saves it in DB and Solr as well.
     *
     * @return ResponseEntity
     */
    @PostMapping(value = TaskTrackerURI.ADD_TASK_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDTO> addTask(@Valid @NotNull @RequestBody TaskDTO taskDTO, HttpServletRequest httpServletRequest) {
        LOGGER.debug("Enter in TaskResources.addTask()");
        TaskDTO savedTask = taskService.addTask(taskDTO);
        LOGGER.debug("Exit in TaskResources.addTask()");
        return ResponseEntity.status(HttpStatus.OK).body(savedTask);
    }

    @PostMapping(value = "callML/{taskName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> callML(@Valid @NotNull @PathVariable String taskName, HttpServletRequest httpServletRequest) {
        LOGGER.debug("Enter in TaskResources.callML()");
        taskService.callML(taskName, httpServletRequest);
        LOGGER.debug("Exit in TaskResources.callML()");
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }


    @PostMapping(value = TaskTrackerURI.GET_TASK_URI, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getTask(@NotNull @RequestParam Long taskId) {
        LOGGER.debug("Enter in TaskResources.getTask()");
        TaskDTO task = taskService.getTask(taskId);
        LOGGER.debug("Exit in TaskResources.getTask()");
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @PostMapping(value = TaskTrackerURI.GET_ALL_ME_TASK_URI, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getAllMeTask() throws Exception {
        LOGGER.debug("Enter in TaskResources.getTask()");
        List<Map<String, Object>> allTasks = taskService.getAllTasks();
        LOGGER.debug("Exit in TaskResources.getTask()");
        return ResponseEntity.status(HttpStatus.OK).body(allTasks);
    }

    @PostMapping(value = TaskTrackerURI.REMOVE_TASK_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> removeTask() {
        LOGGER.debug("Enter in TaskResources.removeTask()");
        LOGGER.debug("Exit in TaskResources.removeTask()");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }


}
