package com.ab.tasktracker.dto;

import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    @NotBlank(message = TaskTrackerConstants.USERNAME_REQUIRED_MESSAGE)
    private String userName;

    @Email(message = TaskTrackerConstants.INVALID_EMAIL_MESSAGE)
    @NotBlank(message = TaskTrackerConstants.EMAIL_REQUIRED_MESSAGE)
    private String email;

    @NotBlank(message = TaskTrackerConstants.MOBILE_NUMBER_REQUIRED_MESSAGE)
    private String mobileNumber;

    @NotBlank(message = TaskTrackerConstants.PASSWORD_REQUIRED_MESSAGE)
    @Size(min = 8, max = 20, message = TaskTrackerConstants.PASSWORD_VALIDATION_MESSAGE)
    private String hashedPassword;

    @JsonIgnore
    private boolean isDeleted;
}
