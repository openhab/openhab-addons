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
package org.openhab.binding.foobot.internal.handler;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.foobot.internal.FoobotApiConnector;
import org.openhab.binding.foobot.internal.FoobotApiException;
import org.openhab.binding.foobot.internal.FoobotBindingConstants;
import org.openhab.binding.foobot.internal.json.FoobotDevice;
import org.openhab.binding.foobot.internal.json.FoobotJsonData;
import org.openhab.binding.foobot.internal.json.FoobotSensor;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FoobotDeviceHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Divya Chauhan - Initial contribution
 * @author George Katsis - Add Bridge thing type
 *
 */
@NonNullByDefault
public class FoobotDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FoobotDeviceHandler.class);
    private final FoobotApiConnector connector;

    private @NonNullByDefault({}) ExpiringCache<FoobotJsonData> dataCache;
    private String uuid = "";

    public FoobotDeviceHandler(final Thing thing, final FoobotApiConnector connector) {
        super(thing);
        this.connector = connector;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            final FoobotJsonData sensorData = dataCache.getValue();

            if (sensorData != null) {
                updateState(channelUID, sensorDataToState(channelUID.getId(), sensorData));
            }
        } else {
            logger.debug("The Foobot binding is read-only and can not handle command {}", command);
        }
    }

    /**
     * @return Returns the uuid associated with this device.
     */
    public String getUuid() {
        return uuid;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Foobot handler.");
        uuid = (String) getConfig().get(FoobotBindingConstants.CONFIG_UUID);

        if (uuid.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter 'uuid' is mandatory and must be configured");
            return;
        }
        final FoobotAccountHandler bridgeHandler = getBridgeHandler();
        final int refreshInterval = bridgeHandler == null ? FoobotBindingConstants.DEFAULT_REFRESH_PERIOD_MINUTES
                : bridgeHandler.getRefreshInterval();

        dataCache = new ExpiringCache<>(Duration.ofMinutes(refreshInterval), this::retrieveSensorData);
        scheduler.execute(this::refreshSensors);
    }

    /**
     * Updates the thing properties as retrieved by the bridge.
     *
     * @param foobot device parameters.
     */
    public void handleUpdateProperties(final FoobotDevice foobot) {
        final Map<String, String> properties = editProperties();

        properties.put(Thing.PROPERTY_MAC_ADDRESS, foobot.getMac());
        properties.put(FoobotBindingConstants.PROPERTY_NAME, foobot.getName());
        updateProperties(properties);
    }

    /**
     * Calls the footbot api to retrieve the sensor data. Sets thing offline in case of errors.
     *
     * @return returns the retrieved sensor data or null if no data or an error occurred.
     */
    private @Nullable FoobotJsonData retrieveSensorData() {
        logger.debug("Refresh sensor data for: {}", uuid);
        FoobotJsonData sensorData = null;

        try {
            sensorData = connector.getSensorData(uuid);
            if (sensorData == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No sensor data received");
                return sensorData;
            }
        } catch (FoobotApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return null;
        } catch (RuntimeException e) {
            logger.debug("Error requesting sensor data: ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            return null;
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        final FoobotAccountHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler != null) {
            bridgeHandler.updateRemainingLimitStatus();
        }
        return sensorData;
    }

    /**
     * Refreshes the device channels.
     */
    public void refreshSensors() {
        final FoobotJsonData sensorData = dataCache.getValue();

        if (sensorData != null) {
            for (final Channel channel : getThing().getChannels()) {
                final ChannelUID channelUid = channel.getUID();

                updateState(channelUid, sensorDataToState(channelUid.getId(), sensorData));
            }
            updateTime(sensorData);
        }
    }

    private void updateTime(final FoobotJsonData sensorData) {
        final State lastTime = sensorDataToState(FoobotSensor.TIME.getChannelId(), sensorData);

        if (lastTime instanceof DecimalType) {
            ((DecimalType) lastTime).intValue();
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Foobot handler.");
    }

    protected State sensorDataToState(final String channelId, final FoobotJsonData data) {
        final FoobotSensor sensor = FoobotSensor.findSensorByChannelId(channelId);

        if (sensor == null || data.getSensors() == null || data.getDatapoints() == null
                || data.getDatapoints().isEmpty()) {
            return UnDefType.UNDEF;
        }
        final int sensorIndex = data.getSensors().indexOf(sensor.getDataKey());

        if (sensorIndex == -1) {
            return UnDefType.UNDEF;
        }
        final String value = data.getDatapoints().get(0).get(sensorIndex);
        final String unit = data.getUnits().get(sensorIndex);

        if (value == null) {
            return UnDefType.UNDEF;
        } else {
            final Unit<?> stateUnit = sensor.getUnit(unit);
            if (sensor == FoobotSensor.TIME) {
                return new DateTimeType(
                        ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(value)), ZoneId.systemDefault()));
            } else if (stateUnit == null) {
                return new DecimalType(value);
            } else {
                return new QuantityType(new BigDecimal(value), stateUnit);
            }
        }
    }

    private @Nullable FoobotAccountHandler getBridgeHandler() {
        return getBridge() != null && getBridge().getHandler() instanceof FoobotAccountHandler
                ? (FoobotAccountHandler) getBridge().getHandler()
                : null;
    }
}
