package com.ab.tasktracker.service;

import com.ab.jwt.JwtUtil;
import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.ab.tasktracker.dto.LoginUserDTO;
import com.ab.tasktracker.dto.UserDTO;
import com.ab.tasktracker.entity.Device;
import com.ab.tasktracker.entity.User;
import com.ab.tasktracker.exception.InvalidPasswordException;
import com.ab.tasktracker.exception.UserNotExistsException;
import com.ab.tasktracker.repository.DeviceRepository;
import com.ab.tasktracker.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;


@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private MockHttpServletRequest httpServletRequest;

    @Before
    public void doBefore() {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("deviceId", "TestDevice");
    }

    @Test
    public void testUserSignUpSuccess() {
        UserDTO userDTO = prepareUserDTO(0);
        Mockito.when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(false);
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(new User());
        Mockito.when(deviceRepository.save(Mockito.any())).thenReturn(new Device());
        Mockito.when(jwtUtil.generateJWTToken(Mockito.any(), Mockito.any())).thenReturn("ijdfhdkfjh3&739bfjh");
        String result = userService.userSignUp(userDTO, "123", httpServletRequest);
        Assert.assertEquals(result, "ijdfhdkfjh3&739bfjh");
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUserSignUpValidationFail() {
        UserDTO userDTO = prepareUserDTO(1);
        userService.userSignUp(userDTO, "123", httpServletRequest);
    }

    @Test(expected = Exception.class)
    public void testUserSignUpException() {
        UserDTO userDTO = prepareUserDTO(2);
        userService.userSignUp(userDTO, "123", httpServletRequest);
    }

    @Test
    public void testUserSignUpEmailAlreadyExists() {
        UserDTO userDTO = prepareUserDTO(3);
        Mockito.when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(true);
        try {
            userService.userSignUp(userDTO, "123", httpServletRequest);
        } catch (RuntimeException e) {
            Assert.assertEquals(TaskTrackerConstants.USER_ALREADY_EXISTS_MESSAGE, e.getMessage());
        }

    }

    @Test
    public void testUserLoginSuccessful() {
        LoginUserDTO loginUserDTO = prepareLoginDTO(0);
        Mockito.when(userRepository.findActiveUserByEmail(Mockito.anyString())).thenReturn(prepareUser());
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(jwtUtil.generateJWTToken(Mockito.anyString(), Mockito.anyLong())).thenReturn("ijdfhdkfjh3&739bfjh");
        JSONObject result = userService.userLogin(loginUserDTO, httpServletRequest);
        Assert.assertNotNull(result);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUserLoginValidationFail() {
        LoginUserDTO loginUserDTO = prepareLoginDTO(1);
        userService.userLogin(loginUserDTO, httpServletRequest);
    }

    @Test(expected = UserNotExistsException.class)
    public void testUserLoginUserDoesNotExist() {
        LoginUserDTO loginUserDTO = prepareLoginDTO(0);
        Mockito.when(userRepository.findActiveUserByEmail(Mockito.anyString())).thenReturn(null);
        userService.userLogin(loginUserDTO, httpServletRequest);
    }

    @Test(expected = InvalidPasswordException.class)
    public void testUserLoginPasswordInvalid() {
        String hashedPassword = "V6p3R6OgWy0nJnCVGlK";
        User user = new User();
        user.setHashedPassword(hashedPassword);
        LoginUserDTO loginUserDTO = prepareLoginDTO(0);
        Mockito.when(userRepository.findActiveUserByEmail(Mockito.anyString())).thenReturn(user);
        userService.userLogin(loginUserDTO, httpServletRequest);
    }

    @Test
    public void testUserLoginDeviceIdNotExists() {
        String deviceId = "V6p3R6OgWy0nJnCVGlK";
        Device device = new Device();
        device.setDeviceId(deviceId);
        LoginUserDTO loginUserDTO = prepareLoginDTO(0);
        Mockito.when(userRepository.findActiveUserByEmail(Mockito.anyString())).thenReturn(prepareUser());
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        userService.userLogin(loginUserDTO, httpServletRequest);
    }


    @Test(expected = Exception.class)
    public void testUserLoginException() {
        LoginUserDTO loginUserDTO = prepareLoginDTO(2);
        userService.userLogin(loginUserDTO, httpServletRequest);
    }

    private UserDTO prepareUserDTO(int index) {
        return switch (index) {
            case 1 -> prepareUserDTOForSignUpValidationFail();
            case 2 -> prepareUserDTOForSignUpException();
            default -> prepareUserDTOForSignUpSuccess();
        };
    }

    private LoginUserDTO prepareLoginDTO(int index) {
        return switch (index) {
            case 1 -> prepareLoginDTOValidationFail();
            case 2 -> prepareLoginDTOException();
            default -> prepareLoginDTOSuccess();
        };
    }

    private LoginUserDTO prepareLoginDTOSuccess() {
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("adarshlal@gmail.com");
        loginUserDTO.setPassword("Adarsh@123");
        return loginUserDTO;
    }

    private LoginUserDTO prepareLoginDTOValidationFail() {
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("bhavikbhatia@");
        loginUserDTO.setPassword("Bhavik@123");
        return loginUserDTO;
    }

    private LoginUserDTO prepareLoginDTOException() {
        return null;
    }

    private UserDTO prepareUserDTOForSignUpSuccess() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName("Bhavik Bhatia");
        userDTO.setEmail("bhavikbhatia9@gmail.com");
        userDTO.setMobileNumber("7890458348");
        userDTO.setHashedPassword("Bhavik@123");
        return userDTO;
    }

    private UserDTO prepareUserDTOForSignUpValidationFail() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName("Bhavik Bhatia");
        userDTO.setEmail("bhavikbhatia9gmail.com");
        userDTO.setMobileNumber("7890458348");
        userDTO.setHashedPassword("B");
        return userDTO;
    }

    private UserDTO prepareUserDTOForSignUpException() {
        return null;
    }

    private User prepareUser() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("bhavikbhatia9@gmail.com");
        user.setHashedPassword("$2a$10$Q5UxSFkj.nwFNmnxgjRW..1/93hlALRbwid9OXrkhpy5da3HedEfa");
        user.setCreatedDate(ZonedDateTime.now());
        user.setUpdatedDate(ZonedDateTime.now());
        user.setMobileNumber("7069096437");
        user.setDeleted(false);
        return user;
    }

}
