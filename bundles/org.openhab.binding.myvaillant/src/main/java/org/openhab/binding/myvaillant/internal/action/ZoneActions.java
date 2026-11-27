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

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.handler.SystemHandler;
import org.openhab.binding.myvaillant.internal.handler.ZoneHandler;
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Actions of a zone.
 *
 * @author Marius Korte - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ZoneActions.class)
@ThingActionsScope(name = "myvaillant")
@NonNullByDefault
public class ZoneActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(ZoneActions.class);
    private @Nullable ZoneHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler instanceof ZoneHandler zoneHandler ? zoneHandler : null;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.zone.set-quick-veto.label", description = "@text/action.zone.set-quick-veto.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean setQuickVeto(
            @ActionInput(name = "temperature", label = "@text/action.zone.set-quick-veto.input.temperature.label", description = "@text/action.zone.set-quick-veto.input.temperature.description") @Nullable Double temperature,
            @ActionInput(name = "durationHours", label = "@text/action.zone.set-quick-veto.input.duration-hours.label", description = "@text/action.zone.set-quick-veto.input.duration-hours.description") @Nullable Double durationHours) {
        return execute("setQuickVeto", (systemHandler, index, zone) -> {
            if (temperature == null) {
                throw new IllegalArgumentException("The temperature is required");
            }
            double duration = durationHours == null ? systemHandler.getAccountConfiguration().quickVetoDurationInHours
                    : durationHours;
            if ("QUICK_VETO".equals(JsonUtil.getString(zone, "current_special_function"))) {
                systemHandler.updateZoneQuickVeto(index, temperature, duration);
            } else {
                systemHandler.startZoneQuickVeto(index, temperature, duration);
            }
        });
    }

    @RuleAction(label = "@text/action.zone.cancel-quick-veto.label", description = "@text/action.zone.cancel-quick-veto.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean cancelQuickVeto() {
        return execute("cancelQuickVeto", (systemHandler, index, zone) -> systemHandler.cancelZoneQuickVeto(index));
    }

    @RuleAction(label = "@text/action.zone.set-manual-mode-setpoint.label", description = "@text/action.zone.set-manual-mode-setpoint.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean setManualModeSetpoint(
            @ActionInput(name = "temperature", label = "@text/action.zone.set-manual-mode-setpoint.input.temperature.label", description = "@text/action.zone.set-manual-mode-setpoint.input.temperature.description") @Nullable Double temperature,
            @ActionInput(name = "type", label = "@text/action.zone.set-manual-mode-setpoint.input.type.label", description = "@text/action.zone.set-manual-mode-setpoint.input.type.description") @Nullable String type) {
        return execute("setManualModeSetpoint", (systemHandler, index, zone) -> {
            if (temperature == null) {
                throw new IllegalArgumentException("The temperature is required");
            }
            systemHandler.setZoneManualModeSetpoint(index, temperature, getType(type).toUpperCase(Locale.ROOT));
        });
    }

    /**
     * @return the time program as JSON in the format of the API ({@code monday} ... {@code sunday} with
     *         {@code startTime} and {@code endTime} in minutes since midnight and {@code setpoint}), or {@code null}
     */
    @RuleAction(label = "@text/action.zone.get-time-program.label", description = "@text/action.zone.get-time-program.description")
    public @ActionOutput(label = "@text/action.zone.get-time-program.output.label", type = "java.lang.String") @Nullable String getTimeProgram(
            @ActionInput(name = "type", label = "@text/action.zone.get-time-program.input.type.label", description = "@text/action.zone.get-time-program.input.type.description") @Nullable String type) {
        ZoneHandler zoneHandler = handler;
        JsonObject zone = zoneHandler == null ? null : zoneHandler.getZone();
        String operatingType = getType(type);
        JsonObject timeProgram = JsonUtil.getObject(JsonUtil.getObject(zone, operatingType),
                "time_program_" + operatingType);
        return timeProgram == null ? null : JsonUtil.toCamelCase(timeProgram).toString();
    }

    @RuleAction(label = "@text/action.zone.set-time-program.label", description = "@text/action.zone.set-time-program.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean setTimeProgram(
            @ActionInput(name = "type", label = "@text/action.zone.set-time-program.input.type.label", description = "@text/action.zone.set-time-program.input.type.description") @Nullable String type,
            @ActionInput(name = "timeProgram", label = "@text/action.zone.set-time-program.input.time-program.label", description = "@text/action.zone.set-time-program.input.time-program.description") @Nullable String timeProgram) {
        return execute("setTimeProgram", (systemHandler, index, zone) -> {
            JsonObject program = parseTimeProgram(timeProgram);
            program.addProperty("type", getType(type));
            systemHandler.setZoneTimeProgram(index, program);
        });
    }

    static JsonObject parseTimeProgram(@Nullable String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("The time program is required");
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("The time program must be a JSON object");
            }
            JsonObject program = element.getAsJsonObject();
            program.remove("metaInfo");
            return program;
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid time program: " + e.getMessage(), e);
        }
    }

    private static String getType(@Nullable String type) {
        String lower = type == null || type.isBlank() ? "heating" : type.toLowerCase(Locale.ROOT);
        if (!"heating".equals(lower) && !"cooling".equals(lower)) {
            throw new IllegalArgumentException("Unknown type " + type + ", use heating or cooling");
        }
        return lower;
    }

    // Static methods with the actions as first argument, they make the actions available to Rules DSL

    public static boolean setQuickVeto(ThingActions actions, @Nullable Double temperature,
            @Nullable Double durationHours) {
        return ((ZoneActions) actions).setQuickVeto(temperature, durationHours);
    }

    public static boolean cancelQuickVeto(ThingActions actions) {
        return ((ZoneActions) actions).cancelQuickVeto();
    }

    public static boolean setManualModeSetpoint(ThingActions actions, @Nullable Double temperature,
            @Nullable String type) {
        return ((ZoneActions) actions).setManualModeSetpoint(temperature, type);
    }

    public static @Nullable String getTimeProgram(ThingActions actions, @Nullable String type) {
        return ((ZoneActions) actions).getTimeProgram(type);
    }

    public static boolean setTimeProgram(ThingActions actions, @Nullable String type, @Nullable String timeProgram) {
        return ((ZoneActions) actions).setTimeProgram(type, timeProgram);
    }

    interface ZoneRequest {
        void execute(SystemHandler systemHandler, int index, JsonObject zone)
                throws MyVaillantException, InterruptedException;
    }

    private boolean execute(String action, ZoneRequest request) {
        ZoneHandler zoneHandler = handler;
        SystemHandler systemHandler = zoneHandler == null ? null : zoneHandler.getSystem();
        JsonObject zone = zoneHandler == null ? null : zoneHandler.getZone();
        if (zoneHandler == null || systemHandler == null || zone == null) {
            logger.debug("{} failed, the zone is not online", action);
            return false;
        }
        try {
            request.execute(systemHandler, zoneHandler.getIndex(), zone);
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
