/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link ModbusReadRequestMessage} implements Nibe read request message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ModbusReadRequestMessage extends NibeHeatPumpBaseMessage {

    private int coilAddress;

    private ModbusReadRequestMessage(MessageBuilder builder) {
        super.msgType = MessageType.MODBUS_READ_REQUEST_MSG;
        this.coilAddress = builder.coilAddress;
    }

    ModbusReadRequestMessage(byte[] data) throws NibeHeatPumpException {
        encodeMessage(data);
    }

    public int getCoilAddress() {
        return coilAddress;
    }

    @Override
    public void encodeMessage(byte[] data) throws NibeHeatPumpException {
        if (NibeHeatPumpProtocol.isModbus40ReadRequestPdu(data)) {
            super.encodeMessage(data);
            coilAddress = (rawMessage[4] & 0xFF) << 8 | (rawMessage[3] & 0xFF);
        } else {
            throw new NibeHeatPumpException("Not Read Request message");
        }
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[6];
        data[0] = NibeHeatPumpProtocol.FRAME_START_CHAR_REQ;
        data[1] = NibeHeatPumpProtocol.CMD_MODBUS_READ_REQ;
        data[2] = (byte) 0x02; // data len
        data[3] = (byte) (coilAddress & 0xFF);
        data[4] = (byte) ((coilAddress >> 8) & 0xFF);
        data[5] = NibeHeatPumpProtocol.calculateChecksum(data, 0, 5);

        return data;
    }

    @Override
    public String toString() {
        String str = super.toString();
        str += ", Coil address = " + coilAddress;

        return str;
    }

    public static class MessageBuilder {
        private int coilAddress;

        public MessageBuilder coilAddress(int coilAddress) {
            this.coilAddress = coilAddress;
            return this;
        }

        public ModbusReadRequestMessage build() {
            return new ModbusReadRequestMessage(this);
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
