package com.ab.tasktracker.repository;

import com.ab.tasktracker.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Boolean existsByDeviceId(String deviceId);

    @Query(value = "select * from tasktracker_service_device_ms_tbl where user_id=?1 And device_id=?2", nativeQuery = true)
    Device existsByDeviceIdAndUserId(Long userId, String deviceId);
}
