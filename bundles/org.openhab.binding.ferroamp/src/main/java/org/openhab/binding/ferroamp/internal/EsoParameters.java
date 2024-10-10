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
 * The {@link EsoParameters} is responsible for all parameters regarded to ESO
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class EsoParameters {
    public String jsonPostEso;

    public EsoParameters(String jsonPostEso) {
        this.jsonPostEso = jsonPostEso;
    }

    public static List<String> getChannelParametersEso() {
        final List<String> channelParametersEso = Arrays.asList("eso-unique-identifier", "eso-measured-voltage-battery",
                "eso-measured-current-battery", "eso-battery-energy-produced", "eso-battery-energy-consumed", "eso-soc",
                "eso-relay-status", "eso-temperature", "eso-fault-code", "eso-dc-link-voltage", "eso-timestamp");
        return channelParametersEso;
    }
}
