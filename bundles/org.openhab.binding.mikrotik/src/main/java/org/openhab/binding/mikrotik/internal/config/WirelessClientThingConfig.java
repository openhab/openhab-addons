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
package org.openhab.binding.mikrotik.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WirelessClientThingConfig} class contains fields mapping thing configuration parameters for
 * WiFi client thing.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class WirelessClientThingConfig implements ConfigValidation {
    public String mac = "";
    public String ssid = "";
    public int considerContinuous = 180;

    @Override
    public boolean isValid() {
        return !mac.isBlank() && considerContinuous > 0;
    }

    @Override
    public String toString() {
        return String.format("WirelessClientThingConfig{mac=%s, ssid=%s, considerContinuous=%ds}", mac, ssid,
                considerContinuous);
    }
}
