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
package org.openhab.binding.philipsair.internal.connection;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests the translation between the field naming schemes of CoAP devices in {@link CoapKeyScheme}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CoapKeySchemeTest {

    private static final String CLASSIC_STATUS = """
            {"name":"Living Room","modelid":"AC2889/10","swversion":"1.0.7","pwr":"1","cl":false,"pm25":6,
            "iaql":1,"fltsts0":287,"fltsts1":4751,"fltsts2":2111}""";
    private static final String GEN2_STATUS = """
            {"D01-03":"Bedroom","D01-05":"AC0850/11","D01-21":"1.2.3","D03-02":"ON","D03-12":"Auto General",
            "D03-32":3,"D03-33":12,"D05-13":340,"D05-14":4200,"DeviceId":"abc"}""";
    private static final String GEN3_STATUS = """
            {"D01S03":"Office","D01S05":"AC3737/10","D01S12":"0.2.1","D03102":0,"D03103":1,"D03120":2,
            "D03221":8,"D03125":48,"D03224":215,"D03240":0,"D0312A":1,"D0520D":100,"D0540E":3900,"D0310C":1,
            "DeviceId":"def"}""";
    // status of an AC3210/12 reported on the openHAB community forum, without the device and network details
    private static final String UNICORN_STATUS = """
            {"D01102":0,"D01S03":"Bedroom","D01S04":"Unicorn","D01S05":"AC3210/12","D01108":3,"D0110C":19,
            "D01S12":"0.2.3","D03102":1,"D03103":0,"D03105":101,"D0310A":2,"D0310C":0,"D0310D":1,"D03110":0,
            "D03211":0,"D03120":1,"D03221":1,"D03224":241,"D03125":46,"D0312A":1,"D0312C":7,"D03240":0,
            "D05207":720,"D05408":9600,"D0520D":601,"D0540E":2870}""";

    private final Gson gson = new Gson();

    private static JsonObject parse(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    public void schemeIsDetectedFromFieldNames() {
        assertEquals(CoapKeyScheme.CLASSIC, CoapKeyScheme.detect(parse(CLASSIC_STATUS)));
        assertEquals(CoapKeyScheme.GEN2, CoapKeyScheme.detect(parse(GEN2_STATUS)));
        assertEquals(CoapKeyScheme.GEN3, CoapKeyScheme.detect(parse(GEN3_STATUS)));
        assertEquals(CoapKeyScheme.UNICORN, CoapKeyScheme.detect(parse(UNICORN_STATUS)));
        assertEquals(CoapKeyScheme.UNICORN, CoapKeyScheme.detect(parse("{\"D01S04\":\"unicorn\"}")));
        assertEquals(CoapKeyScheme.GEN3, CoapKeyScheme.detect(parse("{\"D01S04\":\"Pegasus\"}")));
        assertEquals(CoapKeyScheme.CLASSIC, CoapKeyScheme.detect(new JsonObject()));
    }

    @Test
    public void classicStatusIsUnchanged() {
        JsonObject status = parse(CLASSIC_STATUS);

        assertEquals(status, CoapKeyScheme.CLASSIC.toClassic(status));
    }

    @Test
    public void gen2StatusIsTranslated() {
        JsonObject classic = CoapKeyScheme.GEN2.toClassic(parse(GEN2_STATUS));

        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals("1", data.getPower());
        assertEquals(12, data.getPm25());
        assertEquals(3, data.getAllergenLevel());
        assertNull(data.getFanSpeed());
        assertNull(data.getTemperature());

        PhilipsAirPurifierDeviceDTO device = gson.fromJson(classic, PhilipsAirPurifierDeviceDTO.class);
        assertNotNull(device);
        assertEquals("Bedroom", device.getName());
        assertEquals("AC0850/11", device.getModelId());
        assertEquals("1.2.3", device.getSoftwareVersion());

        PhilipsAirPurifierFiltersDTO filters = gson.fromJson(classic, PhilipsAirPurifierFiltersDTO.class);
        assertNotNull(filters);
        assertEquals(340, filters.getPreFilter());
        assertEquals(4200, filters.getHepaFilter());
        assertNull(filters.getCarbonFilter());
    }

    @Test
    public void gen3StatusIsTranslated() {
        JsonObject classic = CoapKeyScheme.GEN3.toClassic(parse(GEN3_STATUS));

        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals("0", data.getPower());
        assertEquals(Boolean.TRUE, data.getChildLock());
        assertEquals(8, data.getPm25());
        assertEquals(2, data.getAllergenLevel());
        assertEquals(48f, data.getHumidity());
        assertEquals(21.5f, data.getTemperature());
        assertEquals(0, data.getErrorCode());
        assertEquals("1", data.getDisplayIndex());
        assertNull(data.getMode());

        PhilipsAirPurifierDeviceDTO device = gson.fromJson(classic, PhilipsAirPurifierDeviceDTO.class);
        assertNotNull(device);
        assertEquals("Office", device.getName());
        assertEquals("AC3737/10", device.getModelId());
        assertEquals("0.2.1", device.getSoftwareVersion());

        PhilipsAirPurifierFiltersDTO filters = gson.fromJson(classic, PhilipsAirPurifierFiltersDTO.class);
        assertNotNull(filters);
        assertEquals(100, filters.getPreFilter());
        assertEquals(3900, filters.getHepaFilter());
    }

    @Test
    public void unicornStatusIsTranslated() {
        JsonObject classic = CoapKeyScheme.UNICORN.toClassic(parse(UNICORN_STATUS));

        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals("1", data.getPower());
        assertEquals(Boolean.FALSE, data.getChildLock());
        assertEquals("P", data.getMode());
        assertEquals("1", data.getFanSpeed());
        assertEquals(0, data.getTimer());
        assertEquals(0, data.getTimerLeft());
        assertEquals(7, data.getAqit());
        assertEquals(0, data.getErrorCode());
        assertEquals("1", data.getDisplayIndex());
        assertEquals(24.1f, data.getTemperature());

        PhilipsAirPurifierDeviceDTO device = gson.fromJson(classic, PhilipsAirPurifierDeviceDTO.class);
        assertNotNull(device);
        assertEquals("AC3210/12", device.getModelId());
        assertEquals("Unicorn", device.getRange());
    }

    @Test
    public void unicornModeAndSpeedAreTranslated() {
        assertModeAndSpeed("P", "3", "{\"D0310C\":0,\"D0310D\":3}");
        assertModeAndSpeed("S", "1", "{\"D0310C\":17,\"D0310D\":1}");
        assertModeAndSpeed("M", "t", "{\"D0310C\":18,\"D0310D\":18}");
        assertModeAndSpeed("M", "m", "{\"D0310C\":19,\"D0310D\":3}");
        assertModeAndSpeed("M", "4", "{\"D0310C\":4,\"D0310D\":4}");
        // the speed of the manual mode is the selected one, whatever the actual speed field says
        assertModeAndSpeed("M", "2", "{\"D0310C\":2,\"D0310D\":5}");
        assertModeAndSpeed(null, null, "{\"D0310C\":99,\"D0310D\":3}");
        assertModeAndSpeed("P", null, "{\"D0310C\":0}");
        assertModeAndSpeed(null, null, "{\"D0310C\":\"auto\",\"D0310D\":3}");
    }

    private void assertModeAndSpeed(@Nullable String mode, @Nullable String speed, String reported) {
        JsonObject classic = CoapKeyScheme.UNICORN.toClassic(parse("{\"D01S04\":\"Unicorn\"," + reported.substring(1)));

        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(mode, data.getMode(), reported);
        assertEquals(speed, data.getFanSpeed(), reported);
    }

    @Test
    public void unicornTimerIsTranslated() {
        assertTimer(0, 0, "{\"D03110\":0,\"D03211\":0}");
        assertTimer(1, 59, "{\"D03110\":2,\"D03211\":59}");
        assertTimer(2, 119, "{\"D03110\":3,\"D03211\":119}");
        assertTimer(12, 720, "{\"D03110\":13,\"D03211\":720}");
        // 1 is the timer of 30 minutes of other ranges, the Unicorn does not offer it
        assertTimer(null, null, "{\"D03110\":1}");
        assertTimer(null, null, "{\"D03110\":14,\"D03211\":\"soon\"}");
    }

    private void assertTimer(@Nullable Integer hours, @Nullable Integer minutesLeft, String reported) {
        JsonObject classic = CoapKeyScheme.UNICORN.toClassic(parse("{\"D01S04\":\"Unicorn\"," + reported.substring(1)));

        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(hours, data.getTimer(), reported);
        assertEquals(minutesLeft, data.getTimerLeft(), reported);
    }

    @Test
    public void modeSpeedAndTimerAreOnlyTranslatedForUnicorn() {
        JsonObject classic = CoapKeyScheme.GEN3.toClassic(
                parse("{\"D01S04\":\"Pegasus\",\"D0310C\":18,\"D0310D\":3,\"D03110\":3,\"D03211\":60,\"D0312C\":4}"));

        assertFalse(classic.has("mode"));
        assertFalse(classic.has("om"));
        assertFalse(classic.has("dt"));
        assertFalse(classic.has("dtrs"));
        // the threshold is read from every recent model
        assertEquals(4, classic.get("aqit").getAsInt());
        assertEquals("Pegasus", classic.get("range").getAsString());
    }

    @Test
    public void unexpectedValueTypesAreIgnored() {
        JsonObject classic = CoapKeyScheme.GEN3
                .toClassic(parse("{\"D01S05\":\"AC3737/10\",\"D03102\":\"on\",\"D03224\":\"hot\",\"D03221\":null}"));

        assertFalse(classic.has("pwr"));
        assertFalse(classic.has("temp"));
        assertFalse(classic.has("pm25"));
    }

    @Test
    public void gen3ErrorAndDisplayIndexAreOnlyTranslatedWhenReported() {
        JsonObject classic = CoapKeyScheme.GEN3.toClassic(parse("{\"D01S05\":\"AC3737/10\"}"));
        assertFalse(classic.has("err"));
        assertFalse(classic.has("ddp"));

        classic = CoapKeyScheme.GEN3
                .toClassic(parse("{\"D01S05\":\"AC3737/10\",\"D03240\":\"none\",\"D0312A\":\"pm25\"}"));
        assertFalse(classic.has("err"));
        assertFalse(classic.has("ddp"));
    }

    private JsonObject command(PhilipsAirPurifierWritableDataDTO command) {
        return (JsonObject) gson.toJsonTree(command);
    }

    @Test
    public void classicCommandIsUnchanged() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setFanSpeed("2");
        command.setMode("M");

        assertEquals(parse("{\"om\":\"2\",\"mode\":\"M\"}"), CoapKeyScheme.CLASSIC.toDevice(command(command)));
    }

    @Test
    public void powerCommandIsTranslated() {
        PhilipsAirPurifierWritableDataDTO on = new PhilipsAirPurifierWritableDataDTO();
        on.setPower("1");
        PhilipsAirPurifierWritableDataDTO off = new PhilipsAirPurifierWritableDataDTO();
        off.setPower("0");

        assertEquals(parse("{\"D03-02\":\"ON\"}"), CoapKeyScheme.GEN2.toDevice(command(on)));
        assertEquals(parse("{\"D03-02\":\"OFF\"}"), CoapKeyScheme.GEN2.toDevice(command(off)));
        assertEquals(parse("{\"D03102\":1}"), CoapKeyScheme.GEN3.toDevice(command(on)));
        assertEquals(parse("{\"D03102\":0}"), CoapKeyScheme.GEN3.toDevice(command(off)));
    }

    @Test
    public void unicornModeCommandIsTranslated() {
        assertEquals(parse("{\"D0310C\":0}"), CoapKeyScheme.UNICORN.toDevice(modeCommand("P", null)));
        assertEquals(parse("{\"D0310C\":17}"), CoapKeyScheme.UNICORN.toDevice(modeCommand("S", null)));
        // the fan speed command selects the manual mode together with the speed
        assertEquals(parse("{\"D0310C\":3}"), CoapKeyScheme.UNICORN.toDevice(modeCommand("M", "3")));
        assertEquals(parse("{\"D0310C\":5}"), CoapKeyScheme.UNICORN.toDevice(modeCommand("M", "5")));
        assertEquals(parse("{\"D0310C\":18}"), CoapKeyScheme.UNICORN.toDevice(modeCommand("M", "t")));
        assertEquals(parse("{\"D0310C\":19}"), CoapKeyScheme.UNICORN.toDevice(modeCommand("M", "m")));
        assertEquals(parse("{\"D0310C\":2}"), CoapKeyScheme.UNICORN.toDevice(modeCommand(null, "2")));
    }

    @Test
    public void unicornModeCommandsWithoutMatchingModeAreDropped() {
        // the manual mode needs a speed, the other modes do not exist on the device
        assertTrue(CoapKeyScheme.UNICORN.toDevice(modeCommand("M", null)).isEmpty());
        assertTrue(CoapKeyScheme.UNICORN.toDevice(modeCommand("A", null)).isEmpty());
        assertTrue(CoapKeyScheme.UNICORN.toDevice(modeCommand("B", "1")).isEmpty());
        assertTrue(CoapKeyScheme.UNICORN.toDevice(modeCommand("M", "0")).isEmpty());
        assertTrue(CoapKeyScheme.UNICORN.toDevice(modeCommand("M", "6")).isEmpty());
        assertTrue(CoapKeyScheme.UNICORN.toDevice(modeCommand("M", "s")).isEmpty());
        assertTrue(CoapKeyScheme.UNICORN.toDevice(modeCommand("M", "10")).isEmpty());
    }

    private JsonObject modeCommand(@Nullable String mode, @Nullable String speed) {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        if (mode != null) {
            command.setMode(mode);
        }
        if (speed != null) {
            command.setFanSpeed(speed);
        }
        return command(command);
    }

    @Test
    public void unicornTimerCommandIsTranslated() {
        assertEquals(parse("{\"D03110\":0}"), CoapKeyScheme.UNICORN.toDevice(timerCommand(0)));
        assertEquals(parse("{\"D03110\":2}"), CoapKeyScheme.UNICORN.toDevice(timerCommand(1)));
        assertEquals(parse("{\"D03110\":13}"), CoapKeyScheme.UNICORN.toDevice(timerCommand(12)));
        assertTrue(CoapKeyScheme.UNICORN.toDevice(timerCommand(13)).isEmpty());
        assertTrue(CoapKeyScheme.UNICORN.toDevice(timerCommand(-1)).isEmpty());
    }

    private JsonObject timerCommand(int hours) {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setTimer(hours);
        return command(command);
    }

    @Test
    public void unicornThresholdAndDisplayedIndexCommandsAreTranslated() {
        for (int threshold : new int[] { 1, 4, 7, 10 }) {
            PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
            command.setAqit(threshold);
            assertEquals(parse("{\"D0312C\":" + threshold + "}"), CoapKeyScheme.UNICORN.toDevice(command(command)));
        }
        PhilipsAirPurifierWritableDataDTO unknownThreshold = new PhilipsAirPurifierWritableDataDTO();
        unknownThreshold.setAqit(5);
        assertTrue(CoapKeyScheme.UNICORN.toDevice(command(unknownThreshold)).isEmpty());
        PhilipsAirPurifierWritableDataDTO textThreshold = new PhilipsAirPurifierWritableDataDTO();
        textThreshold.setAqit("7");
        assertTrue(CoapKeyScheme.UNICORN.toDevice(command(textThreshold)).isEmpty());

        for (String index : new String[] { "0", "1" }) {
            PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
            command.setDisplayIndex(index);
            assertEquals(parse("{\"D0312A\":" + index + "}"), CoapKeyScheme.UNICORN.toDevice(command(command)));
        }
        PhilipsAirPurifierWritableDataDTO gas = new PhilipsAirPurifierWritableDataDTO();
        gas.setDisplayIndex("2");
        assertTrue(CoapKeyScheme.UNICORN.toDevice(command(gas)).isEmpty());
    }

    @Test
    public void unicornCommandKeepsPowerAndChildLock() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setPower("1");
        command.setChildLock(true);

        assertEquals(parse("{\"D03102\":1,\"D03103\":1}"), CoapKeyScheme.UNICORN.toDevice(command(command)));
    }

    @Test
    public void onlyUnicornTranslatesModeTimerThresholdAndIndexCommands() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setMode("P");
        command.setFanSpeed("2");
        command.setTimer(2);
        command.setAqit(7);
        command.setDisplayIndex("1");

        assertTrue(CoapKeyScheme.GEN3.toDevice(command(command)).isEmpty());
        assertTrue(CoapKeyScheme.GEN2.toDevice(command(command)).isEmpty());
    }

    @Test
    public void childLockCommandIsTranslated() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setChildLock(true);

        assertEquals(parse("{\"D03103\":1}"), CoapKeyScheme.GEN3.toDevice(command(command)));
        assertTrue(CoapKeyScheme.GEN2.toDevice(command(command)).isEmpty());
    }

    @Test
    public void unsupportedCommandIsEmpty() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setFanSpeed("2");
        command.setMode("M");

        assertTrue(CoapKeyScheme.GEN2.toDevice(command(command)).isEmpty());
        assertTrue(CoapKeyScheme.GEN3.toDevice(command(command)).isEmpty());
    }
}
