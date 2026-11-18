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
package org.openhab.binding.verisure.internal.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The battery status of the devices in a Verisure installation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureBatteryStatusDTO {

    public static final String STATUS_GOOD = "GOOD";
    public static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

    private @Nullable Data data;

    /**
     * @return battery status per device label
     */
    public Map<String, String> getStatusByDeviceLabel() {
        Map<String, String> result = new HashMap<>();
        Data localData = data;
        Installation installation = localData != null ? localData.installation : null;
        DeviceBatteryLifeCycle lifeCycle = installation != null ? installation.deviceBatteryLifeCycle : null;
        List<BatteryDevice> devices = lifeCycle != null ? lifeCycle.devices : null;
        if (devices != null) {
            for (BatteryDevice batteryDevice : devices) {
                Device device = batteryDevice.device;
                String deviceLabel = device != null ? device.deviceLabel : null;
                String status = batteryDevice.status;
                if (deviceLabel != null && status != null) {
                    result.put(deviceLabel, status);
                }
            }
        }
        return result;
    }

    public static boolean isLowBattery(@Nullable String status) {
        return status != null && !STATUS_GOOD.equals(status) && !STATUS_UNAVAILABLE.equals(status);
    }

    private static class Data {
        private @Nullable Installation installation;
    }

    private static class Installation {
        private @Nullable DeviceBatteryLifeCycle deviceBatteryLifeCycle;
    }

    private static class DeviceBatteryLifeCycle {
        private @Nullable List<BatteryDevice> devices;
    }

    private static class BatteryDevice {
        private @Nullable Device device;
        private @Nullable String status;
    }

    private static class Device {
        private @Nullable String deviceLabel;
    }
}
