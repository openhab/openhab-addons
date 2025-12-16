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
package org.openhab.binding.iaqualink.internal.v2;

import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.iaqualink.internal.IAqualinkBindingConstants;
import org.openhab.binding.iaqualink.internal.config.IAqualinkConfiguration;
import org.openhab.binding.iaqualink.internal.v2.api.IAqualinkClient;
import org.openhab.binding.iaqualink.internal.v2.api.IAqualinkClient.NotAuthorizedException;
import org.openhab.binding.iaqualink.internal.v2.api.IAqualinkDeviceListener;
import org.openhab.binding.iaqualink.internal.v2.api.PropertyStorage;
import org.openhab.binding.iaqualink.internal.v2.api.dto.Device;
import org.openhab.binding.iaqualink.internal.v2.api.mapping.ChannelDef;
import org.openhab.binding.iaqualink.internal.v2.api.mapping.Channels;
import org.openhab.binding.iaqualink.internal.v2.api.mapping.DeviceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
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
 * iAquaLink Control Binding
 *
 * iAquaLink controllers allow remote access to Jandy/Zodiac pool systems. This
 * binding allows openHAB to both monitor and control a pool system through
 * these controllers.
 * <p>
 * The {@link IAqualinkV2Handler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class IAqualinkV2Handler extends BaseThingHandler implements IAqualinkDeviceListener, PropertyStorage {

    private final Logger logger = LoggerFactory.getLogger(IAqualinkV2Handler.class);

    /**
     * Default iAqulink key used by existing clients in the marketplace
     */
    private static final String DEFAULT_API_KEY = "EOOEMOW4YR6QNB07";

    /**
     * Local cache of iAqualink states
     */
    private final Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * When we first connect we will dynamically create channels based on what the controller is configured for
     */
    private @Nullable Collection<ChannelDef> channelDefs = null;

    private @Nullable Device device;

    /**
     * The client interface to the iAqualink Service
     */
    private @Nullable IAqualinkClient client;

    private final HttpClient httpClient;

    /**
     * Temperature unit, will be set based on user setting
     */
    private final Unit<Temperature> temperatureUnit = CELSIUS;

    /**
     * Constructs a new {@link IAqualinkV2Handler}
     */
    public IAqualinkV2Handler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        // don't hold up initialize
        scheduler.schedule(this::configure, 0, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");

        if (client != null) {
            try {
                disconnect();
            } catch (ExecutionException | InterruptedException e) {
                // swallow exception to allow handler to be disposed
                logger.error("Failed to disconnect from iAqualink", e);
            }
        }
    }

    @Override
    public @Nullable String getProperty(String name) {
        return editProperties().get(name);
    }

    @Override
    public void setProperty(String name, @Nullable String value) {
        Map<String, String> props = editProperties();
        if (value == null) {
            props.remove(name);
        } else {
            props.put(name, value);
        }
        updateProperties(props);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // clear our cached value so the new channel gets updated on the next poll
        stateMap.remove(channelUID.getAsString());
    }

    /**
     * Configures this thing
     */
    private void configure() {

        try {
            disconnect();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        channelDefs = null;

        IAqualinkConfiguration configuration = getConfig().as(IAqualinkConfiguration.class);
        String confSerialId = configuration.serialId;
        String confApiKey = configuration.apiKey.isBlank() ? DEFAULT_API_KEY : configuration.apiKey;

        try {
            IAqualinkClient client = new IAqualinkClient(httpClient, configuration.userName, configuration.password,
                    confApiKey, scheduler, this);

            Device[] devices = client.getDevices();

            if (devices.length == 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No registered devices found");
                return;
            }

            Device newDevice;

            if (!confSerialId.isBlank()) {
                String serialNumber = confSerialId.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

                newDevice = Arrays.stream(devices).filter(d -> d.getSerialNumber().equals(serialNumber)).findFirst()
                        .orElseGet(() -> devices[0]);

                if (Arrays.stream(devices).noneMatch(d -> d.getSerialNumber().equals(serialNumber))) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No Device for given serialId found");
                    return;
                }
            } else {
                newDevice = devices[0];
            }

            logger.debug("Using serial number {}", newDevice.getSerialNumber());

            device = newDevice;
            this.client = client;

            client.connect();
            startListening(newDevice);
        } catch (IOException e) {
            logger.debug("Could not connect to service {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (NotAuthorizedException e) {
            logger.debug("Credentials not valid");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Credentials not valid");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts/Restarts listening to message queue.
     */
    private void startListening(Device device)
            throws UnsupportedEncodingException, ExecutionException, InterruptedException {
        IAqualinkClient client = this.client;

        if (client == null) {
            throw new IllegalStateException("Client not initialized");
        }

        client.subscribe(device, this)
                .thenAccept((ignore) -> logger.info("Listening for events for {}", device.getSerialNumber())).get();

        client.doGetDevice(device);
    }

    /**
     * Stops/clears this thing's polling future
     */
    private void disconnect() throws ExecutionException, InterruptedException {
        IAqualinkClient client = this.client;

        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    public void onGetAccepted(String deviceId, String msg) {
        DeviceState newState = DeviceState.parse(msg);

        Collection<ChannelDef> channelDefs = this.channelDefs;

        if (channelDefs == null) {
            channelDefs = Channels.appliesToState(Channels.all(), newState);
            this.channelDefs = channelDefs;
            updateChannels(channelDefs);
        }

        for (ChannelDef channel : channelDefs) {
            updatedState(channel, channel.value(newState));
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void onUpdateAccepted(String deviceId, String msg) {
        DeviceState partialState = DeviceState.parse(msg);

        Collection<ChannelDef> channelDefs = this.channelDefs;

        if (channelDefs == null) {
            logger.warn("Update ignored; Controller channels not yet initialized");
            return;
        }

        for (ChannelDef channel : channelDefs) {
            Object newValue = channel.value(partialState);
            // only update present values
            if (newValue != null) {
                updatedState(channel, newValue);
            }
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void onUpdateRejected(String deviceId, String msg) {
        logger.error("Update rejected for device {}: {}", deviceId, msg);
    }

    @Override
    public void onDisconnected(String deviceId) {
        logger.error("Disconnected from device {}!", deviceId);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    /**
     * Update a channel value only if the value of the channel has changed since our last poll.
     */
    private void updatedState(ChannelDef channelDef, @Nullable Object newState) {
        logger.trace("updatedState {} : {}", channelDef.id(), newState);
        Channel channel = getThing().getChannel(channelDef.id());
        if (channel != null) {
            State state = toState(channel.getAcceptedItemType(), newState != null ? newState.toString() : null);
            State oldState = stateMap.put(channel.getUID().getAsString(), state);
            if (!state.equals(oldState)) {
                logger.trace("updating channel {} with value {} (old value {})", channel.getUID(), state, oldState);
                updateState(channel.getUID(), state);
            }
        }
    }

    /**
     * Converts a {@link String} value to a {@link State} for a given
     * {@link String} accepted type
     */
    private State toState(@Nullable String type, @Nullable String value) {
        try {
            if (value == null || value.isBlank()) {
                return UnDefType.UNDEF;
            } else {
                if (type == null) {
                    return StringType.valueOf(value);
                } else {
                    return switch (type) {
                        case "Number:Temperature" -> new QuantityType<>(Float.parseFloat(value), temperatureUnit);
                        case "Number:Time" -> new QuantityType<>(
                                Duration.parse("PT" + value.replace(':', 'H') + "M").toMinutes(), Units.MINUTE);
                        case "Number" -> new DecimalType(value);
                        case "Dimmer" -> new PercentType(value);
                        case "Switch" -> OnOffType.from(Integer.parseInt(value) > 0);
                        default -> StringType.valueOf(value);
                    };
                }
            }
        } catch (IllegalArgumentException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Creates channels based on what is supported by the controller.
     */
    private void updateChannels(Collection<ChannelDef> channelDefs) {
        List<Channel> channels = new ArrayList<>(getThing().getChannels());

        for (ChannelDef channelDef : channelDefs) {
            logger.debug("Add channel Id: {} Type: {}", channelDef.id(), channelDef.itemType());
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(IAqualinkBindingConstants.BINDING_ID,
                    channelDef.typeId());
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelDef.id());
            Channel channel = ChannelBuilder.create(channelUID, channelDef.itemType()).withType(channelTypeUID)
                    .withLabel(channelDef.label()).build();

            // if there is no entry, add it
            if (channels.stream().noneMatch(c -> c.getUID().equals(channelUID))) {
                logger.debug("Adding channel {}", channelDef.id());
                channels.add(channel);
            } else if (channels
                    .removeIf(c -> c.getUID().equals(channelUID) && !channelTypeUID.equals(c.getChannelTypeUID()))) {
                // this channel uid exists, but has a different type so remove and add our new one
                logger.debug("Replacing channel {}", channelDef.id());
                channels.add(channel);
            }
        }

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand channel: {} command: {}", channelUID, command);

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Controller is not ONLINE and is not responding to commands");
            return;
        }

        Collection<ChannelDef> channelDefs = this.channelDefs;

        if (channelDefs == null) {
            logger.warn("Controller channels not yet initialized");
            return;
        }

        String channelName = channelUID.getIdWithoutGroup();
        // remove the current value to ensure we send an update
        stateMap.remove(channelUID.getAsString());
        try {
            if (command instanceof RefreshType) {
                logger.debug("Channel {} value has been cleared", channelName);
            } else {
                Optional<ChannelDef> channelDef = channelDefs.stream()
                        .filter(channel -> channel.id().equals(channelName)).findFirst();

                if (channelDef.isEmpty()) {
                    logger.warn("Channel {} is not supported", channelName);
                } else {
                    Object newStateAsJson = commandToJsonObject(command);
                    if (newStateAsJson != null) {
                        DeviceState newState = channelDef.get().updateJson(newStateAsJson);
                        IAqualinkClient client = this.client;
                        Device device = this.device;
                        if (client != null && device != null) {
                            client.publishUpdate(device.getSerialNumber(), newState.jsonString()).get();
                        } else {
                            logger.warn("Update not published as client or device disposing.");
                        }
                    } else {
                        logger.warn("Command {} from channel {} does not map to a json object", command,
                                channelDef.get().id());
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private @Nullable Object commandToJsonObject(Command command) {
        if (command instanceof OnOffType onOffType) {
            if (onOffType == OnOffType.ON) {
                return 1;
            } else if (onOffType == OnOffType.OFF) {
                return 0;
            } else {
                return null;
            }
        } else if (command instanceof DecimalType) {
            return ((DecimalType) command).doubleValue();
        } else {
            throw new IllegalArgumentException("Unsupported command type " + command.getClass().getSimpleName());
        }
    }
}
