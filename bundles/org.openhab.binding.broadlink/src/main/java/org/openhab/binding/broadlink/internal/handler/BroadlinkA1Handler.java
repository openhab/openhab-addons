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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.ModelMapper;
import org.openhab.core.thing.Thing;

/**
 * Handles the A1 environmental sensor.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkA1Handler extends BroadlinkBaseThingHandler {

    public BroadlinkA1Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected void getStatusFromDevice() throws IOException, BroadlinkException {
        byte payload[];
        payload = new byte[16];
        payload[0] = 1;

        byte message[] = buildMessage((byte) 0x6a, payload);

        byte[] response = sendAndReceiveDatagram(message, "A1 device status");
        if (response == null) {
            throw new BroadlinkStatusException("No status response received.");
        }
        byte decryptResponse[] = decodeDevicePacket(response);
        double temperature = ((decryptResponse[4] * 10 + decryptResponse[5]) / 10D);
        logger.trace("A1 getStatusFromDevice got temperature {}", temperature);

        updateTemperature(temperature);
        updateHumidity((decryptResponse[6] * 10 + decryptResponse[7]) / 10D);
        updateState(BroadlinkBindingConstants.LIGHT_CHANNEL, ModelMapper.getLightValue(decryptResponse[8]));
        updateState(BroadlinkBindingConstants.AIR_CHANNEL, ModelMapper.getAirValue(decryptResponse[10]));
        updateState(BroadlinkBindingConstants.NOISE_CHANNEL, ModelMapper.getNoiseValue(decryptResponse[12]));
    }
}
