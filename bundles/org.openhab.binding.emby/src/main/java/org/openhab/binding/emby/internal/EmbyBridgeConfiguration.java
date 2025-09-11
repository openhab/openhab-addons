/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;

/**
 * The {@link EmbyBridgeConfiguration} class contains fields mapping thing configuration parameters.
 * 
 * @param api - This is the API key generated from EMBY used for Authorization.
 * @param ipAddress - IP address of the EMBY Server.
 * @param port - Port of the EMBY Server. Default is 8096.
 * @param refreshInterval - Refresh interval in milliseconds. Default is 10,000.
 * @param discovery - Enable/disable device auto-discovery. Default is true (enabled).
 * 
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyBridgeConfiguration extends Configuration {

    public String api = "";
    public String ipAddress = "";
    public int port = 8096; // Default server port
    public int refreshInterval = 10000; // Default refresh interval
    public boolean discovery = true; // Discovery enabled by default
}
