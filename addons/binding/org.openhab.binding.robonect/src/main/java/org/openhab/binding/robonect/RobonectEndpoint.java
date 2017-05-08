package org.openhab.binding.robonect;

import org.eclipse.jetty.util.StringUtil;

public class RobonectEndpoint {
    
    private String ipAddress;
    
    private String user;
    
    private String password;
    
    private boolean useAuthentication = false;

    public RobonectEndpoint(String ipAddress, String user, String password) {
        this.ipAddress = ipAddress;
        this.user = user;
        this.password = password;
        this.useAuthentication = StringUtil.isNotBlank(user) && StringUtil.isNotBlank(password);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }
}
