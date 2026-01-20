package com.ab.tasktracker.actuator.health;

import com.ab.tasktracker.config.TypesenseConfig;
import com.ab.tasktracker.exception.AppException;
import com.ab.tasktracker.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.typesense.api.Client;

@Component
public class TypeSenseHealthIndicator implements HealthIndicator {

    private final TypesenseConfig typesenseConfig;

    @Autowired
    public TypeSenseHealthIndicator(TypesenseConfig typesenseConfig) {
        this.typesenseConfig = typesenseConfig;
    }

    @Override
    public Health health() {
        try {
            Client client = typesenseConfig.getTypeSenseClient();
            if (client.health.retrieve().get("ok") != null)
                return Health.up().withDetail("TypeSense", "Available").build();
            else
                throw new AppException(ErrorCode.TYPE_SENSE_CONNECTION_ERROR, "Type Sense Client not available");
        } catch (Exception ex) {
            return Health.up().withDetail("TypeSense", "UnAvailable").build();
        }

    }
}
