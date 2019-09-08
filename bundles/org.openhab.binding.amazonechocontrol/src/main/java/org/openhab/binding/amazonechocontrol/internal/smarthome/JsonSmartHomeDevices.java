/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.jsons.SmartHomeBaseDevice;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDeviceNetworkState.SmartHomeDeviceNetworkState;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeTags.JsonSmartHomeTag;

/**
 * @author Lukas Knoeller
 */
@NonNullByDefault
public class JsonSmartHomeDevices {

    public static class SmartHomeDevice implements SmartHomeBaseDevice {

        @Override
        public @Nullable String findId() {
            return applianceId;
        }

        @Override
        public boolean isGroup() {
            return false;
        }

        public @Nullable String applianceId;
        public @Nullable String manufacturerName;
        public @Nullable String friendlyDescription;
        public @Nullable String modelName;
        public @Nullable String friendlyName;
        public @Nullable String reachability;
        public @Nullable String entityId;
        public @Nullable SmartHomeDeviceNetworkState applianceNetworkState;
        public @Nullable SmartHomeCapability @Nullable [] capabilities;
        public @Nullable JsonSmartHomeTag tags;
        public @Nullable String @Nullable [] applianceTypes;
        public @Nullable JsonSmartHomeDeviceAlias @Nullable [] aliases;
        public @Nullable SmartHomeDevice @Nullable [] groupDevices;
        public @Nullable String connectedVia;
        public @Nullable DriverIdentity driverIdentity;
    }

    static class DriverIdentity {
        public @Nullable String namespace;
        public @Nullable String identifier;
    }

    public @Nullable SmartHomeDevice @Nullable [] smarthomeDevices;
}
