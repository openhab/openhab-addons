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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeTags.JsonSmartHomeTag;

/**
 * @author Lukas Knoeller
 */
@NonNullByDefault
public class JsonSmartHomeDevices {

    public static class SmartHomeDevice {
        public @Nullable String applianceId;
        public @Nullable String manufacturerName;
        public @Nullable String friendlyDescription;
        public @Nullable String friendlyName;
        public @Nullable String reachability;
        public @Nullable String entityId;
        public @Nullable JsonSmartHomeTag tags;
        public @Nullable String @Nullable [] applianceTypes;
        public @Nullable JsonSmartHomeDeviceAlias @Nullable [] aliases;
        public @Nullable SmartHomeDevice @Nullable [] groupDevices;
        public boolean brightness = false;
        public boolean color = false;
        public boolean colorTemperature = false;

        public SmartHomeDevice(String applianceId, String manufacturerName, String friendlyDescription,
                @Nullable String friendlyName, String reachability, String entityId, JsonSmartHomeDeviceAlias[] aliases,
                SmartHomeDevice[] groupDevices) {
            this.applianceId = applianceId;
            this.manufacturerName = manufacturerName;
            this.friendlyDescription = friendlyDescription;
            this.friendlyName = friendlyName;
            this.reachability = reachability;
            this.entityId = entityId;
            this.aliases = aliases;
            this.groupDevices = groupDevices;
        }

        public SmartHomeDevice() {
        }
    }

    public @Nullable SmartHomeDevice @Nullable [] smarthomeDevices;
}
