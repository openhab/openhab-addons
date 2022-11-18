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
package org.openhab.binding.emby.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;

/**
 * The {@link EmbyBridgeConfiguration} class contains fields mapping thing configuration parameters.
 * 
 * @param api - This is the API key generated from EMBY used for Authorization.
 * @param buffersize - Here you can define a custom size for the websocket buffer size. Default is 10,0000
 * @param ipAddress - This is the ip address of the EMBY Server.
 * @param port - This is the port of the EMBY server.
 * @param refreshInterval - This is the refresh interval in milliseconds that will be sent to the websocket. Default is
 *            10,000
 * @param discovery - If set to false the controller will not add new things from devices to the inbox.
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyBridgeConfiguration extends Configuration {

    public String api = "";
    public int bufferSize;
    public String ipAddress = "";
    public int port;
    public int refreshInterval;
    public boolean discovery;
}
