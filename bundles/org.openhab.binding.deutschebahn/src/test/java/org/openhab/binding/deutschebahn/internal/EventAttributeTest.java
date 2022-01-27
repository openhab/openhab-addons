/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Event;
import org.openhab.binding.deutschebahn.internal.timetable.dto.EventStatus;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Message;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * Tests Mapping from {@link Event} attribute values to openhab state values.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
@SuppressWarnings("unchecked")
public class EventAttributeTest {

    private static final String SAMPLE_PATH = "Bielefeld Hbf|Herford|Löhne(Westf)|Bad Oeynhausen|Porta Westfalica|Minden(Westf)|Bückeburg|Stadthagen|Haste|Wunstorf|Hannover Hbf|Lehrte";

    private <VALUE_TYPE, STATE_TYPE extends State> void doTestEventAttribute( //
            String channelName, //
            @Nullable String expectedChannelName, //
            Consumer<Event> setValue, //
            VALUE_TYPE expectedValue, //
            @Nullable STATE_TYPE expectedState, //
            EventType eventType, //
            boolean performSetterTest) { //
        final EventAttribute<VALUE_TYPE, STATE_TYPE> attribute = (EventAttribute<VALUE_TYPE, STATE_TYPE>) EventAttribute
                .getByChannelName(channelName, eventType);
        assertThat(attribute, is(not(nullValue())));
        assertThat(attribute.getChannelTypeName(), is(expectedChannelName == null ? channelName : expectedChannelName));
        assertThat(attribute.getValue(new Event()), is(nullValue()));
        assertThat(attribute.getState(new Event()), is(nullValue()));

        // Create an event and set the attribute value.
        final Event eventWithValueSet = new Event();
        setValue.accept(eventWithValueSet);

        // then try get value and state.
        assertThat(attribute.getValue(eventWithValueSet), is(expectedValue));
        assertThat(attribute.getState(eventWithValueSet), is(expectedState));

        // Try set Value in new Event
        final Event copyTarget = new Event();
        attribute.setValue(copyTarget, expectedValue);
        if (performSetterTest) {
            assertThat(attribute.getValue(copyTarget), is(expectedValue));
        }
    }

    @Test
    public void testGetNonExistingChannel() {
        assertThat(EventAttribute.getByChannelName("unkownChannel", EventType.ARRIVAL), is(nullValue()));
    }

    @Test
    public void testPlannedPath() {
        doTestEventAttribute("planned-path", null, (Event e) -> e.setPpth(SAMPLE_PATH), SAMPLE_PATH,
                new StringType(SAMPLE_PATH), EventType.DEPARTURE, true);
    }

    @Test
    public void testChangedPath() {
        doTestEventAttribute("changed-path", null, (Event e) -> e.setCpth(SAMPLE_PATH), SAMPLE_PATH,
                new StringType(SAMPLE_PATH), EventType.DEPARTURE, true);
    }

    @Test
    public void testPlannedPlatform() {
        String platform = "2";
        doTestEventAttribute("planned-platform", null, (Event e) -> e.setPp(platform), platform,
                new StringType(platform), EventType.DEPARTURE, true);
    }

    @Test
    public void testChangedPlatform() {
        String platform = "2";
        doTestEventAttribute("changed-platform", null, (Event e) -> e.setCp(platform), platform,
                new StringType(platform), EventType.DEPARTURE, true);
    }

    @Test
    public void testWings() {
        String wings = "-906407760000782942-1403311431";
        doTestEventAttribute("wings", null, (Event e) -> e.setWings(wings), wings, new StringType(wings),
                EventType.DEPARTURE, true);
    }

    @Test
    public void testTransition() {
        String transition = "2016448009055686515-1403311438-1";
        doTestEventAttribute("transition", null, (Event e) -> e.setTra(transition), transition,
                new StringType(transition), EventType.DEPARTURE, true);
    }

    @Test
    public void testPlannedDistantEndpoint() {
        String endpoint = "Hannover Hbf";
        doTestEventAttribute("planned-distant-endpoint", null, (Event e) -> e.setPde(endpoint), endpoint,
                new StringType(endpoint), EventType.DEPARTURE, true);
    }

    @Test
    public void testChangedDistantEndpoint() {
        String endpoint = "Hannover Hbf";
        doTestEventAttribute("changed-distant-endpoint", null, (Event e) -> e.setCde(endpoint), endpoint,
                new StringType(endpoint), EventType.DEPARTURE, true);
    }

