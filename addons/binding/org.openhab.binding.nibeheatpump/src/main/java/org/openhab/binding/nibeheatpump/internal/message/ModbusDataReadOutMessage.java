/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal.message;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocol;

/**
 * The {@link ModbusDataReadOutMessage} implements Nibe data read out message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ModbusDataReadOutMessage extends NibeHeatPumpBaseMessage {

    private List<ModbusValue> values;

    private ModbusDataReadOutMessage(MessageBuilder builder) {
        super.msgType = MessageType.MODBUS_DATA_READ_OUT_MSG;
        this.values = builder.values;
    }

    public ModbusDataReadOutMessage(byte[] data) throws NibeHeatPumpException {
        encodeMessage(data);
    }

    public List<ModbusValue> getValues() {
        return values;
    }

    @Override
    public void encodeMessage(byte[] data) throws NibeHeatPumpException {
        values = parseMessage(data);
    }

    @Override
    public byte[] decodeMessage() {
        return createDataReadOutPdu(values);
    }

    private byte[] createDataReadOutPdu(List<ModbusValue> values) {

        byte datalen = (byte) (values.size() * 4);
        byte msglen = (byte) (6 + datalen);

        byte[] data = new byte[msglen];

        data[0] = NibeHeatPumpProtocol.FRAME_START_CHAR_FROM_NIBE;
        data[1] = 0x00;
        data[2] = NibeHeatPumpProtocol.ADR_MODBUS40;
        data[3] = NibeHeatPumpProtocol.CMD_MODBUS_DATA_MSG;
        data[4] = datalen;

        int i = NibeHeatPumpProtocol.OFFSET_DATA;

        for (ModbusValue value : values) {

            int coildAddress = value.getCoilAddress();
            int val = value.getValue();

            data[i + 0] = (byte) (coildAddress & 0xFF);
            data[i + 1] = (byte) ((coildAddress >> 8) & 0xFF);
            data[i + 2] = (byte) (val & 0xFF);
            data[i + 3] = (byte) ((val >> 8) & 0xFF);
            i += 4;
        }

        data[msglen - 1] = NibeHeatPumpProtocol.calculateChecksum(data, 2, msglen);

        return data;
    }

    @Override
    public String toString() {
        String str = super.toString();
        str += ", Values: ";
        str += values.toString();
        return str;
    }

    private List<ModbusValue> parseMessage(byte[] data) throws NibeHeatPumpException {
        if (NibeHeatPumpProtocol.isModbus40DataReadOut(data)) {
            super.encodeMessage(data);
            final int msglen = 5 + rawMessage[NibeHeatPumpProtocol.OFFSET_LEN];

            List<ModbusValue> vals = new ArrayList<>();

            try {
                for (int i = NibeHeatPumpProtocol.OFFSET_DATA; i < (msglen - 1); i += 4) {

                    int id = ((rawMessage[i + 1] & 0xFF) << 8 | (rawMessage[i + 0] & 0xFF));
                    int value = (short) ((rawMessage[i + 3] & 0xFF) << 8 | (rawMessage[i + 2] & 0xFF));

                    if (id != 0xFFFF) {
                        vals.add(new ModbusValue(id, value));
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new NibeHeatPumpException("Error occurred during data parsing", e);
            }

            return vals;

        } else {
            throw new NibeHeatPumpException("Not Modbus data readout message");
        }
    }

    public static class MessageBuilder {
        private List<ModbusValue> values = new ArrayList<>();

        public MessageBuilder values(List<ModbusValue> values) {
            this.values = values;
            return this;
        }

        public MessageBuilder value(ModbusValue value) {
            this.values.add(value);
            return this;
        }

        public MessageBuilder value(int coilAddress, int value) {
            this.values.add(new ModbusValue(coilAddress, value));
            return this;
        }

        public ModbusDataReadOutMessage build() {
            return new ModbusDataReadOutMessage(this);
        }
    }
}
