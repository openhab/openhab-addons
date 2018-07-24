/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_BATTERY_LEVEL;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;

/**
 * A base class for all battery device messages
 *
 * @author Martin van Wingerden - Simplify some code in the RFXCOM binding
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
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_BATTERY_LEVEL:
                return convertBatteryLevelToSystemWideLevel(batteryLevel);

            default:
                return super.convertToState(channelId);
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
