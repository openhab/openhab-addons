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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.handler.DhwHandler;
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

import com.google.gson.JsonObject;

/**
 * Actions of the hot water.
 *
 * @author Marius Korte - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DhwActions.class)
@ThingActionsScope(name = "myvaillant")
@NonNullByDefault
public class DhwActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(DhwActions.class);
    private @Nullable DhwHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler instanceof DhwHandler dhwHandler ? dhwHandler : null;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.dhw.get-time-program.label", description = "@text/action.dhw.get-time-program.description")
    public @ActionOutput(label = "@text/action.dhw.get-time-program.output.label", type = "java.lang.String") @Nullable String getTimeProgram() {
        return getProgram("time_program_dhw");
    }

    @RuleAction(label = "@text/action.dhw.set-time-program.label", description = "@text/action.dhw.set-time-program.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean setTimeProgram(
            @ActionInput(name = "timeProgram", label = "@text/action.dhw.set-time-program.input.time-program.label", description = "@text/action.dhw.set-time-program.input.time-program.description") @Nullable String timeProgram) {
        return execute("setTimeProgram", (systemHandler, index) -> systemHandler.setDhwTimeProgram(index,
                ZoneActions.parseTimeProgram(timeProgram)));
    }

    @RuleAction(label = "@text/action.dhw.get-circulation-time-program.label", description = "@text/action.dhw.get-circulation-time-program.description")
    public @ActionOutput(label = "@text/action.dhw.get-circulation-time-program.output.label", type = "java.lang.String") @Nullable String getCirculationTimeProgram() {
        return getProgram("time_program_circulation_pump");
    }

    @RuleAction(label = "@text/action.dhw.set-circulation-time-program.label", description = "@text/action.dhw.set-circulation-time-program.description")
    public @ActionOutput(label = "@text/action.output.success.label", type = "java.lang.Boolean") boolean setCirculationTimeProgram(
            @ActionInput(name = "timeProgram", label = "@text/action.dhw.set-circulation-time-program.input.time-program.label", description = "@text/action.dhw.set-circulation-time-program.input.time-program.description") @Nullable String timeProgram) {
        return execute("setCirculationTimeProgram", (systemHandler, index) -> systemHandler
                .setDhwCirculationTimeProgram(index, ZoneActions.parseTimeProgram(timeProgram)));
    }

    private @Nullable String getProgram(String key) {
        DhwHandler dhwHandler = handler;
        JsonObject timeProgram = JsonUtil.getObject(dhwHandler == null ? null : dhwHandler.getDomesticHotWater(), key);
        return timeProgram == null ? null : JsonUtil.toCamelCase(timeProgram).toString();
    }

    // Static methods with the actions as first argument, they make the actions available to Rules DSL

    public static @Nullable String getTimeProgram(ThingActions actions) {
        return ((DhwActions) actions).getTimeProgram();
    }

    public static boolean setTimeProgram(ThingActions actions, @Nullable String timeProgram) {
        return ((DhwActions) actions).setTimeProgram(timeProgram);
    }

    public static @Nullable String getCirculationTimeProgram(ThingActions actions) {
        return ((DhwActions) actions).getCirculationTimeProgram();
    }

    public static boolean setCirculationTimeProgram(ThingActions actions, @Nullable String timeProgram) {
        return ((DhwActions) actions).setCirculationTimeProgram(timeProgram);
    }

    interface DhwRequest {
        void execute(SystemHandler systemHandler, int index) throws MyVaillantException, InterruptedException;
    }

    private boolean execute(String action, DhwRequest request) {
        DhwHandler dhwHandler = handler;
        SystemHandler systemHandler = dhwHandler == null ? null : dhwHandler.getSystem();
        if (dhwHandler == null || systemHandler == null || dhwHandler.getDomesticHotWater() == null) {
            logger.debug("{} failed, the hot water is not online", action);
            return false;
        }
        try {
            request.execute(systemHandler, dhwHandler.getIndex());
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
