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
package org.openhab.binding.remoteopenhab.internal.handler;

import static org.openhab.binding.remoteopenhab.internal.RemoteopenhabBindingConstants.BINDING_ID;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.remoteopenhab.internal.RemoteopenhabChannelTypeProvider;
import org.openhab.binding.remoteopenhab.internal.RemoteopenhabStateDescriptionOptionProvider;
import org.openhab.binding.remoteopenhab.internal.config.RemoteopenhabInstanceConfiguration;
import org.openhab.binding.remoteopenhab.internal.data.Item;
import org.openhab.binding.remoteopenhab.internal.data.Option;
import org.openhab.binding.remoteopenhab.internal.data.StateDescription;
import org.openhab.binding.remoteopenhab.internal.exceptions.RemoteopenhabException;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabStreamingDataListener;
import org.openhab.binding.remoteopenhab.internal.rest.RemoteopenhabRestClient;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.TypeParser;
import org.openhab.core.types.UnDefType;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link RemoteopenhabBridgeHandler} is responsible for handling commands and updating states
 * using the REST API of the remote openHAB server.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabBridgeHandler extends BaseBridgeHandler implements RemoteopenhabStreamingDataListener {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);

    private static final long CONNECTION_TIMEOUT_MILLIS = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
    private static final int MAX_STATE_SIZE_FOR_LOGGING = 50;

    private final Logger logger = LoggerFactory.getLogger(RemoteopenhabBridgeHandler.class);

    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final RemoteopenhabChannelTypeProvider channelTypeProvider;
    private final RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider;
    private final Gson jsonParser;

    private final Object updateThingLock = new Object();

    private @NonNullByDefault({}) RemoteopenhabInstanceConfiguration config;

    private @Nullable ScheduledFuture<?> checkConnectionJob;
    private @Nullable RemoteopenhabRestClient restClient;

    public RemoteopenhabBridgeHandler(Bridge bridge, ClientBuilder clientBuilder,
            SseEventSourceFactory eventSourceFactory, RemoteopenhabChannelTypeProvider channelTypeProvider,
            RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider, final Gson jsonParser) {
        super(bridge);
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.channelTypeProvider = channelTypeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.jsonParser = jsonParser;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing remote openHAB handler for bridge {}", getThing().getUID());

        config = getConfigAs(RemoteopenhabInstanceConfiguration.class);

        String host = config.host.trim();
        if (host.length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Undefined server address setting in the thing configuration");
            return;
        }
        List<String> localIpAddresses = NetUtil.getAllInterfaceAddresses().stream()
                .filter(a -> !a.getAddress().isLinkLocalAddress())
                .map(a -> a.getAddress().getHostAddress().split("%")[0]).collect(Collectors.toList());
        if (localIpAddresses.contains(host)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Do not use the local server as a remote server in the thing configuration");
            return;
        }
        String path = config.restPath.trim();
        if (path.length() == 0 || !path.startsWith("/")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid REST API path setting in the thing configuration");
            return;
        }
        URL url;
        try {
            url = new URL("http", host, config.port, path);
        } catch (MalformedURLException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid REST URL built from the settings in the thing configuration");
            return;
        }

        String urlStr = url.toString();
        if (urlStr.endsWith("/")) {
            urlStr = urlStr.substring(0, urlStr.length() - 1);
        }
        logger.debug("REST URL = {}", urlStr);

        RemoteopenhabRestClient client = new RemoteopenhabRestClient(clientBuilder, eventSourceFactory, jsonParser,
                config.token, urlStr);
        restClient = client;

        updateStatus(ThingStatus.UNKNOWN);

        startCheckConnectionJob(client);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing remote openHAB handler for bridge {}", getThing().getUID());
        stopStreamingUpdates();
        stopCheckConnectionJob();
        this.restClient = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }
        RemoteopenhabRestClient client = restClient;
        if (client == null) {
            return;
        }

        try {
            if (command instanceof RefreshType) {
                String state = client.getRemoteItemState(channelUID.getId());
                updateChannelState(channelUID.getId(), null, state);
            } else if (isLinked(channelUID)) {
                client.sendCommandToRemoteItem(channelUID.getId(), command);
                String commandStr = command.toFullString();
                logger.debug("Sending command {} to remote item {} succeeded",
                        commandStr.length() < MAX_STATE_SIZE_FOR_LOGGING ? commandStr
                                : commandStr.substring(0, MAX_STATE_SIZE_FOR_LOGGING) + "...",
                        channelUID.getId());
            }
        } catch (RemoteopenhabException e) {
            logger.debug("{}", e.getMessage());
        }
    }

    private void createChannels(List<Item> items, boolean replace) {
        synchronized (updateThingLock) {
            int nbGroups = 0;
            List<Channel> channels = new ArrayList<>();
            for (Item item : items) {
                String itemType = item.type;
                boolean readOnly = false;
                if ("Group".equals(itemType)) {
                    if (item.groupType.isEmpty()) {
                        // Standard groups are ignored
                        nbGroups++;
                        continue;
                    } else {
                        itemType = item.groupType;
                    }
                } else {
                    if (item.stateDescription != null && item.stateDescription.readOnly) {
                        readOnly = true;
                    }
                }
                String channelTypeId = String.format("item%s%s", itemType.replace(":", ""), readOnly ? "RO" : "");
                ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelTypeId);
                ChannelType channelType = channelTypeProvider.getChannelType(channelTypeUID, null);
                String label;
                String description;
                if (channelType == null) {
                    logger.trace("Create the channel type {} for item type {}", channelTypeUID, itemType);
                    label = String.format("Remote %s Item", itemType);
                    description = String.format("An item of type %s from the remote server.", itemType);
                    channelType = ChannelTypeBuilder.state(channelTypeUID, label, itemType).withDescription(description)
                            .withStateDescriptionFragment(
                                    StateDescriptionFragmentBuilder.create().withReadOnly(readOnly).build())
                            .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
                    channelTypeProvider.addChannelType(channelType);
                }
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), item.name);
                logger.trace("Create the channel {} of type {}", channelUID, channelTypeUID);
                label = "Item " + item.name;
                description = String.format("Item %s from the remote server.", item.name);
                channels.add(ChannelBuilder.create(channelUID, itemType).withType(channelTypeUID)
                        .withKind(ChannelKind.STATE).withLabel(label).withDescription(description).build());
            }
            ThingBuilder thingBuilder = editThing();
            if (replace) {
                thingBuilder.withChannels(channels);
                updateThing(thingBuilder.build());
                logger.debug("{} channels defined for the thing {} (from {} items including {} groups)",
                        channels.size(), getThing().getUID(), items.size(), nbGroups);
            } else if (channels.size() > 0) {
                int nbRemoved = 0;
                for (Channel channel : channels) {
                    if (getThing().getChannel(channel.getUID()) != null) {
                        thingBuilder.withoutChannel(channel.getUID());
                        nbRemoved++;
                    }
                }
                if (nbRemoved > 0) {
                    logger.debug("{} channels removed for the thing {} (from {} items)", nbRemoved, getThing().getUID(),
                            items.size());
                }
                for (Channel channel : channels) {
                    thingBuilder.withChannel(channel);
                }
                updateThing(thingBuilder.build());
                if (nbGroups > 0) {
                    logger.debug("{} channels added for the thing {} (from {} items including {} groups)",
                            channels.size(), getThing().getUID(), items.size(), nbGroups);
                } else {
                    logger.debug("{} channels added for the thing {} (from {} items)", channels.size(),
                            getThing().getUID(), items.size());
                }
            }
        }
    }

    private void removeChannels(List<Item> items) {
        synchronized (updateThingLock) {
            int nbRemoved = 0;
            ThingBuilder thingBuilder = editThing();
            for (Item item : items) {
                Channel channel = getThing().getChannel(item.name);
                if (channel != null) {
                    thingBuilder.withoutChannel(channel.getUID());
                    nbRemoved++;
                }
            }
            if (nbRemoved > 0) {
                updateThing(thingBuilder.build());
                logger.debug("{} channels removed for the thing {} (from {} items)", nbRemoved, getThing().getUID(),
                        items.size());
            }
        }
    }

    private void setStateOptions(List<Item> items) {
        for (Item item : items) {
            Channel channel = getThing().getChannel(item.name);
            StateDescription descr = item.stateDescription;
            List<Option> options = descr == null ? null : descr.options;
            if (channel != null && options != null && options.size() > 0) {
                List<StateOption> stateOptions = new ArrayList<>();
                for (Option option : options) {
                    stateOptions.add(new StateOption(option.value, option.label));
                }
                stateDescriptionProvider.setStateOptions(channel.getUID(), stateOptions);
                logger.trace("{} options set for the channel {}", options.size(), channel.getUID());
            }
        }
    }

    public void checkConnection(RemoteopenhabRestClient client) {
        logger.debug("Try the root REST API...");
        try {
            client.tryApi();
            if (client.getRestApiVersion() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "OH 1.x server not supported by the binding");
            } else {
                List<Item> items = client.getRemoteItems();

                createChannels(items, true);
                setStateOptions(items);
                for (Item item : items) {
                    updateChannelState(item.name, null, item.state);
                }

                updateStatus(ThingStatus.ONLINE);

                restartStreamingUpdates();
            }
        } catch (RemoteopenhabException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            stopStreamingUpdates();
        }
    }

    private void startCheckConnectionJob(RemoteopenhabRestClient client) {
        ScheduledFuture<?> localCheckConnectionJob = checkConnectionJob;
        if (localCheckConnectionJob == null || localCheckConnectionJob.isCancelled()) {
            checkConnectionJob = scheduler.scheduleWithFixedDelay(() -> {
                long millisSinceLastEvent = System.currentTimeMillis() - client.getLastEventTimestamp();
                if (millisSinceLastEvent > CONNECTION_TIMEOUT_MILLIS) {
                    logger.debug("Check: Disconnected from streaming events, millisSinceLastEvent={}",
                            millisSinceLastEvent);
                    checkConnection(client);
                } else {
                    logger.debug("Check: Receiving streaming events, millisSinceLastEvent={}", millisSinceLastEvent);
                }
            }, 0, CONNECTION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private void stopCheckConnectionJob() {
        ScheduledFuture<?> localCheckConnectionJob = checkConnectionJob;
        if (localCheckConnectionJob != null) {
            localCheckConnectionJob.cancel(true);
            checkConnectionJob = null;
        }
    }

    private void restartStreamingUpdates() {
        RemoteopenhabRestClient client = restClient;
        if (client != null) {
            synchronized (client) {
                stopStreamingUpdates();
                startStreamingUpdates();
            }
        }
    }

    private void startStreamingUpdates() {
        RemoteopenhabRestClient client = restClient;
        if (client != null) {
            synchronized (client) {
                client.addStreamingDataListener(this);
                client.start();
            }
        }
    }

    private void stopStreamingUpdates() {
        RemoteopenhabRestClient client = restClient;
        if (client != null) {
            synchronized (client) {
                client.stop();
                client.removeStreamingDataListener(this);
            }
        }
    }

    @Override
    public void onConnected() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onError(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    @Override
    public void onItemStateEvent(String itemName, String stateType, String state) {
        updateChannelState(itemName, stateType, state);
    }

    @Override
    public void onItemAdded(Item item) {
        createChannels(List.of(item), false);
    }

    @Override
    public void onItemRemoved(Item item) {
        removeChannels(List.of(item));
    }

    @Override
    public void onItemUpdated(Item newItem, Item oldItem) {
        if (!newItem.type.equals(oldItem.type)) {
            createChannels(List.of(newItem), false);
        } else {
            logger.trace("Updated remote item {} ignored because item type {} is unchanged", newItem.name,
                    newItem.type);
        }
    }

    private void updateChannelState(String itemName, @Nullable String stateType, String state) {
        Channel channel = getThing().getChannel(itemName);
        if (channel == null) {
            logger.trace("No channel for item {}", itemName);
            return;
        }
        String acceptedItemType = channel.getAcceptedItemType();
        if (acceptedItemType == null) {
            logger.trace("Channel without accepted item type for item {}", itemName);
            return;
        }
        if (!isLinked(channel.getUID())) {
            logger.trace("Unlinked channel {}", channel.getUID());
            return;
        }
        State channelState = null;
        if (stateType == null && "NULL".equals(state)) {
            channelState = UnDefType.NULL;
        } else if (stateType == null && "UNDEF".equals(state)) {
            channelState = UnDefType.UNDEF;
        } else if ("UnDef".equals(stateType)) {
            switch (state) {
                case "NULL":
                    channelState = UnDefType.NULL;
                    break;
                case "UNDEF":
                    channelState = UnDefType.UNDEF;
                    break;
                default:
                    logger.debug("Invalid UnDef value {} for item {}", state, itemName);
                    break;
            }
        } else if (acceptedItemType.startsWith(CoreItemFactory.NUMBER + ":")) {
            // Item type Number with dimension
            if (checkStateType(itemName, stateType, "Quantity")) {
                List<Class<? extends State>> stateTypes = Collections.singletonList(QuantityType.class);
                channelState = TypeParser.parseState(stateTypes, state);
            }
        } else {
            switch (acceptedItemType) {
                case CoreItemFactory.STRING:
                    if (checkStateType(itemName, stateType, "String")) {
                        channelState = new StringType(state);
                    }
                    break;
                case CoreItemFactory.NUMBER:
                    if (checkStateType(itemName, stateType, "Decimal")) {
                        channelState = new DecimalType(state);
                    }
                    break;
                case CoreItemFactory.SWITCH:
                    if (checkStateType(itemName, stateType, "OnOff")) {
                        channelState = "ON".equals(state) ? OnOffType.ON : OnOffType.OFF;
                    }
                    break;
                case CoreItemFactory.CONTACT:
                    if (checkStateType(itemName, stateType, "OpenClosed")) {
                        channelState = "OPEN".equals(state) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                    }
                    break;
                case CoreItemFactory.DIMMER:
                    if (checkStateType(itemName, stateType, "Percent")) {
                        channelState = new PercentType(state);
                    }
                    break;
                case CoreItemFactory.COLOR:
                    if (checkStateType(itemName, stateType, "HSB")) {
                        channelState = HSBType.valueOf(state);
                    }
                    break;
                case CoreItemFactory.DATETIME:
                    if (checkStateType(itemName, stateType, "DateTime")) {
                        try {
                            channelState = new DateTimeType(ZonedDateTime.parse(state, FORMATTER_DATE));
                        } catch (DateTimeParseException e) {
                            logger.debug("Failed to parse date {} for item {}", state, itemName);
                            channelState = null;
                        }
                    }
                    break;
                case CoreItemFactory.LOCATION:
                    if (checkStateType(itemName, stateType, "Point")) {
                        channelState = new PointType(state);
                    }
                    break;
                case CoreItemFactory.IMAGE:
                    if (checkStateType(itemName, stateType, "Raw")) {
                        channelState = RawType.valueOf(state);
                    }
                    break;
                case CoreItemFactory.PLAYER:
                    if (checkStateType(itemName, stateType, "PlayPause")) {
                        switch (state) {
                            case "PLAY":
                                channelState = PlayPauseType.PLAY;
                                break;
                            case "PAUSE":
                                channelState = PlayPauseType.PAUSE;
                                break;
                            default:
                                logger.debug("Unexpected value {} for item {}", state, itemName);
                                break;
                        }
                    }
                    break;
                case CoreItemFactory.ROLLERSHUTTER:
                    if (checkStateType(itemName, stateType, "Percent")) {
                        channelState = new PercentType(state);
                    }
                    break;
                default:
                    logger.debug("Item type {} is not yet supported", acceptedItemType);
                    break;
            }
        }
        if (channelState != null) {
            updateState(channel.getUID(), channelState);
            String channelStateStr = channelState.toFullString();
            logger.debug("updateState {} with {}", channel.getUID(),
                    channelStateStr.length() < MAX_STATE_SIZE_FOR_LOGGING ? channelStateStr
                            : channelStateStr.substring(0, MAX_STATE_SIZE_FOR_LOGGING) + "...");

        }
    }

    private boolean checkStateType(String itemName, @Nullable String stateType, String expectedType) {
        if (stateType != null && !expectedType.equals(stateType)) {
            logger.debug("Unexpected value type {} for item {}", stateType, itemName);
            return false;
        } else {
            return true;
        }
    }
}
