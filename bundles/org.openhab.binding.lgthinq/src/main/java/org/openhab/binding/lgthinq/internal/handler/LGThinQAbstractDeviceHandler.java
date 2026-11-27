/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.internal.handler;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqAccessException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiExhaustionException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqDeviceV1MonitorExpiredException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqDeviceV1OfflineException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDataType;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.openhab.binding.lgthinq.lgservices.model.SnapshotDefinition;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.CourseDefinition;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.items.Item;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;

/**
 * The {@link LGThinQAbstractDeviceHandler} is a main interface contract for all LG Thinq things
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class LGThinQAbstractDeviceHandler<@NonNull C extends CapabilityDefinition, @NonNull S extends SnapshotDefinition>
        extends BaseThingWithExtraInfoHandler {
    private static final Set<ThingStatusDetail> BRIDGE_STATUS_DETAIL_ERROR = Set.of(ThingStatusDetail.BRIDGE_OFFLINE,
            ThingStatusDetail.BRIDGE_UNINITIALIZED, ThingStatusDetail.COMMUNICATION_ERROR,
            ThingStatusDetail.CONFIGURATION_ERROR);
    protected final String lgPlatformType;
    protected final ItemChannelLinkRegistry itemChannelLinkRegistry;
    protected final LinkedBlockingQueue<AsyncCommandParams> commandBlockQueue = new LinkedBlockingQueue<>(30);
    protected final LGThinQStateDescriptionProvider stateDescriptionProvider;
    protected @Nullable LGThinQBridgeHandler account;
    @Nullable
    protected C thinQCapability;
    private volatile S lastShot;
    /**
     * Requests to LG block (a V1 monitor waits up to 5 seconds for data), so polling and commands run on a pool of the
     * binding instead of the scheduler shared by all thing handlers.
     */
    private final ScheduledExecutorService deviceScheduler = ThreadPoolManager.getScheduledPool(BINDING_ID);
    /**
     * Commands are executed by a task that drains {@link #commandBlockQueue} and ends when the queue is empty, so idle
     * devices don't hold a thread. At most one drain task exists per device, from its submission until it exits, which
     * keeps the commands of a device in order and one at a time. The fields below are guarded by
     * {@link #commandQueueLock}.
     */
    private final Object commandQueueLock = new Object();
    private @Nullable Future<?> commandDrainJob;
    private boolean commandDrainRunning;
    private boolean commandQueueEnabled;
    private @Nullable ScheduledFuture<?> thingStatePollingJob;
    private @Nullable ScheduledFuture<?> extraInfoCollectorPollingJob;
    /**
     * Defined in the configurations of the thing.
     */
    private int pollingPeriodOnSeconds = 10;
    private int pollingPeriodOffSeconds = 10;
    private int currentPeriodSeconds = 10;
    private int pollingExtraInfoPeriodSeconds = 60;
    private boolean pollExtraInfoOnPowerOff = false;

    private enum MonitorPhase {
        IDLE,
        IN_PROGRESS
    }

    private volatile MonitorPhase monitorPhase = MonitorPhase.IDLE;
    private boolean isThingReconfigured = false;
    private volatile String monitorWorkId = "";
    private String bridgeId = "";
    private ThingStatus lastThingStatus = ThingStatus.UNKNOWN;

    public LGThinQAbstractDeviceHandler(Thing thing, LGThinQStateDescriptionProvider stateDescriptionProvider,
            ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing);
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.stateDescriptionProvider = stateDescriptionProvider;
        normalizeConfigurationsAndProperties();
        lgPlatformType = String.valueOf(thing.getProperties().get(PROP_INFO_PLATFORM_TYPE));

        Class<S> snapshotClass = getSnapshotClass();
        try {
            Constructor<S> constructor = snapshotClass.getDeclaredConstructor();
            this.lastShot = Objects.requireNonNull(constructor.newInstance(),
                    "Unexpected null returned from newInstance()");
        } catch (Exception e) {
            throw new IllegalArgumentException("Snapshot class can't be instantiated. It is most likely a bug", e);
        }
    }

    protected S getLastShot() {
        return Objects.requireNonNull(lastShot, "LastShot shouldn't be null. It is most likely a bug.");
    }

    @SuppressWarnings("unchecked")
    public Class<S> getSnapshotClass() {
        ParameterizedType genSupClass = (ParameterizedType) getClass().getGenericSuperclass();
        if (genSupClass == null) {
            throw new IllegalStateException("Snapshot class has no parameterized type. It is most likely a bug!");
        }
        return (Class<S>) genSupClass.getActualTypeArguments()[1];
    }

    private void normalizeConfigurationsAndProperties() {
        List.of(PROP_INFO_PLATFORM_TYPE, PROP_INFO_MODEL_URL_INFO, PROP_INFO_DEVICE_ID).forEach(p -> {
            if (!thing.getProperties().containsKey(p)) {
                thing.setProperty(p, (String) thing.getConfiguration().get(p));
            }
        });
    }

    /**
     * Returns the simple channel UID name, i.e., without group.
     *
     * @param uid Full UID name
     * @return simple channel UID name, i.e., without group.
     */
    protected String getSimpleChannelUID(String uid) {
        String simpleChannelUID;
        if (uid.indexOf("#") > 0) {
            // I have to remove the channelGroup from de channelUID
            simpleChannelUID = uid.split("#")[1];
        } else {
            simpleChannelUID = uid;
        }
        return simpleChannelUID;
    }

    /**
     * Return empty string if null argument is passed
     *
     * @param value value to test
     * @return empty string if null argument is passed
     */
    protected final String emptyIfNull(@Nullable String value) {
        return Objects.requireNonNullElse(value, "");
    }

    /**
     * Return the key informed if there is no correpondent value in map for that key.
     *
     * @param map map with key/value
     * @param key key to search for a value into map
     * @return return value related to that key in the map, or the own key if there is no correspondent.
     */
    protected final String keyIfValueNotFound(Map<String, String> map, String key) {
        return Objects.requireNonNullElse(map.get(key), key);
    }

    /**
     * Builds and registers {@link org.openhab.core.types.StateOption}s for a course channel from a
     * {@link CourseDefinition} map.
     *
     * @param courses map of course key → course definition (course name used as display label)
     * @param channelUID target channel to register the options on
     */
    protected void loadCourseOptions(Map<String, CourseDefinition> courses, ChannelUID channelUID) {
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, channelUID, courses,
                e -> emptyIfNull(e.getValue().getCourseName()));
    }

    /**
     * Starts executing queued commands, including the ones queued while the queue was disabled.
     */
    protected void enableCommandQueue() {
        synchronized (commandQueueLock) {
            commandQueueEnabled = true;
            scheduleCommandDrain();
        }
    }

    /**
     * Stops executing commands and discards the pending ones.
     */
    protected void disableCommandQueue() {
        synchronized (commandQueueLock) {
            commandQueueEnabled = false;
            commandBlockQueue.clear();
            Future<?> commandDrainJob = this.commandDrainJob;
            // A task that hasn't started isn't cancelled: it must run to release the queue, and it finds no command.
            // A running task keeps the queue until it exits, so a command queued after the queue is enabled again
            // can't run concurrently with the one being interrupted.
            if (commandDrainJob != null && commandDrainRunning) {
                commandDrainJob.cancel(true);
            }
        }
    }

    private void scheduleCommandDrain() {
        synchronized (commandQueueLock) {
            if (commandQueueEnabled && commandDrainJob == null && !commandBlockQueue.isEmpty()) {
                commandDrainJob = deviceScheduler.submit(this::drainCommandQueue);
            }
        }
    }

    private void drainCommandQueue() {
        synchronized (commandQueueLock) {
            commandDrainRunning = true;
        }
        try {
            AsyncCommandParams params;
            while (!Thread.currentThread().isInterrupted() && (params = nextQueuedCommand()) != null) {
                executeCommand(params);
            }
        } finally {
            synchronized (commandQueueLock) {
                commandDrainRunning = false;
                commandDrainJob = null;
                // commands left by an interrupted task or queued while it was finishing
                scheduleCommandDrain();
            }
        }
    }

    private @Nullable AsyncCommandParams nextQueuedCommand() {
        synchronized (commandQueueLock) {
            return commandQueueEnabled ? commandBlockQueue.poll() : null;
        }
    }

    private void executeCommand(AsyncCommandParams params) {
        try {
            processCommand(params);
            String channelUid = getSimpleChannelUID(params.channelUID());
            if (CHANNEL_AC_POWER_ID.equals(channelUid)) {
                // if processed command come from POWER channel, then force updateDeviceChannels immediatly
                // this is important to analise if the polling needs to be changed in time.
                updateThingStateFromLG();
            } else if (CHANNEL_EXTENDED_INFO_COLLECTOR_ID.equals(channelUid)) {
                if (OnOffType.ON.equals(params.command())) {
                    getLogger().debug("Turning ON extended information collector");
                    if (pollExtraInfoOnPowerOff
                            || DevicePowerState.DV_POWER_ON.equals(getLastShot().getPowerStatus())) {
                        startExtraInfoCollectorPolling();
                    }
                } else if (OnOffType.OFF.equals(params.command())) {
                    getLogger().debug("Turning OFF extended information collector");
                    stopExtraInfoCollectorPolling();
                } else {
                    getLogger().error("Command {} for {} channel is unexpected. It's most likely a bug",
                            params.command(), CHANNEL_EXTENDED_INFO_COLLECTOR_ID);
                }
            }
        } catch (LGThinqException e) {
            getLogger().error("Error executing Command {} to the channel {}. Thing goes offline until retry",
                    params.command(), params.channelUID(), e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (Exception e) {
            getLogger().error("System error executing Command {} to the channel {}. Ignoring command", params.command(),
                    params.channelUID(), e);
        }
    }

    protected void handleStatusChanged(ThingStatus newStatus, ThingStatusDetail statusDetail) {
        if (lastThingStatus != ThingStatus.ONLINE && newStatus == ThingStatus.ONLINE) {
            // start the thing polling
            startThingStatePolling();
        } else if (lastThingStatus == ThingStatus.ONLINE && newStatus == ThingStatus.OFFLINE
                && BRIDGE_STATUS_DETAIL_ERROR.contains(statusDetail)) {
            // comunication error is not a specific Bridge error, then we must analise it to give
            // this thinq the change to recovery from communication errors
            if (statusDetail != ThingStatusDetail.COMMUNICATION_ERROR
                    || (getBridge() instanceof Bridge bridge && bridge.getStatus() != ThingStatus.ONLINE)) {
                stopThingStatePolling();
                stopExtraInfoCollectorPolling();
            }
        }
        lastThingStatus = newStatus;
    }

    @Override
    protected void updateStatus(ThingStatus newStatus) {
        super.updateStatus(newStatus);
        handleStatusChanged(newStatus, ThingStatusDetail.NONE);
    }

    @Override
    protected void updateStatus(ThingStatus newStatus, @Nullable String description) {
        super.updateStatus(newStatus, description);
        handleStatusChanged(newStatus, ThingStatusDetail.NONE);
    }

    @Override
    protected void updateStatus(ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(statusDetail, description);
        handleStatusChanged(thing.getStatus(), statusDetail);
    }

    @Override

    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingStateFromLG();
        } else {
            AsyncCommandParams params = new AsyncCommandParams(channelUID.getId(), command);
            try {
                // Ensure commands are send in a pipe per device.
                commandBlockQueue.add(params);
                scheduleCommandDrain();
            } catch (IllegalStateException ex) {
                getLogger().warn(
                        "Device's command queue reached the size limit. Probably the device is busy or stuck. Ignoring command.");
                if (getLogger().isDebugEnabled()) {
                    boolean draining;
                    synchronized (commandQueueLock) {
                        draining = commandDrainJob != null;
                    }
                    getLogger().debug("Status of the commandQueue: consumer: {}, size: {}", draining ? "ON" : "OFF",
                            commandBlockQueue.size());
                }
                if (getLogger().isTraceEnabled()) {
                    // logging the thread dump to analise possible stuck thread.
                    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                    ThreadInfo[] infos = bean.dumpAllThreads(true, true);
                    String message = "";
                    for (ThreadInfo i : infos) {
                        message = String.format("%s\n%s", message, i.toString());
                    }
                    getLogger().trace("{}", message);
                }
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.handler.device-cmd-queue-busy");
            }
        }
    }

    public String getDeviceId() {
        return Objects.requireNonNullElse(getThing().getProperties().get(PROP_INFO_DEVICE_ID), "undef");
    }

    public abstract String getDeviceAlias();

    public abstract String getDeviceUriJsonConfig();

    public abstract void onDeviceRemoved();

    public abstract void onDeviceDisconnected();

    public abstract void updateChannelDynStateDescription() throws LGThinqApiException;

    public abstract LGThinQApiClientService<@NonNull C, @NonNull S> getLgThinQAPIClientService();

    public C getCapabilities() throws LGThinqApiException {
        if (thinQCapability == null) {
            thinQCapability = getLgThinQAPIClientService().getCapability(getDeviceId(), getDeviceUriJsonConfig(),
                    false);
        }
        return Objects.requireNonNull(thinQCapability, "Unexpected error. Return of capability shouldn't ever be null");
    }

    /**
     * Get the first item value associated to the channel
     *
     * @param channelUID channel
     * @return value of the first item related to this channel, or null if unlinked or undefined.
     */
    @Nullable
    protected String getItemLinkedValue(ChannelUID channelUID) {
        Set<Item> items = itemChannelLinkRegistry.getLinkedItems(channelUID);
        if (!items.isEmpty()) {
            for (Item i : items) {
                State state = i.getState();
                return state instanceof UnDefType ? null : state.toString();
            }
        }
        return null;
    }

    protected abstract Logger getLogger();

    @Override
    public void initialize() {
        getLogger().debug("Initializing Thinq thing.");

        final Bridge bridge = getBridge();
        if (bridge == null) {
            getLogger().debug("Bridge unavailable while initializing Thing: {}", thing.getUID());
            return;
        }
        if (bridge.getHandler() instanceof LGThinQBridgeHandler bridgeHandler) {
            this.account = bridgeHandler;
            this.bridgeId = bridge.getUID().getId();
        } else {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.communication-error.no-bridge-set");
            return;
        }

        initializeThing(bridge.getStatus());
    }

    protected void initializeThing(@Nullable ThingStatus bridgeStatus) {
        getLogger().debug("initializeThing LQ Thinq {}. Bridge status {}", getThing().getUID(), bridgeStatus);
        String thingId = getThing().getUID().getId();

        // setup configurations
        loadConfigurations();

        if (!thingId.isBlank()) {
            try {
                updateChannelDynStateDescription();
            } catch (LGThinqApiException e) {
                getLogger().warn(
                        "Error updating channels dynamic options descriptions based on capabilities of the device. Fallback to default values.",
                        e);
            }
            // registry this thing to the bridge
            var account = this.account;
            if (account == null) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.communication-error.no-bridge-set");
            } else {
                account.registryListenerThing(this);
                if (bridgeStatus == null) {
                    updateStatus(ThingStatus.UNKNOWN);
                } else {
                    switch (bridgeStatus) {
                        case ONLINE:
                            updateStatus(ThingStatus.ONLINE);
                            break;
                        case INITIALIZING:
                        case UNINITIALIZED:
                            updateStatus(ThingStatus.UNKNOWN);
                            break;
                        case UNKNOWN:
                            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR);
                            break;
                        default:
                            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
                            break;
                    }
                }
            }
        } else {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error-no-device-id");
        }
        // finally, start command queue, regardless of the thing state, since we can still try to send commands without
        // property ONLINE (the successful result from command request can put the thing in ONLINE status).
        enableCommandQueue();
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                getLgThinQAPIClientService().initializeDevice(bridgeId, getDeviceId());
            } catch (Exception e) {
                getLogger().warn("Error initializing the device {} from bridge {}.", thingId, bridgeId, e);
            }
            // force start state pooling if the device is ONLINE
            resetExtraInfoChannels();
            startThingStatePolling();
        }
    }

    public void refreshStatus() {
        if (thing.getStatus() == ThingStatus.OFFLINE) {
            initialize();
        }
    }

    private void loadConfigurations() {
        isThingReconfigured = true;
        Map<String, Object> props = getThing().getConfiguration().getProperties();
        pollingPeriodOnSeconds = (props.get(CFG_POLLING_PERIOD_POWER_ON_SEC) instanceof BigDecimal value)
                ? value.intValue()
                : pollingPeriodOnSeconds;
        pollingPeriodOffSeconds = (props.get(CFG_POLLING_PERIOD_POWER_OFF_SEC) instanceof BigDecimal value)
                ? value.intValue()
                : pollingPeriodOffSeconds;
        pollingExtraInfoPeriodSeconds = (props.get(CFG_POLLING_EXTRA_INFO_PERIOD_SEC) instanceof BigDecimal value)
                ? value.intValue()
                : pollingExtraInfoPeriodSeconds;
        pollExtraInfoOnPowerOff = (props.get(CFG_POLLING_EXTRA_INFO_ON_POWER_OFF) instanceof Boolean value) ? value
                : pollExtraInfoOnPowerOff;
        // if the periods are the same, I can define currentPeriod for polling right now. If not, I postpone to the nest
        // snapshot update
        if (pollingPeriodOffSeconds == pollingPeriodOnSeconds) {
            currentPeriodSeconds = pollingPeriodOffSeconds;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        getLogger().debug("bridgeStatusChanged {}", bridgeStatusInfo);
        // restart scheduler
        initializeThing(bridgeStatusInfo.getStatus());
    }

    /**
     * Determine if the Handle for this device supports Energy State Collector
     *
     * @return always false and must be overridden if the implemented handler supports energy collector
     */
    protected boolean isExtraInfoCollectorSupported() {
        return false;
    }

    /**
     * Returns if the energy collector is enabled. The handle that supports energy collection must
     * provide a logic that defines if the collector is currently enabled. Normally, it uses a Switch Channel
     * to provide a way to the user turn on/off the collector.
     *
     * @return true if the energyCollector must be enabled.
     */
    protected boolean isExtraInfoCollectorEnabled() {
        return false;
    }

    private void updateExtraInfoState() {
        if (!isExtraInfoCollectorSupported()) {
            getLogger().error(
                    "The Energy Collector was started for a Handler that doesn't support it. It is most likely a bug.");
            return;
        }
        try {
            Map<String, Object> extraInfoCollected = collectExtraInfoState();
            updateExtraInfoStateChannels(extraInfoCollected);
        } catch (LGThinqException ex) {
            getLogger().warn(
                    "Error getting energy state and update the correlated channels. DeviceName: {}, DeviceId: {}. Error: {}",
                    getDeviceAlias(), getDeviceId(), ex.getMessage(), ex);
        }
    }

    protected void updateThingStateFromLG() {
        try {
            @Nullable
            S shot = getSnapshotDeviceAdapter(getDeviceId());
            if (shot == null) {
                // no data to update. Maybe, the monitor stopped, then it's going to be restarted next try.
                return;
            }
            if (!shot.isOnline()) {
                if (getThing().getStatus() != ThingStatus.OFFLINE) {
                    // only update channels if the device has just gone OFFLINE.
                    updateDeviceChannelsWrapper(shot);
                    updateStatus(ThingStatus.OFFLINE, "@text/offline.device-disconnected");
                    onDeviceDisconnected();
                }
            } else {
                // do not update channels if the device is offline
                updateDeviceChannelsWrapper(shot);
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        } catch (LGThinqAccessException e) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (LGThinqApiExhaustionException e) {
            // ThinQ V1 monitors can return no data for several cycles (ex. while the device is idle) although the
            // device is reachable, so the last known state is kept instead of taking the Thing offline.
            getLogger().debug("LG API returned no monitoring data for the thing {}/{}. Keeping the last known state.",
                    getDeviceAlias(), getDeviceId());
        } catch (LGThinqException e) {
            getLogger().error("Error updating thing {}/{} from LG API. Thing goes OFFLINE until next retry: {}",
                    getDeviceAlias(), getDeviceId(), e.getMessage(), e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (Exception e) {
            getLogger().error(
                    "System error in pooling thread (UpdateDevice) for device {}/{}. Filtering to do not stop the thread",
                    getDeviceAlias(), getDeviceId(), e);
        }
    }

    private void handlePowerChange(@Nullable DevicePowerState previous, DevicePowerState current) {
        // isThingReconfigured is true when configurations has been updated or thing has just initialized
        // this will force to analyse polling periods and starts
        if (!isThingReconfigured && previous == current) {
            // no changes needed
            return;
        }

        // change from OFF to ON / OFF to ON
        boolean isEnableToStartCollector = isExtraInfoCollectorEnabled() && isExtraInfoCollectorSupported();

        if (current == DevicePowerState.DV_POWER_ON) {
            currentPeriodSeconds = pollingPeriodOnSeconds;

            // if extendedInfo collector is enabled, then force do start to prevent previous stop
            if (isEnableToStartCollector) {
                startExtraInfoCollectorPolling();
            }
        } else {
            currentPeriodSeconds = pollingPeriodOffSeconds;

            // if it's configured to stop extra-info collection on PowerOff, then stop the job
            if (!pollExtraInfoOnPowerOff) {
                stopExtraInfoCollectorPolling();
            } else if (isEnableToStartCollector) {
                startExtraInfoCollectorPolling();
            }
        }

        // restart thing state polling for the new poolingPeriod configuration
        if (pollingPeriodOffSeconds != pollingPeriodOnSeconds) {
            stopThingStatePolling();
        }

        startThingStatePolling();
    }

    private void updateDeviceChannelsWrapper(S snapshot) throws LGThinqApiException {
        updateDeviceChannels(snapshot);
        // handle power changes
        handlePowerChange(getLastShot().getPowerStatus(), snapshot.getPowerStatus());
        // after updated successfully, copy snapshot to last snapshot
        lastShot = snapshot;
        // and finish the cycle of thing reconfiguration (when thing starts or has configurations changed - if it's the
        // case)
        isThingReconfigured = false;
    }

    protected abstract void updateDeviceChannels(S snapshot) throws LGThinqApiException;

    protected String translateFeatureToItemType(FeatureDataType dataType) {
        return switch (dataType) {
            case UNDEF, ENUM -> CoreItemFactory.STRING;
            case RANGE -> CoreItemFactory.DIMMER;
            case BOOLEAN -> CoreItemFactory.SWITCH;
            default -> throw new IllegalStateException(
                    String.format("Feature DataType %s not supported for this ThingHandler", dataType));
        };
    }

    protected void stopThingStatePolling() {
        try {
            ScheduledFuture<?> thingStatePollingJob = this.thingStatePollingJob;

            if (!(thingStatePollingJob == null || thingStatePollingJob.isDone())) {
                getLogger().debug("Stopping LG thinq polling for device/alias: {}/{}", getDeviceId(), getDeviceAlias());
                thingStatePollingJob.cancel(true);
            }
            this.thingStatePollingJob = null;
        } catch (Exception ex) {
            getLogger().warn("Unexpected error trying to cancel state polling job.");
        }
    }

    private void stopExtraInfoCollectorPolling() {
        try {
            ScheduledFuture<?> extraInfoCollectorPollingJob = this.extraInfoCollectorPollingJob;
            if (extraInfoCollectorPollingJob != null && !extraInfoCollectorPollingJob.isDone()) {
                getLogger().debug("Stopping Energy Collector for device/alias: {}/{}", getDeviceId(), getDeviceAlias());
                extraInfoCollectorPollingJob.cancel(true);
            }
            resetExtraInfoChannels();
            this.extraInfoCollectorPollingJob = null;
        } catch (Exception ex) {
            getLogger().warn("Unexpected error trying to cancel extra info polling job.");
        }
    }

    protected void startThingStatePolling() {
        ScheduledFuture<?> thingStatePollingJob = this.thingStatePollingJob;
        if (thingStatePollingJob == null || thingStatePollingJob.isDone()) {
            getLogger().debug("Starting LG thinq polling for device/alias: {}/{}", getDeviceId(), getDeviceAlias());
            this.thingStatePollingJob = deviceScheduler.scheduleWithFixedDelay(new UpdateThingStateFromLG(), 5,
                    currentPeriodSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Method responsible for start the Energy Collector Polling. Must be called buy the handles when it's desired.
     * Normally, the thing has a Switch Channel that enable/disable the energy collector. By default, the collector is
     * disabled.
     */
    private void startExtraInfoCollectorPolling() {
        ScheduledFuture<?> extraInfoCollectorPollingJob = this.extraInfoCollectorPollingJob;
        if (extraInfoCollectorPollingJob == null || extraInfoCollectorPollingJob.isDone()) {
            getLogger().debug("Starting Energy Collector for device/alias: {}/{}", getDeviceId(), getDeviceAlias());
            this.extraInfoCollectorPollingJob = deviceScheduler.scheduleWithFixedDelay(new UpdateExtraInfoCollector(),
                    10, pollingExtraInfoPeriodSeconds, TimeUnit.SECONDS);
        }
    }

    private void stopDeviceV1Monitor(String deviceId) {
        try {
            monitorPhase = MonitorPhase.IDLE;
            getLgThinQAPIClientService().stopMonitor(getBridgeId(), deviceId, monitorWorkId);
        } catch (LGThinqDeviceV1OfflineException e) {
            getLogger().debug("Monitor stopped. Device is unavailable/disconnected", e);
        } catch (Exception e) {
            getLogger().error("Error stopping LG Device monitor", e);
        }
    }

    protected String getBridgeId() {
        if (bridgeId.isBlank()) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                getLogger().debug("Bridge unavailable for Thing: {}", thing.getUID());
                return "UNKNOWN";
            }
            bridgeId = bridge.getUID().getId();
        }
        return bridgeId;
    }

    protected abstract DeviceTypes getDeviceType();

    @Nullable
    protected S getSnapshotDeviceAdapter(String deviceId) throws LGThinqApiException, LGThinqApiExhaustionException {
        if (LG_API_PLATFORM_TYPE_V2.equals(lgPlatformType)) {
            return getLgThinQAPIClientService().getDeviceData(getBridgeId(), getDeviceId(), getCapabilities());
        } else {
            try {
                if (monitorPhase == MonitorPhase.IDLE) {
                    monitorWorkId = getLgThinQAPIClientService().startMonitor(getBridgeId(), getDeviceId());
                    monitorPhase = MonitorPhase.IN_PROGRESS;
                }
            } catch (LGThinqDeviceV1OfflineException e) {
                try {
                    stopDeviceV1Monitor(deviceId);
                } catch (Exception ignored) {
                }
                return getLgThinQAPIClientService().buildDefaultOfflineSnapshot();
            } catch (Exception e) {
                stopDeviceV1Monitor(deviceId);
                throw new LGThinqApiException("Error starting device monitor in LG API for the device:" + deviceId, e);
            }
            try {
                return pollMonitorData(deviceId);
            } finally {
                try {
                    stopDeviceV1Monitor(deviceId);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Nullable
    private S pollMonitorData(String deviceId) throws LGThinqApiException, LGThinqApiExhaustionException {
        int retries = 10;
        try {
            while (retries > 0) {
                @Nullable
                S shot = getLgThinQAPIClientService().getMonitorData(getBridgeId(), deviceId, monitorWorkId,
                        getDeviceType(), getCapabilities());
                if (shot != null) {
                    return shot;
                }
                Thread.sleep(500);
                retries--;
            }
        } catch (LGThinqDeviceV1MonitorExpiredException | LGThinqUnmarshallException e) {
            getLogger().debug("Monitor for device {} is invalid. Forcing stop and start to next cycle.", deviceId);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            throw new LGThinqApiException("Error getting monitor data for the device:" + deviceId, e);
        }
        throw new LGThinqApiExhaustionException("Exhausted trying to get monitor data for the device:" + deviceId);
    }

    protected abstract void processCommand(AsyncCommandParams params) throws LGThinqApiException;

    @Override
    public void dispose() {
        getLogger().debug("Disposing Thinq Thing {}", getDeviceId());
        var account = this.account;
        if (account != null) {
            account.unRegistryListenerThing(this);
        }

        stopThingStatePolling();
        stopExtraInfoCollectorPolling();
        disableCommandQueue();
        try {
            if (LGAPIVerion.V1_0.equals(getCapabilities().getDeviceVersion())) {
                stopDeviceV1Monitor(getDeviceId());
            }
        } catch (Exception e) {
            getLogger().warn("Can't stop active monitor. It's can be normally ignored. Cause:{}", e.getMessage());
        }
    }

    /**
     * Create Dynamic channel. The channel type <b>must be pre-defined in the thing definition (xml) and with
     * the same name as the channel.</b>
     *
     * @param channelNameAndTypeName channel name to be created and the same channel type name defined in the channels
     *            descriptor
     * @param channelUuid Uid of the channel
     * @param itemType item type (see openhab documentation)
     * @return return the new channel created
     */
    protected Channel createDynChannel(String channelNameAndTypeName, ChannelUID channelUuid, String itemType) {
        if (getCallback() == null) {
            throw new IllegalStateException("Unexpected behaviour. Callback not ready! Can't create dynamic channels");
        } else {
            // dynamic create channel
            ChannelBuilder builder = Objects
                    .requireNonNull(getCallback(), "Not expected callback null here. It is most likely a bug")
                    .createChannelBuilder(channelUuid, new ChannelTypeUID(BINDING_ID, channelNameAndTypeName));
            Channel channel = builder.withKind(ChannelKind.STATE).withAcceptedItemType(itemType).build();
            updateThing(editThing().withChannel(channel).build());
            return channel;
        }
    }

    protected void manageDynChannel(ChannelUID channelUid, String channelName, String itemType,
            boolean isFeatureAvailable) {
        Channel chan = getThing().getChannel(channelUid);
        if (chan == null && isFeatureAvailable) {
            createDynChannel(channelName, channelUid, itemType);
        } else if (chan != null && (!isFeatureAvailable)) {
            updateThing(editThing().withoutChannel(chan.getUID()).build());
        }
    }

    private class UpdateExtraInfoCollector implements Runnable {
        @Override
        public void run() {
            updateExtraInfoState();
        }
    }

    private class UpdateThingStateFromLG implements Runnable {
        @Override
        public void run() {
            updateThingStateFromLG();
        }
    }
}
