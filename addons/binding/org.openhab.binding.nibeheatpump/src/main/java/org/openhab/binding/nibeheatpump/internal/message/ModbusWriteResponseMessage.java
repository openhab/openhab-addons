/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        result = modbus40WriteSuccess(data);
    }

    public boolean isSuccessfull() {
        return result;
    }

    @Override
    public byte[] decodeMessage() {
        return createModbusWriteResponsePdu(result);
    }

    private byte[] createModbusWriteResponsePdu(boolean result) {
        byte[] data = new byte[7];

        data[0] = NibeHeatPumpProtocol.FRAME_START_CHAR_FROM_NIBE;
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

    private boolean modbus40WriteSuccess(byte[] data) throws NibeHeatPumpException {
        if (NibeHeatPumpProtocol.isModbus40WriteResponsePdu(data)) {
            super.encodeMessage(data);
            return data[NibeHeatPumpProtocol.OFFSET_DATA] == 1;
        }
        throw new NibeHeatPumpException("Not Write Response message");
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
