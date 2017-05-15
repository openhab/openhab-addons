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
 * RFXCOM data class for control message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComControlMessage extends RFXComBaseMessage {

    public RFXComControlMessage() {

    }

    public RFXComControlMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public byte[] decodeMessage() {
        return null;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public Object convertSubType(String subType) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return null;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return null;
    }
}
