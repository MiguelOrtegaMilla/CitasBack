package com.general.citas.security.AccountLocker;


import java.time.LocalDateTime;

import com.general.citas.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "account_locks")
public class AccountLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false , name = "bloqueada")
    @Builder.Default
    private boolean isLocked = false;

    @Builder.Default
    @Column(nullable = false , name = "intentos" )
    private int failedAttempts = 0;

    @Column(name = "ultimo_intento")
    private LocalDateTime lastAttemptAt;

    @Column(name = "fecha_desbloqueo")
    private LocalDateTime unlocksAt;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false , unique = true)
    private User user;

}
