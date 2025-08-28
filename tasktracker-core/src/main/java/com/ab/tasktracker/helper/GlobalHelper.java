package com.ab.tasktracker.helper;

import com.ab.cache_service.service.CacheService;
import com.ab.jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlobalHelper {
    private final JwtUtil jwtUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalHelper.class);

    @Autowired
    public GlobalHelper(CacheService service, JwtUtil util) {
        jwtUtil = util;
    }

    /**
     * This method will be used to generate token from microservice name via Hs526 algo and key
     * hence securing internal microservice calls
     *
     * @param serviceName String
     * @return String
     */
    public String generateTokenViaSubjectForRestCall(String serviceName) {
        return jwtUtil.generateJWTToken(serviceName);
    }


}
