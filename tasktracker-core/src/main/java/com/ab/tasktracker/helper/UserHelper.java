package com.ab.tasktracker.helper;

import com.ab.cache_service.service.CacheService;
import com.ab.tasktracker.entity.Device;
import com.ab.tasktracker.entity.User;
import com.ab.tasktracker.repository.DeviceRepository;
import com.ab.tasktracker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

import static com.ab.tasktracker.constants.TaskTrackerConstants.*;

/**
 * Helper class for User Service
 */
@Component
@AllArgsConstructor
public class UserHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHelper.class);

    private CacheService cacheService;

    private UserRepository userRepository;

    private DeviceRepository deviceRepository;

    /**
     * Todo : This will be used when UI support is provided
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
}
