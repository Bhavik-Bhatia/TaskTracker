package com.ab;

import com.ab.tasktracker.config.TypesenseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;


//TODO 7: Use AOP and make annotations for Logging, Exception Handling, Security etc. Do not repeat code.
@SpringBootApplication
@EnableConfigurationProperties
@EnableFeignClients
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@EnableCaching
public class TaskTrackerApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTrackerApplication.class);


    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication.run(TaskTrackerApplication.class, args);
        TypesenseConfig typesenseConfig = ctx.getBean(TypesenseConfig.class);
        typesenseConfig.getTypeSenseClient();
        LOGGER.info("Typesense Bean: {}", typesenseConfig);
    }
}