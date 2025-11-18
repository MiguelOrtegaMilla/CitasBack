package com.general.citas.security.RateLimiter;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimitsProperties {

    private Limit defaultLimit = new Limit();

    private Map<String, Limit> overrides; // key: endpoint path

    public static class Limit {

        private int requests = 100;
        private long windowSeconds = 60;

        public int getRequests () {return requests;}
        public void setRequests(int requests) {this.requests = requests;}
        public long getWindowSeconds() {return windowSeconds;}
        public void setWindowSeconds(long windowSeconds) {this.windowSeconds = windowSeconds;}
    }


    public Limit getDefaultLimit(){return defaultLimit;}
    public void setDefaultLimit(Limit defaultLimit){this.defaultLimit = defaultLimit;} 

    public Map<String, Limit> getOverrides (){return overrides;}
    public void setOverrides (Map<String, Limit> overrides){this.overrides = overrides;}
}
