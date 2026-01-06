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

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final int index;
    private final String vehicleID;
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
                    state.addProperty(JSON_KEY_ACTIVE, true);
                    cachedOneTimePlan = state.deepCopy();
                } else {
                    JsonObject plan = new JsonObject();
                    plan.addProperty(JSON_KEY_ACTIVE, false);
                    state = plan;
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
        if (command instanceof State state) {
            String stateString = state.toString();
            if (CHANNEL_PLAN_SOC.equals(channelUID.getId()) || CHANNEL_PLAN_PRECONDITION.equals(channelUID.getId())) {
                int spaceIdx = stateString.indexOf(" ");
                if (spaceIdx != -1) {
                    stateString = stateString.substring(0, spaceIdx);
                }
            }
            String channelKey = Utils.getKeyFromChannelUID(channelUID);
            updatePlan(channelKey, stateString);
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    private void updatePlan(String channelKey, String value) {
        if (index > 0 && (index - 1) < cachedRepeatingPlans.size()) {
            updateRepeatingPlan(channelKey, value);
        } else {
            updateOneTimePlan(channelKey, value);
        }
    }

    private void updateRepeatingPlan(String channelKey, String changedValue) {
        JsonArray payload = cachedRepeatingPlans.deepCopy();
        JsonObject plan = payload.get(index - 1).getAsJsonObject();
        switch (channelKey) {
            case JSON_KEY_ACTIVE -> plan.addProperty(channelKey, "ON".equals(changedValue));
            case JSON_KEY_SOC, JSON_KEY_PRECONDITION ->
                plan.add(channelKey, new JsonPrimitive(Integer.parseInt(changedValue)));
            default -> plan.addProperty(channelKey, changedValue);
        }
        for (Map.Entry<String, JsonElement> entry : plan.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();
            switch (key) {
                case JSON_KEY_WEEKDAYS -> {
                    JsonArray weekdaysArray = new JsonArray();
                    String[] days = plan.get(JSON_KEY_WEEKDAYS).getAsString().split(";");
                    for (String day : days) {
                        Integer dayValue = localizedReverseMap.get(day);
                        if (dayValue != null) {
                            weekdaysArray.add(new JsonPrimitive(dayValue));
                        }
                    }
                    plan.add(JSON_KEY_WEEKDAYS, weekdaysArray);
                }
                case JSON_KEY_TIME -> {
                    JsonElement tzElement = plan.get(JSON_KEY_TZ);
                    if (tzElement == null || tzElement.isJsonNull()) {
                        return;
                    }
                    try {
                        ZonedDateTime.parse(value);
                    } catch (DateTimeParseException ignored) {
                        try {
                            OffsetDateTime odt = OffsetDateTime.parse(value,
                                    DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
                            value = odt.toString();

                        } catch (DateTimeParseException e) {
                            return;
                        }
                    }
                    String tz = tzElement.getAsString();
                    ZonedDateTime evccTime = ZonedDateTime.parse(value);
                    String time = convertLocalTimeToEvcc(evccTime, tz);
                    plan.add(JSON_KEY_TIME, new JsonPrimitive(time));
                }
                default -> plan.add(key, entry.getValue());
            }
        }

        performApiRequest(endpoint, POST, payload);
    }

    private void updateOneTimePlan(String channelKey, String value) {
        if (JSON_KEY_ACTIVE.equals(channelKey) && "OFF".equals(value)) {
            performApiRequest(endpoint, DELETE, JsonNull.INSTANCE);
            return;
        }
        ZonedDateTime zdt = ZonedDateTime.now(localZone).plusHours(1).withSecond(0).withNano(0);
        String time = zdt.toInstant().toString(); // Default time
        if (JSON_KEY_TIME.equals(channelKey)) {
            if (!TimeFormatValidator.isExactTimeFormat(value)) {
                try {
                    OffsetDateTime odt = OffsetDateTime.parse(value, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
                    time = odt.toZonedDateTime().toInstant().toString();
                } catch (DateTimeParseException ignored) {
                    return; // time is not null and is not matching the time formats
                }
            }
        } else if (cachedOneTimePlan.has(JSON_KEY_TIME)) {
            time = cachedOneTimePlan.get(JSON_KEY_TIME).getAsString();
        }
        String soc = "100";
        if (JSON_KEY_SOC.equals(channelKey)) {
            soc = value;
        } else if (cachedOneTimePlan.has(JSON_KEY_SOC)) {
            soc = cachedOneTimePlan.get(JSON_KEY_SOC).getAsString();
        }
        String url = String.join("/", endpoint, soc, time);
        String precondition = "";
        if (JSON_KEY_PRECONDITION.equals(channelKey)) {
            precondition = value;
        } else if (cachedOneTimePlan.has(JSON_KEY_PRECONDITION)) {
            cachedOneTimePlan.get(JSON_KEY_PRECONDITION).getAsString();
        }
        if (!precondition.isEmpty()) {
            url = String.join("?", url, "precondition=" + precondition);
        }
        performApiRequest(url, POST, JsonNull.INSTANCE);
    }

    private ZonedDateTime convertEvccTimeToLocal(String time, String tz) {
        LocalTime lt = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        ZoneId evccZone = ZoneId.of(tz);
        ZonedDateTime evccTime = lt.atDate(LocalDate.now(localZone)).atZone(evccZone);
        return evccTime.withZoneSameInstant(localZone);
    }

    private String convertLocalTimeToEvcc(ZonedDateTime localTime, String tz) {
        LocalTime lt = localTime.toLocalTime();
        ZonedDateTime evccTime = lt.atDate(LocalDate.now(localZone)).atZone(ZoneId.of(tz));
        return evccTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private static final class TimeFormatValidator {

        // Exact: yyyy-MM-dd'T'HH:mm:ss'Z'
        private static final DateTimeFormatter EXACT_TIME_FMT = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").toFormatter(Locale.ROOT)
                .withResolverStyle(ResolverStyle.STRICT);

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
