/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.handler;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evohome.internal.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.action.EvohomeZoneActions;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient.CachedSchedule;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.HeatSetpointStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.TemperatureStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.ZoneStatus;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvohomeHeatingZoneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Neil Renaud - Working implementation
 * @author Jasper van Zuijlen - Refactor + Permanent Zone temperature setting
 * @author Leo Siepel - Add UoM
 * @author Kovacs Istvan - Unavailable measurements and explicit refresh
 * @author Kovacs Istvan - Schedule and temporary override controls
 * @author Kovacs Istvan - Weekly program and next scheduled target
 */
@NonNullByDefault
public class EvohomeHeatingZoneHandler extends BaseEvohomeHandler {

    private static final int CANCEL_SET_POINT_OVERRIDE = 0;
    private static final Set<String> SCHEDULE_CHANNELS = Set.of(EvohomeBindingConstants.ZONE_WEEKLY_SCHEDULE_CHANNEL,
            EvohomeBindingConstants.ZONE_NEXT_SWITCHPOINT_CHANNEL,
            EvohomeBindingConstants.ZONE_NEXT_SCHEDULED_TEMPERATURE_CHANNEL,
            EvohomeBindingConstants.ZONE_SCHEDULE_LAST_UPDATED_CHANNEL);
    private final Logger logger = LoggerFactory.getLogger(EvohomeHeatingZoneHandler.class);
    private volatile @Nullable SetPointContext actualSetPoint;
    private final AtomicReference<@Nullable PendingSetPoint> pendingSetPoint = new AtomicReference<>();
    private volatile boolean disposed;

    private record SetPointContext(double temperature, String mode, @Nullable Instant until) {
    }

    private record PendingSetPoint(SetPointContext context, Instant validUntil) {
    }

