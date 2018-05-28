/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal;

import org.eclipse.jetty.util.StringUtil;

/**
 * The {@link RobonectEndpoint} is holds the information required to a Robonect endpoint.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class RobonectEndpoint {
    
    private final String ipAddress;
    
    private final String user;
    
    private final String password;
    
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
