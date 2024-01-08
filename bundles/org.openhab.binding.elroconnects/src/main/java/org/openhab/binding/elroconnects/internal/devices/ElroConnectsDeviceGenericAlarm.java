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
package org.openhab.binding.elroconnects.internal.devices;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.ElroDeviceStatus;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsBridgeHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsDeviceHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElroConnectsDeviceGenericAlarm} is representing a generic ELRO Connects Alarm device.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsDeviceGenericAlarm extends ElroConnectsDevice {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsDeviceGenericAlarm.class);

    // device states
    private static final String STAT_ALARM = "55";
    private static final String STAT_TEST = "BB";
    private static final String STAT_FAULT = "11";
    private static final String STAT_SILENCE = "50";
    private static final String STAT_NORMAL = "AA";

    private static final Set<String> T_ALARM = Set.of(STAT_ALARM);
    private static final Set<String> T_TEST = Set.of(STAT_TEST);
    private static final Set<String> T_FAULT = Set.of(STAT_FAULT);
    private static final Set<String> T_SILENCE = Set.of(STAT_SILENCE);
    private static final Set<String> T_NORMAL = Set.of(STAT_NORMAL);

    private static final Map<ElroDeviceStatus, Set<String>> DEVICE_STATUS_MAP = Map.ofEntries(
            Map.entry(ElroDeviceStatus.NORMAL, T_NORMAL), Map.entry(ElroDeviceStatus.TRIGGERED, T_ALARM),
            Map.entry(ElroDeviceStatus.TEST, T_TEST), Map.entry(ElroDeviceStatus.SILENCE, T_SILENCE),
            Map.entry(ElroDeviceStatus.FAULT, T_FAULT));

    // device commands
    protected static final String CMD_TEST = "BB000000";
    protected static final String CMD_SILENCE = "50000000";

    public ElroConnectsDeviceGenericAlarm(int deviceId, ElroConnectsBridgeHandler bridge) {
        super(deviceId, bridge);
        statusMap = DEVICE_STATUS_MAP.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(v -> Map.entry(v, e.getKey())))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void muteAlarm() {
        try {
            bridge.deviceControl(deviceId, CMD_SILENCE);
        } catch (IOException e) {
            logger.debug("Failed to control device: {}", e.getMessage());
        }
    }

    @Override
    public void testAlarm() {
        try {
            ElroDeviceStatus elroStatus = getStatus();
            if (!(ElroDeviceStatus.TRIGGERED.equals(elroStatus) || ElroDeviceStatus.TEST.equals(elroStatus))) {
                bridge.deviceControl(deviceId, CMD_TEST);
            }
        } catch (IOException e) {
            logger.debug("Failed to control device: {}", e.getMessage());
        }
    }

    @Override
    public void updateState() {
        ElroConnectsDeviceHandler handler = getHandler();
        if (handler == null) {
            return;
        }

        ElroDeviceStatus elroStatus = getStatus();
        int signalStrength = 0;
        int batteryLevel = 0;
        String deviceStatus = this.deviceStatus;
        if (deviceStatus.length() >= 6) {
            signalStrength = Integer.parseInt(deviceStatus.substring(0, 2), 16);
            signalStrength = (signalStrength > 4) ? 4 : ((signalStrength < 0) ? 0 : signalStrength);
            batteryLevel = Integer.parseInt(deviceStatus.substring(2, 4), 16);
        } else {
            elroStatus = ElroDeviceStatus.FAULT;
            logger.debug("Could not decode device status: {}", deviceStatus);
        }

        switch (elroStatus) {
            case UNDEF:
                handler.updateState(SIGNAL_STRENGTH, UnDefType.UNDEF);
                handler.updateState(BATTERY_LEVEL, UnDefType.UNDEF);
                handler.updateState(LOW_BATTERY, UnDefType.UNDEF);
                String msg = String.format("@text/offline.device-not-syncing [ \"%d\" ]", deviceId);
                handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                break;
            case FAULT:
                handler.updateState(SIGNAL_STRENGTH, UnDefType.UNDEF);
                handler.updateState(BATTERY_LEVEL, UnDefType.UNDEF);
                handler.updateState(LOW_BATTERY, UnDefType.UNDEF);
                msg = String.format("@text/offline.device-fault [ \"%d\" ]", deviceId);
                handler.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, msg);
                break;
            default:
                handler.updateState(SIGNAL_STRENGTH, new DecimalType(signalStrength));
                handler.updateState(BATTERY_LEVEL, new DecimalType(batteryLevel));
                handler.updateState(LOW_BATTERY, OnOffType.from(batteryLevel < 15));
                handler.updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void switchState(boolean state) {
        // nothing
    }
}
