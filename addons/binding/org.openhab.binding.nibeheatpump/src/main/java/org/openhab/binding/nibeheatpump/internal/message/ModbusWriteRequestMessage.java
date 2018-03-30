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
 * The {@link ModbusWriteRequestMessage} implements Nibe write request message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ModbusWriteRequestMessage extends NibeHeatPumpBaseMessage {

    private int coilAddress;
    private int value;

    private ModbusWriteRequestMessage(MessageBuilder builder) {
        super.msgType = MessageType.MODBUS_WRITE_REQUEST_MSG;
        this.coilAddress = builder.coilAddress;
        this.value = builder.value;
    }

    public ModbusWriteRequestMessage(byte[] data) throws NibeHeatPumpException {
        encodeMessage(data);
    }

    public int getCoilAddress() {
        return coilAddress;
    }

    public void setCoildAddress(int coildAddress) {
        this.coilAddress = coildAddress;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void encodeMessage(byte[] data) throws NibeHeatPumpException {
        if (NibeHeatPumpProtocol.isModbus40WriteRequestPdu(data)) {
            super.encodeMessage(data);
            coilAddress = (data[4] & 0xFF) << 8 | (data[3] & 0xFF);
            value = (data[8] & 0xFF) << 24 | (data[7] & 0xFF) << 16 | (data[6] & 0xFF) << 8 | (data[5] & 0xFF);
        } else {
            throw new NibeHeatPumpException("Not Write Request message");
        }
    }

    @Override
    public byte[] decodeMessage() {
        return createModbus40WritePdu(coilAddress, value);
    }

    private byte[] createModbus40WritePdu(int coildAddress, int value) {

        byte[] data = new byte[10];

        data[0] = NibeHeatPumpProtocol.FRAME_START_CHAR_TO_NIBE;
        data[1] = NibeHeatPumpProtocol.CMD_MODBUS_WRITE_REQ;
        data[2] = (byte) 0x06; // data len
        data[3] = (byte) (coildAddress & 0xFF);
        data[4] = (byte) ((coildAddress >> 8) & 0xFF);
        data[5] = (byte) (value & 0xFF);
        data[6] = (byte) ((value >> 8) & 0xFF);
        data[7] = (byte) ((value >> 16) & 0xFF);
        data[8] = (byte) ((value >> 24) & 0xFF);
        data[9] = NibeHeatPumpProtocol.calculateChecksum(data, 0, 9);

        return data;
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Coil address = " + coilAddress;
        str += ", Value = " + value;

        return str;
    }

    public static class MessageBuilder {
        private int coilAddress;
        private int value;

        public MessageBuilder coilAddress(int coilAddress) {
            this.coilAddress = coilAddress;
            return this;
        }

        public MessageBuilder value(int value) {
            this.value = value;
            return this;
        }

        public ModbusWriteRequestMessage build() {
            return new ModbusWriteRequestMessage(this);
        }
    }

    @Override
    public String toHexString() {
        if (rawMessage == null) {
            rawMessage = decodeMessage();
        }

        return super.toHexString();
    }
}
