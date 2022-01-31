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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonAnnouncementTarget} encapsulate the GSON data of the sequence command AlexaAnnouncement for
 * announcement target
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonAnnouncementTarget {

    public @Nullable String customerId;
    public List<TargetDevice> devices;

    public JsonAnnouncementTarget(List<JsonDevices.Device> deviceList) {
        customerId = deviceList.get(0).deviceOwnerCustomerId;
        devices = deviceList.stream().map(TargetDevice::new).collect(Collectors.toList());
    }

    public static class TargetDevice {
        public @Nullable String deviceSerialNumber;
        public @Nullable String deviceTypeId;

        public TargetDevice(JsonDevices.Device device) {
            deviceSerialNumber = device.serialNumber;
            deviceTypeId = device.deviceType;
        }
    }
}
