/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.visualcrossing.internal;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.visualcrossing.internal.VisualCrossingBindingConstants.BINDING_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingApi.UnitGroup;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingApiException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingAuthException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingRateException;
import org.openhab.binding.visualcrossing.internal.api.dto.WeatherResponse;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ThingActionsScope(name = BINDING_ID)
@NonNullByDefault
public class WeatherCrossingActions implements ThingActions {
    private @Nullable VisualCrossingHandler handler;

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        this.handler = (VisualCrossingHandler) thingHandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.timeline.label", description = "@text/action.timeline.description")
    public @Nullable @ActionOutput(label = "Result", type = "org.openhab.binding.visualcrossing.internal.api.dto.WeatherResponse") WeatherResponse timeline(
            @ActionInput(name = "location", label = "@text/action.label.location", description = "@text/action.description.location") @Nullable String location,
            @ActionInput(name = "unitGroup", label = "@text/action.label.unitGroup", description = "@text/action.description.unitGroup") @Nullable UnitGroup unitGroup,
            @ActionInput(name = "lang", label = "@text/action.label.lang", description = "@text/action.description.lang") @Nullable String lang,
            @ActionInput(name = "dateFrom", label = "@text/action.label.dateFrom", description = "@text/action.description.dateFrom") @Nullable String dateFrom,
            @ActionInput(name = "dateTo", label = "@text/action.label.dateTo", description = "@text/action.description.dateTo") @Nullable String dateTo)
            throws VisualCrossingAuthException, VisualCrossingApiException, VisualCrossingRateException {
        var localHandler = handler;
        if (localHandler == null) {
            return null;
        }
        return localHandler.timeline(location, unitGroup, lang, dateFrom, dateTo);
    }

    public static @Nullable WeatherResponse timeline(@Nullable ThingActions actions, @Nullable String location,
            @Nullable UnitGroup unitGroup, @Nullable String lang, @Nullable String dateFrom, @Nullable String dateTo)
            throws VisualCrossingAuthException, VisualCrossingApiException, VisualCrossingRateException {
        return ((VisualCrossingHandler) requireNonNull(actions)).timeline(location, unitGroup, lang, dateFrom, dateTo);
    }

    @RuleAction(label = "@text/action.timeline.label", description = "@text/action.timeline.description")
    public @Nullable @ActionOutput(label = "Result", type = "org.openhab.binding.visualcrossing.internal.api.dto.WeatherResponse") WeatherResponse timeline()
            throws VisualCrossingAuthException, VisualCrossingApiException, VisualCrossingRateException {
        return timeline(null, null, null, null, null);
    }

    public static @Nullable WeatherResponse timeline(@Nullable ThingActions actions)
            throws VisualCrossingAuthException, VisualCrossingApiException, VisualCrossingRateException {
        return ((VisualCrossingHandler) requireNonNull(actions)).timeline(null, null, null, null, null);
    }
}
