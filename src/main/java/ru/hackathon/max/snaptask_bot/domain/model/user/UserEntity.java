package ru.hackathon.max.snaptask_bot.domain.model.user;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "max_user_id", unique = true, nullable = false)
    private Long maxUserId;

    @Column(name = "username")
    private String maxUsername;

    @Column(name = "timezone_id")
    private String timezoneId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}