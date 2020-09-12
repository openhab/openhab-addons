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
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEDOCKHANDLESTATUS;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEMESSAGETYPE;
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
    private WebSocketClient yioremoteDockHandlerwebSocketClient = new WebSocketClient();
    private YIOremoteDockWebsocket yioremoteDockwebSocketClient = new YIOremoteDockWebsocket();
    private ClientUpgradeRequest yioremoteDockwebSocketClientrequest = new ClientUpgradeRequest();
    private @Nullable URI uriyiodockwebsocketaddress;
    private YIOREMOTEDOCKHANDLESTATUS yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.UNINITIALIZED;
    private @Nullable Future<?> pollingJob;
    private String sendircode = "";

    public YIOremoteDockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                uriyiodockwebsocketaddress = new URI("ws://" + localConfig.host + ":946");
                yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATIONPROCESS;
                try {
                    yioremoteDockHandlerwebSocketClient.start();
                } catch (Exception e) {
                    logger.warn("Web socket start failed {}", e.getMessage());
                    // throw new IOException("Web socket start failed");
                }
                try {
                    yioremoteDockHandlerwebSocketClient.connect(yioremoteDockwebSocketClient,
                            uriyiodockwebsocketaddress, yioremoteDockwebSocketClientrequest);
                    yioremoteDockwebSocketClient.getLatch().await();
                    Thread.sleep(1000);
                    try {
                        if (yioremoteDockwebSocketClient.getbooleanauthenticationrequired()) {
                            yioremoteDockwebSocketClient.sendMessage(YIOREMOTEMESSAGETYPE.AUTHENTICATE,
                                    localConfig.accesstoken);
                            Thread.sleep(1000);
                            if (yioremoteDockwebSocketClient.getbooleanauthenticationok()) {
                                yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATED;
                                logger.debug("authentication to YIO dock ok");
                                updateStatus(ThingStatus.ONLINE);
                                try {
                                    Runnable heartbeatpolling = new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (yioremotedockactualstatus
                                                        .equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATED)) {
                                                    yioremoteDockwebSocketClient
                                                            .sendMessage(YIOREMOTEMESSAGETYPE.HEARTBEAT, "");

                                                    Thread.sleep(1000);

                                                    if (yioremoteDockwebSocketClient.getbooleanheartbeat()) {
                                                        logger.debug("heartbeat ok");
                                                        triggerChannel(getChannelUuid(GROUP_OUTPUT, YIODOCKSTATUS));
                                                        updateChannelString(GROUP_OUTPUT, YIODOCKSTATUS,
                                                                yioremoteDockwebSocketClient.getstringreceivedstatus());
                                                    } else {
                                                        yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.CONNECTIONFAILED;
                                                        updateStatus(ThingStatus.OFFLINE,
                                                                ThingStatusDetail.CONFIGURATION_ERROR,
                                                                "Connection lost no ping from YIO DOCK");
                                                        updateState(GROUP_OUTPUT, YIODOCKSTATUS, UnDefType.UNDEF);
                                                        pollingJob.cancel(true);
                                                    }
                                                } else {
                                                    yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.CONNECTIONFAILED;
                                                    updateStatus(ThingStatus.OFFLINE,
                                                            ThingStatusDetail.CONFIGURATION_ERROR,
                                                            "Connection lost no ping from YIO DOCK");
                                                    updateState(GROUP_OUTPUT, YIODOCKSTATUS, UnDefType.UNDEF);
                                                    pollingJob.cancel(true);
                                                }
                                            } catch (Exception e) {
                                                logger.warn("Error during initializing the WebSocket polling Thread {}",
                                                        e.getMessage());
                                            }
                                        }
                                    };

                                    pollingJob = scheduler.scheduleWithFixedDelay(heartbeatpolling, 0, 30,
                                            TimeUnit.SECONDS);
                                } catch (Exception e) {
                                    updateChannelString(GROUP_OUTPUT, YIODOCKSTATUS, "Connection/Configuration Error");
                                }
                            } else {
                                logger.debug("authentication to YIO dock not ok");
                                yioremotedockactualstatus = YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATEDFAILED;
                                updateStatus(ThingStatus.OFFLINE);
                            }
                        } else {
                            logger.debug("authentication error YIO dock");
                        }
                    } catch (IllegalArgumentException e) {
                        logger.warn("JSON convertion failure {}", e.getMessage());
                    }
                } catch (Exception e) {
                    logger.warn("Web socket connect failed {}", e.getMessage());
                }
            } catch (URISyntaxException e) {
                logger.debug("Initialize web socket failed {}", e.getMessage());
            }
        });
    }

    protected void updateState(String group, String channelId, State value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, value);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(YIOremoteDockActions.class);
    }

    @Override
    public void dispose() {
        pollingJob.cancel(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (YIODOCKRECEIVERSWITCH.equals(channelUID.getIdWithoutGroup())) {
            if (yioremotedockactualstatus.equals(YIOREMOTEDOCKHANDLESTATUS.AUTHENTICATED)) {
                if (command.toString().equals("ON")) {
                    logger.debug("YIODOCKRECEIVERSWITCH ON procedure: Switching IR Receiver on");
                    yioremoteDockwebSocketClient.sendMessage(YIOREMOTEMESSAGETYPE.IRRECEIVERON, "");
                } else if (command.toString().equals("OFF")) {
                    logger.debug("YIODOCKRECEIVERSWITCH OFF procedure: Switching IR Receiver off");
                    yioremoteDockwebSocketClient.sendMessage(YIOREMOTEMESSAGETYPE.IRRECEIVEROFF, "");
                } else {
                    logger.debug("YIODOCKRECEIVERSWITCH no procedure");
                }
            }
        }
    }

    public void sendircode(@Nullable String string_ircode) {
        if (string_ircode != null) {
            if (string_ircode.matches("[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                yioremoteDockwebSocketClient.sendMessage(YIOREMOTEMESSAGETYPE.IRSEND, string_ircode);
            } else {
                logger.warn("Wrong IR code Format {}", string_ircode);
            }
        }
    }

    private ChannelUID getChannelUuid(String group, String typeId) {
        return new ChannelUID(getThing().getUID(), group, typeId);
    }

    private void updateChannelString(String group, String channelId, String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new StringType(value));
        }
    }

    private void updateChannelString(ChannelUID channelId, String value) {
        if (isLinked(channelId)) {
            updateState(channelId, new StringType(value));
        }
    }
}
