/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.broadlinkthermostat.internal.handler;

import static org.openhab.binding.broadlinkthermostat.internal.BroadlinkThermostatBindingConstants.*;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.FloureonDevice;
import com.github.mob41.blapi.dev.hysen.AdvancedStatusInfo;
import com.github.mob41.blapi.dev.hysen.BaseStatusInfo;
import com.github.mob41.blapi.dev.hysen.SensorControl;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.cmd.hysen.SetTimeCommand;

/**
 * The {@link FloureonThermostatHandler} is responsible for handling thermostats labeled as Floureon Thermostat.
 *
 * @author Florian Mueller - Initial contribution
 */
@NonNullByDefault
public class FloureonThermostatHandler extends BroadlinkThermostatHandler {

    private final Logger logger = LoggerFactory.getLogger(FloureonThermostatHandler.class);
    private @Nullable FloureonDevice floureonDevice;

    private static final long CACHE_EXPIRY = TimeUnit.SECONDS.toSeconds(3);
    private final ExpiringCache<AdvancedStatusInfo> advancedStatusInfoExpiringCache = new ExpiringCache<>(CACHE_EXPIRY,
            this::refreshAdvancedStatus);

    /**
     * Creates a new instance of this class for the {@link FloureonThermostatHandler}.
     *
     * @param thing the thing that should be handled, not null
     */
    public FloureonThermostatHandler(Thing thing) {
        super(thing);
    }

