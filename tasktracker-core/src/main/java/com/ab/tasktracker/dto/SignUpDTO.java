package com.ab.tasktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UserDTO user;
    private String otp;
}
