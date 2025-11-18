package com.general.citas.security.AccountLocker;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "login")
public class AccLockProperties {

    private int maxFailedAttempts;
    private int failedWindowMinutes;
    private int lockDurationMinutes;

    public int getMaxFailedAttempts() { return maxFailedAttempts; }
    public void setMaxFailedAttempts(int maxFailedAttempts) { this.maxFailedAttempts = maxFailedAttempts; }

    public int getFailedWindowMinutes() { return failedWindowMinutes; }
    public void setFailedWindowMinutes(int failedWindowMinutes) { this.failedWindowMinutes = failedWindowMinutes; }

    public int getLockDurationMinutes() { return lockDurationMinutes; }
    public void setLockDurationMinutes(int lockDurationMinutes) { this.lockDurationMinutes = lockDurationMinutes; }

}
