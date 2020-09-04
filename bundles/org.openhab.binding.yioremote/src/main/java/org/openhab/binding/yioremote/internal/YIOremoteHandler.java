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

import static org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEHANDLESTATUS;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEMESSAGETYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    private @Nullable JsonObject JsonObject_recievedJsonObject;
    private YIOREMOTEHANDLESTATUS YIOREMOTEHANDLESTATUS_actualstatus = YIOREMOTEHANDLESTATUS.UNINITIALIZED;
    private @Nullable Future<?> pollingJob;
    private String send_ircode = "";

    public YIOremoteHandler(Thing thing) {
        super(thing);
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
            YIOREMOTEHANDLESTATUS_actualstatus = YIOREMOTEHANDLESTATUS.AUTHENTICATION_PROCESS;
            try {

                logger.debug("Starting websocket Client");
                YIOremote_DockwebSocketClient.start();
                logger.debug("Started websocket Client");
            } catch (Exception e) {
                logger.warn("Web socket start failed {}", e);
                // throw new IOException("Web socket start failed");
            }
            try {
                logger.debug("Connect websocket client");
                YIOremote_DockwebSocketClient.connect(YIOremote_DockwebSocketClientSocket, URI_yiodockwebsocketaddress,
                        YIOremote_DockwebSocketClientrequest);
                logger.debug("Connected websocket client");

                logger.debug("Check for authentication requested by YIO Dock");
                YIOremote_DockwebSocketClientSocket.getLatch().await();
                Thread.sleep(1000);

                try {
                    if (YIOremote_DockwebSocketClientSocket.get_boolean_authentication_required()) {
                        logger.debug("send authentication to YIO dock");
                        YIOremote_DockwebSocketClientSocket.sendMessage(YIOREMOTEMESSAGETYPE.AUTHENTICATE,
                                config.yiodockaccesstoken);
                        Thread.sleep(1000);

                        if (YIOremote_DockwebSocketClientSocket.get_boolean_authentication_ok()) {
                            YIOREMOTEHANDLESTATUS_actualstatus = YIOREMOTEHANDLESTATUS.AUTHENTICATED;

                        } else {
                            logger.debug("authentication to YIO dock not ok");
                            YIOREMOTEHANDLESTATUS_actualstatus = YIOREMOTEHANDLESTATUS.AUTHENTICATED_FAILED;
                        }
                    } else {
                        logger.debug("authentication error YIO dock");
                    }

                } catch (IllegalArgumentException e) {
                    logger.warn("JSON convertion failure {}", e);
                }

            } catch (

            Exception e) {
                logger.warn("Web socket connect failed {}", e);
                // throw new IOException("Web socket start failed");
            }
        } catch (URISyntaxException e) {
            logger.debug("Initialize web socket failed {}", e);
        }

        if (YIOREMOTEHANDLESTATUS_actualstatus.equals(YIOREMOTEHANDLESTATUS.AUTHENTICATED)) {
            updateStatus(ThingStatus.ONLINE);
            Runnable heartbeatpolling = new Runnable() {
                @Override
                public void run() {
                    if (YIOREMOTEHANDLESTATUS_actualstatus.equals(YIOREMOTEHANDLESTATUS.AUTHENTICATED)) {
                        YIOremote_DockwebSocketClientSocket.sendMessage(YIOREMOTEMESSAGETYPE.HEARTBEAT, "");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (YIOremote_DockwebSocketClientSocket.get_boolean_heartbeat()) {
                            logger.debug("heartbeat ok");
                            updateChannelString(GROUP_OUTPUT, YIODOCKSTATUS,
                                    YIOremote_DockwebSocketClientSocket.get_string_receivedstatus());
                        } else {
                            logger.warn("Connection lost no ping from YIO DOCK");
                            YIOREMOTEHANDLESTATUS_actualstatus = YIOREMOTEHANDLESTATUS.CONNECTION_FAILED;
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "Connection lost no ping from YIO DOCK");
                            pollingJob.cancel(true);
                        }
                    } else {
                        logger.warn("Connection lost no ping from YIO DOCK");
                        YIOREMOTEHANDLESTATUS_actualstatus = YIOREMOTEHANDLESTATUS.CONNECTION_FAILED;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Connection lost no ping from YIO DOCK");
                        pollingJob.cancel(true);
                    }
                }
            };
            pollingJob = scheduler.scheduleWithFixedDelay(heartbeatpolling, 0, 30, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
        /*
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

        logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        pollingJob.cancel(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (YIODOCKRECEIVERSWITCH.equals(channelUID.getIdWithoutGroup())) {
            if (YIOREMOTEHANDLESTATUS_actualstatus.equals(YIOREMOTEHANDLESTATUS.AUTHENTICATED)) {
                if (command instanceof RefreshType) {
                    // TODO: handle data refresh

                }

                if (command.toString().equals("ON")) {
                    logger.debug("YIODOCKRECEIVERSWITCH ON procedure: Switching IR Receiver on");
                    YIOremote_DockwebSocketClientSocket.sendMessage(YIOREMOTEMESSAGETYPE.IRRECEIVERON, "");
                } else if (command.toString().equals("OFF")) {
                    logger.debug("YIODOCKRECEIVERSWITCH OFF procedure: Switching IR Receiver off");
                    YIOremote_DockwebSocketClientSocket.sendMessage(YIOREMOTEMESSAGETYPE.IRRECEIVEROFF, "");
                } else {
                    logger.debug("YIODOCKRECEIVERSWITCH no procedure");
                }

            } else {
                logger.warn("YIOremoteHandler not authenticated");
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        } else if (YIODOCKSENDIRCODE.equals(channelUID.getIdWithoutGroup())) {
            if (YIOREMOTEHANDLESTATUS_actualstatus.equals(YIOREMOTEHANDLESTATUS.AUTHENTICATED)) {
                if (command instanceof RefreshType) {
                    // TODO: handle data refresh
                    // logger.warn("YIOremoteHandler not authenticated");
                }
                logger.debug("YIODOCKSENDIRCODE procedure: {}", command.toString());
                send_ircode = command.toString();
                if (send_ircode.matches("[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                    YIOremote_DockwebSocketClientSocket.sendMessage(YIOREMOTEMESSAGETYPE.IRSEND, send_ircode);
                } else {
                    logger.warn("Wrong IR code Format {}", send_ircode);
                    send_ircode = "";
                }
            } else {
                logger.warn("YIOremoteHandler not authenticated");
            }
        }
    }

    private JsonObject StringtoJsonObject(String jsonString) {
        logger.debug("StringtoJsonElement function called");
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(jsonString);

        JsonObject result = null;
        if (jsonElement instanceof JsonObject) {
            result = jsonElement.getAsJsonObject();
        } else {
            logger.debug("{} is not valid JSON stirng", jsonString);
            throw new IllegalArgumentException(jsonString + " is not valid JSON stirng");
        }
        return result;
    }

    protected void updateChannelString(String group, String channelId, String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);

        if (isLinked(id)) {
            updateState(id, new StringType(value));
        }

    }
}
