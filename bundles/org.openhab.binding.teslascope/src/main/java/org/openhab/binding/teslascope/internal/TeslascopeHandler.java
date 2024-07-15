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

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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

import com.google.gson.Gson;

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

    private final Gson gson = new Gson();

    public TeslascopeHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        webTargets = new TeslascopeWebTargets(httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case TeslascopeBindingConstants.CHANNEL_AUTOCONDITIONING:
                    if (command instanceof OnOffType onOffCommand) {
                        setAutoConditioning(onOffCommand == OnOffType.ON);
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_CHARGE:
                    if (command instanceof OnOffType onOffCommand) {
                        charge(onOffCommand == OnOffType.ON);
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_CHARGE_PORT:
                    if (command instanceof OnOffType onOffCommand) {
                        chargeDoor(onOffCommand == OnOffType.ON);
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_DOOR_LOCK:
                    if (command instanceof OnOffType onOffCommand) {
                        lock(onOffCommand == OnOffType.ON);
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_FLASH_LIGHTS:
                    if (command instanceof OnOffType onOffCommand) {
                        flashLights();
                        return;
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_FRONT_TRUNK:
                    if (command instanceof OnOffType onOffCommand) {
                        openFrunk();
                        return;
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_HONK_HORN:
                    if (command instanceof OnOffType onOffCommand) {
                        honkHorn();
                        return;
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_REAR_TRUNK:
                    if (command instanceof OnOffType onOffCommand) {
                        openTrunk();
                        return;
                    }
                    break;
                case TeslascopeBindingConstants.CHANNEL_SENTRY_MODE:
                    if (command instanceof OnOffType onOffCommand) {
                        sentry(onOffCommand == OnOffType.ON);
                    }
                    break;
                default:
                    logger.debug("Received command ({}) of wrong type for thing '{}' on channel {}", command,
                            thing.getUID().getAsString(), channelUID.getId());
            }
        } catch (TeslascopeAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::pollStatus, 0, config.refreshInterval,
                TimeUnit.SECONDS);
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    private void pollStatus() {
        String response = "";

        try {
            response = webTargets.getDetailedInformation(config.publicID, config.apiKey);
            updateStatus(ThingStatus.ONLINE);
        } catch (TeslascopeAuthenticationException e) {
            logger.debug("Unexpected authentication error connecting to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        } catch (TeslascopeCommunicationException e) {
            logger.debug("Unexpected error connecting to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }

        DetailedInformation detailedInformation = gson.fromJson(response, DetailedInformation.class);

        updateState(TeslascopeBindingConstants.CHANNEL_VIN, new StringType(detailedInformation.vin));
        updateState(TeslascopeBindingConstants.CHANNEL_VEHICLE_NAME, new StringType(detailedInformation.name));
        updateState(TeslascopeBindingConstants.CHANNEL_VEHICLE_STATE, new StringType(detailedInformation.state));
        updateState(TeslascopeBindingConstants.CHANNEL_ODOMETER,
                new QuantityType<>(detailedInformation.odometer, ImperialUnits.MILE));

        // charge state
        updateState(TeslascopeBindingConstants.CHANNEL_BATTERY_LEVEL,
                new DecimalType(detailedInformation.battery_level));
        updateState(TeslascopeBindingConstants.CHANNEL_USABLE_BATTERY_LEVEL,
                new DecimalType(detailedInformation.usable_battery_level));
        updateState(TeslascopeBindingConstants.CHANNEL_BATTERY_RANGE,
                new QuantityType<>(detailedInformation.battery_range, ImperialUnits.MILE));
        updateState(TeslascopeBindingConstants.CHANNEL_ESTIMATED_BATTERY_RANGE,
                new QuantityType<>(detailedInformation.est_battery_range, ImperialUnits.MILE));
        // charge_enable_request isn't the right flag to determine if car is charging or not
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGE,
                OnOffType.from("Charging".equals(detailedInformation.charging_state)));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGE_ENERGY_ADDED,
                new QuantityType<>(detailedInformation.charge_energy_added, Units.KILOWATT_HOUR));
        updateState(CHANNEL_CHARGE_LIMIT_SOC_STANDARD, new DecimalType(detailedInformation.charge_limit_soc / 100));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGE_PORT,
                OnOffType.from(detailedInformation.charge_port_door_open));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGE_RATE,
                new QuantityType<>(detailedInformation.charge_rate, ImperialUnits.MILES_PER_HOUR));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGER_POWER,
                new QuantityType<>(detailedInformation.charger_power, MetricPrefix.KILO(Units.WATT)));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGER_VOLTAGE,
                new QuantityType<>(detailedInformation.charge_rate, Units.VOLT));
        updateState(TeslascopeBindingConstants.CHANNEL_TIME_TO_FULL_CHARGE,
                new QuantityType<>(detailedInformation.time_to_full_charge, Units.HOUR));
        updateState(TeslascopeBindingConstants.CHANNEL_CHARGING_STATE,
                new StringType(detailedInformation.charging_state));
        updateState(TeslascopeBindingConstants.CHANNEL_SCHEDULED_CHARGING_PENDING,
                OnOffType.from(detailedInformation.scheduled_charging_pending));
        updateState(TeslascopeBindingConstants.CHANNEL_SCHEDULED_CHARGING_START,
                new StringType(detailedInformation.scheduled_charging_start_time));

        // climate state
        updateState(TeslascopeBindingConstants.CHANNEL_AUTOCONDITIONING,
                OnOffType.from(detailedInformation.is_auto_conditioning_on));
        updateState(TeslascopeBindingConstants.CHANNEL_CLIMATE, OnOffType.from(detailedInformation.is_climate_on));
        updateState(TeslascopeBindingConstants.CHANNEL_FRONT_DEFROSTER,
                OnOffType.from(detailedInformation.is_front_defroster_on));
        updateState(TeslascopeBindingConstants.CHANNEL_PRECONDITIONING,
                OnOffType.from(detailedInformation.is_preconditioning));
        updateState(TeslascopeBindingConstants.CHANNEL_REAR_DEFROSTER,
                OnOffType.from(detailedInformation.is_rear_defroster_on));
        updateState(TeslascopeBindingConstants.CHANNEL_LEFT_SEAT_HEATER,
                new DecimalType(detailedInformation.seat_heater_left));
        updateState(TeslascopeBindingConstants.CHANNEL_CENTER_REAR_SEAT_HEATER,
                new DecimalType(detailedInformation.seat_heater_rear_center));
        updateState(TeslascopeBindingConstants.CHANNEL_LEFT_REAR_SEAT_HEATER,
                new DecimalType(detailedInformation.seat_heater_rear_left));
        updateState(TeslascopeBindingConstants.CHANNEL_RIGHT_REAR_SEAT_HEATER,
                new DecimalType(detailedInformation.seat_heater_rear_right));
        updateState(TeslascopeBindingConstants.CHANNEL_RIGHT_SEAT_HEATER,
                new DecimalType(detailedInformation.seat_heater_right));
        updateState(TeslascopeBindingConstants.CHANNEL_SIDE_MIRROR_HEATERS,
                OnOffType.from(detailedInformation.side_mirror_heaters));
        updateState(TeslascopeBindingConstants.CHANNEL_SMARTPRECONDITIONG,
                OnOffType.from(detailedInformation.smart_preconditioning));
        updateState(TeslascopeBindingConstants.CHANNEL_STEERING_WHEEL_HEATER,
                OnOffType.from(detailedInformation.steering_wheel_heater));
        updateState(TeslascopeBindingConstants.CHANNEL_WIPER_BLADE_HEATER,
                OnOffType.from(detailedInformation.wiper_blade_heater));
        updateState(TeslascopeBindingConstants.CHANNEL_DRIVER_TEMP,
                new QuantityType<>(detailedInformation.driver_temp_setting, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_INSIDE_TEMP,
                new QuantityType<>(detailedInformation.inside_temp, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_OUTSIDE_TEMP,
                new QuantityType<>(detailedInformation.outside_temp, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_PASSENGER_TEMP,
                new QuantityType<>(detailedInformation.passenger_temp_setting, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_FAN, new DecimalType(detailedInformation.fan_status));
        updateState(TeslascopeBindingConstants.CHANNEL_LEFT_TEMP_DIRECTION,
                new DecimalType(detailedInformation.left_temp_direction));
        updateState(TeslascopeBindingConstants.CHANNEL_MAX_AVAILABLE_TEMP,
                new QuantityType<>(detailedInformation.max_avail_temp, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_MIN_AVAILABLE_TEMP,
                new QuantityType<>(detailedInformation.min_avail_temp, SIUnits.CELSIUS));
        updateState(TeslascopeBindingConstants.CHANNEL_RIGHT_TEMP_DIRECTION,
                new DecimalType(detailedInformation.right_temp_direction));

        // drive state
        updateState(TeslascopeBindingConstants.CHANNEL_HEADING, new DecimalType(detailedInformation.heading));
        updateState(TeslascopeBindingConstants.CHANNEL_LOCATION,
                new PointType(detailedInformation.latitude + "," + detailedInformation.longitude));
        updateState(TeslascopeBindingConstants.CHANNEL_POWER,
                new QuantityType<>(detailedInformation.power, MetricPrefix.KILO(Units.WATT)));
        updateState(TeslascopeBindingConstants.CHANNEL_SHIFT_STATE, new StringType(detailedInformation.shift_state));
        updateState(TeslascopeBindingConstants.CHANNEL_SPEED,
                new QuantityType<>(detailedInformation.speed, ImperialUnits.MILES_PER_HOUR));

        // vehicle state
        updateState(TeslascopeBindingConstants.CHANNEL_DOOR_LOCK, OnOffType.from(detailedInformation.locked));
        updateState(TeslascopeBindingConstants.CHANNEL_SENTRY_MODE, OnOffType.from(detailedInformation.sentry_mode));
        updateState(TeslascopeBindingConstants.CHANNEL_VALET_MODE, OnOffType.from(detailedInformation.valet_mode));
        updateState(TeslascopeBindingConstants.CHANNEL_SOFTWARE_UPDATE_AVAILABLE,
                OnOffType.from(!"".equals(detailedInformation.software_update_status)));
        updateState(TeslascopeBindingConstants.CHANNEL_SOFTWARE_UPDATE_STATUS,
                new StringType(detailedInformation.software_update_status));
        updateState(TeslascopeBindingConstants.CHANNEL_SOFTWARE_UPDATE_VERSION,
                new StringType(detailedInformation.software_update_version));
        updateState(TeslascopeBindingConstants.CHANNEL_SUNROOF_STATE,
                new StringType(detailedInformation.sun_roof_state));
        updateState(TeslascopeBindingConstants.CHANNEL_SUNROOF,
                new DecimalType(detailedInformation.sun_roof_percent_open));
        updateState(TeslascopeBindingConstants.CHANNEL_HOMELINK, OnOffType.from(detailedInformation.homelink_nearby));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_FL,
                new QuantityType<>(detailedInformation.tpms_pressure_fl, Units.BAR));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_FR,
                new QuantityType<>(detailedInformation.tpms_pressure_fr, Units.BAR));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_RL,
                new QuantityType<>(detailedInformation.tpms_pressure_rl, Units.BAR));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_RR,
                new QuantityType<>(detailedInformation.tpms_pressure_rr, Units.BAR));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_SOFT_WARNING_FL,
                OnOffType.from(detailedInformation.tpms_soft_warning_fl));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_SOFT_WARNING_FR,
                OnOffType.from(detailedInformation.tpms_soft_warning_fr));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_SOFT_WARNING_RL,
                OnOffType.from(detailedInformation.tpms_soft_warning_rl));
        updateState(TeslascopeBindingConstants.CHANNEL_TPMS_SOFT_WARNING_RR,
                OnOffType.from(detailedInformation.tpms_soft_warning_rr));
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

    protected void setAutoConditioning(boolean b)
            throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        webTargets.sendCommand(config.publicID, config.apiKey, b ? "startAC" : "stopAC");
    }

    protected void charge(boolean b) throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        webTargets.sendCommand(config.publicID, config.apiKey, b ? "startCharging" : "stopCharging");
    }

    protected void chargeDoor(boolean b) throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        webTargets.sendCommand(config.publicID, config.apiKey, b ? "openChargeDoor" : "closeChargeDoor");
    }

    protected void flashLights() throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        webTargets.sendCommand(config.publicID, config.apiKey, "flashLights");
        updateState(TeslascopeBindingConstants.CHANNEL_FLASH_LIGHTS, OnOffType.OFF);
    }

    protected void honkHorn() throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        webTargets.sendCommand(config.publicID, config.apiKey, "honkHorn");
        updateState(TeslascopeBindingConstants.CHANNEL_HONK_HORN, OnOffType.OFF);
    }

    protected void lock(boolean b) throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        webTargets.sendCommand(config.publicID, config.apiKey, b ? "lock" : "unlock");
    }

    protected void openFrunk() throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        webTargets.sendCommand(config.publicID, config.apiKey, "openFrunk");
    }

    protected void openTrunk() throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        webTargets.sendCommand(config.publicID, config.apiKey, "openTrunk");
    }

    protected void sentry(boolean b) throws TeslascopeCommunicationException, TeslascopeAuthenticationException {
        webTargets.sendCommand(config.publicID, config.apiKey, b ? "enableSentryMode" : "disableSentryMode");
    }
}