    @Test
    public void testLine() {
        String line = "RE60";
        doTestEventAttribute("line", null, (Event e) -> e.setL(line), line, new StringType(line), EventType.DEPARTURE,
                true);
    }

    @Test
    public void testPlannedTime() {
        String time = "2109111825";
        GregorianCalendar expectedValue = new GregorianCalendar(2021, 8, 11, 18, 25, 0);
        DateTimeType expectedState = new DateTimeType(
                ZonedDateTime.ofInstant(expectedValue.toInstant(), ZoneId.systemDefault()));
        doTestEventAttribute("planned-time", null, (Event e) -> e.setPt(time), expectedValue.getTime(), expectedState,
                EventType.DEPARTURE, true);
    }

    @Test
    public void testChangedTime() {
        String time = "2109111825";
        GregorianCalendar expectedValue = new GregorianCalendar(2021, 8, 11, 18, 25, 0);
        DateTimeType expectedState = new DateTimeType(
                ZonedDateTime.ofInstant(expectedValue.toInstant(), ZoneId.systemDefault()));
        doTestEventAttribute("changed-time", null, (Event e) -> e.setCt(time), expectedValue.getTime(), expectedState,
                EventType.DEPARTURE, true);
    }

    @Test
    public void testCancellationTime() {
        String time = "2109111825";
        GregorianCalendar expectedValue = new GregorianCalendar(2021, 8, 11, 18, 25, 0);
        DateTimeType expectedState = new DateTimeType(
                ZonedDateTime.ofInstant(expectedValue.toInstant(), ZoneId.systemDefault()));
        doTestEventAttribute("cancellation-time", null, (Event e) -> e.setClt(time), expectedValue.getTime(),
                expectedState, EventType.DEPARTURE, true);
    }

    @Test
    public void testPlannedStatus() {
        EventStatus expectedValue = EventStatus.A;
        doTestEventAttribute("planned-status", null, (Event e) -> e.setPs(expectedValue), expectedValue,
                new StringType(expectedValue.name().toLowerCase()), EventType.DEPARTURE, true);
    }

    @Test
    public void testChangedStatus() {
        EventStatus expectedValue = EventStatus.C;
        doTestEventAttribute("changed-status", null, (Event e) -> e.setCs(expectedValue), expectedValue,
                new StringType(expectedValue.name().toLowerCase()), EventType.DEPARTURE, true);
    }

    @Test
    public void testHidden() {
        doTestEventAttribute("hidden", null, (Event e) -> e.setHi(0), 0, OnOffType.OFF, EventType.DEPARTURE, true);
        doTestEventAttribute("hidden", null, (Event e) -> e.setHi(1), 1, OnOffType.ON, EventType.DEPARTURE, true);
    }

    @Test
    public void testDistantChange() {
        doTestEventAttribute("distant-change", null, (Event e) -> e.setDc(42), 42, new DecimalType(42),
                EventType.DEPARTURE, true);
    }

    @Test
    public void testPlannedFinalStation() {
        doTestEventAttribute("planned-final-station", "planned-target-station", (Event e) -> e.setPpth(SAMPLE_PATH),
                "Lehrte", new StringType("Lehrte"), EventType.DEPARTURE, false);
        doTestEventAttribute("planned-final-station", "planned-start-station", (Event e) -> e.setPpth(SAMPLE_PATH),
                "Bielefeld Hbf", new StringType("Bielefeld Hbf"), EventType.ARRIVAL, false);
    }

    @Test
    public void testChangedFinalStation() {
        doTestEventAttribute("changed-final-station", "changed-target-station", (Event e) -> e.setCpth(SAMPLE_PATH),
                "Lehrte", new StringType("Lehrte"), EventType.DEPARTURE, false);
        doTestEventAttribute("changed-final-station", "changed-start-station", (Event e) -> e.setCpth(SAMPLE_PATH),
                "Bielefeld Hbf", new StringType("Bielefeld Hbf"), EventType.ARRIVAL, false);
    }

