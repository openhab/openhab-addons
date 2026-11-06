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
package org.openhab.automation.optimalwindow.internal.type;

import static org.openhab.automation.optimalwindow.internal.OptimalWindowConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ParameterOption;

/**
 * Configuration parameters shared by the trigger and the condition.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class WindowConfigDescriptions {
    static final String ITEM = "item";

    private WindowConfigDescriptions() {
    }

    public static List<ConfigDescriptionParameter> window() {
        List<ConfigDescriptionParameter> parameters = new ArrayList<>();
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_FORECAST_ITEM, Type.TEXT) //
                .withRequired(true) //
                .withContext(ITEM) //
                .withLabel("Forecast Item") //
                .withDescription("Number item with forecast values, e.g. electricity prices. "
                        + "The future values must be persisted with the forecast strategy.") //
                .build());
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_RANGE_START, Type.TEXT) //
                .withContext("time") //
                .withDefault("00:00") //
                .withLabel("Range Start") //
                .withDescription("Start time of the range to search the window in, e.g. 22:00.") //
                .build());
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_RANGE_DURATION, Type.TEXT) //
                .withDefault("24h") //
                .withLabel("Range Duration") //
                .withDescription("Duration of the range to search the window in, e.g. 8h or 10h30m, at most 48h.") //
                .build());
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_LENGTH, Type.TEXT) //
                .withRequired(true) //
                .withLabel("Length") //
                .withDescription("Length of the window, e.g. 3h, 45m or 1h30m.") //
                .build());
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_CONSECUTIVE, Type.BOOLEAN) //
                .withDefault("true") //
                .withLabel("Consecutive") //
                .withDescription("Find one consecutive window. If disabled, the best intervals with a total "
                        + "duration of the given length are selected.") //
                .build());
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_GOAL, Type.TEXT) //
                .withDefault(GOAL_MINIMUM) //
                .withOptions(List.of(new ParameterOption(GOAL_MINIMUM, "Lowest values, e.g. cheapest prices"),
                        new ParameterOption(GOAL_MAXIMUM, "Highest values, e.g. most PV power"))) //
                .withLimitToOptions(true) //
                .withLabel("Goal") //
                .withDescription("Search for the lowest or the highest values.") //
                .build());
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_PREFER_START, Type.BOOLEAN) //
                .withDefault("false") //
                .withAdvanced(true) //
                .withLabel("Prefer Start") //
                .withDescription("Weight the start of a consecutive window higher, decreasing linearly towards the "
                        + "end. Useful for devices that often finish early, like a boiler.") //
                .build());
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_PERSISTENCE_SERVICE, Type.TEXT) //
                .withAdvanced(true) //
                .withLabel("Persistence Service") //
                .withDescription("Persistence service to read the forecast from. Uses the default service if empty.") //
                .build());
        return parameters;
    }

    /**
     * The optional status items. They only show the window and are never commanded.
     *
     * @param advanced true to show them as advanced parameters, e.g. in the rule template
     */
    public static List<ConfigDescriptionParameter> statusItems(boolean advanced) {
        List<ConfigDescriptionParameter> parameters = new ArrayList<>();
        parameters.add(statusItem(CONFIG_ACTIVE_ITEM, "Window Active Status",
                "Switch item, ON while the window is active. It is updated, not commanded, "
                        + "so it does not switch a device. Also receives the planned window as time series.",
                advanced));
        parameters.add(statusItem(CONFIG_START_ITEM, "Status Item: Start",
                "Status only: DateTime item for the start of the window.", advanced));
        parameters.add(statusItem(CONFIG_END_ITEM, "Status Item: End",
                "Status only: DateTime item for the end of the window.", advanced));
        parameters.add(statusItem(CONFIG_COUNTDOWN_ITEM, "Status Item: Countdown",
                "Status only: Number:Time item for the time until the next start of the window.", advanced));
        parameters.add(statusItem(CONFIG_REMAINING_ITEM, "Status Item: Remaining",
                "Status only: Number:Time item for the time until the end of the active part of the window.",
                advanced));
        parameters.add(statusItem(CONFIG_WINDOW_TEXT_ITEM, "Status Item: Window Text",
                "Status only: String item for the window as text, e.g. 10:45\u201314:45.", advanced));
        parameters.add(statusItem(CONFIG_AVERAGE_ITEM, "Status Item: Average",
                "Status only: Number item for the average forecast value within the window, e.g. the average price.",
                advanced));
        return parameters;
    }

    private static ConfigDescriptionParameter statusItem(String name, String label, String description,
            boolean advanced) {
        return ConfigDescriptionParameterBuilder.create(name, Type.TEXT) //
                .withContext(ITEM) //
                .withAdvanced(advanced) //
                .withLabel(label) //
                .withDescription(description) //
                .build();
    }
}
