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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Smart power socket handler with (optional) nightlight
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkSocketModel2Handler extends BroadlinkSocketHandler {

    public BroadlinkSocketModel2Handler(Thing thing) {
        super(thing);
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

            if (channelUID.getId().equals("powerOn")) {
                setStatusOnDevice(mergeOnOffBits(command, nightLightStatus));
            }

            if (channelUID.getId().equals("nightLight")) {
                setStatusOnDevice(mergeOnOffBits(powerStatus, command));
            }
        } catch (IOException e) {
            logger.warn("Could not send command to socket device", e);
        }
    }

    protected boolean getStatusFromDevice() {
        try {
            logger.debug("SP2/SP3 getting status...");
            byte[] statusByte = getStatusByteFromDevice();
            updateState("powerOn", derivePowerStateFromStatusByte(statusByte));
            updateState("nightLight", deriveNightLightStateFromStatusByte(statusByte));
            return true;
        } catch (Exception ex) {
            logger.warn("Exception while getting status from device", ex);
            return false;
        }
    }

    private byte[] getStatusByteFromDevice() throws IOException {
        byte payload[] = new byte[16];
        payload[0] = 1;
        byte message[] = buildMessage((byte) 0x6a, payload);
        byte response[] = sendAndReceiveDatagram(message, "SP2/3 status byte");
        if (response == null) {
            throw new IOException("No response while fetching status byte from SP2/3 device");
        }
        return decodeDevicePacket(response);
    }

    protected boolean onBroadlinkDeviceBecomingReachable() {
        return getStatusFromDevice();
    }
}
