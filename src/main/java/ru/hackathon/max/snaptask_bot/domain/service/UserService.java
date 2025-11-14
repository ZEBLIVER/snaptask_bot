package ru.hackathon.max.snaptask_bot.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.domain.model.user.UserEntity;
import ru.hackathon.max.snaptask_bot.domain.model.user.UserStatus;
import ru.hackathon.max.snaptask_bot.domain.port.out.UserRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Возвращает сущность пользователя по его внешнему ID, или пустой Optional.
     */
    public Optional<UserEntity> findOptionalByMaxUserId(Long maxUserId) {
        return userRepository.findByMaxUserId(maxUserId);
    }

    /**
     * Находит UserEntity по внешнему ID. Используется, когда пользователь гарантированно есть.
     */
    public UserEntity findByMaxUserId(Long maxUserId) {
        return findOptionalByMaxUserId(maxUserId)
                .orElseThrow(() -> new NoSuchElementException("User Entity not found with ID: " + maxUserId));
    }

    /**
     * Получает ZoneId пользователя по его внешнему ID.
     */
    public ZoneId getUserTimezone(Long maxUserId) {
        UserEntity userEntity = findByMaxUserId(maxUserId);

        if (userEntity.getTimezoneId() == null) {
            throw new IllegalStateException("User is not fully registered. Timezone is missing for user " + maxUserId);
        }

        return ZoneId.of(userEntity.getTimezoneId());
    }

    /**
     * Создает нового пользователя с начальным статусом AWAITING_TIMEZONE.
     */
    public UserEntity registerNewUser(Long maxUserId, String maxUsername) {
        UserEntity user = new UserEntity();
        user.setMaxUserId(maxUserId);
        user.setMaxUsername(maxUsername);
        user.setCreatedAt(Instant.now());
        user.setStatus(UserStatus.AWAITING_TIMEZONE);

        return userRepository.save(user);
    }

    /**
     * Устанавливает новый статус пользователя. Используется для инициации смены часового пояса.
     */
    public UserEntity setUserStatus(Long maxUserId, UserStatus status) {
        UserEntity user = findByMaxUserId(maxUserId);
        user.setStatus(status);
        return userRepository.save(user);
    }

    /**
     * Обновляет часовой пояс пользователя и переводит его в статус REGISTERED.
     */
    public UserEntity completeRegistration(Long maxUserId, ZoneId timezone) {
        UserEntity user = findByMaxUserId(maxUserId);
        user.setTimezoneId(timezone.getId());
        user.setStatus(UserStatus.REGISTERED);

        return userRepository.save(user);
    }

    /**
     * Проверяет, зарегистрирован ли пользователь (т.е. имеет ли статус REGISTERED).
     */
    public boolean isUserFullyRegistered(Long maxUserId) {
        return findOptionalByMaxUserId(maxUserId)
                .map(user -> user.getStatus() == UserStatus.REGISTERED)
                .orElse(false);
    }

    public UserStatus getUserStatus(Long maxUserId) {
        return findOptionalByMaxUserId(maxUserId)
                .map(UserEntity::getStatus)
                .orElse(null);
    }
}