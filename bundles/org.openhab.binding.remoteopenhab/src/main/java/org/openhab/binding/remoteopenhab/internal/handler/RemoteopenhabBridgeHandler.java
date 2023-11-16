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
package org.openhab.binding.remoteopenhab.internal.handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.remoteopenhab.internal.RemoteopenhabChannelTypeProvider;
import org.openhab.binding.remoteopenhab.internal.RemoteopenhabCommandDescriptionOptionProvider;
import org.openhab.binding.remoteopenhab.internal.RemoteopenhabStateDescriptionOptionProvider;
import org.openhab.binding.remoteopenhab.internal.config.RemoteopenhabServerConfiguration;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabCommandDescription;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabCommandOption;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabItem;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabStateDescription;
import org.openhab.binding.remoteopenhab.internal.data.RemoteopenhabStateOption;
import org.openhab.binding.remoteopenhab.internal.discovery.RemoteopenhabDiscoveryService;
import org.openhab.binding.remoteopenhab.internal.exceptions.RemoteopenhabException;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabItemsDataListener;
import org.openhab.binding.remoteopenhab.internal.listener.RemoteopenhabStreamingDataListener;
import org.openhab.binding.remoteopenhab.internal.rest.RemoteopenhabRestClient;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.TypeParser;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
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
public class RemoteopenhabBridgeHandler extends BaseBridgeHandler
        implements RemoteopenhabStreamingDataListener, RemoteopenhabItemsDataListener {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm[:ss[.SSSSSSSSS][.SSSSSSSS][.SSSSSSS][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]]Z";
    private static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);

    private static final int MAX_STATE_SIZE_FOR_LOGGING = 50;

    private final Logger logger = LoggerFactory.getLogger(RemoteopenhabBridgeHandler.class);

    private final HttpClient httpClientTrustingCert;
    private final RemoteopenhabChannelTypeProvider channelTypeProvider;
    private final RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider;
    private final RemoteopenhabCommandDescriptionOptionProvider commandDescriptionProvider;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    private final Object updateThingLock = new Object();

    private @NonNullByDefault({}) RemoteopenhabServerConfiguration config;

    private @Nullable ScheduledFuture<?> checkConnectionJob;
    private RemoteopenhabRestClient restClient;

    private Map<ChannelUID, State> channelsLastStates = new HashMap<>();

    public RemoteopenhabBridgeHandler(Bridge bridge, HttpClient httpClient, HttpClient httpClientTrustingCert,
            ClientBuilder clientBuilder, SseEventSourceFactory eventSourceFactory,
            RemoteopenhabChannelTypeProvider channelTypeProvider,
            RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider,
            RemoteopenhabCommandDescriptionOptionProvider commandDescriptionProvider, final Gson jsonParser,
            final TranslationProvider i18nProvider, final LocaleProvider localeProvider) {
        super(bridge);
        this.httpClientTrustingCert = httpClientTrustingCert;
        this.channelTypeProvider = channelTypeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.restClient = new RemoteopenhabRestClient(httpClient, clientBuilder, eventSourceFactory, jsonParser,
                i18nProvider);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing remote openHAB handler for bridge {}", getThing().getUID());

        config = getConfigAs(RemoteopenhabServerConfiguration.class);

        String host = config.host.trim();
        if (host.length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-undefined-host");
            return;
        }
        String path = config.restPath.trim();
        if (path.length() == 0 || !path.startsWith("/")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-invalid-rest-path");
            return;
        }
        URL url;
        try {
            url = new URL(config.useHttps ? "https" : "http", host, config.port, path);
        } catch (MalformedURLException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-invalid-rest-url");
            return;
        }

        String urlStr = url.toString();
        logger.debug("REST URL = {}", urlStr);

        restClient.setRestUrl(urlStr);
        restClient.setAuthenticationData(config.authenticateAnyway, config.token, config.username, config.password);
        if (config.useHttps && config.trustedCertificate) {
            restClient.setHttpClient(httpClientTrustingCert);
            restClient.setTrustedCertificate(true);
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.submit(() -> checkConnection(false));
        if (config.accessibilityInterval > 0) {
            startCheckConnectionJob(config.accessibilityInterval, config.aliveInterval, config.restartIfNoActivity);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing remote openHAB handler for bridge {}", getThing().getUID());
        stopStreamingUpdates(false);
        stopCheckConnectionJob();
        channelsLastStates.clear();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        try {
            if (isLinked(channelUID)) {
                restClient.sendCommandToRemoteItem(channelUID.getId(), command);
                String commandStr = command.toFullString();
                logger.debug("Sending command {} to remote item {} succeeded",
                        commandStr.length() < MAX_STATE_SIZE_FOR_LOGGING ? commandStr
                                : commandStr.substring(0, MAX_STATE_SIZE_FOR_LOGGING) + "...",
                        channelUID.getId());
            }
        } catch (RemoteopenhabException e) {
            logger.debug("Handling command for channel {} failed: {}", channelUID.getId(),
                    e.getMessage(bundle, i18nProvider));
        }
    }

    private boolean createChannels(List<RemoteopenhabItem> items, boolean replace) {
        synchronized (updateThingLock) {
            try {
                int nbGroups = 0;
                int nbChannelTypesCreated = 0;
                List<Channel> channels = new ArrayList<>();
                for (RemoteopenhabItem item : items) {
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
                    // Ignore pattern containing a transformation (detected by a parenthesis in the pattern)
                    RemoteopenhabStateDescription stateDescription = item.stateDescription;
                    String pattern = (stateDescription == null || stateDescription.pattern.contains("(")) ? ""
                            : stateDescription.pattern;
                    ChannelTypeUID channelTypeUID;
                    ChannelType channelType = channelTypeProvider.getChannelType(itemType, readOnly, pattern);
                    String label;
                    String description;
                    String defaultValue;
                    if (channelType == null) {
                        channelTypeUID = channelTypeProvider.buildNewChannelTypeUID(itemType);
                        logger.trace("Create the channel type {} for item type {} ({} and with pattern {})",
                                channelTypeUID, itemType, readOnly ? "read only" : "read write", pattern);
                        defaultValue = String.format("Remote %s Item", itemType);
                        label = i18nProvider.getText(bundle, "channel-type.label", defaultValue,
                                localeProvider.getLocale(), itemType);
                        label = label != null && !label.isBlank() ? label : defaultValue;
                        description = i18nProvider.getText(bundle, "channel-type.description", defaultValue,
                                localeProvider.getLocale(), itemType);
                        description = description != null && !description.isBlank() ? description : defaultValue;
                        StateDescriptionFragmentBuilder stateDescriptionBuilder = StateDescriptionFragmentBuilder
                                .create().withReadOnly(readOnly);
                        if (!pattern.isEmpty()) {
                            stateDescriptionBuilder = stateDescriptionBuilder.withPattern(pattern);
                        }
                        channelType = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                                .withDescription(description)
                                .withStateDescriptionFragment(stateDescriptionBuilder.build())
                                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
                        channelTypeProvider.addChannelType(itemType, channelType);
                        nbChannelTypesCreated++;
                    } else {
                        channelTypeUID = channelType.getUID();
                    }
                    ChannelUID channelUID = new ChannelUID(getThing().getUID(), item.name);
                    logger.trace("Create the channel {} of type {}", channelUID, channelTypeUID);
                    defaultValue = String.format("Item %s", item.name);
                    label = i18nProvider.getText(bundle, "channel.label", defaultValue, localeProvider.getLocale(),
                            item.name);
                    label = label != null && !label.isBlank() ? label : defaultValue;
                    description = i18nProvider.getText(bundle, "channel.description", defaultValue,
                            localeProvider.getLocale(), item.name);
                    description = description != null && !description.isBlank() ? description : defaultValue;
                    channels.add(ChannelBuilder.create(channelUID, itemType).withType(channelTypeUID)
                            .withKind(ChannelKind.STATE).withLabel(label).withDescription(description).build());
                }
                ThingBuilder thingBuilder = editThing();
                if (replace) {
                    thingBuilder.withChannels(channels);
                    updateThing(thingBuilder.build());
                    logger.debug(
                            "{} channels defined (with {} different channel types) for the thing {} (from {} items including {} groups)",
                            channels.size(), nbChannelTypesCreated, getThing().getUID(), items.size(), nbGroups);
                } else if (!channels.isEmpty()) {
                    int nbRemoved = 0;
                    for (Channel channel : channels) {
                        if (getThing().getChannel(channel.getUID()) != null) {
                            thingBuilder.withoutChannel(channel.getUID());
                            nbRemoved++;
                        }
                    }
                    if (nbRemoved > 0) {
                        logger.debug("{} channels removed for the thing {} (from {} items)", nbRemoved,
                                getThing().getUID(), items.size());
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
                return true;
            } catch (IllegalArgumentException e) {
                logger.warn("An error occurred while creating the channels for the server {}: {}", getThing().getUID(),
                        e.getMessage());
                return false;
            }
        }
    }

    private void removeChannels(List<RemoteopenhabItem> items) {
        synchronized (updateThingLock) {
            int nbRemoved = 0;
            ThingBuilder thingBuilder = editThing();
            for (RemoteopenhabItem item : items) {
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

    private void setDynamicOptions(List<RemoteopenhabItem> items) {
        for (RemoteopenhabItem item : items) {
            Channel channel = getThing().getChannel(item.name);
            if (channel == null) {
                continue;
            }
            RemoteopenhabStateDescription stateDescr = item.stateDescription;
            List<RemoteopenhabStateOption> stateOptions = stateDescr == null ? null : stateDescr.options;
            if (stateOptions != null && !stateOptions.isEmpty()) {
                List<StateOption> options = new ArrayList<>();
                for (RemoteopenhabStateOption option : stateOptions) {
                    options.add(new StateOption(option.value, option.label));
                }
                stateDescriptionProvider.setStateOptions(channel.getUID(), options);
                logger.trace("{} state options set for the channel {}", options.size(), channel.getUID());
            }
            RemoteopenhabCommandDescription commandDescr = item.commandDescription;
            List<RemoteopenhabCommandOption> commandOptions = commandDescr == null ? null : commandDescr.commandOptions;
            if (commandOptions != null && !commandOptions.isEmpty()) {
                List<CommandOption> options = new ArrayList<>();
                for (RemoteopenhabCommandOption option : commandOptions) {
                    options.add(new CommandOption(option.command, option.label));
                }
                commandDescriptionProvider.setCommandOptions(channel.getUID(), options);
                logger.trace("{} command options set for the channel {}", options.size(), channel.getUID());
            }
        }
    }

    public void checkConnection(boolean restartSse) {
        logger.debug("Try the root REST API...");
        try {
            restClient.tryApi();
            if (restClient.getRestApiVersion() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-unsupported-server");
            } else if (getThing().getStatus() != ThingStatus.ONLINE) {
                List<RemoteopenhabItem> items = restClient.getRemoteItems("name,type,groupType,state,stateDescription");

                if (createChannels(items, true)) {
                    setDynamicOptions(items);
                    for (RemoteopenhabItem item : items) {
                        updateChannelState(item.name, null, item.state, false);
                    }

                    updateStatus(ThingStatus.ONLINE);

                    restartStreamingUpdates();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.error-channels-creation");
                    stopStreamingUpdates();
                }
            } else if (restartSse) {
                logger.debug("The SSE connection is restarted because there was no recent event received");
                restartStreamingUpdates();
            }
        } catch (RemoteopenhabException e) {
            logger.debug("checkConnection for thing {} failed: {}", getThing().getUID(),
                    e.getMessage(bundle, i18nProvider), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
            stopStreamingUpdates();
        }
    }

    private void startCheckConnectionJob(int accessibilityInterval, int aliveInterval, boolean restartIfNoActivity) {
        ScheduledFuture<?> localCheckConnectionJob = checkConnectionJob;
        if (localCheckConnectionJob == null || localCheckConnectionJob.isCancelled()) {
            checkConnectionJob = scheduler.scheduleWithFixedDelay(() -> {
                long millisSinceLastEvent = System.currentTimeMillis() - restClient.getLastEventTimestamp();
                if (getThing().getStatus() != ThingStatus.ONLINE || aliveInterval == 0
                        || restClient.getLastEventTimestamp() == 0) {
                    logger.debug("Time to check server accessibility");
                    checkConnection(restartIfNoActivity && aliveInterval != 0);
                } else if (millisSinceLastEvent > (aliveInterval * 60000)) {
                    logger.debug(
                            "Time to check server accessibility (maybe disconnected from streaming events, millisSinceLastEvent={})",
                            millisSinceLastEvent);
                    checkConnection(restartIfNoActivity);
                } else {
                    logger.debug(
                            "Bypass server accessibility check (receiving streaming events, millisSinceLastEvent={})",
                            millisSinceLastEvent);
                }
            }, accessibilityInterval, accessibilityInterval, TimeUnit.MINUTES);
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
        synchronized (restClient) {
            stopStreamingUpdates();
            startStreamingUpdates();
        }
    }

    private void startStreamingUpdates() {
        synchronized (restClient) {
            restClient.addStreamingDataListener(this);
            restClient.addItemsDataListener(this);
            restClient.start();
        }
    }

    private void stopStreamingUpdates() {
        stopStreamingUpdates(true);
    }

    private void stopStreamingUpdates(boolean waitingForCompletion) {
        synchronized (restClient) {
            restClient.stop(waitingForCompletion);
            restClient.removeStreamingDataListener(this);
            restClient.removeItemsDataListener(this);
        }
    }

    public RemoteopenhabRestClient gestRestClient() {
        return restClient;
    }

    @Override
    public void onConnected() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/offline.comm-error-disconnected");
    }

    @Override
    public void onError(String message) {
        logger.debug("onError: {}", message);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/offline.comm-error-receiving-events");
    }

    @Override
    public void onItemStateEvent(String itemName, String stateType, String state, boolean onlyIfStateChanged) {
        updateChannelState(itemName, stateType, state, onlyIfStateChanged);
    }

    @Override
    public void onItemAdded(RemoteopenhabItem item) {
        createChannels(List.of(item), false);
    }

    @Override
    public void onItemRemoved(RemoteopenhabItem item) {
        removeChannels(List.of(item));
    }

    @Override
    public void onItemUpdated(RemoteopenhabItem newItem, RemoteopenhabItem oldItem) {
        if (!newItem.type.equals(oldItem.type)) {
            createChannels(List.of(newItem), false);
        } else {
            logger.trace("Updated remote item {} ignored because item type {} is unchanged", newItem.name,
                    newItem.type);
        }
    }

    @Override
    public void onItemOptionsUpdatedd(RemoteopenhabItem item) {
        setDynamicOptions(List.of(item));
    }

    private void updateChannelState(String itemName, @Nullable String stateType, String state,
            boolean onlyIfStateChanged) {
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
        try {
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
                if (stateType == null || "Quantity".equals(stateType)) {
                    List<Class<? extends State>> stateTypes = List.of(QuantityType.class);
                    channelState = TypeParser.parseState(stateTypes, state);
                } else if ("Decimal".equals(stateType)) {
                    channelState = new DecimalType(state);
                } else {
                    logger.debug("Unexpected value type {} for item {}", stateType, itemName);
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
                            channelState = new DateTimeType(ZonedDateTime.parse(state, FORMATTER_DATE));
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
        } catch (IllegalArgumentException | DateTimeException e) {
            logger.warn("Failed to parse state \"{}\" for item {}: {}", state, itemName, e.getMessage());
            channelState = UnDefType.UNDEF;
        }
        if (channelState != null) {
            if (onlyIfStateChanged && channelState.equals(channelsLastStates.get(channel.getUID()))) {
                logger.trace("ItemStateChangedEvent ignored for item {} as state is identical to the last state",
                        itemName);
                return;
            }
            channelsLastStates.put(channel.getUID(), channelState);
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

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RemoteopenhabDiscoveryService.class);
    }
}
