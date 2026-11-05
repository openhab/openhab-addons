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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evohome.internal.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.action.EvohomeZoneActions;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient.AcceptedSetPoint;
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
 * @author Kovacs Istvan - Serialized set point mode selection
 * @author Kovacs Istvan - Lifecycle-safe commands and accepted override context
 * @author Kovacs Istvan - Lifecycle-safe state publication
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
    private volatile boolean disposed = true;
    private long commandGeneration;

    private record SetPointContext(double temperature, String mode, @Nullable Instant until) {
    }

    private record PendingSetPoint(SetPointContext context, Instant readbackGraceUntil) {
    }

    private record ZoneCommand(EvohomeAccountBridgeHandler bridge, String zoneId, long generation, int duration) {
    }

    record ZoneUpdate(String zoneId, long generation) {
    }

    public EvohomeHeatingZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void initialize() {
        commandGeneration++;
        actualSetPoint = null;
        pendingSetPoint.set(null);
        super.initialize();
        disposed = false;
        int duration = getEvohomeThingConfig().temporaryOverrideDuration;
        if (duration < 0 || duration > 1440) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
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
        commandGeneration++;
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

    synchronized @Nullable ZoneUpdate captureUpdate() {
        return disposed ? null : new ZoneUpdate(getId(), commandGeneration);
    }

    private boolean isCurrent(ZoneUpdate update) {
        return !disposed && update.generation() == commandGeneration && update.zoneId().equals(getId());
    }

    public void updateSchedule(@Nullable CachedSchedule cached, Instant now) {
        ZoneUpdate update = captureUpdate();
        if (update != null) {
            updateSchedule(update, cached, now);
        }
    }

    synchronized void updateSchedule(ZoneUpdate update, @Nullable CachedSchedule cached, Instant now) {
        if (!isCurrent(update)) {
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
        ZoneUpdate update = captureUpdate();
        if (update != null) {
            update(update, tcsStatus, zoneStatus);
        }
    }

    synchronized void update(ZoneUpdate update, @Nullable ThingStatus tcsStatus, @Nullable ZoneStatus zoneStatus) {
        if (!isCurrent(update) || zoneStatus != null && !update.zoneId().equals(zoneStatus.getZoneId())) {
            return;
        }
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
                        || !pending.readbackGraceUntil().isAfter(Instant.now())
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
            } else if ("PermanentOverride".equals(mode) || "TemporaryOverride".equals(mode)) {
                executeZoneCommand(zoneCommand -> {
                    SetPointContext context = getSetPointContext();
                    if (context == null) {
                        logger.debug("Thing {}: cannot change override mode without a known target temperature",
                                getThing().getUID());
                        return false;
                    } else if ("PermanentOverride".equals(mode)) {
                        return setPermanentSetPoint(zoneCommand, context.temperature());
                    } else {
                        return setTemporarySetPoint(zoneCommand, context.temperature());
                    }
                });
            } else {
                logger.debug("Thing {}: unsupported setpoint mode {}", getThing().getUID(), mode);
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
                executeZoneCommand(zoneCommand -> {
                    SetPointContext context = getSetPointContext();
                    if (context != null && "TemporaryOverride".equals(context.mode())) {
                        Instant until = context.until();
                        if (until != null && until.isAfter(Instant.now())) {
                            return setTemporarySetPoint(zoneCommand, temperature, until);
                        } else {
                            // An expired or unparseable temporary override must never become permanent implicitly.
                            return setTemporarySetPoint(zoneCommand, temperature);
                        }
                    } else {
                        return setPermanentSetPoint(zoneCommand, temperature);
                    }
                });
            }
        }
    }

    private synchronized @Nullable ZoneCommand captureZoneCommand() {
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        return bridge == null || disposed ? null
                : new ZoneCommand(bridge, getId(), commandGeneration,
                        getEvohomeThingConfig().temporaryOverrideDuration);
    }

    private synchronized boolean isCurrent(ZoneCommand command) {
        return !disposed && command.generation() == commandGeneration && command.bridge().equals(getEvohomeBridge())
                && command.zoneId().equals(getId());
    }

    private boolean executeZoneCommand(Function<ZoneCommand, Boolean> operation) {
        ZoneCommand command = captureZoneCommand();
        if (command == null) {
            return false;
        }
        AtomicBoolean accepted = new AtomicBoolean();
        command.bridge().executeZoneCommand(() -> {
            if (isCurrent(command)) {
                accepted.set(operation.apply(command));
            }
        });
        return accepted.get();
    }

    public boolean setPermanentSetPoint(double temperature) {
        return executeZoneCommand(command -> setPermanentSetPoint(command, temperature));
    }

    private boolean setPermanentSetPoint(ZoneCommand command, double temperature) {
        return command.bridge().setPermanentSetPoint(command.zoneId(), temperature,
                accepted -> rememberCommand(command, accepted, "PermanentOverride"));
    }

    public boolean setTemporarySetPoint(double temperature, Instant until) {
        return executeZoneCommand(command -> setTemporarySetPoint(command, temperature, until));
    }

    private boolean setTemporarySetPoint(ZoneCommand command, double temperature, Instant until) {
        return command.bridge().setTemporarySetPoint(command.zoneId(), temperature, until,
                accepted -> rememberCommand(command, accepted, "TemporaryOverride"));
    }

    public boolean setTemporarySetPoint(double temperature) {
        return executeZoneCommand(command -> setTemporarySetPoint(command, temperature));
    }

    private boolean setTemporarySetPoint(ZoneCommand command, double temperature) {
        int duration = command.duration();
        if (duration == 0) {
            return setTemporarySetPointUntilNextSwitchpoint(command, temperature);
        }
        return duration > 0 && duration <= 1440
                && setTemporarySetPoint(command, temperature, Instant.now().plusSeconds(duration * 60L));
    }

    public boolean setTemporarySetPointUntilNextSwitchpoint(double temperature) {
        return executeZoneCommand(command -> setTemporarySetPointUntilNextSwitchpoint(command, temperature));
    }

    private boolean setTemporarySetPointUntilNextSwitchpoint(ZoneCommand command, double temperature) {
        return command.bridge().setTemporarySetPointUntilNextSwitchpoint(command.zoneId(), temperature,
                accepted -> rememberCommand(command, accepted, "TemporaryOverride")) != null;
    }

    public boolean cancelSetPointOverride() {
        return executeZoneCommand(command -> command.bridge().cancelSetPointOverride(command.zoneId(),
                () -> rememberCancellation(command)));
    }

    Consumer<String> resetCommandContextCallback() {
        ZoneCommand command = captureZoneCommand();
        return zoneId -> {
            if (command != null && command.zoneId().equals(zoneId)) {
                rememberCancellation(command);
            }
        };
    }

    void resetCommandContext() {
        resetCommandContextCallback().accept(getId());
    }

    private void rememberCancellation(ZoneCommand command) {
        SetPointContext context = getSetPointContext();
        rememberCommand(command, new AcceptedSetPoint(context == null ? 0 : context.temperature(), null),
                "FollowSchedule");
    }

    private synchronized void rememberCommand(ZoneCommand command, AcceptedSetPoint accepted, String mode) {
        if (isCurrent(command)) {
            // Retain accepted intent until fresh readback, allowing time for the cloud to apply it.
            pendingSetPoint.set(new PendingSetPoint(new SetPointContext(accepted.temperature(), mode, accepted.until()),
                    Instant.now().plusSeconds(30)));
        }
    }

    private @Nullable SetPointContext getSetPointContext() {
        PendingSetPoint pending = pendingSetPoint.get();
        return pending != null ? pending.context() : actualSetPoint;
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
