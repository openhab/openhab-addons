/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.govee.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.govee.internal.model.StatusResponse;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link RefreshStatusReceiver} is a thread that handles the answers of all devices
 * Therefore it needs to apply the information it to the right thing.
 *
 * Discovery uses the same response code, so we must not refresh the status during discovery
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class RefreshStatusReceiver implements Runnable {
    final Logger logger = LoggerFactory.getLogger(RefreshStatusReceiver.class);
    private static final Gson GSON = new Gson();

    public RefreshStatusReceiver() {
    }

    @Override
    public void run() {
        if (GoveeDiscoveryService.isDiscoveryActive()) {
            logger.debug("Not running refresh as Scan is currently active");
        }

        GoveeHandler.refreshJobRunning = true;
        logger.trace("REFRESH: running refresh cycle for {} devices", GoveeHandler.getThingHandlers().size());

        if (GoveeHandler.getThingHandlers().isEmpty()) {
            return;
        }

        GoveeHandler thingHandler;

        try (MulticastSocket socket = new MulticastSocket(GoveeHandler.RECEIVEFROMDEVICE_PORT)) {
            byte[] buffer = new byte[10240];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.setReuseAddress(true);
            logger.debug("waiting for Status");
            socket.receive(packet);

            String response = new String(packet.getData()).trim();
            String deviceIPAddress = packet.getAddress().toString().replace("/", "");
            logger.trace("Response from {} = {}", deviceIPAddress, response);
            logger.trace("received = {} from {}", response, deviceIPAddress);

            thingHandler = GoveeHandler.getThingHandlers().get(deviceIPAddress);
            if (thingHandler == null) {
                logger.warn("thing Handler for {} couldn't be found.", deviceIPAddress);
                return;
            }

            logger.debug("updating status for thing {} ", thingHandler.getThing().getLabel());

            if (!response.isEmpty()) {
                try {
                    StatusResponse statusMessage = GSON.fromJson(response, StatusResponse.class);
                    thingHandler.updateDeviceState(statusMessage);
                } catch (JsonSyntaxException jse) {
                    thingHandler.statusUpdate(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            jse.getMessage());
                }
            } else {
                thingHandler.statusUpdate(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error.empty-response");
            }
            if (!thingHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                thingHandler.statusUpdate(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            logger.warn("exception when receiving status packet {}", Arrays.toString(e.getStackTrace()));
            // as we haven't received a packet we also don't know where it should have come from
            // hence, we don't know which thing put offline.
            // a way to monitor this would be to keep track in a list, which device answers we expect
            // and supervise an expected answer within a given time but that will make the whole
            // mechanism much more complicated and may be added in the future
        } finally {
            GoveeHandler.refreshJobRunning = false;
        }
    }
}
