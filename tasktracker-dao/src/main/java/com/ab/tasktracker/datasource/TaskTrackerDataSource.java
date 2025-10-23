package com.ab.tasktracker.datasource;

import com.ab.tasktracker.properties.JPAProperties;
import com.ab.tasktracker.properties.TaskTrackerDataSourceProperties;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory", basePackages = {"com.ab.tasktracker.repository"}, transactionManagerRef = "transactionManager")
@EnableTransactionManagement
public class TaskTrackerDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTrackerDataSource.class);

    private TaskTrackerDataSourceProperties taskTrackerDataSourceProperties;

    private JPAProperties jpaProperties;

    @Autowired
    public TaskTrackerDataSource(TaskTrackerDataSourceProperties dataSourceProperties,JPAProperties properties){
        jpaProperties = properties;
        taskTrackerDataSourceProperties = dataSourceProperties;
    }

    @Value("${current.database}")
    private String currentDatabase;

    @Autowired
    private Environment environment;

    @Bean(name = {"tasktrackerDataSource"})
    public DataSource dataSource() {
        DataSource datasource = DataSourceBuilder.create().
                url(environment.getProperty(String.format("spring.datasource.%s.url", currentDatabase))).
                username(environment.getProperty(String.format("spring.datasource.%s.username", currentDatabase))).
                password(environment.getProperty(String.format("spring.datasource.%s.password", currentDatabase))).build();
        LOGGER.debug("TaskTrackerDataSource.datasource config url :{}", environment.getProperty(String.format("spring.datasource.%s.url", currentDatabase)));
        return datasource;
    }


    @Bean(name = {"entityManagerFactory"})
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = builder.dataSource(dataSource).packages("com.ab.tasktracker.entity").persistenceUnit("tasktracker").properties(taskTrackerDataSourceProperties.getJPAProperties(jpaProperties)).build();
        factory.setJpaVendorAdapter(vendorAdapter);
        LOGGER.debug("TaskTrackerDataSource.entityManagerFactory config");
        return factory;
    }

    @Bean(name = {"transactionManager"})
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        LOGGER.debug("TaskTrackerDataSource.transactionManager config");
        return new JpaTransactionManager(entityManagerFactory);
    }
}
