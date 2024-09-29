/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.config;

/**
 *
 * This class acts simply a structure for holding the thing configuration.
 *
 * @author Marco Meyer - Initial contribution
 */
public class RobonectConfig {

    private String host;

    private String user;

    private String password;

    private int pollInterval;

    private int offlineTimeout;

    private String timezone;

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

    public int getOfflineTimeout() {
        return offlineTimeout;
    }

    public String getTimezone() {
        return timezone;
    }
}
