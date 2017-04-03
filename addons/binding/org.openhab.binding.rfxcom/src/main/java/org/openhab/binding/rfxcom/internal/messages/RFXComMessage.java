/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.util.List;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * This interface defines interface which every message class should implement.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface RFXComMessage {

    /**
     * Procedure for present class information in string format. Used for
     * logging purposes.
     *
     */
    @Override
    String toString();

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
     * Procedure for converting RFXCOM value to Openhab state.
     *
     * @param valueSelector
     *
     * @return Openhab state.
     */
    State convertToState(RFXComValueSelector valueSelector) throws RFXComException;

    /**
     * Procedure for converting Openhab state to RFXCOM object.
     *
     */
    void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException;

    /**
     * Procedure for converting sub type as string to sub type object.
     *
     * @return sub type object.
     */
    Object convertSubType(String subType) throws RFXComException;

    /**
     * Procedure to set sub type.
     *
     */
    void setSubType(Object subType) throws RFXComException;

    /**
     * Procedure to get device id.
     *
     * @return device Id.
     */
    String getDeviceId() throws RFXComException;

    /**
     * Procedure to set device id.
     *
     */
    void setDeviceId(String deviceId) throws RFXComException;

    /**
     * Procedure for get supported value selector list for input values.
     *
     * @return List of supported value selectors.
     */
    List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException;

    /**
     * Procedure for get supported value selector list for output values.
     *
     * @return List of supported value selectors.
     */
    List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException;

}
