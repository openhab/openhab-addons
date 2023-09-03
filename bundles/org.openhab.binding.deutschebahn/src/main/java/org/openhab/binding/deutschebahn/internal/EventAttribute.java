/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Event;
import org.openhab.binding.deutschebahn.internal.timetable.dto.EventStatus;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Message;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * Selector for the Attribute of an {@link Event}.
 *
 * chapter "1.2.11 Event" in Technical Interface Description for external Developers
 *
 * @see <a href="https://developers.deutschebahn.com/db-api-marketplace/apis/product/timetables">DB API Marketplace</a>
 *
 * @author Sönke Küper - initial contribution
 *
 * @param <VALUE_TYPE> type of value in Bean.
 * @param <STATE_TYPE> type of state.
 */
@NonNullByDefault
public final class EventAttribute<VALUE_TYPE, STATE_TYPE extends State>
        extends AbstractDtoAttributeSelector<Event, @Nullable VALUE_TYPE, STATE_TYPE> {

    /**
     * Planned Path.
     */
    public static final EventAttribute<String, StringType> PPTH = new EventAttribute<>("planned-path", Event::getPpth,
            Event::setPpth, StringType::new, EventAttribute::splitOnPipeToList, StringType.class);

    /**
     * Changed Path.
     */
    public static final EventAttribute<String, StringType> CPTH = new EventAttribute<>("changed-path", Event::getCpth,
            Event::setCpth, StringType::new, EventAttribute::splitOnPipeToList, StringType.class);
    /**
     * Planned platform.
     */
    public static final EventAttribute<String, StringType> PP = new EventAttribute<>("planned-platform", Event::getPp,
            Event::setPp, StringType::new, EventAttribute::singletonList, StringType.class);
    /**
     * Changed platform.
     */
    public static final EventAttribute<String, StringType> CP = new EventAttribute<>("changed-platform", Event::getCp,
            Event::setCp, StringType::new, EventAttribute::singletonList, StringType.class);
    /**
     * Planned time.
     */
    public static final EventAttribute<Date, DateTimeType> PT = new EventAttribute<>("planned-time",
            getDate(Event::getPt), setDate(Event::setPt), EventAttribute::createDateTimeType,
            EventAttribute::mapDateToStringList, DateTimeType.class);
    /**
     * Changed time.
     */
    public static final EventAttribute<Date, DateTimeType> CT = new EventAttribute<>("changed-time",
            getDate(Event::getCt), setDate(Event::setCt), EventAttribute::createDateTimeType,
            EventAttribute::mapDateToStringList, DateTimeType.class);
    /**
     * Planned status.
     */
    public static final EventAttribute<EventStatus, StringType> PS = new EventAttribute<>("planned-status",
            Event::getPs, Event::setPs, EventAttribute::fromEventStatus, EventAttribute::listFromEventStatus,
            StringType.class);
    /**
     * Changed status.
     */
    public static final EventAttribute<EventStatus, StringType> CS = new EventAttribute<>("changed-status",
            Event::getCs, Event::setCs, EventAttribute::fromEventStatus, EventAttribute::listFromEventStatus,
            StringType.class);
    /**
     * Hidden.
     */
    public static final EventAttribute<Integer, OnOffType> HI = new EventAttribute<>("hidden", Event::getHi,
            Event::setHi, EventAttribute::parseHidden, EventAttribute::mapIntegerToStringList, OnOffType.class);
    /**
     * Cancellation time.
     */
    public static final EventAttribute<Date, DateTimeType> CLT = new EventAttribute<>("cancellation-time",
            getDate(Event::getClt), setDate(Event::setClt), EventAttribute::createDateTimeType,
            EventAttribute::mapDateToStringList, DateTimeType.class);
    /**
     * Wing.
     */
    public static final EventAttribute<String, StringType> WINGS = new EventAttribute<>("wings", Event::getWings,
            Event::setWings, StringType::new, EventAttribute::splitOnPipeToList, StringType.class);
    /**
     * Transition.
     */
    public static final EventAttribute<String, StringType> TRA = new EventAttribute<>("transition", Event::getTra,
            Event::setTra, StringType::new, EventAttribute::singletonList, StringType.class);
    /**
     * Planned distant endpoint.
     */
    public static final EventAttribute<String, StringType> PDE = new EventAttribute<>("planned-distant-endpoint",
            Event::getPde, Event::setPde, StringType::new, EventAttribute::singletonList, StringType.class);
    /**
     * Changed distant endpoint.
     */
    public static final EventAttribute<String, StringType> CDE = new EventAttribute<>("changed-distant-endpoint",
            Event::getCde, Event::setCde, StringType::new, EventAttribute::singletonList, StringType.class);
    /**
     * Distant change.
     */
    public static final EventAttribute<Integer, DecimalType> DC = new EventAttribute<>("distant-change", Event::getDc,
            Event::setDc, DecimalType::new, EventAttribute::mapIntegerToStringList, DecimalType.class);
    /**
     * Line.
     */
    public static final EventAttribute<String, StringType> L = new EventAttribute<>("line", Event::getL, Event::setL,
            StringType::new, EventAttribute::singletonList, StringType.class);

    /**
     * Messages.
     */
    public static final EventAttribute<List<Message>, StringType> MESSAGES = new EventAttribute<>("messages",
            EventAttribute.getMessages(), EventAttribute::setMessages, EventAttribute::mapMessages,
            EventAttribute::mapMessagesToList, StringType.class);

    /**
     * Planned Start station.
     */
    public static final EventAttribute<String, StringType> PLANNED_START_STATION = new EventAttribute<>(
            "planned-start-station", EventAttribute.getSingleStationFromPath(Event::getPpth, true),
            EventAttribute.voidSetter(), StringType::new, EventAttribute::singletonList, StringType.class);

    /**
     * Planned Previous stations.
     */
    public static final EventAttribute<List<String>, StringType> PLANNED_PREVIOUS_STATIONS = new EventAttribute<>(
            "planned-previous-stations", EventAttribute.getIntermediateStationsFromPath(Event::getPpth, true),
            EventAttribute.voidSetter(), EventAttribute::fromStringList, EventAttribute::nullToEmptyList,
            StringType.class);

    /**
     * Planned Target station.
     */
    public static final EventAttribute<String, StringType> PLANNED_TARGET_STATION = new EventAttribute<>(
            "planned-target-station", EventAttribute.getSingleStationFromPath(Event::getPpth, false),
            EventAttribute.voidSetter(), StringType::new, EventAttribute::singletonList, StringType.class);

    /**
     * Planned Following stations.
     */
    public static final EventAttribute<List<String>, StringType> PLANNED_FOLLOWING_STATIONS = new EventAttribute<>(
            "planned-following-stations", EventAttribute.getIntermediateStationsFromPath(Event::getPpth, false),
            EventAttribute.voidSetter(), EventAttribute::fromStringList, EventAttribute::nullToEmptyList,
            StringType.class);

    /**
     * Changed Start station.
     */
    public static final EventAttribute<String, StringType> CHANGED_START_STATION = new EventAttribute<>(
            "changed-start-station", EventAttribute.getSingleStationFromPath(Event::getCpth, true),
            EventAttribute.voidSetter(), StringType::new, EventAttribute::singletonList, StringType.class);

    /**
     * Changed Previous stations.
     */
    public static final EventAttribute<List<String>, StringType> CHANGED_PREVIOUS_STATIONS = new EventAttribute<>(
            "changed-previous-stations", EventAttribute.getIntermediateStationsFromPath(Event::getCpth, true),
            EventAttribute.voidSetter(), EventAttribute::fromStringList, EventAttribute::nullToEmptyList,
            StringType.class);

    /**
     * Changed Target station.
     */
    public static final EventAttribute<String, StringType> CHANGED_TARGET_STATION = new EventAttribute<>(
            "changed-target-station", EventAttribute.getSingleStationFromPath(Event::getCpth, false),
            EventAttribute.voidSetter(), StringType::new, EventAttribute::singletonList, StringType.class);

    /**
     * Changed Following stations.
     */
    public static final EventAttribute<List<String>, StringType> CHANGED_FOLLOWING_STATIONS = new EventAttribute<>(
            "changed-following-stations", EventAttribute.getIntermediateStationsFromPath(Event::getCpth, false),
            EventAttribute.voidSetter(), EventAttribute::fromStringList, EventAttribute::nullToEmptyList,
            StringType.class);

    /**
     * List containing all known {@link EventAttribute}.
     */
    public static final List<EventAttribute<?, ?>> ALL_ATTRIBUTES = Arrays.asList(PPTH, CPTH, PP, CP, PT, CT, PS, CS,
            HI, CLT, WINGS, TRA, PDE, CDE, DC, L, MESSAGES);

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyMMddHHmm");

    /**
     * Creates a new {@link EventAttribute}.
     *
     * @param getter Function to get the raw value.
     * @param setter Function to set the raw value.
     * @param getState Function to get the Value as {@link State}.
     */
    private EventAttribute(final String channelTypeName, //
            final Function<Event, @Nullable VALUE_TYPE> getter, //
            final BiConsumer<Event, VALUE_TYPE> setter, //
            final Function<VALUE_TYPE, @Nullable STATE_TYPE> getState, //
            final Function<VALUE_TYPE, List<String>> valueToList, //
            final Class<STATE_TYPE> stateType) {
        super(channelTypeName, getter, setter, getState, valueToList, stateType);
    }

    private static StringType fromEventStatus(final EventStatus value) {
        return new StringType(value.value());
    }

    private static List<String> listFromEventStatus(final @Nullable EventStatus value) {
        if (value == null) {
            return List.of();
        } else {
            return List.of(value.value());
        }
    }

    private static StringType fromStringList(final List<String> value) {
        return new StringType(value.stream().collect(Collectors.joining(" - ")));
    }

    private static List<String> nullToEmptyList(@Nullable final List<String> value) {
        return value == null ? List.of() : value;
    }

    /**
     * Returns a list containing only the given value or empty list if value is <code>null</code>.
     */
    private static List<String> singletonList(@Nullable String value) {
        return value == null ? List.of() : List.of(value);
    }

    private static OnOffType parseHidden(@Nullable Integer value) {
        return OnOffType.from(value != null && value == 1);
    }

    private static Function<Event, @Nullable Date> getDate(final Function<Event, @Nullable String> getValue) {
        return (final Event event) -> parseDate(getValue.apply(event));
    }

    private static BiConsumer<Event, Date> setDate(final BiConsumer<Event, String> setter) {
        return (final Event event, final Date value) -> {
            synchronized (DATETIME_FORMAT) {
                String formattedDate = DATETIME_FORMAT.format(value);
                setter.accept(event, formattedDate);
            }
        };
    }

    private static void setMessages(Event event, List<Message> messages) {
        event.getM().clear();
        event.getM().addAll(messages);
    }

    @Nullable
    private static synchronized Date parseDate(@Nullable final String dateValue) {
        if ((dateValue == null) || dateValue.isEmpty()) {
            return null;
        }
        try {
            synchronized (DATETIME_FORMAT) {
                return DATETIME_FORMAT.parse(dateValue);
            }
        } catch (final ParseException e) {
            return null;
        }
    }

    @Nullable
    private static DateTimeType createDateTimeType(final @Nullable Date value) {
        if (value == null) {
            return null;
        } else {
            final ZonedDateTime d = ZonedDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
            return new DateTimeType(d);
        }
    }

    /**
     * Maps the status codes from the messages into status texts.
     */
    @Nullable
    private static StringType mapMessages(final @Nullable List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return StringType.EMPTY;
        } else {
            final String messageTexts = messages //
                    .stream()//
                    .filter((Message message) -> message.getC() != null) //
                    .map(Message::getC) //
                    .distinct() //
                    .map(MessageCodes::getMessage) //
                    .filter((String messageText) -> !messageText.isEmpty()) //
                    .collect(Collectors.joining(" - "));

            return new StringType(messageTexts);
        }
    }

    /**
     * Maps the status codes from the messages into string list.
     */
    private static List<String> mapMessagesToList(final @Nullable List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        } else {
            return messages //
                    .stream()//
                    .filter((Message message) -> message.getC() != null) //
                    .map(Message::getC) //
                    .distinct() //
                    .map(MessageCodes::getMessage) //
                    .filter((String messageText) -> !messageText.isEmpty()) //
                    .collect(Collectors.toList());
        }
    }

    private static Function<Event, @Nullable List<Message>> getMessages() {
        return new Function<Event, @Nullable List<Message>>() {

            @Override
            public @Nullable List<Message> apply(Event t) {
                if (t.getM().isEmpty()) {
                    return null;
                } else {
                    return t.getM();
                }
            }
        };
    }

    private static List<String> mapIntegerToStringList(@Nullable Integer value) {
        if (value == null) {
            return List.of();
        } else {
            return List.of(String.valueOf(value));
        }
    }

    private static List<String> mapDateToStringList(@Nullable Date value) {
        if (value == null) {
            return List.of();
        } else {
            synchronized (DATETIME_FORMAT) {
                return List.of(DATETIME_FORMAT.format(value));
            }
        }
    }

    /**
     * Returns a single station from a path value (i.e. pipe separated value of stations).
     * 
     * @param getPath Getter for the path.
     * @param returnFirst if <code>true</code> the first value will be returned, <code>false</code> will return the last
     *            value.
     */
    private static Function<Event, @Nullable String> getSingleStationFromPath(
            final Function<Event, @Nullable String> getPath, boolean returnFirst) {
        return (final Event event) -> {
            String path = getPath.apply(event);
            if (path == null || path.isEmpty()) {
                return null;
            }

            final String[] stations = splitPath(path);
            if (returnFirst) {
                return stations[0];
            } else {
                return stations[stations.length - 1];
            }
        };
    }

    /**
     * Returns all intermediate stations from a path. The first or last station will be omitted. The values will be
     * separated by a single dash -.
     * 
     * @param getPath Getter for the path.
     * @param removeFirst if <code>true</code> the first value will be removed, <code>false</code> will remove the last
     *            value.
     */
    private static Function<Event, @Nullable List<String>> getIntermediateStationsFromPath(
            final Function<Event, @Nullable String> getPath, boolean removeFirst) {
        return (final Event event) -> {
            final String path = getPath.apply(event);
            if (path == null || path.isEmpty()) {
                return null;
            }
            final String[] stationValues = splitPath(path);
            Stream<String> stations = Arrays.stream(stationValues);
            if (removeFirst) {
                stations = stations.skip(1);
            } else {
                stations = stations.limit(stationValues.length - 1);
            }
            return stations.collect(Collectors.toList());
        };
    }

    /**
     * Setter that does nothing.
     * Used for derived attributes that can't be set.
     */
    private static <VALUE_TYPE> BiConsumer<Event, VALUE_TYPE> voidSetter() {
        return new BiConsumer<Event, VALUE_TYPE>() {

            @Override
            public void accept(Event t, VALUE_TYPE u) {
            }
        };
    }

    private static String[] splitPath(final String path) {
        return path.split("\\|");
    }

    private static List<String> splitOnPipeToList(final String value) {
        return Arrays.asList(value.split("\\|"));
    }

    /**
     * Returns an {@link EventAttribute} for the given channel-type and {@link EventType}.
     */
    @Nullable
    public static EventAttribute<?, ?> getByChannelName(final String channelName, EventType eventType) {
        switch (channelName) {
            case "planned-path":
                return PPTH;
            case "changed-path":
                return CPTH;
            case "planned-platform":
                return PP;
            case "changed-platform":
                return CP;
            case "planned-time":
                return PT;
            case "changed-time":
                return CT;
            case "planned-status":
                return PS;
            case "changed-status":
                return CS;
            case "hidden":
                return HI;
            case "cancellation-time":
                return CLT;
            case "wings":
                return WINGS;
            case "transition":
                return TRA;
            case "planned-distant-endpoint":
                return PDE;
            case "changed-distant-endpoint":
                return CDE;
            case "distant-change":
                return DC;
            case "line":
                return L;
            case "messages":
                return MESSAGES;
            case "planned-final-station":
                return eventType == EventType.ARRIVAL ? PLANNED_START_STATION : PLANNED_TARGET_STATION;
            case "planned-intermediate-stations":
                return eventType == EventType.ARRIVAL ? PLANNED_PREVIOUS_STATIONS : PLANNED_FOLLOWING_STATIONS;
            case "changed-final-station":
                return eventType == EventType.ARRIVAL ? CHANGED_START_STATION : CHANGED_TARGET_STATION;
            case "changed-intermediate-stations":
                return eventType == EventType.ARRIVAL ? CHANGED_PREVIOUS_STATIONS : CHANGED_FOLLOWING_STATIONS;
            default:
                return null;
        }
    }
}
