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
import org.openhab.binding.myvaillant.internal.api.MyVaillantApi;
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
    @RuleAction(label = "export energy data", description = "Returns the energy values of the devices as JSON.")
    public @ActionOutput(label = "Energy Data", type = "java.lang.String") @Nullable String exportEnergyData(
            @ActionInput(name = "resolution", label = "Resolution", description = "HOUR, DAY or MONTH") @Nullable String resolution,
            @ActionInput(name = "start", label = "Start", description = "e.g. 2026-11-01 or 2026-11-01 00:00:00, in the time zone of the system") @Nullable String start,
            @ActionInput(name = "end", label = "End", description = "e.g. 2026-11-20 23:59:59") @Nullable String end,
            @ActionInput(name = "operationMode", label = "Operation Mode", description = "Only HEATING, DOMESTIC_HOT_WATER or COOLING, empty for all") @Nullable String operationMode) {
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
        }
    }

    /**
     * Sets away mode (holidays) for the whole system.
     *
     * @return whether the API accepted the request
     */
    @RuleAction(label = "set holiday", description = "Sets away mode for the system.")
    public @ActionOutput(type = "java.lang.Boolean") boolean setHoliday(
            @ActionInput(name = "start", label = "Start", description = "Start, empty for now") @Nullable String start,
            @ActionInput(name = "end", label = "End", description = "End, empty for the configured holiday duration") @Nullable String end) {
        return execute("setHoliday", (api, systemHandler, data) -> {
            ZoneId zone = data.getTimeZone();
            Instant startInstant = start == null || start.isBlank() ? Instant.now()
                    : DateTimeParser.parse(start, zone).toInstant();
            Instant endInstant = end == null || end.isBlank()
                    ? startInstant.plus(Duration.ofDays(systemHandler.getAccountConfiguration().holidayDuration))
                    : DateTimeParser.parse(end, zone).toInstant();
            api.setHoliday(data.getSystemId(), startInstant, endInstant);
        });
    }

    @RuleAction(label = "cancel holiday", description = "Ends away mode of the system.")
    public @ActionOutput(type = "java.lang.Boolean") boolean cancelHoliday() {
        return execute("cancelHoliday", (api, systemHandler, data) -> api.cancelHoliday(data.getSystemId()));
    }

    @RuleAction(label = "set cooling for days", description = "Cools manually between start and end.")
    public @ActionOutput(type = "java.lang.Boolean") boolean setCoolingForDays(
            @ActionInput(name = "start", label = "Start", description = "Start, empty for now") @Nullable String start,
            @ActionInput(name = "end", label = "End", description = "End") @Nullable String end) {
        return execute("setCoolingForDays", (api, systemHandler, data) -> {
            if (end == null || end.isBlank()) {
                throw new IllegalArgumentException("The end is required");
            }
            ZoneId zone = data.getTimeZone();
            Instant startInstant = start == null || start.isBlank() ? Instant.now()
                    : DateTimeParser.parse(start, zone).toInstant();
            api.setCoolingForDays(data.getSystemId(), startInstant, DateTimeParser.parse(end, zone).toInstant());
        });
    }

    @RuleAction(label = "cancel cooling for days", description = "Ends manual cooling.")
    public @ActionOutput(type = "java.lang.Boolean") boolean cancelCoolingForDays() {
        return execute("cancelCoolingForDays",
                (api, systemHandler, data) -> api.cancelCoolingForDays(data.getSystemId()));
    }

    @RuleAction(label = "refresh", description = "Updates the system (after the configured refresh delay).")
    public void refresh() {
        SystemHandler systemHandler = handler;
        if (systemHandler != null) {
            systemHandler.scheduleRefresh();
        }
    }

    interface SystemRequest {
        void execute(MyVaillantApi api, SystemHandler systemHandler, SystemData data) throws MyVaillantException;
    }

    private boolean execute(String action, SystemRequest request) {
        SystemHandler systemHandler = handler;
        MyVaillantApi api = systemHandler == null ? null : systemHandler.getApi();
        SystemData data = systemHandler == null ? null : systemHandler.getData();
        if (systemHandler == null || api == null || data == null) {
            logger.debug("{} failed, the system is not online", action);
            return false;
        }
        try {
            request.execute(api, systemHandler, data);
            systemHandler.scheduleRefresh();
            return true;
        } catch (MyVaillantException e) {
            systemHandler.handleRequestFailure(action, e);
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("{} failed: {}", action, e.getMessage());
            return false;
        }
    }
}
