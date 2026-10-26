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
package org.openhab.binding.awtrixng.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.awtrixng.internal.dto.AppPayload;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

import com.google.gson.JsonParser;

/**
 * Test cases for the {@link AppChannels}.
 *
 * @author Keith T. Garner - Initial contribution
 */
class AppChannelsTest {

    private static PayloadChannel<AppPayload> channel(String id) {
        PayloadChannel<AppPayload> channel = AppChannels.get(id);
        assertNotNull(channel, id);
        return channel;
    }

    /** The JSON an app that only received this command would send. */
    private static String json(String channelId, Command command) {
        AppPayload payload = channel(channelId).toPayload(AppPayload::new, command);
        assertNotNull(payload, channelId);
        return Helper.toJson(payload);
    }

    private static void assertJson(String expected, String actual) {
        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(actual), actual);
    }

    private static AppPayload app(String json) {
        AppPayload payload = Helper.fromJson(json, AppPayload.class);
        assertNotNull(payload);
        return payload;
    }

    @Test
    void textWithFontTagsBecomesFragments() {
        assertJson("{\"text\":\"hello\"}", json("text", new StringType("hello")));
        assertJson("{\"text\":[{\"text\":\"a \"},{\"text\":\"b\",\"color\":\"#FF0000\"}]}",
                json("text", new StringType("a <font color=\"#FF0000\">b</font>")));
    }

    @Test
    void textStateShowsTheMarkup() {
        AppPayload payload = app("{\"text\":[{\"text\":\"b\",\"color\":\"#FF0000\"}]}");

        assertEquals(new StringType("<font color=\"#FF0000\">b</font>"), channel("text").toState(payload));
    }

    @Test
    void enumsAreSentAsNgWords() {
        assertJson("{\"textCase\":\"asTyped\"}", json("text-case", new StringType("asTyped")));
        assertJson("{\"iconMode\":\"pushOnce\"}", json("icon-mode", new StringType("pushOnce")));
        assertJson("{\"lifetimeExpiry\":\"mark\"}", json("lifetime-expiry", new StringType("mark")));
        assertJson("{\"textAlign\":\"start\"}", json("text-align", new StringType("start")));
    }

    @Test
    void durationsAreSentInMilliseconds() {
        assertJson("{\"durationMs\":10000}", json("duration", new QuantityType<>(10, Units.SECOND)));
        assertJson("{\"lifetimeMs\":300000}", json("lifetime", new DecimalType(300000)));
        assertJson("{\"textBlinkMs\":600}",
                json("text-blink", new QuantityType<>(600, MetricPrefix.MILLI(Units.SECOND))));
        assertJson("{\"textFadeMs\":500}", json("text-fade", new DecimalType(500)));
    }

    private static boolean accepts(String channelId, Command command) {
        return channel(channelId).apply(new AppPayload(), command);
    }

    @Test
    void valuesOutsideTheRangeOfNgAreRejected() {
        assertFalse(accepts("progress", new DecimalType(150)));
        assertFalse(accepts("scroll-speed", new DecimalType(-1)));
        assertFalse(accepts("repeat", new DecimalType(-1)));
        assertFalse(accepts("icon-gap", new DecimalType(129)));
        assertFalse(accepts("effect-speed", new DecimalType(0.05)));
        assertFalse(accepts("effect-speed", new DecimalType(11)));
        assertFalse(accepts("palette-speed", new DecimalType(10.5)));
        assertFalse(accepts("text-blink", new DecimalType(-5)));
        assertFalse(accepts("text-fade", new DecimalType(3_000_000_000L)));
        assertFalse(accepts("lifetime", new DecimalType(-1)));
        assertFalse(accepts("repeat", new DecimalType(3_000_000_000L)));
    }

    @Test
    void valuesAtTheEdgeOfTheRangeAreAccepted() {
        assertTrue(accepts("progress", new DecimalType(100)));
        assertTrue(accepts("progress", new DecimalType(-1)));
        assertTrue(accepts("scroll-speed", new DecimalType(250)));
        assertTrue(accepts("icon-gap", new DecimalType(128)));
        assertTrue(accepts("effect-speed", new DecimalType(0.1)));
        assertTrue(accepts("effect-speed", new DecimalType(10)));
        assertTrue(accepts("palette-speed", new DecimalType(0)));
        assertTrue(accepts("duration", new DecimalType(-1)));
    }

    @Test
    void wordsThatNgDoesNotKnowAreRejected() {
        assertFalse(accepts("text-case", new StringType("upperCase")));
        assertFalse(accepts("text-align", new StringType("left")));
        assertFalse(accepts("scroll-mode", new StringType("marquee")));
        assertFalse(accepts("icon-mode", new StringType("")));
        assertFalse(accepts("lifetime-expiry", new StringType("delete")));
        assertTrue(accepts("scroll-mode", new StringType("bounce")));
        assertTrue(accepts("effect", new StringType("")));
        assertTrue(accepts("overlay", new StringType("")));
    }

    @Test
    void durationStateIsInMilliseconds() {
        AppPayload payload = app("{\"durationMs\":7000,\"lifetimeMs\":0}");

        assertEquals(new QuantityType<>(7000, MetricPrefix.MILLI(Units.SECOND)), channel("duration").toState(payload));
        assertEquals(new QuantityType<>(0, MetricPrefix.MILLI(Units.SECOND)), channel("lifetime").toState(payload));
    }

    @Test
    void scrollChannelsShareOneObject() {
        AppPayload payload = new AppPayload();
        assertTrue(channel("scroll-mode").apply(payload, new StringType("bounce")));
        assertTrue(channel("scroll-speed").apply(payload, new DecimalType(50)));
        assertTrue(channel("scroll-hold").apply(payload, new DecimalType(2000)));

        assertJson("{\"scroll\":{\"mode\":\"bounce\",\"speed\":50,\"holdMs\":2000}}", Helper.toJson(payload));
        assertEquals(new StringType("bounce"), channel("scroll-mode").toState(payload));
        assertEquals(new QuantityType<>(50, Units.PERCENT), channel("scroll-speed").toState(payload));
        assertNull(channel("scroll-gap").toState(payload));
    }

    @Test
    void colorsAreSentAsHex() {
        assertJson("{\"backgroundColor\":\"#FF0000\"}", json("background-color", HSBType.RED));
        assertJson("{\"progressColor\":\"#FFFFFF\"}", json("progress-color", HSBType.WHITE));
        assertJson("{\"chartColor\":\"#0000FF\"}", json("chart-color", HSBType.BLUE));
    }

    @Test
    void paletteStopsThatHaveNoTextAreLeftOut() {
        AppPayload payload = new AppPayload();
        payload.palette = JsonParser.parseString("[\"#FF0000\",{\"a\":1},\"#00FF00\"]");

        assertEquals(new StringType("#FF0000,#00FF00"), channel("palette").toState(payload));

        payload.palette = JsonParser.parseString("{\"a\":1}");

        assertEquals(new StringType(""), channel("palette").toState(payload));
    }

    @Test
    void textColorAndPaletteAreAlternatives() {
        AppPayload payload = new AppPayload();

        assertTrue(channel("text-palette").apply(payload, OnOffType.ON));
        assertJson("{\"textColor\":\"palette\"}", Helper.toJson(payload));
        assertEquals(OnOffType.ON, channel("text-palette").toState(payload));
        assertNull(channel("text-color").toState(payload));

        assertTrue(channel("text-color").apply(payload, HSBType.RED));
        assertJson("{\"textColor\":\"#FF0000\"}", Helper.toJson(payload));
        assertEquals(OnOffType.OFF, channel("text-palette").toState(payload));
        assertEquals(HSBType.RED, channel("text-color").toState(payload));

        // Switching the palette off is no reason to lose the color that is set
        assertTrue(channel("text-palette").apply(payload, OnOffType.OFF));
        assertJson("{\"textColor\":\"#FF0000\"}", Helper.toJson(payload));

        assertTrue(channel("text-palette").apply(payload, OnOffType.ON));
        assertTrue(channel("text-palette").apply(payload, OnOffType.OFF));
        assertJson("{}", Helper.toJson(payload));
    }

    @Test
    void paletteIsANameOrAListOfColors() {
        assertJson("{\"palette\":\"Rainbow\"}", json("palette", new StringType("Rainbow")));
        assertJson("{\"palette\":[\"#FFFF00\",\"#FF0000\"]}", json("palette", new StringType("#FFFF00, #FF0000")));
        assertJson("{}", json("palette", new StringType("")));

        assertEquals(new StringType("Rainbow"), channel("palette").toState(app("{\"palette\":\"Rainbow\"}")));
        assertEquals(new StringType("#FFFF00,#FF0000"),
                channel("palette").toState(app("{\"palette\":[\"#FFFF00\",\"#FF0000\"]}")));
    }

    @Test
    void chartsAreCommaSeparatedAndKeepTheNewest16Values() {
        assertJson("{\"barChart\":[1,2,3]}", json("bar-chart", new StringType("1, 2,3")));
        assertJson("{\"lineChart\":[5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20]}",
                json("line-chart", new StringType("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20")));
        assertJson("{}", json("bar-chart", new StringType("")));
        assertEquals(new StringType("4,5"), channel("bar-chart").toState(app("{\"barChart\":[4,5]}")));
    }

    @Test
    void invalidChartIsRejected() {
        assertNull(channel("bar-chart").toPayload(AppPayload::new, new StringType("1,two,3")));
        assertNull(channel("bar-chart").toPayload(AppPayload::new, new DecimalType(1)));
    }

    @Test
    void iconsNeedAnIconAndCoordinatesThatNgAccepts() {
        assertJson("{\"icons\":[{\"icon\":\"1234\"}]}", json("icons", new StringType("[{\"icon\":\"1234\"}]")));
        assertJson("{\"icons\":[{\"icon\":\"1234\",\"x\":-65535,\"y\":65535}]}",
                json("icons", new StringType("[{\"icon\":\"1234\",\"x\":-65535,\"y\":65535}]")));
        for (String invalid : new String[] { "[{\"x\":1}]", "[{\"icon\":\"\"}]", "[{\"icon\":\" \"}]", "[null]",
                "[{\"icon\":\"1234\",\"x\":65536}]", "[{\"icon\":\"1234\",\"y\":-65536}]" }) {
            assertNull(channel("icons").toPayload(AppPayload::new, new StringType(invalid)), invalid);
        }
    }

    @Test
    void iconsAreAJsonArrayOfAtMostFour() {
        assertJson("{\"icons\":[{\"icon\":\"1234\",\"x\":0,\"y\":1}]}",
                json("icons", new StringType("[{\"icon\":\"1234\",\"x\":0,\"y\":1}]")));
        assertNull(channel("icons").toPayload(AppPayload::new, new StringType("not json")));
        assertNull(channel("icons").toPayload(AppPayload::new, new StringType(
                "[{\"icon\":\"1\"},{\"icon\":\"2\"},{\"icon\":\"3\"},{\"icon\":\"4\"},{\"icon\":\"5\"}]")));
        assertJson("{}", json("icons", new StringType("")));
    }

    @Test
    void drawIsAJsonArray() {
        assertJson("{\"draw\":[[\"pixel\",1,2,\"#FF0000\"]]}",
                json("draw", new StringType("[[\"pixel\",1,2,\"#FF0000\"]]")));
        assertNull(channel("draw").toPayload(AppPayload::new, new StringType("{\"dp\":[]}")));
        assertNull(channel("draw").toPayload(AppPayload::new, new StringType("[unclosed")));
        assertJson("{}", json("draw", new StringType("")));
    }

    @Test
    void numbersAndSwitches() {
        assertJson("{\"progress\":64}", json("progress", new QuantityType<>(64, Units.PERCENT)));
        assertJson("{\"repeat\":2}", json("repeat", new DecimalType(2)));
        assertJson("{\"effectSpeed\":1.5}", json("effect-speed", new DecimalType(1.5)));
        assertJson("{\"paletteSpeed\":0.5}", json("palette-speed", new DecimalType(0.5)));
        assertJson("{\"chartAutoscale\":false}", json("chart-autoscale", OnOffType.OFF));
        assertJson("{\"textInFront\":true}", json("text-in-front", OnOffType.ON));
        assertJson("{\"paletteBlend\":false}", json("palette-blend", OnOffType.OFF));
    }

    @Test
    void commandOfTheWrongTypeIsRejected() {
        assertNull(channel("text").toPayload(AppPayload::new, new DecimalType(1)));
        assertNull(channel("duration").toPayload(AppPayload::new, new StringType("1")));
        assertNull(channel("background-color").toPayload(AppPayload::new, OnOffType.ON));
        assertNull(channel("chart-autoscale").toPayload(AppPayload::new, new StringType("true")));
    }

    @Test
    void fieldsThatAreNotSetHaveNoState() {
        AppPayload payload = new AppPayload();

        for (var entry : AppChannels.all()) {
            State state = entry.getValue().toState(payload);
            assertNull(state, entry.getKey());
        }
    }

    @Test
    void controlChannelsAreNotPayloadFields() {
        assertNull(AppChannels.get("active"));
        assertNull(AppChannels.get("reset"));
        assertNull(AppChannels.get("enabled"));
    }
}
