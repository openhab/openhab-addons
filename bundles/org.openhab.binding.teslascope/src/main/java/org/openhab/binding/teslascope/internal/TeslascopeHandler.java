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
package org.openhab.binding.teslascope.internal;

import static org.openhab.binding.teslascope.internal.TeslascopeBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teslascope.internal.api.DetailedInformation;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeslascopeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class TeslascopeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TeslascopeHandler.class);

    private @NonNullByDefault({}) TeslascopeConfiguration config;
    private @NonNullByDefault({}) TeslascopeWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;

    public TeslascopeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case TeslascopeBindingConstants.CHANNEL_HONK_HORN:
                    if (command instanceof OnOffType onOffCommand) {
                        honkHorn();
                        return;
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_FLASH_LIGHTS:
                    if (command instanceof OnOffType onOffCommand) {
                        flashLights();
                        return;
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_CHARGE:
                    if (command instanceof OnOffType onOffCommand) {
                        if (onOffCommand == OnOffType.ON) {
                            charge(true);
                        } else {
                            charge(false);
                        }
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_CHARGE_PORT:
                    if (command instanceof OnOffType onOffCommand) {
                        if (onOffCommand == OnOffType.ON) {
                            chargeDoor(true);
                        } else {
                            chargeDoor(false);
                        }
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_SENTRY_MODE:
                    if (command instanceof OnOffType onOffCommand) {
                        if (onOffCommand == OnOffType.ON) {
                            sentry(true);
                        } else {
                            sentry(false);
                        }
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_AUTOCONDITIONING:
                    if (command instanceof OnOffType onOffCommand) {
                        if (onOffCommand == OnOffType.ON) {
                            ac(true);
                        } else {
                            ac(false);
                        }
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_FRONT_TRUNK:
                    if (command instanceof OnOffType onOffCommand) {
                        openFrunk();
                        return;
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_REAR_TRUNK:
                    if (command instanceof OnOffType onOffCommand) {
                        openTrunk();
                        return;
                    }
                    break;
            }
            logger.debug("Received command ({}) of wrong type for thing '{}' on channel {}", command,
                    thing.getUID().getAsString(), channelUID.getId());
        } catch (TeslascopeCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(TeslascopeConfiguration.class);
        if (config.apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-api-key");
            return;
        }

        webTargets = new TeslascopeWebTargets();
        updateStatus(ThingStatus.UNKNOWN);

        schedulePoll();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        logger.debug("Scheduling poll every {} s", config.refreshInterval);
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void poll() {
        try {
            logger.debug("Polling for state");
            pollStatus();
        } catch (IOException e) {
            logger.debug("Could not connect to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error connecting to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    private void pollStatus() throws IOException {
        DetailedInformation detailedInformation = null;

        try {
            detailedInformation = webTargets.getDetailedInformation(config.publicID, config.apiKey);
            updateStatus(ThingStatus.ONLINE);
        } catch (TeslascopeCommunicationException e) {
            logger.debug("Unexpected error connecting to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        updateState(TeslascopeBindingConstants.CHANNEL_VIN, new StringType(detailedInformation.vin));
        updateState(TeslascopeBindingConstants.CHANNEL_VEHICLE_NAME, new StringType(detailedInformation.vehicleName));
        updateState(TeslascopeBindingConstants.CHANNEL_VEHICLE_STATE, new StringType(detailedInformation.vehicleState));
        updateState(TeslascopeBindingConstants.CHANNEL_ODOMETER,
                new QuantityType<>(detailedInformation.odometer, ImperialUnits.MILE));

        // charge state
        updateState(TeslascopeBindingConstants.CHANNEL_BATTERY_LEVEL,
                new DecimalType(detailedInformation.batteryLevel));
        updateState(TeslascopeBindingConstants.CHANNEL_USABLE_BATTERY_LEVEL,
                new DecimalType(detailedInformation.usableBatteryLevel));
        updateState(TeslascopeBindingConstants.CHANNEL_BATTERY_RANGE,
                new QuantityType<>(detailedInformation.batteryRange, ImperialUnits.MILE));
        updateState(TeslascopeBindingConstants.CHANNEL_ESTIMATED_BATTERY_RANGE,
                new QuantityType<>(detailedInformation.estBatteryRange, ImperialUnits.MILE));
        // charge_enable_request isn't the right flag to determine if car is charging or not
        if (detailedInformation.chargingState.equals("Charging")) {
            updateState(TeslascopeBindingConstants.CHANNEL_CHARGE, OnOffType.ON);
        } else {
            updateState(TeslascopeBindingConstants.CHANNEL_CHARGE, OnOffType.OFF);
        }
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGE_ENERGY_ADDED,
                new QuantityType<>(detailedInformation.chargeEnergyAdded, Units.KILOWATT_HOUR));
        updateState(CHANNEL_CHARGE_LIMIT_SOC_STANDARD, new DecimalType(detailedInformation.chargeLimitSoc / 100));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGE_PORT,
                OnOffType.from(detailedInformation.chargePortDoorOpen));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGE_RATE,
                new QuantityType<>(detailedInformation.chargeRate, ImperialUnits.MILES_PER_HOUR));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGER_POWER,
                new QuantityType<>(detailedInformation.chargerPower, MetricPrefix.KILO(Units.WATT)));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGER_VOLTAGE,
                new QuantityType<>(detailedInformation.chargerVoltage, Units.VOLT));
        updateState(TeslascopeBindingConstants.CHANNEL_TIME_TO_FULL_CHARGE,
                new QuantityType<>(detailedInformation.timeToFullCharge, Units.HOUR));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGING_STATE,
                new StringType(detailedInformation.chargingState));
        updateState(TeslascopeBindingConstants.CHANNEL_SCHEDULED_CHARGING_PENDING,
                OnOffType.from(detailedInformation.scheduledChargingPending));
        updateState(TeslascopeBindingConstants.CHANNEL_SCHEDULED_CHARGING_START,
                new StringType(detailedInformation.scheduledChargingStartTime));

        // climate state
        updateState(TeslascopeBindingConstants.CHANNEL_AUTOCONDITIONING,
                OnOffType.from(detailedInformation.isAutoConditioningOn));
        updateState(TeslascopeBindingConstants.CHANNEL_CLIMATE, OnOffType.from(detailedInformation.isClimateOn));
        updateState(TeslascopeBindingConstants.CHANNEL_FRONT_DEFROSTER,
                OnOffType.from(detailedInformation.isFrontDefrosterOn));
        updateState(TeslascopeBindingConstants.CHANNEL_PRECONDITIONING,
                OnOffType.from(detailedInformation.isPreconditioning));
        updateState(TeslascopeBindingConstants.CHANNEL_REAR_DEFROSTER,
                OnOffType.from(detailedInformation.isRearDefrosterOn));
        updateState(TeslascopeBindingConstants.CHANNEL_LEFT_SEAT_HEATER,
                new DecimalType(detailedInformation.seatHeaterLeft));
        updateState(TeslascopeBindingConstants.CHANNEL_CENTER_REAR_SEAT_HEATER,
                new DecimalType(detailedInformation.seatHeaterRearCenter));
        updateState(TeslascopeBindingConstants.CHANNEL_LEFT_REAR_SEAT_HEATER,
                new DecimalType(detailedInformation.seatHeaterRearLeft));
        updateState(TeslascopeBindingConstants.CHANNEL_RIGHT_REAR_SEAT_HEATER,
                new DecimalType(detailedInformation.seatHeaterRearRight));
        updateState(TeslascopeBindingConstants.CHANNEL_RIGHT_SEAT_HEATER,
                new DecimalType(detailedInformation.seatHeaterRight));
        updateState(TeslascopeBindingConstants.CHANNEL_SIDE_MIRROR_HEATERS,
                OnOffType.from(detailedInformation.sideMirrorHeaters));
        updateState(TeslascopeBindingConstants.CHANNEL_SMARTPRECONDITIONG,
                OnOffType.from(detailedInformation.smartPreconditioning));
        updateState(TeslascopeBindingConstants.CHANNEL_STEERING_WHEEL_HEATER,
                OnOffType.from(detailedInformation.steeringWheelHeater));
        updateState(TeslascopeBindingConstants.CHANNEL_WIPER_BLADE_HEATER,
                OnOffType.from(detailedInformation.wiperBladeHeater));
        updateState(TeslascopeBindingConstants.CHANNEL_DRIVER_TEMP,
                new QuantityType<>(detailedInformation.driverTempSetting, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_INSIDE_TEMP,
                new QuantityType<>(detailedInformation.insideTemp, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_OUTSIDE_TEMP,
                new QuantityType<>(detailedInformation.outsideTemp, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_PASSENGER_TEMP,
                new QuantityType<>(detailedInformation.passengerTempSetting, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_FAN, new DecimalType(detailedInformation.fanStatus));
        updateState(TeslascopeBindingConstants.CHANNEL_LEFT_TEMP_DIRECTION,
                new DecimalType(detailedInformation.leftTempDirection));
        updateState(TeslascopeBindingConstants.CHANNEL_MAX_AVAILABLE_TEMP,
                new QuantityType<>(detailedInformation.maxAvailTemp, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_MIN_AVAILABLE_TEMP,
                new QuantityType<>(detailedInformation.minAvailTemp, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_RIGHT_TEMP_DIRECTION,
                new DecimalType(detailedInformation.rightTempDirection));

        // drive state
        updateState(TeslascopeBindingConstants.CHANNEL_HEADING, new DecimalType(detailedInformation.heading));
        updateState(TeslascopeBindingConstants.CHANNEL_LOCATION,
                new PointType(detailedInformation.latitude + "," + detailedInformation.longitude));
        updateState(TeslascopeBindingConstants.CHANNEL_POWER,
                new QuantityType<>(detailedInformation.power, MetricPrefix.KILO(Units.WATT)));
        updateState(TeslascopeBindingConstants.CHANNEL_SHIFT_STATE, new StringType(detailedInformation.shiftState));
        updateState(TeslascopeBindingConstants.CHANNEL_SPEED,
                new QuantityType<>(detailedInformation.speed, ImperialUnits.MILES_PER_HOUR));

        // vehicle state
        updateState(TeslascopeBindingConstants.CHANNEL_DOOR_LOCK, OnOffType.from(detailedInformation.locked));
        updateState(TeslascopeBindingConstants.CHANNEL_SENTRY_MODE, OnOffType.from(detailedInformation.sentryMode));
        updateState(TeslascopeBindingConstants.CHANNEL_VALET_MODE, OnOffType.from(detailedInformation.valetMode));
        if (detailedInformation.softwareUpdateStatus.equals("")) {
            updateState(TeslascopeBindingConstants.CHANNEL_SOFTWARE_UPDATE_AVAILABLE, OnOffType.OFF);
        } else {
            updateState(TeslascopeBindingConstants.CHANNEL_SOFTWARE_UPDATE_AVAILABLE, OnOffType.ON);
        }
        updateState(TeslascopeBindingConstants.CHANNEL_SOFTWARE_UPDATE_STATUS,
                new StringType(detailedInformation.softwareUpdateStatus));
        updateState(TeslascopeBindingConstants.CHANNEL_SOFTWARE_UPDATE_VERSION,
                new StringType(detailedInformation.softwareUpdateVersion));
        updateState(TeslascopeBindingConstants.CHANNEL_SUNROOF_STATE, new StringType(detailedInformation.sunRoofState));
        updateState(TeslascopeBindingConstants.CHANNEL_SUNROOF,
                new DecimalType(detailedInformation.sunRoofPercentOpen));
        updateState(TeslascopeBindingConstants.CHANNEL_HOMELINK, OnOffType.from(detailedInformation.homelinkNearby));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_FL,
                new QuantityType<>(detailedInformation.tpmsPressureFL, Units.BAR));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_FR,
                new QuantityType<>(detailedInformation.tpmsPressureFR, Units.BAR));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_RL,
                new QuantityType<>(detailedInformation.tpmsPressureRL, Units.BAR));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_RR,
                new QuantityType<>(detailedInformation.tpmsPressureRR, Units.BAR));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_SOFT_WARNING_FL,
                OnOffType.from(detailedInformation.tpmsSoftWarningFL));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_SOFT_WARNING_FR,
                OnOffType.from(detailedInformation.tpmsSoftWarningFR));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_SOFT_WARNING_RL,
                OnOffType.from(detailedInformation.tpmsSoftWarningRL));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_SOFT_WARNING_RR,
                OnOffType.from(detailedInformation.tpmsSoftWarningRR));
        updateState(TeslascopeBindingConstants.CHANNEL_DRIVER_FRONT_DOOR,
                detailedInformation.df ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(TeslascopeBindingConstants.CHANNEL_DRIVER_REAR_DOOR,
                detailedInformation.dr ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(TeslascopeBindingConstants.CHANNEL_PASSENGER_FRONT_DOOR,
                detailedInformation.pf ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(TeslascopeBindingConstants.CHANNEL_PASSENGER_REAR_DOOR,
                detailedInformation.pr ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(TeslascopeBindingConstants.CHANNEL_FRONT_TRUNK, OnOffType.from(detailedInformation.ft));
        updateState(TeslascopeBindingConstants.CHANNEL_REAR_TRUNK, OnOffType.from(detailedInformation.rt));

        // virtual items
        updateState(TeslascopeBindingConstants.CHANNEL_HONK_HORN, OnOffType.OFF);
        updateState(TeslascopeBindingConstants.CHANNEL_FLASH_LIGHTS, OnOffType.OFF);
    }

    protected void ac(boolean b) throws TeslascopeCommunicationException {
        webTargets.sendCommand(config.publicID, config.apiKey, b ? "startAC" : "stopAC");
    }

    protected void charge(boolean b) throws TeslascopeCommunicationException {
        webTargets.sendCommand(config.publicID, config.apiKey, b ? "startCharging" : "stopCharging");
    }

    protected void chargeDoor(boolean b) throws TeslascopeCommunicationException {
        webTargets.sendCommand(config.publicID, config.apiKey, b ? "openChargeDoor" : "closeChargeDoor");
    }

    protected void flashLights() throws TeslascopeCommunicationException {
        webTargets.sendCommand(config.publicID, config.apiKey, "flashLights");
        updateState(TeslascopeBindingConstants.CHANNEL_FLASH_LIGHTS, OnOffType.OFF);
    }

    protected void honkHorn() throws TeslascopeCommunicationException {
        webTargets.sendCommand(config.publicID, config.apiKey, "honkHorn");
        updateState(TeslascopeBindingConstants.CHANNEL_HONK_HORN, OnOffType.OFF);
    }

    protected void openFrunk() throws TeslascopeCommunicationException {
        webTargets.sendCommand(config.publicID, config.apiKey, "openFrunk");
    }

    protected void openTrunk() throws TeslascopeCommunicationException {
        webTargets.sendCommand(config.publicID, config.apiKey, "openTrunk");
    }

    protected void sentry(boolean b) throws TeslascopeCommunicationException {
        webTargets.sendCommand(config.publicID, config.apiKey, b ? "enableSentryMode" : "disableSentryMode");
    }
}
