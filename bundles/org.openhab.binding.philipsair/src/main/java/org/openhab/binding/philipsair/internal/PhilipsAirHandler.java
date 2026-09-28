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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
 *
 */
@NonNullByDefault
public class PhilipsAirHandler extends BaseThingHandler {
    /**
     * Channels only supported by some models (e.g. humidifiers), mapped to their channel group. They are added to the
     * thing once the device reports the corresponding value.
     */
    private static final Map<String, String> OPTIONAL_CHANNELS = Map.of(HUMIDITY_SETPOINT, CONTROLS, FUNCTION, CONTROLS,
            HUMIDITY, SENSORS, TEMPERATURE, SENSORS, WATER_LEVEL, SENSORS, WICKS_FILTER, FILTERS);
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private final Object connectionLock = new Object();
    // serializes updates from the refresh job, refresh commands and data pushed by the device
    private final Object updateLock = new Object();
    private volatile @Nullable PhilipsAirAPIConnection connection;
    private boolean disposed;
    private @Nullable PhilipsAirPurifierDataDTO currentData;
    private @Nullable PhilipsAirPurifierDeviceDTO deviceInfo;
    private @Nullable PhilipsAirPurifierFiltersDTO filters;
    private boolean filtersProbed;
    // commands and refreshes are executed in order on the scheduler, so handleCommand does not block
    private final Object commandLock = new Object();
    private CompletableFuture<@Nullable Void> commandQueue = CompletableFuture.completedFuture(null);
    private PhilipsAirConfiguration config;
    private final HttpClient httpClient;

