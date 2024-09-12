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
package org.openhab.binding.rfxcom.internal.messages;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * An interface for message about devices, so interface message do not (have to) implement this
 *
 * @author Martin van Wingerden - Initial contribution
 */
public interface RFXComDeviceMessage<T> extends RFXComMessage {
    /**
     * Procedure for converting RFXCOM value to openHAB command.
     *
     * @param channelId id of the channel
     * @param config Configuration of the thing being handled
     * @param deviceState
     * @return openHAB command.
     * @throws RFXComUnsupportedChannelException if the channel is not supported
     * @throws RFXComInvalidStateException if the channel is supported, but the device is not configured for the value
     */
    Command convertToCommand(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException;

    /**
     * Procedure for converting RFXCOM value to openHAB state.
     *
     * @param channelId id of the channel
     * @param config configuration of the thing being handled
     * @param deviceState
     * @return openHAB state.
     * @throws RFXComUnsupportedChannelException if the channel is not supported
     * @throws RFXComInvalidStateException if the channel is supported, but the device is not configured for the value
     */
    State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException;

    /**
     * Procedure to get device id.
     *
     * @return device Id.
     */
    String getDeviceId();

    /**
     * Get the packet type for this device message
     *
     * @return the message its packet type
     */
    RFXComBaseMessage.PacketType getPacketType();

    /**
     * Given a DiscoveryResultBuilder add any new properties to the builder for the given message
     *
     * @param discoveryResultBuilder existing builder containing some early details
     * @throws RFXComException
     */
    void addDevicePropertiesTo(DiscoveryResultBuilder discoveryResultBuilder) throws RFXComException;

    /**
     * Procedure for converting sub type as string to sub type object.
     *
     * @param subType
     * @return sub type object.
     * @throws RFXComUnsupportedValueException if the given subType cannot be converted
     */
    T convertSubType(String subType) throws RFXComUnsupportedValueException;

    /**
     * Procedure to set sub type.
     *
     * @param subType
     */
    void setSubType(T subType);

    /**
     * Procedure to set device id.
     *
     * @param deviceId
     * @throws RFXComException
     */
    void setDeviceId(String deviceId) throws RFXComException;

    /**
     * Set the config to be applied to this message
     *
     * @param config
     * @throws RFXComException
     */
    @Override
    void setConfig(RFXComDeviceConfiguration config) throws RFXComException;
}
