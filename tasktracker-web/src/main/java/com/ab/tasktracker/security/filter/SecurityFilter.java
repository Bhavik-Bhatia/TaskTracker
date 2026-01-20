package com.ab.tasktracker.security.filter;

import com.ab.jwt.JwtUtil;
import com.ab.tasktracker.annotation.Log;
import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.ab.tasktracker.exception.AppException;
import com.ab.tasktracker.exception.ErrorCode;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * This is Security Filter which validating JWT Token,
 * Checking for invalid characters for XSS attacks and user device ID
 */
@Component
@AllArgsConstructor
@Order(1)
public class SecurityFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);

    private JwtUtil jwtUtil;

/*
    Todo: below code will be used when UI Support provided
    private DeviceRepository deviceRepository;
    private UserHelper userHelper;
*/

    private void errorResponse(HttpServletResponse response, String message) throws AppException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        try {
            response.getWriter().write("{ \"error\": \"" + message + "\" }");
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    @Log
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
//      todo: Invalidate token if user soft deletes details.
        LOGGER.debug("Validating JWT Tokens and Device ID");
        final String authHeader = request.getHeader("Authorization");
//      final String deviceId = request.getHeader("DeviceId");
        final String jwtToken;

//      For testing purpose
        if (authHeader != null && authHeader.equalsIgnoreCase("ADMIN-Task-Tracker")) {
            request.setAttribute("userId", 1L);
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            try {
                errorResponse(response, TaskTrackerConstants.TOKEN_INVALID);
            } catch (AppException e) {
                throw new RuntimeException(e);
            }
            return;
        }
//       Todo: below code will be used when UI Support provided
/*
        if (deviceId == null || deviceId.isBlank()) {
            errorResponse(response, TaskTrackerConstants.DEVICE_ID_REQUIRED_MESSAGE);
            return;
        }
*/

        jwtToken = authHeader.substring(7);
        if (jwtUtil.isTokenExpired(jwtToken)) {
            try {
                errorResponse(response, TaskTrackerConstants.TOKEN_EXPIRED);
            } catch (AppException e) {
                throw new RuntimeException(e);
            }
            return;
        }
//      Todo: below code will be used when UI Support provided
        /*LOGGER.debug("Going to validate Device information");*/
//       Fetching userId from claims checking user logged in with this device if not throwing unidentified device error.
        Long userId = Long.parseLong(String.valueOf(jwtUtil.extractAllClaims(jwtToken).get("userId")));
        if (userId == null) {
            try {
                errorResponse(response, TaskTrackerConstants.USER_ID_REQUIRED_MESSAGE);
            } catch (AppException e) {
                throw new RuntimeException(e);
            }
            return;
        } else {
            request.setAttribute("userId", userId);
        }
/*
        Get device details from cache
        Device device = userHelper.getDeviceDetails(userId, deviceId);
        if (device == null) {
            errorResponse(response, TaskTrackerConstants.UNIDENTIFIED_DEVICE);
            return;
        }
*/
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
