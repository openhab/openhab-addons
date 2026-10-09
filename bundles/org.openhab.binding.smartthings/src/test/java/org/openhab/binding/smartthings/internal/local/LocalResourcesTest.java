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
package org.openhab.binding.smartthings.internal.local;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.local.LocalResources.Point;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests for the local OCF representation and command model without a network connection.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class LocalResourcesTest {
    private final LocalResources resources = new LocalResources();

    @Test
    void readsEveryBatchEntryIncludingIndexZero() throws IOException {
        resources.update("/device/0", json("""
                [
                    {"href":"/power/0","rep":{"value":true}},
                    {"href":"/temperature/current/0","rep":{"temperature":21.5,"units":"C"}}
                ]
                """));
        assertEquals(OnOffType.ON, resources.state(point("/power/0", "value")));
        assertEquals(new QuantityType<>(21.5, SIUnits.CELSIUS),
                resources.state(point("/temperature/current/0", "temperature")));
    }

    @Test
    void readsCollectionMetadataAndLinkedRepresentations() throws IOException {
        resources.update("/device/0", json("""
                [
                    {"rt":["oic.wk.col"],"links":[{"href":"/power/0"}]},
                    {"href":"/power/0","rep":{"value":false}},
                    {"href":"/device/1","rep":{"links":[
                        {"href":"/new/resource","rep":{"status":"Ready"}}
                    ]}}
                ]
                """));
        assertEquals(OnOffType.OFF, resources.state(point("/power/0", "value")));
        assertEquals("{\"status\":\"Ready\"}", resources.state(point("/new/resource", "")).toString());
    }

    @Test
    void distinguishesDiscoveredStubsFromConfirmedEmptyResources() throws IOException {
        resources.update("/oic/res", json("""
                {"links":[{"href":"/power/0","rt":["oic.r.switch.binary"]},
                    {"href":"/unknown/0"}]}
                """));
        assertTrue(resources.points().stream().noneMatch(p -> p.href().equals("/power/0")));
        assertTrue(resources.points().stream().noneMatch(p -> p.href().equals("/unknown/0")));
        resources.update("/device/0", json("""
                [{"href":"/power/0","rep":{"href":"/power/0"}},
                 {"href":"/unknown/0","rep":{}}]
                """));
        assertTrue(resources.points().stream().noneMatch(p -> p.href().equals("/power/0")));
        assertEquals(new StringType("{}"), resources.state(point("/unknown/0", "")));
        resources.update("/power/0", json("{\"value\":true}"));
        resources.update("/power/0", json("{\"href\":\"/power/0\"}"));
        assertEquals(OnOffType.ON, resources.state(point("/power/0", "value")));
    }

    @Test
    void mergesPartialUpdatesAndProtectsCacheFromMutations() throws IOException {
        JsonElement initial = json("""
                {"temperature":20,"units":"C","range":[16,30],"increment":0.5,
                 "extra":{"one":1,"two":2}}
                """);
        resources.update("/temperature/desired/0", initial);
        initial.getAsJsonObject().addProperty("temperature", 99);
        resources.update("/temperature/desired/0", json("{\"temperature\":21,\"extra\":{\"one\":3}}"));
        Map<String, JsonObject> snapshot = resources.snapshot();
        JsonObject rep = snapshot.get("/temperature/desired/0");
        assertEquals(21, rep.get("temperature").getAsInt());
        assertEquals("C", rep.get("units").getAsString());
        assertEquals(2, rep.getAsJsonObject("extra").get("two").getAsInt());
        rep.getAsJsonArray("range").set(0, json("100"));
        rep.getAsJsonObject("extra").addProperty("two", 99);
        snapshot.clear();
        assertEquals(16, resources.snapshot().get("/temperature/desired/0").getAsJsonArray("range").get(0).getAsInt());
        assertTrue(point("/temperature/desired/0", "temperature").writable());
    }

    @Test
    void rejectsUnsafePathsAndMalformedEnvelopesAtomically() throws IOException {
        for (String path : List.of("power/0", "/", "/../power/0", "/a/./power/0", "/a//power/0", "/power/0?q=x",
                "/power/0#fragment", "https://example.org/power/0", "/%2e%2e/power/0", "/a\\power/0")) {
            assertThrows(IOException.class, () -> resources.update(path, json("{}")), path);
        }
        for (String payload : List.of("null", "true", "12", "[true]", "[{\"value\":true}]", "{\"links\":{}}",
                "{\"links\":[null]}", "{\"links\":[{\"href\":true}]}", "[{\"href\":\"/power/0\",\"rep\":[]}]",
                "[{\"href\":\"/power/0\",\"rep\":{\"href\":\"/power/1\"}}]")) {
            assertThrows(IOException.class, () -> resources.update("/device/0", json(payload)), payload);
        }
        resources.update("/power/0", json("{\"value\":false}"));
        assertThrows(IOException.class, () -> resources.update("/device/0", json("""
                [{"href":"/power/0","rep":{"value":true}},
                 {"href":"/../invalid","rep":{}}]
                """)));
        assertEquals(OnOffType.OFF, resources.state(point("/power/0", "value")));
    }

    @Test
    void booleanFieldsRequireActualBooleansAndUnknownIsNotOff() throws IOException {
        resources.update("/power/0", json("{\"value\":false}"));
        assertEquals(OnOffType.OFF, resources.state(point("/power/0", "value")));
        for (String invalid : List.of("\"false\"", "0", "null", "{}", "[]")) {
            resources.update("/power/0", json("{\"value\":" + invalid + "}"));
            Point point = point("/power/0", "value");
            assertEquals(UnDefType.UNDEF, resources.state(point));
            assertFalse(point.writable());
        }
    }

    @Test
    void powerCommandsRequireExplicitRemoteControlAndDoNotUpdateState() throws IOException {
        resources.update("/power/0", json("{\"value\":false}"));
        Point power = point("/power/0", "value");
        assertFalse(resources.remoteControlEnabled());
        assertThrows(IllegalArgumentException.class, () -> resources.command(power, OnOffType.ON));
        resources.update("/remotectrl/0", json("{\"value\":true}"));
        assertTrue(resources.remoteControlEnabled());
        assertEquals(json("{\"value\":true}"), resources.command(power, OnOffType.ON));
        assertEquals(OnOffType.OFF, resources.state(power));
        assertThrows(IllegalArgumentException.class, () -> resources.command(power, new StringType("ON")));
        assertThrows(IllegalArgumentException.class, () -> resources.command(power, new DecimalType(1)));
        assertThrows(IllegalArgumentException.class,
                () -> resources.command(point("/remotectrl/0", "value"), OnOffType.OFF));
        resources.update("/remotectrl/0", json("{\"value\":false}"));
        assertFalse(resources.remoteControlEnabled());
        assertThrows(IllegalArgumentException.class, () -> resources.command(power, OnOffType.ON));
        resources.update("/remotectrl/0", json("{\"value\":null}"));
        assertFalse(resources.remoteControlEnabled());
        assertThrows(IllegalArgumentException.class, () -> resources.command(power, OnOffType.ON));
    }

    @Test
    void usesVendorPowerUntilStandardResourceIsUsable() throws IOException {
        resources.update("/power/0", json("{\"href\":\"/power/0\"}"));
        resources.update("/power/vs/0", json("{\"x.com.samsung.da.power\":\"On\"}"));
        resources.update("/remotectrl/vs/0", json("{\"x.com.samsung.da.remoteControlEnabled\":\"true\"}"));
        Point vendor = point("/power/vs/0", "x.com.samsung.da.power");
        assertEquals(OnOffType.ON, resources.state(vendor));
        assertEquals(json("{\"x.com.samsung.da.power\":\"Off\"}"), resources.command(vendor, OnOffType.OFF));
        resources.update("/power/0", json("{\"value\":\"unavailable\"}"));
        assertEquals(OnOffType.ON, resources.state(vendor));
        assertFalse(point("/power/0", "value").writable());
        resources.update("/power/0", json("{\"value\":false}"));
        assertEquals(OnOffType.OFF, resources.state(point("/power/0", "value")));
        assertTrue(resources.points().stream().noneMatch(p -> p.field().equals("x.com.samsung.da.power")));
        assertThrows(IllegalArgumentException.class, () -> resources.command(vendor, OnOffType.ON));
    }

    @Test
    void standardRemoteControlTakesPrecedenceWhenConfirmed() throws IOException {
        resources.update("/remotectrl/vs/0", json("{\"x.com.samsung.da.remoteControlEnabled\":\"true\"}"));
        resources.update("/remotectrl/0", json("{\"href\":\"/remotectrl/0\"}"));
        assertTrue(resources.remoteControlEnabled());
        resources.update("/remotectrl/0", json("{\"value\":false}"));
        assertFalse(resources.remoteControlEnabled());
        assertTrue(
                resources.points().stream().noneMatch(p -> p.field().equals("x.com.samsung.da.remoteControlEnabled")));
        resources.update("/remotectrl/0", json("{\"value\":{}}"));
        assertTrue(resources.remoteControlEnabled());
        for (String invalid : List.of("\"unknown\"", "true", "null", "1", "{}", "[]")) {
            resources.update("/remotectrl/vs/0", json("{\"x.com.samsung.da.remoteControlEnabled\":" + invalid + "}"));
            assertFalse(resources.remoteControlEnabled());
        }
    }

    @Test
    void respectsPowerCapabilityChangesAndRejectsStalePoints() throws IOException {
        resources.update("/power/0", json("{\"value\":true}"));
        resources.update("/remotectrl/0", json("{\"value\":true}"));
        Point previous = point("/power/0", "value");
        resources.update("/wm/setinfo/vs/0", json("{\"x.com.samsung.da.isModelSettingPowerOnOff\":\"false\"}"));
        assertFalse(point("/power/0", "value").writable());
        assertThrows(IllegalArgumentException.class, () -> resources.command(previous, OnOffType.OFF));
        assertEquals(OnOffType.ON, resources.state(previous));
        resources.update("/wm/setinfo/vs/0", json("{\"x.com.samsung.da.isModelSettingPowerOnOff\":\"true\"}"));
        assertTrue(point("/power/0", "value").writable());
        assertEquals(json("{\"value\":false}"), resources.command(previous, OnOffType.OFF));
    }

    @Test
    void temperaturesPreserveUnitsAndValidateConvertedCommands() throws IOException {
        resources.update("/temperature/current/0", json("{\"temperature\":68,\"units\":\"F\"}"));
        Point measured = point("/temperature/current/0", "temperature");
        assertFalse(measured.writable());
        assertEquals(new QuantityType<>(68, ImperialUnits.FAHRENHEIT), resources.state(measured));
        resources.update("/temperature/desired/0", json("""
                {"temperature":20,"units":"C","range":[16,30],"increment":0.5}
                """));
        resources.update("/remotectrl/0", json("{\"value\":true}"));
        Point desired = point("/temperature/desired/0", "temperature");
        assertTrue(desired.writable());
        assertEquals(json("{\"temperature\":20}"),
                resources.command(desired, new QuantityType<>(68, ImperialUnits.FAHRENHEIT)));
        assertEquals(json("{\"temperature\":16}"), resources.command(desired, new DecimalType(16)));
        assertEquals(json("{\"temperature\":30}"), resources.command(desired, new DecimalType(30)));
        assertThrows(IllegalArgumentException.class, () -> resources.command(desired, new DecimalType(15.5)));
        assertThrows(IllegalArgumentException.class, () -> resources.command(desired, new DecimalType(30.5)));
        assertThrows(IllegalArgumentException.class, () -> resources.command(desired, new DecimalType(20.1)));
        assertThrows(IllegalArgumentException.class,
                () -> resources.command(desired, new QuantityType<>(70, ImperialUnits.FAHRENHEIT)));
        assertEquals(json("{\"temperature\":16}"),
                resources.command(desired, new QuantityType<>(60.8, ImperialUnits.FAHRENHEIT)));
        assertEquals(json("{\"temperature\":30}"),
                resources.command(desired, new QuantityType<>(86, ImperialUnits.FAHRENHEIT)));
        assertThrows(IllegalArgumentException.class, () -> resources.command(desired, new StringType("20")));
        assertThrows(IllegalArgumentException.class,
                () -> resources.command(desired, new QuantityType<>(20, Units.WATT)));
        resources.update("/temperature/desired/0",
                json("{\"units\":\"F\",\"range\":[60,86],\"step\":1,\"increment\":1}"));
        assertEquals(json("{\"temperature\":68}"), resources.command(point("/temperature/desired/0", "temperature"),
                new QuantityType<>(20, SIUnits.CELSIUS)));
        assertThrows(IllegalArgumentException.class, () -> resources.command(measured, new DecimalType(70)));
    }

    @Test
    void temperatureWritesRequireDeviceBoundsUnitsAndIncrement() throws IOException {
        resources.update("/remotectrl/0", json("{\"value\":true}"));
        for (String payload : List.of("{\"temperature\":20,\"units\":\"C\",\"range\":[16,30]}",
                "{\"temperature\":20,\"units\":\"C\",\"increment\":1}",
                "{\"temperature\":20,\"range\":[16,30],\"increment\":1}",
                "{\"temperature\":20,\"units\":\"Kelvin\",\"range\":[16,30],\"increment\":1}",
                "{\"temperature\":20,\"units\":\"C\",\"range\":[30,16],\"increment\":1}",
                "{\"temperature\":20,\"units\":\"C\",\"range\":[16,30],\"increment\":0}",
                "{\"temperature\":20,\"units\":\"C\",\"range\":[16,30],\"increment\":-1}",
                "{\"temperature\":20,\"units\":\"C\",\"range\":[16,30],\"increment\":99}",
                "{\"temperature\":20,\"units\":\"C\",\"range\":[16,null],\"increment\":1}",
                "{\"temperature\":{},\"units\":\"C\",\"range\":[16,30],\"increment\":1}")) {
            LocalResources model = new LocalResources();
            model.update("/temperature/desired/0", json(payload));
            Point desired = model.points().getFirst();
            assertFalse(desired.writable(), payload);
            assertThrows(IllegalArgumentException.class, () -> model.command(desired, new DecimalType(20)), payload);
        }
    }

    @Test
    void readsSeparateAdvertisedTemperatureIncrement() throws IOException {
        resources.update("/temperature/desired/0", json("""
                {"temperature":20,"units":"C","range":[16,30]}
                """));
        assertFalse(point("/temperature/desired/0", "temperature").writable());
        resources.update("/temperature/control/vs/0", json("{\"x.com.samsung.da.increment\":\"0.5\"}"));
        assertTrue(point("/temperature/desired/0", "temperature").writable());
        resources.update("/temperature/control/vs/0", json("{\"units\":\"F\"}"));
        assertFalse(point("/temperature/desired/0", "temperature").writable());
        resources.update("/temperature/control/vs/0", json("{\"units\":\"C\"}"));
        assertTrue(point("/temperature/desired/0", "temperature").writable());
    }

    @Test
    void malformedTemperatureValuesBecomeUndefined() throws IOException {
        for (String value : List.of("null", "\"NaN\"", "\"Infinity\"", "true", "[]", "{}", "1e2147483647",
                "1e-2147483647", "\"" + "9".repeat(300) + "\"")) {
            resources.update("/temperature/current/0", json("{\"temperature\":" + value + ",\"units\":\"C\"}"));
            assertEquals(UnDefType.UNDEF, resources.state(point("/temperature/current/0", "temperature")), value);
        }
        resources.update("/temperature/current/0", json("{\"temperature\":20,\"units\":null}"));
        assertEquals(UnDefType.UNDEF, resources.state(point("/temperature/current/0", "temperature")));
    }

    @Test
    void knownMeasurementsDoNotGuessCounterUnitsOrCoerceNegativePower() throws IOException {
        resources.update("/energy/consumption/vs/0", json("""
                {"x.com.samsung.da.instantaneousPower":"-500","x.com.samsung.da.cumulativePower":"1234"}
                """));
        Point watts = point("/energy/consumption/vs/0", "x.com.samsung.da.instantaneousPower");
        Point cumulative = point("/energy/consumption/vs/0", "x.com.samsung.da.cumulativePower");
        assertEquals(UnDefType.UNDEF, resources.state(watts));
        assertEquals(new DecimalType(1234), resources.state(cumulative));
        assertEquals("Number", cumulative.itemType());
        assertFalse(cumulative.writable());
        resources.update("/energy/consumption/vs/0", json("{\"x.com.samsung.da.instantaneousPower\":93}"));
        assertEquals(new QuantityType<>(93, Units.WATT), resources.state(watts));
        assertEquals(new DecimalType(1234), resources.state(cumulative));
        resources.update("/water/consumption/vs/0", json("{\"x.com.samsung.da.cumulativeWater\":\"45\"}"));
        assertEquals(new DecimalType(45),
                resources.state(point("/water/consumption/vs/0", "x.com.samsung.da.cumulativeWater")));
        resources.update("/kidslock/vs/0", json("{\"x.com.samsung.da.kidsLock\":\"Run\"}"));
        assertEquals(new StringType("Run"), resources.state(point("/kidslock/vs/0", "x.com.samsung.da.kidsLock")));
    }

    @Test
    void airflowWritesOnlyConfirmedPurifierSpeedAndMinimalPayload() throws IOException {
        resources.update("/airflow/0", json("{\"speed\":1,\"direction\":\"Off\"}"));
        resources.update("/remotectrl/0", json("{\"value\":true}"));
        assertFalse(point("/airflow/0", "speed").writable());
        resources.update("/oic/d", json("{\"rt\":[\"oic.wk.d\",\"oic.d.airpurifier\"]}"));
        Point speed = point("/airflow/0", "speed");
        assertTrue(speed.writable());
        assertEquals(json("{\"speed\":2}"), resources.command(speed, new DecimalType(2)));
        assertEquals(json("{\"speed\":0}"), resources.command(speed, new DecimalType(0)));
        assertEquals(json("{\"speed\":4}"), resources.command(speed, new DecimalType(4)));
        for (double invalid : List.of(-1.0, 4.5, 5.0, 2.2)) {
            assertThrows(IllegalArgumentException.class, () -> resources.command(speed, new DecimalType(invalid)));
        }
        assertThrows(IllegalArgumentException.class, () -> resources.command(speed, new StringType("2")));
        assertThrows(IllegalArgumentException.class, () -> resources.command(speed, new QuantityType<>(2, Units.WATT)));
        resources.update("/oic/d", json("{\"rt\":[\"oic.d.oven\"]}"));
        assertFalse(point("/airflow/0", "speed").writable());
        assertThrows(IllegalArgumentException.class, () -> resources.command(speed, new DecimalType(2)));
    }

    @Test
    void operatingModesNeedConfirmedModelAndAdvertisedEnum() throws IOException {
        resources.update("/mode/vs/0", json("""
                {"x.com.samsung.da.modes":["Auto"],"x.com.samsung.da.supportedModes":["Auto","Sleep"]}
                """));
        resources.update("/remotectrl/0", json("{\"value\":true}"));
        assertFalse(point("/mode/vs/0", "x.com.samsung.da.modes").writable());
        resources.update("/oic/d", json("{\"rt\":[\"oic.d.dehumidifier\"]}"));
        Point mode = point("/mode/vs/0", "x.com.samsung.da.modes");
        assertEquals(new StringType("Auto"), resources.state(mode));
        assertEquals(json("{\"x.com.samsung.da.modes\":[\"Sleep\"]}"),
                resources.command(mode, new StringType("Sleep")));
        assertThrows(IllegalArgumentException.class, () -> resources.command(mode, new StringType("Start")));
        assertThrows(IllegalArgumentException.class, () -> resources.command(mode, OnOffType.ON));
        resources.update("/mode/vs/0", json("{\"x.com.samsung.da.supportedModes\":[\"Auto\",{}]}"));
        assertFalse(point("/mode/vs/0", "x.com.samsung.da.modes").writable());
        resources.update("/mode/vs/0", json("{\"x.com.samsung.da.modes\":[]}"));
        assertEquals(UnDefType.UNDEF, resources.state(point("/mode/vs/0", "x.com.samsung.da.modes")));
    }

    @Test
    void unknownOperationalResourcesAreReadOnlyJson() throws IOException {
        resources.update("/operational/state/vs/0", json("""
                {"x.com.samsung.da.operatingState":"Ready","supportedStates":["Run","Stop"]}
                """));
        resources.update("/remotectrl/0", json("{\"value\":true}"));
        Point diagnostic = point("/operational/state/vs/0", "");
        assertFalse(diagnostic.writable());
        assertEquals("String", diagnostic.itemType());
        assertEquals(resources.snapshot().get(diagnostic.href()), json(resources.state(diagnostic).toString()));
        assertThrows(IllegalArgumentException.class, () -> resources.command(diagnostic, new StringType("Run")));
        Point invented = new Point(diagnostic.id(), diagnostic.href(), diagnostic.field(), diagnostic.itemType(),
                diagnostic.label(), true);
        assertThrows(IllegalArgumentException.class, () -> resources.command(invented, new StringType("Run")));
    }

    @Test
    void securityResourcesNeverBecomeDiagnosticChannels() throws IOException {
        resources.update("/oic/sec/cred", json("{\"creds\":[{\"privatedata\":{\"data\":\"secret\"}}]}"));
        resources.update("/device/1/oic/sec/cred", json("{\"creds\":[]}"));
        assertTrue(resources.points().isEmpty());
    }

    @Test
    void compositePathsKeepTheirIdentityAndUseTheirOwnRemoteControl() throws IOException {
        resources.update("/units/1/power/0", json("{\"value\":false}"));
        resources.update("/units/2/power/vs/0", json("{\"x.com.samsung.da.power\":\"Off\"}"));
        resources.update("/units/1/remotectrl/0", json("{\"value\":true}"));
        resources.update("/units/2/remotectrl/0", json("{\"value\":false}"));
        Point first = point("/units/1/power/0", "value");
        Point second = point("/units/2/power/vs/0", "x.com.samsung.da.power");
        assertNotEquals(first.id(), second.id());
        assertEquals(json("{\"value\":true}"), resources.command(first, OnOffType.ON));
        assertThrows(IllegalArgumentException.class, () -> resources.command(second, OnOffType.ON));
        assertFalse(resources.remoteControlEnabled());
        resources.update("/units/2/remotectrl/0", json("{\"value\":true}"));
        assertTrue(resources.remoteControlEnabled());
        assertEquals(json("{\"x.com.samsung.da.power\":\"On\"}"), resources.command(second, OnOffType.ON));
    }

    @Test
    void readsObjectEnvelopesAndRejectsInventedStateMappings() throws IOException {
        resources.update("/device/0", json("{\"href\":\"/power/0\",\"rep\":{\"value\":true}}"));
        Point power = point("/power/0", "value");
        assertEquals(OnOffType.ON, resources.state(power));
        Point invented = new Point(power.id(), power.href(), "value", "String", power.label(), false);
        assertEquals(UnDefType.UNDEF, resources.state(invented));
        Point unknown = new Point("unknown", power.href(), power.field(), power.itemType(), power.label(), true);
        assertEquals(UnDefType.UNDEF, resources.state(unknown));
        assertThrows(IllegalArgumentException.class, () -> resources.command(unknown, OnOffType.OFF));
    }

    @Test
    void channelIdsRemainUniqueAndIndependentOfArrivalOrder() throws IOException {
        resources.update("/a/b", json("{\"value\":1}"));
        String firstId = point("/a/b", "").id();
        resources.update("/a-b", json("{\"value\":2}"));
        String secondId = point("/a-b", "").id();
        assertEquals(firstId, point("/a/b", "").id());
        assertNotEquals(firstId, secondId);
        assertTrue(firstId.matches("[A-Za-z0-9_-]+"));
        LocalResources reverse = new LocalResources();
        reverse.update("/a-b", json("{\"value\":2}"));
        reverse.update("/a/b", json("{\"value\":1}"));
        assertEquals(resources.points(), reverse.points());
    }

    @Test
    void airConditionerAllowsItsOptionalRemoteGateToBeAbsent() throws IOException {
        resources.update("/oic/d", json("{\"rt\":[\"oic.d.airconditioner\"]}"));
        resources.update("/power/vs/0", json("{\"x.com.samsung.da.power\":\"Off\"}"));
        resources.update("/mode/vs/0",
                json("""
                        {"x.com.samsung.da.modes":["Heat"],"x.com.samsung.da.supportedModes":["Cool","Dry","Wind","Auto","Heat"],
                         "x.com.samsung.da.options":[{"x.com.samsung.da.unknown":"preserve"}]}
                        """));
        assertTrue(resources.remoteControlEnabled());
        assertEquals(json("{\"x.com.samsung.da.power\":\"On\"}"),
                resources.command(point("/power/vs/0", "x.com.samsung.da.power"), OnOffType.ON));
        Point mode = point("/mode/vs/0", "x.com.samsung.da.modes");
        assertEquals(new StringType("Heat"), resources.state(mode));
        assertEquals(json("{\"x.com.samsung.da.modes\":[\"Cool\"]}"), resources.command(mode, new StringType("Cool")));
        assertThrows(IllegalArgumentException.class, () -> resources.command(mode, new StringType("Start")));
        assertEquals(new StringType("Heat"), resources.state(mode));
    }

    @Test
    void airConditionerRejectsPresentUnknownAndDisabledRemoteGates() throws IOException {
        resources.update("/oic/d", json("{\"rt\":[\"oic.d.airconditioner\"]}"));
        resources.update("/power/vs/0", json("{\"x.com.samsung.da.power\":\"Off\"}"));
        Point power = point("/power/vs/0", "x.com.samsung.da.power");
        for (String gate : List.of("{\"href\":\"/remotectrl/0\"}", "{\"value\":false}", "{\"value\":null}")) {
            resources.update("/remotectrl/0", json(gate));
            assertFalse(resources.remoteControlEnabled());
            assertThrows(IllegalArgumentException.class, () -> resources.command(power, OnOffType.ON));
        }
        resources.update("/remotectrl/0", json("{\"value\":true}"));
        assertEquals(json("{\"x.com.samsung.da.power\":\"On\"}"), resources.command(power, OnOffType.ON));
    }

    @Test
    void airConditionerFanSwingAndWindFreePreserveScalarWireFormat() throws IOException {
        resources.update("/oic/d", json("{\"rt\":[\"oic.d.airconditioner\"]}"));
        for (String path : List.of("/wind/strength/vs/0", "/wind/direction/vs/0", "/mode/convenient/vs/0")) {
            String payload = switch (path) {
                case "/wind/strength/vs/0" -> """
                        {"x.com.samsung.da.modes":"0","x.com.samsung.da.supportedModes":["0","1","2","3","4"],
                         "x.com.samsung.da.modesName":["Auto","Low","Mid","High","Turbo"]}
                        """;
                case "/wind/direction/vs/0" ->
                    """
                            {"x.com.samsung.da.modes":"Fix","x.com.samsung.da.supportedModes":["Fix","All","Up_And_Low","Left_And_Right"]}
                            """;
                default ->
                    """
                            {"x.com.samsung.da.modes":"Off","x.com.samsung.da.supportedModes":["Off","Sleep","Speed","Nano","NanoSleep"]}
                            """;
            };
            resources.update(path, json(payload));
            Point mode = point(path, "x.com.samsung.da.modes");
            String requested = path.contains("strength") ? "3" : path.contains("direction") ? "All" : "Nano";
            assertTrue(mode.writable());
            assertEquals(json("{\"x.com.samsung.da.modes\":\"" + requested + "\"}"),
                    resources.command(mode, new StringType(requested)));
            assertThrows(IllegalArgumentException.class, () -> resources.command(mode, new StringType("unsupported")));
            assertThrows(IllegalArgumentException.class, () -> resources.command(mode, new DecimalType(3)));
        }
    }

    @Test
    void unsupportedAndCompositeModesAreReadOnly() throws IOException {
        resources.update("/wind/strength/vs/0", json("""
                {"x.com.samsung.da.modes":"0","x.com.samsung.da.supportedModes":["0","1"]}
                """));
        assertFalse(point("/wind/strength/vs/0", "x.com.samsung.da.modes").writable());
        resources.update("/oic/d", json("{\"rt\":[\"oic.d.airconditioner\"]}"));
        resources.update("/mode/vs/0", json("""
                {"x.com.samsung.da.modes":["Heat","Dry"],"x.com.samsung.da.supportedModes":["Heat","Dry"]}
                """));
        Point mode = point("/mode/vs/0", "x.com.samsung.da.modes");
        assertFalse(mode.writable());
        assertEquals(UnDefType.UNDEF, resources.state(mode));
        assertThrows(IllegalArgumentException.class, () -> resources.command(mode, new StringType("Heat")));
    }

    @Test
    void vendorTemperatureFallbackUsesMinimalBoundedUpdates() throws IOException {
        vendorTemperatures();
        Point desired = point("/temperatures/vs/0", "x.com.samsung.da.desired");
        assertEquals(new QuantityType<>("22 °C"), resources.state(desired));
        assertEquals(new QuantityType<>("21 °C"),
                resources.state(point("/temperatures/vs/0", "x.com.samsung.da.current")));
        assertEquals(json("""
                {"x.com.samsung.da.items":[{"x.com.samsung.da.id":"0","x.com.samsung.da.desired":"23"}]}
                """), resources.command(desired, new DecimalType(23)));
        assertThrows(IllegalArgumentException.class, () -> resources.command(desired, new DecimalType("22.5")));
        assertThrows(IllegalArgumentException.class, () -> resources.command(desired, new DecimalType(31)));
        assertEquals(new QuantityType<>("22 °C"), resources.state(desired));
    }

    @Test
    void standardTemperaturesTakePrecedenceAndUseVendorIncrementFallback() throws IOException {
        vendorTemperatures();
        resources.update("/temperature/current/0", json("{\"temperature\":21.5,\"units\":\"C\"}"));
        resources.update("/temperature/desired/0", json("{\"temperature\":22,\"units\":\"C\",\"range\":[16,30]}"));
        assertTrue(resources.points().stream()
                .noneMatch(p -> p.href().equals("/temperatures/vs/0") && !p.field().isEmpty()));
        assertEquals(json("{\"temperature\":23}"),
                resources.command(point("/temperature/desired/0", "temperature"), new DecimalType(23)));
        resources.update("/temperature/control/vs/0", json("{\"x.com.samsung.da.increment\":\"unknown\"}"));
        assertFalse(point("/temperature/desired/0", "temperature").writable());
    }

    @Test
    void vendorTemperatureUnknownIdsAndMultipleItemsRemainDiagnostics() throws IOException {
        vendorTemperatures();
        resources.update("/temperatures/vs/0", json("""
                {"x.com.samsung.da.items":[{"x.com.samsung.da.id":"unknown","x.com.samsung.da.desired":"20"}]}
                """));
        assertFalse(point("/temperatures/vs/0", "").writable());
        resources.update("/temperatures/vs/0", json("""
                {"x.com.samsung.da.items":[{"x.com.samsung.da.id":"0"},{"x.com.samsung.da.id":"1"}]}
                """));
        assertFalse(point("/temperatures/vs/0", "").writable());
    }

    @Test
    void excludesSecurityResourcesAndRedactsNestedDiagnosticCredentials() throws IOException {
        resources.update("/sec/cred", json("{\"secret\":\"must-not-publish\"}"));
        resources.update("/oic/sec/doxm", json("{\"ownerId\":\"must-not-publish\"}"));
        resources.update("/diagnostic/vs/0",
                json("""
                        {"normal":"visible","nested":{"access_token":"must-not-publish","password":"must-not-publish","value":1},
                         "links":[{"href":"/sec/cred","rep":{"credential":"must-not-publish"}}]}
                        """));
        assertFalse(resources.snapshot().containsKey("/sec/cred"));
        assertFalse(resources.snapshot().containsKey("/oic/sec/doxm"));
        String diagnostic = resources.state(point("/diagnostic/vs/0", "")).toString();
        assertFalse(diagnostic.contains("must-not-publish"));
        assertTrue(diagnostic.contains("visible"));
        assertTrue(diagnostic.contains("value"));
    }

    private void vendorTemperatures() throws IOException {
        resources.update("/oic/d", json("{\"rt\":[\"oic.d.airconditioner\"]}"));
        resources.update("/temperatures/vs/0",
                json("""
                        {"x.com.samsung.da.items":[{"x.com.samsung.da.id":"0","x.com.samsung.da.description":"Temperature",
                          "x.com.samsung.da.desired":"22.0","x.com.samsung.da.current":"21.0","x.com.samsung.da.maximum":"30",
                          "x.com.samsung.da.minimum":"16","x.com.samsung.da.increment":"1.0","x.com.samsung.da.unit":"Celsius"}]}
                        """));
    }

    private Point point(String href, String field) {
        return resources.points().stream().filter(p -> p.href().equals(href) && p.field().equals(field)).findFirst()
                .orElseThrow(() -> new AssertionError("Missing point " + href + " " + field));
    }

    private static JsonElement json(String text) {
        return JsonParser.parseString(text);
    }
}
