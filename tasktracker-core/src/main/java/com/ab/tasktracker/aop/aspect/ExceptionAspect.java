package com.ab.tasktracker.aop.aspect;

import com.ab.tasktracker.exception.AppException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAspect.class);

    @AfterThrowing(pointcut = "execution(* com.ab.tasktracker..*(..))", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Throwable e) {
        Object[] objects = joinPoint.getArgs();
        StringBuilder stringBuilder = new StringBuilder();
        String traceId = "---";
        for (Object obj : objects) {
            stringBuilder.append(obj).append(", ");
        }
        if (stringBuilder.isEmpty()) {
            stringBuilder.append("No Params");
        }
        if (e instanceof AppException) {
            traceId = ((AppException) e).getTraceID();
        }
        LOGGER.error("ExceptionAspect: Exception has been thrown in the class {} and the method is {}, method args: {}, error message is: {}, trace ID is: {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), stringBuilder, e.getMessage(), traceId, e);

        //TODO: Can be used for metrics purpose with spring actuator for critical exceptions, alerts etc.
    }

}
