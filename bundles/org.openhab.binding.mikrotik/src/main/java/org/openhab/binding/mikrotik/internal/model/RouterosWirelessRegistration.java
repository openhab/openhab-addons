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
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openhab.binding.mikrotik.internal.util.Converter;

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

    public int getRxSignal() {
        String signalValue = propMap.getOrDefault("signal-strength", "0@hz").split("@")[0];
        return Integer.parseInt(signalValue);
    }

    public @Nullable DateTime getLastActivity() {
        if (propMap.containsKey("last-activity")) {
            Period lastActiveBack = Converter.fromRouterosPeriod(propMap.get("last-activity"));
            return DateTime.now().minus(lastActiveBack);
        }
        return null;
    }
}
