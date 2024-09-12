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
package org.openhab.binding.irobot.internal.config;

import static org.openhab.binding.irobot.internal.IRobotBindingConstants.UNKNOWN;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IRobotConfiguration} is a class for IRobot thing configuration
 *
 * @author Pavel Fedin - Initial contribution
 * @author Alexander Falkenstern - Add supported robot type
 */
@NonNullByDefault
public class IRobotConfiguration {
    private String ipaddress = UNKNOWN;
    private String password = UNKNOWN;
    private String blid = UNKNOWN;

    public String getIpAddress() {
        return ipaddress;
    }

    public void setIpAddress(final String ipaddress) {
        this.ipaddress = ipaddress.trim();
    }

    public String getPassword() {
        return password.isBlank() ? UNKNOWN : password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getBlid() {
        return blid.isBlank() ? UNKNOWN : blid;
    }

    public void setBlid(final String blid) {
        this.blid = blid;
    }
}
