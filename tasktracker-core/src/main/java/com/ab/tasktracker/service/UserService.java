package com.ab.tasktracker.service;

import com.ab.tasktracker.client.EmailClient;
import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.ab.tasktracker.dto.LoginUserDTO;
import com.ab.tasktracker.dto.UserDTO;
import com.ab.tasktracker.entity.Device;
import com.ab.tasktracker.entity.User;
import com.ab.tasktracker.enums.MailType;
import com.ab.tasktracker.exception.InvalidPasswordException;
import com.ab.tasktracker.exception.UserNotExistsException;
import com.ab.tasktracker.helper.UserHelper;
import com.ab.tasktracker.repository.UserRepository;
import com.ab.tasktracker.util.JwtUtil;
import com.ab.tasktracker.util.ModelMapperUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private EmailClient emailClient;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private AuthenticationManager authenticationManager;


    public Boolean validateUserSignUp(String email, HttpServletRequest httpServletRequest) {
        LOGGER.debug("Enter in UserService.validateUserSignUp()");
        userHelper.validateDeviceHeader(httpServletRequest);

        LOGGER.debug("Performing Validation");
        Set<ConstraintViolation<String>> violations = validator.validate(email);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<String> constraintViolation : violations) {
                sb.append(constraintViolation.getMessage());
            }
            throw new ConstraintViolationException("Error occurred: " + sb, violations);
        }

//      Generate OTP
        String otp;
        try {
            otp = userHelper.generateOTP();
        } catch (Exception e) {
            LOGGER.error("Error while generating OTP, message: {}", e.getMessage());
            return false;
        }

        if (!otp.isBlank()) {
            LOGGER.debug("OTP generated");
//          Send mail to user for validating
            Map<String, String> prepareSendMailMap = userHelper.prepareSendMailMap(email, MailType.VALIDATE_SIGNUP_MAIL);
            prepareSendMailMap.put("text", String.format("Hi please use this OTP %s to sign up", otp));
            emailClient.sendEmail(prepareSendMailMap);
        }
