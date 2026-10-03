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
package org.openhab.binding.philipsair.internal;

import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.philipsair.internal.connection.CoapProfile;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIConnection;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIException;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirCoapAPIConnection;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirHttpAPIConnection;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link PhilipsAirHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michal Boronski - Initial contribution
 * @author Marcel Verpaalen - OH3 migration
 * @author Marcel Verpaalen - Add optional channels reported by the device
 * @author Marcel Verpaalen - Release the connection on dispose
 * @author Marcel Verpaalen - Execute commands asynchronously
 * @author Marcel Verpaalen - Model specific displayed index and threshold options
 *
 */
@NonNullByDefault
public class PhilipsAirHandler extends BaseThingHandler {
    /**
     * Channels only supported by some models (e.g. humidifiers), mapped to their channel group. They are added to the
     * thing once the device reports the corresponding value.
     */
    private static final List<Map.Entry<String, String>> OPTIONAL_CHANNELS = List.of(Map.entry(AUTO_TIMEOFF, CONTROLS),
            Map.entry(TIMER_COUNTDOWN, CONTROLS), Map.entry(HUMIDITY_SETPOINT, CONTROLS), Map.entry(FUNCTION, CONTROLS),
            Map.entry(HUMIDITY, SENSORS), Map.entry(TEMPERATURE, SENSORS), Map.entry(WATER_LEVEL, SENSORS),
            Map.entry(TVOC, SENSORS), Map.entry(RSSI, SENSORS), Map.entry(WICKS_FILTER, FILTERS),
            Map.entry(BEEP, CONTROLS_UI), Map.entry(DISPLAY_BRIGHTNESS, CONTROLS_UI), Map.entry(LAMP_MODE, CONTROLS_UI),
            Map.entry(DISPLAY, CONTROLS_UI), Map.entry(STANDBY_SENSORS, CONTROLS), Map.entry(ALLERGY_SLEEP, CONTROLS));
    /**
     * Model id prefixes of the devices that can show the gas (TVOC) index on the display, as offered by the Philips
     * app.
     */
    private static final List<String> GAS_INDEX_MODELS = List.of("AC45", "AC6675", "AC56", "MS3", "MS4");
    private static final String DISPLAYED_INDEX_ALLERGEN = "0";
    private static final String DISPLAYED_INDEX_PM25 = "1";
    private static final String DISPLAYED_INDEX_GAS = "2";
    /**
     * The air quality notification thresholds (good, fair, poor, very poor) as offered by the Philips app. The AC4373
     * and AC4375 use other values, which they expect as text.
     */
    private static final List<String> THRESHOLD_LABELS = List.of("Good", "Fair", "Poor", "Very poor");
    private static final List<Integer> THRESHOLDS = List.of(1, 4, 7, 10);
    private static final List<Integer> TEXT_THRESHOLDS = List.of(13, 19, 29, 40);
    private static final List<String> TEXT_THRESHOLD_MODELS = List.of("AC4373", "AC4375");
    /**
     * The options of the channels as defined by the channel types, which are set again when the options of a device
     * profile are not used anymore.
     */
    private static final List<StateOption> DEFAULT_FAN_OPTIONS = List.of(new StateOption("s", "Silent"),
            new StateOption("1", "1"), new StateOption("2", "2"), new StateOption("3", "3"),
            new StateOption("t", "Turbo"));
    private static final List<StateOption> DEFAULT_MODE_OPTIONS = List.of(new StateOption("P", "Auto"),
            new StateOption("A", "Allergen"), new StateOption("S", "Sleep"), new StateOption("M", "Manual"),
            new StateOption("B", "Bacteria"), new StateOption("N", "Night"));
    private static final List<StateOption> DEFAULT_TIMER_OPTIONS = List.of(new StateOption("0", "Off"),
            new StateOption("1", "1 h"), new StateOption("2", "2 h"), new StateOption("3", "3 h"),
            new StateOption("4", "4 h"), new StateOption("5", "5 h"));
    /**
     * The filter status of devices that are not polled for it is requested this many times to detect the optional wick
     * filter channel, in case a request fails.
     */
    private static final int MAX_FILTER_PROBE_ATTEMPTS = 3;
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirHandler.class);
    private volatile @Nullable ScheduledFuture<?> refreshJob;
    private final Object connectionLock = new Object();
    // serializes updates from the refresh job, refresh commands and data pushed by the device
    private final Object updateLock = new Object();
    private volatile @Nullable PhilipsAirAPIConnection connection;
    // incremented on dispose, so work that started before is not published after the handler was initialized again
    private long generation;
    private @Nullable PhilipsAirPurifierDataDTO currentData;
    private volatile @Nullable PhilipsAirPurifierDeviceDTO deviceInfo;
    private @Nullable PhilipsAirPurifierFiltersDTO filters;
    // the host the data above was received from, it is discarded when the thing is configured for another device
    private String dataHost = "";
    private int filterProbeAttempts;
    // the status is requested outside the update lock, so a request that started earlier may finish later
    private long requestSequence;
    private long appliedSequence;
    // commands and refreshes are executed in order on the scheduler, so handleCommand does not block
    private final Object commandLock = new Object();
    private CompletableFuture<@Nullable Void> commandQueue = CompletableFuture.completedFuture(null);
    private volatile PhilipsAirConfiguration config;
    private final HttpClient httpClient;
    private final PhilipsAirStateDescriptionOptionProvider stateDescriptionProvider;
    private boolean modelOptionsSet;
    // the channels that have the options of a device profile, and the profile these are the options of
    private final Set<ChannelUID> profileOptionChannels = new HashSet<>();
    private @Nullable CoapProfile optionsProfile;

    public PhilipsAirHandler(Thing thing, HttpClient httpClient,
            PhilipsAirStateDescriptionOptionProvider stateDescriptionProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.config = loadConfiguration();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        PhilipsAirAPIConnection connection;
        long generation;
        synchronized (connectionLock) {
            connection = this.connection;
            generation = this.generation;
        }
        if (connection == null) {
            logger.debug("Ignoring {} for {}, there is no connection", command, channelUID);
            return;
        }
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            enqueue(generation, () -> updateData(connection, generation));
        } else {
            PhilipsAirPurifierWritableDataDTO commandData = prepareCommandData(channelUID.getIdWithoutGroup(), command);
            if (commandData == null) {
                logger.debug("Ignoring unsupported command {} for {}", command, channelUID);
                return;
            }
            enqueue(generation, () -> sendCommand(connection, generation, channelUID, command, commandData));
        }
    }

    private void enqueue(long generation, Runnable task) {
        synchronized (commandLock) {
            commandQueue = commandQueue.thenRunAsync(() -> {
                if (!isCurrent(generation)) {
                    return;
                }
                try {
                    task.run();
                } catch (RuntimeException e) {
                    // a failed stage would skip all following commands
                    logger.warn("Unexpected error while handling a command for {}", thing.getUID(), e);
                }
            }, scheduler);
        }
    }

    private void sendCommand(PhilipsAirAPIConnection connection, long generation, ChannelUID channelUID,
            Command command, PhilipsAirPurifierWritableDataDTO commandData) {
        logger.debug("Sending {} as {}", channelUID.getId(), command);
        PhilipsAirPurifierDataDTO data = null;
        try {
            data = connection.sendCommand(channelUID.getIdWithoutGroup(), commandData);
        } catch (PhilipsAirAPIException e) {
            logger.debug("Sending {} to {} failed: {}", command, channelUID, e.getMessage());
        }
        synchronized (updateLock) {
            if (!isCurrent(generation)) {
                return;
            }
            if (data != null) {
                currentData = data;
            }
            updateChannels();
        }
    }

    /**
     * @return the data to send for the command, or null if the channel does not accept the command
     */
    @Nullable
    PhilipsAirPurifierWritableDataDTO prepareCommandData(String parameter, Command command) {
        OnOffType onOffCommand = command instanceof OnOffType onOff ? onOff : null;
        String stringCommand = command instanceof StringType ? command.toString() : null;
        Integer intCommand = toInteger(command);

        PhilipsAirPurifierWritableDataDTO data = new PhilipsAirPurifierWritableDataDTO();
        switch (parameter) {
            case LED_LIGHT_LEVEL:
                if (intCommand == null) {
                    return null;
                }
                data.setLightLevel(toLightLevel(intCommand));
                break;
            case DISPLAYED_INDEX:
                if (stringCommand == null) {
                    return null;
                }
                data.setDisplayIndex(stringCommand);
                break;
            case BUTTONS_LIGHT:
                if (onOffCommand == null) {
                    return null;
                }
                data.setButtons(onOffCommand == OnOffType.ON ? "1" : "0");
                break;
            case POWER:
                if (onOffCommand == null) {
                    return null;
                }
                data.setPower(onOffCommand == OnOffType.ON ? "1" : "0");
                break;
            case FAN_MODE:
                if (stringCommand == null) {
                    return null;
                }
                data.setFanSpeed(stringCommand);
                data.setMode("M");
                break;
            case CHILD_LOCK:
                if (onOffCommand == null) {
                    return null;
                }
                data.setChildLock(onOffCommand == OnOffType.ON);
                break;
            case AUTO_TIMEOFF:
                if (intCommand == null) {
                    return null;
                }
                data.setTimer(intCommand);
                break;
            case MODE:
                if (stringCommand == null) {
                    return null;
                }
                data.setMode(stringCommand);
                break;
            case AIR_QUALITY_NOTIFICATION_THRESHOLD:
                if (intCommand == null) {
                    return null;
                }
                if (hasTextThresholds(deviceInfo)) {
                    data.setAqit(intCommand.toString());
                } else {
                    data.setAqit(intCommand);
                }
                break;
            case HUMIDITY_SETPOINT:
                if (intCommand == null) {
                    return null;
                }
                data.setHumiditySetpoint(intCommand);
                break;
            case BEEP:
                if (onOffCommand == null) {
                    return null;
                }
                data.setBeep(onOffCommand == OnOffType.ON);
                break;
            case STANDBY_SENSORS:
                if (onOffCommand == null) {
                    return null;
                }
                data.setStandbySensors(onOffCommand == OnOffType.ON);
                break;
            case ALLERGY_SLEEP:
                if (onOffCommand == null) {
                    return null;
                }
                data.setAllergySleep(onOffCommand == OnOffType.ON);
                break;
            case DISPLAY:
                if (onOffCommand == null) {
                    return null;
                }
                data.setDisplayOn(onOffCommand == OnOffType.ON);
                break;
            case DISPLAY_BRIGHTNESS:
                if (stringCommand == null) {
                    return null;
                }
                data.setDisplayBrightness(stringCommand);
                break;
            case LAMP_MODE:
                if (stringCommand == null) {
                    return null;
                }
                data.setLampMode(stringCommand);
                break;
            case FUNCTION:
                if (stringCommand == null) {
                    return null;
                }
                data.setFunction(stringCommand);
                break;
            default:
                return null;
        }
        return data;
    }

    /**
     * Converts numeric commands to the integer value the device expects. Dimensionless quantities (e.g. a humidity
     * setpoint sent as {@code 0.5} or {@code 50 %}) are converted to percent.
     */
    private static @Nullable Integer toInteger(Command command) {
        if (command instanceof DecimalType decimal) {
            return decimal.intValue();
        } else if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> percent = quantity.toUnit(Units.PERCENT);
            return (percent != null ? percent : quantity).intValue();
        }
        return null;
    }

    /**
     * The device only supports the light levels 0, 25, 50, 75 and 100 %, so other values are rounded to the nearest
     * supported level.
     */
    private static int toLightLevel(int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        return Math.round(clamped / 25f) * 25;
    }

    @Override
    public void initialize() {
        PhilipsAirConfiguration config = loadConfiguration();
        this.config = config;
        if (config.getHost().isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error.missing-host");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        synchronized (updateLock) {
            if (!config.getHost().equals(dataHost)) {
                currentData = null;
                deviceInfo = null;
                filters = null;
                filterProbeAttempts = 0;
                dataHost = config.getHost();
            }
        }
        long generation = currentGeneration();
        modelOptionsSet = false;
        int refreshInterval = config.getRefreshInterval();
        logger.debug("Start refresh job for {} at interval {} sec.", thing.getUID(), refreshInterval);
        ScheduledFuture<?> previousJob = refreshJob;
        if (previousJob != null) {
            previousJob.cancel(true);
        }
        // the first run creates the connection, as the HTTP key exchange may block
        refreshJob = scheduler.scheduleWithFixedDelay(() -> poll(generation), 0, refreshInterval, TimeUnit.SECONDS);
    }

    /**
     * Creates a new connection and publishes it, unless the handler was disposed in the meantime.
     *
     * @return the new connection, or null if the handler was disposed
     */
    private @Nullable PhilipsAirAPIConnection getConnection(PhilipsAirConfiguration config, long generation) {
        // created outside the lock, as the HTTP connection may block on the key exchange
        PhilipsAirAPIConnection newConnection = createConnection(config);
        PhilipsAirAPIConnection oldConnection;
        boolean published;
        synchronized (connectionLock) {
            published = this.generation == generation;
            if (published) {
                oldConnection = connection;
                connection = newConnection;
            } else {
                oldConnection = newConnection;
            }
        }
        if (oldConnection != null) {
            oldConnection.dispose();
        }
        if (!published) {
            return null;
        }
        newConnection.ensureConnected();
        return newConnection;
    }

    PhilipsAirAPIConnection createConnection(PhilipsAirConfiguration config) {
        if (SUPPORTED_COAP_THING_TYPES_UIDS.contains(getThing().getThingTypeUID())) {
            logger.debug("Starting Coap based connectivity");
            return new PhilipsAirCoapAPIConnection(config, this::dataReceived);
        } else {
            logger.debug("Starting HTTP based connectivity");
            return new PhilipsAirHttpAPIConnection(config, httpClient);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
        synchronized (commandLock) {
            commandQueue.cancel(false);
            commandQueue = CompletableFuture.completedFuture(null);
        }
        PhilipsAirAPIConnection oldConnection;
        synchronized (connectionLock) {
            generation++;
            oldConnection = connection;
            connection = null;
        }
        if (oldConnection != null) {
            oldConnection.dispose();
        }
        super.dispose();
    }

    private void poll(long generation) {
        try {
            PhilipsAirAPIConnection connection = this.connection;
            if (connection == null) {
                connection = getConnection(config, generation);
                // a device pushing its status reports it once the subscription is established
                if (connection == null || connection.isPushingStatus()) {
                    return;
                }
            } else {
                connection.ensureConnected();
            }
            updateData(connection, generation);
        } catch (RuntimeException e) {
            // an uncaught exception would stop the scheduled refresh job
            logger.debug("Exception while updating thing {}: {}", thing.getUID(), e.getMessage());
            if (isCurrent(generation)) {
                String message = e.getMessage();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        message != null ? message : "@text/offline.communication-error.no-response");
            }
        }
    }

    private long currentGeneration() {
        synchronized (connectionLock) {
            return generation;
        }
    }

    private boolean isCurrent(long generation) {
        return currentGeneration() == generation;
    }

    /**
     * Called by a connection when the device pushed a new status.
     */
    void dataReceived(PhilipsAirAPIConnection source) {
        long generation;
        synchronized (connectionLock) {
            if (!source.equals(connection)) {
                return;
            }
            generation = this.generation;
        }
        // not on the thread of the connection, as updating calls the framework
        scheduler.execute(() -> updateData(source, generation));
    }

    void updateData(@Nullable PhilipsAirAPIConnection connection) {
        updateData(connection, currentGeneration());
    }

    private void updateData(@Nullable PhilipsAirAPIConnection connection, long generation) {
        logger.trace("Update data for {}", thing.getUID());
        long sequence;
        synchronized (updateLock) {
            sequence = ++requestSequence;
        }
        DeviceStatus status = null;
        @Nullable
        String error = null;
        try {
            status = requestData(connection);
        } catch (PhilipsAirAPIException | JsonSyntaxException e) {
            error = e.getLocalizedMessage();
        }
        synchronized (updateLock) {
            // the request may have blocked while the handler was disposed
            if (!isCurrent(generation) || sequence < appliedSequence) {
                return;
            }
            appliedSequence = sequence;
            if (status != null && !status.isEmpty() && connection != null) {
                if (status.data() != null) {
                    currentData = status.data();
                }
                if (status.deviceInfo() != null) {
                    deviceInfo = status.deviceInfo();
                }
                if (status.filters() != null) {
                    filters = status.filters();
                }
                updateDeviceConfiguration(connection);
                addOptionalChannels();
                setModelOptions();
                updateProfileOptions(connection);
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("No data received for {}: {}", thing.getUID(), error != null ? error : "no status");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        error != null ? error : "@text/offline.communication-error.no-response");
            }
        }
    }

    /**
     * Requests the status from the device. This blocks, so it must not be called while holding a lock.
     *
     * @return the received values, or null if there is no connection
     */
    private @Nullable DeviceStatus requestData(@Nullable PhilipsAirAPIConnection connection)
            throws PhilipsAirAPIException {
        if (connection == null) {
            return null;
        }

        String host = config.getHost();
        // the device info is static, so it is only requested once
        PhilipsAirPurifierDeviceDTO deviceInfo = this.deviceInfo == null ? connection.getAirPurifierDevice(host) : null;
        PhilipsAirPurifierDataDTO data = connection.getAirPurifierStatus(host);
        PhilipsAirPurifierFiltersDTO filters = null;
        List<Channel> filterGroup = thing.getChannelsOfGroup(PhilipsAirBindingConstants.FILTERS);
        if (connection.isPushingStatus() || filterGroup.stream().anyMatch(fg -> isLinked(fg.getUID()))) {
            // pushing devices report the filter status with the status, so it is available without an extra request
            filters = connection.getAirPurifierFiltersStatus(host);
        } else if (startFilterProbe()) {
            // the filter status is requested to detect the optional wick filter channel, until the device answers
            try {
                filters = connection.getAirPurifierFiltersStatus(host);
                synchronized (updateLock) {
                    filterProbeAttempts = MAX_FILTER_PROBE_ATTEMPTS;
                }
            } catch (PhilipsAirAPIException | JsonSyntaxException e) {
                logger.debug("Could not request the filter status of {}: {}", thing.getUID(), e.getMessage());
            }
        }
        return new DeviceStatus(deviceInfo, data, filters);
    }

    private boolean startFilterProbe() {
        synchronized (updateLock) {
            if (filterProbeAttempts >= MAX_FILTER_PROBE_ATTEMPTS) {
                return false;
            }
            filterProbeAttempts++;
            return true;
        }
    }

    private record DeviceStatus(@Nullable PhilipsAirPurifierDeviceDTO deviceInfo,
            @Nullable PhilipsAirPurifierDataDTO data, @Nullable PhilipsAirPurifierFiltersDTO filters) {
        boolean isEmpty() {
            return deviceInfo == null && data == null && filters == null;
        }
    }

    /**
     * Persists the key the connection may have (re)negotiated and publishes the device properties.
     */
    private void updateDeviceConfiguration(PhilipsAirAPIConnection connection) {
        Configuration configuration = editConfiguration();
        if (updateConfigValue(configuration, PhilipsAirConfiguration.CONFIG_KEY, connection.getConfig().getKey())) {
            updateConfiguration(configuration);
        }
        PhilipsAirPurifierDeviceDTO deviceInfo = this.deviceInfo;
        PhilipsAirPurifierFiltersDTO filters = this.filters;
        CoapProfile profile = connection.getDeviceProfile();
        if (deviceInfo != null || filters != null) {
            Map<String, String> properties = editProperties();
            if (deviceInfo != null) {
                fillDeviceProperties(deviceInfo, properties);
            }
            if (profile != null && profile != CoapProfile.CLASSIC) {
                properties.put(PROPERTY_DEVICE_PROFILE, profile.name());
            }
            if (filters != null) {
                putIfNotNull(properties, PROPERTY_PRE_FILTER_TYPE, filters.getPreFilterType());
                putIfNotNull(properties, PROPERTY_HEPA_FILTER_TYPE, filters.getHepaFilterType());
                putIfNotNull(properties, PROPERTY_CARBON_FILTER_TYPE, filters.getCarbonFilterType());
            }
            updateProperties(properties);
        }
    }

    /**
     * Adds the optional channels the device reports but the thing does not have yet. Channels are never removed, as a
     * value missing from a single response does not prove the device lacks the feature.
     */
    private void addOptionalChannels() {
        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            return;
        }
        PhilipsAirPurifierDataDTO data = currentData;
        PhilipsAirPurifierFiltersDTO filters = this.filters;
        ThingBuilder thingBuilder = null;
        for (Map.Entry<String, String> optionalChannel : OPTIONAL_CHANNELS) {
            String channelId = optionalChannel.getKey();
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), optionalChannel.getValue(), channelId);
            if (getThing().getChannel(channelUID) == null && isReported(channelId, data, filters)) {
                if (thingBuilder == null) {
                    thingBuilder = editThing();
                }
                logger.debug("Adding channel {} reported by {}", channelUID, getThing().getUID());
                thingBuilder.withChannel(
                        callback.createChannelBuilder(channelUID, new ChannelTypeUID(BINDING_ID, channelId)).build());
            }
        }
        if (thingBuilder != null) {
            updateThing(thingBuilder.build());
        }
    }

    /**
     * Limits the options of the displayed index and the air quality notification threshold to the values the model
     * supports. This is decided once after the device reported its status, as a single report may lack optional
     * fields. The options depend on the model, so they are not decided before the device info is known.
     */
    private void setModelOptions() {
        PhilipsAirPurifierDeviceDTO deviceInfo = this.deviceInfo;
        if (modelOptionsSet || deviceInfo == null) {
            return;
        }
        modelOptionsSet = true;

        List<StateOption> indexOptions = new ArrayList<>(
                List.of(new StateOption(DISPLAYED_INDEX_ALLERGEN, "Allergen Index"),
                        new StateOption(DISPLAYED_INDEX_PM25, "PM2.5")));
        if (supportsGasIndex(deviceInfo, currentData)) {
            indexOptions.add(new StateOption(DISPLAYED_INDEX_GAS, "Gas"));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), CONTROLS_UI, DISPLAYED_INDEX),
                indexOptions);

        List<Integer> thresholds = hasTextThresholds(deviceInfo) ? TEXT_THRESHOLDS : THRESHOLDS;
        List<StateOption> thresholdOptions = new ArrayList<>();
        for (int i = 0; i < thresholds.size(); i++) {
            thresholdOptions.add(new StateOption(thresholds.get(i).toString(), THRESHOLD_LABELS.get(i)));
        }
        stateDescriptionProvider.setStateOptions(
                new ChannelUID(thing.getUID(), SENSORS, AIR_QUALITY_NOTIFICATION_THRESHOLD), thresholdOptions);
        logger.debug("Options of {}: displayed index {}, threshold {}", thing.getUID(), indexOptions, thresholdOptions);
    }

    /**
     * Sets the options of the settings the device profile offers, when the profile is not the one the options were set
     * for. The profile is detected from the status of the device or selected in the configuration.
     */
    private void updateProfileOptions(PhilipsAirAPIConnection connection) {
        CoapProfile profile = connection.getDeviceProfile();
        if (profile == optionsProfile) {
            return;
        }
        optionsProfile = profile;
        setProfileOptions(CONTROLS, FAN_MODE, profile != null ? profile.getFanSpeedOptions() : List.of(),
                DEFAULT_FAN_OPTIONS);
        setProfileOptions(CONTROLS, MODE, profile != null ? profile.getModeOptions() : List.of(), DEFAULT_MODE_OPTIONS);
        setProfileOptions(CONTROLS, AUTO_TIMEOFF, profile != null ? profile.getTimerOptions() : List.of(),
                DEFAULT_TIMER_OPTIONS);
        // these channels only exist for the profiles that have them, so they have no default options
        setProfileOptions(CONTROLS_UI, DISPLAY_BRIGHTNESS,
                profile != null ? profile.getDisplayBrightnessOptions() : List.of(), List.of());
        setProfileOptions(CONTROLS_UI, LAMP_MODE, profile != null ? profile.getLampModeOptions() : List.of(),
                List.of());
        logger.debug("Profile of {}: {}", thing.getUID(), profile);
    }

    /**
     * Sets the options a profile offers for a channel of the controls. A channel that had the options of another
     * profile before, because the profile changed, gets the default options again, as options cannot be removed.
     */
    private void setProfileOptions(String group, String channelId, List<StateOption> profileOptions,
            List<StateOption> defaultOptions) {
        ChannelUID channelUID = new ChannelUID(thing.getUID(), group, channelId);
        if (!profileOptions.isEmpty()) {
            stateDescriptionProvider.setStateOptions(channelUID, profileOptions);
            profileOptionChannels.add(channelUID);
        } else if (profileOptionChannels.remove(channelUID)) {
            stateDescriptionProvider.setStateOptions(channelUID, defaultOptions);
        }
    }

    static boolean supportsGasIndex(@Nullable PhilipsAirPurifierDeviceDTO deviceInfo,
            @Nullable PhilipsAirPurifierDataDTO data) {
        String model = getModel(deviceInfo);
        if (model != null && GAS_INDEX_MODELS.stream().anyMatch(model::startsWith)) {
            return true;
        }
        // models unknown to the Philips app are recognized by their gas sensor
        return data != null && data.getTvoc() != null;
    }

    static boolean hasTextThresholds(@Nullable PhilipsAirPurifierDeviceDTO deviceInfo) {
        String model = getModel(deviceInfo);
        return model != null && TEXT_THRESHOLD_MODELS.stream().anyMatch(model::startsWith);
    }

    /**
     * @return the upper case model id, or the device type if the device reports no model id
     */
    private static @Nullable String getModel(@Nullable PhilipsAirPurifierDeviceDTO deviceInfo) {
        if (deviceInfo == null) {
            return null;
        }
        String model = deviceInfo.getModelId();
        if (model == null || model.isBlank()) {
            model = deviceInfo.getType();
        }
        return model != null ? model.toUpperCase(Locale.ROOT) : null;
    }

    private static boolean isReported(String channelId, @Nullable PhilipsAirPurifierDataDTO data,
            @Nullable PhilipsAirPurifierFiltersDTO filters) {
        if (WICKS_FILTER.equals(channelId)) {
            return filters != null && filters.getWickFilter() != null;
        }
        if (data == null) {
            return false;
        }
        return switch (channelId) {
            case AUTO_TIMEOFF -> data.getTimer() != null;
            case TIMER_COUNTDOWN -> data.getTimerLeft() != null;
            case HUMIDITY_SETPOINT -> data.getHumiditySetpoint() != null;
            case FUNCTION -> data.getFunction() != null;
            case HUMIDITY -> data.getHumidity() != null;
            case TEMPERATURE -> data.getTemperature() != null;
            case WATER_LEVEL -> data.getWaterLevel() != null;
            case TVOC -> data.getTvoc() != null;
            case RSSI -> data.getRssi() != null;
            case BEEP -> data.getBeep() != null;
            case STANDBY_SENSORS -> data.getStandbySensors() != null;
            case ALLERGY_SLEEP -> data.getAllergySleep() != null;
            case DISPLAY -> data.getDisplayOn() != null;
            case DISPLAY_BRIGHTNESS -> data.getDisplayBrightness() != null;
            case LAMP_MODE -> data.getLampMode() != null;
            default -> false;
        };
    }

    private static boolean updateConfigValue(Configuration configuration, String key, @Nullable String value) {
        if (value == null || value.isEmpty() || value.equals(configuration.get(key))) {
            return false;
        }
        configuration.put(key, value);
        return true;
    }

    private void updateChannels() {
        if (getCallback() != null) {
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                if (ChannelKind.STATE.equals(channel.getKind()) && isLinked(channelUID)) {
                    updateChannel(channelUID, currentData, deviceInfo, filters);
                }
            }
        }
    }

    private void updateChannel(ChannelUID channelUID, @Nullable PhilipsAirPurifierDataDTO data,
            @Nullable PhilipsAirPurifierDeviceDTO deviceInfo, @Nullable PhilipsAirPurifierFiltersDTO filters) {
        Object value = getValue(channelUID, data, deviceInfo, filters);
        State state = UnDefType.NULL;
        if (value instanceof State stateValue) {
            state = stateValue;
        } else if (value instanceof Integer intValue) {
            state = new DecimalType(BigDecimal.valueOf(intValue.longValue()));
        } else if (value instanceof String stringValue) {
            state = new StringType(stringValue);
        } else if (value != null) {
            logger.warn("Update channel {}: Unsupported value type {}", channelUID, value.getClass().getSimpleName());
        }
        updateState(channelUID, state);
    }

    @Nullable
    Object getValue(ChannelUID channelUID, @Nullable PhilipsAirPurifierDataDTO data,
            @Nullable PhilipsAirPurifierDeviceDTO deviceInfo, @Nullable PhilipsAirPurifierFiltersDTO filters) {
        String field = channelUID.getIdWithoutGroup();

        if (data != null) {
            switch (field) {
                case LED_LIGHT_LEVEL:
                    return toPercent(data.getLightLevel());
                case DISPLAYED_INDEX:
                    return data.getDisplayIndex();
                case BUTTONS_LIGHT:
                    return toOnOff(data.getButtons());
                case POWER:
                    return toOnOff(data.getPower());
                case PM25:
                    Integer pm25 = data.getPm25();
                    return pm25 != null ? new QuantityType<Density>(pm25, DENSITY_UNIT) : null;
                case FAN_MODE:
                    return data.getFanSpeed();
                case CHILD_LOCK:
                    Boolean childLock = data.getChildLock();
                    return childLock != null ? OnOffType.from(childLock) : null;
                case AUTO_TIMEOFF:
                    return data.getTimer();
                case TIMER_COUNTDOWN:
                    Integer timerLeft = data.getTimerLeft();
                    return timerLeft != null ? new QuantityType<>(timerLeft, Units.MINUTE) : null;
                case MODE:
                    return data.getMode();
                case ALLERGEN_INDEX:
                    return data.getAllergenLevel();
                case AIR_QUALITY_NOTIFICATION_THRESHOLD:
                    return data.getAqit();
                case ERROR_CODE:
                    Integer errorCode = data.getErrorCode();
                    return errorCode != null ? errorCode.toString() : null;
                case HUMIDITY:
                    Float humidity = data.getHumidity();
                    return humidity != null
                            ? new QuantityType<Dimensionless>(humidity + config.getHumidityOffset(), HUMIDITY_UNIT)
                            : null;
                case HUMIDITY_SETPOINT:
                    return toPercent(data.getHumiditySetpoint());
                case TEMPERATURE:
                    Float temperature = data.getTemperature();
                    return temperature != null
                            ? new QuantityType<>(temperature + config.getTemperatureOffset(), TEMPERATURE_UNIT)
                            : null;
                case FUNCTION:
                    return data.getFunction();
                case WATER_LEVEL:
                    return toPercent(data.getWaterLevel());
                case TVOC:
                    return data.getTvoc();
                case RSSI:
                    Integer rssi = data.getRssi();
                    return rssi != null ? new QuantityType<>(rssi, Units.DECIBEL_MILLIWATTS) : null;
                case BEEP:
                    return toOnOff(data.getBeep());
                case STANDBY_SENSORS:
                    return toOnOff(data.getStandbySensors());
                case ALLERGY_SLEEP:
                    return toOnOff(data.getAllergySleep());
                case DISPLAY:
                    return toOnOff(data.getDisplayOn());
                case DISPLAY_BRIGHTNESS:
                    return data.getDisplayBrightness();
                case LAMP_MODE:
                    return data.getLampMode();
            }
        }

        if (filters != null) {
            switch (field) {
                case PRE_FILTER:
                    return toHours(filters.getPreFilter());
                case WICKS_FILTER:
                    return toHours(filters.getWickFilter());
                case CARBON_FILTER:
                    return toHours(filters.getCarbonFilter());
                case HEPA_FILTER:
                    return toHours(filters.getHepaFilter());
            }
        }

        return null;
    }

    private static @Nullable OnOffType toOnOff(@Nullable String value) {
        return value != null ? OnOffType.from(!"0".equals(value)) : null;
    }

    private static @Nullable OnOffType toOnOff(@Nullable Boolean value) {
        return value != null ? OnOffType.from(value) : null;
    }

    private static @Nullable QuantityType<Dimensionless> toPercent(@Nullable Integer value) {
        return value != null ? new QuantityType<>(value, Units.PERCENT) : null;
    }

    private static @Nullable QuantityType<Time> toHours(@Nullable Integer value) {
        return value != null ? new QuantityType<>(value, Units.HOUR) : null;
    }

    private PhilipsAirConfiguration loadConfiguration() {
        PhilipsAirConfiguration config = getConfigAs(PhilipsAirConfiguration.class);
        if (config.getRefreshInterval() < PhilipsAirConfiguration.MIN_REFRESH_INTERVAL) {
            config.setRefreshInterval(PhilipsAirConfiguration.MIN_REFRESH_INTERVAL);
        }
        return config;
    }

    private static void fillDeviceProperties(PhilipsAirPurifierDeviceDTO device, Map<String, String> properties) {
        properties.put(PROPERTY_VENDOR, PhilipsAirBindingConstants.VENDOR);
        // a null value would make updateProperties persist the thing on every update
        putIfNotNull(properties, PROPERTY_MODEL_ID, device.getModelId());
        putIfNotNull(properties, PROPERTY_FIRMWARE_VERSION, device.getSoftwareVersion());
        putIfNotNull(properties, PhilipsAirBindingConstants.PROPERTY_NAME, device.getName());
    }

    private static void putIfNotNull(Map<String, String> properties, String key, @Nullable String value) {
        if (value != null) {
            properties.put(key, value);
        }
    }
}
