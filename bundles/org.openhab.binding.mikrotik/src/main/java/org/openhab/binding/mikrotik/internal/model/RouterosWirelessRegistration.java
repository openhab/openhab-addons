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
package org.openhab.binding.mikrotik.internal.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
        String signalValue = getProp("signal", "0");
        if (signalValue.isEmpty()) {
            signalValue = getProp("signal-strength", "0@hz").split("@")[0];
        } else {
            int value = Integer.parseInt(signalValue);
            if (value < -80) {
                return 0;
            } else if (value < -75) {
                return 1;
            } else if (value < -68) {
                return 2;
            } else if (value < -55) {
                return 3;
            } else {
                return 4;
            }
        }
        return Integer.parseInt(signalValue);
    }

    public @Nullable LocalDateTime getLastActivity() {
        return Converter.routerosPeriodBack(getProp("last-activity"));
    }
}
