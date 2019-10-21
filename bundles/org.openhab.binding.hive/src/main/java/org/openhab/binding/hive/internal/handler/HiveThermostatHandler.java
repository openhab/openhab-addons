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
package org.openhab.binding.hive.internal.handler;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.dto.HiveAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chris Foot - Initial contribution
 */
@NonNullByDefault
public class HiveThermostatHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(HiveThermostatHandler.class);
    protected int failureCount = 0;
    @Nullable protected HiveBridgeHandler bridgeHandler;
    protected OnOffType boostCache = OnOffType.OFF;

    public HiveThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        bridgeHandler = getHiveBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.requestRefresh();
        }
    }

    protected void updateChannel() {
        HiveAttributes reading = getThermostatReading();

        if (!reading.isValid) {
            failureCount++;
            if (failureCount > 2) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to get the status of your thermostat, this may be a temporary problem with the HIVE api");
            }
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        Channel temperatureChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_CURRENT_TEMPERATURE);
        if (temperatureChannel != null) {
            updateState(temperatureChannel.getUID().getId(), new DecimalType(reading.temperature.reportedValue));
        }

        Channel targetChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_TARGET_TEMPERATURE);
        if (targetChannel != null) {
            updateState(targetChannel.getUID().getId(), new DecimalType(reading.targetHeatTemperature.reportedValue));
        }

        Channel heatingOnChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_HEATING_ON);
        if (heatingOnChannel != null) {
            updateState(heatingOnChannel.getUID().getId(), OnOffType.valueOf(reading.stateHeatingRelay.reportedValue));
        }

        Channel boostOnChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_BOOST);
        if (boostOnChannel != null) {
            if (reading.activeHeatCoolMode.reportedValue.equals("BOOST")) {
                boostCache = OnOffType.ON;
                updateState(boostOnChannel.getUID().getId(), OnOffType.ON);
            } else {
                boostCache = OnOffType.OFF;
                updateState(boostOnChannel.getUID().getId(), OnOffType.OFF);
            }
        }

        Channel hotWaterOnChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_HOTWATER_ON);
        if (hotWaterOnChannel != null) {
            if (reading.stateHotWaterRelay != null) {
                updateState(hotWaterOnChannel.getUID().getId(),
                        OnOffType.valueOf(reading.stateHotWaterRelay.reportedValue));
            }
        }

        Channel thermostatBatteryChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_THERMOSTAT_BATTERY);
        if (thermostatBatteryChannel != null) {
            updateState(thermostatBatteryChannel.getUID().getId(),
                    DecimalType.valueOf(reading.batteryLevel.displayValue));
        }

        // Work out which mode we are in
        Boolean scheduleLock = Boolean.parseBoolean(reading.activeScheduleLock.displayValue);
        Boolean heatCoolMode = reading.activeHeatCoolMode.displayValue.equals("HEAT");
        Boolean boost = reading.activeHeatCoolMode.displayValue.equals("BOOST");

        Channel modeChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_MODE);
        if (modeChannel != null) {
            if (boost) {
                updateState(modeChannel.getUID().getId(), new StringType("Boost"));
            } else if (scheduleLock && heatCoolMode) {
                updateState(modeChannel.getUID().getId(), new StringType("Manual"));
            } else if (!scheduleLock && heatCoolMode) {
                updateState(modeChannel.getUID().getId(), new StringType("Schedule"));
            } else if (scheduleLock && !heatCoolMode) {
                updateState(modeChannel.getUID().getId(), new StringType("Off"));
            }
        }

        Channel boostRemainingChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_BOOST_REMAINING);
        if (boostRemainingChannel != null) {
            if (reading.activeOverrides != null && boost) {
                int expiryMinutes = new Integer(reading.scheduleLockDuration.displayValue);
                if (expiryMinutes > 0) {
                    updateState(boostRemainingChannel.getUID().getId(), new DecimalType(expiryMinutes));
                } else {
                    updateState(boostRemainingChannel.getUID().getId(), new DecimalType(0));
                }
            } else {
                updateState(boostRemainingChannel.getUID().getId(), new DecimalType(0));
            }
        }
    }

    public HiveAttributes getThermostatReading() {
        if (bridgeHandler != null) {
            return bridgeHandler.getThermostatReading(this.thing);
        } else {
            return new HiveAttributes();
        }
    }

    protected @Nullable HiveBridgeHandler getHiveBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.warn("Required bridge not defined for device {}", this.getThing().getUID());
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HiveBridgeHandler) {
                this.bridgeHandler = (HiveBridgeHandler) handler;
            } else {
                logger.warn("No available bridge handler found for device {} bridge {} .", this.getThing().getUID(),
                        bridge.getUID());
                return null;
            }
        }
        return this.bridgeHandler;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            if (bridgeHandler != null) {
                bridgeHandler.requestRefresh();
            }
            return;
        }
        switch (channelUID.getId()) {
            case HiveBindingConstants.CHANNEL_TARGET_TEMPERATURE: {
                if (command instanceof QuantityType) {
                    QuantityType<Temperature> temperature = (QuantityType<Temperature>) command;
                    if (bridgeHandler != null) {
                        bridgeHandler.setTargetTemperature(getThing().getUID(), temperature.floatValue());
                    }
                } else {
                    logger.warn("CHANNEL_TARGET_TEMPERATURE channel only supports DecimalType");
                }
                break;
            }
            case HiveBindingConstants.CHANNEL_BOOST_REMAINING: {
                if (command instanceof DecimalType) {
                    if (boostCache == OnOffType.ON) {
                        DecimalType duration = (DecimalType) command;
                        if (bridgeHandler != null) {
                            bridgeHandler.boost(getThing().getUID(), OnOffType.ON, duration.intValue());
                        }
                    }
                } else {
                    logger.warn("CHANNEL_BOOST_REMAINING only supports numbers.");
                }
                break;
            }
            case HiveBindingConstants.CHANNEL_BOOST: {
                if (command instanceof OnOffType) {
                    OnOffType boost = (OnOffType) command;
                    if (bridgeHandler != null) {
                        bridgeHandler.boost(getThing().getUID(), boost, 30);
                    }
                    boostCache = boost;
                    Channel boostOnChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_BOOST);
                    if (boostOnChannel != null) {
                        updateState(boostOnChannel.getUID().getId(), boost);
                    }
                    Channel boostRemainingChannel = getThing().getChannel(HiveBindingConstants.CHANNEL_BOOST_REMAINING);
                    if (boostRemainingChannel != null) {
                        if (boost == OnOffType.ON) {
                            updateState(boostRemainingChannel.getUID().getId(), new DecimalType(30));
                        } else {
                            updateState(boostRemainingChannel.getUID().getId(), new DecimalType(0));
                        }
                    }
                } else {
                    logger.warn("CHANNEL_BOOST only supports switch type.");
                }
                break;
            }
            default:
                logger.warn("Channel unknown {}", channelUID.getId());
        }
    }
}
