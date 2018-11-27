/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
