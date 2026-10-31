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
package org.openhab.binding.volvocars.internal.handler;

import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.volvocars.internal.api.ApiUnits;
import org.openhab.binding.volvocars.internal.api.VolvoCarsApi;
import org.openhab.binding.volvocars.internal.api.VolvoCarsException;
import org.openhab.binding.volvocars.internal.api.dto.CommandResult;
import org.openhab.binding.volvocars.internal.api.dto.Location;
import org.openhab.binding.volvocars.internal.api.dto.ValueField;
import org.openhab.binding.volvocars.internal.api.dto.Vehicle;
import org.openhab.binding.volvocars.internal.config.VehicleConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link VehicleHandler} polls the state of a single vehicle and executes remote commands.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private static final Unit<?> KILOMETRE = MetricPrefix.KILO(SIUnits.METRE);
    private static final int REFRESH_AFTER_COMMAND_SECONDS = 15;
    private static final int COALESCED_REFRESH_DELAY_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);
    private final Object pollLock = new Object();
    private final Object refreshJobLock = new Object();
    private final Object sessionLock = new Object();

    private @Nullable ScheduledFuture<?> pollJob;
    private @Nullable ScheduledFuture<?> refreshJob;

    /**
     * Identifies the current initialization so that tasks scheduled before a dispose do not publish stale state or
     * send commands for a previous configuration. Cleared by {@link #dispose()} under {@link #sessionLock}.
     */
    private volatile @Nullable Session session;

    /**
     * Configuration and vehicle information of one initialization. Kept per session so that a poll or command still
     * running from a previous initialization cannot leak its state into the current one, or use the current
     * configuration. The vehicle information is only accessed from poll().
     */
    private static class Session {
        private final VehicleConfiguration config;
        private final List<Future<?>> commandJobs = new ArrayList<>();
        private @Nullable Vehicle vehicle;
        /** null if unknown, e.g. because the energy scope was not granted; empty if the Energy API is unsupported */
        private @Nullable Set<String> energyCapabilities;
        /** null if unknown, e.g. because the scope was not granted */
        private @Nullable Set<String> commands;
        private boolean locationSupported = true;
        private Instant nextSlowRefresh = Instant.MIN;

        private Session(VehicleConfiguration config) {
            this.config = config;
        }

        private void addCommandJob(Future<?> job) {
            synchronized (commandJobs) {
                commandJobs.removeIf(Future::isDone);
                commandJobs.add(job);
            }
        }

        private void cancelCommandJobs() {
            synchronized (commandJobs) {
                // a command that is already being sent is not interrupted, its result is just no longer published
                commandJobs.forEach(job -> job.cancel(false));
                commandJobs.clear();
            }
        }
    }

    public VehicleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        VehicleConfiguration config = getConfigAs(VehicleConfiguration.class);
        if (config.vin.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.no-vin");
            return;
        }
        if (config.refreshInterval < 1 || config.slowRefreshInterval < config.refreshInterval) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.invalid-refresh-interval");
            return;
        }
        Session currentSession = new Session(config);
        session = currentSession;
        updateStatus(ThingStatus.UNKNOWN);
        pollJob = scheduler.scheduleWithFixedDelay(() -> poll(currentSession), 0, config.refreshInterval,
                TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        Session oldSession;
        synchronized (sessionLock) {
            oldSession = session;
            session = null;
        }
        if (oldSession != null) {
            oldSession.cancelCommandJobs();
        }
        ScheduledFuture<?> job = pollJob;
        if (job != null) {
            job.cancel(true);
            pollJob = null;
        }
        cancelRefreshJob();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                scheduleRefresh(COALESCED_REFRESH_DELAY_SECONDS);
            }
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // the API returns the values of a whole group at once, so coalesce the refresh requests of all channels
            scheduleRefresh(COALESCED_REFRESH_DELAY_SECONDS);
            return;
        }
        String group = channelUID.getGroupId();
        String channel = channelUID.getIdWithoutGroup();
        if (GROUP_DOORS.equals(group) && CHANNEL_LOCK.equals(channel) && command instanceof OnOffType onOff) {
            executeCommand(onOff == OnOffType.ON ? "lock" : "unlock", null, null);
        } else if (GROUP_CONTROL.equals(group)) {
            switch (channel) {
                case CHANNEL_CLIMATIZATION -> {
                    if (command == OnOffType.ON) {
                        executeCommand("climatization-start", null, CHANNEL_CLIMATIZATION);
                    } else if (command == OnOffType.OFF) {
                        executeCommand("climatization-stop", null, null);
                    }
                }
                case CHANNEL_ENGINE_REMOTE_START -> {
                    Session currentSession = session;
                    if (command == OnOffType.ON && currentSession != null) {
                        JsonObject body = new JsonObject();
                        body.addProperty("runtimeMinutes", currentSession.config.engineRunTime);
                        executeCommand("engine-start", body, CHANNEL_ENGINE_REMOTE_START);
                    } else if (command == OnOffType.OFF) {
                        executeCommand("engine-stop", null, null);
                    }
                }
                case CHANNEL_HONK_FLASH -> {
                    String apiCommand = HONK_FLASH_COMMANDS.get(command.toString());
                    if (apiCommand != null) {
                        executeCommand(apiCommand, null, null);
                    } else {
                        logger.debug("Unsupported command {} for channel {}", command, channelUID);
                    }
                }
                case CHANNEL_LOCK_REDUCED_GUARD -> {
                    if (command == OnOffType.ON) {
                        executeCommand("lock-reduced-guard", null, CHANNEL_LOCK_REDUCED_GUARD);
                    }
                }
                default -> logger.debug("Channel {} does not accept commands", channelUID);
            }
        } else {
            logger.debug("Channel {} does not accept command {}", channelUID, command);
        }
    }

    /**
     * @param resetChannel channel of the control group that acts as a push button and is set back to OFF once the
     *            command was sent successfully, or null
     */
    private void executeCommand(String apiCommand, @Nullable JsonObject body, @Nullable String resetChannel) {
        Session currentSession = session;
        if (currentSession == null) {
            return;
        }
        currentSession.addCommandJob(scheduler.submit(() -> {
            VolvoCarsApi api = getApi();
            if (api == null) {
                logger.debug("Unable to execute {}, account is not available", apiCommand);
                return;
            }
            if (!currentSession.equals(session)) {
                logger.debug("Discarding command {}, the Thing was disposed or reconfigured", apiCommand);
                return;
            }
            String status;
            boolean sent;
            try {
                CommandResult result = api.executeCommand(currentSession.config.vin, apiCommand, body);
                String message = result.message;
                status = String.valueOf(result.invokeStatus)
                        + (message != null && !message.isBlank() ? ": " + message : "");
                sent = true;
                logger.debug("Command {} returned {}", apiCommand, status);
            } catch (VolvoCarsException e) {
                logger.debug("Command {} failed: {}", apiCommand, e.getMessage());
                status = "FAILED: " + e.getMessage();
                sent = false;
            }
            if (currentSession.equals(session)) {
                updateState(currentSession, GROUP_CONTROL, CHANNEL_LAST_COMMAND_STATUS, new StringType(status));
                if (sent && resetChannel != null) {
                    updateState(currentSession, GROUP_CONTROL, resetChannel, OnOffType.OFF);
                }
                // the vehicle needs some time until the command is reflected in its reported state
                scheduleRefresh(REFRESH_AFTER_COMMAND_SECONDS);
            }
        }));
    }

    private void scheduleRefresh(int delaySeconds) {
        synchronized (refreshJobLock) {
            Session currentSession = session;
            ScheduledFuture<?> job = refreshJob;
            if (currentSession != null && (job == null || job.isDone())) {
                refreshJob = scheduler.schedule(() -> poll(currentSession), delaySeconds, TimeUnit.SECONDS);
            }
        }
    }

    private void cancelRefreshJob() {
        synchronized (refreshJobLock) {
            ScheduledFuture<?> job = refreshJob;
            if (job != null) {
                job.cancel(true);
                refreshJob = null;
            }
        }
    }

    private @Nullable AccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        ThingHandler handler = bridge != null ? bridge.getHandler() : null;
        return handler instanceof AccountHandler accountHandler ? accountHandler : null;
    }

    private @Nullable VolvoCarsApi getApi() {
        AccountHandler accountHandler = getAccountHandler();
        return accountHandler != null ? accountHandler.getApi() : null;
    }

    /**
     * Polls are serialized as they share the per-initialization vehicle information.
     */
    private void poll(Session pollSession) {
        synchronized (pollLock) {
            pollLocked(pollSession);
        }
    }

    private void pollLocked(Session pollSession) {
        if (!pollSession.equals(session)) {
            return;
        }
        AccountHandler accountHandler = getAccountHandler();
        VolvoCarsApi api = accountHandler != null ? accountHandler.getApi() : null;
        Bridge bridge = getBridge();
        if (accountHandler == null || api == null || bridge == null || bridge.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        try {
            Vehicle localVehicle = pollSession.vehicle;
            if (localVehicle == null) {
                localVehicle = loadVehicle(api, pollSession);
                if (localVehicle == null) {
                    return;
                }
            } else if (localVehicle.hasBattery() && pollSession.energyCapabilities == null) {
                // retry, e.g. because the account was authorized again with the energy scope in the meantime
                loadEnergyCapabilities(api, pollSession, localVehicle);
            }
            refreshFast(api, pollSession, localVehicle);
            if (!Instant.now().isBefore(pollSession.nextSlowRefresh)) {
                refreshSlow(api, pollSession);
                pollSession.nextSlowRefresh = Instant.now()
                        .plusSeconds(pollSession.config.slowRefreshInterval * 60L - 30);
            }
            if (pollSession.equals(session)) {
                updateStatus(ThingStatus.ONLINE);
                accountHandler.reportSuccess(api);
            }
        } catch (VolvoCarsException e) {
            logger.debug("Refreshing vehicle {} failed: {}", getThing().getUID(), e.getMessage());
            if (!pollSession.equals(session)) {
                return;
            }
            if (e.isAuthorizationRequired()) {
                updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
                accountHandler.reportFailure(api, e);
            } else {
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private @Nullable Vehicle loadVehicle(VolvoCarsApi api, Session pollSession) throws VolvoCarsException {
        String vin = pollSession.config.vin;
        Vehicle localVehicle;
        try {
            localVehicle = api.getVehicle(vin);
        } catch (VolvoCarsException e) {
            if (e.getHttpStatus() == HttpStatus.NOT_FOUND_404 || e.getHttpStatus() == HttpStatus.FORBIDDEN_403) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.unknown-vin");
                return null;
            }
            throw e;
        }
        Set<String> capabilities = localVehicle.hasBattery() ? optional(() -> api.getSupportedEnergyCapabilities(vin))
                : null;
        Set<String> commands = optional(() -> api.getAvailableCommands(vin));
        if (!pollSession.equals(session)) {
            return null;
        }
        updateProperties(localVehicle, vin);
        pollSession.energyCapabilities = capabilities;
        pollSession.commands = commands;
        reconcileChannels(localVehicle, pollSession);
        pollSession.vehicle = localVehicle;
        return localVehicle;
    }

    private void loadEnergyCapabilities(VolvoCarsApi api, Session pollSession, Vehicle localVehicle)
            throws VolvoCarsException {
        Set<String> capabilities;
        try {
            capabilities = optional(() -> api.getSupportedEnergyCapabilities(pollSession.config.vin));
        } catch (VolvoCarsException e) {
            if (e.isAuthorizationRequired()) {
                throw e;
            }
            // the capabilities are optional, so a failed retry must not fail the whole refresh
            logger.debug("Energy capabilities of {} are not available: {}", getThing().getUID(), e.getMessage());
            return;
        }
        if (capabilities != null && pollSession.equals(session)) {
            pollSession.energyCapabilities = capabilities;
            reconcileChannels(localVehicle, pollSession);
        }
    }

    private void updateProperties(Vehicle localVehicle, String vin) {
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_VIN, vin);
        putProperty(properties, PROPERTY_MODEL, localVehicle.getModel());
        putProperty(properties, PROPERTY_MODEL_YEAR,
                localVehicle.modelYear > 0 ? Integer.toString(localVehicle.modelYear) : null);
        putProperty(properties, PROPERTY_FUEL_TYPE, localVehicle.fuelType);
        putProperty(properties, PROPERTY_GEARBOX, localVehicle.gearbox);
        putProperty(properties, PROPERTY_EXTERIOR_COLOUR, localVehicle.externalColour);
        Double capacity = localVehicle.batteryCapacityKWh;
        putProperty(properties, PROPERTY_BATTERY_CAPACITY, capacity != null ? capacity.toString() : null);
        Vehicle.Images images = localVehicle.images;
        putProperty(properties, PROPERTY_EXTERIOR_IMAGE_URL, images != null ? images.exteriorImageUrl : null);
        updateProperties(properties);
    }

    private static void putProperty(Map<String, String> properties, String key, @Nullable String value) {
        if (value != null && !value.isBlank()) {
            properties.put(key, value);
        } else {
            properties.remove(key);
        }
    }

    /**
     * Aligns the channels with the features of the vehicle: removes the channels of features it does not have, e.g.
     * fuel channels of an electric car, and adds the defined channels of features it has that are missing, e.g. after
     * the VIN was changed. The Thing is only updated if its channels change.
     */
    private void reconcileChannels(Vehicle localVehicle, Session pollSession) {
        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            return;
        }
        Thing localThing = getThing();
        List<Channel> unsupported = new ArrayList<>();
        for (Channel channel : localThing.getChannels()) {
            if (!isChannelSupported(channel.getUID(), localVehicle, pollSession.energyCapabilities,
                    pollSession.commands)) {
                unsupported.add(channel);
            }
        }
        List<Channel> missing = new ArrayList<>();
        for (String group : CHANNEL_GROUPS) {
            for (ChannelBuilder builder : callback.createChannelBuilders(
                    new ChannelGroupUID(localThing.getUID(), group), new ChannelGroupTypeUID(BINDING_ID, group))) {
                Channel channel = builder.build();
                if (localThing.getChannel(channel.getUID()) == null && isChannelSupported(channel.getUID(),
                        localVehicle, pollSession.energyCapabilities, pollSession.commands)) {
                    missing.add(channel);
                }
            }
        }
        if (!unsupported.isEmpty() || !missing.isEmpty()) {
            logger.debug("Removing {} and adding {} channels of vehicle {}", unsupported.size(), missing.size(),
                    localThing.getUID());
            ThingBuilder thingBuilder = editThing().withoutChannels(unsupported);
            missing.forEach(thingBuilder::withChannel);
            updateThing(thingBuilder.build());
        }
    }

    /**
     * @param energyCapabilities see {@link #isBatteryChannelSupported(String, Set)}
     * @param commands the commands supported by the vehicle, or null if they are unknown
     */
    static boolean isChannelSupported(ChannelUID uid, Vehicle localVehicle, @Nullable Set<String> energyCapabilities,
            @Nullable Set<String> commands) {
        String group = uid.getGroupId();
        String id = uid.getIdWithoutGroup();
        return switch (group != null ? group : "") {
            case GROUP_FUEL -> localVehicle.hasCombustionEngine();
            case GROUP_BATTERY -> localVehicle.hasBattery() && isBatteryChannelSupported(id, energyCapabilities);
            case GROUP_TRIP ->
                !((CHANNEL_AVERAGE_FUEL_CONSUMPTION.equals(id) || CHANNEL_AVERAGE_FUEL_CONSUMPTION_AUTOMATIC.equals(id))
                        && !localVehicle.hasCombustionEngine())
                        && !(CHANNEL_AVERAGE_ENERGY_CONSUMPTION.equals(id) && !localVehicle.hasBattery());
            case GROUP_CONTROL -> commands == null || switch (id) {
                case CHANNEL_CLIMATIZATION -> commands.contains("CLIMATIZATION_START");
                case CHANNEL_ENGINE_REMOTE_START -> commands.contains("ENGINE_START");
                case CHANNEL_HONK_FLASH ->
                    commands.contains("HONK") || commands.contains("FLASH") || commands.contains("HONK_AND_FLASH");
                case CHANNEL_LOCK_REDUCED_GUARD -> commands.contains("LOCK_REDUCED_GUARD");
                default -> true;
            };
            default -> true;
        };
    }

    /**
     * @param energyCapabilities the energy state values the vehicle supports, empty if the vehicle does not support
     *            the Energy API, or null if they are unknown
     */
    static boolean isBatteryChannelSupported(String channel, @Nullable Set<String> energyCapabilities) {
        String value = ENERGY_CHANNELS.get(channel);
        if (value == null || energyCapabilities == null) {
            return true;
        }
        if (energyCapabilities.isEmpty()) {
            // without the Energy API the battery level is read from the fuel status
            return CHANNEL_BATTERY_LEVEL.equals(channel);
        }
        return energyCapabilities.contains(value);
    }

    private void refreshFast(VolvoCarsApi api, Session pollSession, Vehicle localVehicle) throws VolvoCarsException {
        String vin = pollSession.config.vin;
        Set<String> energyCapabilities = pollSession.energyCapabilities;
        boolean energyApiSupported = energyCapabilities != null && !energyCapabilities.isEmpty();
        Map<String, ValueField> doors = getValues(api, vin, "doors");
        if (doors != null) {
            ValueField centralLock = doors.get("centralLock");
            updateState(pollSession, GROUP_DOORS, CHANNEL_LOCK,
                    centralLock == null ? UnDefType.UNDEF : switch (String.valueOf(centralLock.getString())) {
                        case "LOCKED" -> OnOffType.ON;
                        case "UNLOCKED" -> OnOffType.OFF;
                        default -> UnDefType.UNDEF;
                    });
            DOOR_CHANNELS.forEach(
                    (key, channel) -> updateState(pollSession, GROUP_DOORS, channel, toOpenClosed(doors.get(key))));
        }
        Map<String, ValueField> windows = getValues(api, vin, "windows");
        if (windows != null) {
            WINDOW_CHANNELS.forEach(
                    (key, channel) -> updateState(pollSession, GROUP_WINDOWS, channel, toOpenClosed(windows.get(key))));
        }
        Map<String, ValueField> engine = getValues(api, vin, "engine-status");
        if (engine != null) {
            ValueField engineStatus = engine.get("engineStatus");
            updateState(pollSession, GROUP_STATUS, CHANNEL_ENGINE_RUNNING,
                    engineStatus == null || "UNSPECIFIED".equals(engineStatus.getString()) ? UnDefType.UNDEF
                            : OnOffType.from("RUNNING".equals(engineStatus.getString())));
        }
        Map<String, ValueField> odometer = getValues(api, vin, "odometer");
        if (odometer != null) {
            updateState(pollSession, GROUP_STATUS, CHANNEL_ODOMETER, toQuantity(odometer.get("odometer"), KILOMETRE));
        }
        Map<String, ValueField> accessibility = getValues(api, vin, "command-accessibility");
        if (accessibility != null) {
            ValueField availability = accessibility.get("availabilityStatus");
            String reason = availability != null ? availability.unavailableReason : null;
            updateState(pollSession, GROUP_STATUS, CHANNEL_AVAILABILITY,
                    reason != null && !reason.isBlank() ? new StringType(reason) : toString(availability));
        }
        Map<String, ValueField> statistics = getValues(api, vin, "statistics");
        if (statistics != null) {
            updateStatistics(pollSession, statistics);
        }
        if (localVehicle.hasCombustionEngine() || (localVehicle.hasBattery() && !energyApiSupported)) {
            Map<String, ValueField> fuel = getValues(api, vin, "fuel");
            if (fuel != null) {
                updateState(pollSession, GROUP_FUEL, CHANNEL_FUEL_AMOUNT,
                        toQuantity(fuel.get("fuelAmount"), Units.LITRE));
                if (!energyApiSupported) {
                    // the fuel status also reports the battery level, used if the Energy API is not available
                    updateState(pollSession, GROUP_BATTERY, CHANNEL_BATTERY_LEVEL,
                            toQuantity(fuel.get("batteryChargeLevel"), Units.PERCENT));
                }
            }
        }
        if (energyApiSupported) {
            Map<String, ValueField> energy = getEnergyState(api, vin);
            if (energy != null) {
                updateEnergy(pollSession, energy);
            }
        }
        if (pollSession.locationSupported) {
            pollSession.locationSupported = updateLocation(api, pollSession);
        }
    }

    private void refreshSlow(VolvoCarsApi api, Session pollSession) throws VolvoCarsException {
        String vin = pollSession.config.vin;
        // retry the location in case it was temporarily unavailable
        pollSession.locationSupported = true;

        Map<String, ValueField> diagnostics = getValues(api, vin, "diagnostics");
        if (diagnostics != null) {
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_SERVICE_WARNING,
                    toString(diagnostics.get("serviceWarning")));
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_DISTANCE_TO_SERVICE,
                    toQuantity(diagnostics.get("distanceToService"), KILOMETRE));
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_TIME_TO_SERVICE,
                    toQuantity(diagnostics.get("timeToService"), Units.MONTH));
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_ENGINE_HOURS_TO_SERVICE,
                    toQuantity(diagnostics.get("engineHoursToService"), Units.HOUR));
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_WASHER_FLUID_WARNING,
                    toString(diagnostics.get("washerFluidLevelWarning")));
        }
        Map<String, ValueField> brakes = getValues(api, vin, "brakes");
        if (brakes != null) {
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_BRAKE_FLUID_WARNING,
                    toString(brakes.get("brakeFluidLevelWarning")));
        }
        Map<String, ValueField> engineWarnings = getValues(api, vin, "engine");
        if (engineWarnings != null) {
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_OIL_LEVEL_WARNING,
                    toString(engineWarnings.get("oilLevelWarning")));
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_COOLANT_LEVEL_WARNING,
                    toString(engineWarnings.get("engineCoolantLevelWarning")));
        }
        Map<String, ValueField> tyres = getValues(api, vin, "tyres");
        if (tyres != null) {
            TYRE_CHANNELS.forEach(
                    (key, channel) -> updateState(pollSession, GROUP_MAINTENANCE, channel, toString(tyres.get(key))));
        }
        Map<String, ValueField> warnings = getValues(api, vin, "warnings");
        if (warnings != null) {
            Set<String> failures = new TreeSet<>();
            warnings.forEach((key, field) -> {
                if ("FAILURE".equals(field.getString())) {
                    failures.add(key.endsWith("Warning") ? key.substring(0, key.length() - "Warning".length()) : key);
                }
            });
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_LIGHT_FAILURE, OnOffType.from(!failures.isEmpty()));
            updateState(pollSession, GROUP_MAINTENANCE, CHANNEL_LIGHT_FAILURES,
                    new StringType(String.join(", ", failures)));
        }
    }

    private void updateStatistics(Session pollSession, Map<String, ValueField> statistics) {
        updateState(pollSession, GROUP_TRIP, CHANNEL_AVERAGE_SPEED,
                toQuantity(statistics.get("averageSpeed"), SIUnits.KILOMETRE_PER_HOUR));
        updateState(pollSession, GROUP_TRIP, CHANNEL_AVERAGE_SPEED_AUTOMATIC,
                toQuantity(statistics.get("averageSpeedAutomatic"), SIUnits.KILOMETRE_PER_HOUR));
        updateState(pollSession, GROUP_TRIP, CHANNEL_TRIP_METER_MANUAL,
                toQuantity(statistics.get("tripMeterManual"), KILOMETRE));
        updateState(pollSession, GROUP_TRIP, CHANNEL_TRIP_METER_AUTOMATIC,
                toQuantity(statistics.get("tripMeterAutomatic"), KILOMETRE));
        updateState(pollSession, GROUP_TRIP, CHANNEL_AVERAGE_FUEL_CONSUMPTION,
                toDecimal(statistics.get("averageFuelConsumption")));
        updateState(pollSession, GROUP_TRIP, CHANNEL_AVERAGE_FUEL_CONSUMPTION_AUTOMATIC,
                toDecimal(statistics.get("averageFuelConsumptionAutomatic")));
        updateState(pollSession, GROUP_TRIP, CHANNEL_AVERAGE_ENERGY_CONSUMPTION,
                toDecimal(statistics.get("averageEnergyConsumption")));
        updateState(pollSession, GROUP_FUEL, CHANNEL_DISTANCE_TO_EMPTY_TANK,
                toQuantity(statistics.get("distanceToEmptyTank"), KILOMETRE));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_DISTANCE_TO_EMPTY_BATTERY,
                toQuantity(statistics.get("distanceToEmptyBattery"), KILOMETRE));
    }

    private void updateEnergy(Session pollSession, Map<String, ValueField> energy) {
        updateState(pollSession, GROUP_BATTERY, CHANNEL_BATTERY_LEVEL,
                toQuantity(energy.get("batteryChargeLevel"), Units.PERCENT));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_ELECTRIC_RANGE,
                toQuantity(energy.get("electricRange"), KILOMETRE));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_CHARGER_CONNECTION_STATUS,
                toString(energy.get("chargerConnectionStatus")));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_CHARGING_STATUS, toString(energy.get("chargingStatus")));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_CHARGING_TYPE, toString(energy.get("chargingType")));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_CHARGER_POWER_STATUS,
                toString(energy.get("chargerPowerStatus")));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_ESTIMATED_CHARGING_TIME,
                toQuantity(energy.get("estimatedChargingTimeToTargetBatteryChargeLevel"), Units.MINUTE));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_TARGET_BATTERY_LEVEL,
                toQuantity(energy.get("targetBatteryChargeLevel"), Units.PERCENT));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_CHARGING_CURRENT_LIMIT,
                toQuantity(energy.get("chargingCurrentLimit"), Units.AMPERE));
        updateState(pollSession, GROUP_BATTERY, CHANNEL_CHARGING_POWER,
                toQuantity(energy.get("chargingPower"), Units.WATT));
    }

    /**
     * @return false if the location is not available for the vehicle or authorization
     */
    private boolean updateLocation(VolvoCarsApi api, Session pollSession) throws VolvoCarsException {
        Location location;
        try {
            location = api.getLocation(pollSession.config.vin);
        } catch (VolvoCarsException e) {
            // Volvo answers with 403 if the location scope was not granted or the vehicle's region is not supported
            if (e.getHttpStatus() == HttpStatus.FORBIDDEN_403 || e.getHttpStatus() == HttpStatus.NOT_FOUND_404) {
                logger.debug("Location of {} is not available: {}", getThing().getUID(), e.getMessage());
                return false;
            }
            throw e;
        }
        Location.Geometry geometry = location != null ? location.geometry : null;
        List<Double> coordinates = geometry != null ? geometry.coordinates : null;
        if (coordinates != null && coordinates.size() >= 2) {
            DecimalType latitude = new DecimalType(coordinates.get(1));
            DecimalType longitude = new DecimalType(coordinates.get(0));
            updateState(pollSession, GROUP_LOCATION, CHANNEL_POSITION,
                    coordinates.size() > 2 ? new PointType(latitude, longitude, new DecimalType(coordinates.get(2)))
                            : new PointType(latitude, longitude));
        } else {
            updateState(pollSession, GROUP_LOCATION, CHANNEL_POSITION, UnDefType.UNDEF);
        }
        Location.Properties properties = location != null ? location.properties : null;
        State heading = UnDefType.UNDEF;
        State timestamp = UnDefType.UNDEF;
        if (properties != null) {
            String localHeading = properties.heading;
            if (localHeading != null) {
                try {
                    heading = new QuantityType<>(new BigDecimal(localHeading), Units.DEGREE_ANGLE);
                } catch (NumberFormatException e) {
                    logger.debug("Ignoring invalid heading {}", localHeading);
                }
            }
            String localTimestamp = properties.timestamp;
            if (localTimestamp != null) {
                try {
                    timestamp = new DateTimeType(ZonedDateTime.parse(localTimestamp).toInstant());
                } catch (DateTimeParseException e) {
                    logger.debug("Ignoring invalid timestamp {}", localTimestamp);
                }
            }
        }
        updateState(pollSession, GROUP_LOCATION, CHANNEL_HEADING, heading);
        updateState(pollSession, GROUP_LOCATION, CHANNEL_TIMESTAMP, timestamp);
        return true;
    }

    /**
     * Reads a Connected Vehicle API resource.
     *
     * @return the values, or null if the vehicle does not provide the resource or the scope was not granted
     */
    private @Nullable Map<String, ValueField> getValues(VolvoCarsApi api, String vin, String resource)
            throws VolvoCarsException {
        return optional(() -> api.getValues(vin, resource));
    }

    private @Nullable Map<String, ValueField> getEnergyState(VolvoCarsApi api, String vin) throws VolvoCarsException {
        return optional(() -> api.getEnergyState(vin));
    }

    @FunctionalInterface
    private interface ApiCall<T> {
        T call() throws VolvoCarsException;
    }

    /**
     * Executes an API call whose resource may not be available for every vehicle or authorization: the API
     * answers with 404 for resources the vehicle does not support and with 403 for scopes that were not granted.
     *
     * @return the result of the call, or null if the resource is not available
     */
    private <T> @Nullable T optional(ApiCall<T> call) throws VolvoCarsException {
        try {
            return call.call();
        } catch (VolvoCarsException e) {
            if (e.getHttpStatus() == HttpStatus.NOT_FOUND_404 || e.getHttpStatus() == HttpStatus.FORBIDDEN_403) {
                logger.debug("Resource not available for {}: {}", getThing().getUID(), e.getMessage());
                return null;
            }
            throw e;
        }
    }

    /**
     * Publishes a channel state read for the given session, unless the Thing was disposed or reconfigured since.
     * Runs under the lock {@link #dispose()} takes, so that no state of a previous session is published after it.
     */
    private void updateState(Session stateSession, String group, String channel, State state) {
        synchronized (sessionLock) {
            if (stateSession.equals(session)) {
                updateState(new ChannelUID(getThing().getUID(), group, channel), state);
            }
        }
    }

    private static State toOpenClosed(@Nullable ValueField field) {
        if (field == null) {
            return UnDefType.UNDEF;
        }
        return switch (String.valueOf(field.getString())) {
            case "OPEN", "AJAR" -> OpenClosedType.OPEN;
            case "CLOSED" -> OpenClosedType.CLOSED;
            default -> UnDefType.UNDEF;
        };
    }

    private static State toString(@Nullable ValueField field) {
        String value = field != null && field.isValid() ? field.getString() : null;
        return value != null ? new StringType(value) : UnDefType.UNDEF;
    }

    private static State toDecimal(@Nullable ValueField field) {
        BigDecimal value = field != null && field.isValid() ? field.getNumber() : null;
        return value != null ? new DecimalType(value) : UnDefType.UNDEF;
    }

    /**
     * @param defaultUnit the unit to use if the API reports no or an unknown unit
     */
    static State toQuantity(@Nullable ValueField field, Unit<?> defaultUnit) {
        BigDecimal value = field != null && field.isValid() ? field.getNumber() : null;
        if (field == null || value == null) {
            return UnDefType.UNDEF;
        }
        Unit<?> unit = ApiUnits.parse(field.unit);
        if (unit == null || !unit.isCompatible(defaultUnit)) {
            unit = defaultUnit;
        }
        return new QuantityType<>(value, unit);
    }
}
