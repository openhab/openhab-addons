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
import org.openhab.core.types.Type;

/**
 * This interface defines interface which every message class should implement.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface RFXComMessage {

    /**
     * Procedure for encode raw data.
     *
     * @param data Raw data.
     */
    void encodeMessage(byte[] data) throws RFXComException;

    /**
     * Procedure for decode object to raw data.
     *
     * @return raw data.
     */
    byte[] decodeMessage() throws RFXComException;

    /**
     * Procedure for converting openHAB state to RFXCOM object.
     *
     * @throws RFXComUnsupportedChannelException If we do not support setting this channel
     * @throws RFXComInvalidStateException If the state (type) is invalid for the channel
     */
    void convertFromState(String channelId, Type type)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException;

    /**
     * Procedure to pass configuration to a message
     *
     * @param deviceConfiguration configuration about the device
     * @throws RFXComException if the configuration could not be handled properly
     */
    void setConfig(RFXComDeviceConfiguration deviceConfiguration) throws RFXComException;
}
