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

/**
 * @author Lukas Knoeller - Contributor
 */
@NonNullByDefault
public class JsonSmartHomeDevices {

    public class SmartHomeDevice {
        public @Nullable String applianceId;
        public @Nullable String manufacturerName;
        public @Nullable String friendlyDescription;
        public @Nullable String friendlyName;
        public @Nullable String reachability;
        public @Nullable String entityId;
        public @Nullable String groupIdentity;
        public @Nullable JsonSmartHomeDeviceAlias @Nullable [] alias;
        public @Nullable SmartHomeDevice @Nullable [] groupDevices;
        public boolean brightness = false;
        public boolean color = false;
        public boolean colorTemperature = false;

        public SmartHomeDevice(String applianceId, String manufacturerName, String friendlyDescription,
                String friendlyName, String reachability, String entityId, JsonSmartHomeDeviceAlias[] alias,
                SmartHomeDevice[] groupDevices) {
            this.applianceId = applianceId;
            this.manufacturerName = manufacturerName;
            this.friendlyDescription = friendlyDescription;
            this.friendlyName = friendlyName;
            this.reachability = reachability;
            this.entityId = entityId;
            this.alias = alias;
            this.groupDevices = groupDevices;
        }
    }

    public @Nullable SmartHomeDevice @Nullable [] smarthomeDevices;
}
