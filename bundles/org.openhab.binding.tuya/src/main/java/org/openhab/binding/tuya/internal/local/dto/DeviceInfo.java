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
package org.openhab.binding.tuya.internal.local.dto;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DeviceInfo} holds information for the device communication
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DeviceInfo {
    public final String ip;
    public final String protocolVersion;

    public DeviceInfo(String ip, String protocolVersion) {
        this.ip = ip;
        this.protocolVersion = protocolVersion;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceInfo that = (DeviceInfo) o;
        return ip.equals(that.ip) && protocolVersion.equals(that.protocolVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, protocolVersion);
    }

    @Override
    public String toString() {
        return "DeviceInfo{ip='" + ip + "', version='" + protocolVersion + "'}";
    }
}
