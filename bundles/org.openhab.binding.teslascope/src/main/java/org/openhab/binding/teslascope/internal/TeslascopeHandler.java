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

    private long refreshInterval;
    private String apiKey = "";
    private String publicID = "";

    private @Nullable TeslascopeConfiguration config;
    private @NonNullByDefault({}) TeslascopeWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;

    public TeslascopeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case TeslascopeBindingConstants.CHANNEL_HONKHORN:
                    if (command instanceof OnOffType onOffCommand) {
                        honkHorn();
                        return;
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_FLASHLIGHTS:
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
                case TeslascopeBindingConstants.CHANNEL_CHARGEPORT:
                    if (command instanceof OnOffType onOffCommand) {
                        if (onOffCommand == OnOffType.ON) {
                            chargeDoor(true);
                        } else {
                            chargeDoor(false);
                        }
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_SENTRYMODE:
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
                case TeslascopeBindingConstants.CHANNEL_FRONTTRUNK:
                    if (command instanceof OnOffType onOffCommand) {
                        openFrunk();
                        return;
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_REARTRUNK:
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
        refreshInterval = config.refreshInterval;
        publicID = config.publicID;
        apiKey = config.apiKey;

        schedulePoll();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        logger.debug("Scheduling poll every {} s", refreshInterval);
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, refreshInterval, TimeUnit.SECONDS);
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

        try {
            DetailedInformation detailedInformation = webTargets.getDetailedInformation(publicID, apiKey);

            updateStatus(ThingStatus.ONLINE);
            updateState(TeslascopeBindingConstants.CHANNEL_VIN, new StringType(detailedInformation.vin));
            updateState(TeslascopeBindingConstants.CHANNEL_VEHICLENAME,
                    new StringType(detailedInformation.vehiclename));
            updateState(TeslascopeBindingConstants.CHANNEL_VEHICLESTATE,
                    new StringType(detailedInformation.vehiclestate));
            updateState(TeslascopeBindingConstants.CHANNEL_ODOMETER,
                    new QuantityType<>(detailedInformation.odometer, ImperialUnits.MILE));

            // charge state
            updateState(TeslascopeBindingConstants.CHANNEL_BATTERYLEVEL,
                    new DecimalType(detailedInformation.battery_level));
            updateState(TeslascopeBindingConstants.CHANNEL_USABLEBATTERYLEVEL,
                    new DecimalType(detailedInformation.usable_battery_level));
            updateState(TeslascopeBindingConstants.CHANNEL_BATTERYRANGE,
                    new QuantityType<>(detailedInformation.battery_range, ImperialUnits.MILE));
            updateState(TeslascopeBindingConstants.CHANNEL_ESTIMATEDBATTERYRANGE,
                    new QuantityType<>(detailedInformation.est_battery_range, ImperialUnits.MILE));
            // charge_enable_request isn't the right flag to determine if car is charging or not
            if (detailedInformation.chargingstate.equals("Charging")) {
                updateState(TeslascopeBindingConstants.CHANNEL_CHARGE, OnOffType.ON);
            } else {
                updateState(TeslascopeBindingConstants.CHANNEL_CHARGE, OnOffType.OFF);
            }
            updateState(TeslascopeBindingConstants.CHANNEL_CHARGEENERGYADDED,
                    new QuantityType<>(detailedInformation.charge_energy_added, Units.KILOWATT_HOUR));
            updateState(CHANNEL_CHARGELIMITSOCSTANDARD, new DecimalType(detailedInformation.charge_limit_soc / 100));
            updateState(TeslascopeBindingConstants.CHANNEL_CHARGEPORT,
                    OnOffType.from(detailedInformation.charge_port_door_open));
            updateState(TeslascopeBindingConstants.CHANNEL_CHARGERATE,
                    new QuantityType<>(detailedInformation.charge_rate, ImperialUnits.MILES_PER_HOUR));
            updateState(TeslascopeBindingConstants.CHANNEL_CHARGERPOWER,
                    new QuantityType<>(detailedInformation.charger_power, MetricPrefix.KILO(Units.WATT)));
            updateState(TeslascopeBindingConstants.CHANNEL_CHARGERVOLTAGE,
                    new QuantityType<>(detailedInformation.charger_voltage, Units.VOLT));
            updateState(TeslascopeBindingConstants.CHANNEL_TIMETOFULLCHARGE,
                    new QuantityType<>(detailedInformation.time_to_full_charge, Units.HOUR));
            updateState(TeslascopeBindingConstants.CHANNEL_CHARGINGSTATE,
                    new StringType(detailedInformation.chargingstate));
            updateState(TeslascopeBindingConstants.CHANNEL_SCHEDULEDCHARGINGPENDING,
                    OnOffType.from(detailedInformation.scheduled_charging_pending));
            updateState(TeslascopeBindingConstants.CHANNEL_SCHEDULEDCHARGINGSTART,
                    new StringType(detailedInformation.scheduled_charging_start_time));

            // climate state
            updateState(TeslascopeBindingConstants.CHANNEL_AUTOCONDITIONING,
                    OnOffType.from(detailedInformation.is_auto_conditioning_on));
            updateState(TeslascopeBindingConstants.CHANNEL_CLIMATE, OnOffType.from(detailedInformation.is_climate_on));
            updateState(TeslascopeBindingConstants.CHANNEL_FRONTDEFROSTER,
                    OnOffType.from(detailedInformation.is_front_defroster_on));
            updateState(TeslascopeBindingConstants.CHANNEL_PRECONDITIONING,
                    OnOffType.from(detailedInformation.is_preconditioning));
            updateState(TeslascopeBindingConstants.CHANNEL_REARDEFROSTER,
                    OnOffType.from(detailedInformation.is_rear_defroster_on));
            updateState(TeslascopeBindingConstants.CHANNEL_LEFTSEATHEATER,
                    new DecimalType(detailedInformation.seat_heater_left));
            updateState(TeslascopeBindingConstants.CHANNEL_CENTERREARSEATHEATER,
                    new DecimalType(detailedInformation.seat_heater_rear_center));
            updateState(TeslascopeBindingConstants.CHANNEL_LEFTREARSEATHEATER,
                    new DecimalType(detailedInformation.seat_heater_rear_left));
            updateState(TeslascopeBindingConstants.CHANNEL_RIGHTREARSEATHEATER,
                    new DecimalType(detailedInformation.seat_heater_rear_right));
            updateState(TeslascopeBindingConstants.CHANNEL_RIGHTSEATHEATER,
                    new DecimalType(detailedInformation.seat_heater_right));
            updateState(TeslascopeBindingConstants.CHANNEL_SIDEMIRRORHEATERS,
                    OnOffType.from(detailedInformation.side_mirror_heaters));
            updateState(TeslascopeBindingConstants.CHANNEL_SMARTPRECONDITIONG,
                    OnOffType.from(detailedInformation.smart_preconditioning));
            updateState(TeslascopeBindingConstants.CHANNEL_STEERINGWHEELHEATER,
                    OnOffType.from(detailedInformation.steering_wheel_heater));
            updateState(TeslascopeBindingConstants.CHANNEL_WIPERBLADEHEATER,
                    OnOffType.from(detailedInformation.wiper_blade_heater));
            updateState(TeslascopeBindingConstants.CHANNEL_DRIVERTEMP,
                    new QuantityType<>(detailedInformation.driver_temp_setting, SIUnits.CELSIUS));
            updateState(TeslascopeBindingConstants.CHANNEL_INSIDETEMP,
                    new QuantityType<>(detailedInformation.inside_temp, SIUnits.CELSIUS));
            updateState(TeslascopeBindingConstants.CHANNEL_OUTSIDETEMP,
                    new QuantityType<>(detailedInformation.outside_temp, SIUnits.CELSIUS));
            updateState(TeslascopeBindingConstants.CHANNEL_PASSENGERTEMP,
                    new QuantityType<>(detailedInformation.passenger_temp_setting, SIUnits.CELSIUS));
            updateState(TeslascopeBindingConstants.CHANNEL_FAN, new DecimalType(detailedInformation.fan_status));
            updateState(TeslascopeBindingConstants.CHANNEL_LEFTTEMPDIRECTION,
                    new DecimalType(detailedInformation.left_temp_direction));
            updateState(TeslascopeBindingConstants.CHANNEL_MAXAVAILABLETEMP,
                    new QuantityType<>(detailedInformation.max_avail_temp, SIUnits.CELSIUS));
            updateState(TeslascopeBindingConstants.CHANNEL_MINAVAILABLETEMP,
                    new QuantityType<>(detailedInformation.min_avail_temp, SIUnits.CELSIUS));
            updateState(TeslascopeBindingConstants.CHANNEL_RIGHTTEMPDIRECTION,
                    new DecimalType(detailedInformation.right_temp_direction));

            // drive state
            updateState(TeslascopeBindingConstants.CHANNEL_HEADING, new DecimalType(detailedInformation.heading));
            updateState(TeslascopeBindingConstants.CHANNEL_LOCATION,
                    new PointType(detailedInformation.latitude + "," + detailedInformation.longitude));
            updateState(TeslascopeBindingConstants.CHANNEL_POWER,
                    new QuantityType<>(detailedInformation.power, MetricPrefix.KILO(Units.WATT)));
            updateState(TeslascopeBindingConstants.CHANNEL_SHIFTSTATE, new StringType(detailedInformation.shift_state));
            updateState(TeslascopeBindingConstants.CHANNEL_SPEED,
                    new QuantityType<>(detailedInformation.speed, ImperialUnits.MILES_PER_HOUR));

            // vehicle state
            updateState(TeslascopeBindingConstants.CHANNEL_DOORLOCK, OnOffType.from(detailedInformation.locked));
            updateState(TeslascopeBindingConstants.CHANNEL_SENTRYMODE, OnOffType.from(detailedInformation.sentry_mode));
            updateState(TeslascopeBindingConstants.CHANNEL_VALETMODE, OnOffType.from(detailedInformation.valet_mode));
            updateState(TeslascopeBindingConstants.CHANNEL_SOFTWAREUPDATESTATUS,
                    new StringType(detailedInformation.software_update_status));
            updateState(TeslascopeBindingConstants.CHANNEL_SOFTWAREUPDATEVERSION,
                    new StringType(detailedInformation.software_update_version));
            updateState(TeslascopeBindingConstants.CHANNEL_SUNROOFSTATE,
                    new StringType(detailedInformation.sun_roof_state));
            updateState(TeslascopeBindingConstants.CHANNEL_SUNROOF,
                    new DecimalType(detailedInformation.sun_roof_percent_open));
            updateState(TeslascopeBindingConstants.CHANNEL_HOMELINK,
                    OnOffType.from(detailedInformation.homelink_nearby));
            updateState(TeslascopeBindingConstants.CHANNEL_TPMSFL,
                    new QuantityType<>(detailedInformation.tpms_pressure_fl, Units.BAR));
            updateState(TeslascopeBindingConstants.CHANNEL_TPMSFR,
                    new QuantityType<>(detailedInformation.tpms_pressure_fr, Units.BAR));
            updateState(TeslascopeBindingConstants.CHANNEL_TPMSRL,
                    new QuantityType<>(detailedInformation.tpms_pressure_rl, Units.BAR));
            updateState(TeslascopeBindingConstants.CHANNEL_TPMSRR,
                    new QuantityType<>(detailedInformation.tpms_pressure_rr, Units.BAR));
            updateState(TeslascopeBindingConstants.CHANNEL_TPMSSOFTWARNINGFL,
                    OnOffType.from(detailedInformation.tpms_soft_warning_fl));
            updateState(TeslascopeBindingConstants.CHANNEL_TPMSSOFTWARNINGFR,
                    OnOffType.from(detailedInformation.tpms_soft_warning_fr));
            updateState(TeslascopeBindingConstants.CHANNEL_TPMSSOFTWARNINGRL,
                    OnOffType.from(detailedInformation.tpms_soft_warning_rl));
            updateState(TeslascopeBindingConstants.CHANNEL_TPMSSOFTWARNINGRR,
                    OnOffType.from(detailedInformation.tpms_soft_warning_rr));
            updateState(TeslascopeBindingConstants.CHANNEL_DRIVERFRONTDOOR,
                    detailedInformation.df ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(TeslascopeBindingConstants.CHANNEL_DRIVERREARDOOR,
                    detailedInformation.dr ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(TeslascopeBindingConstants.CHANNEL_PASSENGERFRONTDOOR,
                    detailedInformation.pf ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(TeslascopeBindingConstants.CHANNEL_PASSENGERREARDOOR,
                    detailedInformation.pr ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(TeslascopeBindingConstants.CHANNEL_FRONTTRUNK, OnOffType.from(detailedInformation.ft));
            updateState(TeslascopeBindingConstants.CHANNEL_REARTRUNK, OnOffType.from(detailedInformation.rt));

            // virtual items
            updateState(TeslascopeBindingConstants.CHANNEL_HONKHORN, OnOffType.OFF);
            updateState(TeslascopeBindingConstants.CHANNEL_FLASHLIGHTS, OnOffType.OFF);

        } catch (TeslascopeCommunicationException e) {
            logger.debug("Unexpected error connecting to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected void ac(boolean b) throws TeslascopeCommunicationException {
        webTargets.sendCommand(publicID, apiKey, b ? "startAC" : "stopAC");
    }

    protected void charge(boolean b) throws TeslascopeCommunicationException {
        webTargets.sendCommand(publicID, apiKey, b ? "startCharging" : "stopCharging");
    }

    protected void chargeDoor(boolean b) throws TeslascopeCommunicationException {
        webTargets.sendCommand(publicID, apiKey, b ? "openChargeDoor" : "closeChargeDoor");
    }

    protected void flashLights() throws TeslascopeCommunicationException {
        webTargets.sendCommand(publicID, apiKey, "flashLights");
        updateState(TeslascopeBindingConstants.CHANNEL_FLASHLIGHTS, OnOffType.OFF);
    }

    protected void honkHorn() throws TeslascopeCommunicationException {
        webTargets.sendCommand(publicID, apiKey, "honkHorn");
        updateState(TeslascopeBindingConstants.CHANNEL_HONKHORN, OnOffType.OFF);
    }

    protected void openFrunk() throws TeslascopeCommunicationException {
        webTargets.sendCommand(publicID, apiKey, "openFrunk");
    }

    protected void openTrunk() throws TeslascopeCommunicationException {
        webTargets.sendCommand(publicID, apiKey, "openTrunk");
    }

    protected void sentry(boolean b) throws TeslascopeCommunicationException {
        webTargets.sendCommand(publicID, apiKey, b ? "enableSentryMode" : "disableSentryMode");
    }
}