    public PhilipsAirHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
        this.config = loadConfiguration();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        PhilipsAirAPIConnection connection = this.connection;
        if (connection == null) {
            return;
        }
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            enqueue(() -> updateData(connection));
        } else {
            PhilipsAirPurifierWritableDataDTO commandData = prepareCommandData(channelUID.getIdWithoutGroup(), command);
            if (commandData == null) {
                logger.debug("Ignoring unsupported command {} for {}", command, channelUID);
                return;
            }
            enqueue(() -> sendCommand(connection, channelUID, command, commandData));
        }
    }

    private void enqueue(Runnable task) {
        synchronized (commandLock) {
            commandQueue = commandQueue.thenRunAsync(() -> {
                if (isDisposed()) {
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

    private void sendCommand(PhilipsAirAPIConnection connection, ChannelUID channelUID, Command command,
            PhilipsAirPurifierWritableDataDTO commandData) {
        logger.debug("Sending {} as {}", channelUID.getId(), command);
        PhilipsAirPurifierDataDTO data = null;
        try {
            data = connection.sendCommand(channelUID.getIdWithoutGroup(), commandData);
        } catch (PhilipsAirAPIException e) {
            logger.debug("Sending {} to {} failed: {}", command, channelUID, e.getMessage());
        }
        synchronized (updateLock) {
            if (isDisposed()) {
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
                data.setLightLevel(intCommand);
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
                data.setAqit(intCommand);
                break;
            case HUMIDITY_SETPOINT:
                if (intCommand == null) {
                    return null;
                }
                data.setHumiditySetpoint(intCommand);
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
        synchronized (connectionLock) {
            disposed = false;
        }
        int refreshInterval = config.getRefreshInterval();
        logger.debug("Start refresh job for {} at interval {} sec.", thing.getUID(), refreshInterval);
        // the first run creates the connection, as the HTTP key exchange may block
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateThing, 0, refreshInterval, TimeUnit.SECONDS);
    }

    /**
     * Creates a new connection and publishes it, unless the handler was disposed in the meantime.
     *
     * @return the new connection, or null if the handler was disposed
     */
    private @Nullable PhilipsAirAPIConnection getConnection(PhilipsAirConfiguration config) {
        // created outside the lock, as the HTTP connection may block on the key exchange
        PhilipsAirAPIConnection newConnection = createConnection(config);
        PhilipsAirAPIConnection oldConnection;
        boolean published;
        synchronized (connectionLock) {
            published = !disposed;
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
            disposed = true;
            oldConnection = connection;
            connection = null;
        }
        if (oldConnection != null) {
            oldConnection.dispose();
        }
        super.dispose();
    }

    private void updateThing() {
        try {
            PhilipsAirAPIConnection connection = this.connection;
            if (connection == null) {
                connection = getConnection(config);
                // a device pushing its status reports it once the subscription is established
                if (connection == null || connection.isPushingStatus()) {
                    return;
                }
            } else {
                connection.ensureConnected();
            }
            updateData(connection);
        } catch (RuntimeException e) {
            // an uncaught exception would stop the scheduled refresh job
            logger.debug("Exception while updating thing {}: {}", thing.getUID(), e.getMessage());
            if (!isDisposed()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private boolean isDisposed() {
        synchronized (connectionLock) {
            return disposed;
        }
    }

    /**
     * Called by a connection when the device pushed a new status.
     */
    void dataReceived(PhilipsAirAPIConnection source) {
        synchronized (connectionLock) {
            if (disposed || !source.equals(connection)) {
                return;
            }
        }
        updateData(source);
    }

    void updateData(@Nullable PhilipsAirAPIConnection connection) {
        logger.trace("Update data for {}", thing.getUID());
        synchronized (updateLock) {
            boolean received;
            String error = "@text/offline.communication-error.no-response";
            try {
                received = requestData(connection);
            } catch (PhilipsAirAPIException | JsonSyntaxException e) {
                received = false;
                error = e.getLocalizedMessage();
            }
            // the request may have blocked while the handler was disposed
            if (isDisposed()) {
                return;
            }
            if (received && connection != null) {
                updateDeviceConfiguration(connection);
                addOptionalChannels();
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("No data received for {}: {}", thing.getUID(), error);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
            }
        }
    }

    private boolean requestData(@Nullable PhilipsAirAPIConnection connection) throws PhilipsAirAPIException {
        if (connection == null) {
            return false;
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
        } else if (!filtersProbed) {
            // the filter status is requested once to detect the optional wick filter channel
            filtersProbed = true;
            try {
                filters = connection.getAirPurifierFiltersStatus(host);
            } catch (PhilipsAirAPIException | JsonSyntaxException e) {
                logger.debug("Could not request the filter status of {}: {}", thing.getUID(), e.getMessage());
            }
        }

        if (data != null) {
            currentData = data;
        }

        if (deviceInfo != null) {
            this.deviceInfo = deviceInfo;
        }

        if (filters != null) {
            this.filters = filters;
        }

        return data != null || deviceInfo != null || filters != null;
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
        if (deviceInfo != null) {
            updateProperties(fillDeviceProperties(deviceInfo, editProperties()));
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
        for (Map.Entry<String, String> optionalChannel : OPTIONAL_CHANNELS.entrySet()) {
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

    private static boolean isReported(String channelId, @Nullable PhilipsAirPurifierDataDTO data,
            @Nullable PhilipsAirPurifierFiltersDTO filters) {
        if (WICKS_FILTER.equals(channelId)) {
            return filters != null && filters.getWickFilter() != null;
        }
        if (data == null) {
            return false;
        }
        return switch (channelId) {
            case HUMIDITY_SETPOINT -> data.getHumiditySetpoint() != null;
            case FUNCTION -> data.getFunction() != null;
            case HUMIDITY -> data.getHumidity() != null;
            case TEMPERATURE -> data.getTemperature() != null;
            case WATER_LEVEL -> data.getWaterLevel() != null;
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
                    return data.getHumiditySetpoint();
                case TEMPERATURE:
                    Float temperature = data.getTemperature();
                    return temperature != null
                            ? new QuantityType<>(temperature + config.getTemperatureOffset(), TEMPERATURE_UNIT)
                            : null;
                case FUNCTION:
                    return data.getFunction();
                case WATER_LEVEL:
                    return toPercent(data.getWaterLevel());
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

    private static Map<String, String> fillDeviceProperties(PhilipsAirPurifierDeviceDTO device,
            Map<String, String> properties) {
        properties.put(PROPERTY_VENDOR, PhilipsAirBindingConstants.VENDOR);
        // a null value would make updateProperties persist the thing on every update
        putIfNotNull(properties, PROPERTY_MODEL_ID, device.getModelId());
        putIfNotNull(properties, PROPERTY_FIRMWARE_VERSION, device.getSoftwareVersion());
        putIfNotNull(properties, PhilipsAirBindingConstants.PROPERTY_NAME, device.getName());

        return properties;
    }

    private static void putIfNotNull(Map<String, String> properties, String key, @Nullable String value) {
        if (value != null) {
            properties.put(key, value);
        }
    }
}
