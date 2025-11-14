package ru.hackathon.max.snaptask_bot.domain.model.task;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hackathon.max.snaptask_bot.domain.model.user.UserEntity;

import java.time.Instant;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;


    @Column(name = "action_text", nullable = false, columnDefinition = "TEXT")
    private String actionText;


    @Column(name = "deadline")
    private Instant deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.TODO;;

    @Column(name = "recurrence_rule")
    private String recurrenceRule;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}