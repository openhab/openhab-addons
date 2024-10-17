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
package org.openhab.binding.broadlink.internal.handler;

import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

import java.io.IOException;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Smart power socket handler with optional power consumption (SP2s)
 *
 * @author Cato Sognen - Initial contribution
 * @author John Marshall - rework for correct SP2s handling
 */
@NonNullByDefault
public class BroadlinkSocketModel2Handler extends BroadlinkSocketHandler {
    protected boolean supportsPowerConsumptionMeasurement;

    public BroadlinkSocketModel2Handler(Thing thing, boolean supportsPowerConsumptionMeasurement) {
        super(thing);
        this.supportsPowerConsumptionMeasurement = supportsPowerConsumptionMeasurement;
    }

    @Override
    protected void setStatusOnDevice(int status) throws IOException {
        byte payload[] = new byte[16];
        payload[0] = 2;
        payload[4] = (byte) status;
        byte message[] = buildMessage((byte) 0x6a, payload);
        sendAndReceiveDatagram(message, "Setting switch device status to " + status);
    }

    protected OnOffType deriveOnOffBitFromStatusPayload(byte[] statusPayload, byte mask) {
        byte statusByte = statusPayload[4];
        return OnOffType.from((statusByte & mask) == mask);
    }

    OnOffType derivePowerStateFromStatusBytes(byte[] statusPayload) {
        return deriveOnOffBitFromStatusPayload(statusPayload, (byte) 0x01);
    }

    // https://github.com/mjg59/python-broadlink/blob/822b3c326631c1902b5892a83db126291acbf0b6/broadlink/switch.py#L186
    double derivePowerConsumption(byte[] statusPayload) throws IOException {
        if (statusPayload.length > 6) {
            // Bytes are little-endian, at positions 4,5 and 6
            int highByte = statusPayload[6] & 0xFF;
            int midByte = statusPayload[5] & 0xFF;
            int lowByte = statusPayload[4] & 0xFF;
            int intValue = (highByte << 16) + (midByte << 8) + lowByte;
            return (double) intValue / 1000;
        }
        return 0D;
    }

    protected int toPowerOnOffBits(Command powerOnOff) {
        return powerOnOff == OnOffType.ON ? 0x01 : 0x00;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals(COMMAND_POWER_ON)) {
                setStatusOnDevice(toPowerOnOffBits(command));
            }
        } catch (IOException e) {
            logger.warn("Could not send command to socket device: {}", e.getMessage());
        }
    }

    protected void updatePowerConsumption(double consumptionWatts) {
        updateState(POWER_CONSUMPTION_CHANNEL,
                new QuantityType<Power>(consumptionWatts, BROADLINK_POWER_CONSUMPTION_UNIT));
    }

    @Override
    protected void getStatusFromDevice() throws IOException {
        byte[] statusBytes = getStatusBytesFromDevice();
        updateState(COMMAND_POWER_ON, derivePowerStateFromStatusBytes(statusBytes));
        if (supportsPowerConsumptionMeasurement) {
            updatePowerConsumption(derivePowerConsumption(statusBytes));
        }
    }

    protected byte[] getStatusBytesFromDevice() throws IOException {
        byte payload[] = new byte[16];
        payload[0] = 1;
        byte message[] = buildMessage((byte) 0x6a, payload);
        byte response[] = sendAndReceiveDatagram(message, "SP2/SP2s status byte");
        if (response == null) {
            throw new IOException("No response while fetching status byte from SP2/SP2s device");
        }
        return decodeDevicePacket(response);
    }
}
