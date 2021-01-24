/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RouterosWirelessRegistration} is a model class for WiFi client data retrieced from RouterOS
 * physical wireless interface. Is a subclass of {@link RouterosRegistrationBase}.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosWirelessRegistration extends RouterosRegistrationBase {
    public RouterosWirelessRegistration(Map<String, String> props) {
        super(props);
    }

    public String getUptime() {
        return propMap.get("uptime");
    }
}
