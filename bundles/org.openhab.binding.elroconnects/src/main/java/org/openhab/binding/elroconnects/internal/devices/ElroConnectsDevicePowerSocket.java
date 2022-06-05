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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsBridgeHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsDeviceHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElroConnectsDevicePowerSocket} is representing an ELRO Connects power socket device.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsDevicePowerSocket extends ElroConnectsDevice {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsDevicePowerSocket.class);

    // device states
    private static final String STAT_ON = "00";
    private static final String STAT_OFF = "01";

    // device commands
    protected static final String CMD_OFF = "0101FFFF";
    protected static final String CMD_ON = "0100FFFF";

    public ElroConnectsDevicePowerSocket(int deviceId, ElroConnectsBridgeHandler bridge) {
        super(deviceId, bridge);
    }

    @Override
    public void switchState(boolean state) {
        try {
            bridge.deviceControl(deviceId, state ? CMD_ON : CMD_OFF);
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

        String deviceStatus = this.deviceStatus;
        if (deviceStatus.length() < 6) {
            logger.debug("Could not decode device status: {}", deviceStatus);
            String msg = String.format("@text/offline.device-fault [ \"%d\" ]", deviceId);
            handler.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, msg);
            return;
        }

        int signalStrength = Integer.parseInt(deviceStatus.substring(0, 2), 16);
        signalStrength = (signalStrength > 4) ? 4 : ((signalStrength < 0) ? 0 : signalStrength);
        handler.updateState(SIGNAL_STRENGTH, new DecimalType(signalStrength));

        String status = deviceStatus.substring(4, 6);
        State state = STAT_ON.equals(status) ? OnOffType.ON
                : (STAT_OFF.equals(status) ? OnOffType.OFF : UnDefType.UNDEF);
        handler.updateState(POWER_STATE, state);
        if (UnDefType.UNDEF.equals(state)) {
            String msg = String.format("@text/offline.device-not-syncing [ \"%d\" ]", deviceId);
            handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
        } else {
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
}
