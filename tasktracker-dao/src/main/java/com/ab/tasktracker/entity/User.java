package com.ab.tasktracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "tasktracker_service_user_ms_tbl")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Component
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "hashed_password")
    private String hashedPassword;

    @Column(name = "created_date", updatable = false)
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private ZonedDateTime createdDate;

    @Column(name = "updated_date")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private ZonedDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        createdDate = ZonedDateTime.now();
        updatedDate = createdDate;
    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = ZonedDateTime.now();
    }

}
