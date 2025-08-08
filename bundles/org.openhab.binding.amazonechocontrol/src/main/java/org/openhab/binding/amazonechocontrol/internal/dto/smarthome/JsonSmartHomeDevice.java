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
package org.openhab.binding.amazonechocontrol.internal.dto.smarthome;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDeviceNetworkState.SmartHomeDeviceNetworkState;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeTags.JsonSmartHomeTag;

/**
 * The {@link JsonSmartHomeDevice} encapsulates smarthome device API responses
 *
 * @author Lukas Knoeller - Initial contribution
 */
@NonNullByDefault
public class JsonSmartHomeDevice implements SmartHomeBaseDevice {
    public @Nullable Integer updateIntervalInSeconds;
    public @Nullable String applianceId;
    public @Nullable String manufacturerName;
    public @Nullable String friendlyDescription;
    public @Nullable String modelName;
    public @Nullable String friendlyName;
    public @Nullable String reachability;
    public @Nullable String entityId;
    public @Nullable SmartHomeDeviceNetworkState applianceNetworkState;
    public @Nullable List<JsonSmartHomeCapability> capabilities;
    public @Nullable JsonSmartHomeTag tags;
    public @Nullable List<String> applianceTypes;
    public @Nullable List<JsonSmartHomeDeviceAlias> aliases;
    public @Nullable List<JsonSmartHomeDevice> groupDevices;
    public @Nullable String connectedVia;
    public @Nullable List<DeviceIdentifier> alexaDeviceIdentifierList;
    public @Nullable DriverIdentity driverIdentity;
    public @Nullable List<String> mergedApplianceIds;
    public @Nullable List<JsonSmartHomeDevice> smarthomeDevices;

    public List<JsonSmartHomeCapability> getCapabilities() {
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
        return "JsonSmartHomeDevice{" + "updateIntervalInSeconds=" + updateIntervalInSeconds + ", applianceId='"
                + applianceId + "'" + ", manufacturerName='" + manufacturerName + "'" + ", friendlyDescription='"
                + friendlyDescription + "'" + ", modelName='" + modelName + "'" + ", friendlyName='" + friendlyName
                + "'" + ", reachability='" + reachability + "'" + ", entityId='" + entityId + "'"
                + ", applianceNetworkState=" + applianceNetworkState + ", capabilities=" + capabilities + ", tags="
                + tags + ", applianceTypes=" + applianceTypes + ", aliases=" + aliases + ", groupDevices="
                + groupDevices + ", connectedVia='" + connectedVia + "'" + ", alexaDeviceIdentifierList="
                + alexaDeviceIdentifierList + ", driverIdentity=" + driverIdentity + ", mergedApplianceIds="
                + mergedApplianceIds + ", smarthomeDevices=" + smarthomeDevices + "}";
    }

    public static class DriverIdentity {
        public @Nullable String namespace;
        public @Nullable String identifier;

        @Override
        public String toString() {
            return "DriverIdentity{" + "namespace='" + namespace + '\'' + ", identifier='" + identifier + '\'' + '}';
        }
    }

    public static class DeviceIdentifier {
        public @Nullable String dmsDeviceSerialNumber;
        public @Nullable String dmsDeviceTypeId;

        @Override
        public String toString() {
            return "DeviceIdentifier{" + "dmsDeviceSerialNumber='" + dmsDeviceSerialNumber + "'" + ", dmsDeviceTypeId='"
                    + dmsDeviceTypeId + "'" + "}";
        }
    }
}
