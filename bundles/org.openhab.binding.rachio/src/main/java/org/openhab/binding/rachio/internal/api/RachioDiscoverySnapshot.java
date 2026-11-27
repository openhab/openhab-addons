/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable view of the controller data consumed by discovery.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public record RachioDiscoverySnapshot(List<DeviceSnapshot> devices) {
    static final RachioDiscoverySnapshot EMPTY = new RachioDiscoverySnapshot(List.of());

    public RachioDiscoverySnapshot {
        devices = List.copyOf(devices);
    }

    static RachioDiscoverySnapshot fromDevices(Map<String, RachioDevice> devices) {
        List<DeviceSnapshot> snapshots = new ArrayList<>(devices.size());
        for (RachioDevice device : devices.values()) {
            snapshots.add(device.discoverySnapshot());
        }
        return new RachioDiscoverySnapshot(snapshots);
    }

    public record DeviceSnapshot(String id, String name, String thingId, String status, boolean enabled,
            boolean sleepMode, Map<String, String> properties, List<ZoneSnapshot> zones,
            List<ScheduleSnapshot> schedules, List<ScheduleSnapshot> flexSchedules) {
        public DeviceSnapshot {
            properties = Map.copyOf(properties);
            zones = List.copyOf(zones);
            schedules = List.copyOf(schedules);
            flexSchedules = List.copyOf(flexSchedules);
        }
    }

    public record ZoneSnapshot(String id, String name, String thingId, int zoneNumber, boolean enabled,
            Map<String, String> properties) {
        public ZoneSnapshot {
            properties = Map.copyOf(properties);
        }
    }

    public record ScheduleSnapshot(String id, String name, String type) {
    }
}
