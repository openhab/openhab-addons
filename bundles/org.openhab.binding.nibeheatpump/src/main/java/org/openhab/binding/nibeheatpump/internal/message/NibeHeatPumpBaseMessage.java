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
package org.openhab.binding.nibeheatpump.internal.message;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocol;
import org.openhab.core.util.HexUtils;

/**
 * The {@link NibeHeatPumpBaseMessage} define abstract class for Nibe messages. All message implementations should
 * extend this class.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public abstract class NibeHeatPumpBaseMessage implements NibeHeatPumpMessage {

    public static MessageType getMessageType(byte messageType) {
        for (MessageType p : MessageType.values()) {
            if (p.toByte() == messageType) {
                return p;
            }
        }
        return MessageType.UNKNOWN;
    }

    public enum MessageType {
        MODBUS_DATA_READ_OUT_MSG(NibeHeatPumpProtocol.CMD_MODBUS_DATA_MSG),
        MODBUS_READ_REQUEST_MSG(NibeHeatPumpProtocol.CMD_MODBUS_READ_REQ),
        MODBUS_READ_RESPONSE_MSG(NibeHeatPumpProtocol.CMD_MODBUS_READ_RESP),
        MODBUS_WRITE_REQUEST_MSG(NibeHeatPumpProtocol.CMD_MODBUS_WRITE_REQ),
        MODBUS_WRITE_RESPONSE_MSG(NibeHeatPumpProtocol.CMD_MODBUS_WRITE_RESP),

        UNKNOWN(-1);

        private final int msgType;

        MessageType(int msgType) {
            this.msgType = msgType;
        }

        public byte toByte() {
            return (byte) msgType;
        }
    }

    public byte[] rawMessage;
    public MessageType msgType = MessageType.UNKNOWN;

    public NibeHeatPumpBaseMessage() {
    }

    public NibeHeatPumpBaseMessage(byte[] data) throws NibeHeatPumpException {
        encodeMessage(data);
    }

    @Override
    public void encodeMessage(byte[] data) throws NibeHeatPumpException {
        if (data.length >= NibeHeatPumpProtocol.PDU_MIN_LEN) {
            byte[] d = NibeHeatPumpProtocol.checkMessageChecksumAndRemoveDoubles(data);
            rawMessage = d.clone();

            byte messageTypeByte = NibeHeatPumpProtocol.getMessageType(d);
            msgType = NibeHeatPumpBaseMessage.getMessageType(messageTypeByte);
        } else {
            throw new NibeHeatPumpException("Too short message");
        }
    }

    @Override
    public String toString() {
        return "Message type = " + msgType;
    }

    @Override
    public String toHexString() {
        if (rawMessage == null) {
            return null;
        } else {
            return HexUtils.bytesToHex(rawMessage);
        }
    }
}
