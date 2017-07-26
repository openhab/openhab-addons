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
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
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
     * Procedure for converting RFXCOM value to openHAB state.
     *
     * @param valueSelector
     *
     * @return openHAB state.
     */
    State convertToState(RFXComValueSelector valueSelector) throws RFXComException;

    /**
     * Procedure for converting openHAB state to RFXCOM object.
     *
     */
    void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException;

    /**
     * Procedure to get device id.
     *
     * @return device Id.
     */
    String getDeviceId() throws RFXComException;

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

    /**
     * Procedure to pass configuration to a message
     *
     * @param deviceConfiguration configuration about the device
     * @throws RFXComException if the configuration could not be handled properly
     */
    void setConfig(RFXComDeviceConfiguration deviceConfiguration) throws RFXComException;
}
