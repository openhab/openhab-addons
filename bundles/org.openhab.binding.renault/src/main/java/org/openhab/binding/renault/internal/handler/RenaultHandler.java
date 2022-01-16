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
package org.openhab.binding.renault.internal.handler;

import static org.openhab.binding.renault.internal.RenaultBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.METRE;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.renault.internal.RenaultBindingConstants;
import org.openhab.binding.renault.internal.RenaultConfiguration;
import org.openhab.binding.renault.internal.api.Car;
import org.openhab.binding.renault.internal.api.MyRenaultHttpSession;
import org.openhab.binding.renault.internal.api.exceptions.RenaultForbiddenException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultNotImplementedException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultUpdateException;
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case RenaultBindingConstants.CHANNEL_HVAC_TARGET_TEMPERATURE:
                if (command instanceof RefreshType) {
                    updateState(CHANNEL_HVAC_TARGET_TEMPERATURE, new QuantityType<Temperature>(
                            car.getHvacTargetTemperature().doubleValue(), SIUnits.CELSIUS));
                } else if (command instanceof QuantityType) {
                    car.setHvacTargetTemperature(((QuantityType<?>) command).doubleValue());
                    updateState(CHANNEL_HVAC_TARGET_TEMPERATURE, new QuantityType<Temperature>(
                            car.getHvacTargetTemperature().doubleValue(), SIUnits.CELSIUS));
                }
                break;
            case RenaultBindingConstants.CHANNEL_HVAC_STATUS:
                if (command instanceof OnOffType) {
                    MyRenaultHttpSession httpSession = new MyRenaultHttpSession(this.config, httpClient);
                    try {
                        httpSession.initSesssion(car);
                        if (command == OnOffType.ON) {
                            httpSession.hvacOn(car.getHvacTargetTemperature());
                        } else if (command == OnOffType.OFF) {
                            httpSession.hvacOff();
                        }
                        updateHvacStatus(httpSession);
                    } catch (Exception e) {
                        httpSession = null;
                        logger.warn("Error My Renault Http Session.", e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                }
                break;
            default:
                break;
        }
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
                new QuantityType<Temperature>(car.getHvacTargetTemperature().doubleValue(), SIUnits.CELSIUS));

        // Background initialization:
        ScheduledFuture<?> job = pollingJob;
        if (job == null || job.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(this::getStatus, 0, config.refreshInterval, TimeUnit.MINUTES);
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
        } catch (Exception e) {
            httpSession = null;
            logger.warn("Error My Renault Http Session.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        if (httpSession != null) {
            String imageURL = car.getImageURL();
            if (imageURL != null && !imageURL.isEmpty()) {
                updateState(CHANNEL_IMAGE, new StringType(imageURL));
            }
            updateHvacStatus(httpSession);
            updateCockpit(httpSession);
            updateLocation(httpSession);
            updateBattery(httpSession);
        }
    }

    private void updateHvacStatus(MyRenaultHttpSession httpSession) {
        if (!car.isDisableHvac()) {
            try {
                httpSession.getHvacStatus(car);
                Boolean hvacstatus = car.getHvacstatus();
                if (hvacstatus != null) {
                    updateState(CHANNEL_HVAC_STATUS, OnOffType.from(hvacstatus.booleanValue()));
                }
                Double externalTemperature = car.getExternalTemperature();
                if (externalTemperature != null) {
                    updateState(CHANNEL_EXTERNAL_TEMPERATURE,
                            new QuantityType<Temperature>(externalTemperature.doubleValue(), SIUnits.CELSIUS));
                }
            } catch (RenaultNotImplementedException e) {
                car.setDisableHvac(true);
            } catch (RenaultForbiddenException | RenaultUpdateException e) {
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
            } catch (RenaultNotImplementedException e) {
                car.setDisableLocation(true);
            } catch (RenaultForbiddenException | RenaultUpdateException e) {
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
                car.setDisableCockpit(true);
            } catch (RenaultForbiddenException | RenaultUpdateException e) {
            }
        }
    }

    private void updateBattery(MyRenaultHttpSession httpSession) {
        if (!car.isDisableBattery()) {
            try {
                httpSession.getBatteryStatus(car);
                Double batteryLevel = car.getBatteryLevel();
                if (batteryLevel != null) {
                    updateState(CHANNEL_BATTERY_LEVEL, new DecimalType(batteryLevel.doubleValue()));
                }
                Double estimatedRange = car.getEstimatedRange();
                if (estimatedRange != null) {
                    updateState(CHANNEL_ESTIMATED_RANGE,
                            new QuantityType<Length>(estimatedRange.doubleValue(), KILO(METRE)));
                }
                String plugStatus = car.getPlugStatus();
                if (plugStatus != null) {
                    updateState(CHANNEL_PLUG_STATUS, new StringType(plugStatus));
                }
                String chargingStatus = car.getChargingStatus();
                if (chargingStatus != null) {
                    updateState(CHANNEL_CHARGING_STATUS, new StringType(chargingStatus));
                }
                Double batteryAvailableEnergy = car.getBatteryAvailableEnergy();
                if (batteryAvailableEnergy != null) {
                    updateState(CHANNEL_BATTERY_AVAILABLE_ENERGY,
                            new DecimalType(batteryAvailableEnergy.doubleValue()));
                }
                Double batteryCapacity = car.getBatteryCapacity();
                if (batteryCapacity != null) {
                    updateState(CHANNEL_BATTERY_CAPACITY, new DecimalType(batteryCapacity.doubleValue()));
                }
                Double batteryTemperature = car.getBatteryTemperature();
                if (batteryCapacity != null) {
                    updateState(CHANNEL_BATTERY_TEMPERATURE, new DecimalType(batteryTemperature.doubleValue()));
                }
            } catch (RenaultNotImplementedException e) {
                car.setDisableBattery(true);
            } catch (RenaultForbiddenException | RenaultUpdateException e) {
            }
        }
    }
}
