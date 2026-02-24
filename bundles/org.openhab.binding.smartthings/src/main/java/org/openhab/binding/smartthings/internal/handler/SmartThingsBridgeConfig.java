/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * Configuration data for SmartThings
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartThingsBridgeConfig {

    public String appName = "";
    public String clientId = "";
    public String clientSecret = "";
    public int pollingTime = -1;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("appName = ").append(appName);
        sb.append("clientId = ").append(clientId);
        sb.append("clientSecret = ").append(clientSecret);
        sb.append("pollingTime = ").append(pollingTime);
        return sb.toString();
    }
}
