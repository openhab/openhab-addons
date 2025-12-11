/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openhab.core.types.State;

/**
 * The {@link EvccPlanHandler} is responsible for fetching the data from the API response for Plan things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccPlanHandler extends EvccBaseThingHandler {

    private final int index;
    private final String vehicleID;
    private final Map<ChannelUID, Command> pendingCommands = new ConcurrentHashMap<>();
    private JsonObject cachedState = new JsonObject();
    public Map<Integer, String> localizedDayOfWeekMap = Map.of();


    public EvccPlanHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        index = Integer.parseInt(getPropertyOrConfigValue(PROPERTY_INDEX));
        vehicleID = getPropertyOrConfigValue(PROPERTY_VEHICLE_ID);
        type = PROPERTY_TYPE_PLAN;
    }

    private Map<Integer, String> buildLocalizedDayOfWeekMap(EvccBridgeHandler bridgeHandler) {
        LocaleProvider localeProvider = bridgeHandler.getLocaleProvider();
        Locale locale = localeProvider.getLocale();
        return IntStream.rangeClosed(0, 6).boxed().collect(Collectors.toUnmodifiableMap(d -> d,
                d -> (d == 0 ? DayOfWeek.SUNDAY : DayOfWeek.of(d)).getDisplayName(TextStyle.FULL, locale)));
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            localizedDayOfWeekMap = buildLocalizedDayOfWeekMap(handler);
            handler.register(this);
            updateStatus(ThingStatus.ONLINE);
            isInitialized = true;
            JsonObject stateOpt = handler.getCachedEvccState().deepCopy();
            if (stateOpt.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }
            prepareApiResponseForChannelStateUpdate(stateOpt);
        });
    }

    @Override
    public void prepareApiResponseForChannelStateUpdate(JsonObject state) {
        if (state.has(JSON_KEY_VEHICLES)) {
            state = state.getAsJsonObject(JSON_KEY_VEHICLES).getAsJsonObject(vehicleID);
            if (!isInitialized || state.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }
            updateStatus(ThingStatus.ONLINE);
            if (index == 0 && state.has(JSON_KEY_PLAN)) {
                state = state.getAsJsonObject(JSON_KEY_PLAN);
            } else if (index > 0 && state.has(JSON_KEY_REPEATING_PLANS)) {
                // Get the corresponding repeating plan
                state = state.getAsJsonArray(JSON_KEY_REPEATING_PLANS).get(index - 1).getAsJsonObject();
                if (state.has("time")) {
                    state.add("repeatingTime", state.get("time"));
                    state.remove("time");
                }
                if (state.has(JSON_KEY_WEEKDAYS)) {
                    StringBuilder weekDays = new StringBuilder();
                    for (JsonElement dayElement : state.getAsJsonArray(JSON_KEY_WEEKDAYS)) {
                        String day = localizedDayOfWeekMap.get(dayElement.getAsInt());
                        if (null != day && !day.isEmpty()) {
                            weekDays.append(day).append(";");
                        }
                    }
                    // Delete last semicolon
                    if (!weekDays.isEmpty()) {
                        weekDays.setLength(weekDays.length() - 1);
                    } else {
                        state.remove(JSON_KEY_WEEKDAYS);
                    }
                    // Update the weekdays property to a localized string
                    state.addProperty(JSON_KEY_WEEKDAYS, weekDays.toString());
                }
            }
            cachedState = state;
            updateStatesFromApiResponse(state);
        }
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return new JsonObject();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ("update-plan-trigger".equals(channelUID.getId())) {
            updatePlan();
        } else {
            if (command instanceof State) {
                pendingCommands.put(channelUID, command); // store for later processing
            } else {
                super.handleCommand(channelUID, command);
            }
        }
    }

    // endpoint repeating plan: vehicles/db:69/plan/repeating => all plans need to be sent :(, use cachedState :)
    private void updatePlan() {
        JsonObject payload = new JsonObject();
        pendingCommands.clear();
    }
}
