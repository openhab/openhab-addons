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
package org.openhab.binding.yioremote.internal;

import static org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.CHANNEL_1;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YIOremoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class YIOremoteHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(YIOremoteHandler.class);

    private @Nullable YIOremoteConfiguration config;
    private WebSocketClient YIOremote_DockwebSocketClient = new WebSocketClient();
    private YIOremoteWebsocket YIOremote_DockwebSocketClientSocket = new YIOremoteWebsocket();
    private ClientUpgradeRequest YIOremote_DockwebSocketClientrequest = new ClientUpgradeRequest();
    private @Nullable URI URI_yiodockwebsocketaddress;
    String dest = "ws://192.168.178.21:946/";
    WebSocketClient client = new WebSocketClient();

    public YIOremoteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(YIOremoteConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);
        try {
            logger.debug("Starting generating URI_yiodockwebsocketaddress");
            URI_yiodockwebsocketaddress = new URI("ws://" + config.yiodockhostip + ":946");
            logger.debug("Finished generating URI_yiodockwebsocketaddress");

            try {

                logger.debug("Starting websocket Client");
                YIOremote_DockwebSocketClient.start();
                logger.debug("Started websocket Client");
            } catch (Exception e) {
                logger.warn("Web socket start failed", e);
                // throw new IOException("Web socket start failed");
            }
            try {
                logger.debug("Connect websocket client");
                YIOremote_DockwebSocketClient.connect(YIOremote_DockwebSocketClientSocket, URI_yiodockwebsocketaddress,
                        YIOremote_DockwebSocketClientrequest);
                logger.debug("Connected websocket client");

                YIOremote_DockwebSocketClientSocket.getLatch().await();
                YIOremote_DockwebSocketClientSocket.sendMessage("echo");
                YIOremote_DockwebSocketClientSocket.sendMessage("test");
            } catch (Exception e) {
                logger.warn("Web socket connect failed " + e.toString(), e);
                // throw new IOException("Web socket start failed");
            }
        } catch (URISyntaxException e) {
            logger.debug("Initialize web socket failed", e);
        }

        /*
         * try {
         * URI uri;
         * uri = new URI("ws://" + config.yiodockhostip + ":946");
         *
         * try {
         * logger.debug("Starting websocket Client");
         * YIOremote_DockwebSocketClient.start();
         * } catch (Exception e) {
         * logger.warn("Web socket start failed", e);
         * // throw new IOException("Web socket start failed");
         * }
         *
         * try {
         * logger.debug("Connecting to: {}...", uri);
         * YIOremote_DockwebSocketClientSession = YIOremote_DockwebSocketClient
         * .connect(this, uri, new ClientUpgradeRequest()).get();
         * } catch (Exception e) {
         * logger.warn("Web socket connect failed " + e.toString(), e);
         * // throw new IOException("Web socket start failed");
         * }
         *
         * } catch (URISyntaxException e) {
         * logger.debug("Initialize web socket failed", e);
         * }
         * if (YIOremote_DockwebSocketClientSession != null) {
         * updateStatus(ThingStatus.ONLINE);
         * } else {
         * updateStatus(ThingStatus.OFFLINE);
         * }
         * // Example for background initialization:
         * /*
         * scheduler.execute(() -> {
         * boolean thingReachable = true; // <background task with long running initialization here>
         * // when done do:
         * if (thingReachable) {
         * updateStatus(ThingStatus.ONLINE);
         * } else {
         * updateStatus(ThingStatus.OFFLINE);
         * }
         * });
         */

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
