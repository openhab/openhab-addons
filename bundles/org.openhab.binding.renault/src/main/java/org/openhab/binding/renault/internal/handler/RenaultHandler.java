/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.renault.internal.handler;

import static org.openhab.binding.renault.internal.RenaultBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.METRE;
import static org.openhab.core.library.unit.Units.KILOWATT_HOUR;
import static org.openhab.core.library.unit.Units.MINUTE;

import java.time.ZonedDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.renault.internal.RenaultBindingConstants;
import org.openhab.binding.renault.internal.RenaultConfiguration;
import org.openhab.binding.renault.internal.api.Car;
import org.openhab.binding.renault.internal.api.Car.ChargingMode;
import org.openhab.binding.renault.internal.api.MyRenaultHttpSession;
import org.openhab.binding.renault.internal.api.exceptions.RenaultAPIGatewayException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultActionException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultForbiddenException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultNotImplementedException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultUpdateException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RenaultHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class RenaultHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RenaultHandler.class);

    private RenaultConfiguration config = new RenaultConfiguration();

    private @Nullable ScheduledFuture<?> pollingJob;

    private HttpClient httpClient;

    private Car car;

    public RenaultHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.car = new Car();
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        // reset the car on initialize
        this.car = new Car();
        this.config = getConfigAs(RenaultConfiguration.class);

        // Validate configuration
        if (this.config.myRenaultUsername.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "MyRenault Username is empty!");
            return;
        }
        if (this.config.myRenaultPassword.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "MyRenault Password is empty!");
            return;
        }
        if (this.config.locale.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Location is empty!");
            return;
        }
        if (this.config.vin.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "VIN is empty!");
            return;
        }
        if (this.config.refreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The refresh interval mush to be larger than 1");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        updateState(CHANNEL_HVAC_TARGET_TEMPERATURE,
                new QuantityType<Temperature>(car.getHvacTargetTemperature(), SIUnits.CELSIUS));

        reschedulePollingJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        switch (channelUID.getId()) {
            case RenaultBindingConstants.CHANNEL_HVAC_TARGET_TEMPERATURE:
                if (!car.isDisableHvac()) {
                    if (command instanceof RefreshType) {
                        updateState(CHANNEL_HVAC_TARGET_TEMPERATURE,
                                new QuantityType<Temperature>(car.getHvacTargetTemperature(), SIUnits.CELSIUS));
                    } else if (command instanceof DecimalType) {
                        car.setHvacTargetTemperature(((DecimalType) command).doubleValue());
                        updateState(CHANNEL_HVAC_TARGET_TEMPERATURE,
                                new QuantityType<Temperature>(car.getHvacTargetTemperature(), SIUnits.CELSIUS));
                    } else if (command instanceof QuantityType) {
                        @Nullable
                        QuantityType<Temperature> celsius = ((QuantityType<Temperature>) command)
                                .toUnit(SIUnits.CELSIUS);
                        if (celsius != null) {
                            car.setHvacTargetTemperature(celsius.doubleValue());
                        }
                        updateState(CHANNEL_HVAC_TARGET_TEMPERATURE,
                                new QuantityType<Temperature>(car.getHvacTargetTemperature(), SIUnits.CELSIUS));
                    }
                }
                break;
            case RenaultBindingConstants.CHANNEL_HVAC_STATUS:
                if (!car.isDisableHvac()) {
                    if (command instanceof RefreshType) {
                        reschedulePollingJob();
                    } else if (command instanceof StringType && command.toString().equals(Car.HVAC_STATUS_ON)) {
                        // We can only trigger pre-conditioning of the car.
                        final MyRenaultHttpSession httpSession = new MyRenaultHttpSession(this.config, httpClient);
                        try {
                            updateState(CHANNEL_HVAC_STATUS, new StringType(Car.HVAC_STATUS_PENDING));
                            car.resetHVACStatus();
                            httpSession.initSesssion(car);
                            httpSession.actionHvacOn(car.getHvacTargetTemperature());
                            ScheduledFuture<?> job = pollingJob;
                            if (job != null) {
                                job.cancel(true);
                            }
                            pollingJob = scheduler.scheduleWithFixedDelay(this::getStatus, config.updateDelay,
                                    config.refreshInterval * 60, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            logger.warn("Error My Renault Http Session.", e);
                            Thread.currentThread().interrupt();
                        } catch (RenaultException | RenaultForbiddenException | RenaultUpdateException
                                | RenaultActionException | RenaultNotImplementedException | ExecutionException
                                | TimeoutException e) {
                            logger.warn("Error during action HVAC on.", e);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    }
                }
                break;
            case RenaultBindingConstants.CHANNEL_CHARGING_MODE:
                if (command instanceof RefreshType) {
                    reschedulePollingJob();
                } else if (command instanceof StringType) {
                    try {
                        ChargingMode newMode = ChargingMode.valueOf(command.toString());
                        if (!ChargingMode.UNKNOWN.equals(newMode)) {
                            MyRenaultHttpSession httpSession = new MyRenaultHttpSession(this.config, httpClient);
                            try {
                                httpSession.initSesssion(car);
                                httpSession.actionChargeMode(newMode);
                                car.setChargeMode(newMode);
                                updateState(CHANNEL_CHARGING_MODE, new StringType(newMode.toString()));
                            } catch (InterruptedException e) {
                                logger.warn("Error My Renault Http Session.", e);
                                Thread.currentThread().interrupt();
                            } catch (RenaultException | RenaultForbiddenException | RenaultUpdateException
                                    | RenaultActionException | RenaultNotImplementedException | ExecutionException
                                    | TimeoutException e) {
                                logger.warn("Error during action set charge mode.", e);
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        e.getMessage());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid ChargingMode {}.", command.toString());
                        return;
                    }
                }
                break;
            case RenaultBindingConstants.CHANNEL_PAUSE:
                if (command instanceof RefreshType) {
                    reschedulePollingJob();
                } else if (command instanceof OnOffType) {
                    try {
                        MyRenaultHttpSession httpSession = new MyRenaultHttpSession(this.config, httpClient);
                        try {
                            boolean pause = OnOffType.ON == command;
                            httpSession.initSesssion(car);
                            httpSession.actionPause(pause);
                            car.setPauseMode(pause);
                            updateState(CHANNEL_PAUSE, OnOffType.from(command.toString()));
                        } catch (InterruptedException e) {
                            logger.warn("Error My Renault Http Session.", e);
                            Thread.currentThread().interrupt();
                        } catch (RenaultForbiddenException | RenaultNotImplementedException | RenaultActionException
                                | RenaultException | RenaultUpdateException | ExecutionException | TimeoutException e) {
                            logger.warn("Error during action set pause.", e);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid Pause Mode {}.", command.toString());
                        return;
                    }
                }
                break;
            default:
                if (command instanceof RefreshType) {
                    reschedulePollingJob();
                }
                break;
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
        super.dispose();
    }

    private void getStatus() {
        MyRenaultHttpSession httpSession = new MyRenaultHttpSession(this.config, httpClient);
        try {
            httpSession.initSesssion(car);
            updateStatus(ThingStatus.ONLINE);
        } catch (InterruptedException e) {
            logger.warn("Error My Renault Http Session.", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.warn("Error My Renault Http Session.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        String imageURL = car.getImageURL();
        if (imageURL != null && !imageURL.isEmpty()) {
            updateState(CHANNEL_IMAGE, new StringType(imageURL));
        }
        updateHvacStatus(httpSession);
        updateCockpit(httpSession);
        updateLocation(httpSession);
        updateBattery(httpSession);
        updateLockStatus(httpSession);
    }

    private void updateHvacStatus(MyRenaultHttpSession httpSession) {
        if (!car.isDisableHvac()) {
            try {
                httpSession.getHvacStatus(car);
                Boolean hvacstatus = car.getHvacstatus();
                if (hvacstatus == null) {
                    updateState(CHANNEL_HVAC_STATUS, new StringType(Car.HVAC_STATUS_PENDING));
                } else if (hvacstatus.booleanValue()) {
                    updateState(CHANNEL_HVAC_STATUS, new StringType(Car.HVAC_STATUS_ON));
                } else {
                    updateState(CHANNEL_HVAC_STATUS, new StringType(Car.HVAC_STATUS_OFF));
                }
                Double externalTemperature = car.getExternalTemperature();
                if (externalTemperature != null) {
                    updateState(CHANNEL_EXTERNAL_TEMPERATURE,
                            new QuantityType<Temperature>(externalTemperature.doubleValue(), SIUnits.CELSIUS));
                }
            } catch (RenaultNotImplementedException e) {
                logger.warn("Disabling unsupported HVAC status update.");
                car.setDisableHvac(true);
            } catch (RenaultForbiddenException | RenaultUpdateException | RenaultAPIGatewayException e) {
                logger.warn("Error updating HVAC status.", e);
            }
        }
    }

    private void updateLocation(MyRenaultHttpSession httpSession) {
        if (!car.isDisableLocation()) {
            try {
                httpSession.getLocation(car);
                Double latitude = car.getGpsLatitude();
                Double longitude = car.getGpsLongitude();
                if (latitude != null && longitude != null) {
                    updateState(CHANNEL_LOCATION, new PointType(new DecimalType(latitude.doubleValue()),
                            new DecimalType(longitude.doubleValue())));
                }
                ZonedDateTime locationUpdated = car.getLocationUpdated();
                if (locationUpdated != null) {
                    updateState(CHANNEL_LOCATION_UPDATED, new DateTimeType(locationUpdated));
                }
            } catch (RenaultNotImplementedException e) {
                logger.warn("Disabling unsupported location update.");
                car.setDisableLocation(true);
            } catch (IllegalArgumentException | RenaultForbiddenException | RenaultUpdateException
                    | RenaultAPIGatewayException e) {
                logger.warn("Error updating location.", e);
            }
        }
    }

    private void updateCockpit(MyRenaultHttpSession httpSession) {
        if (!car.isDisableCockpit()) {
            try {
                httpSession.getCockpit(car);
                Double odometer = car.getOdometer();
                if (odometer != null) {
                    updateState(CHANNEL_ODOMETER, new QuantityType<Length>(odometer.doubleValue(), KILO(METRE)));
                }
            } catch (RenaultNotImplementedException e) {
                logger.warn("Disabling unsupported cockpit status update.");
                car.setDisableCockpit(true);
            } catch (RenaultForbiddenException | RenaultUpdateException | RenaultAPIGatewayException e) {
                logger.warn("Error updating cockpit status.", e);
            }
        }
    }

    private void updateBattery(MyRenaultHttpSession httpSession) {
        if (!car.isDisableBattery()) {
            try {
                httpSession.getBatteryStatus(car);
                updateState(CHANNEL_PLUG_STATUS, new StringType(car.getPlugStatus().name()));
                updateState(CHANNEL_CHARGING_STATUS, new StringType(car.getChargingStatus().name()));
                Double batteryLevel = car.getBatteryLevel();
                if (batteryLevel != null) {
                    updateState(CHANNEL_BATTERY_LEVEL, new DecimalType(batteryLevel.doubleValue()));
                }
                Double estimatedRange = car.getEstimatedRange();
                if (estimatedRange != null) {
                    updateState(CHANNEL_ESTIMATED_RANGE,
                            new QuantityType<Length>(estimatedRange.doubleValue(), KILO(METRE)));
                }
                Double batteryAvailableEnergy = car.getBatteryAvailableEnergy();
                if (batteryAvailableEnergy != null) {
                    updateState(CHANNEL_BATTERY_AVAILABLE_ENERGY,
                            new QuantityType<Energy>(batteryAvailableEnergy.doubleValue(), KILOWATT_HOUR));
                }
                Integer chargingRemainingTime = car.getChargingRemainingTime();
                if (chargingRemainingTime != null) {
                    updateState(CHANNEL_CHARGING_REMAINING_TIME,
                            new QuantityType<Time>(chargingRemainingTime.doubleValue(), MINUTE));
                }
                ZonedDateTime batteryStatusUpdated = car.getBatteryStatusUpdated();
                if (batteryStatusUpdated != null) {
                    updateState(CHANNEL_BATTERY_STATUS_UPDATED, new DateTimeType(batteryStatusUpdated));
                }
            } catch (RenaultNotImplementedException e) {
                logger.warn("Disabling unsupported battery update.");
                car.setDisableBattery(true);
            } catch (RenaultForbiddenException | RenaultUpdateException | RenaultAPIGatewayException e) {
                logger.warn("Error updating battery status.", e);
            }
        }
    }

    private void updateLockStatus(MyRenaultHttpSession httpSession) {
        if (!car.isDisableLockStatus()) {
            try {
                httpSession.getLockStatus(car);
                switch (car.getLockStatus()) {
                    case LOCKED:
                        updateState(CHANNEL_LOCKED, OnOffType.ON);
                        break;
                    case UNLOCKED:
                        updateState(CHANNEL_LOCKED, OnOffType.OFF);
                        break;
                    default:
                        updateState(CHANNEL_LOCKED, UnDefType.UNDEF);
                        break;
                }
            } catch (RenaultNotImplementedException | RenaultAPIGatewayException e) {
                // If not supported API returns a Bad Gateway for this call.
                updateState(CHANNEL_LOCKED, UnDefType.UNDEF);
                logger.warn("Disabling unsupported lock status update.");
                car.setDisableLockStatus(true);
            } catch (RenaultForbiddenException | RenaultUpdateException e) {
                logger.warn("Error updating lock status.", e);
            }
        }
    }

    private void reschedulePollingJob() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        pollingJob = scheduler.scheduleWithFixedDelay(this::getStatus, 0, config.refreshInterval, TimeUnit.MINUTES);
    }
}
