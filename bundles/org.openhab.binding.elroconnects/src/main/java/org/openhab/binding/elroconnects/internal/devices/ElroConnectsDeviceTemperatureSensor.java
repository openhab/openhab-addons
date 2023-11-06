/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.ElroDeviceStatus;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsBridgeHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsDeviceHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElroConnectsDeviceTemperatureSensor} is representing an ELRO Connects temperature and humidity sensor
 * device.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsDeviceTemperatureSensor extends ElroConnectsDevice {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsDeviceTemperatureSensor.class);

    public ElroConnectsDeviceTemperatureSensor(int deviceId, ElroConnectsBridgeHandler bridge) {
        super(deviceId, bridge);
    }

    @Override
    public void updateState() {
        ElroConnectsDeviceHandler handler = getHandler();
        if (handler == null) {
            return;
        }

        ElroDeviceStatus elroStatus = ElroDeviceStatus.NORMAL;
        int signalStrength = 0;
        int batteryLevel = 0;
        int temperature = 0;
        int humidity = 0;
        String deviceStatus = this.deviceStatus;
        if (deviceStatus.length() >= 8) {
            signalStrength = Integer.parseInt(deviceStatus.substring(0, 2), 16);
            signalStrength = (signalStrength > 4) ? 4 : ((signalStrength < 0) ? 0 : signalStrength);
            batteryLevel = Integer.parseInt(deviceStatus.substring(2, 4), 16);
            temperature = Byte.parseByte(deviceStatus.substring(4, 6), 16);
            humidity = Integer.parseInt(deviceStatus.substring(6, 8));
        } else {
            elroStatus = ElroDeviceStatus.FAULT;
            logger.debug("Could not decode device status: {}", deviceStatus);
        }

        switch (elroStatus) {
            case FAULT:
                handler.updateState(SIGNAL_STRENGTH, UnDefType.UNDEF);
                handler.updateState(BATTERY_LEVEL, UnDefType.UNDEF);
                handler.updateState(LOW_BATTERY, UnDefType.UNDEF);
                handler.updateState(TEMPERATURE, UnDefType.UNDEF);
                handler.updateState(HUMIDITY, UnDefType.UNDEF);
                String msg = String.format("@text/offline.device-fault [ \"%d\" ]", deviceId);
                handler.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, msg);
                break;
            default:
                handler.updateState(SIGNAL_STRENGTH, new DecimalType(signalStrength));
                handler.updateState(BATTERY_LEVEL, new DecimalType(batteryLevel));
                handler.updateState(LOW_BATTERY, (batteryLevel < 15) ? OnOffType.ON : OnOffType.OFF);
                handler.updateState(TEMPERATURE, new QuantityType<>(temperature, CELSIUS));
                handler.updateState(HUMIDITY, new QuantityType<>(humidity, Units.PERCENT));
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
