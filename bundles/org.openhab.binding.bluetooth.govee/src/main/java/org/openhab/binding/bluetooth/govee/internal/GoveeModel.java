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
package org.openhab.binding.bluetooth.govee.internal;

import static org.openhab.binding.bluetooth.govee.internal.GoveeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public enum GoveeModel {
    H5051(THING_TYPE_HYGROMETER, "Govee Wi-Fi Temperature Humidity Monitor", false),
    H5052(THING_TYPE_HYGROMETER_MONITOR, "Govee Temperature Humidity Monitor", true),
    H5071(THING_TYPE_HYGROMETER, "Govee Temperature Humidity Monitor", false),
    H5072(THING_TYPE_HYGROMETER_MONITOR, "Govee Temperature Humidity Monitor", true),
    H5074(THING_TYPE_HYGROMETER_MONITOR, "Govee Mini Temperature Humidity Monitor", true),
    H5075(THING_TYPE_HYGROMETER_MONITOR, "Govee Temperature Humidity Monitor", true),
    H5101(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    H5102(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    H5177(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    H5179(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    B5175(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    B5178(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true);

    private final ThingTypeUID thingTypeUID;
    private final String label;
    private final boolean supportsWarningBroadcast;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoveeModel.class);

    private GoveeModel(ThingTypeUID thingTypeUID, String label, boolean supportsWarningBroadcast) {
        this.thingTypeUID = thingTypeUID;
        this.label = label;
        this.supportsWarningBroadcast = supportsWarningBroadcast;
    }

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    public String getLabel() {
        return label;
    }

    public boolean supportsWarningBroadcast() {
        return supportsWarningBroadcast;
    }

    public static @Nullable GoveeModel getGoveeModel(BluetoothDiscoveryDevice device) {
        String name = device.getName();
        if (name != null) {
            if ((name.startsWith("Govee") && name.length() >= 11) || name.startsWith("GVH")) {
                String uname = name.toUpperCase();
                for (GoveeModel model : GoveeModel.values()) {
                    if (uname.contains(model.name())) {
                        LOGGER.debug("detected model {}", model);
                        return model;
                    }
                }
            }
        }
        LOGGER.debug("Device {} is no Govee", name);
        return null;
    }
}
