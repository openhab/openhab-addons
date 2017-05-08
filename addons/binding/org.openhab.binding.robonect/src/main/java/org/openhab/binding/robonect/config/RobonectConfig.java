package org.openhab.binding.robonect.config;

public class RobonectConfig {
    
    private String host;
    
    private String user;
    
    private String password;
    
    private int pollInterval;

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getPollInterval() {
        return pollInterval;
    }
}
