package com.ab.tasktracker.security.filter;

import com.ab.jwt.JwtUtil;
import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.ab.tasktracker.entity.Device;
import com.ab.tasktracker.helper.UserHelper;
import com.ab.tasktracker.repository.DeviceRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * This is Security Filter which validating JWT Token,
 * Checking for invalid characters for XSS attacks and user device ID
 */
@Component
@AllArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);

    private JwtUtil jwtUtil;

//  Todo: below code will be used when UI Support provided
    private DeviceRepository deviceRepository;

    private UserHelper userHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        LOGGER.debug("Enter in SecurityFilter.doFilterInternal()");
//      todo: Invalidate token if user soft deletes details.
        LOGGER.debug("Validating JWT Tokens and Device ID");
        final String authHeader = request.getHeader("Authorization");
        final String deviceId = request.getHeader("DeviceId");
        final String jwtToken;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            errorResponse(response, TaskTrackerConstants.TOKEN_INVALID);
            return;
        }
//       Todo: below code will be used when UI Support provided
        if (deviceId == null || deviceId.isBlank()) {
            errorResponse(response, TaskTrackerConstants.DEVICE_ID_REQUIRED_MESSAGE);
            return;
        }

        jwtToken = authHeader.substring(7);
        if (jwtUtil.isTokenExpired(jwtToken)) {
            errorResponse(response, TaskTrackerConstants.TOKEN_EXPIRED);
            return;
        }
//      Todo: below code will be used when UI Support provided
        LOGGER.debug("Going to validate Device information");
//       Fetching userId from claims checking user logged in with this device if not throwing unidentified device error.
        Long userId = Long.parseLong(String.valueOf(jwtUtil.extractAllClaims(jwtToken).get("userId")));
//       Get device details from cache
        Device device = userHelper.getDeviceDetails(userId, deviceId);
        if (device == null) {
            errorResponse(response, TaskTrackerConstants.UNIDENTIFIED_DEVICE);
            return;
        }

        LOGGER.debug("Exit from SecurityFilter.doFilterInternal()");
        filterChain.doFilter(request, response);
    }

    private void errorResponse(HttpServletResponse response, String message) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        try {
            response.getWriter().write("{ \"error\": \"" + message + "\" }");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
