/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.MyenergiBindingConstants;
import org.openhab.binding.myenergi.internal.model.BaseSummary;
import org.openhab.binding.myenergi.internal.model.ZappiSummary;

/**
 * The {@link ThingPropertyMapper} is a utility class to extract the relevant
 * thing properties from the model.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public class ThingPropertyMapper {

    private static Map<String, String> getThingBaseProperties(BaseSummary device) {
        Map<String, String> properties = new HashMap<String, String>();
        Long serialNumber = device.serialNumber;
        if (serialNumber != null) {
            properties.put(MyenergiBindingConstants.PROP_SERIAL_NUMBER, String.valueOf(serialNumber));
        }
        String firmwareVersion = device.firmwareVersion;
        if (firmwareVersion != null) {
            properties.put(MyenergiBindingConstants.PROP_FIRMWARE_VERSION, firmwareVersion);
        }
        return properties;
    }

    public static Map<String, String> getThingProperties(BaseSummary device) {
        return getThingBaseProperties(device);
    }

    public static Map<String, String> getThingProperties(ZappiSummary device) {
        Map<String, String> properties = getThingBaseProperties(device);
        Integer numberOfPhases = device.numberOfPhases;
        if (numberOfPhases != null) {
            properties.put(MyenergiBindingConstants.PROP_NUMBER_OF_PHASES, String.valueOf(numberOfPhases));
        }
        return properties;
    }
}
