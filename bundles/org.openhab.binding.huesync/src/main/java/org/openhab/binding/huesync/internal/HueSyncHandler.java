/**
 * Copyright (c) 2024-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.huesync.internal.api.dto.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.connection.HueSyncConnection;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueSyncHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marco Kawon - Initial contribution
 * @author Patrik Gfeller - Integration into official repository, update to 4.x
 *         infrastructure
 */
@NonNullByDefault
public class HueSyncHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HueSyncHandler.class);

    private @Nullable HueSyncConfiguration config;

    private HttpClient httpClient;

    private HueSyncConnection connection;

    private HueSyncDeviceInfo deviceInfo;

    @SuppressWarnings("null")
    public HueSyncHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClient = new HttpClient(new SslContextFactory.Client(true));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (CHANNEL_1.equals(channelUID.getId())) {
        // if (command instanceof RefreshType) {
        // // TODO: handle data refresh
        // }

        // // TODO: handle command

        // // Note: if communication with thing fails for some reason,
        // // indicate that by setting the status with detail information:
        // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // // "Could not control device at IP address x.x.x.x");
        // }
    }

    // TODO: Check if we can go without the "null" warning ...
    @SuppressWarnings("null")
    @Override
    public void initialize() {
        this.config = getConfigAs(HueSyncConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any
        // network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE
        // or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running
        // connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the
        // real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task
        // decide for the real status.
        // the framework is then able to reuse the resources from the thing handler
        // initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            try {
                this.connection = new HueSyncConnection(this.httpClient, this.config);
                this.connection.start();

                this.deviceInfo = this.connection.getDeviceInfo();

                Map<String, String> properties = editProperties();

                properties.put(Thing.PROPERTY_SERIAL_NUMBER, this.deviceInfo.uniqueId);
                properties.put(Thing.PROPERTY_MODEL_ID, this.deviceInfo.deviceType);
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.firmwareVersion);

                updateProperties(properties);

                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                // TODO: Log message ...
                // TODO: thing status details ...
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details
        // for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not
        // work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        super.dispose();

        try {
            // TODO: Check if we have to unregister openHAB form the Hue HDMI Sync Box
            this.httpClient.stop();
        } catch (Exception e) {
            // TODO: Handle ...
        }
    }
}
