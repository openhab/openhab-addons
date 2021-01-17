/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.*;
import org.slf4j.LoggerFactory;

/**
 * Remote blaster handler
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteModel2Handler extends BroadlinkRemoteHandler {

    public BroadlinkRemoteModel2Handler(Thing thing) {
        super(thing, LoggerFactory.getLogger(BroadlinkRemoteModel2Handler.class));
    }

    protected boolean onBroadlinkDeviceBecomingReachable() {
        return getStatusFromDevice();
    }

    protected boolean getStatusFromDevice() {
        try {
            byte payload[] = new byte[16];
            payload[0] = 1;
            byte message[] = buildMessage((byte) 0x6a, payload);
            byte response[] = sendAndReceiveDatagram(message, "RM2 device status");
            byte decodedPayload[] = decodeDevicePacket(response);
            float temperature = (float) ((double) (decodedPayload[4] * 10 + decodedPayload[5]) / 10D);
            updateState("temperature", new DecimalType(temperature));
            return true;
        } catch (Exception e) {
            thingLogger.logError("Could not get status: ", e);
            return false;
        }
    }
}
