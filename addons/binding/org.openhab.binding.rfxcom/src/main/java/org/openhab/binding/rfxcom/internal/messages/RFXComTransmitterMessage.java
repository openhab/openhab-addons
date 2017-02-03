/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

import java.util.List;

/**
 * RFXCOM data class for transmitter message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComTransmitterMessage extends RFXComBaseMessage {

    public enum SubType {
        ERROR_RECEIVER_DID_NOT_LOCK(0),
        RESPONSE(1),

        UNKNOWN(255);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        SubType(byte subType) {
            this.subType = subType;
        }

        public byte toByte() {
            return (byte) subType;
        }

        public static SubType fromByte(int input) {
            for (SubType c : SubType.values()) {
                if (c.subType == input) {
                    return c;
                }
            }

            return SubType.UNKNOWN;
        }
    }

    public enum Response {
        ACK(0), // ACK, transmit OK
        ACK_DELAYED(1), // ACK, but transmit started after 3 seconds delay
                        // anyway with RF receive data
        NAK(2), // NAK, transmitter did not lock on the requested transmit
                // frequency
        NAK_INVALID_AC_ADDRESS(3), // NAK, AC address zero in id1-id4 not
                                   // allowed

        UNKNOWN(255);

        private final int response;

        Response(int response) {
            this.response = response;
        }

        Response(byte response) {
            this.response = response;
        }

        public byte toByte() {
            return (byte) response;
        }

        public static Response fromByte(int input) {
            for (Response response : Response.values()) {
                if (response.response == input) {
                    return response;
                }
            }

            return Response.UNKNOWN;
        }
    }

    public SubType subType = SubType.UNKNOWN;
    public Response response = Response.UNKNOWN;

    public RFXComTransmitterMessage() {
        packetType = PacketType.TRANSMITTER_MESSAGE;
    }

    public RFXComTransmitterMessage(byte[] data) {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();

        if (subType == SubType.RESPONSE) {
            str += ", Sub type = " + subType;
            str += ", Response = " + response;
        } else {
            str += ", Sub type = " + subType;
            // Response not used
        }

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        response = Response.fromByte(data[4]);
    }

    @Override
    public byte[] decodeMessage() {

        byte[] data = new byte[5];

        data[0] = 0x04;
        data[1] = RFXComBaseMessage.PacketType.TRANSMITTER_MESSAGE.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = response.toByte();

        return data;
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

        for (SubType s : SubType.values()) {
            if (s.toString().equals(subType)) {
                return s;
            }
        }

        // try to find sub type by number
        try {
            return SubType.values()[Integer.parseInt(subType)];
        } catch (Exception e) {
            throw new RFXComException("Unknown sub type " + subType);
        }
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
