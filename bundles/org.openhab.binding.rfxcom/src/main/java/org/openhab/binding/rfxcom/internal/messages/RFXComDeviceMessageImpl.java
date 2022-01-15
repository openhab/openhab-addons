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
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.CHANNEL_SIGNAL_LEVEL;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * A base class for all device messages, so this is not about things as interface messages
 *
 * @author Martin van Wingerden - Initial contribution
 */
abstract class RFXComDeviceMessageImpl<T> extends RFXComBaseMessage implements RFXComDeviceMessage<T> {
    byte signalLevel;

    RFXComDeviceMessageImpl(PacketType packetType) {
        super(packetType);
    }

    RFXComDeviceMessageImpl() {
        // deliberately empty
    }

    @Override
    public void setConfig(RFXComDeviceConfiguration config) throws RFXComException {
        RFXComGenericDeviceConfiguration genericConfig = (RFXComGenericDeviceConfiguration) config;
        this.setSubType(convertSubType(genericConfig.subType));
        this.setDeviceId(genericConfig.deviceId);
    }

    @Override
    public Command convertToCommand(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        return (Command) convertToState(channelId, config, deviceState);
    }

    @Override
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_SIGNAL_LEVEL:
                return convertSignalLevelToSystemWideLevel(signalLevel);

            default:
                throw new RFXComUnsupportedChannelException("Nothing relevant for " + channelId);
        }
    }

    @Override
    public void addDevicePropertiesTo(DiscoveryResultBuilder discoveryResultBuilder) throws RFXComException {
        String subTypeString = convertSubType(String.valueOf(subType)).toString();
        String label = getPacketType() + "-" + getDeviceId();

        discoveryResultBuilder.withLabel(label)
                .withProperty(RFXComGenericDeviceConfiguration.DEVICE_ID_LABEL, getDeviceId())
                .withProperty(RFXComGenericDeviceConfiguration.SUB_TYPE_LABEL, subTypeString);
    }

    /**
     * Convert internal signal level (0-15) to system wide signal level (0-4).
     *
     * @param signalLevel Internal signal level
     * @return Signal level in system wide level
     */
    private State convertSignalLevelToSystemWideLevel(int signalLevel) {
        int newLevel;

        /*
         * RFXCOM signal levels are always between 0-15.
         *
         * Use switch case to make level adaption easier in future if needed.
         */

        switch (signalLevel) {
            case 0:
            case 1:
                newLevel = 0;
                break;

            case 2:
            case 3:
            case 4:
                newLevel = 1;
                break;

            case 5:
            case 6:
            case 7:
                newLevel = 2;
                break;

            case 8:
            case 9:
            case 10:
            case 11:
                newLevel = 3;
                break;

            case 12:
            case 13:
            case 14:
            case 15:
            default:
                newLevel = 4;
        }

        return new DecimalType(newLevel);
    }
}
