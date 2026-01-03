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
package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The {@link EvccPlanHandler} is responsible for fetching the data from the API response for Plan things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccPlanHandler extends EvccBaseThingHandler {

    private final int index;
    private final String vehicleID;
    private final Map<String, String> pendingCommands = new ConcurrentHashMap<>();
    private final JsonArray cachedRepeatingPlans = new JsonArray();
    private JsonObject cachedOneTimePlan = new JsonObject();
    private final Map<Integer, String> localizedDayOfWeekMap = new HashMap<>();
    private final Map<String, Integer> localizedReverseMap = new HashMap<>();
    private final ZoneId localZone;

    public EvccPlanHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry, ZoneId zoneId) {
        super(thing, channelTypeRegistry);
        localZone = zoneId; // necessary for testing
        index = Integer.parseInt(getPropertyOrConfigValue(PROPERTY_INDEX));
        vehicleID = getPropertyOrConfigValue(PROPERTY_VEHICLE_ID);
        type = PROPERTY_TYPE_PLAN;
    }

    private void buildLocalizedMaps(EvccBridgeHandler bridgeHandler) {
        LocaleProvider localeProvider = bridgeHandler.getLocaleProvider();
        Locale locale = localeProvider.getLocale();
        localizedDayOfWeekMap.putAll(IntStream.rangeClosed(0, 6).boxed().collect(Collectors.toUnmodifiableMap(d -> d,
                d -> (d == 0 ? DayOfWeek.SUNDAY : DayOfWeek.of(d)).getDisplayName(TextStyle.FULL, locale))));
        localizedReverseMap.putAll(localizedDayOfWeekMap.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey)));
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            JsonObject stateOpt = handler.getCachedEvccState().deepCopy();
            if (stateOpt.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }
            buildLocalizedMaps(handler);
            endpoint = String.join("/", handler.getBaseURL(), API_PATH_VEHICLES, vehicleID);
            if (index == 0) {
                endpoint = String.join("/", endpoint, API_PATH_PLAN_SOC);
            } else {
                endpoint = String.join("/", endpoint, API_PATH_PLAN_REPEATING);
            }
            handler.register(this);
            updateStatus(ThingStatus.ONLINE);
            isInitialized = true;
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
            if (index == 0) {
                if (state.has(JSON_KEY_PLAN)) {
                    state = state.getAsJsonObject(JSON_KEY_PLAN);
                    cachedOneTimePlan = state.deepCopy();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                    return;
                }
            } else if (index > 0 && state.has(JSON_KEY_REPEATING_PLANS)) {
                // Cache the plans
                while (!cachedRepeatingPlans.isEmpty()) {
                    cachedRepeatingPlans.remove(0);
                }
                cachedRepeatingPlans.addAll(state.getAsJsonArray(JSON_KEY_REPEATING_PLANS).deepCopy());
                // Check the bounds
                if (cachedRepeatingPlans.size() < index) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                    return;
                }
                // Get the corresponding repeating plan
                state = state.getAsJsonArray(JSON_KEY_REPEATING_PLANS).get(index - 1).getAsJsonObject();
                if (state.has(JSON_KEY_TIME) && state.has(JSON_KEY_TZ)) {
                    String time = state.get(JSON_KEY_TIME).getAsString();
                    String tz = state.get(JSON_KEY_TZ).getAsString();
                    ZonedDateTime zdt = convertEvccTimeToLocal(time, tz);
                    state.addProperty(JSON_KEY_TIME, zdt.toString());
                }
                if (state.has(JSON_KEY_WEEKDAYS)) {
                    parseWeekdaysResponse(state);
                }
                cachedRepeatingPlans.set(index - 1, state);
            }
            updateStatesFromApiResponse(state);
        }
    }

    private void parseWeekdaysResponse(JsonObject state) {
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
        }

        // Update the weekdays property to a localized string
        state.addProperty(JSON_KEY_WEEKDAYS, weekDays.toString());
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        if (index == 0 && !cachedOneTimePlan.isEmpty()) {
            return cachedOneTimePlan;
        } else if (index > 0 && cachedRepeatingPlans.size() >= index) {
            return cachedRepeatingPlans.get(index - 1).getAsJsonObject();
        } else {
            return new JsonObject();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_SEND_UPDATE.equals(channelUID.getId()) && OnOffType.ON.equals(command)
                && !pendingCommands.isEmpty()) {
            updatePlan();
            scheduler.schedule(() -> updateState(channelUID, OnOffType.OFF), 500, TimeUnit.MILLISECONDS);
        } else {
            if (!CHANNEL_SEND_UPDATE.equals(channelUID.getId()) && command instanceof State state) {
                String stateString = state.toString();
                if (CHANNEL_PLAN_SOC.equals(channelUID.getId())
                        || CHANNEL_PLAN_PRECONDITION.equals(channelUID.getId())) {
                    int spaceIdx = stateString.indexOf(" ");
                    if (spaceIdx != -1) {
                        stateString = stateString.substring(0, spaceIdx);
                    }
                }
                String channelKey = Utils.getKeyFromChannelUID(channelUID);
                pendingCommands.put(channelKey, stateString); // store for later processing
            } else {
                super.handleCommand(channelUID, command);
            }
        }
    }

    private void updatePlan() {
        boolean successful;
        if (index > 0 && (index - 1) < cachedRepeatingPlans.size()) {
            successful = updateRepeatingPlan();
        } else {
            successful = updateOneTimePlan();
        }
        if (successful) {
            pendingCommands.clear();
        }
    }

    private boolean updateRepeatingPlan() {
        JsonArray payload = cachedRepeatingPlans.deepCopy();
        JsonObject plan = payload.get(index - 1).getAsJsonObject();
        Map<String, String> values = getCachedValues(plan);
        for (Map.Entry<String, JsonElement> entry : plan.entrySet()) {
            String key = entry.getKey();
            if (values.containsKey(key) && values.get(key) instanceof String value) {
                switch (key) {
                    case JSON_KEY_WEEKDAYS -> {
                        JsonArray weekdaysArray = new JsonArray();
                        String[] days = value.split(";");
                        for (String day : days) {
                            Integer dayValue = localizedReverseMap.get(day);
                            if (dayValue != null) {
                                weekdaysArray.add(new JsonPrimitive(dayValue));
                            }
                        }
                        plan.add(key, weekdaysArray);
                    }
                    case JSON_KEY_SOC, JSON_KEY_PRECONDITION -> {
                        plan.add(key, new JsonPrimitive(Integer.parseInt(value)));
                    }
                    case JSON_KEY_ACTIVE -> {
                        if ("ON".equals(value)) {
                            plan.add(key, new JsonPrimitive(true));
                        } else {
                            plan.add(key, new JsonPrimitive(false));
                        }
                    }
                    case JSON_KEY_TIME -> {
                        JsonElement tzElement = plan.get(JSON_KEY_TZ);
                        if (tzElement == null || tzElement.isJsonNull()) {
                            return false;
                        }
                        try {
                            ZonedDateTime.parse(value);
                        } catch (DateTimeParseException ignored) {
                            try {
                                OffsetDateTime odt = OffsetDateTime.parse(value,
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
                                value = odt.toInstant().toString();

                            } catch (DateTimeParseException e) {
                                return false;
                            }
                        }
                        String tz = tzElement.getAsString();
                        ZonedDateTime evccTime = ZonedDateTime.parse(value);
                        String time = convertLocalTimeToEvcc(evccTime, tz);
                        plan.add(key, new JsonPrimitive(time));
                    }
                    default -> plan.add(key, new JsonPrimitive(value));
                }
            }
        }
        payload.set(index - 1, plan);
        return sendCommand(endpoint, payload);
    }

    private boolean updateOneTimePlan() {
        Map<String, String> values = getCachedValues(cachedOneTimePlan);
        String soc = values.get(JSON_KEY_SOC);
        String time = values.get(JSON_KEY_TIME);
        if (time == null || soc == null) {
            return false;
        }
        if (!TimeFormatValidator.isExactTimeFormat(time)) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(time,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
                time = odt.toInstant().toString();
            } catch (DateTimeParseException ignored) {
                return false; // time is not null and is not matching the time formats
            }
        }
        String precondition = values.get(JSON_KEY_PRECONDITION);
        String url = String.join("/", endpoint, soc, time);
        if (precondition != null) {
            url = String.join("?", url, "precondition=" + precondition);
        }
        return sendCommand(url, JsonNull.INSTANCE);
    }

    private Map<String, String> getCachedValues(JsonObject cachedValues) {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : cachedValues.entrySet()) {
            if (pendingCommands.containsKey(entry.getKey())) {
                String value = pendingCommands.get(entry.getKey());
                if (value != null) {
                    values.put(entry.getKey(), value);
                }
            } else {
                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                    values.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        }
        return values;
    }

    private ZonedDateTime convertEvccTimeToLocal(String time, String tz) {
        LocalTime lt = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        ZoneId evccZone = ZoneId.of(tz);
        ZonedDateTime evccTime = lt.atDate(LocalDate.now()).atZone(evccZone);
        return evccTime.withZoneSameInstant(localZone);
    }

    private String convertLocalTimeToEvcc(ZonedDateTime localTime, String tz) {
        LocalTime lt = localTime.toLocalTime();
        ZonedDateTime evccTime = lt.atDate(LocalDate.now()).atZone(localZone).withZoneSameInstant(ZoneId.of(tz));
        return evccTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private static final class TimeFormatValidator {

        // Exact: yyyy-MM-dd'T'HH:mm:ss'Z'
        private static final DateTimeFormatter EXACT_TIME_FMT = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").toFormatter().withResolverStyle(ResolverStyle.STRICT);

        /**
         * true, if input is exact yyyy-MM-dd'T'HH:mm:ss'Z'
         */
        public static boolean isExactTimeFormat(String input) {
            try {
                EXACT_TIME_FMT.parse(input);
                return true;
            } catch (DateTimeParseException ex) {
                return false;
            }
        }
    }
}
