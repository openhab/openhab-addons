/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