    @Test
    public void testPlannedIntermediateStations() {
        String expectedFollowing = "Bielefeld Hbf - Herford - Löhne(Westf) - Bad Oeynhausen - Porta Westfalica - Minden(Westf) - Bückeburg - Stadthagen - Haste - Wunstorf - Hannover Hbf";
        doTestEventAttribute("planned-intermediate-stations", "planned-following-stations",
                (Event e) -> e.setPpth(SAMPLE_PATH),
                Arrays.asList("Bielefeld Hbf", "Herford", "Löhne(Westf)", "Bad Oeynhausen", "Porta Westfalica",
                        "Minden(Westf)", "Bückeburg", "Stadthagen", "Haste", "Wunstorf", "Hannover Hbf"),
                new StringType(expectedFollowing), EventType.DEPARTURE, false);
        String expectedPrevious = "Herford - Löhne(Westf) - Bad Oeynhausen - Porta Westfalica - Minden(Westf) - Bückeburg - Stadthagen - Haste - Wunstorf - Hannover Hbf - Lehrte";
        doTestEventAttribute("planned-intermediate-stations", "planned-previous-stations",
                (Event e) -> e.setPpth(SAMPLE_PATH),
                Arrays.asList("Herford", "Löhne(Westf)", "Bad Oeynhausen", "Porta Westfalica", "Minden(Westf)",
                        "Bückeburg", "Stadthagen", "Haste", "Wunstorf", "Hannover Hbf", "Lehrte"),
                new StringType(expectedPrevious), EventType.ARRIVAL, false);
    }

    @Test
    public void testChangedIntermediateStations() {
        String expectedFollowing = "Bielefeld Hbf - Herford - Löhne(Westf) - Bad Oeynhausen - Porta Westfalica - Minden(Westf) - Bückeburg - Stadthagen - Haste - Wunstorf - Hannover Hbf";
        doTestEventAttribute("changed-intermediate-stations", "changed-following-stations",
                (Event e) -> e.setCpth(SAMPLE_PATH),
                Arrays.asList("Bielefeld Hbf", "Herford", "Löhne(Westf)", "Bad Oeynhausen", "Porta Westfalica",
                        "Minden(Westf)", "Bückeburg", "Stadthagen", "Haste", "Wunstorf", "Hannover Hbf"),
                new StringType(expectedFollowing), EventType.DEPARTURE, false);
        String expectedPrevious = "Herford - Löhne(Westf) - Bad Oeynhausen - Porta Westfalica - Minden(Westf) - Bückeburg - Stadthagen - Haste - Wunstorf - Hannover Hbf - Lehrte";
        doTestEventAttribute("changed-intermediate-stations", "changed-previous-stations",
                (Event e) -> e.setCpth(SAMPLE_PATH),
                Arrays.asList("Herford", "Löhne(Westf)", "Bad Oeynhausen", "Porta Westfalica", "Minden(Westf)",
                        "Bückeburg", "Stadthagen", "Haste", "Wunstorf", "Hannover Hbf", "Lehrte"),
                new StringType(expectedPrevious), EventType.ARRIVAL, false);
    }

    @Test
    public void testMessages() {
        String expectedOneMessage = "Verzögerungen im Betriebsablauf";
        List<Message> messages = new ArrayList<>();
        Message m1 = new Message();
        m1.setC(99);
        messages.add(m1);
        doTestEventAttribute("messages", null, (Event e) -> e.getM().addAll(messages), messages,
                new StringType(expectedOneMessage), EventType.DEPARTURE, true);

        String expectedTwoMessages = "Verzögerungen im Betriebsablauf - keine Qualitätsmängel";
        Message m2 = new Message();
        m2.setC(88);
        messages.add(m2);
        doTestEventAttribute("messages", null, (Event e) -> e.getM().addAll(messages), messages,
                new StringType(expectedTwoMessages), EventType.DEPARTURE, true);
    }

    @Test
    public void testFilterDuplicateMessages() {
        String expectedOneMessage = "andere Reihenfolge der Wagen - technische Störung am Zug - Zug verkehrt richtig gereiht";
        List<Message> messages = new ArrayList<>();
        Message m1 = new Message();
        m1.setC(80);
        messages.add(m1);
        Message m2 = new Message();
        m2.setC(80);
        messages.add(m2);
        Message m3 = new Message();
        m3.setC(36);
        messages.add(m3);
        Message m4 = new Message();
        m4.setC(80);
        messages.add(m4);
        Message m5 = new Message();
        m5.setC(84);
        messages.add(m5);

        doTestEventAttribute("messages", null, (Event e) -> e.getM().addAll(messages), messages,
                new StringType(expectedOneMessage), EventType.DEPARTURE, true);
    }
}
