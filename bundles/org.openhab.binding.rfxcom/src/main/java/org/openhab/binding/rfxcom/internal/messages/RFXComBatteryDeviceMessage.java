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
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.CHANNEL_BATTERY_LEVEL;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * A base class for all battery device messages
 *
 * @author Martin van Wingerden - Initial contribution
 */
abstract class RFXComBatteryDeviceMessage<T> extends RFXComDeviceMessageImpl<T> {
    int batteryLevel;

    RFXComBatteryDeviceMessage(PacketType packetType) {
        super(packetType);
    }

    RFXComBatteryDeviceMessage() {
        // deliberately empty
    }

    @Override
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_BATTERY_LEVEL:
                return convertBatteryLevelToSystemWideLevel(batteryLevel);

            default:
                return super.convertToState(channelId, config, deviceState);
        }
    }

    /**
     * Convert internal battery level (0-9) to system wide battery level (0-100%).
     *
     * @param batteryLevel Internal battery level
     * @return Battery level in system wide level
     */
    private State convertBatteryLevelToSystemWideLevel(int batteryLevel) {
        int ohLevel = (batteryLevel + 1) * 10;
        return new DecimalType(ohLevel);
    }
}
