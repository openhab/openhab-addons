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
package org.openhab.binding.milight.internal.handler;

/**
 * The configuration for all bridge types.
 *
 * @author David Graeff - Initial contribution
 */
public class BridgeHandlerConfig {
    public static final String CONFIG_BRIDGE_ID = "bridgeid";
    String bridgeid = "";
    public static final String CONFIG_HOST_NAME = "host";
    String host = "";
    int refreshTime = 5000;
    int port = 0;
    int passwordByte1 = 0;
    int passwordByte2 = 0;
    int repeat = 3;
    int delayTime = 100;
}
