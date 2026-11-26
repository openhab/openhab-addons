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
package org.openhab.binding.myvaillant.internal.action;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.api.SystemData;
import org.openhab.binding.myvaillant.internal.handler.SystemHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actions of a system.
 *
 * @author Marius Korte - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SystemActions.class)
@ThingActionsScope(name = "myvaillant")
@NonNullByDefault
public class SystemActions implements ThingActions {

    private static final Set<String> RESOLUTIONS = Set.of("HOUR", "DAY", "MONTH");

    private final Logger logger = LoggerFactory.getLogger(SystemActions.class);
    private @Nullable SystemHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler instanceof SystemHandler systemHandler ? systemHandler : null;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    /**
     * Energy values of the devices. Every device and value type costs one request. Vaillant accepts at most 3 days
     * with HOUR and 60 days with DAY.
     *
     * @return JSON array with one entry per device: {@code device} and {@code data} (one entry per value type with
     *         {@code operation_mode}, {@code energy_type} and the buckets in {@code data}: {@code start_date},
     *         {@code end_date}, {@code value} in Wh), or {@code null} if the request failed
     */
    @RuleAction(label = "@text/action.system.export-energy-data.label", description = "@text/action.system.export-energy-data.description")
    public @ActionOutput(label = "@text/action.system.export-energy-data.output.label", type = "java.lang.String") @Nullable String exportEnergyData(
            @ActionInput(name = "resolution", label = "@text/action.system.export-energy-data.input.resolution.label", description = "@text/action.system.export-energy-data.input.resolution.description") @Nullable String resolution,
            @ActionInput(name = "start", label = "@text/action.system.export-energy-data.input.start.label", description = "@text/action.system.export-energy-data.input.start.description") @Nullable String start,
            @ActionInput(name = "end", label = "@text/action.system.export-energy-data.input.end.label", description = "@text/action.system.export-energy-data.input.end.description") @Nullable String end,
            @ActionInput(name = "operationMode", label = "@text/action.system.export-energy-data.input.operation-mode.label", description = "@text/action.system.export-energy-data.input.operation-mode.description") @Nullable String operationMode) {
        SystemHandler systemHandler = handler;
        SystemData data = systemHandler == null ? null : systemHandler.getData();
        if (systemHandler == null || data == null) {
            logger.debug("exportEnergyData failed, the system is not online");
            return null;
        }
        if (resolution == null || start == null || end == null) {
            logger.warn("exportEnergyData needs resolution, start and end");
            return null;
        }
        String upperResolution = resolution.toUpperCase(Locale.ROOT);
        if (!RESOLUTIONS.contains(upperResolution)) {
            logger.warn("Unknown resolution {}, use one of {}", resolution, RESOLUTIONS);
            return null;
        }
        try {
            ZoneId zone = data.getTimeZone();
            return systemHandler.exportEnergyData(upperResolution, DateTimeParser.parse(start, zone),
                    DateTimeParser.parse(end, zone), operationMode);
        } catch (IllegalArgumentException e) {
            logger.warn("exportEnergyData failed: {}", e.getMessage());
            return null;
        } catch (MyVaillantException e) {
            systemHandler.handleRequestFailure("exportEnergyData", e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Sets away mode (holidays) for the whole system.
     *
     * @return whether the API accepted the request
     */
    @RuleAction(label = "@text/action.system.set-holiday.label", description = "@text/action.system.set-holiday.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean setHoliday(
            @ActionInput(name = "start", label = "@text/action.system.set-holiday.input.start.label", description = "@text/action.system.set-holiday.input.start.description") @Nullable String start,
            @ActionInput(name = "end", label = "@text/action.system.set-holiday.input.end.label", description = "@text/action.system.set-holiday.input.end.description") @Nullable String end) {
        return execute("setHoliday", (systemHandler, data) -> {
            ZoneId zone = data.getTimeZone();
            Instant startInstant = start == null || start.isBlank() ? Instant.now()
                    : DateTimeParser.parse(start, zone).toInstant();
            Instant endInstant = end == null || end.isBlank()
                    ? startInstant.plus(Duration.ofDays(systemHandler.getAccountConfiguration().holidayDurationInDays))
                    : DateTimeParser.parse(end, zone).toInstant();
            systemHandler.setHoliday(startInstant, endInstant);
        });
    }

    @RuleAction(label = "@text/action.system.cancel-holiday.label", description = "@text/action.system.cancel-holiday.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean cancelHoliday() {
        return execute("cancelHoliday", (systemHandler, data) -> systemHandler.cancelHoliday());
    }

    @RuleAction(label = "@text/action.system.set-cooling-for-days.label", description = "@text/action.system.set-cooling-for-days.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean setCoolingForDays(
            @ActionInput(name = "start", label = "@text/action.system.set-cooling-for-days.input.start.label", description = "@text/action.system.set-cooling-for-days.input.start.description") @Nullable String start,
            @ActionInput(name = "end", label = "@text/action.system.set-cooling-for-days.input.end.label", description = "@text/action.system.set-cooling-for-days.input.end.description") @Nullable String end) {
        return execute("setCoolingForDays", (systemHandler, data) -> {
            if (end == null || end.isBlank()) {
                throw new IllegalArgumentException("The end is required");
            }
            ZoneId zone = data.getTimeZone();
            Instant startInstant = start == null || start.isBlank() ? Instant.now()
                    : DateTimeParser.parse(start, zone).toInstant();
            systemHandler.setCoolingForDays(startInstant, DateTimeParser.parse(end, zone).toInstant());
        });
    }

    @RuleAction(label = "@text/action.system.cancel-cooling-for-days.label", description = "@text/action.system.cancel-cooling-for-days.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean cancelCoolingForDays() {
        return execute("cancelCoolingForDays", (systemHandler, data) -> systemHandler.cancelCoolingForDays());
    }

    @RuleAction(label = "@text/action.system.refresh.label", description = "@text/action.system.refresh.description")
    public void refresh() {
        SystemHandler systemHandler = handler;
        if (systemHandler != null) {
            systemHandler.scheduleRefresh();
        }
    }

    // Static methods with the actions as first argument, they make the actions available to Rules DSL

    public static @Nullable String exportEnergyData(ThingActions actions, @Nullable String resolution,
            @Nullable String start, @Nullable String end, @Nullable String operationMode) {
        return ((SystemActions) actions).exportEnergyData(resolution, start, end, operationMode);
    }

    public static boolean setHoliday(ThingActions actions, @Nullable String start, @Nullable String end) {
        return ((SystemActions) actions).setHoliday(start, end);
    }

    public static boolean cancelHoliday(ThingActions actions) {
        return ((SystemActions) actions).cancelHoliday();
    }

    public static boolean setCoolingForDays(ThingActions actions, @Nullable String start, @Nullable String end) {
        return ((SystemActions) actions).setCoolingForDays(start, end);
    }

    public static boolean cancelCoolingForDays(ThingActions actions) {
        return ((SystemActions) actions).cancelCoolingForDays();
    }

    public static void refresh(ThingActions actions) {
        ((SystemActions) actions).refresh();
    }

    interface SystemRequest {
        void execute(SystemHandler systemHandler, SystemData data) throws MyVaillantException, InterruptedException;
    }

    private boolean execute(String action, SystemRequest request) {
        SystemHandler systemHandler = handler;
        SystemData data = systemHandler == null ? null : systemHandler.getData();
        if (systemHandler == null || data == null) {
            logger.debug("{} failed, the system is not online", action);
            return false;
        }
        try {
            request.execute(systemHandler, data);
            systemHandler.scheduleRefresh();
            return true;
        } catch (MyVaillantException e) {
            systemHandler.handleRequestFailure(action, e);
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("{} failed: {}", action, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
