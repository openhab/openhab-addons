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
package org.openhab.binding.argoclima.internal.handler;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;
import org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationBase;
import org.openhab.binding.argoclima.internal.device.api.IArgoClimaDeviceAPI;
import org.openhab.binding.argoclima.internal.device.api.types.ArgoDeviceSettingType;
import org.openhab.binding.argoclima.internal.exception.ArgoApiCommunicationException;
import org.openhab.binding.argoclima.internal.exception.ArgoApiProtocolViolationException;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;
import org.openhab.binding.argoclima.internal.exception.ArgoRemoteServerStubStartupException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code ArgoClimaHandlerBase} is an abstract base class for common logic (across local and remote thing
 * implementations) responsible for handling commands, which are sent to one of the channels.
 *
 * @see ArgoClimaHandlerLocal
 * @see ArgoClimaHandlerRemote
 *
 * @param <ConfigT> Type of configuration class used:
 *            {@link org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationLocal
 *            ArgoClimaConfigurationLocal} or
 *            {@link org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationRemote
 *            ArgoClimaConfigurationRemote}
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public abstract class ArgoClimaHandlerBase<ConfigT extends ArgoClimaConfigurationBase> extends BaseThingHandler {
    enum StateRequestType {
        REQUEST_FRESH_STATE,
        GET_CACHED_STATE
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final boolean awaitConfirmationUponSendingCommands;
    private final Duration sendCommandStatusPollFrequency;
    private final Duration sendCommandResubmitFrequency;
    private final Duration sendCommandMaxWaitTime;
    private final Duration sendCommandMaxWaitTimeIndirectMode;
    protected final ArgoClimaTranslationProvider i18nProvider;

    // Set-up through initialize()
    private Optional<IArgoClimaDeviceAPI> deviceApi = Optional.empty();
    private Optional<ConfigT> config = Optional.empty();

    // Threading/job related stuff
    private Optional<ScheduledFuture<?>> refreshTask = Optional.empty();
    private Optional<Future<?>> initializeFuture = Optional.empty();
    private Optional<Future<?>> deviceCommandSenderFuture = Optional.empty();
    private AtomicLong lastRefreshTime = new AtomicLong(Instant.now().toEpochMilli());
    private AtomicInteger failedApiCallsCounter = new AtomicInteger(0);

    /**
     * C-tor
     *
     * @param thing The @code Thing} this handler serves (provided by the framework through
     *            {@link org.openhab.binding.argoclima.internal.ArgoClimaHandlerFactory ArgoClimaHandlerFactory}
     * @param awaitConfirmationAfterSend If true, will wait for device to confirm the update after sending a command to
     *            it
     * @param poolFrequencyAfterSend The status refresh frequency for updated status after issuing a command (relevant
     *            only if {@code awaitConfirmationAfterSend == true})
     * @param sendRetryFrequency The retry frequency (to re-issue a command if no confirmation received). Relevant only
     *            if {@code awaitConfirmationAfterSend == true}, should be higher than {@code poolFrequencyAfterSend}
     * @param sendMaxRetryTimeDirect Max time to wait for device-side confirmation in direct mode (when this binding is
     *            issuing the comms). (relevant only if {@code awaitConfirmationAfterSend == true})
     * @param sendMaxWaitTimeIndirect Max time to wait for device-side confirmation in indirect mode (when this binding
     *            is only sniffing/intercepting the comms and injecting commands into a server replies). Typically
     *            longer than {@code sendMaxRetryTimeDirect}. Relevant only if
     *            {@code awaitConfirmationAfterSend == true})
     * @param i18nProvider Framework's translation provider
     */
    public ArgoClimaHandlerBase(Thing thing, boolean awaitConfirmationAfterSend, Duration poolFrequencyAfterSend,
            Duration sendRetryFrequency, Duration sendMaxRetryTimeDirect, Duration sendMaxWaitTimeIndirect,
            final ArgoClimaTranslationProvider i18nProvider) {
        super(thing);
        this.awaitConfirmationUponSendingCommands = awaitConfirmationAfterSend;
        this.sendCommandStatusPollFrequency = poolFrequencyAfterSend;
        this.sendCommandResubmitFrequency = sendRetryFrequency;
        this.sendCommandMaxWaitTime = sendMaxRetryTimeDirect;
        this.sendCommandMaxWaitTimeIndirectMode = sendMaxWaitTimeIndirect;
        this.i18nProvider = i18nProvider;
    }

    /**
     * Initializes the thing config with concrete type and transforms it to the given class.
     *
     * @return config
     * @throws ArgoConfigurationException in case of configuration errors
     */
    protected abstract ConfigT getConfigInternal() throws ArgoConfigurationException;

    /**
     * Creates and initializes concrete device API (the actual communication path to the device).
     * In case the API has passive passive components (such as pass-through server), they are started as well
     * (their lifecycle is tracked by this class)
     *
     * @param config The Thing configuration
     * @return Initialized Device API
     * @throws ArgoConfigurationException In case the API initialization fails due to Thing configuration issues
     * @throws ArgoRemoteServerStubStartupException In case the Device API startup involved launching an intercepting
     *             server (thing type and configuration-dependent), and the startup has failed
     */
    protected abstract IArgoClimaDeviceAPI initializeDeviceApi(ConfigT config)
            throws ArgoRemoteServerStubStartupException, ArgoConfigurationException;

    /**
     * {@inheritDoc}
     *
     * @implNote Initializes thing config and device API, and continues the thing initialization asynchronously through
     *           {@link #initializeThing()} - as this method must return quickly. Also launches device state regular
     *           polling (if configured) -
     *           {@link #startAutomaticRefresh()}. While the poll will also (re)initialize the device, a dedicated
     *           initialization logic is kept b/c polling may be disabled by the user (and there's no harm in triggering
     *           both poll and refresh -> first to complete will win).
     * @implNote If either of the initialize/poll threads are launched, both {@link #config} and {@link #deviceApi} are
     *           guaranteed to have values (so their use is safe from any other method from this class except for
     *           {@code dispose()}, as they are either invoked by the threads started herein, or guaranteed by the
     *           framework to not get called if the device is not initialized. Hence a check for successful
     *           initialization is NOT performed on each and every method.
     */
    @Override
    public final void initialize() {
        // Step0: If this a re-initialize (ex. config change), let's stop everything and start anew (not supporting
        // graceful updates to the refresher threads and/or passthrough server)
        this.config.ifPresent(c -> stopRunningTasks());

        // Step1: Init config
        try {
            this.config = Optional.of(getConfigInternal());
        } catch (ArgoConfigurationException ex) {
            logger.debug("[{}] {}", getThing().getUID().getId(), ex.getMessage()); // the non-i18nzed message is logged
                                                                                   // explicitly (not redundant with
                                                                                   // updateStatus's logging)
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getLocalizedMessage());
            return;
        }
        logger.debug("[{}] Running with config: {}", getThing().getUID(), config.get().toString());

        var configValidationError = config.get().validate();
        if (!configValidationError.isEmpty()) {
            var message = i18nProvider.getText("thing-status.argoclima.invalid-config",
                    "Invalid thing configuration. {0}", configValidationError);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            return;
        }

        // Step2: Init device API (this will start passthrough server & client threads, if configured)
        try {
            this.deviceApi = Optional.of(initializeDeviceApi(config.get()));
        } catch (ArgoRemoteServerStubStartupException | ArgoConfigurationException e) {
            logger.debug("[{}] Failed to initialize Device API. Error: {}", getThing().getUID(), e.getMessage()); // the
                                                                                                                  // non-i18nzed
                                                                                                                  // message
                                                                                                                  // is
                                                                                                                  // logged
                                                                                                                  // explicitly
                                                                                                                  // (not
                                                                                                                  // redundant
                                                                                                                  // with
                                                                                                                  // updateStatus's
                                                                                                                  // logging)
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getLocalizedMessage());
            return;
        } catch (Exception e) {
            logger.debug("[{}] Failed to initialize Device API. Unknown Error: {}", getThing().getUID(),
                    e.getMessage()); // the non-i18nzed message is logged explicitly (not redundant with updateStatus's
                                     // logging)
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    i18nProvider.getText("thing-status.argoclima.handler-init-failure",
                            "Error while initializing Thing: {0}", e.getLocalizedMessage()));
            return;
        }

        // Step 3: Set the thing status to UNKNOWN temporarily and let the background task decide the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        updateStatus(ThingStatus.UNKNOWN);

        // Step 4: Start polling (if configured)
        if (this.config.get().getRefreshInterval() > 0) {
            lastRefreshTime.set(Instant.now().toEpochMilli()); // Skips 1st refresh cycle (no need, initializer will do
                                                               // it instead)
            startAutomaticRefresh();
        }

        // Step 5: Kick off the "real" initialization logic :)
        synchronized (this) {
            initializeFuture = Optional.ofNullable(scheduler.submit(this::initializeThing));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This is deliberately made final, as the class-specific disposal has been moved to
     *           {@link ArgoClimaHandlerBase#stopRunningTasks()}, to semantically separate a stop-on-dispose on a
     *           regular stop (which is also done on re-initialize)
     */
    @Override
    public final void dispose() {
        logger.trace("{}: Thing {} is disposing", getThing().getUID().getId(), thing.getUID());
        stopRunningTasks();
        logger.trace("{}: Disposed", getThing().getUID().getId());
    }

    /**
     * @implNote This is overridden in {@link org.openhab.binding.argoclima.internal.handler.ArgoClimaHandlerLocal} to
     *           handle disposal of the
     */
    protected synchronized void stopRunningTasks() {
        if (this.deviceApi.isPresent()) {
            // Setting the device API as empty first, as its absence also serves as a marker of disposal started
            // Note it may still be alive after that, as the shared HTTP client may have pending I/O withstanding.
            // These will be cleaned up when the respective refresher tasks stop (later in this function)
            deviceApi = Optional.empty();
        }

        try {
            stopRefreshTask(); // Stop polling for new updates
        } catch (Exception e) {
            logger.trace("Exception during handler disposal", e);
        }

        try {
            initializeFuture.ifPresent(initter -> initter.cancel(true));
        } catch (Exception e) {
            logger.trace("Exception during handler disposal", e);
        }

        try {
            cancelPendingDeviceCommandSenderJob();
        } catch (Exception e) {
            logger.trace("Exception during handler disposal", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The Argo API always gets every readable params in one go and sends multiple commands in one request.
     *           Hence, if the requested command is a {@link RefreshType}, the {@code channelUID} is ignored, since
     *           everything will be updated anyway (refresh one == refresh them all).
     * @implNote The actual device comms are made on a separate thread (and have a baked-in debounce time to avoid
     *           sending multiple requests). The device responses (even in direct communication mode) are somewhat slow
     *           and may take 1-2 cycles for the new value to apply, which is why the binding waits for them to be
     *           confirmed (and uses a device-reported state only after it gives up trying to effect the command).
     * @implNote In a remote or indirect (pass-through) modes, we're constrained by the device own poll cycles (seem to
     *           occur every minute, assuming the device is up and has successful uplink to Argo servers). Hence, an
     *           update may take long to apply (circa 1-2 min). During this time, if new commands are send to the
     *           Thing, they get stacked with the existing ones (each command tracks its expire time separately though!)
     * @implNote While {@link ArgoDeviceSettingType#RESET_TO_FACTORY_SETTINGS} is an API parameter it is modeled as
     *           configuration property and NOT a channel (hence not available here). Similarly
     *           {@link ArgoDeviceSettingType#UNIT_FIRMWARE_VERSION} is a property, not a Channel.
     */
    @Override
    public final void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            sendCommandsToDeviceAwaitConfirmation(true); // Irrespective of channel (all in one go). Note this has no
                                                         // effect for indirect/pass-through mode. We're bound to
                                                         // device's own poll cycles anyway
            return;
        }

        boolean hasUpdates = false;
        // Channel -> ArgoDeviceSettingType mapping (could be within enum, but kept here for better visibility)
        if (ArgoClimaBindingConstants.CHANNEL_POWER.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.POWER, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_ACTIVE_TIMER.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.ACTIVE_TIMER, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_CURRENT_TEMPERATURE.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.ACTUAL_TEMPERATURE, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_ECO_MODE.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.ECO_MODE, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_FAN_SPEED.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.FAN_LEVEL, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_FILTER_MODE.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.FILTER_MODE, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_SWING_MODE.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.FLAP_LEVEL, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_I_FEEL_ENABLED.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.I_FEEL_TEMPERATURE, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_DEVICE_LIGHTS.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.LIGHT, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_MODE.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.MODE, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_MODE_EX.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.MODE, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_NIGHT_MODE.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.NIGHT_MODE, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_SET_TEMPERATURE.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.TARGET_TEMPERATURE, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_TURBO_MODE.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.TURBO_MODE, command, channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_TEMPERATURE_DISPLAY_UNIT.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.DISPLAY_TEMPERATURE_SCALE, command,
                    channelUID);
        }
        if (ArgoClimaBindingConstants.CHANNEL_ECO_POWER_LIMIT.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.ECO_POWER_LIMIT, command, channelUID);
        }

        if (ArgoClimaBindingConstants.CHANNEL_DELAY_TIMER.equals(channelUID.getId())) {
            hasUpdates |= handleIndividualSettingCommand(ArgoDeviceSettingType.TIMER_0_DELAY_TIME, command, channelUID);
        }

        if (hasUpdates) {
            sendCommandsToDeviceAwaitConfirmation(false); // Schedule sending to device (without forcing value refresh)
        }
    }

    /**
     * Convert received HVAC state: {@code deviceState} into Thing channel updates
     *
     * @param deviceState The state read/received from device (may also be cached)
     * @implNote Not all device-reported elements are modeled as channels, and may be reflected as configuration or
     *           properties (ex.: {@code UNIT_FIRMWARE_VERSION}, or schedule timer on/off/weekdays params)
     * @implNote A single device update may update more than one channel. For example the device mode is represented as
     *           BOTH {@code CHANNEL_MODE} and {@code CHANNEL_MODE_EX}. This is because the remote protocol supports
     *           more values than the typical HVAC device. Hence, the full list of modes is available in its own
     *           advanced ("_EX") channel, and the regular one is providing most common options for better usability.
     *           Both Channels get updated off of the same API field though.
     * @apiNote This method is also called asynchronously from an intercepting/stub server
     */
    protected final void updateChannelsFromDevice(Map<ArgoDeviceSettingType, State> deviceState) {
        if (deviceApi.isEmpty()) {
            return; // The thing handler is disposing. No need to update channels
        }

        for (Entry<ArgoDeviceSettingType, State> entry : deviceState.entrySet()) {
            var channelNames = Set.<String> of();
            switch (entry.getKey()) {
                case ACTIVE_TIMER:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_ACTIVE_TIMER);
                    break;
                case ACTUAL_TEMPERATURE:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_CURRENT_TEMPERATURE);
                    break;
                case DISPLAY_TEMPERATURE_SCALE:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_TEMPERATURE_DISPLAY_UNIT);
                    break;
                case ECO_MODE:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_ECO_MODE);
                    break;
                case ECO_POWER_LIMIT:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_ECO_POWER_LIMIT);
                    break;
                case FAN_LEVEL:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_FAN_SPEED);
                    break;
                case FILTER_MODE:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_FILTER_MODE);
                    break;
                case FLAP_LEVEL:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_SWING_MODE);
                    break;
                case I_FEEL_TEMPERATURE:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_I_FEEL_ENABLED);
                    break;
                case LIGHT:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_DEVICE_LIGHTS);
                    break;
                case MODE: // As 2 channels. See thing-type.xml for description of these
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_MODE,
                            ArgoClimaBindingConstants.CHANNEL_MODE_EX);
                    break;
                case NIGHT_MODE:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_NIGHT_MODE);
                    break;
                case POWER:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_POWER);
                    break;
                case TARGET_TEMPERATURE:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_SET_TEMPERATURE);
                    break;
                case TIMER_0_DELAY_TIME:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_DELAY_TIMER);
                    break;
                case TURBO_MODE:
                    channelNames = Set.of(ArgoClimaBindingConstants.CHANNEL_TURBO_MODE);
                    break;
                case CURRENT_DAY_OF_WEEK: // not reflected anywhere (write-only part of protocol)
                case CURRENT_TIME:
                    break;
                case TIMER_N_ENABLED_DAYS: // Timer schedule is represented as config, not as channel
                case TIMER_N_OFF_TIME:
                case TIMER_N_ON_TIME:
                    break;
                case RESET_TO_FACTORY_SETTINGS: // Represented as config
                    break;
                case UNIT_FIRMWARE_VERSION: // Represented as property
                    break;
                default:
                    break;
            }

            // Send updates to the framework
            channelNames.forEach(chnl -> updateState(chnl, entry.getValue()));
        }
    }

    /**
     * Informs the underlying DeviceAPI about a framework-issued command and returns status if the update was
     * commissioned.
     *
     * @param settingType The API-side setting type receiving a command
     * @param command The command sent by the framework
     * @param channelUID Original channel the command got issued through
     * @return True if the command was handled and is now in-flight (about to be sent to device). False - otherwise
     */
    private final boolean handleIndividualSettingCommand(ArgoDeviceSettingType settingType, Command command,
            ChannelUID channelUID) {
        if (command instanceof RefreshType) {
            return true; // Refresh commands always trigger an update
        }

        // Pass value to underlying handler (if handled, it will make it in-flight and communicated to device on next
        // comms cycle)
        boolean updateInitiated = this.deviceApi.orElseThrow().handleSettingCommand(settingType, command);
        if (updateInitiated) {
            // Get updated device state and inform framework immediately that the binding accepted it. Note this
            // technically doesn't yet mean the device changed its state nor even that the command got sent just this
            // minute, but given some values are write-only (never confirmed) and the value *is* committed to be sent,
            // we're confirming at this point (so that the value doesn't linger as "predicted")
            State currentState = this.deviceApi.orElseThrow().getCurrentStateNoPoll(settingType);
            logger.trace("State of {} after update: {}", channelUID, currentState);
            updateState(channelUID, currentState);
        }
        return updateInitiated;
    }

    /**
     * Updates dynamic Thing properties from values read from device
     *
     * @param entries The new properties to append/replace (this does not clear existing properties!)
     *
     * @implNote Unfortunately framework's {@link BaseThingHandler#updateProperties(Map<String, String>)} implementation
     *           clones the map into a {@code HashMap}, which means the edited properties will lose their sorting, yet
     *           still providing it via a {@code TreeMap} in hopes framework may respect the ordering some day ;)
     * @apiNote This method is also called asynchronously from an intercepting/stub server
     */
    protected final void updateThingProperties(SortedMap<String, String> entries) {
        if (deviceApi.isEmpty()) {
            return; // The thing handler is disposing. No need to update properties
        }

        TreeMap<String, String> currentProps = new TreeMap<>(this.editProperties()); // This unfortunately loses sorting
        entries.entrySet().stream().forEach(x -> currentProps.put(x.getKey(), x.getValue()));
        this.updateProperties(currentProps);
    }

    /**
     * Updates the status of the thing to ONLINE (no details)
     *
     * @apiNote This method is also called asynchronously from an intercepting/stub server
     */
    protected final void updateThingStatusToOnline(ThingStatus newStatus) {
        if (ThingStatus.ONLINE.equals(newStatus)) {
            // only one-way update from callback
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.trace("The remote stub server attempted to update the thing status to {}. The request was ignored",
                    newStatus);
        }
    }

    /**
     * Trigger channel update from HVAC device. If {@code useCachedState==false}, will trigger outbound communications
     * to the device (or remote Argo server, depending on mode).
     * <p>
     * For local mode with pass-through, if {@code Use Local Connection} is on, the state is always sniffed from device
     * pool, so the triggered update has no effect
     *
     * @param requestType If {@link StateRequestType#GET_CACHED_STATE} allows to use cached state. Otherwise triggers
     *            device communications (if permitted by other settings)
     * @throws ArgoApiCommunicationException If communication with the device fails
     */
    private final void updateStateFromDevice(StateRequestType requestType) throws ArgoApiCommunicationException {
        if (deviceApi.isEmpty()) {
            return;
        }
        var devApi = deviceApi.orElseThrow();
        updateChannelsFromDevice(
                StateRequestType.GET_CACHED_STATE.equals(requestType) ? devApi.getLastStateReadFromDevice()
                        : devApi.queryDeviceForUpdatedState());
        updateThingProperties(devApi.getCurrentDeviceProperties());
    }

    /**
     * Start polling for device status at an interval configured via
     * {@link ArgoClimaBindingConstants#PARAMETER_REFRESH_INTERNAL} (in seconds)
     *
     * @implNote Since both {@link #initializeThing() initialize} as well as {@link #startAutomaticRefresh() refresh}
     *           are doing similar device communication, the 1st refresh cycle is purposefully omitted so that
     *           initialization has chances to finish. Ex. first refresh time is
     *           {@code Thing initialize time + refresh frequency (s)}. This is accomplished through a
     *           {@link #lastRefreshTime} member instead of simply delaying the scheduler, as it gives more flexibility
     * @implNote If N refreshes ({@link ArgoClimaBindingConstants#MAX_API_RETRIES})fail in a row, the Thing will be
     *           considered offline and require re-initialization on next refresh.
     * @implNote In order not to flood the device (or Argo servers), the binding is *not* doing any "obsessing" and
     *           ad-hoc retries of failed connections, but instead waits till next refresh cycle
     */
    private final synchronized void startAutomaticRefresh() {
        Runnable refresher = () -> {
            try {
                // Fail-safe: Do not trigger if time since last refresh is lower than frequency
                if (isMinimumRefreshTimeExceeded()) {
                    // If the device is offline, try to re-initialize it
                    if (getThing().getStatus() == ThingStatus.OFFLINE) {
                        logger.trace("{}: Re-initialize device", getThing().getUID());
                        initializeThing();
                        return;
                    }

                    updateStateFromDevice(StateRequestType.REQUEST_FRESH_STATE);
                    failedApiCallsCounter.set(0); // we're good!
                }
            } catch (RuntimeException | ArgoApiCommunicationException e) {
                var retryCount = failedApiCallsCounter.getAndIncrement() + 1; // 1-based
                logger.trace("[{}] Polling for device-side update for HVAC device failed [{} of {}]. Error=[{}]",
                        getThing().getUID(), retryCount, ArgoClimaBindingConstants.MAX_API_RETRIES, e.getMessage());
                if (retryCount >= ArgoClimaBindingConstants.MAX_API_RETRIES) {
                    var statusMsg = i18nProvider.getText("thing-status.argoclima.poll-failed",
                            "Polling for device-side update failed. Unable to communicate with HVAC device for past {0} refresh cycles. Last error: {1}",
                            ArgoClimaBindingConstants.MAX_API_RETRIES, e.getLocalizedMessage());

                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, statusMsg);
                    // Not resetting the counter here (will track every failure till we reinitialize & poll successfully
                }
            }
        };

        if (refreshTask.isEmpty()) {
            refreshTask = Optional.ofNullable(scheduler.scheduleWithFixedDelay(refresher, 0,
                    config.get().getRefreshInterval(), TimeUnit.SECONDS));
            logger.trace("{}: Automatic refresh started ({} second interval)", getThing().getUID().getId(),
                    config.get().getRefreshInterval());
        }
    }

    /**
     * Checks if time since last refresh is greater than interval (and advances the last checked time if so)
     *
     * @return True if time elapsed since last refresh is greater than interval (in which case last checked marker is
     *         also advanced). False - otherwise
     */
    private final boolean isMinimumRefreshTimeExceeded() {
        long currentTime = Instant.now().toEpochMilli();
        long timeSinceLastRefresh = currentTime - lastRefreshTime.get();
        if (timeSinceLastRefresh < config.get().getRefreshInterval() * 1000) {
            return false;
        }
        lastRefreshTime.lazySet(currentTime);
        return true;
    }

    /**
     * Synchronous initializer of the Thing. Expected to be called from a worker thread/future.
     * Performs device (or remote API) direct communication, unless explicitly disabled by settings
     *
     * @implNote In order not to flood the device (or Argo servers), the binding is *not* doing any "obsessing" and
     *           retries of failed connections, but instead waits till {@link #startAutomaticRefresh() refresher} to
     *           kick-off a retry (or a device-side pool happens, in intercepting/sniffing mode)
     * @implNote Since {@code RESET} is modeled as a write-only (one shot) configuration setting (in line with how Main
     *           UI handles those for other bindings, like ZWave), if it was set and the thing comes online... let's
     *           send the reset to the device and **CLEAR** the config property (so that we won't reset every time the
     *           device comes up)
     */
    private final void initializeThing() {
        if (this.config.get().getRefreshInterval() == 0) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NOT_YET_READY,
                    "@text/thing-status.argoclima.awaiting-request");
            return;
        }

        String message = "";
        try {
            var reachabilityTestResult = this.deviceApi.get().isReachable();

            if (reachabilityTestResult.isReachable()) {
                updateStatus(ThingStatus.ONLINE); // YAY!
                updateStateFromDevice(StateRequestType.GET_CACHED_STATE); // The reachability test actually was a full
                                                                          // protocolar message (b/c there's no other
                                                                          // :)), so it has conveniently fetched us all
                                                                          // updates. Let's use them now that the Thing
                                                                          // is healthy!

                // Handle reset config knob (if true) as one-shot command
                if (config.isPresent() && config.orElseThrow().resetToFactoryDefaults) {
                    var resetSent = this.deviceApi.get()
                            .handleSettingCommand(ArgoDeviceSettingType.RESET_TO_FACTORY_SETTINGS, OnOffType.ON);
                    if (resetSent) {
                        logger.info("[{}] Resetting HVAC device to factory defaults. RESET: {}", getThing().getUID(),
                                this.deviceApi.map(
                                        d -> d.getCurrentStateNoPoll(ArgoDeviceSettingType.RESET_TO_FACTORY_SETTINGS)
                                                .toString())
                                        .orElse(""));
                        sendCommandsToDeviceAwaitConfirmation(false); // Schedule sending to device

                        config.orElseThrow().resetToFactoryDefaults = false;

                        var configUpdated = editConfiguration();
                        configUpdated.put(ArgoClimaBindingConstants.PARAMETER_RESET_TO_FACTORY_DEFAULTS, false);
                        updateConfiguration(configUpdated); // Update (note this can't update text-based configs, so
                                                            // this would kick-off on every Thing reinitialize
                    }
                }
                return;
            }
            message = reachabilityTestResult.unreachabilityReason();
        } catch (Exception e) {
            // Since isReachable is a no-throw, hitting an exception (ex. during device-side message parsing) is very
            // unlikely, though in case a stray one happens -> let's embed it in the user-facing message
            logger.debug("{}: Initialization exception", getThing().getUID(), e);
            message = e.getLocalizedMessage();
        }

        if (getThing().getStatus() != ThingStatus.OFFLINE) {
            // Update to offline. If offline already, let's not update the reason not to flood framework with boring
            // updates (first error wins user's attention)
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }

    /**
     * Start sending pending commands to the device. Await their confirmation by the device (write-only parameters
     * faithfully confirm on send).
     * <p>
     * This method is async and starts a new update job. At most one job is supported (on re-entry, while an update is
     * running, the old update is stopped and replaced with the new one). Implementation does support staggering
     * requests though (ex. calling it multiple times in short time period will cause all updates to be sent in one go)
     *
     * @implNote In order to limit flapping and outgoing I/O, the actual work is delayed by
     *           {@link ArgoClimaBindingConstants.SEND_COMMAND_DEBOUNCE_TIME} to allow multiple commands to fit in one
     *           go. It's a naive implementation, starting/stopping a thread, but we're within a threadpool so this is
     *           ~fine :)
     *
     * @implNote On retries and confirmations: The commands are re-sent every {@link #sendCommandResubmitFrequency} and
     *           the device state is checked (for value confirmation) every {@link #sendCommandStatusPollFrequency}
     *           until either all withstanding commands are confirmed or total wait time expires. These
     *           timers are checked every tick which is by {@link ArgoClimaBindingConstants.SEND_COMMAND_DUTY_CYCLE}
     *
     * @implNote For local devices, this implementation can be called in a {@code Use Local Connection == false}
     *           ({@link ArgoClimaBindingConstants#PARAMETER_USE_LOCAL_CONNECTION ref}) mode. In this case, a "re-send"
     *           or a "update from device" commands are not really triggering any device communication (merely update
     *           internal statuses), and we're waiting the device to call us. For this reason, while the regular
     *           completion time (when we can talk to the device direct) is typically shorter
     *           ({@link #sendCommandMaxWaitTime}), in this mode this API will wait a
     *           {@link #sendCommandMaxWaitTimeIndirectMode}
     *
     * @param forceRefresh If true, force an active no-op("ping") command to the device to get freshest state
     */
    private final void sendCommandsToDeviceAwaitConfirmation(boolean forceRefresh) {
        if (sendCommandStatusPollFrequency.isNegative() || sendCommandResubmitFrequency.isNegative()
                || sendCommandMaxWaitTime.isNegative() || sendCommandMaxWaitTimeIndirectMode.isNegative()) {
            throw new IllegalArgumentException("The frequency cannot be negative");
        }

        // Note: While a lot of checks could be done before the thread launches, it deliberately has been moved to
        // WITHIN the thread, b/c this function may be called many times in case multiple items receive command at once

        Runnable commandSendWorker = () -> {
            // Stage0: Naive debounce (not to overflow the device if multiple commands are sent at once). We *want* to
            // get interrupted at this stage!
            try {
                Thread.sleep(ArgoClimaBindingConstants.SEND_COMMAND_DEBOUNCE_TIME.toMillis());
            } catch (InterruptedException e) {
                return; // Got interrupted while within debounce window (which was the point!)
            }

            // Stage1: Calculate what to do
            var valuesToUpdate = this.deviceApi.orElseThrow().getItemsWithPendingUpdates();
            logger.debug("[{}] Will UPDATE the following items: {}", getThing().getUID(), valuesToUpdate);

            var config = this.config.orElseThrow();
            var deviceApi = this.deviceApi.orElseThrow();
            final var maxWorkTime = config.useDirectConnection() ? sendCommandMaxWaitTime
                    : sendCommandMaxWaitTimeIndirectMode;
            final var giveUpTime = Instant.now().plus(maxWorkTime);
            var nextCommandSendTime = Objects.requireNonNull(Instant.MIN); // 1st send is instant
            var nextStateUpdateTime = Instant.now().plus(sendCommandStatusPollFrequency); // 1st poll is delayed
            Optional<Exception> lastException = Optional.empty();

            // Stage 2: Start spinnin' ;)
            while (true) { // Handles both polling as well as retries
                try {
                    // 2.1: Send command to the device
                    if (Instant.now().isAfter(nextCommandSendTime)) {
                        nextCommandSendTime = Instant.now().plus(sendCommandResubmitFrequency);
                        if (!deviceApi.hasPendingCommands()) {
                            if (forceRefresh) {
                                updateStateFromDevice(
                                        config.useDirectConnection() ? StateRequestType.REQUEST_FRESH_STATE
                                                : StateRequestType.GET_CACHED_STATE);
                            } else {
                                logger.trace("Nothing to update... skipping"); // update might have occurred async
                            }
                            return; // no command sending state to device was issued, we're safe to consider our job
                                    // D-O-N-E
                        }

                        if (config.useDirectConnection()) {
                            // Have a command and send it *now* (triggers I/O)
                            deviceApi.sendCommandsToDevice();
                        } else {
                            logger.trace(
                                    "Not sending the device update directly - waiting for device-side poll to happen");
                        }

                        // Check if the device confirmed in the same message exchange where we sent our update (very
                        // unlikely for 1st-time commands, but quite possible if we retried or the command was a
                        // write-only)
                        if (!this.deviceApi.get().hasPendingCommands()) {
                            logger.trace("All pending commands got confirmed on 1st try after a (re)send!");
                            return; // Woo-hoo! Device is happy, we're DONE!
                        }
                    }

                    if (!awaitConfirmationUponSendingCommands) {
                        return; // Nobody want's confirmations? Okay, we have less work to do (aka, we're done!)
                    }

                    // 2.2: Let's wait for the device to confirm flipping to the just commanded state
                    // Note: the device takes long to process commands which is why we're not querying immediately after
                    // send, and give it few seconds before re-confirming
                    if (Instant.now().isAfter(nextStateUpdateTime)) {
                        nextStateUpdateTime = Instant.now().plus(sendCommandStatusPollFrequency);
                        updateStateFromDevice(config.useDirectConnection() ? StateRequestType.REQUEST_FRESH_STATE
                                : StateRequestType.GET_CACHED_STATE);
                        if (this.deviceApi.get().hasPendingCommands()) {
                            // No biggie, we just didn't get the confirmation yet. This exception will be swallowed (on
                            // next try) or logged (if we run out of tries)
                            throw new ArgoApiProtocolViolationException("Update not confirmed. Value was not set");
                        }
                        return; // Woo-hoo! Device is happy, we're A-OK!
                    }
                    // empty loop cycle (no command, no update), just spinning...
                } catch (Exception ex) {
                    lastException = Optional.of(ex);
                }

                // 2.3: If we're still within the working window, let's roll the dice once more
                if (Instant.now().isBefore(giveUpTime)) {
                    try {
                        Thread.sleep(ArgoClimaBindingConstants.SEND_COMMAND_DUTY_CYCLE.toMillis());
                    } catch (InterruptedException e) {
                        return; // Cancelled during duty cycle (we want interrupts to happen here!)
                    }
                    logger.trace("Failed to update. Will retry...");
                    continue;
                }

                // 2.3B: Out of tries. Do one last check before we fail
                if (!this.deviceApi.get().hasPendingCommands()) {
                    logger.trace("All pending commands got confirmed on last try!");
                    return; // Woo-hoo! Device is happy, we're DONE!
                }

                // 2.4: Max time exceeded and update failed to send or not confirmed. Giving up :(
                valuesToUpdate.stream().forEach(x -> x.abortPendingCommand());
                updateChannelsFromDevice(deviceApi.getLastStateReadFromDevice()); // Update channels back to device
                                                                                  // values upon abort

                // The device wasn't nice with us, so we're going to consider it offline
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, i18nProvider.getText(
                        "thing-status.argoclima.confirmation-not-received",
                        "Could not control HVAC device. Command(s): {0} were not confirmed by the device within {1} s",
                        valuesToUpdate.isEmpty() ? "REFRESH" : valuesToUpdate.toString(), maxWorkTime.toSeconds()));
                logger.debug("[{}] Device command failed: {}", this.getThing().getUID().toString(),
                        lastException.map(ex -> ex.getMessage()).orElse("No error details"));
                break;
            }
        };

        synchronized (this) {
            cancelPendingDeviceCommandSenderJob();
            deviceCommandSenderFuture = Optional.ofNullable(scheduler.submit(commandSendWorker));
        }
    }

    private final synchronized void stopRefreshTask() {
        refreshTask.ifPresent(rt -> {
            rt.cancel(true);
        });
        refreshTask = Optional.empty();
    }

    private final synchronized void cancelPendingDeviceCommandSenderJob() {
        deviceCommandSenderFuture.ifPresent(x -> {
            if (!x.isDone()) {
                logger.trace("Cancelling previous update job");
                x.cancel(true);
            }
        });
        deviceCommandSenderFuture = Optional.empty();
    }
}
