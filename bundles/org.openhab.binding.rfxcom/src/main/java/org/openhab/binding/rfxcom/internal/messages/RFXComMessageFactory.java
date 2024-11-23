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
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;

/**
 * Factory to create RFXCom messages from either bytes delivered by the RFXCom device
 * or from openhab state to transmit.
 *
 * @author Pauli Anttila - Initial contribution
 * @author James Hewitt-Thomas - Convert to interface to allow dependency injection
 * @author James Hewitt-Thomas - Switch to making messages for a specific command
 */
public interface RFXComMessageFactory {
    RFXComMessage createMessage(PacketType packetType, RFXComDeviceConfiguration config, ChannelUID channelUID,
            Command command) throws RFXComException;

    RFXComMessage createMessage(byte[] packet) throws RFXComException;
}
