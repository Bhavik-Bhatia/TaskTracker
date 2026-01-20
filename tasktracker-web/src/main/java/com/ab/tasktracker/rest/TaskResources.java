package com.ab.tasktracker.rest;

import com.ab.tasktracker.annotation.Log;
import com.ab.tasktracker.constants.TaskTrackerURI;
import com.ab.tasktracker.dto.TaskDTO;
import com.ab.tasktracker.exception.AppException;
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
     * This API inserts tasks in DB and TypeSense. Gets task category from ML service
     * and saves it in DB and Typesense as well.
     *
     * @return ResponseEntity
     */
    @PostMapping(value = TaskTrackerURI.ADD_TASK_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Log
    public ResponseEntity<TaskDTO> addTask(@Valid @NotNull @RequestBody TaskDTO taskDTO, HttpServletRequest httpServletRequest) throws AppException {
        TaskDTO savedTask = taskService.addTask(taskDTO,httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(savedTask);
    }

    @PostMapping(value = "callML/{taskName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Log
    public ResponseEntity<Boolean> callML(@Valid @NotNull @PathVariable String taskName, HttpServletRequest httpServletRequest) {
        taskService.callML(taskName, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }


    @PostMapping(value = TaskTrackerURI.GET_TASK_URI, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Log
    public ResponseEntity<Object> getTask(@NotNull @RequestParam Long taskId, HttpServletRequest httpServletRequest) throws AppException {
        TaskDTO task = taskService.getTask(taskId, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @PostMapping(value = TaskTrackerURI.GET_ALL_ME_TASK_URI, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Log
    public ResponseEntity<List<Map<String, Object>>> getAllMeTask(HttpServletRequest httpServletRequest) throws Exception {
        List<Map<String, Object>> allTasks = taskService.getAllTasks(httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(allTasks);
    }

    @PostMapping(value = TaskTrackerURI.REMOVE_TASK_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Log
    public ResponseEntity<Object> removeTask() {
        return ResponseEntity.status(HttpStatus.OK).body("");
    }


}
