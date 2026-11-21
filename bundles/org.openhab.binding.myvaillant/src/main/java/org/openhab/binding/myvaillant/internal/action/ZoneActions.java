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
import org.openhab.binding.myvaillant.internal.api.MyVaillantApi;
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

    @RuleAction(label = "set quick veto", description = "Overrides the target temperature for some hours.")
    public @ActionOutput(type = "java.lang.Boolean") boolean setQuickVeto(
            @ActionInput(name = "temperature", label = "Temperature", description = "Target temperature in °C") @Nullable Double temperature,
            @ActionInput(name = "durationHours", label = "Duration", description = "Hours, empty for the configured duration") @Nullable Double durationHours) {
        return execute("setQuickVeto", (api, systemId, index, zone, systemHandler) -> {
            if (temperature == null) {
                throw new IllegalArgumentException("The temperature is required");
            }
            double duration = durationHours == null ? systemHandler.getAccountConfiguration().quickVetoDuration
                    : durationHours;
            if ("QUICK_VETO".equals(JsonUtil.getString(zone, "current_special_function"))) {
                api.updateZoneQuickVeto(systemId, index, temperature, duration);
            } else {
                api.startZoneQuickVeto(systemId, index, temperature, duration);
            }
        });
    }

    @RuleAction(label = "cancel quick veto", description = "Ends the quick veto of the zone.")
    public @ActionOutput(type = "java.lang.Boolean") boolean cancelQuickVeto() {
        return execute("cancelQuickVeto",
                (api, systemId, index, zone, systemHandler) -> api.cancelZoneQuickVeto(systemId, index));
    }

    @RuleAction(label = "set manual mode setpoint", description = "Sets the target temperature of the manual mode.")
    public @ActionOutput(type = "java.lang.Boolean") boolean setManualModeSetpoint(
            @ActionInput(name = "temperature", label = "Temperature", description = "Target temperature in °C") @Nullable Double temperature,
            @ActionInput(name = "type", label = "Type", description = "HEATING (default) or COOLING") @Nullable String type) {
        return execute("setManualModeSetpoint", (api, systemId, index, zone, systemHandler) -> {
            if (temperature == null) {
                throw new IllegalArgumentException("The temperature is required");
            }
            api.setZoneManualModeSetpoint(systemId, index, temperature, getType(type).toUpperCase(Locale.ROOT));
        });
    }

    /**
     * @return the time program as JSON in the format of the API ({@code monday} ... {@code sunday} with
     *         {@code startTime} and {@code endTime} in minutes since midnight and {@code setpoint}), or {@code null}
     */
    @RuleAction(label = "get time program", description = "Returns the time program of the zone as JSON.")
    public @ActionOutput(type = "java.lang.String") @Nullable String getTimeProgram(
            @ActionInput(name = "type", label = "Type", description = "heating (default) or cooling") @Nullable String type) {
        ZoneHandler zoneHandler = handler;
        JsonObject zone = zoneHandler == null ? null : zoneHandler.getZone();
        String operatingType = getType(type);
        JsonObject timeProgram = JsonUtil.getObject(JsonUtil.getObject(zone, operatingType),
                "time_program_" + operatingType);
        return timeProgram == null ? null : JsonUtil.toCamelCase(timeProgram).toString();
    }

    @RuleAction(label = "set time program", description = "Sets the time program of the zone.")
    public @ActionOutput(type = "java.lang.Boolean") boolean setTimeProgram(
            @ActionInput(name = "type", label = "Type", description = "heating (default) or cooling") @Nullable String type,
            @ActionInput(name = "timeProgram", label = "Time Program", description = "JSON like returned by getTimeProgram") @Nullable String timeProgram) {
        return execute("setTimeProgram", (api, systemId, index, zone, systemHandler) -> {
            JsonObject program = parseTimeProgram(timeProgram);
            program.addProperty("type", getType(type));
            api.setZoneTimeProgram(systemId, index, program);
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

    interface ZoneRequest {
        void execute(MyVaillantApi api, String systemId, int index, @Nullable JsonObject zone,
                SystemHandler systemHandler) throws MyVaillantException;
    }

    private boolean execute(String action, ZoneRequest request) {
        ZoneHandler zoneHandler = handler;
        SystemHandler systemHandler = zoneHandler == null ? null : zoneHandler.getSystem();
        MyVaillantApi api = systemHandler == null ? null : systemHandler.getApi();
        JsonObject zone = zoneHandler == null ? null : zoneHandler.getZone();
        if (zoneHandler == null || systemHandler == null || api == null || zone == null) {
            logger.debug("{} failed, the zone is not online", action);
            return false;
        }
        try {
            request.execute(api, systemHandler.getSystemId(), zoneHandler.getIndex(), zone, systemHandler);
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
