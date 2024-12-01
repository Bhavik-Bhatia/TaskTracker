package com.ab.tasktracker.helper;

import com.ab.tasktracker.dto.UserDTO;
import com.ab.tasktracker.entity.Device;
import com.ab.tasktracker.entity.User;
import com.ab.tasktracker.enums.MailType;
import com.ab.tasktracker.repository.DeviceRepository;
import com.ab.tasktracker.repository.UserRepository;
import com.ab.tasktracker.service.CacheService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import static com.ab.tasktracker.constants.TaskTrackerConstants.*;

/**
 * Helper class for User Service
 */
@Component
public class UserHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHelper.class);

    @Autowired
    private Environment environment;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;


    /**
     * Generates random OTP of length 6 for forgot password
     *
     * @return String
     */
    public String generateOTP() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        int sixDigitNumber = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(sixDigitNumber);
    }

    /**
     * We check if DeviceId is present if not we throw exception. This method is useful for
     * excluded URIs
     *
     * @param httpServletRequest We get deviceHeader from request object
     */
    public String validateDeviceHeader(HttpServletRequest httpServletRequest) {
        String deviceId = httpServletRequest.getHeader("deviceId");
        if (deviceId == null)
            throw new RuntimeException("Error occurred: DeviceId not found");
        return deviceId;
    }

    /**
     * Map which contains, mail sending details
     *
     * @return Map
     */
    public Map<String, String> prepareSendMailMap(String mailTo, MailType mailType) {
        Map<String, String> map = new HashMap<>();
        map.put("mailFrom", environment.getProperty("spring.mail.username"));
        map.put("isMultipart", "false");
        map.put("mailTo", mailTo);
        switch (mailType) {
            case MailType.NEW_DEVICE_LOGIN_MAIL:
                map.put("subject", NEW_DEVICE_LOGIN_MAIL_SUBJECT);
                break;
            case MailType.FORGOT_PASSWORD_MAIL:
                map.put("subject", environment.getProperty("forgot.password.subject"));
                break;
            case MailType.VALIDATE_SIGNUP_MAIL:
                map.put("subject", VALIDATE_SIGN_UP_MAIL_SUBJECT);
                break;
        }
        return map;
    }

    /**
     * Get user details from cache/DB
     *
     * @param email to get details with
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public User getUserDetails(String email) {
        User userEntity = null;
        try {
//          Get user details via cache
            Map<String, Object> map = new HashMap<>();
            map.put(email + CACHE_USER_DETAILS, null);
            User user = (User) cacheService.cacheOps(map, CacheService.CacheOperation.FETCH).getFirst();
            if (user != null) {
                userEntity = user;
            } else {
//              Either from DB
                userEntity = userRepository.findActiveUserByEmail(email);
            }
        } catch (Exception e) {
            LOGGER.error("Error while getting user details: {}", e.getMessage());
        }
        return userEntity;
    }

    /**
     * Get device details from cache/DB
     *
     * @param userId to get details with
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Device getDeviceDetails(Long userId, String deviceId) {
        Device deviceEntity = null;
        try {
//          Get user details via cache
            Map<String, Object> map = new HashMap<>();
            map.put(userId + CACHE_DEVICE_DETAILS + "#" + deviceId, null);
            Device device = (Device) cacheService.cacheOps(map, CacheService.CacheOperation.FETCH).getFirst();
            if (device != null) {
                deviceEntity = device;
            } else {
//              Either from DB
                deviceEntity = deviceRepository.existsByDeviceIdAndUserId(userId, deviceId);
            }
        } catch (Exception e) {
            LOGGER.error("Error while getting device details: {}", e.getMessage());
        }
        return deviceEntity;
    }

    /**
     * Insert device details into DB and Cache
     *
     * @param device to get details with
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Device insertDeviceDetails(Device device) {
        Device deviceEntity = null;
        try {
            deviceEntity = deviceRepository.save(device);
            Map<String, Object> map = new HashMap<>();
            map.put(device.getUser().getUserId() + CACHE_DEVICE_DETAILS + "#" + deviceEntity.getId(), deviceEntity);
            cacheService.cacheOps(map, CacheService.CacheOperation.INSERT);
        } catch (Exception e) {
            LOGGER.error("Error while inserting device details: {}", e.getMessage());
            throw e;
        }
        return deviceEntity;
    }

    /**
     * Insert user details into DB and Cache
     *
     * @param user to get details with
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User insertUserDetails(User user) {
        User userEntity = null;
        try {
            userEntity = userRepository.save(user);
            Map<String, Object> map = new HashMap<>();
            map.put(user.getEmail() + CACHE_USER_DETAILS, userEntity);
            cacheService.cacheOps(map, CacheService.CacheOperation.INSERT);
        } catch (Exception e) {
            LOGGER.error("Error while inserting user details: {}", e.getMessage());
            throw e;
        }
        return userEntity;
    }

    /**
     * Insert OTP for 2-factor auth
     *
     * @param otp   cache value
     * @param email cache key
     * @return Boolean
     */
    public Boolean insertTwoFactorAuthOTP(String otp, String email) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put(email + TWO_FACTOR_OTP_CACHE, otp);
            cacheService.cacheOps(map, CacheService.CacheOperation.INSERT);
        } catch (Exception e) {
            LOGGER.error("Error while inserting 2 factor cache details: {}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Get OTP for 2-factor auth
     *
     * @param email cache key
     * @return Boolean
     */
    public String getTwoFactorAuthOTP(String email) {
        String otp = null;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put(email + TWO_FACTOR_OTP_CACHE, null);
            otp = (String) cacheService.cacheOps(map, CacheService.CacheOperation.FETCH).getFirst();
        } catch (Exception e) {
            LOGGER.error("Error while getting 2 factor cache details: {}", e.getMessage());
        }
        return otp;
    }


    /**
     * General method to check user exists
     *
     * @param user get email
     * @return Boolean
     */
    public Boolean isUserExists(UserDTO user) {
        LOGGER.debug("Checking if user already exists");
        return userRepository.existsByEmail(user.getEmail());
    }

}
