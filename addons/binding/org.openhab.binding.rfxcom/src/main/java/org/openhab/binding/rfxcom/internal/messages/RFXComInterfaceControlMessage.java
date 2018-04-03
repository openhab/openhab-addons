/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * RFXCOM data class for control message.
 *
 * @author Mike Jagdis
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComInterfaceControlMessage extends RFXComBaseMessage {
    private byte[] data = new byte[14];

    public RFXComInterfaceControlMessage(RFXComInterfaceMessage.TransceiverType transceiverType,
            RFXComBridgeConfiguration configuration) {
        data[0] = 0x0D;
        data[1] = RFXComBaseMessage.PacketType.INTERFACE_CONTROL.toByte();
        data[2] = 0;
        data[3] = 2;
        data[4] = RFXComInterfaceMessage.Commands.SET_MODE.toByte();
        data[5] = transceiverType.toByte();
        data[6] = (byte) (configuration.transmitPower + 18);

        data[7] = (byte)(
              (configuration.enableUndecoded        ? 0x80 : 0x00)
            | (configuration.enableImagintronixOpus ? 0x40 : 0x00)
            | (configuration.enableByronSX          ? 0x20 : 0x00)
            | (configuration.enableRSL              ? 0x10 : 0x00)
            | (configuration.enableLighting4        ? 0x08 : 0x00)
            | (configuration.enableFineOffsetViking ? 0x04 : 0x00)
            | (configuration.enableRubicson         ? 0x02 : 0x00)
            | (configuration.enableAEBlyss          ? 0x01 : 0x00)
        );

        data[8] = (byte)(
              (configuration.enableBlindsT1T2T3T4   ? 0x80 : 0x00)
            | (configuration.enableBlindsT0         ? 0x40 : 0x00)
            | (configuration.enableProGuard         ? 0x20 : 0x00)
            | (configuration.enableFS20             ? 0x10 : 0x00)
            | (configuration.enableLaCrosse         ? 0x08 : 0x00)
            | (configuration.enableHidekiUPM        ? 0x04 : 0x00)
            | (configuration.enableADLightwaveRF    ? 0x02 : 0x00)
            | (configuration.enableMertik           ? 0x01 : 0x00)
        );

        data[9] = (byte)(
              (configuration.enableVisonic          ? 0x80 : 0x00)
            | (configuration.enableATI              ? 0x40 : 0x00)
            | (configuration.enableOregonScientific ? 0x20 : 0x00)
            | (configuration.enableMeiantech        ? 0x10 : 0x00)
            | (configuration.enableHomeEasyEU       ? 0x08 : 0x00)
            | (configuration.enableAC               ? 0x04 : 0x00)
            | (configuration.enableARC              ? 0x02 : 0x00)
            | (configuration.enableX10              ? 0x01 : 0x00)
        );

        data[10] = (byte)(
              (configuration.enableHomeConfort      ? 0x02 : 0x00)
            | (configuration.enableKEELOQ           ? 0x01 : 0x00)
        );
        data[11] = 0;
        data[12] = 0;
        data[13] = 0;
    }

    public RFXComInterfaceControlMessage(byte[] data) throws RFXComException {
        // We should never receive control messages
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] decodeMessage() {
        return data;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void convertFromState(String channelId, Type type) {
        throw new UnsupportedOperationException();
    }
}
