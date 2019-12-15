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
package org.openhab.binding.homeconnect.internal.handler;

import static org.eclipse.smarthome.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.listener.ServerSentEventListener;
import org.openhab.binding.homeconnect.internal.client.model.AvailableProgramOption;
import org.openhab.binding.homeconnect.internal.client.model.Data;
import org.openhab.binding.homeconnect.internal.client.model.Event;
import org.openhab.binding.homeconnect.internal.client.model.HomeAppliance;
import org.openhab.binding.homeconnect.internal.client.model.Option;
import org.openhab.binding.homeconnect.internal.client.model.Program;
import org.openhab.binding.homeconnect.internal.logger.EmbeddedLoggingService;
import org.openhab.binding.homeconnect.internal.logger.LogWriter;
import org.openhab.binding.homeconnect.internal.logger.Type;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.slf4j.event.Level;

/**
 * The {@link AbstractHomeConnectThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractHomeConnectThingHandler extends BaseThingHandler implements ServerSentEventListener {

    private static final int CACHE_TTL = 2;

    private @Nullable String operationState;

    private final ConcurrentHashMap<String, EventHandler> eventHandlers;
    private final ConcurrentHashMap<String, ChannelUpdateHandler> channelUpdateHandlers;
    private final HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private final ExpiringCacheMap<ChannelUID, State> stateCache;
    private final LogWriter logger;

    public AbstractHomeConnectThingHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            EmbeddedLoggingService loggingService) {
        super(thing);
        eventHandlers = new ConcurrentHashMap<>();
        channelUpdateHandlers = new ConcurrentHashMap<>();
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
        logger = loggingService.getLogger(AbstractHomeConnectThingHandler.class);
        stateCache = new ExpiringCacheMap<>(TimeUnit.SECONDS.toMillis(CACHE_TTL));

        configureEventHandlers(eventHandlers);
        configureChannelUpdateHandlers(channelUpdateHandlers);
    }

    @Override
    public void initialize() {
        logger.debugWithHaId(getThingHaId(), "Initialize thing handler ({}).", getThingLabel());

        Bridge bridge = getBridge();
        if (bridge != null && ThingStatus.ONLINE.equals(bridge.getStatus())) {
            refreshThingStatus();
            updateSelectedProgramStateDescription();
            updateChannels();
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler != null && bridgeHandler instanceof HomeConnectBridgeHandler) {
                HomeConnectBridgeHandler homeConnectBridgeHandler = (HomeConnectBridgeHandler) bridgeHandler;
                homeConnectBridgeHandler.registerServerSentEventListener(this);
            }
        } else {
            logger.debugWithHaId(getThingHaId(), "Bridge is not online ({}), skip initialization of thing handler.",
                    getThingLabel());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler != null && bridgeHandler instanceof HomeConnectBridgeHandler) {
                HomeConnectBridgeHandler homeConnectBridgeHandler = (HomeConnectBridgeHandler) bridgeHandler;
                homeConnectBridgeHandler.unregisterServerSentEventListener(this);
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        logger.debugWithHaId(getThingHaId(), "Bridge status changed to {} ({}).", bridgeStatusInfo, getThingLabel());

        dispose();
        initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (isThingReadyToHandleCommand()) {
            logger.debugWithHaId(getThingHaId(), "Handle \"{}\" command ({}).", command, channelUID.getId());
            try {
                HomeConnectApiClient apiClient = getApiClient();

                if (command instanceof RefreshType) {
                    updateChannel(channelUID);
                } else if (command instanceof StringType && CHANNEL_BASIC_ACTIONS_STATE.equals(channelUID.getId())
                        && apiClient != null) {
                    updateState(channelUID, new StringType(""));

                    if ("start".equalsIgnoreCase(command.toFullString())) {
                        apiClient.startSelectedProgram(getThingHaId());
                    } else if ("stop".equalsIgnoreCase(command.toFullString())) {
                        apiClient.stopProgram(getThingHaId());
                    } else if ("selected".equalsIgnoreCase(command.toFullString())) {
                        apiClient.getSelectedProgram(getThingHaId());
                    } else {
                        logger.log(Type.DEFAULT, Level.INFO, getThingHaId(), getThingLabel(),
                                Arrays.asList(command.toFullString()), null, null, "Start custom program");
                        apiClient.startCustomProgram(getThingHaId(), command.toFullString());
                    }
                } else if (command instanceof StringType && CHANNEL_SELECTED_PROGRAM_STATE.equals(channelUID.getId())
                        && apiClient != null) {
                    apiClient.setSelectedProgram(getThingHaId(), command.toFullString());
                }
            } catch (CommunicationException e) {
                logger.warnWithHaId(getThingHaId(), "Could not handle command {}. API communication problem! error: {}",
                        command.toFullString(), e.getMessage());
            } catch (AuthorizationException e) {
                logger.warnWithHaId(getThingHaId(), "Could not handle command {}. Authorization problem! error: {}",
                        command.toFullString(), e.getMessage());

                handleAuthenticationError(e);
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        logger.debugWithHaId(getThingHaId(), "{}", event);

        if (EVENT_DISCONNECTED.equals(event.getKey())) {
            logger.infoWithHaId(getThingHaId(), "Received DISCONNECTED event. Set {} to OFFLINE.",
                    getThing().getLabel());
            updateStatus(ThingStatus.OFFLINE);
            resetChannelsOnOfflineEvent();
            resetProgramStateChannels();
        } else if (EVENT_CONNECTED.equals(event.getKey()) && ThingStatus.ONLINE.equals(getThing().getStatus())) {
            logger.infoWithHaId(getThingHaId(), "Received CONNECTED event. Update power state channel.");
            getThingChannel(CHANNEL_POWER_STATE).ifPresent(c -> updateChannel(c.getUID()));
        } else if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
            updateStatus(ThingStatus.ONLINE);
            logger.infoWithHaId(getThingHaId(), "Set {} to ONLINE and update channels.", getThing().getLabel());
            updateChannels();
        }

        if (EVENT_OPERATION_STATE.contentEquals(event.getKey())) {
            operationState = event.getValue() == null ? null : event.getValue();
        }

        if (eventHandlers.containsKey(event.getKey())) {
            eventHandlers.get(event.getKey()).handle(event);
        } else {
            logger.debugWithHaId(getThingHaId(), "No event handler registered for event {}. Ignore event.", event);
        }
    }

    @Override
    public void onReconnectFailed() {
        logger.errorWithHaId(getThingHaId(), "SSE connection was closed due to authentication problems!");
        handleAuthenticationError(new AuthorizationException("SSE connection was killed!"));
    }

    /**
     * Get {@link HomeConnectApiClient}.
     *
     * @return client instance
     */
    protected @Nullable HomeConnectApiClient getApiClient() {
        HomeConnectApiClient apiClient = null;
        Bridge bridge = getBridge();
        if (bridge != null && ThingStatus.ONLINE.equals(bridge.getStatus())) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler != null && bridgeHandler instanceof HomeConnectBridgeHandler) {
                HomeConnectBridgeHandler homeConnectBridgeHandler = (HomeConnectBridgeHandler) bridgeHandler;
                apiClient = homeConnectBridgeHandler.getApiClient();
            }
        }

        return apiClient;
    }

    /**
     * Update state description of selected program (Fetch programs via API).
     */
    protected void updateSelectedProgramStateDescription() {
        logger.debugWithHaId(getThingHaId(), "updateSelectedProgramStateDescription()");
        Bridge bridge = getBridge();
        if (bridge == null || ThingStatus.OFFLINE.equals(bridge.getStatus())) {
            return;
        }

        if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            return;
        }

        // exclude fridge/freezer as they don't have programs
        if (!(this instanceof HomeConnectFridgeFreezerHandler)) {
            HomeConnectApiClient apiClient = getApiClient();
            if (apiClient != null) {
                try {
                    ArrayList<StateOption> stateOptions = new ArrayList<>();
                    apiClient.getPrograms(getThingHaId()).stream().forEach(p -> {
                        stateOptions.add(new StateOption(p.getKey(), mapStringType(p.getKey())));
                    });

                    StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withPattern("%s")
                            .withReadOnly(stateOptions.isEmpty()).withOptions(stateOptions).build()
                            .toStateDescription();

                    if (stateDescription != null && !stateOptions.isEmpty()) {
                        dynamicStateDescriptionProvider.putStateDescriptions(
                                getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE).get().getUID().getAsString(),
                                stateDescription);
                    } else {
                        logger.debugWithHaId(getThingHaId(), "No state description available.");
                        removeSelectedProgramStateDescription();
                    }
                } catch (CommunicationException | AuthorizationException e) {
                    logger.errorWithHaId(getThingHaId(), "Could not fetch available programs. {}", e.getMessage());
                    removeSelectedProgramStateDescription();
                }
            } else {
                removeSelectedProgramStateDescription();
            }
        }
    }

    /**
     * Remove state description of selected program.
     */
    protected void removeSelectedProgramStateDescription() {
        // exclude fridge/freezer as they don't have programs
        if (!(this instanceof HomeConnectFridgeFreezerHandler)) {
            dynamicStateDescriptionProvider.removeStateDescriptions(
                    getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE).get().getUID().getAsString());
        }
    }

    /**
     * Is thing ready to process commands. If bridge or thing itself is offline commands will be ignored.
     *
     * @return
     */
    protected boolean isThingReadyToHandleCommand() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.warnWithHaId(getThingHaId(), "BridgeHandler not found. Cannot handle command without bridge.");
            return false;
        }
        if (ThingStatus.OFFLINE.equals(bridge.getStatus())) {
            logger.debugWithHaId(getThingHaId(), "Bridge is OFFLINE. Ignore command.");
            return false;
        }

        if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            logger.debugWithHaId(getThingHaId(), "{} is OFFLINE. Ignore command.", getThing().getLabel());
            return false;
        }

        return true;
    }

    /**
     * Get thing channel by given channel id.
     *
     * @param channelId
     * @return
     */
    protected Optional<Channel> getThingChannel(String channelId) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            return Optional.empty();
        } else {
            return Optional.of(channel);
        }
    }

    /**
     * Configure channel update handlers. Classes which extend {@link AbstractHomeConnectThingHandler} must implement
     * this class and add handlers.
     *
     * @param handlers channel update handlers
     */
    protected abstract void configureChannelUpdateHandlers(
            final ConcurrentHashMap<String, ChannelUpdateHandler> handlers);

    /**
     * Configure event handlers. Classes which extend {@link AbstractHomeConnectThingHandler} must implement
     * this class and add handlers.
     *
     * @param handlers Server-Sent-Event handlers
     */
    protected abstract void configureEventHandlers(final ConcurrentHashMap<String, EventHandler> handlers);

    /**
     * Update all channels via API.
     *
     */
    protected void updateChannels() {
        logger.traceWithHaId(getThingHaId(), "updateChannels()");

        Bridge bridge = getBridge();
        if (bridge == null || ThingStatus.OFFLINE.equals(bridge.getStatus())) {
            logger.warnWithHaId(getThingHaId(), "Bridge handler not found or offline. Stopping update of channels.");
            return;
        }

        if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            logger.debugWithHaId(getThingHaId(), "{} offline. Stopping update of channels.", getThing().getLabel());
            return;
        }

        List<Channel> channels = getThing().getChannels();
        for (Channel channel : channels) {
            updateChannel(channel.getUID());
        }
    }

    /**
     * Update Channel values via API.
     *
     * @param channelUID
     */
    protected void updateChannel(ChannelUID channelUID) {
        logger.traceWithHaId(getThingHaId(), "updateChannel({})", channelUID.getId());
        HomeConnectApiClient apiClient = getApiClient();

        if (apiClient == null) {
            logger.errorWithHaId(getThingHaId(), "Cannot update channel. No instance of api client found!");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null || ThingStatus.OFFLINE.equals(bridge.getStatus())) {
            logger.warnWithHaId(getThingHaId(), "BridgeHandler not found or offline. Stopping update of channel {}.",
                    channelUID);
            return;
        }

        if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            logger.debugWithHaId(getThingHaId(), "{} offline. Stopping update of channel {}.", getThing().getLabel(),
                    channelUID);
            return;
        }

        if (channelUpdateHandlers.containsKey(channelUID.getId())) {
            try {
                channelUpdateHandlers.get(channelUID.getId()).handle(channelUID, stateCache);
            } catch (CommunicationException e) {
                logger.errorWithHaId(getThingHaId(), "API communication problem while trying to update!", e);
            } catch (AuthorizationException e) {
                logger.errorWithHaId(getThingHaId(), "Authentication problem while trying to update!", e);
                handleAuthenticationError(e);
            }
        }
    }

    /**
     * Reset program related channels.
     */
    protected void resetProgramStateChannels() {
        logger.debugWithHaId(getThingHaId(), "Resetting active program channel states");
    }

    /**
     * Reset all channels on OFFLINE event.
     */
    protected void resetChannelsOnOfflineEvent() {
        logger.debugWithHaId(getThingHaId(), "Resetting channel states due to OFFLINE event.");
        getThingChannel(CHANNEL_POWER_STATE).ifPresent(channel -> updateState(channel.getUID(), OnOffType.OFF));
        getThingChannel(CHANNEL_OPERATION_STATE).ifPresent(channel -> updateState(channel.getUID(), UnDefType.NULL));
        getThingChannel(CHANNEL_DOOR_STATE).ifPresent(channel -> updateState(channel.getUID(), UnDefType.NULL));
        getThingChannel(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE)
                .ifPresent(channel -> updateState(channel.getUID(), UnDefType.NULL));
        getThingChannel(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE)
                .ifPresent(channel -> updateState(channel.getUID(), UnDefType.NULL));
        getThingChannel(CHANNEL_REMOTE_START_ALLOWANCE_STATE)
                .ifPresent(channel -> updateState(channel.getUID(), UnDefType.NULL));
        getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE)
                .ifPresent(channel -> updateState(channel.getUID(), UnDefType.NULL));
    }

    /**
     * Map Home Connect key and value names to label.
     * e.g. Dishcare.Dishwasher.Program.Eco50 --> Eco50 or BSH.Common.EnumType.OperationState.DelayedStart --> Delayed
     * Start
     *
     * @param type
     * @return
     */
    protected String mapStringType(String type) {
        int index = type.lastIndexOf(".");
        if (index > 0 && type.length() > index) {
            String sub = type.substring(index + 1);
            StringBuilder sb = new StringBuilder();
            for (String word : sub.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
                sb.append(" ");
                sb.append(word);
            }
            return sb.toString().trim();
        }
        return type;
    }

    /**
     * Map unit string (returned by home connect api) to Unit
     *
     * @param unit String eg. "°C"
     * @return Unit
     */
    protected Unit<Temperature> mapTemperature(@Nullable String unit) {
        if (unit == null) {
            return CELSIUS;
        } else if (unit.endsWith("C")) {
            return CELSIUS;
        } else {
            return FAHRENHEIT;
        }
    }

    /**
     * Check bridge status and refresh connection status of thing accordingly.
     *
     * @return status has changed
     */
    protected void refreshThingStatus() {
        HomeConnectApiClient client = getApiClient();

        if (client != null) {
            try {
                HomeAppliance homeAppliance = client.getHomeAppliance(getThingHaId());
                if (!homeAppliance.isConnected()) {
                    logger.debugWithHaId(getThingHaId(), "Update status to OFFLINE.");
                    updateStatus(ThingStatus.OFFLINE);
                } else {
                    logger.debugWithHaId(getThingHaId(), "Update status to ONLINE.");
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (CommunicationException | RuntimeException e) {
                logger.debugWithHaId(getThingHaId(),
                        "Update status to OFFLINE. Home Connect service is not reachable or a problem occurred! ({}).",
                        e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Home Connect service is not reachable or a problem occurred! (" + e.getMessage() + ").");
            } catch (AuthorizationException e) {
                logger.debugWithHaId(getThingHaId(),
                        "Update status to OFFLINE. Home Connect service is not reachable or a problem occurred! ({}).",
                        e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Home Connect service is not reachable or a problem occurred! (" + e.getMessage() + ").");

                handleAuthenticationError(e);
            }
        } else {
            logger.debugWithHaId(getThingHaId(), "Update status to OFFLINE (BRIDGE_UNINITIALIZED)");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    /**
     * Get home appliance id of Thing.
     *
     * @return home appliance id
     */
    protected String getThingHaId() {
        return getThing().getConfiguration().get(HA_ID).toString();
    }

    /**
     * Returns the human readable label for this thing.
     *
     * @return the human readable label
     */
    protected @Nullable String getThingLabel() {
        return getThing().getLabel();
    }

    /**
     * Handle authentication exception.
     */
    protected void handleAuthenticationError(AuthorizationException exception) {
        logger.infoWithHaId(getThingHaId(),
                "Thing handler got authentication exception --> clear credential storage ({})", exception.getMessage());
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler != null && bridgeHandler instanceof HomeConnectBridgeHandler) {
                HomeConnectBridgeHandler homeConnectBridgeHandler = (HomeConnectBridgeHandler) bridgeHandler;

                try {
                    homeConnectBridgeHandler.getOAuthClientService().remove();
                } catch (OAuthException e) {
                    logger.errorWithHaId(getThingHaId(), "Could not clear oAuth storage!", e);
                }
                homeConnectBridgeHandler.dispose();
                homeConnectBridgeHandler.initialize();
            }
        }
    }

    /**
     * Get operation state of device.
     *
     * @return
     */
    protected @Nullable String getOperationState() {
        return operationState;
    }

    protected EventHandler defaultElapsedProgramTimeEventHandler() {
        return event -> {
            getThingChannel(CHANNEL_ELAPSED_PROGRAM_TIME).ifPresent(
                    channel -> updateState(channel.getUID(), new QuantityType<>(event.getValueAsInt(), SECOND)));
        };
    }

    protected EventHandler defaultPowerStateEventHandler() {
        return event -> {
            getThingChannel(CHANNEL_POWER_STATE).ifPresent(channel -> updateState(channel.getUID(),
                    STATE_POWER_ON.equals(event.getValue()) ? OnOffType.ON : OnOffType.OFF));

            if (STATE_POWER_ON.equals(event.getValue())) {
                getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE).ifPresent(c -> updateChannel(c.getUID()));
            } else {
                resetProgramStateChannels();
                getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.NULL));
            }
        };
    }

    protected EventHandler defaultDoorStateEventHandler() {
        return event -> {
            getThingChannel(CHANNEL_DOOR_STATE).ifPresent(channel -> updateState(channel.getUID(),
                    STATE_DOOR_OPEN.equals(event.getValue()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED));
        };
    }

    protected EventHandler defaultOperationStateEventHandler() {
        return event -> {
            String value = event.getValue();
            getThingChannel(CHANNEL_OPERATION_STATE).ifPresent(channel -> updateState(channel.getUID(),
                    value == null ? UnDefType.NULL : new StringType(mapStringType(value))));

            if (STATE_OPERATION_FINISHED.equals(event.getValue())) {
                getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE)
                        .ifPresent(c -> updateState(c.getUID(), new QuantityType<>(100, PERCENT)));
                getThingChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE)
                        .ifPresent(c -> updateState(c.getUID(), new QuantityType<>(0, SECOND)));
            } else if (STATE_OPERATION_RUN.equals(event.getValue())) {
                getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE)
                        .ifPresent(c -> updateState(c.getUID(), new QuantityType<>(0, PERCENT)));
                getThingChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateChannel(c.getUID()));
            } else if (STATE_OPERATION_READY.equals(event.getValue())) {
                resetProgramStateChannels();
            }
        };
    }

    protected EventHandler defaultActiveProgramEventHandler() {
        return event -> {
            String value = event.getValue();
            getThingChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(channel -> {
                updateState(channel.getUID(), value == null ? UnDefType.NULL : new StringType(mapStringType(value)));
            });
            if (event.getValue() == null) {
                resetProgramStateChannels();
            }
        };
    }

    protected EventHandler defaultEventPresentStateEventHandler(String channelId) {
        return event -> {
            getThingChannel(channelId).ifPresent(channel -> updateState(channel.getUID(),
                    STATE_EVENT_PRESENT_STATE_OFF.equals(event.getValue()) ? OnOffType.OFF : OnOffType.ON));
        };
    }

    protected EventHandler defaultBooleanEventHandler(String channelId) {
        return event -> {
            getThingChannel(channelId).ifPresent(
                    channel -> updateState(channel.getUID(), event.getValueAsBoolean() ? OnOffType.ON : OnOffType.OFF));
        };
    }

    protected EventHandler defaultRemainingProgramTimeEventHandler() {
        return event -> {
            getThingChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE).ifPresent(
                    channel -> updateState(channel.getUID(), new QuantityType<>(event.getValueAsInt(), SECOND)));
        };
    }

    protected EventHandler defaultSelectedProgramStateEventHandler() {
        return event -> {
            getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE).ifPresent(channel -> {
                updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.NULL : new StringType(event.getValue()));
            });
        };
    }

    protected EventHandler updateProgramOptionsAndSelectedProgramStateEventHandler() {
        return event -> {
            defaultSelectedProgramStateEventHandler().handle(event);

            // update available program options
            try {
                String programKey = event.getValue();
                if (programKey != null) {
                    updateProgramOptionsStateDescriptions(programKey);
                }
            } catch (CommunicationException | AuthorizationException e) {
                logger.warn("Could not update program options. {}", e.getMessage());
            }
        };
    }

    protected EventHandler defaultProgramProgressEventHandler() {
        return event -> {
            getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE).ifPresent(
                    channel -> updateState(channel.getUID(), new QuantityType<>(event.getValueAsInt(), PERCENT)));
        };
    }

    protected ChannelUpdateHandler defaultDoorStateChannelUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Data data = apiClient.getDoorState(getThingHaId());
                    if (data.getValue() != null) {
                        return STATE_DOOR_OPEN.equals(data.getValue()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                    } else {
                        return UnDefType.NULL;
                    }
                } else {
                    return UnDefType.NULL;
                }
            }));
        };
    }

    protected ChannelUpdateHandler defaultPowerStateChannelUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Data data = apiClient.getPowerState(getThingHaId());
                    if (data.getValue() != null) {
                        return STATE_POWER_ON.equals(data.getValue()) ? OnOffType.ON : OnOffType.OFF;
                    } else {
                        return UnDefType.NULL;
                    }
                } else {
                    return UnDefType.NULL;
                }
            }));
        };
    }

    protected ChannelUpdateHandler defaultNoOpUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, UnDefType.NULL);
        };
    }

    protected ChannelUpdateHandler defaultOperationStateChannelUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Data data = apiClient.getOperationState(getThingHaId());

                    String value = data.getValue();
                    if (value != null) {
                        operationState = data.getValue();
                        return new StringType(mapStringType(value));
                    } else {
                        operationState = null;
                        return UnDefType.NULL;
                    }
                } else {
                    return UnDefType.NULL;
                }
            }));
        };
    }

    protected ChannelUpdateHandler defaultRemoteControlActiveStateChannelUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    return apiClient.isRemoteControlActive(getThingHaId()) ? OnOffType.ON : OnOffType.OFF;
                }
                return OnOffType.OFF;
            }));
        };
    }

    protected ChannelUpdateHandler defaultLocalControlActiveStateChannelUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    return apiClient.isLocalControlActive(getThingHaId()) ? OnOffType.ON : OnOffType.OFF;
                }
                return OnOffType.OFF;
            }));
        };
    }

    protected ChannelUpdateHandler defaultRemoteStartAllowanceChannelUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    return apiClient.isRemoteControlStartAllowed(getThingHaId()) ? OnOffType.ON : OnOffType.OFF;
                }
                return OnOffType.OFF;
            }));
        };
    }

    protected ChannelUpdateHandler defaultSelectedProgramStateUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Program program = apiClient.getSelectedProgram(getThingHaId());
                    if (program != null) {
                        processProgramOptions(program.getOptions());
                        return new StringType(program.getKey());
                    } else {
                        return UnDefType.NULL;
                    }
                }
                return UnDefType.NULL;
            }));
        };
    }

    protected ChannelUpdateHandler updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Program program = apiClient.getSelectedProgram(getThingHaId());

                    if (program != null) {
                        updateProgramOptionsStateDescriptions(program.getKey());
                        processProgramOptions(program.getOptions());

                        return new StringType(program.getKey());
                    } else {
                        return UnDefType.NULL;
                    }
                }
                return UnDefType.NULL;
            }));
        };
    }

    protected ChannelUpdateHandler defaultActiveProgramStateUpdateHandler() {
        return (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Program program = apiClient.getActiveProgram(getThingHaId());

                    if (program != null) {
                        processProgramOptions(program.getOptions());
                        return new StringType(mapStringType(program.getKey()));
                    } else {
                        resetProgramStateChannels();
                        return UnDefType.NULL;
                    }
                }
                return UnDefType.NULL;
            }));
        };
    }

    protected void processProgramOptions(List<Option> options) {
        options.forEach(option -> {
            String key = option.getKey();
            if (key != null) {
                switch (key) {
                    case OPTION_WASHER_TEMPERATURE:
                        getThingChannel(CHANNEL_WASHER_TEMPERATURE)
                                .ifPresent(channel -> updateState(channel.getUID(), new StringType(option.getValue())));
                        break;
                    case OPTION_WASHER_SPIN_SPEED:
                        getThingChannel(CHANNEL_WASHER_SPIN_SPEED)
                                .ifPresent(channel -> updateState(channel.getUID(), new StringType(option.getValue())));
                        break;
                    case OPTION_WASHER_IDOS_1_DOSING_LEVEL:
                        getThingChannel(CHANNEL_WASHER_IDOS1)
                                .ifPresent(channel -> updateState(channel.getUID(), new StringType(option.getValue())));
                        break;
                    case OPTION_WASHER_IDOS_2_DOSING_LEVEL:
                        getThingChannel(CHANNEL_WASHER_IDOS2)
                                .ifPresent(channel -> updateState(channel.getUID(), new StringType(option.getValue())));
                        break;
                    case OPTION_DRYER_DRYING_TARGET:
                        getThingChannel(CHANNEL_DRYER_DRYING_TARGET)
                                .ifPresent(channel -> updateState(channel.getUID(), new StringType(option.getValue())));
                        break;
                    case OPTION_HOOD_INTENSIVE_LEVEL:
                        getThingChannel(CHANNEL_HOOD_INTENSIVE_LEVEL)
                                .ifPresent(channel -> updateState(channel.getUID(), new StringType(option.getValue())));
                        break;
                    case OPTION_HOOD_VENTING_LEVEL:
                        getThingChannel(CHANNEL_HOOD_VENTING_LEVEL)
                                .ifPresent(channel -> updateState(channel.getUID(), new StringType(option.getValue())));
                        break;
                    case OPTION_SETPOINT_TEMPERATURE:
                        getThingChannel(CHANNEL_SETPOINT_TEMPERATURE).ifPresent(channel -> updateState(channel.getUID(),
                                new QuantityType<>(option.getValueAsInt(), mapTemperature(option.getUnit()))));
                        break;
                    case OPTION_DURATION:
                        getThingChannel(CHANNEL_DURATION).ifPresent(channel -> updateState(channel.getUID(),
                                new QuantityType<>(option.getValueAsInt(), SECOND)));
                        break;
                    case OPTION_REMAINING_PROGRAM_TIME:
                        getThingChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE)
                                .ifPresent(channel -> updateState(channel.getUID(),
                                        new QuantityType<>(option.getValueAsInt(), SECOND)));
                        break;
                    case OPTION_ELAPSED_PROGRAM_TIME:
                        getThingChannel(CHANNEL_ELAPSED_PROGRAM_TIME).ifPresent(channel -> updateState(channel.getUID(),
                                new QuantityType<>(option.getValueAsInt(), SECOND)));
                        break;
                    case OPTION_PROGRAM_PROGRESS:
                        getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE)
                                .ifPresent(channel -> updateState(channel.getUID(),
                                        new QuantityType<>(option.getValueAsInt(), PERCENT)));
                        break;
                }
            }
        });
    }

    protected State cachePutIfAbsentAndGet(ChannelUID channelUID, ExpiringCacheMap<ChannelUID, State> cache,
            SupplierWithException<State> supplier) {
        State state = cache.putIfAbsentAndGet(channelUID, () -> {
            try {
                return supplier.get();
            } catch (CommunicationException e) {
                logger.errorWithHaId(getThingHaId(), "API communication problem while trying to update!", e);
                return UnDefType.NULL;
            } catch (AuthorizationException e) {
                logger.errorWithHaId(getThingHaId(), "Authentication problem while trying to update!", e);
                handleAuthenticationError(e);
                return UnDefType.NULL;
            }
        });
        if (state == null) {
            return UnDefType.NULL;
        }
        return state;
    }

    protected String convertWasherTemperature(String value) {
        if (value.startsWith("LaundryCare.Washer.EnumType.Temperature.GC")) {
            return value.replace("LaundryCare.Washer.EnumType.Temperature.GC", "") + "°C";
        }

        if (value.startsWith("LaundryCare.Washer.EnumType.Temperature.Ul")) {
            return mapStringType(value.replace("LaundryCare.Washer.EnumType.Temperature.Ul", ""));
        }

        return mapStringType(value);
    }

    protected String convertWasherSpinSpeed(String value) {
        if (value.startsWith("LaundryCare.Washer.EnumType.SpinSpeed.RPM")) {
            return value.replace("LaundryCare.Washer.EnumType.SpinSpeed.RPM", "") + " RPM";
        }

        if (value.startsWith("LaundryCare.Washer.EnumType.SpinSpeed.Ul")) {
            return value.replace("LaundryCare.Washer.EnumType.SpinSpeed.Ul", "");
        }

        return mapStringType(value);
    }

    protected String convertLevel(String value) {
        if (value.startsWith("Cooking.Hood.EnumType.IntensiveStage.IntensiveStage")) {
            return value.replace("Cooking.Hood.EnumType.IntensiveStage.IntensiveStage", "");
        }

        if (value.startsWith("Cooking.Hood.EnumType.Stage.FanStage0")) {
            return value.replace("Cooking.Hood.EnumType.Stage.FanStage0", "");
        }

        return mapStringType(value);
    }

    protected void updateProgramOptionsStateDescriptions(String programKey)
            throws CommunicationException, AuthorizationException {
        HomeConnectApiClient apiClient = getApiClient();
        if (apiClient != null) {
            List<AvailableProgramOption> availableProgramOptions = apiClient.getProgramOptions(getThingHaId(),
                    programKey);

            Optional<Channel> channelSpinSpeed = getThingChannel(CHANNEL_WASHER_SPIN_SPEED);
            Optional<Channel> channelTemperature = getThingChannel(CHANNEL_WASHER_TEMPERATURE);
            Optional<Channel> channelDryingTarget = getThingChannel(CHANNEL_DRYER_DRYING_TARGET);
            Optional<Channel> channelHoodIntensiveLevel = getThingChannel(CHANNEL_HOOD_INTENSIVE_LEVEL);
            Optional<Channel> channelHoodVentingLevel = getThingChannel(CHANNEL_HOOD_VENTING_LEVEL);

            if (availableProgramOptions.isEmpty()) {
                if (channelSpinSpeed.isPresent()) {
                    dynamicStateDescriptionProvider
                            .removeStateDescriptions(channelSpinSpeed.get().getUID().getAsString());
                }
                if (channelTemperature.isPresent()) {
                    dynamicStateDescriptionProvider
                            .removeStateDescriptions(channelTemperature.get().getUID().getAsString());
                }
                if (channelDryingTarget.isPresent()) {
                    dynamicStateDescriptionProvider
                            .removeStateDescriptions(channelDryingTarget.get().getUID().getAsString());
                }
                if (channelHoodIntensiveLevel.isPresent()) {
                    dynamicStateDescriptionProvider
                            .removeStateDescriptions(channelHoodIntensiveLevel.get().getUID().getAsString());
                }
                if (channelHoodVentingLevel.isPresent()) {
                    dynamicStateDescriptionProvider
                            .removeStateDescriptions(channelHoodVentingLevel.get().getUID().getAsString());
                }
            }

            availableProgramOptions.stream().forEach(option -> {
                if (OPTION_WASHER_SPIN_SPEED.equals(option.getKey())) {
                    ArrayList<StateOption> stateOptions = new ArrayList<>();

                    option.getAllowedValues()
                            .forEach(av -> stateOptions.add(new StateOption(av, convertWasherSpinSpeed(av))));
                    StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withPattern("%s")
                            .withReadOnly(stateOptions.isEmpty()).withOptions(stateOptions).build()
                            .toStateDescription();

                    if (stateDescription != null) {
                        if (channelSpinSpeed.isPresent()) {
                            dynamicStateDescriptionProvider.putStateDescriptions(
                                    channelSpinSpeed.get().getUID().getAsString(), stateDescription);
                        }
                    }
                } else if (OPTION_WASHER_TEMPERATURE.equals(option.getKey())) {
                    ArrayList<StateOption> stateOptions = new ArrayList<>();

                    option.getAllowedValues()
                            .forEach(av -> stateOptions.add(new StateOption(av, convertWasherTemperature(av))));
                    StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withPattern("%s")
                            .withReadOnly(stateOptions.isEmpty()).withOptions(stateOptions).build()
                            .toStateDescription();

                    if (stateDescription != null) {
                        if (channelTemperature.isPresent()) {
                            dynamicStateDescriptionProvider.putStateDescriptions(
                                    channelTemperature.get().getUID().getAsString(), stateDescription);
                        }
                    }
                } else if (OPTION_DRYER_DRYING_TARGET.equals(option.getKey())) {
                    ArrayList<StateOption> stateOptions = new ArrayList<>();

                    option.getAllowedValues().forEach(av -> stateOptions.add(new StateOption(av, mapStringType(av))));
                    StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withPattern("%s")
                            .withReadOnly(stateOptions.isEmpty()).withOptions(stateOptions).build()
                            .toStateDescription();

                    if (stateDescription != null) {
                        if (channelDryingTarget.isPresent()) {
                            dynamicStateDescriptionProvider.putStateDescriptions(
                                    channelDryingTarget.get().getUID().getAsString(), stateDescription);
                        }
                    }
                } else if (OPTION_HOOD_INTENSIVE_LEVEL.equals(option.getKey())) {
                    ArrayList<StateOption> stateOptions = new ArrayList<>();

                    option.getAllowedValues().forEach(av -> stateOptions.add(new StateOption(av, convertLevel(av))));
                    StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withPattern("%s")
                            .withReadOnly(stateOptions.isEmpty()).withOptions(stateOptions).build()
                            .toStateDescription();

                    if (stateDescription != null) {
                        if (channelHoodIntensiveLevel.isPresent()) {
                            dynamicStateDescriptionProvider.putStateDescriptions(
                                    channelHoodIntensiveLevel.get().getUID().getAsString(), stateDescription);
                        }
                    }
                } else if (OPTION_HOOD_VENTING_LEVEL.equals(option.getKey())) {
                    ArrayList<StateOption> stateOptions = new ArrayList<>();

                    option.getAllowedValues().forEach(av -> stateOptions.add(new StateOption(av, convertLevel(av))));
                    StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withPattern("%s")
                            .withReadOnly(stateOptions.isEmpty()).withOptions(stateOptions).build()
                            .toStateDescription();

                    if (stateDescription != null) {
                        if (channelHoodVentingLevel.isPresent()) {
                            dynamicStateDescriptionProvider.putStateDescriptions(
                                    channelHoodVentingLevel.get().getUID().getAsString(), stateDescription);
                        }
                    }
                }
            });
        }
    }
}
