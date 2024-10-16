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
import org.openhab.binding.broadlink.internal.BroadlinkRemoteDynamicCommandDescriptionProvider;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;

/**
 * Remote blaster handler
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteModelProHandler extends BroadlinkRemoteHandler {

    public BroadlinkRemoteModelProHandler(Thing thing,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider,
            StorageService storageService) {
        super(thing, commandDescriptionProvider, storageService);
    }

    @Override
    protected void getStatusFromDevice() throws IOException, BroadlinkStatusException {
        byte payload[] = new byte[16];
        payload[0] = 1;
        byte message[] = buildMessage((byte) 0x6a, payload);
        byte response[] = sendAndReceiveDatagram(message, "RM Pro device status");
        if (response == null) {
            throw new BroadlinkStatusException(
                    "response from RM Pro device was null, did you configure the right address for the device?");
        }
        byte decodedPayload[] = decodeDevicePacket(response);
        double temperature = ((decodedPayload[4] * 10 + decodedPayload[5]) / 10D);
        updateTemperature(temperature);
    }
}