//      Store OTP in cache
        Boolean result = userHelper.insertTwoFactorAuthOTP(otp, email);
        LOGGER.debug("Exit in UserService.validateUserSignUp()");
        return result;
    }

    /**
     * This is service layer user SIgn Up methods which validates user request data,
     * checks if user exists, encode passwords and stores user details in DB.
     * We store device related information in DB while user signed up
     *
     * @param user UserDTO
     * @return String
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String userSignUp(UserDTO user, String otp, HttpServletRequest httpServletRequest) {
        LOGGER.debug("Enter in UserService.userSignUp()");
        String deviceId = userHelper.validateDeviceHeader(httpServletRequest);

        LOGGER.debug("Performing Validation");
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<UserDTO> constraintViolation : violations) {
                sb.append(constraintViolation.getMessage());
            }
            throw new ConstraintViolationException("Error occurred: " + sb, violations);
        }

        LOGGER.debug("Checking if user already exists");
        Boolean isUserExist = userHelper.isUserExists(user);
        if (isUserExist)
            throw new RuntimeException(TaskTrackerConstants.USER_ALREADY_EXISTS_MESSAGE);

//      Validate OTP
        String twoFactorAuthOTP = userHelper.getTwoFactorAuthOTP(user.getEmail());
        if (twoFactorAuthOTP.isBlank() || (!twoFactorAuthOTP.equals(otp))) {
            throw new RuntimeException("2 factor authentication failed, OTP not valid");
        }

        LOGGER.debug("Encrypt Password");
        User userEntity = new User();
        BeanUtils.copyProperties(user, userEntity);
        userEntity.setHashedPassword(passwordEncoder.encode(userEntity.getHashedPassword()));

        LOGGER.debug("Saving user data in DB and Cache");
        User persistedUser = userHelper.insertUserDetails(userEntity);

        LOGGER.debug("Saving device data in DB and cache");
        Device persistedDevice = null;
        if (persistedUser != null) {
            Device device = ModelMapperUtil.getDeviceEntityFromUserEntity(persistedUser, deviceId);
            persistedDevice = userHelper.insertDeviceDetails(device);
        } else {
            throw new UserNotExistsException(TaskTrackerConstants.USER_DOES_NOT_EXIST_MESSAGE);
        }
        if (persistedDevice == null) {
            throw new RuntimeException("Error while saving device details");
        }

        LOGGER.debug("Generating JWT Token");
        String jwtToken = jwtUtil.generateJWTToken(userEntity.getEmail(), userEntity.getUserId());

        LOGGER.debug("Exit in UserService.userSignUp()");
        return jwtToken;
    }

    /**
     * This is user login method in service layer, we perform validations first
     * then get user details for active users, compare passwords and send user details
     * back. We also check for user's device ID if it does not exist we add in DB,
     *
     * @param loginUserDTO LoginUserDTO contains email and password for login
     * @return UserDTO
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public JSONObject userLogin(LoginUserDTO loginUserDTO, HttpServletRequest httpServletRequest) throws UserNotExistsException, InvalidPasswordException {
        LOGGER.debug("Enter in UserService.userLogin()");
        String deviceId = userHelper.validateDeviceHeader(httpServletRequest);

        LOGGER.debug("Performing Validation");
        Set<ConstraintViolation<LoginUserDTO>> violations = validator.validate(loginUserDTO);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<LoginUserDTO> constraintViolation : violations) {
                sb.append(constraintViolation.getMessage());
            }
            throw new ConstraintViolationException("Error occurred: " + sb, violations);
        }

        LOGGER.debug("Checking if user already exists and password comparison");
//      Spring security authentication manager to handle auth check
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUserDTO.getEmail(), loginUserDTO.getPassword()));
        if (authenticate.isAuthenticated()) {
            LOGGER.debug("Authentication Complete");
//          User userEntity = userHelper.getUserDetails(loginUserDTO.getEmail());
            User userEntity = (User) authenticate.getPrincipal();
            if (userEntity == null)
                throw new UserNotExistsException(TaskTrackerConstants.USER_DOES_NOT_EXIST_MESSAGE);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(userEntity, userDTO);

            LOGGER.debug("Saving device data in DB");
            Device device = userHelper.getDeviceDetails(userEntity.getUserId(), deviceId);
//          We check if device ID exists for this user, if not we enter device ID as user logged in with new device.
            if (device == null) {
//              Send mail as user logged in with new device
                Map<String, String> prepareSendMailMap = userHelper.prepareSendMailMap(userDTO.getEmail(), MailType.NEW_DEVICE_LOGIN_MAIL);
                prepareSendMailMap.put("text", "Hi " + userDTO.getUserName() + " your account was logged in with new device " + deviceId);
                emailClient.sendEmail(prepareSendMailMap);
                Device deviceEntity = ModelMapperUtil.getDeviceEntityFromUserEntity(userEntity, deviceId);
                userHelper.insertDeviceDetails(deviceEntity);
            }

            LOGGER.debug("Generating JWT Token");
            String jwtToken = jwtUtil.generateJWTToken(userDTO.getEmail(), userDTO.getUserId());

            LOGGER.debug("Exit in UserService.userLogin()");
            JSONObject loginJson = new JSONObject();
            loginJson.put("token", jwtToken);
            loginJson.put("isLoginSuccessful", true);
            return loginJson;
        } else {
            LOGGER.debug("Exception in UserService.userLogin()");
            throw new InvalidPasswordException(TaskTrackerConstants.INVALID_PASSWORD_MESSAGE);
        }
    }

    /**
     * This gives details of user by principal in context holder of current thread
     *
     * @return JSONObject
     */
    public JSONObject userMe() throws UserNotExistsException, InvalidPasswordException {
        LOGGER.debug("Enter in UserService.userMe()");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            JSONObject response = new JSONObject();
            User user = null;
            if (authentication != null) {
                String email = authentication.getPrincipal().toString();
                if (!email.isBlank()) {
                    user = userHelper.getUserDetails(email);
                    if (user != null) {
                        response.put("email", user.getEmail());
                        response.put("userName", user.getUserName());
                        response.put("mobileNumber", user.getMobileNumber());
                        response.put("createdDate", user.getCreatedDate());
                        response.put("updatedDate", user.getUpdatedDate());
                    } else {
                        throw new UserNotExistsException(TaskTrackerConstants.USER_DOES_NOT_EXIST_MESSAGE);
                    }
                }
            }
            LOGGER.debug("Exit in UserService.userMe()");
            return response;
        } catch (Exception e) {
            LOGGER.error("Exception in UserService.getMe(), message: {}", e.getMessage());
        }
        return new JSONObject();
    }

    /**
     * We validate email and send mail to user with an OTP, which we store in cache with TTL Cache Evict strategy
     *
     * @param email to validate user
     * @return success/fail boolean
     */
    public boolean forgotPassword(String email, HttpServletRequest httpServletRequest) {
        LOGGER.debug("Enter in UserService.forgotPassword()");
        userHelper.validateDeviceHeader(httpServletRequest);

        try {
//          Check if user exists
            if (!email.isBlank()) {
                User userEntity = userHelper.getUserDetails(email);
                if (userEntity != null && userEntity.getUserId() != null) {
                    LOGGER.debug("User details found");
                } else {
                    LOGGER.debug("User Not found");
                    return false;
                }
            }
//          Generate OTP
            String otp = userHelper.generateOTP();

            if (!otp.isBlank()) {
                LOGGER.debug("OTP generated");
//              Send mail to user
                Map<String, String> prepareSendMailMap = userHelper.prepareSendMailMap(email, MailType.FORGOT_PASSWORD_MAIL);
                prepareSendMailMap.put("text", "Your OTP is " + otp);
                emailClient.sendEmail(prepareSendMailMap);
            }
//          Store in cache
            Map<String, Object> map = new HashMap<>();
            String cacheKey = email + TaskTrackerConstants.CACHE_FORGOT_PASSWORD;
            map.put(cacheKey, otp);
            boolean result = (Boolean) cacheService.cacheOps(map, CacheService.CacheOperation.INSERT).getFirst();
            LOGGER.debug("Exit in UserService.forgotPassword()");
            return result;
        } catch (Exception e) {
            LOGGER.error("Exception in UserService.forgotPassword(), message: {}", e.getMessage());
            return false;
        }
    }

    /**
     * We validate email and OTP
     *
     * @param email to validate user
     * @return success/fail boolean
     */
    public JSONObject validateOTP(String email, String otp, HttpServletRequest httpServletRequest) {
        LOGGER.debug("Enter in UserService.validateOTP()");
        JSONObject responseJson = new JSONObject();
        String deviceId = userHelper.validateDeviceHeader(httpServletRequest);
        User userEntity = null;
        try {
//          Check if user exists
            if (!email.isBlank()) {
                userEntity = userHelper.getUserDetails(email);
                if (userEntity != null) {
                    LOGGER.debug("User details found");
                } else {
                    LOGGER.debug("User Not found");
                    responseJson.put("isUserAndOTPValidatedAnd", false);
                    return responseJson;
                }
            }
//          Get otp from cache
            Map<String, Object> map = new HashMap<>();
            String cacheKey = email + TaskTrackerConstants.CACHE_FORGOT_PASSWORD;
            map.put(cacheKey, null);
            String otpFromCache = (String) cacheService.cacheOps(map, CacheService.CacheOperation.FETCH).getFirst();
            if (otpFromCache != null && !otpFromCache.isBlank() && otpFromCache.equals(otp) && userEntity != null) {
                LOGGER.debug("OTP valid");
                LOGGER.debug("Saving device data in DB");
                Device device = userHelper.getDeviceDetails(userEntity.getUserId(), deviceId);
//              We check if device ID exists for this user, if not we enter device ID as user logged in with new device.
                if (device == null) {
                    Device deviceEntity = ModelMapperUtil.getDeviceEntityFromUserEntity(userEntity, deviceId);
                    userHelper.insertDeviceDetails(deviceEntity);
                }
                LOGGER.debug("Generating JWT Token");
                String jwtToken = jwtUtil.generateJWTToken(email, userEntity.getUserId());
                responseJson.put("isUserAndOTPValidatedAnd", true);
                responseJson.put("token", jwtToken);
                return responseJson;
            } else {
                LOGGER.debug("Exit in UserService.validateOTP() OTP invalid");
                responseJson.put("isUserAndOTPValidatedAnd", false);
                return responseJson;
            }
        } catch (Exception e) {
            LOGGER.error("Exception in UserService.validateOTP(), message: {}", e.getMessage());
            responseJson.put("isUserAndOTPValidatedAnd", false);
            return responseJson;
        }
    }

    /**
     * We validate user, password and set new password.
     *
     * @param loginUserDTO to get login details and reset password
     * @return success/fail boolean
     */
    public Boolean changePassword(LoginUserDTO loginUserDTO, HttpServletRequest httpServletRequest) {
        LOGGER.debug("Enter in UserService.changePassword()");

        LOGGER.debug("Performing Validation");
        Set<ConstraintViolation<LoginUserDTO>> violations = validator.validate(loginUserDTO);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<LoginUserDTO> constraintViolation : violations) {
                sb.append(constraintViolation.getMessage());
            }
            throw new ConstraintViolationException("Error occurred: " + sb, violations);
        }
//      todo: Invalidate token if user soft deletes details.

//      User existence already checked with Device in Filter
        User userEntity = userHelper.getUserDetails(loginUserDTO.getEmail());
        if (userEntity == null) {
            LOGGER.debug("User details not found");
            return false;
        }

        LOGGER.debug("Encrypt Password");
        userEntity.setHashedPassword(passwordEncoder.encode(loginUserDTO.getPassword()));

        LOGGER.debug("Saving user data in DB/Cache");
        User updatedUser = userHelper.insertUserDetails(userEntity);

        if (updatedUser == null) {
            return false;
        }

        LOGGER.debug("Exit in UserService.changePassword()");
        return true;
    }
}
