/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.types.Type;

/**
 * RFXCOM data class for transmitter message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComTransmitterMessage extends RFXComBaseMessage {

    public enum SubType implements ByteEnumWrapper {
        ERROR_RECEIVER_DID_NOT_LOCK(0),
        RESPONSE(1);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public enum Response implements ByteEnumWrapper {
        ACK(0), // ACK, transmit OK
        ACK_DELAYED(1), // ACK, but transmit started after 3 seconds delay
                        // anyway with RF receive data
        NAK(2), // NAK, transmitter did not lock on the requested transmit
                // frequency
        NAK_INVALID_AC_ADDRESS(3); // NAK, AC address zero in id1-id4 not
                                   // allowed

        private final int response;

        Response(int response) {
            this.response = response;
        }

        @Override
        public byte toByte() {
            return (byte) response;
        }
    }

    public SubType subType;
    public Response response;

    public RFXComTransmitterMessage() {
        super(PacketType.TRANSMITTER_MESSAGE);
    }

    public RFXComTransmitterMessage(byte[] data) throws RFXComException {
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
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        response = fromByte(Response.class, data[4]);
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
    public void convertFromState(String channelId, Type type) {
        throw new UnsupportedOperationException();
    }
}
