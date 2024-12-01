package com.ab.tasktracker.dto;

import com.ab.tasktracker.constants.TaskTrackerConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Email(message = TaskTrackerConstants.INVALID_EMAIL_MESSAGE)
    @NotBlank(message = TaskTrackerConstants.EMAIL_REQUIRED_MESSAGE)
    private String email;

    @NotBlank(message = TaskTrackerConstants.PASSWORD_REQUIRED_MESSAGE)
    @Size(min = 8, max = 20, message = TaskTrackerConstants.PASSWORD_VALIDATION_MESSAGE)
    private String password;

}
