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
import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEMESSAGETYPE;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIO_REMOTE_DOCK_HANDLE_STATUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YIOremoteDockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class YIOremoteDockHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(YIOremoteDockHandler.class);

    YIOremoteConfiguration localConfig = getConfigAs(YIOremoteConfiguration.class);
    private WebSocketClient yioRemoteDockHandler_webSocketClient = new WebSocketClient();
    private YIOremoteDockWebsocket yioRemoteDockwebSocketClient = new YIOremoteDockWebsocket();
    private ClientUpgradeRequest YIOremote_DockwebSocketClientrequest = new ClientUpgradeRequest();
    private @Nullable URI URI_yiodockwebsocketaddress;
    private YIO_REMOTE_DOCK_HANDLE_STATUS YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus = YIO_REMOTE_DOCK_HANDLE_STATUS.UNINITIALIZED;
    private @Nullable Future<?> pollingJob;
    private String send_ircode = "";

    public YIOremoteDockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        Runnable heartbeatpolling = new Runnable() {
            @Override
            public void run() {
                try {
                    if (YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus
                            .equals(YIO_REMOTE_DOCK_HANDLE_STATUS.AUTHENTICATED)) {
                        yioRemoteDockwebSocketClient.sendMessage(YIOREMOTEMESSAGETYPE.HEARTBEAT, "");

                        Thread.sleep(1000);

                        if (yioRemoteDockwebSocketClient.get_boolean_heartbeat()) {
                            logger.debug("heartbeat ok");
                            updateChannelString(GROUP_OUTPUT, YIODOCKSTATUS,
                                    yioRemoteDockwebSocketClient.get_string_receivedstatus());
                        } else {
                            logger.warn("Connection lost no ping from YIO DOCK");
                            YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus = YIO_REMOTE_DOCK_HANDLE_STATUS.CONNECTION_FAILED;
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "Connection lost no ping from YIO DOCK");
                            updateChannelString(GROUP_OUTPUT, YIODOCKSTATUS, "Connection/Configuration Error");
                            pollingJob.cancel(true);
                        }
                    } else {
                        logger.warn("Connection lost no ping from YIO DOCK");
                        YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus = YIO_REMOTE_DOCK_HANDLE_STATUS.CONNECTION_FAILED;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Connection lost no ping from YIO DOCK");
                        updateChannelString(GROUP_OUTPUT, YIODOCKSTATUS, "Connection/Configuration Error");
                        pollingJob.cancel(true);
                    }
                } catch (InterruptedException e) {
                    logger.warn("Error during initializing the WebSocket polling Thread {}", e.getMessage());
                }
            }
        };

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                logger.debug("Starting generating URI_yiodockwebsocketaddress");
                URI_yiodockwebsocketaddress = new URI("ws://" + localConfig.host + ":946");
                logger.debug("Finished generating URI_yiodockwebsocketaddress");
                YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus = YIO_REMOTE_DOCK_HANDLE_STATUS.AUTHENTICATION_PROCESS;
                try {

                    logger.debug("Starting websocket Client");
                    yioRemoteDockHandler_webSocketClient.start();
                    logger.debug("Started websocket Client");
                } catch (Exception e) {
                    logger.warn("Web socket start failed {}", e.getMessage());
                    // throw new IOException("Web socket start failed");
                }
                try {
                    logger.debug("Connect websocket client");
                    yioRemoteDockHandler_webSocketClient.connect(yioRemoteDockwebSocketClient,
                            URI_yiodockwebsocketaddress, YIOremote_DockwebSocketClientrequest);
                    logger.debug("Connected websocket client");

                    logger.debug("Check for authentication requested by YIO Dock");
                    yioRemoteDockwebSocketClient.getLatch().await();
                    Thread.sleep(1000);

                    try {
                        if (yioRemoteDockwebSocketClient.get_boolean_authentication_required()) {
                            logger.debug("send authentication to YIO dock");
                            yioRemoteDockwebSocketClient.sendMessage(YIOREMOTEMESSAGETYPE.AUTHENTICATE,
                                    localConfig.accesstoken);
                            Thread.sleep(1000);

                            if (yioRemoteDockwebSocketClient.get_boolean_authentication_ok()) {
                                YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus = YIO_REMOTE_DOCK_HANDLE_STATUS.AUTHENTICATED;

                            } else {
                                logger.debug("authentication to YIO dock not ok");
                                YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus = YIO_REMOTE_DOCK_HANDLE_STATUS.AUTHENTICATED_FAILED;
                            }
                        } else {
                            logger.debug("authentication error YIO dock");
                        }

                    } catch (IllegalArgumentException e) {
                        logger.warn("JSON convertion failure {}", e.getMessage());
                    }

                } catch (

                Exception e) {
                    logger.warn("Web socket connect failed {}", e.getMessage());
                    // throw new IOException("Web socket start failed");
                }
            } catch (URISyntaxException e) {
                logger.debug("Initialize web socket failed {}", e.getMessage());
            }

            if (YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus.equals(YIO_REMOTE_DOCK_HANDLE_STATUS.AUTHENTICATED)) {
                updateStatus(ThingStatus.ONLINE);
                try {
                    pollingJob = scheduler.scheduleWithFixedDelay(heartbeatpolling, 0, 30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    updateChannelString(GROUP_OUTPUT, YIODOCKSTATUS, "Connection/Configuration Error");
                    logger.warn("Error during starting the WebSocket polling Thread {}", e.getMessage());
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        logger.debug("Finished initializing!");
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(YIOremoteDockActions.class);
    }

    @Override
    public void dispose() {
        updateChannelString(GROUP_OUTPUT, YIODOCKSTATUS, "Connection/Configuration Error");
        pollingJob.cancel(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (YIODOCKRECEIVERSWITCH.equals(channelUID.getIdWithoutGroup())) {
            if (YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus.equals(YIO_REMOTE_DOCK_HANDLE_STATUS.AUTHENTICATED)) {
                if (command.toString().equals("ON")) {
                    logger.debug("YIODOCKRECEIVERSWITCH ON procedure: Switching IR Receiver on");
                    yioRemoteDockwebSocketClient.sendMessage(YIOREMOTEMESSAGETYPE.IRRECEIVERON, "");
                } else if (command.toString().equals("OFF")) {
                    logger.debug("YIODOCKRECEIVERSWITCH OFF procedure: Switching IR Receiver off");
                    yioRemoteDockwebSocketClient.sendMessage(YIOREMOTEMESSAGETYPE.IRRECEIVEROFF, "");
                } else {
                    logger.debug("YIODOCKRECEIVERSWITCH no procedure");
                }

            } else {
                logger.warn("YIOremoteDockHandler not authenticated");
            }

        } else if (YIODOCKSENDIRCODE.equals(channelUID.getIdWithoutGroup())) {
            if (YIO_REMOTE_DOCK_HANDLE_STATUS_actualstatus.equals(YIO_REMOTE_DOCK_HANDLE_STATUS.AUTHENTICATED)) {
                logger.debug("YIODOCKSENDIRCODE procedure: {}", command.toString());
                send_ircode = command.toString();
                if (send_ircode.matches("[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                    yioRemoteDockwebSocketClient.sendMessage(YIOREMOTEMESSAGETYPE.IRSEND, send_ircode);
                } else {
                    logger.warn("Wrong IR code Format {}", send_ircode);
                    send_ircode = "";
                }
            } else {
                logger.warn("YIOremoteDockHandler not authenticated");
            }
        }
    }

    protected void updateChannelString(String group, String channelId, String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);

        if (isLinked(id)) {
            updateState(id, new StringType(value));
        }
    }
}
