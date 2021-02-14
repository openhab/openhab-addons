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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.Utils;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smart power socket handler with (optional) nightlight and power consumption meter
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkSocketModel2Handler extends BroadlinkSocketHandler {

    private boolean checkPowerConsumption = true;

    public BroadlinkSocketModel2Handler(Thing thing) {
        super(thing, LoggerFactory.getLogger(BroadlinkSocketModel2Handler.class));
    }

    public BroadlinkSocketModel2Handler(Thing thing, Logger logger, boolean checkPowerConsumption) {
        super(thing, logger);
        this.checkPowerConsumption = checkPowerConsumption;
    }

    protected void setStatusOnDevice(int status) throws IOException {
        byte payload[] = new byte[16];
        payload[0] = 2;
        payload[4] = (byte) status;
        byte message[] = buildMessage((byte) 0x6a, payload);
        sendAndReceiveDatagram(message, "Setting SP2/3 status to " + status);
    }

    static int mergeOnOffBits(Command powerOnOff, Command nightLightOnOff) {
        int powerBit = powerOnOff == OnOffType.ON ? 0x01 : 0x00;
        int nightLightBit = nightLightOnOff == OnOffType.ON ? 0x02 : 0x00;
        return powerBit | nightLightBit;
    }

    private static OnOffType deriveOnOffBitFromStatusPayload(byte[] statusPayload, byte mask) {
        byte powerByte = statusPayload[4];
        if ((powerByte & mask) == mask) {
            return OnOffType.ON;
        }
        return OnOffType.OFF;
    }

    static OnOffType derivePowerStateFromStatusByte(byte[] statusPayload) {
        return deriveOnOffBitFromStatusPayload(statusPayload, (byte) 0x01);
    }

    static OnOffType deriveNightLightStateFromStatusByte(byte[] statusPayload) {
        return deriveOnOffBitFromStatusPayload(statusPayload, (byte) 0x02);
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            // Always pull back the latest device status and merge it:
            byte[] statusByte = getStatusByteFromDevice();
            OnOffType powerStatus = derivePowerStateFromStatusByte(statusByte);
            OnOffType nightLightStatus = deriveNightLightStateFromStatusByte(statusByte);

            if (channelUID.getId().equals(BroadlinkBindingConstants.CHANNEL_POWER)) {
                setStatusOnDevice(mergeOnOffBits(command, nightLightStatus));
            }

            if (channelUID.getId().equals(BroadlinkBindingConstants.CHANNEL_NIGHTLIGHT_POWER)) {
                setStatusOnDevice(mergeOnOffBits(powerStatus, command));
            }
        } catch (IOException e) {
            thingLogger.logError("Could not send command to socket device", e);
        }
    }

    protected boolean getStatusFromDevice() {
        try {
            thingLogger.logDebug("SP2/SP3 getting status...");
            byte[] statusByte = getStatusByteFromDevice();
            updateState(BroadlinkBindingConstants.CHANNEL_POWER, derivePowerStateFromStatusByte(statusByte));
            updateState(BroadlinkBindingConstants.CHANNEL_NIGHTLIGHT_POWER,
                    deriveNightLightStateFromStatusByte(statusByte));
            if (checkPowerConsumption) {
                getPowerConsumptionFromDevice();
            }
            return true;
        } catch (Exception ex) {
            thingLogger.logError("Exception while getting status from device", ex);
            return false;
        }
    }

    private static byte[] POWER_CONSUMPTION_REQUEST_BYTES = { 8, 0, (byte) 254, 1, 5, 1, 0, 0, 0, 45, 0, 0, 0, 0, 0,
            0 };

    // Translated from: https://github.com/mjg59/python-broadlink/blob/master/broadlink/switch.py#L215
    // The returned values need to be treated as BCD - thanks to https://github.com/almazik
    protected void getPowerConsumptionFromDevice() {
        try {
            thingLogger.logDebug("SP2/SP3 getting power consumption status...");
            byte message[] = buildMessage((byte) 0x6a, POWER_CONSUMPTION_REQUEST_BYTES);
            byte response[] = sendAndReceiveDatagram(message, "SP2/3 power consumption status bytes");
            byte[] statusBytes = decodeDevicePacket(response);
            byte[] powerBytes = Utils.slice(statusBytes, 5, 8);
            float powerValue = (bcdValue(powerBytes[2]) * 100) + bcdValue(powerBytes[1])
                    + (bcdValue(powerBytes[0]) / 100);
            updateState(BroadlinkBindingConstants.CHANNEL_POWER_CONSUMPTION,
                    new QuantityType<>(powerValue, Units.WATT));
        } catch (Exception ex) {
            thingLogger.logError("Exception while getting power consumption status from device", ex);
        }
    }

    private static int bcdValue(byte b) {
        return (b >> 4) * 10 + (b & 0x0F);
    }

    private byte[] getStatusByteFromDevice() throws IOException {
        byte payload[] = new byte[16];
        payload[0] = 1;
        byte message[] = buildMessage((byte) 0x6a, payload);
        byte response[] = sendAndReceiveDatagram(message, "SP2/3 status byte");
        return decodeDevicePacket(response);
    }

    protected boolean onBroadlinkDeviceBecomingReachable() {
        return getStatusFromDevice();
    }
}
