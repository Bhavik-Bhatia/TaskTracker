package com.ab.tasktracker.service;

import com.ab.tasktracker.config.TypesenseConfig;
import com.ab.tasktracker.dto.TaskDTO;
import com.ab.tasktracker.entity.Task;
import com.ab.tasktracker.entity.User;
import com.ab.tasktracker.helper.TaskHelper;
import com.ab.tasktracker.repository.TaskRepository;
import com.ab.tasktracker.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ab.tasktracker.constants.TaskTrackerConstants.CACHE_USER_DETAILS;
import static com.ab.tasktracker.service.CacheService.CacheOperation.FETCH;
import static com.ab.tasktracker.service.CacheService.CacheOperation.INSERT;

@Service
public class APITestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(APITestService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private APITestService testService;

    @PersistenceContext(name = "entityManagerFactory")
    private EntityManager entityManager;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private TypesenseConfig typesenseConfig;

    @Autowired
    private TaskHelper taskHelper;

    @Autowired
    private TypesenseService typesenseService;

    @Autowired
    private TaskRepository taskRepository;

    public void callTransService(int testType) throws InterruptedException {
        switch (testType) {
            case 1 -> testService.transRollbackTestGet();
            case 2 -> testService.transNoRollbackTestGet();
            case 3 -> testService.transNestedBackTestGet();
            case 4 -> testService.transIsolationTestGet();
            case 5 -> testService.transIsolationTestUpdate();
            case 6 -> testService.transNonRepeatableTestFetch();
            case 7 -> testService.transNonRepeatableTestUpdate();
            case 8 -> testService.parentReadOnlyTest();
        }
    }

    public void callCacheService(int testType) throws InterruptedException {
        switch (testType) {
            case 1 -> testService.cacheTestGetDataFromDB();
            case 2 -> testService.cacheTestGetDataFromCache();
        }
    }

    public void callTypesenseService(int testType) throws InterruptedException {
        switch (testType) {
            case 1 -> testService.typesenseTestMakeCollection();
            case 2 -> testService.typesenseTestGetAllData();
            case 3 -> testService.typesenseTestInsert();
            case 4 -> testService.typesenseTestFetchAllTasks();
        }
    }


    /**
     * Rollback with @Transaction
     */
    @Transactional
    public void transRollbackTestGet() {
//      Roll Back Scenario
        User user = userRepository.findById(1L).orElse(null);
        if (user != null) {
            user.setMobileNumber("7069096439");
            userRepository.save(user);
        }
        throw new RuntimeException("Rollback Scenario");
    }

    /**
     * NoRollbackFor RuntimeException with @Transaction
     */
    @Transactional(noRollbackFor = {RuntimeException.class})
    public void transNoRollbackTestGet() {
//      No Roll Back Scenario
        User user = userRepository.findById(1L).orElse(null);
        if (user != null) {
            user.setMobileNumber("000000000");
            userRepository.save(user);
        }
        throw new RuntimeException("No Rollback Scenario");
    }

    /**
     * Nested Transactions Scenario when exception is caught in child
     * DEFAULT - (Child was rollback but parent not)
     * NESTED - (No rollback)
     */
    @Transactional
    public void transNestedBackTestGet() {
        User user = userRepository.findById(1L).orElse(null);
        user.setEmail("bhavikbhatia9@gmail.com");
        userRepository.save(user);
        testService.childTestGet();
    }

    /**
     * Exception should cause a Rollback (With try Catch no rollback occurred)
     */
    @Transactional(propagation = Propagation.NESTED)
    private void childTestGet() {
        User user = userRepository.findById(1L).orElse(null);
        if (user != null) {
            user.setMobileNumber("7069096439");
            userRepository.save(user);
        }
        throw new RuntimeException("Nested Transaction Scenario");
    }

    /**
     * Isolation Committed Level with @Transaction
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void transIsolationTestGet() {
        List<User> user = userRepository.findAll();
        LOGGER.debug(String.valueOf(user.size()));
        LOGGER.debug(String.valueOf(user.get(user.size() - 1).getEmail()));
    }

    /**
     * Isolation Committed Level with @Transaction
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void transIsolationTestUpdate() throws InterruptedException {
        User user = User.builder().userName("JoshH2").email("JoshH2@yopmail.com").hashedPassword("maksadnahibhulna").isDeleted(false).mobileNumber("97764788668").build();
        userRepository.save(user);
        LOGGER.debug(String.valueOf(user));
    }

    public void parentReadOnlyTest() {
        testService.transReadOnly();
        testService.trans2ReadOnly();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public void transReadOnly() {
        User user = userRepository.findById(2L).orElse(null);
        if (user != null) {
            user.setUserName("OneMaksad");
            userRepository.save(user);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void trans2ReadOnly() {
        User user = userRepository.findById(2L).orElse(null);
        if (user != null) {
            user.setMobileNumber("Two");
            userRepository.save(user);
        }
    }


    /**
     * Non Repeatable Read Level Problem
     */
    public void transNonRepeatableTestFetch() throws InterruptedException {
        User user = entityManager.find(User.class, 2L);
        LOGGER.debug(String.valueOf(user.getEmail()));
//      To clear it from cache
        entityManager.detach(user);
        User user2 = entityManager.find(User.class, 2L);
        LOGGER.debug(String.valueOf(user2.getEmail()));
    }

    /**
     * Update operation to test Non Repeatable Read Level Problem
     */
    public void transNonRepeatableTestUpdate() throws InterruptedException {
        User user = userRepository.findById(2L).orElse(null);
        if (user != null) {
            user.setMobileNumber("00000000000");
            userRepository.save(user);
        }
    }

    public void cacheTestGetDataFromDB() {
//      Get from DB save to Cache
        User user = userRepository.findById(2L).orElse(null);
        Map<String, Object> map = new HashMap<>();
        map.put(user.getEmail() + CACHE_USER_DETAILS, user);
        cacheService.cacheOps(map, INSERT);
        LOGGER.debug(user.toString());
    }

    public void cacheTestGetDataFromCache() {
//      Get Data from Cache
        Map<String, Object> map = new HashMap<>();
        map.put("maksad@yopmail.com" + CACHE_USER_DETAILS, null);
        User user = (User) cacheService.cacheOps(map, FETCH).getFirst();
        LOGGER.debug(user.toString());
    }

    public void typesenseTestMakeCollection() {
        try {
            typesenseConfig.makeInitialCollections(typesenseConfig.getTypeSenseClient());
            LOGGER.debug(typesenseConfig.getTypeSenseClient().collections("task").toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void typesenseTestGetAllData() {
        try {
            LOGGER.debug(typesenseConfig.getTypeSenseClient().collections("task").documents().export());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void typesenseTestInsert() {
        try {
            Task task = taskRepository.findById(21L).orElse(null);
            Map<String, Object> map1 = taskHelper.getMapFromTask(task);
            typesenseService.insertDataToCollection("task", map1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void typesenseTestFetchAllTasks() {
        try {
            List<Task> task = taskRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
