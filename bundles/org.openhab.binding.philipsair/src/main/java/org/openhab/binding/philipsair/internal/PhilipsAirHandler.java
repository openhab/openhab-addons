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
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelKind;
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
 * @author Mmarcel Verpaalen- OH3 migration
 *
 */
@NonNullByDefault
public class PhilipsAirHandler extends BaseThingHandler {
    private static final long INITIAL_DELAY_IN_SECONDS = 10;
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable PhilipsAirAPIConnection connection;
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
                currentData = connection.sendCommand(channelUID.getIdWithoutGroup(), commandData);
            } catch (PhilipsAirAPIException e) {
                logger.debug("An exception occured", e);
            }
            updateChannels();
        }
    }

    public PhilipsAirPurifierWritableDataDTO prepareCommandData(String parameter, Command command) {
        OnOffType onOffCommand = null;
        DecimalType decimalCommand = null;
        String stringCommand = null;

        if (command instanceof OnOffType) {
            onOffCommand = (OnOffType) command;
        } else if (command instanceof DecimalType) {
            decimalCommand = (DecimalType) command;
        } else if (command instanceof StringType) {
            stringCommand = command.toString();
        }

        PhilipsAirPurifierWritableDataDTO data = new PhilipsAirPurifierWritableDataDTO();
        switch (parameter) {
            case LED_LIGHT_LEVEL:
                if (decimalCommand != null) {
                    data.setLightLevel(decimalCommand.intValue());
                }
                break;
            case DISPLAYED_INDEX:
                if (stringCommand != null) {
                    data.setDisplayIndex(stringCommand);
                }
                break;
            case BUTTONS_LIGHT:
                if (onOffCommand != null && onOffCommand.as(DecimalType.class) != null) {
                    data.setButtons(onOffCommand.as(DecimalType.class).toString());
                }
                break;
            case POWER:
                if (onOffCommand != null && onOffCommand.as(DecimalType.class) != null) {
                    data.setPower(onOffCommand.as(DecimalType.class).toString());
                }
                break;
            case FAN_MODE:
                if (stringCommand != null) {
                    data.setFanSpeed(stringCommand);
                }
                data.setMode("M");
                break;
            case CHILD_LOCK:
                if (onOffCommand != null && onOffCommand.as(DecimalType.class) != null) {
                    data.setChildLock(command == OnOffType.ON);
                }
                break;
            case AUTO_TIMEOFF:
                if (decimalCommand != null) {
                    data.setTimer(decimalCommand.intValue());
                }
                break;
            case MODE:
                if (stringCommand != null) {
                    data.setMode(stringCommand);
                }
                break;
            case AIR_QUALITY_NOTIFICATION_THRESHOLD:
                if (decimalCommand != null) {
                    data.setAqit(decimalCommand.intValue());
                }
                break;
            case HUMIDITY_SETPOINT:
                if (decimalCommand != null) {
                    data.setHumiditySetpoint(decimalCommand.intValue());
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

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        final PhilipsAirConfiguration config = getAirPurifierConfig();
        int refreshInterval = config.getRefreshInterval();
        if (refreshInterval < PhilipsAirConfiguration.MIN_REFESH_INTERVAL) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "refreshInterval too low");
            return;
        }

        this.getConfig().put(PhilipsAirConfiguration.CONFIG_DEF_REFRESH_INTERVAL, config.getRefreshInterval());
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            callback.configurationUpdated(thing);
        }
        updateStatus(ThingStatus.OFFLINE);
        scheduler.submit(() -> getConnection(config));
        final ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            logger.debug("Start refresh job at interval {} sec.", refreshInterval);
            this.refreshJob = scheduler.scheduleWithFixedDelay(this::updateThing, INITIAL_DELAY_IN_SECONDS,
                    refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void getConnection(PhilipsAirConfiguration config) {
        if (SUPPORTED_COAP_THING_TYPES_UIDS.contains(getThing().getThingTypeUID())) {
            logger.debug("Starting Coap based connectivity");
            connection = new PhilipsAirCoapAPIConnection(config);
        } else {
            logger.debug("Starting HTTP based connectivity");
            connection = new PhilipsAirHttpAPIConnection(config, httpClient);
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }

        super.dispose();
    }

    private void updateThing() {
        try {
            if (connection != null) {
                this.updateData(connection);
            } else {
                logger.debug("Cannot update Air Purifier device {}", thing.getUID());
                getConnection(getAirPurifierConfig());
            }
        } catch (Exception e) {
            logger.info("Exception while updating thing: {}", e.getMessage(), e);
        }
    }

    public void updateData(@Nullable PhilipsAirAPIConnection connection) {
        try {
            if (requestData(connection)) {
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (PhilipsAirAPIException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
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
            getAirPurifierConfig().setModelid(deviceInfo.getModelId());
            this.getConfig().put(PhilipsAirConfiguration.CONFIG_DEF_MODEL_ID, deviceInfo.getModelId()); // TODO : change
                                                                                                        // to
                                                                                                        // properties?
            this.getConfig().put(PhilipsAirConfiguration.CONFIG_KEY, connection.getConfig().getKey());
            Map<String, String> properties = fillDeviceProperties(deviceInfo, editProperties());
            updateProperties(properties);
            ThingHandlerCallback callback = getCallback();
            if (callback != null) {
                callback.configurationUpdated(thing);
            }
        }

        if (filters != null) {
            this.filters = filters;
        }

        return data != null || deviceInfo != null || filters != null;
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
                    return data.getButtons().equals("0") ? OnOffType.OFF : OnOffType.ON;
                case POWER:
                    return data.getPower().equals("0") ? OnOffType.OFF : OnOffType.ON;
                case PM25:
                    return new QuantityType<Density>(data.getPm25(), DENSITY_UNIT);
                case FAN_MODE:
                    return data.getFanSpeed();
                case CHILD_LOCK:
                    return data.getChildLock() ? OnOffType.ON : OnOffType.OFF;
                case AUTO_TIMEOFF:
                    return data.getTimer();
                case TIMER_COUNTDOWN:
                    return data.getTimerLeft();
                case MODE:
                    return data.getMode();
                case ALLERGEN_INDEX:
                    return data.getAllergenLevel();
                case AIR_QUALITY_NOTIFICATION_THRESHOLD:
                    return data.getAqit();
                case ERROR_CODE:
                    return String.valueOf(data.getErrorCode());
                case HUMIDITY:
                    return new QuantityType<Dimensionless>(
                            data.getHumidity() + getAirPurifierConfig().getHumidityOffset(), HUMIDITY_UNIT);
                case HUMIDITY_SETPOINT:
                    return data.getHumiditySetpoint();
                case TEMPERATURE:
                    return new QuantityType<>(data.getTemperature() + getAirPurifierConfig().getTemperatureOffset(),
                            TEMPERATURE_UNIT);
                case FUNCTION:
                    return data.getFunction();
                case WATER_LEVEL:
                    return data.getWaterLevel();
            }

            if (deviceInfo != null) {
                switch (field) {
                    case SOFTWARE_VERSION:
                        return deviceInfo.getSoftwareVersion();

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
        }

        return null;
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