    /**
     * Initializes a new instance of a {@link FloureonThermostatHandler}.
     */
    @Override
    public void initialize() {
        super.initialize();
        if (host != null && macAddress != null) {
            try {
                blDevice = new FloureonDevice(host, new Mac(macAddress));
                this.floureonDevice = (FloureonDevice) blDevice;
                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not find broadlinkthermostat device at host" + host + "with MAC+" + macAddress + ": "
                                + e.getMessage());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command: {}", command.toFullString());
        authenticate(false);

        if (command == RefreshType.REFRESH) {
            refreshData();
            return;
        }

        switch (channelUID.getIdWithoutGroup()) {
            case SETPOINT:
                handleSetpointCommand(channelUID, command);
                break;
            case POWER:
                handlePowerCommand(channelUID, command);
                break;
            case MODE:
                handleModeCommand(channelUID, command);
                break;
            case SENSOR:
                handleSensorCommand(channelUID, command);
                break;
            case REMOTE_LOCK:
                handleRemoteLockCommand(channelUID, command);
                break;
            case TIME:
                handleSetTimeCommand(channelUID, command);
                break;
            default:
                logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handlePowerCommand(ChannelUID channelUID, Command command) {
        FloureonDevice floureonDevice = this.floureonDevice;
        if (command instanceof OnOffType && floureonDevice != null) {
            try {
                floureonDevice.setPower(command == OnOffType.ON);
            } catch (Exception e) {
                logger.warn("Error while setting power of {} to {}: {}", thing.getUID(), command, e.getMessage());
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleModeCommand(ChannelUID channelUID, Command command) {
        FloureonDevice floureonDevice = this.floureonDevice;
        if (command instanceof StringType && floureonDevice != null) {
            try {
                if (MODE_AUTO.equals(command.toFullString())) {
                    floureonDevice.switchToAuto();
                } else {
                    floureonDevice.switchToManual();
                }
            } catch (Exception e) {
                logger.warn("Error while setting power off {} to {}: {}", thing.getUID(), command, e.getMessage());
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleSetpointCommand(ChannelUID channelUID, Command command) {
        FloureonDevice floureonDevice = this.floureonDevice;
        if (command instanceof QuantityType && floureonDevice != null) {
            try {
                QuantityType<?> temperatureQuantityType = ((QuantityType<?>) command).toUnit(SIUnits.CELSIUS);
                if (temperatureQuantityType != null) {
                    floureonDevice.setThermostatTemp(temperatureQuantityType.doubleValue());
                } else {
                    logger.warn("Could not convert {} to °C", command);
                }
            } catch (Exception e) {
                logger.warn("Error while setting setpoint of {} to {}: {}", thing.getUID(), command, e.getMessage());
            }

        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleSensorCommand(ChannelUID channelUID, Command command) {
        FloureonDevice floureonDevice = this.floureonDevice;
        if (command instanceof StringType && floureonDevice != null) {
            try {
                BaseStatusInfo statusInfo = floureonDevice.getBasicStatus();
                if (SENSOR_INTERNAL.equals(command.toFullString())) {
                    floureonDevice.setMode(statusInfo.getAutoMode(), statusInfo.getLoopMode(), SensorControl.INTERNAL);
                } else if (SENSOR_EXTERNAL.equals(command.toFullString())) {
                    floureonDevice.setMode(statusInfo.getAutoMode(), statusInfo.getLoopMode(), SensorControl.EXTERNAL);
                } else {
                    floureonDevice.setMode(statusInfo.getAutoMode(), statusInfo.getLoopMode(),
                            SensorControl.INTERNAL_TEMP_EXTERNAL_LIMIT);
                }
            } catch (Exception e) {
                logger.warn("Error while trying to set sensor mode {}: {}", command, e.getMessage());
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleRemoteLockCommand(ChannelUID channelUID, Command command) {
        FloureonDevice floureonDevice = this.floureonDevice;
        if (command instanceof OnOffType && floureonDevice != null) {
            try {
                floureonDevice.setLock(command == OnOffType.ON);
            } catch (Exception e) {
                logger.warn("Error while setting remote lock of {} to {}: {}", thing.getUID(), command, e.getMessage());
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleSetTimeCommand(ChannelUID channelUID, Command command) {
        if (command instanceof DateTimeType) {
            ZonedDateTime zonedDateTime = ((DateTimeType) command).getZonedDateTime();
            try {
                new SetTimeCommand(tob(zonedDateTime.getHour()), tob(zonedDateTime.getMinute()),
                        tob(zonedDateTime.getSecond()), tob(zonedDateTime.getDayOfWeek().getValue()))
                                .execute(floureonDevice);
            } catch (Exception e) {
                logger.warn("Error while setting time of {} to {}: {}", thing.getUID(), command, e.getMessage());
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    @Nullable
    private AdvancedStatusInfo refreshAdvancedStatus() {
        if (ThingStatus.ONLINE != thing.getStatus()) {
            return null;
        }

        FloureonDevice floureonDevice = this.floureonDevice;
        if (floureonDevice != null) {
            try {
                AdvancedStatusInfo advancedStatusInfo = floureonDevice.getAdvancedStatus();
                if (advancedStatusInfo == null) {
                    logger.warn("Device {} did not return any data. Trying to reauthenticate...", thing.getUID());
                    authenticate(true);
                    advancedStatusInfo = floureonDevice.getAdvancedStatus();
                }
                if (advancedStatusInfo == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device not responding.");
                    return null;
                }
                return advancedStatusInfo;
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error while retrieving data for " + thing.getUID() + ":  " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void refreshData() {

        AdvancedStatusInfo advancedStatusInfo = advancedStatusInfoExpiringCache.getValue();
        if (advancedStatusInfo == null) {
            return;
        }
        logger.trace("Retrieved data from device {}: {}", thing.getUID(), advancedStatusInfo);
        updateState(ROOM_TEMPERATURE, new QuantityType<>(advancedStatusInfo.getRoomTemp(), SIUnits.CELSIUS));
        updateState(ROOM_TEMPERATURE_EXTERNAL_SENSOR,
                new QuantityType<>(advancedStatusInfo.getExternalTemp(), SIUnits.CELSIUS));
        updateState(SETPOINT, new QuantityType<>(advancedStatusInfo.getThermostatTemp(), SIUnits.CELSIUS));
        updateState(POWER, OnOffType.from(advancedStatusInfo.getPower()));
        updateState(MODE, StringType.valueOf(advancedStatusInfo.getAutoMode() ? "auto" : "manual"));
        updateState(SENSOR, StringType.valueOf(advancedStatusInfo.getSensorControl().name()));
        updateState(TEMPERATURE_OFFSET, new QuantityType<>(advancedStatusInfo.getDif(), SIUnits.CELSIUS));
        updateState(ACTIVE, OnOffType.from(advancedStatusInfo.getActive()));
        updateState(REMOTE_LOCK, OnOffType.from(advancedStatusInfo.getRemoteLock()));
        updateState(TIME, new DateTimeType(getTimestamp(advancedStatusInfo)));
    }

    private ZonedDateTime getTimestamp(AdvancedStatusInfo advancedStatusInfo) {
        ZonedDateTime now = ZonedDateTime.now();
        return now.with(
                LocalTime.of(advancedStatusInfo.getHour(), advancedStatusInfo.getMin(), advancedStatusInfo.getSec()));
    }

    private static byte tob(int in) {
        return (byte) (in & 0xff);
    }
}
