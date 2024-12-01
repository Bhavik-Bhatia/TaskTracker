package com.ab.tasktracker.service;

import com.ab.tasktracker.helper.TaskHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class GlobalHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskHelper.class);

    /**
     * To Compare Dates
     *
     * @param largerZonedDateTime  Example - June 31
     * @param smallerZonedDateTime Example - June 20
     * @return boolean if larger date is indeed larger than smaller date return true
     */
    public boolean compareDate(ZonedDateTime largerZonedDateTime, ZonedDateTime smallerZonedDateTime) {
        return largerZonedDateTime.isAfter(smallerZonedDateTime);
    }
}
