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
import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpBaseMessage.MessageType;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocol;

/**
 * The {@link MessageFactory} implements factory class to create Nibe protocol messages.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class MessageFactory {

    public static NibeHeatPumpMessage getMessage(byte[] message) throws NibeHeatPumpException {

        if (message != null) {

            byte messageTypeByte = NibeHeatPumpProtocol.getMessageType(message);
            MessageType messageType = NibeHeatPumpBaseMessage.getMessageType(messageTypeByte);

            switch (messageType) {
                case MODBUS_DATA_READ_OUT_MSG:
                    return new ModbusDataReadOutMessage(message);
                case MODBUS_READ_REQUEST_MSG:
                    return new ModbusReadRequestMessage(message);
                case MODBUS_READ_RESPONSE_MSG:
                    return new ModbusReadResponseMessage(message);
                case MODBUS_WRITE_REQUEST_MSG:
                    return new ModbusWriteRequestMessage(message);
                case MODBUS_WRITE_RESPONSE_MSG:
                    return new ModbusWriteResponseMessage(message);
                default:
                    return null;
            }
        }

        throw new NibeHeatPumpException("Illegal message (null)");
    }
}
