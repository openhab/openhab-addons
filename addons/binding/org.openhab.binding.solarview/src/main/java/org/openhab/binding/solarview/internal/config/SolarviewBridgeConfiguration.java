/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarview.internal.config;

import org.eclipse.smarthome.config.core.Configuration;

/**
 * The {@link SolarviewBridgeConfiguration} is a wrapper for
 * configuration settings needed to access the {@link org.openhab.binding.solarview.handler.SolarviewBridgeHandler}
 * device.
 * <p>
 * It contains the factory default values as well.
 * <p>
 *
 * @author Guenther Schreiner - Initial contribution
 */
public class SolarviewBridgeConfiguration extends Configuration {

    public static final String BRIDGE_HOSTNAME_OR_ADDRESS = "hostName";
    public static final String BRIDGE_TCPPORT = "tcpPort";
    public static final String BRIDGE_TIMEOUT_MSECS = "timeoutMsecs";

    public String hostName;
    public int tcpPort;
    public int timeoutMsecs;

    /**
     * Default values - should not be modified
     */
    public SolarviewBridgeConfiguration() {
        hostName = "localhost";
        tcpPort = 15000;
        timeoutMsecs = 2000;
    }

}
/*
 * end-of-internal/config/SolarviewBridgeConfiguration.java
 */
