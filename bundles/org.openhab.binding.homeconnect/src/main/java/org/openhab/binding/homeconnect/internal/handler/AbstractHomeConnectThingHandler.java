/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static java.util.Collections.emptyList;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;
import static org.openhab.binding.homeconnect.internal.client.model.EventType.*;
import static org.openhab.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.*;
import static org.openhab.core.thing.ThingStatus.*;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.HomeConnectEventSourceClient;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.listener.HomeConnectEventListener;
import org.openhab.binding.homeconnect.internal.client.model.AvailableProgramOption;
import org.openhab.binding.homeconnect.internal.client.model.Data;
import org.openhab.binding.homeconnect.internal.client.model.Event;
import org.openhab.binding.homeconnect.internal.client.model.HomeAppliance;
import org.openhab.binding.homeconnect.internal.client.model.Option;
import org.openhab.binding.homeconnect.internal.client.model.Program;
import org.openhab.binding.homeconnect.internal.handler.cache.ExpiringStateMap;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractHomeConnectThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractHomeConnectThingHandler extends BaseThingHandler implements HomeConnectEventListener {

    private static final int CACHE_TTL_SEC = 2;
    private static final int OFFLINE_MONITOR_1_DELAY_MIN = 30;
    private static final int OFFLINE_MONITOR_2_DELAY_MIN = 4;
    private static final int EVENT_LISTENER_CONNECT_RETRY_DELAY_MIN = 10;

    private @Nullable String operationState;
    private @Nullable ScheduledFuture<?> reinitializationFuture1;
    private @Nullable ScheduledFuture<?> reinitializationFuture2;
    private @Nullable ScheduledFuture<?> reinitializationFuture3;
    private boolean ignoreEventSourceClosedEvent;
    private @Nullable String programOptionsDelayedUpdate;

    private final ConcurrentHashMap<String, EventHandler> eventHandlers;
    private final ConcurrentHashMap<String, ChannelUpdateHandler> channelUpdateHandlers;
    private final HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private final ExpiringStateMap expiringStateMap;
    private final AtomicBoolean accessible;
    private final Logger logger = LoggerFactory.getLogger(AbstractHomeConnectThingHandler.class);
    private final Map<String, List<AvailableProgramOption>> availableProgramOptionsCache;

    public AbstractHomeConnectThingHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing);
        eventHandlers = new ConcurrentHashMap<>();
        channelUpdateHandlers = new ConcurrentHashMap<>();
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
        expiringStateMap = new ExpiringStateMap(Duration.ofSeconds(CACHE_TTL_SEC));
        accessible = new AtomicBoolean(false);
        availableProgramOptionsCache = new ConcurrentHashMap<>();

        configureEventHandlers(eventHandlers);
        configureChannelUpdateHandlers(channelUpdateHandlers);
    }

    @Override
    public void initialize() {
        if (getBridgeHandler().isEmpty()) {
            updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            accessible.set(false);
        } else if (isBridgeOffline()) {
            updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            accessible.set(false);
        } else {
            updateStatus(UNKNOWN);
            scheduler.submit(() -> {
                refreshThingStatus(); // set ONLINE / OFFLINE
                updateSelectedProgramStateDescription();
                updateChannels();
                registerEventListener();
                scheduleOfflineMonitor1();
                scheduleOfflineMonitor2();
            });
        }
    }

    @Override
    public void dispose() {
        stopRetryRegistering();
        stopOfflineMonitor1();
        stopOfflineMonitor2();
        unregisterEventListener(true);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {} ({}). haId={}", bridgeStatusInfo, getThingLabel(), getThingHaId());
        reinitialize();
    }

    private void reinitialize() {
        logger.debug("Reinitialize thing handler ({}). haId={}", getThingLabel(), getThingHaId());
        stopRetryRegistering();
        stopOfflineMonitor1();
        stopOfflineMonitor2();
        unregisterEventListener();
        initialize();
    }

    /**
     * Handles a command for a given channel.
     * <p>
     * This method is only called, if the thing has been initialized (status ONLINE/OFFLINE/UNKNOWN).
     * <p>
     *
     * @param channelUID the {@link ChannelUID} of the channel to which the command was sent
     * @param command the {@link Command}
     * @param apiClient the {@link HomeConnectApiClient}
     * @throws CommunicationException communication problem
     * @throws AuthorizationException authorization problem
     * @throws ApplianceOfflineException appliance offline
     */
    protected void handleCommand(ChannelUID channelUID, Command command, HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        if (command instanceof RefreshType) {
            updateChannel(channelUID);
        } else if (command instanceof StringType && CHANNEL_BASIC_ACTIONS_STATE.equals(channelUID.getId())
                && getBridgeHandler().isPresent()) {
            updateState(channelUID, new StringType(""));

            if (COMMAND_START.equalsIgnoreCase(command.toFullString())) {
                HomeConnectBridgeHandler homeConnectBridgeHandler = getBridgeHandler().get();
                // workaround for api bug
                // if simulator, program options have to be passed along with the desired program
                // if non simulator, some options throw a "SDK.Error.UnsupportedOption" error
                if (homeConnectBridgeHandler.getConfiguration().isSimulator()) {
                    apiClient.startSelectedProgram(getThingHaId());
                } else {
                    Program selectedProgram = apiClient.getSelectedProgram(getThingHaId());
                    if (selectedProgram != null) {
                        apiClient.startProgram(getThingHaId(), selectedProgram.getKey());
                    }
                }
            } else if (COMMAND_STOP.equalsIgnoreCase(command.toFullString())) {
                apiClient.stopProgram(getThingHaId());
            } else if (COMMAND_SELECTED.equalsIgnoreCase(command.toFullString())) {
                apiClient.getSelectedProgram(getThingHaId());
            } else {
                logger.debug("Start custom program. command={} haId={}", command.toFullString(), getThingHaId());
                apiClient.startCustomProgram(getThingHaId(), command.toFullString());
            }
        } else if (command instanceof StringType && CHANNEL_SELECTED_PROGRAM_STATE.equals(channelUID.getId())) {
            apiClient.setSelectedProgram(getThingHaId(), command.toFullString());
        }
    }

    @Override
    public final void handleCommand(ChannelUID channelUID, Command command) {
        var apiClient = getApiClient();
        if ((isThingReadyToHandleCommand() || (this instanceof HomeConnectHoodHandler && isBridgeOnline()
                && isThingAccessibleViaServerSentEvents())) && apiClient.isPresent()) {
            logger.debug("Handle \"{}\" command ({}). haId={}", command, channelUID.getId(), getThingHaId());
            try {
                handleCommand(channelUID, command, apiClient.get());
            } catch (ApplianceOfflineException e) {
                logger.debug("Could not handle command {}. Appliance offline. thing={}, haId={}, error={}",
                        command.toFullString(), getThingLabel(), getThingHaId(), e.getMessage());
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                resetChannelsOnOfflineEvent();
                resetProgramStateChannels(true);
            } catch (CommunicationException e) {
                logger.debug("Could not handle command {}. API communication problem! error={}, haId={}",
                        command.toFullString(), e.getMessage(), getThingHaId());
            } catch (AuthorizationException e) {
                logger.debug("Could not handle command {}. Authorization problem! error={}, haId={}",
                        command.toFullString(), e.getMessage(), getThingHaId());

                handleAuthenticationError(e);
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        if (DISCONNECTED.equals(event.getType())) {
            logger.debug("Received DISCONNECTED event. Set {} to OFFLINE. haId={}", getThing().getLabel(),
                    getThingHaId());
            updateStatus(OFFLINE);
            resetChannelsOnOfflineEvent();
            resetProgramStateChannels(true);
        } else if (isThingOnline() && CONNECTED.equals(event.getType())) {
            logger.debug("Received CONNECTED event. Update power state channel. haId={}", getThingHaId());
            getThingChannel(CHANNEL_POWER_STATE).ifPresent(c -> updateChannel(c.getUID()));
        } else if (isThingOffline() && !KEEP_ALIVE.equals(event.getType())) {
            updateStatus(ONLINE);
            logger.debug("Set {} to ONLINE and update channels. haId={}", getThing().getLabel(), getThingHaId());
            updateSelectedProgramStateDescription();
            updateChannels();
        }

        String key = event.getKey();
        if (EVENT_OPERATION_STATE.equals(key)) {
            operationState = event.getValue() == null ? null : event.getValue();
        }

        if (key != null && eventHandlers.containsKey(key)) {
            EventHandler eventHandler = eventHandlers.get(key);
            if (eventHandler != null) {
                eventHandler.handle(event);
            }
        }

        accessible.set(true);
    }

    @Override
    public void onClosed() {
        if (ignoreEventSourceClosedEvent) {
            logger.debug("Ignoring event source close event. thing={}, haId={}", getThing().getLabel(), getThingHaId());
        } else {
            unregisterEventListener();
            refreshThingStatus();
            registerEventListener();
        }
    }

    @Override
    public void onRateLimitReached() {
        unregisterEventListener();

        // retry registering
        scheduleRetryRegistering();
    }

    /**
     * Register event listener.
     */
    protected void registerEventListener() {
        if (isBridgeOnline() && isThingAccessibleViaServerSentEvents()) {
            getEventSourceClient().ifPresent(client -> {
                try {
                    ignoreEventSourceClosedEvent = false;
                    client.registerEventListener(getThingHaId(), this);
                } catch (CommunicationException | AuthorizationException e) {
                    logger.warn("Could not open event source connection. thing={}, haId={}, error={}", getThingLabel(),
                            getThingHaId(), e.getMessage());
                }
            });
        }
    }

    /**
     * Unregister event listener.
     */
    protected void unregisterEventListener() {
        unregisterEventListener(false);
    }

    private void unregisterEventListener(boolean immediate) {
        getEventSourceClient().ifPresent(client -> {
            ignoreEventSourceClosedEvent = true;
            client.unregisterEventListener(this, immediate, false);
        });
    }

    /**
     * Get {@link HomeConnectApiClient}.
     *
     * @return client instance
     */
    protected Optional<HomeConnectApiClient> getApiClient() {
        return getBridgeHandler().map(HomeConnectBridgeHandler::getApiClient);
    }

    /**
     * Get {@link HomeConnectEventSourceClient}.
     *
     * @return client instance if present
     */
    protected Optional<HomeConnectEventSourceClient> getEventSourceClient() {
        return getBridgeHandler().map(HomeConnectBridgeHandler::getEventSourceClient);
    }

    /**
     * Update state description of selected program (Fetch programs via API).
     */
    protected void updateSelectedProgramStateDescription() {
        if (isBridgeOffline() || isThingOffline()) {
            return;
        }

        Optional<HomeConnectApiClient> apiClient = getApiClient();
        if (apiClient.isPresent()) {
            try {
                List<StateOption> stateOptions = apiClient.get().getPrograms(getThingHaId()).stream()
                        .map(p -> new StateOption(p.getKey(), mapStringType(p.getKey()))).collect(Collectors.toList());

                getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE).ifPresent(
                        channel -> dynamicStateDescriptionProvider.setStateOptions(channel.getUID(), stateOptions));
            } catch (CommunicationException | ApplianceOfflineException | AuthorizationException e) {
                logger.debug("Could not fetch available programs. thing={}, haId={}, error={}", getThingLabel(),
                        getThingHaId(), e.getMessage());
                removeSelectedProgramStateDescription();
            }
        } else {
            removeSelectedProgramStateDescription();
        }
    }

    /**
     * Remove state description of selected program.
     */
    protected void removeSelectedProgramStateDescription() {
        getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE)
                .ifPresent(channel -> dynamicStateDescriptionProvider.setStateOptions(channel.getUID(), emptyList()));
    }

    /**
     * Is thing ready to process commands. If bridge or thing itself is offline commands will be ignored.
     *
     * @return true if ready
     */
    protected boolean isThingReadyToHandleCommand() {
        if (isBridgeOffline()) {
            logger.debug("Bridge is OFFLINE. Ignore command. thing={}, haId={}", getThingLabel(), getThingHaId());
            return false;
        }

        if (isThingOffline()) {
            logger.debug("{} is OFFLINE. Ignore command. haId={}", getThing().getLabel(), getThingHaId());
            return false;
        }

        return true;
    }

    /**
     * Checks if bridge is online and set.
     *
     * @return true if online
     */
    protected boolean isBridgeOnline() {
        Bridge bridge = getBridge();
        return bridge != null && ONLINE.equals(bridge.getStatus());
    }

    /**
     * Checks if bridge is offline or not set.
     *
     * @return true if offline
     */
    protected boolean isBridgeOffline() {
        return !isBridgeOnline();
    }

    /**
     * Checks if thing is online.
     *
     * @return true if online
     */
    protected boolean isThingOnline() {
        return ONLINE.equals(getThing().getStatus());
    }

    /**
     * Checks if thing is connected to the cloud and accessible via SSE.
     *
     * @return true if yes
     */
    public boolean isThingAccessibleViaServerSentEvents() {
        return accessible.get();
    }

    /**
     * Checks if thing is offline.
     *
     * @return true if offline
     */
    protected boolean isThingOffline() {
        return !isThingOnline();
    }

    /**
     * Get {@link HomeConnectBridgeHandler}.
     *
     * @return bridge handler
     */
    protected Optional<HomeConnectBridgeHandler> getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof HomeConnectBridgeHandler) {
                return Optional.of((HomeConnectBridgeHandler) bridgeHandler);
            }
        }
        return Optional.empty();
    }

    /**
     * Get thing channel by given channel id.
     *
     * @param channelId channel id
     * @return channel
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
    protected abstract void configureChannelUpdateHandlers(final Map<String, ChannelUpdateHandler> handlers);

    /**
     * Configure event handlers. Classes which extend {@link AbstractHomeConnectThingHandler} must implement
     * this class and add handlers.
     *
     * @param handlers Server-Sent-Event handlers
     */
    protected abstract void configureEventHandlers(final Map<String, EventHandler> handlers);

    /**
     * Update all channels via API.
     *
     */
    protected void updateChannels() {
        if (isBridgeOffline()) {
            logger.debug("Bridge handler not found or offline. Stopping update of channels. thing={}, haId={}",
                    getThingLabel(), getThingHaId());
        } else if (isThingOffline()) {
            logger.debug("{} offline. Stopping update of channels. haId={}", getThing().getLabel(), getThingHaId());
        } else {
            List<Channel> channels = getThing().getChannels();
            for (Channel channel : channels) {
                updateChannel(channel.getUID());
            }
        }
    }

    /**
     * Update Channel values via API.
     *
     * @param channelUID channel UID
     */
    protected void updateChannel(ChannelUID channelUID) {
        if (!getApiClient().isPresent()) {
            logger.error("Cannot update channel. No instance of api client found! thing={}, haId={}", getThingLabel(),
                    getThingHaId());
            return;
        }

        if (!isThingReadyToHandleCommand()) {
            return;
        }

        if ((isLinked(channelUID) || CHANNEL_OPERATION_STATE.equals(channelUID.getId())) // always update operation
                // state channel
                && channelUpdateHandlers.containsKey(channelUID.getId())) {
            try {
                ChannelUpdateHandler channelUpdateHandler = channelUpdateHandlers.get(channelUID.getId());
                if (channelUpdateHandler != null) {
                    channelUpdateHandler.handle(channelUID, expiringStateMap);
                }
            } catch (ApplianceOfflineException e) {
                logger.debug(
                        "API communication problem while trying to update! Appliance offline. thing={}, haId={}, error={}",
                        getThingLabel(), getThingHaId(), e.getMessage());
                updateStatus(OFFLINE);
                resetChannelsOnOfflineEvent();
                resetProgramStateChannels(true);
            } catch (CommunicationException e) {
                logger.debug("API communication problem while trying to update! thing={}, haId={}, error={}",
                        getThingLabel(), getThingHaId(), e.getMessage());
            } catch (AuthorizationException e) {
                logger.debug("Authentication problem while trying to update! thing={}, haId={}", getThingLabel(),
                        getThingHaId(), e);
                handleAuthenticationError(e);
            }
        }
    }

    /**
     * Reset program related channels.
     *
     * @param offline true if the device is considered as OFFLINE
     */
    protected void resetProgramStateChannels(boolean offline) {
        logger.debug("Resetting active program channel states. thing={}, haId={}", getThingLabel(), getThingHaId());
    }

    /**
     * Reset all channels on OFFLINE event.
     */
    protected void resetChannelsOnOfflineEvent() {
        logger.debug("Resetting channel states due to OFFLINE event. thing={}, haId={}", getThingLabel(),
                getThingHaId());
        getThingChannel(CHANNEL_POWER_STATE).ifPresent(channel -> updateState(channel.getUID(), OnOffType.OFF));
        getThingChannel(CHANNEL_OPERATION_STATE).ifPresent(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_DOOR_STATE).ifPresent(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE)
                .ifPresent(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE)
                .ifPresent(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_REMOTE_START_ALLOWANCE_STATE)
                .ifPresent(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE)
                .ifPresent(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
    }

    /**
     * Map Home Connect key and value names to label.
     * e.g. Dishcare.Dishwasher.Program.Eco50 --> Eco50 or BSH.Common.EnumType.OperationState.DelayedStart --> Delayed
     * Start
     *
     * @param type type
     * @return human readable label
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
     * Map Home Connect stage value to label.
     * e.g. Cooking.Hood.EnumType.IntensiveStage.IntensiveStage1 --> 1
     *
     * @param stage stage
     * @return human readable label
     */
    protected String mapStageStringType(String stage) {
        switch (stage) {
            case STAGE_FAN_OFF:
            case STAGE_INTENSIVE_STAGE_OFF:
                stage = "Off";
                break;
            case STAGE_FAN_STAGE_01:
            case STAGE_INTENSIVE_STAGE_1:
                stage = "1";
                break;
            case STAGE_FAN_STAGE_02:
            case STAGE_INTENSIVE_STAGE_2:
                stage = "2";
                break;
            case STAGE_FAN_STAGE_03:
                stage = "3";
                break;
            case STAGE_FAN_STAGE_04:
                stage = "4";
                break;
            case STAGE_FAN_STAGE_05:
                stage = "5";
                break;
            default:
                stage = mapStringType(stage);
        }

        return stage;
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
     * Map hex representation of color to HSB type.
     *
     * @param colorCode color code e.g. #001122
     * @return HSB type
     */
    protected HSBType mapColor(String colorCode) {
        HSBType color = HSBType.WHITE;

        if (colorCode.length() == 7) {
            int r = Integer.valueOf(colorCode.substring(1, 3), 16);
            int g = Integer.valueOf(colorCode.substring(3, 5), 16);
            int b = Integer.valueOf(colorCode.substring(5, 7), 16);
            color = HSBType.fromRGB(r, g, b);
        }
        return color;
    }

    /**
     * Map HSB color type to hex representation.
     *
     * @param color HSB color
     * @return color code e.g. #001122
     */
    protected String mapColor(HSBType color) {
        String redValue = String.format("%02X", (int) (color.getRed().floatValue() * 2.55));
        String greenValue = String.format("%02X", (int) (color.getGreen().floatValue() * 2.55));
        String blueValue = String.format("%02X", (int) (color.getBlue().floatValue() * 2.55));
        return "#" + redValue + greenValue + blueValue;
    }

    /**
     * Check bridge status and refresh connection status of thing accordingly.
     */
    protected void refreshThingStatus() {
        Optional<HomeConnectApiClient> apiClient = getApiClient();

        apiClient.ifPresent(client -> {
            try {
                HomeAppliance homeAppliance = client.getHomeAppliance(getThingHaId());
                if (!homeAppliance.isConnected()) {
                    updateStatus(OFFLINE);
                } else {
                    updateStatus(ONLINE);
                }
                accessible.set(true);
            } catch (CommunicationException e) {
                logger.debug(
                        "Update status to OFFLINE. Home Connect service is not reachable or a problem occurred!  thing={}, haId={}, error={}.",
                        getThingLabel(), getThingHaId(), e.getMessage());
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Home Connect service is not reachable or a problem occurred! (" + e.getMessage() + ").");
                accessible.set(false);
            } catch (AuthorizationException e) {
                logger.debug(
                        "Update status to OFFLINE. Home Connect service is not reachable or a problem occurred!  thing={}, haId={}, error={}",
                        getThingLabel(), getThingHaId(), e.getMessage());
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Home Connect service is not reachable or a problem occurred! (" + e.getMessage() + ").");
                accessible.set(false);
                handleAuthenticationError(e);
            }
        });
        if (apiClient.isEmpty()) {
            updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            accessible.set(false);
        }
    }

    /**
     * Get home appliance id of Thing.
     *
     * @return home appliance id
     */
    public String getThingHaId() {
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
        if (isBridgeOnline()) {
            logger.debug(
                    "Thing handler threw authentication exception --> clear credential storage thing={}, haId={} error={}",
                    getThingLabel(), getThingHaId(), exception.getMessage());

            getBridgeHandler().ifPresent(homeConnectBridgeHandler -> {
                try {
                    homeConnectBridgeHandler.getOAuthClientService().remove();
                    homeConnectBridgeHandler.reinitialize();
                } catch (OAuthException e) {
                    // client is already closed --> we can ignore it
                }
            });
        }
    }

    /**
     * Get operation state of device.
     *
     * @return operation state string
     */
    protected @Nullable String getOperationState() {
        return operationState;
    }

    protected EventHandler defaultElapsedProgramTimeEventHandler() {
        return event -> getThingChannel(CHANNEL_ELAPSED_PROGRAM_TIME)
                .ifPresent(channel -> updateState(channel.getUID(), new QuantityType<>(event.getValueAsInt(), SECOND)));
    }

    protected EventHandler defaultPowerStateEventHandler() {
        return event -> {
            getThingChannel(CHANNEL_POWER_STATE).ifPresent(
                    channel -> updateState(channel.getUID(), OnOffType.from(STATE_POWER_ON.equals(event.getValue()))));

            if (STATE_POWER_ON.equals(event.getValue())) {
                getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE).ifPresent(c -> updateChannel(c.getUID()));
            } else {
                resetProgramStateChannels(true);
                getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE)
                        .ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            }
        };
    }

    protected EventHandler defaultDoorStateEventHandler() {
        return event -> getThingChannel(CHANNEL_DOOR_STATE).ifPresent(channel -> updateState(channel.getUID(),
                STATE_DOOR_OPEN.equals(event.getValue()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED));
    }

    protected EventHandler defaultOperationStateEventHandler() {
        return event -> {
            String value = event.getValue();
            getThingChannel(CHANNEL_OPERATION_STATE).ifPresent(channel -> updateState(channel.getUID(),
                    value == null ? UnDefType.UNDEF : new StringType(mapStringType(value))));

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
                resetProgramStateChannels(false);
            }
        };
    }

    protected EventHandler defaultActiveProgramEventHandler() {
        return event -> {
            String value = event.getValue();
            getThingChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(channel -> updateState(channel.getUID(),
                    value == null ? UnDefType.UNDEF : new StringType(mapStringType(value))));
            if (event.getValue() == null) {
                resetProgramStateChannels(false);
            }
        };
    }

    protected EventHandler defaultEventPresentStateEventHandler(String channelId) {
        return event -> getThingChannel(channelId).ifPresent(channel -> updateState(channel.getUID(),
                OnOffType.from(!STATE_EVENT_PRESENT_STATE_OFF.equals(event.getValue()))));
    }

    protected EventHandler defaultBooleanEventHandler(String channelId) {
        return event -> getThingChannel(channelId)
                .ifPresent(channel -> updateState(channel.getUID(), OnOffType.from(event.getValueAsBoolean())));
    }

    protected EventHandler defaultRemainingProgramTimeEventHandler() {
        return event -> getThingChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE)
                .ifPresent(channel -> updateState(channel.getUID(), new QuantityType<>(event.getValueAsInt(), SECOND)));
    }

    protected EventHandler defaultSelectedProgramStateEventHandler() {
        return event -> getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE)
                .ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue())));
    }

    protected EventHandler defaultAmbientLightColorStateEventHandler() {
        return event -> getThingChannel(CHANNEL_AMBIENT_LIGHT_COLOR_STATE)
                .ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue())));
    }

    protected EventHandler defaultAmbientLightCustomColorStateEventHandler() {
        return event -> getThingChannel(CHANNEL_AMBIENT_LIGHT_CUSTOM_COLOR_STATE).ifPresent(channel -> {
            String value = event.getValue();
            if (value != null) {
                updateState(channel.getUID(), mapColor(value));
            } else {
                updateState(channel.getUID(), UnDefType.UNDEF);
            }
        });
    }

    protected EventHandler updateRemoteControlActiveAndProgramOptionsStateEventHandler() {
        return event -> {
            defaultBooleanEventHandler(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE).handle(event);

            // update available program options if update was previously delayed and remote control is enabled
            try {
                String programKey = programOptionsDelayedUpdate;
                if (programKey != null && Boolean.parseBoolean(event.getValue())) {
                    logger.debug("Delayed update of options for program {}", programKey);
                    updateProgramOptionsStateDescriptions(programKey);
                    programOptionsDelayedUpdate = null;
                }
            } catch (CommunicationException | ApplianceOfflineException | AuthorizationException e) {
                logger.debug("Could not update program options. {}", e.getMessage());
            }
        };
    }

    protected EventHandler updateProgramOptionsAndSelectedProgramStateEventHandler() {
        return event -> {
            defaultSelectedProgramStateEventHandler().handle(event);

            // update available program options
            try {
                Optional<HomeConnectApiClient> apiClient = getApiClient();
                String programKey = event.getValue();
                if (apiClient.isPresent() && programKey != null) {
                    // Delay the update if options are not yet cached and remote control is disabled
                    if (availableProgramOptionsCache.get(programKey) == null
                            && !apiClient.get().isRemoteControlActive(getThingHaId())) {
                        logger.debug("Delay update of options for program {}", programKey);
                        programOptionsDelayedUpdate = programKey;
                    } else {
                        updateProgramOptionsStateDescriptions(programKey);
                    }
                }
            } catch (CommunicationException | ApplianceOfflineException | AuthorizationException e) {
                logger.debug("Could not update program options. {}", e.getMessage());
            }
        };
    }

    protected EventHandler defaultPercentQuantityTypeEventHandler(String channelId) {
        return event -> getThingChannel(channelId).ifPresent(
                channel -> updateState(channel.getUID(), new QuantityType<>(event.getValueAsInt(), PERCENT)));
    }

    protected EventHandler defaultPercentHandler(String channelId) {
        return event -> getThingChannel(channelId)
                .ifPresent(channel -> updateState(channel.getUID(), new PercentType(event.getValueAsInt())));
    }

    protected ChannelUpdateHandler defaultDoorStateChannelUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                Data data = apiClient.get().getDoorState(getThingHaId());
                if (data.getValue() != null) {
                    return STATE_DOOR_OPEN.equals(data.getValue()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                } else {
                    return UnDefType.UNDEF;
                }
            } else {
                return UnDefType.UNDEF;
            }
        }));
    }

    protected ChannelUpdateHandler defaultPowerStateChannelUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                Data data = apiClient.get().getPowerState(getThingHaId());
                if (data.getValue() != null) {
                    return OnOffType.from(STATE_POWER_ON.equals(data.getValue()));
                } else {
                    return UnDefType.UNDEF;
                }
            } else {
                return UnDefType.UNDEF;
            }
        }));
    }

    protected ChannelUpdateHandler defaultAmbientLightChannelUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                Data data = apiClient.get().getAmbientLightState(getThingHaId());
                if (data.getValue() != null) {
                    boolean enabled = data.getValueAsBoolean();
                    if (enabled) {
                        // brightness
                        Data brightnessData = apiClient.get().getAmbientLightBrightnessState(getThingHaId());
                        getThingChannel(CHANNEL_AMBIENT_LIGHT_BRIGHTNESS_STATE)
                                .ifPresent(channel -> updateState(channel.getUID(),
                                        new PercentType(brightnessData.getValueAsInt())));

                        // color
                        Data colorData = apiClient.get().getAmbientLightColorState(getThingHaId());
                        getThingChannel(CHANNEL_AMBIENT_LIGHT_COLOR_STATE).ifPresent(
                                channel -> updateState(channel.getUID(), new StringType(colorData.getValue())));

                        // custom color
                        Data customColorData = apiClient.get().getAmbientLightCustomColorState(getThingHaId());
                        getThingChannel(CHANNEL_AMBIENT_LIGHT_CUSTOM_COLOR_STATE).ifPresent(channel -> {
                            String value = customColorData.getValue();
                            if (value != null) {
                                updateState(channel.getUID(), mapColor(value));
                            } else {
                                updateState(channel.getUID(), UnDefType.UNDEF);
                            }
                        });

                    }
                    return OnOffType.from(enabled);
                } else {
                    return UnDefType.UNDEF;
                }
            } else {
                return UnDefType.UNDEF;
            }
        }));
    }

    protected ChannelUpdateHandler defaultNoOpUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, UnDefType.UNDEF);
    }

    protected ChannelUpdateHandler defaultOperationStateChannelUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                Data data = apiClient.get().getOperationState(getThingHaId());

                String value = data.getValue();
                if (value != null) {
                    operationState = data.getValue();
                    return new StringType(mapStringType(value));
                } else {
                    operationState = null;
                    return UnDefType.UNDEF;
                }
            } else {
                return UnDefType.UNDEF;
            }
        }));
    }

    protected ChannelUpdateHandler defaultRemoteControlActiveStateChannelUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                return OnOffType.from(apiClient.get().isRemoteControlActive(getThingHaId()));
            }
            return OnOffType.OFF;
        }));
    }

    protected ChannelUpdateHandler defaultLocalControlActiveStateChannelUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                return OnOffType.from(apiClient.get().isLocalControlActive(getThingHaId()));
            }
            return OnOffType.OFF;
        }));
    }

    protected ChannelUpdateHandler defaultRemoteStartAllowanceChannelUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                return OnOffType.from(apiClient.get().isRemoteControlStartAllowed(getThingHaId()));
            }
            return OnOffType.OFF;
        }));
    }

    protected ChannelUpdateHandler defaultSelectedProgramStateUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                Program program = apiClient.get().getSelectedProgram(getThingHaId());
                if (program != null) {
                    processProgramOptions(program.getOptions());
                    return new StringType(program.getKey());
                } else {
                    return UnDefType.UNDEF;
                }
            }
            return UnDefType.UNDEF;
        }));
    }

    protected ChannelUpdateHandler updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                Program program = apiClient.get().getSelectedProgram(getThingHaId());

                if (program != null) {
                    updateProgramOptionsStateDescriptions(program.getKey());
                    processProgramOptions(program.getOptions());

                    return new StringType(program.getKey());
                } else {
                    return UnDefType.UNDEF;
                }
            }
            return UnDefType.UNDEF;
        }));
    }

    protected ChannelUpdateHandler getAndUpdateSelectedProgramStateUpdateHandler() {
        return (channelUID, cache) -> {
            Optional<Channel> channel = getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE);
            if (channel.isPresent()) {
                defaultSelectedProgramStateUpdateHandler().handle(channel.get().getUID(), cache);
            }
        };
    }

    protected ChannelUpdateHandler getAndUpdateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler() {
        return (channelUID, cache) -> {
            Optional<Channel> channel = getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE);
            if (channel.isPresent()) {
                updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler()
                        .handle(channel.get().getUID(), cache);
            }
        };
    }

    protected ChannelUpdateHandler defaultActiveProgramStateUpdateHandler() {
        return (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
            Optional<HomeConnectApiClient> apiClient = getApiClient();
            if (apiClient.isPresent()) {
                Program program = apiClient.get().getActiveProgram(getThingHaId());

                if (program != null) {
                    processProgramOptions(program.getOptions());
                    return new StringType(mapStringType(program.getKey()));
                } else {
                    resetProgramStateChannels(false);
                    return UnDefType.UNDEF;
                }
            }
            return UnDefType.UNDEF;
        }));
    }

    protected void handleTemperatureCommand(final ChannelUID channelUID, final Command command,
            final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        if (command instanceof QuantityType) {
            QuantityType<?> quantity = (QuantityType<?>) command;

            String value;
            String unit;

            try {
                if (quantity.getUnit().equals(SIUnits.CELSIUS) || quantity.getUnit().equals(ImperialUnits.FAHRENHEIT)) {
                    unit = quantity.getUnit().toString();
                    value = String.valueOf(quantity.intValue());
                } else {
                    logger.debug("Converting target temperature from {}{} to °C value. thing={}, haId={}",
                            quantity.intValue(), quantity.getUnit().toString(), getThingLabel(), getThingHaId());
                    unit = "°C";
                    var celsius = quantity.toUnit(SIUnits.CELSIUS);
                    if (celsius == null) {
                        logger.warn("Converting temperature to celsius failed! quantity={}", quantity);
                        value = null;
                    } else {
                        value = String.valueOf(celsius.intValue());
                    }
                    logger.debug("Converted value {}{}", value, unit);
                }

                if (value != null) {
                    logger.debug("Set temperature to {} {}. thing={}, haId={}", value, unit, getThingLabel(),
                            getThingHaId());
                    switch (channelUID.getId()) {
                        case CHANNEL_REFRIGERATOR_SETPOINT_TEMPERATURE:
                            apiClient.setFridgeSetpointTemperature(getThingHaId(), value, unit);
                        case CHANNEL_FREEZER_SETPOINT_TEMPERATURE:
                            apiClient.setFreezerSetpointTemperature(getThingHaId(), value, unit);
                            break;
                        case CHANNEL_SETPOINT_TEMPERATURE:
                            apiClient.setProgramOptions(getThingHaId(), OPTION_SETPOINT_TEMPERATURE, value, unit, true,
                                    false);
                            break;
                        default:
                            logger.debug("Unknown channel! Cannot set temperature. channelUID={}", channelUID);
                    }
                }
            } catch (UnconvertibleException e) {
                logger.warn("Could not set temperature! haId={}, error={}", getThingHaId(), e.getMessage());
            }
        }
    }

    protected void handleLightCommands(final ChannelUID channelUID, final Command command,
            final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        switch (channelUID.getId()) {
            case CHANNEL_FUNCTIONAL_LIGHT_BRIGHTNESS_STATE:
            case CHANNEL_AMBIENT_LIGHT_BRIGHTNESS_STATE:
                // turn light on if turned off
                turnLightOn(channelUID, apiClient);

                int newBrightness = BRIGHTNESS_MIN;
                if (command instanceof OnOffType) {
                    newBrightness = command == OnOffType.ON ? BRIGHTNESS_MAX : BRIGHTNESS_MIN;
                } else if (command instanceof IncreaseDecreaseType) {
                    int currentBrightness = getCurrentBrightness(channelUID, apiClient);
                    if (command.equals(IncreaseDecreaseType.INCREASE)) {
                        newBrightness = currentBrightness + BRIGHTNESS_DIM_STEP;
                    } else {
                        newBrightness = currentBrightness - BRIGHTNESS_DIM_STEP;
                    }
                } else if (command instanceof PercentType) {
                    newBrightness = (int) Math.floor(((PercentType) command).doubleValue());
                } else if (command instanceof DecimalType) {
                    newBrightness = ((DecimalType) command).intValue();
                }

                // check in in range
                newBrightness = Math.min(Math.max(newBrightness, BRIGHTNESS_MIN), BRIGHTNESS_MAX);

                setLightBrightness(channelUID, apiClient, newBrightness);
                break;
            case CHANNEL_FUNCTIONAL_LIGHT_STATE:
                if (command instanceof OnOffType) {
                    apiClient.setFunctionalLightState(getThingHaId(), OnOffType.ON.equals(command));
                }
                break;
            case CHANNEL_AMBIENT_LIGHT_STATE:
                if (command instanceof OnOffType) {
                    apiClient.setAmbientLightState(getThingHaId(), OnOffType.ON.equals(command));
                }
                break;
            case CHANNEL_AMBIENT_LIGHT_COLOR_STATE:
                if (command instanceof StringType) {
                    turnLightOn(channelUID, apiClient);
                    apiClient.setAmbientLightColorState(getThingHaId(), command.toFullString());
                }
                break;
            case CHANNEL_AMBIENT_LIGHT_CUSTOM_COLOR_STATE:
                turnLightOn(channelUID, apiClient);

                // make sure 'custom color' is set as color
                Data ambientLightColorState = apiClient.getAmbientLightColorState(getThingHaId());
                if (!STATE_AMBIENT_LIGHT_COLOR_CUSTOM_COLOR.equals(ambientLightColorState.getValue())) {
                    apiClient.setAmbientLightColorState(getThingHaId(), STATE_AMBIENT_LIGHT_COLOR_CUSTOM_COLOR);
                }

                if (command instanceof HSBType) {
                    apiClient.setAmbientLightCustomColorState(getThingHaId(), mapColor((HSBType) command));
                } else if (command instanceof StringType) {
                    apiClient.setAmbientLightCustomColorState(getThingHaId(), command.toFullString());
                }
                break;
        }
    }

    protected void handlePowerCommand(final ChannelUID channelUID, final Command command,
            final HomeConnectApiClient apiClient, String stateNotOn)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        if (command instanceof OnOffType && CHANNEL_POWER_STATE.equals(channelUID.getId())) {
            apiClient.setPowerState(getThingHaId(), OnOffType.ON.equals(command) ? STATE_POWER_ON : stateNotOn);
        }
    }

    private int getCurrentBrightness(final ChannelUID channelUID, final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        String id = channelUID.getId();
        if (CHANNEL_FUNCTIONAL_LIGHT_BRIGHTNESS_STATE.equals(id)) {
            return apiClient.getFunctionalLightBrightnessState(getThingHaId()).getValueAsInt();
        } else {
            return apiClient.getAmbientLightBrightnessState(getThingHaId()).getValueAsInt();
        }
    }

    private void setLightBrightness(final ChannelUID channelUID, final HomeConnectApiClient apiClient, int value)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        switch (channelUID.getId()) {
            case CHANNEL_FUNCTIONAL_LIGHT_BRIGHTNESS_STATE:
                apiClient.setFunctionalLightBrightnessState(getThingHaId(), value);
                break;
            case CHANNEL_AMBIENT_LIGHT_BRIGHTNESS_STATE:
                apiClient.setAmbientLightBrightnessState(getThingHaId(), value);
                break;
        }
    }

    private void turnLightOn(final ChannelUID channelUID, final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        switch (channelUID.getId()) {
            case CHANNEL_FUNCTIONAL_LIGHT_BRIGHTNESS_STATE:
                Data functionalLightState = apiClient.getFunctionalLightState(getThingHaId());
                if (!functionalLightState.getValueAsBoolean()) {
                    apiClient.setFunctionalLightState(getThingHaId(), true);
                }
                break;
            case CHANNEL_AMBIENT_LIGHT_CUSTOM_COLOR_STATE:
            case CHANNEL_AMBIENT_LIGHT_COLOR_STATE:
            case CHANNEL_AMBIENT_LIGHT_BRIGHTNESS_STATE:
                Data ambientLightState = apiClient.getAmbientLightState(getThingHaId());
                if (!ambientLightState.getValueAsBoolean()) {
                    apiClient.setAmbientLightState(getThingHaId(), true);
                }
                break;
        }
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
                        String hoodIntensiveLevelValue = option.getValue();
                        if (hoodIntensiveLevelValue != null) {
                            getThingChannel(CHANNEL_HOOD_INTENSIVE_LEVEL)
                                    .ifPresent(channel -> updateState(channel.getUID(),
                                            new StringType(mapStageStringType(hoodIntensiveLevelValue))));
                        }
                        break;
                    case OPTION_HOOD_VENTING_LEVEL:
                        String hoodVentingLevel = option.getValue();
                        if (hoodVentingLevel != null) {
                            getThingChannel(CHANNEL_HOOD_VENTING_LEVEL)
                                    .ifPresent(channel -> updateState(channel.getUID(),
                                            new StringType(mapStageStringType(hoodVentingLevel))));
                        }
                        break;
                    case OPTION_SETPOINT_TEMPERATURE:
                        getThingChannel(CHANNEL_SETPOINT_TEMPERATURE).ifPresent(channel -> updateState(channel.getUID(),
                                new QuantityType<>(option.getValueAsInt(), mapTemperature(option.getUnit()))));
                        break;
                    case OPTION_DURATION:
                        getThingChannel(CHANNEL_DURATION).ifPresent(channel -> updateState(channel.getUID(),
                                new QuantityType<>(option.getValueAsInt(), SECOND)));
                        break;
                    case OPTION_FINISH_IN_RELATIVE:
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

    protected void updateProgramOptionsStateDescriptions(String programKey)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Optional<HomeConnectApiClient> apiClient = getApiClient();
        if (apiClient.isPresent()) {
            List<AvailableProgramOption> availableProgramOptions;
            if (availableProgramOptionsCache.containsKey(programKey)) {
                logger.debug("Returning cached options for '{}'.", programKey);
                availableProgramOptions = availableProgramOptionsCache.get(programKey);
                availableProgramOptions = availableProgramOptions != null ? availableProgramOptions
                        : Collections.emptyList();
            } else {
                availableProgramOptions = apiClient.get().getProgramOptions(getThingHaId(), programKey);
                availableProgramOptionsCache.put(programKey, availableProgramOptions);
            }

            Optional<Channel> channelSpinSpeed = getThingChannel(CHANNEL_WASHER_SPIN_SPEED);
            Optional<Channel> channelTemperature = getThingChannel(CHANNEL_WASHER_TEMPERATURE);
            Optional<Channel> channelDryingTarget = getThingChannel(CHANNEL_DRYER_DRYING_TARGET);

            if (availableProgramOptions.isEmpty()) {
                channelSpinSpeed.ifPresent(
                        channel -> dynamicStateDescriptionProvider.setStateOptions(channel.getUID(), emptyList()));
                channelTemperature.ifPresent(
                        channel -> dynamicStateDescriptionProvider.setStateOptions(channel.getUID(), emptyList()));
                channelDryingTarget.ifPresent(
                        channel -> dynamicStateDescriptionProvider.setStateOptions(channel.getUID(), emptyList()));
            }

            availableProgramOptions.forEach(option -> {
                switch (option.getKey()) {
                    case OPTION_WASHER_SPIN_SPEED: {
                        channelSpinSpeed
                                .ifPresent(channel -> dynamicStateDescriptionProvider.setStateOptions(channel.getUID(),
                                        createStateOptions(option, this::convertWasherSpinSpeed)));
                        break;
                    }
                    case OPTION_WASHER_TEMPERATURE: {
                        channelTemperature
                                .ifPresent(channel -> dynamicStateDescriptionProvider.setStateOptions(channel.getUID(),
                                        createStateOptions(option, this::convertWasherTemperature)));
                        break;
                    }
                    case OPTION_DRYER_DRYING_TARGET: {
                        channelDryingTarget.ifPresent(channel -> dynamicStateDescriptionProvider
                                .setStateOptions(channel.getUID(), createStateOptions(option, this::mapStringType)));
                        break;
                    }
                }
            });
        }
    }

    protected HomeConnectDynamicStateDescriptionProvider getDynamicStateDescriptionProvider() {
        return dynamicStateDescriptionProvider;
    }

    private List<StateOption> createStateOptions(AvailableProgramOption option,
            Function<String, String> stateConverter) {
        return option.getAllowedValues().stream().map(av -> new StateOption(av, stateConverter.apply(av)))
                .collect(Collectors.toList());
    }

    private synchronized void scheduleOfflineMonitor1() {
        this.reinitializationFuture1 = scheduler.schedule(() -> {
            if (isBridgeOnline() && isThingOffline()) {
                logger.debug("Offline monitor 1: Check if thing is ONLINE. thing={}, haId={}", getThingLabel(),
                        getThingHaId());
                refreshThingStatus();
                if (isThingOnline()) {
                    logger.debug("Offline monitor 1: Thing status changed to ONLINE. thing={}, haId={}",
                            getThingLabel(), getThingHaId());
                    reinitialize();
                } else {
                    scheduleOfflineMonitor1();
                }
            } else {
                scheduleOfflineMonitor1();
            }
        }, AbstractHomeConnectThingHandler.OFFLINE_MONITOR_1_DELAY_MIN, TimeUnit.MINUTES);
    }

    private synchronized void stopOfflineMonitor1() {
        ScheduledFuture<?> reinitializationFuture = this.reinitializationFuture1;
        if (reinitializationFuture != null) {
            reinitializationFuture.cancel(false);
            this.reinitializationFuture1 = null;
        }
    }

    private synchronized void scheduleOfflineMonitor2() {
        this.reinitializationFuture2 = scheduler.schedule(() -> {
            if (isBridgeOnline() && !accessible.get()) {
                logger.debug("Offline monitor 2: Check if thing is ONLINE. thing={}, haId={}", getThingLabel(),
                        getThingHaId());
                refreshThingStatus();
                if (isThingOnline()) {
                    logger.debug("Offline monitor 2: Thing status changed to ONLINE. thing={}, haId={}",
                            getThingLabel(), getThingHaId());
                    reinitialize();
                } else {
                    scheduleOfflineMonitor2();
                }
            } else {
                scheduleOfflineMonitor2();
            }
        }, AbstractHomeConnectThingHandler.OFFLINE_MONITOR_2_DELAY_MIN, TimeUnit.MINUTES);
    }

    private synchronized void stopOfflineMonitor2() {
        ScheduledFuture<?> reinitializationFuture = this.reinitializationFuture2;
        if (reinitializationFuture != null) {
            reinitializationFuture.cancel(false);
            this.reinitializationFuture2 = null;
        }
    }

    private synchronized void scheduleRetryRegistering() {
        this.reinitializationFuture3 = scheduler.schedule(() -> {
            logger.debug("Try to register event listener again. haId={}", getThingHaId());
            unregisterEventListener();
            registerEventListener();
        }, AbstractHomeConnectThingHandler.EVENT_LISTENER_CONNECT_RETRY_DELAY_MIN, TimeUnit.MINUTES);
    }

    private synchronized void stopRetryRegistering() {
        ScheduledFuture<?> reinitializationFuture = this.reinitializationFuture3;
        if (reinitializationFuture != null) {
            reinitializationFuture.cancel(true);
            this.reinitializationFuture3 = null;
        }
    }
}
