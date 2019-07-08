/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.iaqualink.internal.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.iaqualink.internal.IAqualinkBindingConstants;
import org.openhab.binding.iaqualink.internal.api.IAqualinkClient;
import org.openhab.binding.iaqualink.internal.api.IAqualinkClient.NotAuthorizedException;
import org.openhab.binding.iaqualink.internal.api.model.AccountInfo;
import org.openhab.binding.iaqualink.internal.api.model.Auxiliary;
import org.openhab.binding.iaqualink.internal.api.model.Device;
import org.openhab.binding.iaqualink.internal.api.model.OneTouch;
import org.openhab.binding.iaqualink.internal.config.IAqualinkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 *
 * iAquaLink Control Binding
 *
 * iAquaLink controllers allow remote access to Jandy/Zodiac pool systems. This
 * binding allows openHAB to both monitor and control a pool system through
 * these controllers.
 *
 * The {@link IAqualinkHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class IAqualinkHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IAqualinkHandler.class);

    /**
     * Minimum amount of time we can poll for updates
     */
    private static final int MIN_REFRESH_SECONDS = 5;

    /**
     * Minimum amount of time we can poll after a command
     */
    private static final int COMMAND_REFRESH_SECONDS = 2;

    /**
     * Default iAqulink key used by existing clients in the marketplace
     */
    private static final String DEFUALT_API_KEY = "EOOEMOW4YR6QNB07";

    /**
     * Local cache of iAqualink states
     */
    private Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * Our poll rate
     */
    private int refresh;

    /**
     * fixed API key provided by iAqualink clients (Android, IOS), unknown if this will change in the future.
     */
    @Nullable
    private String apiKey;

    /**
     * Optional serial number of the pool controller to connect to, only useful if you have more then one controller
     */
    @Nullable
    private String serialNumber;

    /**
     * Server issued sessionId
     */
    @Nullable
    private String sessionId;

    /**
     * When we first connect we will dynamically create channels based on what the controller is configured for
     */
    private boolean firstRun;

    /**
     * Future to poll for updated
     */
    @Nullable
    private ScheduledFuture<?> pollFuture;

    /**
     * The client interface to the iAqualink Service
     */
    private IAqualinkClient client;

    /**
     * Constructs a new {@link IAqualinkHandler}
     *
     * @param thing
     * @param httpClient
     */
    public IAqualinkHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        client = new IAqualinkClient(httpClient);
    }

    @Override
    public void initialize() {
        configure();
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        clearPolling();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // clear our cached value so the new channel gets updated on the next poll
        stateMap.remove(channelUID.getId());
    }

    @SuppressWarnings("null") // stateMap.remove can return null, which confuses the compiler
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand channel: {} command: {}", channelUID, command);

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Controller is NOT ONLINE and is not responding to commands");
            return;
        }

        clearPolling();

        String channelName = channelUID.getIdWithoutGroup();
        // remove the current state to ensure we send an update
        State oldState = stateMap.remove(channelUID.getAsString());
        try {
            if (command instanceof RefreshType) {
                stateMap.clear();
            } else if (channelName.startsWith("onetouch_")) {
                if (oldState == null || !oldState.toString().equals(command.toString())) {
                    client.oneTouchCommand(serialNumber, sessionId, "set_" + channelName);
                }
            } else if (channelName.endsWith("heater") || channelName.endsWith("pump")) {
                if (oldState == null || !oldState.toString().equals(command.toString())) {
                    client.homeScreenCommand(serialNumber, sessionId, "set_" + channelName);
                }
            } else if (channelName.startsWith("aux_")) {
                if (command instanceof OnOffType) {
                    if (oldState == null || !oldState.toString().equals(command.toString())) {
                        client.auxCommand(serialNumber, sessionId, "set_" + channelName);
                    }
                }
                if (command instanceof DecimalType) {
                    client.auxCommand(serialNumber, sessionId, "set_" + channelName,
                            ((DecimalType) command).intValue());
                }
            } else if ("spa_set_point".equals(channelName) && command instanceof DecimalType) {
                client.setSpaTemp(serialNumber, sessionId, ((DecimalType) command).intValue());
            } else if ("pool_set_point".equals(channelName) && command instanceof DecimalType) {
                client.setPoolTemp(serialNumber, sessionId, ((DecimalType) command).intValue());
            }
            initPolling(COMMAND_REFRESH_SECONDS);
        } catch (InterruptedException e) {
            logger.debug("command interupted", e);
        } catch (IOException e) {
            logger.debug("Exception executing command", e);
            initPolling(COMMAND_REFRESH_SECONDS);
        } catch (NotAuthorizedException e) {
            logger.debug("Authorization Exception sending command", e);
            configure();
        }
    }

    /**
     * Configures this thing
     */
    private void configure() {
        clearPolling();
        firstRun = true;

        IAqualinkConfiguration configuration = getConfig().as(IAqualinkConfiguration.class);
        String username = configuration.userName;
        String password = configuration.password;
        String confSerialId = configuration.serialId;
        String confApiKey = configuration.apiKey;

        if (StringUtils.isBlank(username)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "username must not be empty");
            return;
        }

        if (StringUtils.isBlank(password)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "password must not be empty");
            return;
        }

        if (StringUtils.isNotBlank(confApiKey)) {
            this.apiKey = confApiKey;
        } else {
            this.apiKey = DEFUALT_API_KEY;
        }

        this.refresh = configuration.refresh.intValue() > MIN_REFRESH_SECONDS ? configuration.refresh.intValue()
                : MIN_REFRESH_SECONDS;

        try {
            AccountInfo accountInfo = client.login(username, password, apiKey);
            sessionId = accountInfo.getSessionId();
            if (sessionId == null) {
                throw new IOException("Response from controller not valid");
            }
            logger.debug("SessionID {}", sessionId);

            Device[] devices = client.getDevices(apiKey, accountInfo.getAuthenticationToken(), accountInfo.getId());

            if (devices.length == 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No registered devices found");
                return;
            }

            if (StringUtils.isNotBlank(confSerialId)) {
                serialNumber = confSerialId.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                if (!Arrays.stream(devices).anyMatch(device -> device.getSerialNumber().equals(serialNumber))) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No Device for given serialId found");
                    return;
                }
            } else {
                serialNumber = devices[0].getSerialNumber();
            }

            initPolling(0);

        } catch (InterruptedException | IOException e) {
            logger.debug("Could not connect to service");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (NotAuthorizedException e) {
            logger.debug("Credentials not valid");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Credentials not valid");
        }
    }

    /**
     * Starts/Restarts polling with an initial delay. This allows changes in the poll cycle for when commands are sent
     * and we need to poll sooner then the next refresh cycle.
     */
    private synchronized void initPolling(int initalDelay) {
        clearPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                pollController();
            } catch (Exception e) {
                logger.debug("Exception during poll", e);
            }
        }, initalDelay, refresh, TimeUnit.SECONDS);
    }

    /**
     * Stops/clears this thing's polling future
     */
    private void clearPolling() {
        ScheduledFuture<?> localFuture = pollFuture;
        if (localFuture != null && !localFuture.isCancelled()) {
            logger.trace("Canceling future");
            localFuture.cancel(true);
        }
    }

    /**
     * Poll the controller for updates.
     */
    private void pollController() {
        try {

            JsonElement home = client.getHomeJson(serialNumber, sessionId);
            JsonArray homeScreen = home.getAsJsonObject().getAsJsonArray("home_screen");
            if (homeScreen != null) {
                homeScreen.forEach(element -> {
                    element.getAsJsonObject().entrySet().forEach(entry -> {
                        updatedState(entry.getKey(), entry.getValue().getAsString());
                        if (entry.getKey().endsWith("_heater")) {
                            updatedState(entry.getKey() + "_status", entry.getValue().getAsString());
                        }
                    });
                });
            }

            OneTouch[] oneTouches = client.getOneTouch(serialNumber, sessionId);
            for (OneTouch ot : oneTouches) {
                updatedState(ot.getName(), ot.getState());
            }

            Auxiliary[] auxes = client.getAux(serialNumber, sessionId);
            for (Auxiliary aux : auxes) {
                updatedState(aux.getName(), aux.getState());
            }

            if (firstRun) {
                updateChannels(auxes, oneTouches);
            }

            firstRun = false;

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

        } catch (InterruptedException e) {
            logger.debug("polling interupted", e);
        } catch (IOException e) {
            logger.debug("Exception polling", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (NotAuthorizedException e) {
            logger.debug("Authorization Exception during polling", e);
            clearPolling();
            configure();
        }
    }

    /**
     * Update an channels state only if the value of the channel has changed since our last poll.
     *
     * @param name
     * @param value
     */
    private void updatedState(String name, String value) {
        logger.trace("updatedState {} : {}", name, value);
        Channel channel = getThing().getChannel(name);
        if (channel != null) {
            State state = toState(channel.getAcceptedItemType(), value);
            State oldState = stateMap.put(channel.getUID().getAsString(), state);
            if (!state.equals(oldState)) {
                logger.trace("updating channel {} with state {} (old state {})", channel.getUID(), state, oldState);
                updateState(channel.getUID(), state);
            }
        }
    }

    /**
     * Converts a {@link String} value to a {@link State} for a given
     * {@link String} accepted type
     *
     * @param itemType
     * @param value
     * @return {@link State}
     */
    private State toState(@Nullable String type, String value) throws NumberFormatException {
        if (StringUtils.isBlank(value)) {
            return UnDefType.UNDEF;
        } else if ("Number".equals(type) || "Dimmer".equals(type)) {
            return new DecimalType(value);
        } else if ("Switch".equals(type)) {
            return Integer.parseInt(value) > 0 ? OnOffType.ON : OnOffType.OFF;
        } else {
            return StringType.valueOf(value);
        }
    }

    /**
     * Creates channels based on what is supported by the controller.
     *
     * @param auxes
     * @param oneTouches
     */
    private void updateChannels(Auxiliary[] auxes, OneTouch[] oneTouches) {
        List<Channel> channels = new LinkedList<Channel>();
        for (Auxiliary aux : auxes) {
            Channel channel;
            switch (aux.getType()) {
                case "1":
                    channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), aux.getName()), "Dimmer")
                            .withType(IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_DIMMER).withLabel(aux.getLabel())
                            .build();
                case "2":
                    channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), aux.getName()), "Number")
                            .withType(IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_NUMBER).withLabel(aux.getLabel())
                            .build();

                default:
                    channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), aux.getName()), "Switch")
                            .withType(IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_SWITCH).withLabel(aux.getLabel())
                            .build();
            }
            channels.add(channel);
        }

        for (OneTouch oneTouch : oneTouches) {
            if ("0".equals(oneTouch.getStatus())) {
                // OneTouch is not enabled
                continue;
            }
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), oneTouch.getName()), "Switch")
                    .withType(IAqualinkBindingConstants.CHANNEL_TYPE_UID_ONETOUCH).withLabel(oneTouch.getLabel())
                    .build();
            channels.add(channel);
        }

        // check for duplicates
        for (Channel c : getThing().getChannels()) {
            if (!channels.stream().anyMatch(o -> o.getUID().equals(c.getUID()))) {
                channels.add(c);
            }
        }
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());
    }
}
