/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;

/**
 * This interface defines interface which every message class should implement.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface RFXComMessage {

    /**
     * Procedure for encode raw data.
     *
     * @param data
     *            Raw data.
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
     */
    void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException;

    /**
     * Procedure to pass configuration to a message
     *
     * @param deviceConfiguration configuration about the device
     * @throws RFXComException if the configuration could not be handled properly
     */
    void setConfig(RFXComDeviceConfiguration deviceConfiguration) throws RFXComException;
}
