package com.ab.tasktracker.dto;

import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.ab.tasktracker.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taskId;

    @NotBlank(message = TaskTrackerConstants.TASK_NAME_REQUIRED_MESSAGE)
    private String taskName;

    @NotNull(message = TaskTrackerConstants.TASK_STATUS_NAME_REQUIRED_MESSAGE)
    private TaskStatus taskStatus;

    private String taskDescription;

    private boolean isDeleted;

    private String initialCategory;

    private String updatedCategory;

    private Long assignee;

    private Long userId;

    @NotNull(message = TaskTrackerConstants.PARENT_TASK_NAME_REQUIRED_MESSAGE)
    private Long parentTaskId;

    @NotBlank(message = TaskTrackerConstants.TASK_PRIORITY_REQUIRED_MESSAGE)
    private String taskPriority;

    @NotNull(message = TaskTrackerConstants.TASK_START_DATE_REQUIRED_MESSAGE)
    @DateTimeFormat( pattern="yyyy-MM-dd HH:mm:ss.SSSSSSZ")
    private ZonedDateTime taskStartDate;

    @NotNull(message = TaskTrackerConstants.TASK_DUE_DATE_REQUIRED_MESSAGE)
    @DateTimeFormat( pattern="yyyy-MM-dd HH:mm:ss.SSSSSSZ")
//  @Pattern(regexp = "yyyy-MM-dd HH:mm:ss.SSSSSSZ", message = TaskTrackerConstants.TASK_DATE_FORMAT_INVALID)
// todo : Need to Manage Future or Present via code as user can edit DueDate to be in past
//  @FutureOrPresent(message = TaskTrackerConstants.TASK_DATE_PRESENT_AND_FUTURE)
    private ZonedDateTime taskDueDate;

    @DateTimeFormat( pattern="yyyy-MM-dd HH:mm:ss.SSSSSSZ")
// todo : Need to Manage Future or Present via code as user can edit DueDate to be in past
//  @FutureOrPresent(message = TaskTrackerConstants.TASK_DATE_PRESENT_AND_FUTURE)
    private ZonedDateTime taskCompleteDate;

    private ZonedDateTime createdDate;

    private ZonedDateTime updatedDate;

}
