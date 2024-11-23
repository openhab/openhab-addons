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
package org.openhab.binding.tapocontrol.internal.devices.wifi;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.api.protocol.TapoProtocolEnum;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

/**
 * The {@link TapoDeviceConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Christian Wild - Initial contribution
 */

@NonNullByDefault
public final class TapoDeviceConfiguration {
    /* THING CONFIGUTATION PROPERTYS */
    public static final String CONFIG_DEVICE_IP = "ipAddress";
    public static final String CONFIG_PROTOCOL = "protocol";
    public static final String CONFIG_HTTP_PORT = "httpPort";
    public static final String CONFIG_UPDATE_INTERVAL = "pollingInterval";
    public static final String CONFIG_BACKGROUND_DISCOVERY = "backgroundDiscovery";

    /* thing configuration parameter. */
    public String ipAddress = "";
    public String protocol = "AES";
    public int httpPort = 80;
    public int pollingInterval = 30;
    public boolean backgroundDiscovery = false;

    /**
     * Check for configuration errors
     * 
     * @return true if config is valid
     * @throws TapoErrorHandler
     */
    public boolean checkConfig() throws TapoErrorHandler {
        if (!ipAddress.matches(IPV4_REGEX)) {
            throw new TapoErrorHandler(ERR_CONFIG_IP);
        }
        try {
            TapoProtocolEnum.valueOfString(protocol);
        } catch (Exception e) {
            throw new TapoErrorHandler(ERR_CONFIG_PROTOCOL);
        }
        return true;
    }
}
