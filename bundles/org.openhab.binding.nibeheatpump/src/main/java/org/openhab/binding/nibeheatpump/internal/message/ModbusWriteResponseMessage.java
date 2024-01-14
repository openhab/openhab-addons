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

/**
 * The {@link ModbusWriteResponseMessage} implements Nibe write response message.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ModbusWriteResponseMessage extends NibeHeatPumpBaseMessage {

    private boolean result;

    private ModbusWriteResponseMessage(MessageBuilder builder) {
        super.msgType = MessageType.MODBUS_WRITE_RESPONSE_MSG;
        this.result = builder.result;
    }

    public ModbusWriteResponseMessage(byte[] data) throws NibeHeatPumpException {
        encodeMessage(data);
    }

    @Override
    public void encodeMessage(byte[] data) throws NibeHeatPumpException {
        if (NibeHeatPumpProtocol.isModbus40WriteResponsePdu(data)) {
            super.encodeMessage(data);
            result = rawMessage[NibeHeatPumpProtocol.RES_OFFS_DATA] == 1;
        } else {
            throw new NibeHeatPumpException("Not Write Response message");
        }
    }

    public boolean isSuccessfull() {
        return result;
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[7];

        data[0] = NibeHeatPumpProtocol.FRAME_START_CHAR_RES;
        data[1] = 0x00;
        data[2] = NibeHeatPumpProtocol.ADR_MODBUS40;
        data[3] = NibeHeatPumpProtocol.CMD_MODBUS_WRITE_RESP;
        data[4] = (byte) 0x01; // data len
        data[5] = result ? (byte) 0x01 : (byte) 0x00;
        data[6] = NibeHeatPumpProtocol.calculateChecksum(data, 2, 6);

        return data;
    }

    @Override
    public String toString() {
        String str = super.toString();
        str += ", Result = " + result;

        return str;
    }

    public static class MessageBuilder {
        private boolean result;

        public MessageBuilder result(boolean result) {
            this.result = result;
            return this;
        }

        public ModbusWriteResponseMessage build() {
            return new ModbusWriteResponseMessage(this);
        }
    }
}
