/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration data for Smartthings hub
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsBridgeConfig {

    /**
     * IP address of smartthings hub
     */
    public String smartthingsIp = "";

    /**
     * Port number of smartthings hub
     */
    public int smartthingsPort = -1;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("smartthingsIp = ").append(smartthingsIp);
        sb.append(", smartthingsPort = ").append(smartthingsPort);
        return sb.toString();
    }
}
