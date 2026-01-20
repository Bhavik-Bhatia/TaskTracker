package com.ab.tasktracker.security.filter;

import com.ab.tasktracker.annotation.Log;
import com.ab.tasktracker.exception.AppException;
import com.ab.tasktracker.exception.ErrorCode;
import com.ab.tasktracker.security.TaskTrackerServletRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;

@Component
@Order(2)
public class XSSFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XSSFilter.class);

    @Value("${xss.invalid.literals}")
    private String[] invalidLiterals;

    private void validateXSS(String input) throws Exception {
        if (input != null) {
            for (int i = 0; i < invalidLiterals.length; i++) {
                String invalidLiteralMessage = "Invalid characters entered. HTML Tags or javascript is not allowed in request input!";
                if (input.toLowerCase().contains(invalidLiterals[i].toLowerCase())) {
                    LOGGER.error(invalidLiteralMessage);
                    throw new AppException(ErrorCode.XSS_EXCEPTION, invalidLiteralMessage);
                }
            }
        }
    }

    private String getPayload(HttpServletRequest httpServletRequest) throws IOException {
        String payload;
        StringBuilder request = new StringBuilder();
        try (BufferedReader bufferedReader = httpServletRequest.getReader()) {
            while ((payload = bufferedReader.readLine()) != null) {
                request.append(payload);
            }
        }
        return request.toString();
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

        TaskTrackerServletRequestWrapper taskTrackerServletRequestWrapper = new TaskTrackerServletRequestWrapper(request);
        if (!request.getRequestURI().contains("test")) {
            String payload = getPayload(taskTrackerServletRequestWrapper);
            try {
                validateXSS(payload);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        filterChain.doFilter(taskTrackerServletRequestWrapper, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
