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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable POJO representing the device identification queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class Ident {
    @Nullable
    private Type type;
    @Nullable
    private String deviceName;
    @Nullable
    private DeviceIdentLabel deviceIdentLabel;
    @Nullable
    private XkmIdentLabel xkmIdentLabel;

    public Optional<Type> getType() {
        return Optional.ofNullable(type);
    }

    public Optional<String> getDeviceName() {
        return Optional.ofNullable(deviceName);
    }

    public Optional<DeviceIdentLabel> getDeviceIdentLabel() {
        return Optional.ofNullable(deviceIdentLabel);
    }

    public Optional<XkmIdentLabel> getXkmIdentLabel() {
        return Optional.ofNullable(xkmIdentLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceIdentLabel, deviceName, type, xkmIdentLabel);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Ident other = (Ident) obj;
        return Objects.equals(deviceIdentLabel, other.deviceIdentLabel) && Objects.equals(deviceName, other.deviceName)
                && Objects.equals(type, other.type) && Objects.equals(xkmIdentLabel, other.xkmIdentLabel);
    }

    @Override
    public String toString() {
        return "Ident [type=" + type + ", deviceName=" + deviceName + ", deviceIdentLabel=" + deviceIdentLabel
                + ", xkmIdentLabel=" + xkmIdentLabel + "]";
    }
}
