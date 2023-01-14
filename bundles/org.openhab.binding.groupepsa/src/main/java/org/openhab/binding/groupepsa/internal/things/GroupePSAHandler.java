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
package org.openhab.binding.groupepsa.internal.things;

import static org.openhab.binding.groupepsa.internal.GroupePSABindingConstants.*;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groupepsa.internal.bridge.GroupePSABridgeHandler;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Air;
import org.openhab.binding.groupepsa.internal.rest.api.dto.AirConditioning;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Battery;
import org.openhab.binding.groupepsa.internal.rest.api.dto.BatteryStatus;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Charging;
import org.openhab.binding.groupepsa.internal.rest.api.dto.DoorsState;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Energy;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Environment;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Health;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Ignition;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Kinetic;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Luminosity;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Odometer;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Opening;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Position;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Preconditionning;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Privacy;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Properties;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Safety;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Service;
import org.openhab.binding.groupepsa.internal.rest.api.dto.VehicleStatus;
import org.openhab.binding.groupepsa.internal.rest.exceptions.GroupePSACommunicationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.filosganga.geogson.model.Geometry;
import com.github.filosganga.geogson.model.positions.SinglePosition;

/**
 * The {@link GroupePSAHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class GroupePSAHandler extends BaseThingHandler {
    private static final long DEFAULT_POLLING_INTERVAL_M = TimeUnit.MINUTES.toMinutes(1);
    private static final long DEFAULT_ONLINE_INTERVAL_M = TimeUnit.MINUTES.toMinutes(60);

    private final Logger logger = LoggerFactory.getLogger(GroupePSAHandler.class);

    private final TimeZoneProvider timeZoneProvider;

    private @Nullable String id = null;
    private long lastQueryTimeNs = 0L;

    private @Nullable ScheduledFuture<?> groupepsaPollingJob;
    private long maxQueryFrequencyNanos = TimeUnit.MINUTES.toNanos(1);
    private long onlineIntervalM;

    public GroupePSAHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    protected @Nullable Bridge getBridge() {
        return super.getBridge();
    }

    private void pollStatus() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            updateGroupePSAState();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    };

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshChannels(channelUID);
        }
    }

    private void refreshChannels(ChannelUID channelUID) {
        updateGroupePSAState();
    }

    @Override
    public void initialize() {
        if (getBridgeHandler() != null) {
            GroupePSAConfiguration currentConfig = getConfigAs(GroupePSAConfiguration.class);
            final String id = currentConfig.getId();
            final Integer pollingIntervalM = currentConfig.getPollingInterval();
            final Integer onlineIntervalM = currentConfig.getOnlineInterval();

            if (id == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/conf-error-no-vehicle-id");
            } else if (pollingIntervalM != null && pollingIntervalM < 1) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/conf-error-invalid-polling-interval");
            } else if (onlineIntervalM != null && onlineIntervalM < 1) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/conf-error-invalid-online-interval");
            } else {
                this.id = id;
                this.onlineIntervalM = onlineIntervalM != null ? onlineIntervalM : DEFAULT_ONLINE_INTERVAL_M;
                startGroupePSAPolling(pollingIntervalM);
            }

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Nullable
    public GroupePSABridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof GroupePSABridgeHandler) {
                return (GroupePSABridgeHandler) handler;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        stopGroupePSAPolling();
        id = null;
    }

    private void startGroupePSAPolling(@Nullable Integer pollingIntervalM) {
        if (groupepsaPollingJob == null) {
            final long pollingIntervalToUse = pollingIntervalM == null ? DEFAULT_POLLING_INTERVAL_M : pollingIntervalM;
            groupepsaPollingJob = scheduler.scheduleWithFixedDelay(() -> pollStatus(), 1, pollingIntervalToUse * 60,
                    TimeUnit.SECONDS);
        }
    }

    private void stopGroupePSAPolling() {
        ScheduledFuture<?> job = groupepsaPollingJob;
        if (job != null) {
            job.cancel(true);
            groupepsaPollingJob = null;
        }
    }

    private boolean isValidResult(VehicleStatus vehicle) {
        return vehicle.getUpdatedAt() != null;
    }

    private boolean isConnected(VehicleStatus vehicle) {
        ZonedDateTime updatedAt = vehicle.getUpdatedAt();
        if (updatedAt == null) {
            return false;
        }

        return updatedAt.isAfter(ZonedDateTime.now().minusMinutes(onlineIntervalM));
    }

    private synchronized void updateGroupePSAState() {
        if (System.nanoTime() - lastQueryTimeNs <= maxQueryFrequencyNanos) {
            return;
        }

        lastQueryTimeNs = System.nanoTime();

        String id = this.id;
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/conf-error-no-vehicle-id");
            return;
        }

        GroupePSABridgeHandler groupepsaBridge = getBridgeHandler();
        if (groupepsaBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/conf-error-no-bridge");
            return;
        }

        try {
            VehicleStatus vehicle = groupepsaBridge.getVehicleStatus(id);

            if (vehicle != null && isValidResult(vehicle)) {
                logger.trace("Vehicle: {}", vehicle.toString());

                logger.debug("Update vehicle state now: {}, lastupdate: {}", ZonedDateTime.now(),
                        vehicle.getUpdatedAt());

                updateChannelState(vehicle);

                if (isConnected(vehicle)) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "@text/comm-error-vehicle-not-connected-to-cloud");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error-query-vehicle-failed");
            }
        } catch (GroupePSACommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    MessageFormat.format("@text/comm-error-query-vehicle-failed", e.getMessage()));
        }
    }

    private void updateChannelState(VehicleStatus vehicle) {
        final DoorsState doorsState = vehicle.getDoorsState();
        if (doorsState != null) {
            buildDoorChannels(doorsState);

            List<Opening> openings = doorsState.getOpening();
            if (openings != null) {
                for (Opening opening : openings) {
                    String id = opening.getIdentifier();
                    if (id != null) {
                        ChannelUID channelUID = new ChannelUID(getThing().getUID(), CHANNEL_GROUP_DOORS,
                                id.toLowerCase());
                        updateState(channelUID, "open".equalsIgnoreCase(opening.getState()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                    }
                }
            }

            List<String> lockedState = doorsState.getLockedState();
            updateState(CHANNEL_DOORS_LOCK, lockedState, x -> x.get(0));
        } else {
            updateState(CHANNEL_DOORS_LOCK, UnDefType.UNDEF);
        }

        updateState(CHANNEL_BATTERY_CURRENT, vehicle.getBattery(), Battery::getCurrent, Units.AMPERE);
        updateState(CHANNEL_BATTERY_VOLTAGE, vehicle.getBattery(), Battery::getVoltage, Units.VOLT);

        updateState(CHANNEL_ENVIRONMENT_TEMPERATURE, vehicle.getEnvironment(), Environment::getAir, Air::getTemp,
                SIUnits.CELSIUS);
        updateStateBoolean(CHANNEL_ENVIRONMENT_DAYTIME, vehicle.getEnvironment(), Environment::getLuminosity,
                Luminosity::isDay);

        updateState(CHANNEL_MOTION_IGNITION, vehicle.getIgnition(), Ignition::getType);

        updateStateBoolean(CHANNEL_MOTION_MOVING, vehicle.getKinetic(), Kinetic::isMoving);
        updateState(CHANNEL_MOTION_ACCELERATION, vehicle.getKinetic(), Kinetic::getAcceleration,
                Units.METRE_PER_SQUARE_SECOND);
        updateState(CHANNEL_MOTION_SPEED, vehicle.getKinetic(), Kinetic::getSpeed, SIUnits.KILOMETRE_PER_HOUR);

        updateState(CHANNEL_MOTION_MILEAGE, vehicle.getOdometer(), Odometer::getMileage,
                MetricPrefix.KILO(SIUnits.METRE));

        Position lastPosition = vehicle.getLastPosition();
        if (lastPosition != null) {
            Geometry<SinglePosition> geometry = lastPosition.getGeometry();
            if (geometry != null) {
                SinglePosition position = geometry.positions();
                if (Double.isFinite(position.alt())) {
                    updateState(CHANNEL_POSITION_POSITION, new PointType(new DecimalType(position.lat()),
                            new DecimalType(position.lon()), new DecimalType(position.alt())));
                } else {
                    updateState(CHANNEL_POSITION_POSITION,
                            new PointType(new DecimalType(position.lat()), new DecimalType(position.lon())));
                }
            } else {
                updateState(CHANNEL_POSITION_POSITION, UnDefType.UNDEF);
            }
            updateState(CHANNEL_POSITION_HEADING, lastPosition.getProperties(), Properties::getHeading,
                    Units.DEGREE_ANGLE);
            updateState(CHANNEL_POSITION_TYPE, lastPosition.getProperties(), Properties::getType);
            updateState(CHANNEL_POSITION_SIGNALSTRENGTH, lastPosition.getProperties(), Properties::getSignalQuality,
                    Units.PERCENT);
        }

        updateState(CHANNEL_VARIOUS_LAST_UPDATED, vehicle.getUpdatedAt());
        updateState(CHANNEL_VARIOUS_PRIVACY, vehicle.getPrivacy(), Privacy::getState);
        updateState(CHANNEL_VARIOUS_BELT, vehicle.getSafety(), Safety::getBeltWarning);
        updateState(CHANNEL_VARIOUS_EMERGENCY, vehicle.getSafety(), Safety::getECallTriggeringRequest);
        updateState(CHANNEL_VARIOUS_SERVICE, vehicle.getService(), Service::getType);
        updateState(CHANNEL_VARIOUS_PRECONDITINING, vehicle.getPreconditionning(), Preconditionning::getAirConditioning,
                AirConditioning::getStatus);
        updateState(CHANNEL_VARIOUS_PRECONDITINING_FAILURE, vehicle.getPreconditionning(),
                Preconditionning::getAirConditioning, AirConditioning::getFailureCause);

        List<Energy> energies = vehicle.getEnergy();
        if (energies != null) {
            for (Energy energy : energies) {
                if ("Fuel".equalsIgnoreCase(energy.getType())) {
                    updateState(CHANNEL_FUEL_AUTONOMY, energy, Energy::getAutonomy, MetricPrefix.KILO(SIUnits.METRE));
                    updateState(CHANNEL_FUEL_CONSUMPTION, energy, Energy::getConsumption,
                            Units.LITRE.divide(MetricPrefix.KILO(SIUnits.METRE)));
                    updateState(CHANNEL_FUEL_LEVEL, energy, Energy::getLevel, Units.PERCENT);
                } else if ("Electric".equalsIgnoreCase(energy.getType())) {
                    updateState(CHANNEL_ELECTRIC_AUTONOMY, energy, Energy::getAutonomy,
                            MetricPrefix.KILO(SIUnits.METRE));
                    updateState(CHANNEL_ELECTRIC_RESIDUAL, energy, Energy::getResidual, Units.KILOWATT_HOUR);
                    updateState(CHANNEL_ELECTRIC_LEVEL, energy, Energy::getLevel, Units.PERCENT);

                    updateState(CHANNEL_ELECTRIC_BATTERY_CAPACITY, energy, Energy::getBattery,
                            BatteryStatus::getCapacity, Units.KILOWATT_HOUR);
                    updateState(CHANNEL_ELECTRIC_BATTERY_HEALTH_CAPACITY, energy, Energy::getBattery,
                            BatteryStatus::getHealth, Health::getCapacity, Units.PERCENT);
                    updateState(CHANNEL_ELECTRIC_BATTERY_HEALTH_RESISTANCE, energy, Energy::getBattery,
                            BatteryStatus::getHealth, Health::getResistance, Units.PERCENT);

                    updateState(CHANNEL_ELECTRIC_CHARGING_STATUS, energy, Energy::getCharging, Charging::getStatus);
                    updateState(CHANNEL_ELECTRIC_CHARGING_MODE, energy, Energy::getCharging, Charging::getChargingMode);
                    updateStateBoolean(CHANNEL_ELECTRIC_CHARGING_PLUGGED, energy, Energy::getCharging,
                            Charging::isPlugged);
                    updateState(CHANNEL_ELECTRIC_CHARGING_RATE, energy, Energy::getCharging, Charging::getChargingRate,
                            SIUnits.KILOMETRE_PER_HOUR);

                    updateState(CHANNEL_ELECTRIC_CHARGING_REMAININGTIME, energy, Energy::getCharging,
                            Charging::getRemainingTime, x -> new BigDecimal(x.getSeconds()), Units.SECOND);
                    updateState(CHANNEL_ELECTRIC_CHARGING_NEXTDELAYEDTIME, energy, Energy::getCharging,
                            Charging::getNextDelayedTime, x -> new BigDecimal(x.getSeconds()), Units.SECOND);

                }
            }
        }
    }

    void buildDoorChannels(final DoorsState doorsState) {
        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            return;
        }

        ThingBuilder thingBuilder = editThing();
        List<Channel> channels = getThing().getChannelsOfGroup(CHANNEL_GROUP_DOORS);
        thingBuilder.withoutChannels(channels);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), CHANNEL_DOORS_LOCK);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_DOORLOCK);
        thingBuilder.withChannel(callback.createChannelBuilder(channelUID, channelTypeUID).build());

        List<Opening> openings = doorsState.getOpening();
        if (openings != null) {
            for (Opening opening : openings) {
                String id = opening.getIdentifier();
                if (id != null) {
                    channelUID = new ChannelUID(getThing().getUID(), CHANNEL_GROUP_DOORS, id.toLowerCase());
                    channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_DOOROPEN);
                    thingBuilder.withChannel(callback.createChannelBuilder(channelUID, channelTypeUID).build());
                }
            }
        }

        updateThing(thingBuilder.build());
    }

    // Various update helper functions

    protected <T extends Quantity<T>> void updateState(String channelID, @Nullable BigDecimal number, Unit<T> unit) {
        if (number != null) {
            updateState(channelID, new QuantityType<T>(number, unit));
        } else {
            updateState(channelID, UnDefType.UNDEF);
        }
    }

    protected <T1, T2 extends Quantity<T2>> void updateState(String channelID, final @Nullable T1 object,
            Function<? super T1, @Nullable BigDecimal> mapper, Unit<T2> unit) {
        updateState(channelID, object != null ? mapper.apply(object) : null, unit);
    }

    protected <T1, T2, T3 extends Quantity<T3>> void updateState(String channelID, final @Nullable T1 object1,
            Function<? super T1, @Nullable T2> mapper1, Function<? super T2, @Nullable BigDecimal> mapper2,
            Unit<T3> unit) {
        final @Nullable T2 object2 = object1 != null ? mapper1.apply(object1) : null;
        updateState(channelID, object2 != null ? mapper2.apply(object2) : null, unit);
    }

    protected <T1, T2, T3, T4 extends Quantity<T4>> void updateState(String channelID, final @Nullable T1 object1,
            Function<? super T1, @Nullable T2> mapper1, Function<? super T2, @Nullable T3> mapper2,
            Function<? super T3, @Nullable BigDecimal> mapper3, Unit<T4> unit) {
        final @Nullable T2 object2 = object1 != null ? mapper1.apply(object1) : null;
        final @Nullable T3 object3 = object2 != null ? mapper2.apply(object2) : null;
        updateState(channelID, object3 != null ? mapper3.apply(object3) : null, unit);
    }

    protected void updateState(String channelID, @Nullable ZonedDateTime date) {
        if (date != null) {
            updateState(channelID, new DateTimeType(date).toZone(timeZoneProvider.getTimeZone()));
        } else {
            updateState(channelID, UnDefType.UNDEF);
        }
    }

    protected <T1> void updateStateDate(String channelID, @Nullable T1 object,
            Function<? super T1, @Nullable ZonedDateTime> mapper) {
        updateState(channelID, object != null ? mapper.apply(object) : null);
    }

    protected <T1, T2> void updateStateDate(String channelID, @Nullable T1 object1,
            Function<? super T1, @Nullable T2> mapper1, Function<? super T2, @Nullable ZonedDateTime> mapper2) {
        final @Nullable T2 object2 = object1 != null ? mapper1.apply(object1) : null;
        updateState(channelID, object2 != null ? mapper2.apply(object2) : null);
    }

    protected void updateState(String channelID, @Nullable String text) {
        if (text != null) {
            updateState(channelID, new StringType(text));
        } else {
            updateState(channelID, UnDefType.UNDEF);
        }
    }

    protected <T1> void updateState(String channelID, @Nullable T1 object,
            Function<? super T1, @Nullable String> mapper) {
        updateState(channelID, object != null ? mapper.apply(object) : null);
    }

    protected <T1, T2> void updateState(String channelID, @Nullable T1 object1,
            Function<? super T1, @Nullable T2> mapper1, Function<? super T2, @Nullable String> mapper2) {
        final @Nullable T2 object2 = object1 != null ? mapper1.apply(object1) : null;
        updateState(channelID, object2 != null ? mapper2.apply(object2) : null);
    }

    protected void updateState(String channelID, @Nullable Boolean value) {
        if (value != null) {
            updateState(channelID, value ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        } else {
            updateState(channelID, UnDefType.UNDEF);
        }
    }

    protected <T1> void updateStateBoolean(String channelID, @Nullable T1 object,
            Function<? super T1, @Nullable Boolean> mapper) {
        updateState(channelID, object != null ? mapper.apply(object) : null);
    }

    protected <T1, T2> void updateStateBoolean(String channelID, final @Nullable T1 object1,
            Function<? super T1, @Nullable T2> mapper1, Function<? super T2, @Nullable Boolean> mapper2) {
        final @Nullable T2 object2 = object1 != null ? mapper1.apply(object1) : null;
        updateState(channelID, object2 != null ? mapper2.apply(object2) : null);
    }
}
