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
package org.openhab.binding.ferroamp.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EhubParameters} is responsible for all parameters regarded to EHUB
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class EhubParameters {
    public String jsonPostEhub;

    public EhubParameters(String jsonPostEhub) {
        this.jsonPostEhub = jsonPostEhub;
    }

    public static List<String> getChannelParametersEhub() {
        final List<String> channelParametersEhub = Arrays.asList("grid-frequency", "ace-current-l1", "ace-current-l2",
                "ace-current-l3", "external-voltage-l1", "external-voltage-l2", "external-voltage-l3",
                "inverter-rms-current-l1", "inverter-rms-current-l2", "inverter-rms-current-l3",
                "inverter-current-reactive-l1", "inverter-current-reactive-l2", "inverter-current-reactive-l3",
                "inverter-current-active-l1", "inverter-current-active-l2", "inverter-current-active-l3",
                "grid-current-l1", "grid-current-l2", "grid-current-l3", "grid-current-reactive-l1",
                "grid-current-reactive-l2", "grid-current-reactive-l3", "grid-current-active-l1",
                "grid-current-active-l2", "grid-current-active-l3", "inverter-load-l1", "inverter-load-l2",
                "inverter-load-l3", "apparent-power", "grid-power-active-l1", "grid-power-active-l2",
                "grid-power-active-l3", "grid-power-reactive-l1", "grid-power-reactive-l2", "grid-power-reactive-l3",
                "inverter-power-active-l1", "inverter-power-active-l2", "inverter-power-active-l3",
                "inverter-power-reactive-l1", "inverter-power-reactive-l2", "inverter-power-reactive-l3",
                "consumption-power-l1", "consumption-power-l2", "consumption-power-l3", "consumption-power-reactive-l1",
                "consumption-power-reactive-l2", "consumption-power-reactive-l3", "solar-pv",
                "positive-dc-link-voltage", "negative-dc-link-voltage", "grid-energy-produced-l1",
                "grid-energy-produced-l2", "grid-energy-produced-l3", "grid-energy-consumed-l1",
                "grid-energy-consumed-l2", "grid-energy-consumed-l3", "inverter-energy-produced-l1",
                "inverter-energy-produced-l2", "inverter-energy-produced-l3", "inverter-energy-consumed-l1",
                "inverter-energy-consumed-l2", "inverter-energy-consumed-l3", "load-energy-produced-l1",
                "load-energy-produced-l2", "load-energy-produced-l3", "load-energy-consumed-l1",
                "load-energy-consumed-l2", "load-energy-consumed-l3", "grid-energy-produced-total",
                "grid-energy-consumed-total", "inverter-energy-produced-total", "inverter-energy-consumed-total",
                "load-energy-produced-3p", "load-energy-consumed-3p", "total-solar-energy", "state", "timestamp",
                "battery-energy-produced", "battery-energy-consumed", "soc", "soh", "power-battery",
                "total-capacity-batteries");
        return channelParametersEhub;
    }
}
