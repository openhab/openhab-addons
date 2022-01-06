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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonDevices} encapsulate the GSON data of device list
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonDevices {

    public static class Device {
        public @Nullable String accountName;
        public @Nullable String serialNumber;
        public @Nullable String deviceOwnerCustomerId;
        public @Nullable String deviceAccountId;
        public @Nullable String deviceFamily;
        public @Nullable String deviceType;
        public @Nullable String softwareVersion;
        public boolean online;
        public @Nullable Set<String> capabilities;

        public Set<String> getCapabilities() {
            return Objects.requireNonNullElse(capabilities, Set.of());
        }

        @Override
        public String toString() {
            return "Device{" + "accountName='" + accountName + '\'' + ", serialNumber='" + serialNumber + '\''
                    + ", deviceOwnerCustomerId='" + deviceOwnerCustomerId + '\'' + ", deviceAccountId='"
                    + deviceAccountId + '\'' + ", deviceFamily='" + deviceFamily + '\'' + ", deviceType='" + deviceType
                    + '\'' + ", softwareVersion='" + softwareVersion + '\'' + ", online=" + online + ", capabilities="
                    + capabilities + '}';
        }
    }

    public List<Device> devices = List.of();
}
