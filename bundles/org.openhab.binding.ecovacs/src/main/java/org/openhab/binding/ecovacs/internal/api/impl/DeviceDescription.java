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
package org.openhab.binding.ecovacs.internal.api.impl;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class DeviceDescription {
    public final String modelName;
    public final String deviceClass;
    public final @Nullable String deviceClassLink;
    public final ProtocolVersion protoVersion;
    public final boolean usesMqtt;
    public final Set<DeviceCapability> capabilities;

    public DeviceDescription(String modelName, String deviceClass, @Nullable String deviceClassLink,
            ProtocolVersion protoVersion, boolean usesMqtt, Set<DeviceCapability> capabilities) {
        this.modelName = modelName;
        this.capabilities = capabilities;
        this.deviceClass = deviceClass;
        this.deviceClassLink = deviceClassLink;
        this.protoVersion = protoVersion;
        this.usesMqtt = usesMqtt;
    }

    public DeviceDescription resolveLinkWith(DeviceDescription other) {
        return new DeviceDescription(modelName, deviceClass, null, other.protoVersion, other.usesMqtt,
                other.capabilities);
    }

    public void addImplicitCapabilities() {
        if (protoVersion != ProtocolVersion.XML && capabilities.contains(DeviceCapability.CLEAN_SPEED_CONTROL)) {
            capabilities.add(DeviceCapability.EXTENDED_CLEAN_SPEED_CONTROL);
        }
        if (protoVersion != ProtocolVersion.XML) {
            capabilities.add(DeviceCapability.EXTENDED_CLEAN_LOG_RECORD);
        }
        if (!capabilities.contains(DeviceCapability.SPOT_AREA_CLEANING)) {
            capabilities.add(DeviceCapability.EDGE_CLEANING);
            capabilities.add(DeviceCapability.SPOT_CLEANING);
        }
        if (protoVersion == ProtocolVersion.JSON_V2) {
            capabilities.add(DeviceCapability.DEFAULT_CLEAN_COUNT_SETTING);
        }
    }
}
