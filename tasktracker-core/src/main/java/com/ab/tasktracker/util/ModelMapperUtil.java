package com.ab.tasktracker.util;

import com.ab.tasktracker.dto.TaskDTO;
import com.ab.tasktracker.dto.UserDTO;
import com.ab.tasktracker.entity.Device;
import com.ab.tasktracker.entity.Task;
import com.ab.tasktracker.entity.User;
import org.springframework.beans.BeanUtils;

public class ModelMapperUtil {


    public static Device getDeviceEntityFromUserEntity(User user, String deviceId) {
        Device device = new Device();
        device.setUser(user);
        device.setDeviceId(deviceId);
        return device;
    }

    public static TaskDTO getTaskDTOFromTask(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        taskDTO.setUserId(task.getUserId().getUserId());
        taskDTO.setAssignee(task.getAssignee().getUserId());
        return taskDTO;
    }

    public static Task getTaskFromTaskDTO(TaskDTO taskDTO,User user) {
        Task task = new Task();
        BeanUtils.copyProperties(taskDTO, task);
        task.setAssignee(user);
        task.setUserId(user);
        return task;
    }

    public static UserDTO getUserDTOFromUser(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }


}
