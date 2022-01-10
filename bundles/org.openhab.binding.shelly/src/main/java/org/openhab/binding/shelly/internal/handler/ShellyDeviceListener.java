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
package org.openhab.binding.shelly.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link DeviceUpdateListener} can register on the {@link TradfriGatewayHandler} to be informed about details about
 * devices.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface ShellyDeviceListener {

    /**
     * This method is called when new device information is received.
     */
    public boolean onEvent(String ipAddress, String deviceName, String deviceIndex, String eventType,
            Map<String, String> parameters);
}
