package com.ab.tasktracker.security.filter;

import com.ab.tasktracker.security.TaskTrackerServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class XSSFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XSSFilter.class);

    @Value("${xss.invalid.literals}")
    private String[] invalidLiterals;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
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

    private void validateXSS(String input) throws Exception {
        if (input != null) {
            for (int i = 0; i < invalidLiterals.length; i++) {
                String invalidLiteralMessage = "Invalid characters entered. HTML Tags or javascript is not allowed in request input!";
                if (input.toLowerCase().contains(invalidLiterals[i].toLowerCase())) {
                    LOGGER.error(invalidLiteralMessage);
                    throw new Exception(invalidLiteralMessage);
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
}
