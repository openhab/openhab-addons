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
package org.openhab.binding.melcloud.internal.home.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * {@link ThingActions} exposing cloud schedule management for an {@code atw-unit}, per ADR-011 (schedule management
 * is exposed via {@code ThingActions}, not Channels/Items) and ADR-012 (field/parameter shapes).
 *
 * <p>
 * <b>Provisional (ADR-012).</b> The endpoint paths, day/mode integer encodings, and even whether the underlying
 * {@code POST} endpoint genuinely serves both create and update are not independently confirmed against real ATW
 * traffic — see {@code docs/changes/add-melcloud-home-schedule-management/proposal.md}'s Open Questions. Every
 * method here delegates to {@link MelCloudHomeAtwUnitHandler}, which does the actual (also provisional) request
 * building.
 *
 * @author Bernd Weymann - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MelCloudHomeAtwScheduleActions.class)
@ThingActionsScope(name = "melcloud")
@NonNullByDefault
public class MelCloudHomeAtwScheduleActions implements ThingActions {

    private @Nullable MelCloudHomeAtwUnitHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MelCloudHomeAtwUnitHandler atwHandler) {
            this.handler = atwHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "List Schedules", description = "Lists this unit's cloud schedule entries")
    public List<Map<String, Object>> listSchedules() {
        MelCloudHomeAtwUnitHandler h = handler;
        return h == null ? List.of() : h.listSchedules();
    }

    /**
     * Rules DSL static delegate for {@link #listSchedules()}.
     */
    public static List<Map<String, Object>> listSchedules(ThingActions actions) {
        return asScheduleActions(actions).listSchedules();
    }

    @RuleAction(label = "Create Schedule", description = "Creates a new cloud schedule entry; returns its generated id, or \"\" if rejected/failed")
    public String createSchedule(@ActionInput(name = "days", label = "Days") String days,
            @ActionInput(name = "time", label = "Time") String time,
            @ActionInput(name = "power", label = "Power") @Nullable Boolean power,
            @ActionInput(name = "operationModeZone1", label = "Operation Mode Zone 1") @Nullable String operationModeZone1,
            @ActionInput(name = "setTemperatureZone1", label = "Set Temperature Zone 1") @Nullable Double setTemperatureZone1,
            @ActionInput(name = "setTemperatureZone2", label = "Set Temperature Zone 2") @Nullable Double setTemperatureZone2,
            @ActionInput(name = "setTankWaterTemperature", label = "Set Tank Water Temperature") @Nullable Double setTankWaterTemperature,
            @ActionInput(name = "forcedHotWaterMode", label = "Forced Hot Water Mode") @Nullable Boolean forcedHotWaterMode) {
        MelCloudHomeAtwUnitHandler h = handler;
        if (h == null) {
            return "";
        }
        return h.createSchedule(days, time, power, operationModeZone1, setTemperatureZone1, setTemperatureZone2,
                setTankWaterTemperature, forcedHotWaterMode);
    }

    /**
     * Rules DSL static delegate for {@link #createSchedule}.
     */
    public static String createSchedule(ThingActions actions, String days, String time, @Nullable Boolean power,
            @Nullable String operationModeZone1, @Nullable Double setTemperatureZone1,
            @Nullable Double setTemperatureZone2, @Nullable Double setTankWaterTemperature,
            @Nullable Boolean forcedHotWaterMode) {
        return asScheduleActions(actions).createSchedule(days, time, power, operationModeZone1, setTemperatureZone1,
                setTemperatureZone2, setTankWaterTemperature, forcedHotWaterMode);
    }

    @RuleAction(label = "Update Schedule", description = "Updates an existing cloud schedule entry by id; null fields are meant to be left unchanged")
    public boolean updateSchedule(@ActionInput(name = "id", label = "Id") String id,
            @ActionInput(name = "days", label = "Days") @Nullable String days,
            @ActionInput(name = "time", label = "Time") @Nullable String time,
            @ActionInput(name = "power", label = "Power") @Nullable Boolean power,
            @ActionInput(name = "operationModeZone1", label = "Operation Mode Zone 1") @Nullable String operationModeZone1,
            @ActionInput(name = "setTemperatureZone1", label = "Set Temperature Zone 1") @Nullable Double setTemperatureZone1,
            @ActionInput(name = "setTemperatureZone2", label = "Set Temperature Zone 2") @Nullable Double setTemperatureZone2,
            @ActionInput(name = "setTankWaterTemperature", label = "Set Tank Water Temperature") @Nullable Double setTankWaterTemperature,
            @ActionInput(name = "forcedHotWaterMode", label = "Forced Hot Water Mode") @Nullable Boolean forcedHotWaterMode) {
        MelCloudHomeAtwUnitHandler h = handler;
        if (h == null) {
            return false;
        }
        return h.updateSchedule(id, days, time, power, operationModeZone1, setTemperatureZone1, setTemperatureZone2,
                setTankWaterTemperature, forcedHotWaterMode);
    }

    /**
     * Rules DSL static delegate for {@link #updateSchedule}.
     */
    public static boolean updateSchedule(ThingActions actions, String id, @Nullable String days, @Nullable String time,
            @Nullable Boolean power, @Nullable String operationModeZone1, @Nullable Double setTemperatureZone1,
            @Nullable Double setTemperatureZone2, @Nullable Double setTankWaterTemperature,
            @Nullable Boolean forcedHotWaterMode) {
        return asScheduleActions(actions).updateSchedule(id, days, time, power, operationModeZone1, setTemperatureZone1,
                setTemperatureZone2, setTankWaterTemperature, forcedHotWaterMode);
    }

    @RuleAction(label = "Delete Schedule", description = "Deletes a cloud schedule entry by id")
    public boolean deleteSchedule(@ActionInput(name = "id", label = "Id") String id) {
        MelCloudHomeAtwUnitHandler h = handler;
        return h != null && h.deleteSchedule(id);
    }

    /**
     * Rules DSL static delegate for {@link #deleteSchedule}.
     */
    public static boolean deleteSchedule(ThingActions actions, String id) {
        return asScheduleActions(actions).deleteSchedule(id);
    }

    @RuleAction(label = "Set Schedules Enabled", description = "Enables or disables all of this unit's cloud schedules at once")
    public boolean setSchedulesEnabled(@ActionInput(name = "enabled", label = "Enabled") boolean enabled) {
        MelCloudHomeAtwUnitHandler h = handler;
        return h != null && h.setSchedulesEnabled(enabled);
    }

    /**
     * Rules DSL static delegate for {@link #setSchedulesEnabled}.
     */
    public static boolean setSchedulesEnabled(ThingActions actions, boolean enabled) {
        return asScheduleActions(actions).setSchedulesEnabled(enabled);
    }

    private static MelCloudHomeAtwScheduleActions asScheduleActions(ThingActions actions) {
        if (actions instanceof MelCloudHomeAtwScheduleActions scheduleActions) {
            return scheduleActions;
        }
        throw new IllegalArgumentException(
                "Actions is not an instance of " + MelCloudHomeAtwScheduleActions.class.getName());
    }
}
