/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * An interface for message about devices, so interface message do not (have to) implement this
 *
 * @author Martin van Wingerden - Simplify some code in the RFXCOM binding
 */
public interface RFXComDeviceMessage<T> extends RFXComMessage {
    /**
     * Procedure for converting RFXCOM value to openHAB state.
     *
     * @param channelId id of the channel
     * @return openHAB state.
     * @throws RFXComUnsupportedChannelException if the channel is not supported
     */
    State convertToState(String channelId) throws RFXComUnsupportedChannelException;

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
