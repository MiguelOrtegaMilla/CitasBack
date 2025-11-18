package com.general.citas.security.AccountLocker;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.general.citas.model.User;
import com.general.citas.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AccLockService {

    @Autowired 
    private  AccLockRepository accLockRepository;

    @Autowired
    private AccLockProperties accLockProperties;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void registerFailedAttempt(String userUuid) {

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        AccountLock lock = accLockRepository.findByUser(user)
                .orElseGet(() -> AccountLock.builder()
                        .user(user)
                        .failedAttempts(0)
                        .isLocked(false)
                        .build());

        LocalDateTime now = LocalDateTime.now();

        
        // Si el último intento fue hace más tiempo que la ventana T -> reiniciamos contador
        if (lock.getLastAttemptAt() == null ||
            lock.getLastAttemptAt().isBefore(now.minusMinutes(accLockProperties.getFailedWindowMinutes()))) {
            lock.setFailedAttempts(1);
        } else {
            lock.setFailedAttempts(lock.getFailedAttempts() + 1);
        }

        lock.setLastAttemptAt(now);

        // Si supera el umbral -> bloquear y establecer expires (null = bloqueo indefinido)
        if (lock.getFailedAttempts() >= accLockProperties.getMaxFailedAttempts()) {
            lock.setLocked(true);
            if (accLockProperties.getLockDurationMinutes() > 0) {
                lock.setUnlocksAt(now.plusMinutes(accLockProperties.getLockDurationMinutes()));
            } else {
                lock.setUnlocksAt(null);
            }
        }

        accLockRepository.save(lock);
    }
    

    @Transactional
    public void registerSuccessfulLogin(String userUuid) {
        userRepository.findByUuid(userUuid).ifPresent(user ->
                accLockRepository.findByUser(user).ifPresent(lock -> {
                    lock.setFailedAttempts(0);
                    lock.setLocked(false);
                    lock.setUnlocksAt(null);
                    lock.setLastAttemptAt(null);
                    accLockRepository.save(lock);
                }));
    }

    @Transactional
    public boolean isUserLocked(String userUuid) {

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Optional<AccountLock> optionalLock = accLockRepository.findByUser(user);
        if (optionalLock.isEmpty()) return false;

        AccountLock lock = optionalLock.get();

        if (!lock.isLocked()) return false;

            LocalDateTime unlocksAt = lock.getUnlocksAt();
        if (unlocksAt != null) {
            if (LocalDateTime.now().isAfter(unlocksAt)) {
                // desbloqueo automático
                lock.setLocked(false);
                lock.setFailedAttempts(0);
                lock.setUnlocksAt(null);
                lock.setLastAttemptAt(null);
                accLockRepository.save(lock);
                return false;
            } else {
                return true;
            }
        } else {
            // bloqueo indefinido (manual)
            return true;
        }

    }

    @Transactional
    public void unlockUser(String userUuid) {
        userRepository.findByUuid(userUuid).ifPresent(user ->
                accLockRepository.findByUser(user).ifPresent(lock -> {
                    lock.setLocked(false);
                    lock.setFailedAttempts(0);
                    lock.setUnlocksAt(null);
                    lock.setLastAttemptAt(null);
                    accLockRepository.save(lock);
                }));
    }

}
