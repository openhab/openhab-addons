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
package org.openhab.binding.elroconnects.internal.devices;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.ElroDeviceStatus;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsBridgeHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsDeviceHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElroConnectsDeviceEntrySensor} is representing a generic ELRO Connects Entry Sensor device.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsDeviceEntrySensor extends ElroConnectsDevice {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsDeviceEntrySensor.class);

    // device states
    private static final String STAT_OPEN = "55";
    private static final String STAT_STILL_OPEN = "66";
    private static final String STAT_FAULT = "11";
    private static final String STAT_NORMAL = "AA";

    private static final Set<String> T_OPEN = Set.of(STAT_OPEN, STAT_STILL_OPEN);
    private static final Set<String> T_FAULT = Set.of(STAT_FAULT);
    private static final Set<String> T_NORMAL = Set.of(STAT_NORMAL);

    private static final Map<ElroDeviceStatus, Set<String>> DEVICE_STATUS_MAP = Map.ofEntries(
            Map.entry(ElroDeviceStatus.NORMAL, T_NORMAL), Map.entry(ElroDeviceStatus.OPEN, T_OPEN),
            Map.entry(ElroDeviceStatus.FAULT, T_FAULT));

    public ElroConnectsDeviceEntrySensor(int deviceId, ElroConnectsBridgeHandler bridge) {
        super(deviceId, bridge);
        statusMap = DEVICE_STATUS_MAP.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(v -> Map.entry(v, e.getKey())))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void updateState() {
        ElroConnectsDeviceHandler handler = getHandler();
        if (handler == null) {
            return;
        }

        ElroDeviceStatus elroStatus = getStatus();
        int batteryLevel = 0;
        String deviceStatus = this.deviceStatus;
        if (deviceStatus.length() >= 6) {
            batteryLevel = Integer.parseInt(deviceStatus.substring(2, 4), 16);
        } else {
            elroStatus = ElroDeviceStatus.FAULT;
            logger.debug("Could not decode device status: {}", deviceStatus);
        }

        switch (elroStatus) {
            case UNDEF:
                handler.updateState(ENTRY, UnDefType.UNDEF);
                handler.updateState(BATTERY_LEVEL, UnDefType.UNDEF);
                handler.updateState(LOW_BATTERY, UnDefType.UNDEF);
                handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device " + deviceId + " is not syncing with K1 hub");
                break;
            case FAULT:
                handler.updateState(ENTRY, UnDefType.UNDEF);
                handler.updateState(BATTERY_LEVEL, UnDefType.UNDEF);
                handler.updateState(LOW_BATTERY, UnDefType.UNDEF);
                handler.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Device " + deviceId + " has a fault");
                break;
            default:
                handler.updateState(ENTRY,
                        ElroDeviceStatus.OPEN.equals(elroStatus) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                handler.updateState(BATTERY_LEVEL, new DecimalType(batteryLevel));
                handler.updateState(LOW_BATTERY, (batteryLevel < 15) ? OnOffType.ON : OnOffType.OFF);
                handler.updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void testAlarm() {
        // nothing
    }

    @Override
    public void muteAlarm() {
        // nothing
    }

    @Override
    public void switchState(boolean state) {
        // nothing
    }
}
