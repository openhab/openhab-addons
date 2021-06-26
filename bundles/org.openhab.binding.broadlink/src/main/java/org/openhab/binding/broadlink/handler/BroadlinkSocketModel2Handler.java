/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.handler;

import static org.openhab.binding.broadlink.BroadlinkBindingConstants.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    protected void setStatusOnDevice(int status) throws IOException {
        byte payload[] = new byte[16];
        payload[0] = 2;
        payload[4] = (byte) status;
        byte message[] = buildMessage((byte) 0x6a, payload);
        sendAndReceiveDatagram(message, "Setting switch device status to " + status);
    }

    protected OnOffType deriveOnOffBitFromStatusPayload(byte[] statusPayload, byte mask) {
        byte statusByte = statusPayload[4];
        if ((statusByte & mask) == mask) {
            return OnOffType.ON;
        }
        return OnOffType.OFF;
    }

    OnOffType derivePowerStateFromStatusBytes(byte[] statusPayload) {
        return deriveOnOffBitFromStatusPayload(statusPayload, (byte) 0x01);
    }

    double derivePowerConsumption(byte[] statusPayload) throws IOException {
        if (statusPayload.length > 7) {
            ByteBuffer bb = ByteBuffer.wrap(statusPayload).order(ByteOrder.LITTLE_ENDIAN);
            int intValue = bb.getInt(4);
            return (double) intValue / 1000;
        }
        return 0D;
    }

    protected int toPowerOnOffBits(Command powerOnOff) {
        return powerOnOff == OnOffType.ON ? 0x01 : 0x00;
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals(COMMAND_POWER_ON)) {
                setStatusOnDevice(toPowerOnOffBits(command));
            }
        } catch (IOException e) {
            logger.warn("Could not send command to socket device", e);
        }
    }

    protected void updatePowerConsumption(double consumptionWatts) {
        updateState(CHANNEL_POWER_CONSUMPTION,
                new QuantityType<Power>(consumptionWatts, BROADLINK_POWER_CONSUMPTION_UNIT));
    }

    protected boolean getStatusFromDevice() {
        try {
            logger.debug("SP2/SP2s getting status...");
            byte[] statusBytes = getStatusBytesFromDevice();
            updateState(COMMAND_POWER_ON, derivePowerStateFromStatusBytes(statusBytes));
            if (supportsPowerConsumptionMeasurement) {
                updatePowerConsumption(derivePowerConsumption(statusBytes));
            }
            return true;
        } catch (Exception ex) {
            logger.warn("Exception while getting status from device", ex);
            return false;
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

    protected boolean onBroadlinkDeviceBecomingReachable() {
        return getStatusFromDevice();
    }
}
