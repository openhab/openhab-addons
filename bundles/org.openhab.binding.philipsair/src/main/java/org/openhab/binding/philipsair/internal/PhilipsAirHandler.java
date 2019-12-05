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
package org.openhab.binding.philipsair.internal;

import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.AQIL;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.AQIT;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.CARBON_FILTER;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.CL;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.DDP;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.DT;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.DTRS;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.ERR;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.FUNC;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.HEPA_FILTER;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.IAQL;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.MODE;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.OM;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.PM25;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.POWER;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.PRE_FILTER;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.RH;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.RHSET;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.SWVERSION;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.TEMP;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.UIL;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.WICKS_FILTER;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.WL;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIConnection;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIException;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierData;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDevice;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhilipsAirHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michal Boronski - Initial contribution
 */
@NonNullByDefault
public class PhilipsAirHandler extends BaseThingHandler {
    private static final long INITIAL_DELAY_IN_SECONDS = 5;
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) PhilipsAirAPIConnection connection;
    private @NonNullByDefault({}) PhilipsAirConfiguration config;
    private @Nullable PhilipsAirPurifierData currentData;
    private @Nullable PhilipsAirPurifierDevice deviceInfo;
    private @Nullable PhilipsAirPurifierFilters filters;
    private final HttpClient httpClient;

    public PhilipsAirHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData(connection);
        } else {
            logger.info("Sending {} as {}", channelUID.getId(), command.toString());
            String field = channelUID.getId();
            if (field.contains("#")) {
                field = field.split("#")[1];
            }
            currentData = sendCommand(field, command);
            updateChannels();
        }
    }

    public @Nullable PhilipsAirPurifierData sendCommand(String parameter, Command command) {
        Object value = null;
        if (command instanceof OnOffType) {
            if (parameter.equals(CL)) {
                value = ((OnOffType) command) == OnOffType.ON ? "true" : "false";
            }
            value = ((OnOffType) command) == OnOffType.ON ? "1" : "0";
        } else if (command instanceof DecimalType) {
            value = ((DecimalType) command).intValue();
        } else if (command instanceof StringType) {
            value = command.toString();
        }

        if (value != null) {
            try {
                return connection.sendCommand(parameter, value);
            } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException
                    | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                    | InvalidAlgorithmParameterException e) {
                logger.error("An exception occured", e);
            }
        }

        return null;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(PhilipsAirConfiguration.class);
        boolean configValid = true;

        int refreshInterval = config.getRefreshInterval();
        if (refreshInterval < PhilipsAirConfiguration.MIN_REFESH_INTERVAL) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "refreshInterval too low");
            configValid = false;
        }

        if (configValid) {
            this.getConfig().put(PhilipsAirConfiguration.CONFIG_DEF_REFRESH_INTERVAL, config.getRefreshInterval());
            ThingHandlerCallback callback = getCallback();
            if (callback != null) {
                callback.configurationUpdated(thing);
            }

            connection = new PhilipsAirAPIConnection(config, httpClient);
            updateStatus(ThingStatus.UNKNOWN);
            if (refreshJob == null || refreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} sec.", refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateThing, INITIAL_DELAY_IN_SECONDS,
                        refreshInterval, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    private void updateThing() {
        ThingStatus status = ThingStatus.OFFLINE;

        if (connection != null) {
            this.updateData(connection);
            status = thing.getStatus();
        } else {
            logger.debug("Cannot update Air Purifier device {}", thing.getUID());
            status = ThingStatus.OFFLINE;
        }

        updateStatus(status);
    }

    public void updateData(PhilipsAirAPIConnection connection) {
        try {
            if (requestData(connection)) {
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (PhilipsAirAPIException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    protected boolean requestData(PhilipsAirAPIConnection connection) throws PhilipsAirAPIException {
        try {
            PhilipsAirPurifierDevice info = connection.getAirPurifierDevice(config.getHost());
            PhilipsAirPurifierData data = connection.getAirPurifierStatus(config.getHost());
            PhilipsAirPurifierFilters filters = null;
            List<Channel> filterGroup = thing.getChannelsOfGroup(PhilipsAirBindingConstants.FILTERS);
            if(filterGroup != null && filterGroup.stream().anyMatch( fg -> isLinked(fg.getUID()))) {
                filters = connection.getAirPurifierFiltersStatus(config.getHost());
            }

            if (data != null) {
                currentData = data;
            }

            if (info != null) {
                this.deviceInfo = info;
                config.setModelid(info.getModelid());
                this.getConfig().put(PhilipsAirConfiguration.CONFIG_DEF_MODEL_ID, config.getModelid());
                this.getConfig().put(PhilipsAirConfiguration.CONFIG_KEY, config.getKey());
                ThingHandlerCallback callback = getCallback();
                if (callback != null) {
                    callback.configurationUpdated(thing);
                }
            }

            if (filters != null) {
                this.filters = filters;
            }

            return data != null || info != null || filters != null;
        } catch (Exception exc) {
            logger.error("An exception occured", exc);
            throw exc;
        }
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

    protected void updateChannel(ChannelUID channelUID, @Nullable PhilipsAirPurifierData data,
            @Nullable PhilipsAirPurifierDevice deviceInfo, @Nullable PhilipsAirPurifierFilters filters) {
        if (getCallback() != null && isLinked(channelUID)) {
            Object value;
            try {
                value = getValue(channelUID.getAsString(), data, deviceInfo, filters);
            } catch (Exception e) {
                logger.debug("AirPurifier doesn't provide {} measurement", channelUID.getAsString().toUpperCase());
                return;
            }

            State state = org.eclipse.smarthome.core.types.UnDefType.NULL;
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

    public @Nullable Object getValue(String channelId, @Nullable PhilipsAirPurifierData data,
            @Nullable PhilipsAirPurifierDevice deviceInfo, @Nullable PhilipsAirPurifierFilters filters)
            throws Exception {
        String[] fields = StringUtils.split(channelId, ":");
        String field = fields[fields.length - 1];
        if (field.contains("#")) {
            field = field.split("#")[1];
        }

        if (data != null) {
            switch (field) {
            case AQIL:
                return data.getLightLevel();
            case DDP:
                return Integer.toString(data.getDisplayIndex());
            case UIL:
                return data.getButtons() == 0 ? OnOffType.OFF : OnOffType.ON;
            case POWER:
                return data.getPower() == 0 ? OnOffType.OFF : OnOffType.ON;
            case PM25:
                return data.getPm25();
            case OM:
                return data.getFanSpeed();
            case CL:
                return data.getChildLock() ? OnOffType.ON : OnOffType.OFF;
            case DT:
                return data.getTimer();
            case DTRS:
                return data.getTimerLeft();
            case MODE:
                return data.getMode();
            case IAQL:
                return data.getAllergenLevel();
            case AQIT:
                return data.getAqit();
            case ERR:
                return data.getErrorCode();
            case RH:
                return data.getHumidity();
            case RHSET:
                return data.getHumiditySetpoint();
            case TEMP:
                return data.getTemperature();
            case FUNC:
                return data.getFunction();
            case WL:
                return data.getWaterLevel();
            }

            if (deviceInfo != null) {
                switch (field) {
                case SWVERSION:
                    deviceInfo.getSwversion();
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
        return config;
    }
}