    public EvohomeHeatingZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        disposed = false;
        super.initialize();
        int duration = getEvohomeThingConfig().temporaryOverrideDuration;
        if (duration < 0 || duration > 1440) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Temporary override duration must be between 0 and 1440 minutes");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(EvohomeZoneActions.class);
    }

    @Override
    public synchronized void dispose() {
        disposed = true;
        actualSetPoint = null;
        pendingSetPoint.set(null);
        super.dispose();
    }

    public boolean hasScheduleLinks() {
        return !disposed && SCHEDULE_CHANNELS.stream()
                .anyMatch(channel -> isLinked(new ChannelUID(getThing().getUID(), channel)));
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (SCHEDULE_CHANNELS.contains(channelUID.getId())) {
            if (!disposed) {
                requestUpdate();
            }
        } else {
            super.channelLinked(channelUID);
        }
    }

    public synchronized void updateSchedule(@Nullable CachedSchedule cached, Instant now) {
        if (disposed) {
            return;
        }
        if (cached == null) {
            SCHEDULE_CHANNELS.forEach(channel -> updateState(channel, UnDefType.UNDEF));
            return;
        }
        String json;
        try {
            json = cached.schedule().toJson();
        } catch (IllegalArgumentException | DateTimeException e) {
            logger.debug("Thing {}: cached weekly program is invalid: {}", getThing().getUID(), e.getMessage());
            SCHEDULE_CHANNELS.forEach(channel -> updateState(channel, UnDefType.UNDEF));
            return;
        }
        updateState(EvohomeBindingConstants.ZONE_WEEKLY_SCHEDULE_CHANNEL, new StringType(json));
        updateState(EvohomeBindingConstants.ZONE_SCHEDULE_LAST_UPDATED_CHANNEL,
                new DateTimeType(cached.updatedAt().atZone(ZoneOffset.UTC)));
        State timeState = UnDefType.UNDEF;
        State temperatureState = UnDefType.UNDEF;
        var timeZone = cached.timeZone();
        if (timeZone != null) {
            try {
                var next = cached.schedule().nextScheduledSwitchpoint(now, timeZone);
                timeState = new DateTimeType(next.time().atZone(ZoneOffset.UTC));
                temperatureState = new QuantityType<>(next.temperature(), SIUnits.CELSIUS);
            } catch (IllegalArgumentException | DateTimeException e) {
                logger.debug("Thing {}: next scheduled switchpoint could not be calculated: {}", getThing().getUID(),
                        e.getMessage());
            }
        }
        updateState(EvohomeBindingConstants.ZONE_NEXT_SWITCHPOINT_CHANNEL, timeState);
        updateState(EvohomeBindingConstants.ZONE_NEXT_SCHEDULED_TEMPERATURE_CHANNEL, temperatureState);
        logger.trace("Thing {}: weekly program updated at {}, timeZone={}, next switchpoint={}, scheduled target={}",
                getThing().getUID(), cached.updatedAt(), timeZone, timeState, temperatureState);
    }

    public void update(@Nullable ThingStatus tcsStatus, @Nullable ZoneStatus zoneStatus) {
        if (logger.isTraceEnabled() && zoneStatus != null) {
            TemperatureStatus temperature = zoneStatus.getTemperature();
            HeatSetpointStatus setpoint = zoneStatus.getHeatSetpoint();
            logger.trace(
                    "Zone {} (Thing {}): temperature={} Celsius, available={}, setpoint={} Celsius, mode={}, activeFaults={}, controllerStatus={}",
                    zoneStatus.getZoneId(), getThing().getUID(),
                    temperature == null ? null : temperature.getTemperature(),
                    temperature == null ? null : temperature.isAvailable(),
                    setpoint == null ? null : setpoint.getTargetTemperature(),
                    setpoint == null ? null : setpoint.getSetpointMode(), zoneStatus.hasActiveFaults(), tcsStatus);
        }
        // Make the zone offline when the related display is offline
        // If the related display is not a thing, ignore this
        if (tcsStatus != null && tcsStatus.equals(ThingStatus.OFFLINE)) {
            logger.trace("Thing {}: skipping zone channel updates because the controller is OFFLINE",
                    getThing().getUID());
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Display Controller offline");
        } else if (zoneStatus == null) {
            logger.trace("Thing {}: no status found for configured zone ID {}", getThing().getUID(), getId());
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Status not found, check the zone id");
        } else if (!handleActiveFaults(zoneStatus)) {
            updateEvohomeThingStatus(ThingStatus.ONLINE);

            TemperatureStatus temperatureStatus = zoneStatus.getTemperature();
            Double temperature = temperatureStatus == null ? null : temperatureStatus.getTemperature();
            State temperatureState = temperatureStatus != null && temperatureStatus.isAvailable() && temperature != null
                    && Double.isFinite(temperature) ? new QuantityType<>(temperature, SIUnits.CELSIUS)
                            : UnDefType.UNDEF;
            updateState(EvohomeBindingConstants.ZONE_TEMPERATURE_CHANNEL, temperatureState);
            HeatSetpointStatus setpoint = zoneStatus.getHeatSetpoint();
            if (setpoint == null || setpoint.getSetpointMode() == null
                    || !Double.isFinite(setpoint.getTargetTemperature())) {
                actualSetPoint = null;
                updateState(EvohomeBindingConstants.ZONE_SET_POINT_STATUS_CHANNEL, UnDefType.UNDEF);
                updateState(EvohomeBindingConstants.ZONE_SET_POINT_CHANNEL, UnDefType.UNDEF);
                updateState(EvohomeBindingConstants.ZONE_TEMPORARY_SET_POINT_CHANNEL, UnDefType.UNDEF);
                updateState(EvohomeBindingConstants.ZONE_SET_POINT_UNTIL_CHANNEL, UnDefType.UNDEF);
            } else {
                String mode = setpoint.getSetpointMode();
                Instant until = "TemporaryOverride".equals(mode) ? parseUntil(setpoint.getUntil()) : null;
                SetPointContext context = new SetPointContext(setpoint.getTargetTemperature(), mode, until);
                actualSetPoint = context;
                PendingSetPoint pending = pendingSetPoint.get();
                if (pending != null && (context.equals(pending.context())
                        || !pending.validUntil().isAfter(Instant.now())
                        || "FollowSchedule".equals(mode) && "FollowSchedule".equals(pending.context().mode()))) {
                    pendingSetPoint.compareAndSet(pending, null);
                }
                updateState(EvohomeBindingConstants.ZONE_SET_POINT_STATUS_CHANNEL, new StringType(mode));
                updateState(EvohomeBindingConstants.ZONE_SET_POINT_CHANNEL,
                        new QuantityType<>(context.temperature(), SIUnits.CELSIUS));
                updateState(EvohomeBindingConstants.ZONE_TEMPORARY_SET_POINT_CHANNEL,
                        new QuantityType<>(context.temperature(), SIUnits.CELSIUS));
                updateState(EvohomeBindingConstants.ZONE_SET_POINT_UNTIL_CHANNEL,
                        until == null ? UnDefType.UNDEF : new DateTimeType(until.atZone(ZoneOffset.UTC)));
                logger.trace("Thing {}: published Temperature={}, SetPoint={} Celsius, SetPointStatus={}, until={}",
                        getThing().getUID(), temperatureState, context.temperature(), mode, until);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            if (SCHEDULE_CHANNELS.contains(channelUID.getId())) {
                EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
                if (!disposed && bridge != null) {
                    bridge.requestScheduleRefresh(getId());
                }
            } else {
                requestUpdate();
            }
            return;
        }
        String channelId = channelUID.getId();
        if (EvohomeBindingConstants.ZONE_SET_POINT_STATUS_CHANNEL.equals(channelId) && command instanceof StringType) {
            String mode = command.toString();
            if ("FollowSchedule".equals(mode)) {
                cancelSetPointOverride();
            } else {
                SetPointContext context = getSetPointContext();
                if (context == null) {
                    logger.debug("Thing {}: cannot change override mode without a known target temperature",
                            getThing().getUID());
                } else if ("PermanentOverride".equals(mode)) {
                    setPermanentSetPoint(context.temperature());
                } else if ("TemporaryOverride".equals(mode)) {
                    setTemporarySetPoint(context.temperature());
                } else {
                    logger.debug("Thing {}: unsupported setpoint mode {}", getThing().getUID(), mode);
                }
            }
        } else if (EvohomeBindingConstants.ZONE_SET_POINT_CHANNEL.equals(channelId)
                || EvohomeBindingConstants.ZONE_TEMPORARY_SET_POINT_CHANNEL.equals(channelId)) {
            Double temperature = toCelsius(command);
            if (temperature == null) {
                logger.debug("Thing {}: target temperature command has no valid temperature value",
                        getThing().getUID());
                return;
            }
            if (EvohomeBindingConstants.ZONE_TEMPORARY_SET_POINT_CHANNEL.equals(channelId)) {
                setTemporarySetPoint(temperature);
            } else if (temperature == CANCEL_SET_POINT_OVERRIDE) {
                cancelSetPointOverride();
            } else {
                SetPointContext context = getSetPointContext();
                if (context != null && "TemporaryOverride".equals(context.mode())) {
                    Instant until = context.until();
                    if (until != null && until.isAfter(Instant.now())) {
                        setTemporarySetPoint(temperature, until);
                    } else {
                        // An expired or unparseable temporary override must never become permanent implicitly.
                        setTemporarySetPoint(temperature);
                    }
                } else {
                    setPermanentSetPoint(temperature);
                }
            }
        }
    }

    public boolean setPermanentSetPoint(double temperature) {
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        return bridge != null && bridge.setPermanentSetPoint(getId(), temperature,
                () -> rememberCommand(temperature, "PermanentOverride", null));
    }

    public boolean setTemporarySetPoint(double temperature, Instant until) {
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        return bridge != null && bridge.setTemporarySetPoint(getId(), temperature, until,
                () -> rememberCommand(temperature, "TemporaryOverride", until));
    }

    public boolean setTemporarySetPoint(double temperature) {
        int duration = getEvohomeThingConfig().temporaryOverrideDuration;
        if (duration == 0) {
            return setTemporarySetPointUntilNextSwitchpoint(temperature);
        }
        return duration > 0 && duration <= 1440
                && setTemporarySetPoint(temperature, Instant.now().plusSeconds(duration * 60L));
    }

    public boolean setTemporarySetPointUntilNextSwitchpoint(double temperature) {
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        return bridge != null && bridge.setTemporarySetPointUntilNextSwitchpoint(getId(), temperature,
                until -> rememberCommand(temperature, "TemporaryOverride", until)) != null;
    }

    public boolean cancelSetPointOverride() {
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        return bridge != null && bridge.cancelSetPointOverride(getId(), () -> {
            SetPointContext context = getSetPointContext();
            rememberCommand(context == null ? 0 : context.temperature(), "FollowSchedule", null);
        });
    }

    void resetCommandContext() {
        SetPointContext context = getSetPointContext();
        rememberCommand(context == null ? 0 : context.temperature(), "FollowSchedule", null);
    }

    private void rememberCommand(double temperature, String mode, @Nullable Instant until) {
        // Retain command intent briefly while the API readback may still show the previous override.
        // This context never publishes optimistic channel states.
        pendingSetPoint.set(new PendingSetPoint(
                new SetPointContext(temperature, mode, until == null ? null : until.truncatedTo(ChronoUnit.SECONDS)),
                Instant.now().plusSeconds(30)));
    }

    private @Nullable SetPointContext getSetPointContext() {
        PendingSetPoint pending = pendingSetPoint.get();
        return pending != null && pending.validUntil().isAfter(Instant.now()) ? pending.context() : actualSetPoint;
    }

    private @Nullable Double toCelsius(Command command) {
        Double value = null;
        if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> celsius = quantity.toUnit(SIUnits.CELSIUS);
            value = celsius == null ? null : celsius.doubleValue();
        } else if (command instanceof DecimalType decimal) {
            value = decimal.doubleValue();
        }
        return value != null && Double.isFinite(value) ? value : null;
    }

    private @Nullable Instant parseUntil(@Nullable String until) {
        if (until != null) {
            try {
                return OffsetDateTime.parse(until).toInstant();
            } catch (DateTimeException e) {
                logger.debug("Thing {}: temporary override expiry is not a valid offset date/time",
                        getThing().getUID());
            }
        }
        return null;
    }

    private boolean handleActiveFaults(ZoneStatus zoneStatus) {
        if (zoneStatus.hasActiveFaults()) {
            logger.trace("Thing {}: skipping zone channel updates because of active faults; firstFault={}",
                    getThing().getUID(), zoneStatus.getActiveFault(0).getFaultType());
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    zoneStatus.getActiveFault(0).getFaultType());
            return true;
        }
        return false;
    }
}
