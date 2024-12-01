package com.ab.tasktracker.entity;

import com.ab.tasktracker.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "tasktracker_service.task_tx_tbl")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Component
@NamedEntityGraph(
        name = "Task.user",
        attributeNodes = {@NamedAttributeNode("userId"), @NamedAttributeNode("assignee")}
)
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_name")
    private String taskName;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TaskStatus taskStatus;

    @Column(name = "task_description")
    private String taskDescription;

    @Column(name = "initial_category")
    private String initialCategory;

    @Column(name = "updated_category")
    private String updatedCategory;

    @ManyToOne
    @JoinColumn(name = "assignee_user_id")
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "created_user_id")
    private User userId;

    @Column(name = "parent_task_id")
    private Long parentTaskId;

    @Column(name = "task_priority")
    private String taskPriority;

    @Column(name = "task_start_date")
    private ZonedDateTime taskStartDate;

    @Column(name = "task_due_date")
    private ZonedDateTime taskDueDate;

    @Column(name = "task_complete_date")
    private ZonedDateTime taskCompleteDate;

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
