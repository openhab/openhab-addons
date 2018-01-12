/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.config;

import org.apache.commons.lang.StringUtils;

/**
 * Configuration settings for an {@link org.openhab.binding.lutron.handler.IPBridgeHandler}.
 *
 * @author Allan Tong - Initial contribution
 */
public class IPBridgeConfig {
    private String ipAddress;
    private String user;
    private String password;

    public boolean sameConnectionParameters(IPBridgeConfig config) {
        return StringUtils.equals(this.ipAddress, config.ipAddress) && StringUtils.equals(this.user, config.user)
                && StringUtils.equals(this.password, config.password);
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
