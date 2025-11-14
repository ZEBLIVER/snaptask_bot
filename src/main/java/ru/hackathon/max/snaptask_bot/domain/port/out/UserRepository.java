package ru.hackathon.max.snaptask_bot.domain.port.out;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hackathon.max.snaptask_bot.domain.model.user.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByMaxUserId(Long maxUserId);
}
