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

@SpringBootApplication
@EnableConfigurationProperties
@EnableFeignClients
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@EnableCaching
public class TaskTrackerApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTrackerApplication.class);


    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(TaskTrackerApplication.class, args);
        TypesenseConfig typesenseConfig = context.getBean(TypesenseConfig.class);
        typesenseConfig.getTypeSenseClient();
        LOGGER.info("Typesense Bean: {}", typesenseConfig);
    }
}