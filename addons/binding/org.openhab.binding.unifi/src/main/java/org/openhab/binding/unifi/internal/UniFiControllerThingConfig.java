/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.unifi.internal.handler.UniFiControllerThingHandler;

/**
 * The {@link UniFiControllerThingConfig} encapsulates all the configuration options for an instance of the
 * {@link UniFiControllerThingHandler}.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiControllerThingConfig {

    private String host = "unifi";

    private int port = 8443;

    private String username;

    private String password;

    private int refresh = 10;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getRefresh() {
        return refresh;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(host) && StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password);
    }

    @Override
    public String toString() {
        return "UniFiControllerConfig{host = " + host + ", port = " + port + ", username = " + username
                + ", password = *****, refresh = " + refresh + "}";
    }
}
