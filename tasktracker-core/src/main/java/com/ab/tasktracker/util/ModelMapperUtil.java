package com.ab.tasktracker.util;

import com.ab.tasktracker.dto.TaskDTO;
import com.ab.tasktracker.entity.Task;
import com.ab.tasktracker.entity.User;
import org.springframework.beans.BeanUtils;

public class ModelMapperUtil {

    public static TaskDTO getTaskDTOFromTask(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        taskDTO.setUserId(task.getUserId());
        taskDTO.setAssignee(task.getAssignee());
        return taskDTO;
    }

    public static Task getTaskFromTaskDTO(TaskDTO taskDTO,User user) {
        Task task = new Task();
        BeanUtils.copyProperties(taskDTO, task);
//        No need to set user and assignee explicitly as they are copied by BeanUtils as updated from User to Long type
//        task.setAssignee(user.getUserId());
//        task.setUserId(user.getUserId());
        return task;
    }

}
