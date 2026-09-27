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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;

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

/**
 * The {@link PhilipsAirHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michal Boronski - Initial contribution
 * @author Marcel Verpaalen - OH3 migration
 * @author Marcel Verpaalen - Add optional channels reported by the device
 * @author Marcel Verpaalen - Release the connection on dispose
 *
 */
@NonNullByDefault
public class PhilipsAirHandler extends BaseThingHandler {
    private static final long INITIAL_DELAY_IN_SECONDS = 10;
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
    private boolean disposed = true;
    private @Nullable PhilipsAirPurifierDataDTO currentData;
    private @Nullable PhilipsAirPurifierDeviceDTO deviceInfo;
    private @Nullable PhilipsAirPurifierFiltersDTO filters;
    private final HttpClient httpClient;

    public PhilipsAirHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        PhilipsAirAPIConnection connection = this.connection;
        if (connection == null) {
            return;
        }
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData(connection);
        } else {
            logger.debug("Sending {} as {}", channelUID.getId(), command.toString());
            PhilipsAirPurifierWritableDataDTO commandData = prepareCommandData(channelUID.getIdWithoutGroup(), command);
            try {
                PhilipsAirPurifierDataDTO data = connection.sendCommand(channelUID.getIdWithoutGroup(), commandData);
                if (data != null) {
                    currentData = data;
                }
            } catch (PhilipsAirAPIException e) {
                logger.debug("An exception occured", e);
            }
            updateChannels();
        }
    }

    public PhilipsAirPurifierWritableDataDTO prepareCommandData(String parameter, Command command) {
        OnOffType onOffCommand = command instanceof OnOffType onOff ? onOff : null;
        String stringCommand = command instanceof StringType ? command.toString() : null;
        Integer intCommand = toInteger(command);

        PhilipsAirPurifierWritableDataDTO data = new PhilipsAirPurifierWritableDataDTO();
        switch (parameter) {
            case LED_LIGHT_LEVEL:
                if (intCommand != null) {
                    data.setLightLevel(intCommand);
                }
                break;
            case DISPLAYED_INDEX:
                if (stringCommand != null) {
                    data.setDisplayIndex(stringCommand);
                }
                break;
            case BUTTONS_LIGHT:
                if (onOffCommand != null) {
                    data.setButtons(onOffCommand == OnOffType.ON ? "1" : "0");
                }
                break;
            case POWER:
                if (onOffCommand != null) {
                    data.setPower(onOffCommand == OnOffType.ON ? "1" : "0");
                }
                break;
            case FAN_MODE:
                if (stringCommand != null) {
                    data.setFanSpeed(stringCommand);
                }
                data.setMode("M");
                break;
            case CHILD_LOCK:
                if (onOffCommand != null) {
                    data.setChildLock(onOffCommand == OnOffType.ON);
                }
                break;
            case AUTO_TIMEOFF:
                if (intCommand != null) {
                    data.setTimer(intCommand);
                }
                break;
            case MODE:
                if (stringCommand != null) {
                    data.setMode(stringCommand);
                }
                break;
            case AIR_QUALITY_NOTIFICATION_THRESHOLD:
                if (intCommand != null) {
                    data.setAqit(intCommand);
                }
                break;
            case HUMIDITY_SETPOINT:
                if (intCommand != null) {
                    data.setHumiditySetpoint(intCommand);
                }
                break;
            case FUNCTION:
                if (stringCommand != null) {
                    data.setFunction(stringCommand);
                }
                break;
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
        logger.debug("Start initializing!");
        final PhilipsAirConfiguration config = getAirPurifierConfig();
        int refreshInterval = config.getRefreshInterval();
        if (refreshInterval < PhilipsAirConfiguration.MIN_REFRESH_INTERVAL
                && !(SUPPORTED_COAP_THING_TYPES_UIDS.contains(getThing().getThingTypeUID()))) {
            logger.debug("refreshInterval too low. Using {}", PhilipsAirConfiguration.MIN_REFRESH_INTERVAL);
            refreshInterval = PhilipsAirConfiguration.MIN_REFRESH_INTERVAL;
        }
        updateStatus(ThingStatus.UNKNOWN);
        synchronized (connectionLock) {
            disposed = false;
        }
        scheduler.submit(() -> getConnection(config));
        final ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            logger.debug("Start refresh job at interval {} sec.", refreshInterval);
            this.refreshJob = scheduler.scheduleWithFixedDelay(this::updateThing, INITIAL_DELAY_IN_SECONDS,
                    refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void getConnection(PhilipsAirConfiguration config) {
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
        if (published) {
            newConnection.ensureConnected();
        }
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
            if (connection != null) {
                connection.ensureConnected();
                this.updateData(connection);
            } else {
                logger.debug("Cannot update Air Purifier device {}", thing.getUID());
                getConnection(getAirPurifierConfig());
            }
        } catch (RuntimeException e) {
            logger.debug("Exception while updating thing {}: {}", thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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

    public void updateData(@Nullable PhilipsAirAPIConnection connection) {
        logger.trace("Update data for {}", thing.getUID());
        synchronized (updateLock) {
            try {
                if (requestData(connection)) {
                    addOptionalChannels();
                    updateChannels();
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    logger.debug("No data received for {}", thing.getUID());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No response from device");
                }
            } catch (PhilipsAirAPIException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        }
    }

    protected boolean requestData(@Nullable PhilipsAirAPIConnection connection) throws PhilipsAirAPIException {
        if (connection == null) {
            return false;
        }

        String host = getAirPurifierConfig().getHost();
        PhilipsAirPurifierDeviceDTO deviceInfo = connection.getAirPurifierDevice(host);
        PhilipsAirPurifierDataDTO data = connection.getAirPurifierStatus(host);
        PhilipsAirPurifierFiltersDTO filters = null;
        List<Channel> filterGroup = thing.getChannelsOfGroup(PhilipsAirBindingConstants.FILTERS);
        if (filterGroup.stream().anyMatch(fg -> isLinked(fg.getUID()))) {
            filters = connection.getAirPurifierFiltersStatus(host);
        }

        if (data != null) {
            currentData = data;
        }

        if (deviceInfo != null) {
            this.deviceInfo = deviceInfo;
            Configuration configuration = editConfiguration();
            boolean changed = updateConfigValue(configuration, PhilipsAirConfiguration.CONFIG_DEF_MODEL_ID,
                    deviceInfo.getModelId());
            changed |= updateConfigValue(configuration, PhilipsAirConfiguration.CONFIG_KEY,
                    connection.getConfig().getKey());
            if (changed) {
                updateConfiguration(configuration);
            }
            updateProperties(fillDeviceProperties(deviceInfo, editProperties()));
        }

        if (filters != null) {
            this.filters = filters;
        }

        return data != null || deviceInfo != null || filters != null;
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
        if (value == null || value.equals(configuration.get(key))) {
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

    protected void updateChannel(ChannelUID channelUID, @Nullable PhilipsAirPurifierDataDTO data,
            @Nullable PhilipsAirPurifierDeviceDTO deviceInfo, @Nullable PhilipsAirPurifierFiltersDTO filters) {
        if (getCallback() != null && isLinked(channelUID)) {
            Object value;
            try {
                value = getValue(channelUID, data, deviceInfo, filters);
            } catch (Exception e) {
                logger.debug("AirPurifier doesn't provide '{}' measurement. To avoid this message unlink  channel: {}",
                        channelUID.getId(), channelUID.getAsString());
                return;
            }

            State state = UnDefType.NULL;

            if (value instanceof OnOffType) {
                state = (OnOffType) value;
            } else if (value instanceof QuantityType<?>) {
                state = (QuantityType<?>) value;
            } else if (value instanceof Integer) {
                state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
            } else if (value instanceof String) {
                state = new StringType(value.toString());
            } else if (value != null) {
                logger.warn("Update channel {}: Unsupported value type {}", channelUID,
                        value.getClass().getSimpleName());
            }

            updateState(channelUID, state);
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    public @Nullable Object getValue(ChannelUID channelUID, @Nullable PhilipsAirPurifierDataDTO data,
            @Nullable PhilipsAirPurifierDeviceDTO deviceInfo, @Nullable PhilipsAirPurifierFiltersDTO filters) {
        String field = channelUID.getIdWithoutGroup();

        if (data != null) {
            switch (field) {
                case LED_LIGHT_LEVEL:
                    return data.getLightLevel();
                case DISPLAYED_INDEX:
                    return data.getDisplayIndex();
                case BUTTONS_LIGHT:
                    return toOnOff(data.getButtons());
                case POWER:
                    return toOnOff(data.getPower());
                case PM25:
                    return new QuantityType<Density>(data.getPm25(), DENSITY_UNIT);
                case FAN_MODE:
                    return data.getFanSpeed();
                case CHILD_LOCK:
                    Boolean childLock = data.getChildLock();
                    return childLock != null ? OnOffType.from(childLock) : null;
                case AUTO_TIMEOFF:
                    return data.getTimer();
                case TIMER_COUNTDOWN:
                    return new QuantityType<>(data.getTimerLeft(), Units.MINUTE);
                case MODE:
                    return data.getMode();
                case ALLERGEN_INDEX:
                    return data.getAllergenLevel();
                case AIR_QUALITY_NOTIFICATION_THRESHOLD:
                    return data.getAqit();
                case ERROR_CODE:
                    return String.valueOf(data.getErrorCode());
                case HUMIDITY:
                    Float humidity = data.getHumidity();
                    return humidity != null
                            ? new QuantityType<Dimensionless>(humidity + getAirPurifierConfig().getHumidityOffset(),
                                    HUMIDITY_UNIT)
                            : null;
                case HUMIDITY_SETPOINT:
                    return data.getHumiditySetpoint();
                case TEMPERATURE:
                    Float temperature = data.getTemperature();
                    return temperature != null
                            ? new QuantityType<>(temperature + getAirPurifierConfig().getTemperatureOffset(),
                                    TEMPERATURE_UNIT)
                            : null;
                case FUNCTION:
                    return data.getFunction();
                case WATER_LEVEL:
                    return data.getWaterLevel();
            }
        }

        if (filters != null) {
            switch (field) {
                case PRE_FILTER:
                    return filters.getPreFilter();
                case WICKS_FILTER:
                    return filters.getWickFilter();
                case CARBON_FILTER:
                    return filters.getCarbonFilter();
                case HEPA_FILTER:
                    return filters.getHepaFilter();
            }
        }

        return null;
    }

    private static @Nullable OnOffType toOnOff(@Nullable String value) {
        return value != null ? OnOffType.from(!"0".equals(value)) : null;
    }

    public PhilipsAirConfiguration getAirPurifierConfig() {
        return getConfigAs(PhilipsAirConfiguration.class);
    }

    private static Map<String, String> fillDeviceProperties(PhilipsAirPurifierDeviceDTO device,
            Map<String, String> properties) {
        properties.put(PROPERTY_VENDOR, PhilipsAirBindingConstants.VENDOR);
        if (device != null) {
            properties.put(PROPERTY_MODEL_ID, device.getModelId());
            properties.put(PROPERTY_FIRMWARE_VERSION, device.getSoftwareVersion());
            properties.put(PhilipsAirBindingConstants.PROPERTY_NAME, device.getName());
        }

        return properties;
    }
}
