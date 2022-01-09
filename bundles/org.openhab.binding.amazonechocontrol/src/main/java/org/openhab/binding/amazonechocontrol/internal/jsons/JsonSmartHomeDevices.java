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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDeviceNetworkState.SmartHomeDeviceNetworkState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeTags.JsonSmartHomeTag;

/**
 * @author Lukas Knoeller - Initial contribution
 */
@NonNullByDefault
public class JsonSmartHomeDevices {
    public static class SmartHomeDevice implements SmartHomeBaseDevice {
        public @Nullable Integer updateIntervalInSeconds;
        public @Nullable String applianceId;
        public @Nullable String manufacturerName;
        public @Nullable String friendlyDescription;
        public @Nullable String modelName;
        public @Nullable String friendlyName;
        public @Nullable String reachability;
        public @Nullable String entityId;
        public @Nullable SmartHomeDeviceNetworkState applianceNetworkState;
        public @Nullable List<SmartHomeCapability> capabilities;
        public @Nullable JsonSmartHomeTag tags;
        public @Nullable List<String> applianceTypes;
        public @Nullable List<JsonSmartHomeDeviceAlias> aliases;
        public @Nullable List<SmartHomeDevice> groupDevices;
        public @Nullable String connectedVia;
        public @Nullable DriverIdentity driverIdentity;
        public @Nullable List<String> mergedApplianceIds;
        public @Nullable List<SmartHomeDevice> smarthomeDevices;

        public List<SmartHomeCapability> getCapabilities() {
            return Objects.requireNonNullElse(capabilities, List.of());
        }

        @Override
        public @Nullable String findId() {
            return applianceId;
        }

        @Override
        public boolean isGroup() {
            return false;
        }

        @Override
        public String toString() {
            return "SmartHomeDevice{" + "updateIntervalInSeconds=" + updateIntervalInSeconds + ", applianceId='"
                    + applianceId + '\'' + ", manufacturerName='" + manufacturerName + '\'' + ", friendlyDescription='"
                    + friendlyDescription + '\'' + ", modelName='" + modelName + '\'' + ", friendlyName='"
                    + friendlyName + '\'' + ", reachability='" + reachability + '\'' + ", entityId='" + entityId + '\''
                    + ", applianceNetworkState=" + applianceNetworkState + ", capabilities=" + capabilities + ", tags="
                    + tags + ", applianceTypes=" + applianceTypes + ", aliases=" + aliases + ", groupDevices="
                    + groupDevices + ", connectedVia='" + connectedVia + '\'' + ", driverIdentity=" + driverIdentity
                    + ", mergedApplianceIds=" + mergedApplianceIds + ", smarthomeDevices=" + smarthomeDevices + '}';
        }
    }

    public static class DriverIdentity {
        public @Nullable String namespace;
        public @Nullable String identifier;

        @Override
        public String toString() {
            return "DriverIdentity{" + "namespace='" + namespace + '\'' + ", identifier='" + identifier + '\'' + '}';
        }
    }
}
