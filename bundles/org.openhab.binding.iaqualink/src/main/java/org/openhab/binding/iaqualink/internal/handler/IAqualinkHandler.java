/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.iaqualink.internal.IAqualinkBindingConstants;
import org.openhab.binding.iaqualink.internal.api.IAqualinkClient;
import org.openhab.binding.iaqualink.internal.api.IAqualinkClient.NotAuthorizedException;
import org.openhab.binding.iaqualink.internal.api.dto.AccountInfo;
import org.openhab.binding.iaqualink.internal.api.dto.Auxiliary;
import org.openhab.binding.iaqualink.internal.api.dto.Device;
import org.openhab.binding.iaqualink.internal.api.dto.Home;
import org.openhab.binding.iaqualink.internal.api.dto.OneTouch;
import org.openhab.binding.iaqualink.internal.config.IAqualinkConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final int COMMAND_REFRESH_SECONDS = 5;

    /**
     * Default iAqulink key used by existing clients in the marketplace
     */
    private static final String DEFAULT_API_KEY = "EOOEMOW4YR6QNB07";

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

    private @Nullable String apiKey;

    /**
     * Optional serial number of the pool controller to connect to, only useful if you have more then one controller
     */
    private @Nullable String serialNumber;

    /**
     * Server issued sessionId
     */
    private @Nullable String sessionId;

    /**
     * When we first connect we will dynamically create channels based on what the controller is configured for
     */
    private boolean firstRun;

    /**
     * Future to poll for updated
     */
    private @Nullable ScheduledFuture<?> pollFuture;

    /**
     * The client interface to the iAqualink Service
     */
    private IAqualinkClient client;

    /**
     * Temperature unit, will be set based on user setting
     */
    private Unit<Temperature> temperatureUnit = CELSIUS;

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
        // don't hold up initialize
        scheduler.schedule(this::configure, 0, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        clearPolling();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // clear our cached value so the new channel gets updated on the next poll
        stateMap.remove(channelUID.getAsString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand channel: {} command: {}", channelUID, command);

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Controller is not ONLINE and is not responding to commands");
            return;
        }

        clearPolling();

        String channelName = channelUID.getIdWithoutGroup();
        // remove the current state to ensure we send an update
        stateMap.remove(channelUID.getAsString());
        try {
            if (command instanceof RefreshType) {
                logger.debug("Channel {} state has been cleared", channelName);
            } else if (channelName.startsWith("aux_")) {
                // Auxiliary Commands
                String auxId = channelName.replaceFirst("aux_", "");
                if (command instanceof PercentType) {
                    client.dimmerCommand(serialNumber, sessionId, auxId, command.toString());
                } else if (command instanceof StringType) {
                    String cmd = "off".equals(command.toString()) ? "0"
                            : "on".equals(command.toString()) ? "1" : command.toString();
                    client.lightCommand(serialNumber, sessionId, auxId, cmd,
                            AuxiliaryType.fromChannelTypeUID(getChannelTypeUID(channelUID)).getSubType());
                } else if (command instanceof OnOffType) {
                    // these are toggle commands and require we have the current state to turn on/off
                    Auxiliary[] auxs = client.getAux(serialNumber, sessionId);
                    Optional<Auxiliary> optional = Arrays.stream(auxs).filter(o -> o.getName().equals(channelName))
                            .findFirst();
                    if (optional.isPresent()) {
                        OnOffType onOffCommand = (OnOffType) command;
                        State currentState = toState(channelName, "Switch", optional.get().getState());
                        if (!currentState.equals(onOffCommand)) {
                            client.auxSetCommand(serialNumber, sessionId, channelName);
                        }
                    }
                }
            } else if (channelName.endsWith("_set_point")) {
                // Set Point Commands
                if ("spa_set_point".equals(channelName)) {
                    BigDecimal value = commandToRoundedTemperature(command, temperatureUnit);
                    if (value != null) {
                        client.setSpaTemp(serialNumber, sessionId, value.floatValue());
                    }
                } else if ("pool_set_point".equals(channelName)) {
                    BigDecimal value = commandToRoundedTemperature(command, temperatureUnit);
                    if (value != null) {
                        client.setPoolTemp(serialNumber, sessionId, value.floatValue());
                    }
                }
            } else if (command instanceof OnOffType) {
                OnOffType onOffCommand = (OnOffType) command;
                // these are toggle commands and require we have the current state to turn on/off
                if (channelName.startsWith("onetouch_")) {
                    OneTouch[] ota = client.getOneTouch(serialNumber, sessionId);
                    Optional<OneTouch> optional = Arrays.stream(ota).filter(o -> o.getName().equals(channelName))
                            .findFirst();
                    if (optional.isPresent()) {
                        State currentState = toState(channelName, "Switch", optional.get().getState());
                        if (!currentState.equals(onOffCommand)) {
                            logger.debug("Sending command {} to {}", command, channelName);
                            client.oneTouchSetCommand(serialNumber, sessionId, channelName);
                        }
                    }
                } else if (channelName.endsWith("heater") || channelName.endsWith("pump")) {
                    String value = client.getHome(serialNumber, sessionId).getSerializedMap().get(channelName);
                    State currentState = toState(channelName, "Switch", value);
                    if (!currentState.equals(onOffCommand)) {
                        logger.debug("Sending command {} to {}", command, channelName);
                        client.homeScreenSetCommand(serialNumber, sessionId, channelName);
                    }
                }
            }
            initPolling(COMMAND_REFRESH_SECONDS);
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

        if (confApiKey != null && !confApiKey.isBlank()) {
            this.apiKey = confApiKey;
        } else {
            this.apiKey = DEFAULT_API_KEY;
        }

        this.refresh = Math.max(configuration.refresh, MIN_REFRESH_SECONDS);

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

            if (confSerialId != null && !confSerialId.isBlank()) {
                serialNumber = confSerialId.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                if (!Arrays.stream(devices).anyMatch(device -> device.getSerialNumber().equals(serialNumber))) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No Device for given serialId found");
                    return;
                }
            } else {
                serialNumber = devices[0].getSerialNumber();
            }

            initPolling(COMMAND_REFRESH_SECONDS);
        } catch (IOException e) {
            logger.debug("Could not connect to service {}", e.getMessage());
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
    private synchronized void initPolling(int initialDelay) {
        clearPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(this::pollController, initialDelay, refresh, TimeUnit.SECONDS);
    }

    /**
     * Stops/clears this thing's polling future
     */
    private void clearPolling() {
        ScheduledFuture<?> localFuture = pollFuture;
        if (isFutureValid(localFuture)) {
            if (localFuture != null) {
                localFuture.cancel(true);
            }
        }
    }

    private boolean isFutureValid(@Nullable ScheduledFuture<?> future) {
        return future != null && !future.isCancelled();
    }

    /**
     * Poll the controller for updates.
     */
    private void pollController() {
        ScheduledFuture<?> localFuture = pollFuture;
        try {
            Home home = client.getHome(serialNumber, sessionId);

            if ("Error".equals(home.getResponse())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Service reports controller status as: " + home.getStatus());
                return;
            }

            Map<String, String> map = home.getSerializedMap();
            if (map != null) {
                temperatureUnit = "F".equalsIgnoreCase(map.get("temp_scale")) ? FAHRENHEIT : CELSIUS;
                map.forEach((k, v) -> {
                    updatedState(k, v);
                    if (k.endsWith("_heater")) {
                        HeaterState hs = HeaterState.fromValue(v);
                        updatedState(k + "_status", hs == null ? null : hs.getLabel());
                    }
                });
            }

            OneTouch[] oneTouches = client.getOneTouch(serialNumber, sessionId);
            Auxiliary[] auxes = client.getAux(serialNumber, sessionId);

            if (firstRun) {
                firstRun = false;
                updateChannels(auxes, oneTouches);
            }

            for (OneTouch ot : oneTouches) {
                updatedState(ot.getName(), ot.getState());
            }

            for (Auxiliary aux : auxes) {
                switch (aux.getType()) {
                    // dimmer uses subType for value
                    case "1":
                        updatedState(aux.getName(), aux.getSubtype());
                        break;
                    // Color lights do not report the color value, only on/off
                    case "2":
                        updatedState(aux.getName(), "0".equals(aux.getState()) ? "off" : "on");
                        break;
                    // all else are switches
                    default:
                        updatedState(aux.getName(), aux.getState());
                }
            }

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            // poller will continue to run, set offline until next run
            logger.debug("Exception polling", e);
            if (isFutureValid(localFuture)) {
                // only valid futures should set state, otherwise this exception was do to being canceled.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } catch (NotAuthorizedException e) {
            // if are creds are not valid, we need to try re authorizing again
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
    private void updatedState(String name, @Nullable String value) {
        logger.trace("updatedState {} : {}", name, value);
        Channel channel = getThing().getChannel(name);
        if (channel != null) {
            State state = toState(name, channel.getAcceptedItemType(), value);
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
    private State toState(String name, @Nullable String type, @Nullable String value) {
        try {
            if (value == null || value.isBlank()) {
                return UnDefType.UNDEF;
            }

            if (type == null) {
                return StringType.valueOf(value);
            }

            switch (type) {
                case "Number:Temperature":
                    return new QuantityType<>(Float.parseFloat(value), temperatureUnit);
                case "Number":
                    return new DecimalType(value);
                case "Dimmer":
                    return new PercentType(value);
                case "Switch":
                    return Integer.parseInt(value) > 0 ? OnOffType.ON : OnOffType.OFF;
                default:
                    return StringType.valueOf(value);
            }
        } catch (IllegalArgumentException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Creates channels based on what is supported by the controller.
     */
    private void updateChannels(Auxiliary[] auxes, OneTouch[] oneTouches) {
        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        for (Auxiliary aux : auxes) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), aux.getName());
            logger.debug("Add channel Aux Name: {} Label: {} Type: {} Subtype: {}", aux.getName(), aux.getLabel(),
                    aux.getType(), aux.getSubtype());
            switch (aux.getType()) {
                case "1":
                    addNewChannelToList(channels, channelUID, "Dimmer",
                            IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_DIMMER, aux.getLabel());
                    break;
                case "2": {
                    addNewChannelToList(channels, channelUID, "String",
                            AuxiliaryType.fromSubType(aux.getSubtype()).getChannelTypeUID(), aux.getLabel());
                }
                    break;
                default:
                    addNewChannelToList(channels, channelUID, "Switch",
                            IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_SWITCH, aux.getLabel());
            }
        }

        for (OneTouch oneTouch : oneTouches) {
            if ("0".equals(oneTouch.getStatus())) {
                // OneTouch is not enabled
                continue;
            }

            ChannelUID channelUID = new ChannelUID(getThing().getUID(), oneTouch.getName());
            addNewChannelToList(channels, channelUID, "Switch", IAqualinkBindingConstants.CHANNEL_TYPE_UID_ONETOUCH,
                    oneTouch.getLabel());
        }

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());
    }

    /**
     * Adds a channel to the list of channels if the channel does not exist or is of a different type
     *
     */
    private void addNewChannelToList(List<Channel> list, ChannelUID channelUID, String itemType,
            ChannelTypeUID channelType, String label) {
        // if there is no entry, add it
        if (!list.stream().anyMatch(c -> c.getUID().equals(channelUID))) {
            list.add(ChannelBuilder.create(channelUID, itemType).withType(channelType).withLabel(label).build());
        } else if (list.removeIf(c -> c.getUID().equals(channelUID) && !channelType.equals(c.getChannelTypeUID()))) {
            // this channel uid exists, but has a different type so remove and add our new one
            list.add(ChannelBuilder.create(channelUID, itemType).withType(channelType).withLabel(label).build());
        }
    }

    /**
     * inspired by the openHAB Nest thermostat binding
     */
    @SuppressWarnings("unchecked")
    private @Nullable BigDecimal commandToRoundedTemperature(Command command, Unit<Temperature> unit)
            throws IllegalArgumentException {
        QuantityType<Temperature> quantity;
        if (command instanceof QuantityType) {
            quantity = (QuantityType<Temperature>) command;
        } else {
            quantity = new QuantityType<>(new BigDecimal(command.toString()), unit);
        }

        QuantityType<Temperature> temparatureQuantity = quantity.toUnit(unit);
        if (temparatureQuantity == null) {
            return null;
        }

        BigDecimal value = temparatureQuantity.toBigDecimal();
        BigDecimal increment = CELSIUS == unit ? new BigDecimal("0.5") : new BigDecimal("1");
        BigDecimal divisor = value.divide(increment, 0, RoundingMode.HALF_UP);
        return divisor.multiply(increment);
    }

    private ChannelTypeUID getChannelTypeUID(ChannelUID channelUID) {
        Channel channel = getThing().getChannel(channelUID.getId());
        Objects.requireNonNull(channel);
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        Objects.requireNonNull(channelTypeUID);
        return channelTypeUID;
    }
}
