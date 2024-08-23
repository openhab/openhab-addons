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
 * The {@link ModbusReadResponseMessage} implements Nibe read response message.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ModbusReadResponseMessage extends NibeHeatPumpBaseMessage {

    private int coilAddress;
    private int value;

    private ModbusReadResponseMessage(MessageBuilder builder) {
        super.msgType = MessageType.MODBUS_READ_RESPONSE_MSG;
        this.coilAddress = builder.coilAddress;
        this.value = builder.value;
    }

    public ModbusReadResponseMessage(byte[] data) throws NibeHeatPumpException {
        encodeMessage(data);
    }

    public int getCoilAddress() {
        return coilAddress;
    }

    public void setCoilAddress(int coilAddress) {
        this.coilAddress = coilAddress;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ModbusValue getValueAsModbusValue() {
        return new ModbusValue(coilAddress, value);
    }

    @Override
    public void encodeMessage(byte[] data) throws NibeHeatPumpException {
        if (NibeHeatPumpProtocol.isModbus40ReadResponse(data)) {
            super.encodeMessage(data);
            coilAddress = ((rawMessage[NibeHeatPumpProtocol.RES_OFFS_DATA + 1] & 0xFF) << 8
                    | (rawMessage[NibeHeatPumpProtocol.RES_OFFS_DATA + 0] & 0xFF));
            value = (rawMessage[NibeHeatPumpProtocol.RES_OFFS_DATA + 5] & 0xFF) << 24
                    | (rawMessage[NibeHeatPumpProtocol.RES_OFFS_DATA + 4] & 0xFF) << 16
                    | (rawMessage[NibeHeatPumpProtocol.RES_OFFS_DATA + 3] & 0xFF) << 8
                    | (rawMessage[NibeHeatPumpProtocol.RES_OFFS_DATA + 2] & 0xFF);

        } else {
            throw new NibeHeatPumpException("Not Read Response message");
        }
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[12];

        data[0] = NibeHeatPumpProtocol.FRAME_START_CHAR_RES;
        data[1] = 0x00;
        data[2] = NibeHeatPumpProtocol.ADR_MODBUS40;
        data[3] = NibeHeatPumpProtocol.CMD_MODBUS_READ_RESP;
        data[4] = (byte) 0x06; // data len

        data[5] = (byte) (coilAddress & 0xFF);
        data[6] = (byte) ((coilAddress >> 8) & 0xFF);

        data[7] = (byte) (value & 0xFF);
        data[8] = (byte) ((value >> 8) & 0xFF);
        data[9] = (byte) ((value >> 16) & 0xFF);
        data[10] = (byte) ((value >> 24) & 0xFF);

        data[11] = NibeHeatPumpProtocol.calculateChecksum(data, 2, 11);

        return data;
    }

    @Override
    public String toString() {
        String str = super.toString();
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

        public ModbusReadResponseMessage build() {
            return new ModbusReadResponseMessage(this);
        }
    }
}
