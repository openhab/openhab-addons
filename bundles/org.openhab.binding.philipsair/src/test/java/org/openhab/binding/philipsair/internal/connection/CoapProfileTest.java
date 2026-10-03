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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;
import org.openhab.core.types.StateOption;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests the translation between the field naming schemes of CoAP devices in {@link CoapProfile}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CoapProfileTest {

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
        assertEquals(CoapProfile.CLASSIC, CoapProfile.resolve("auto", parse(CLASSIC_STATUS)));
        assertEquals(CoapProfile.AC0850, CoapProfile.resolve("auto", parse(GEN2_STATUS)));
        assertEquals(CoapProfile.AC3737, CoapProfile.resolve("auto", parse(GEN3_STATUS)));
        assertEquals(CoapProfile.UNICORN, CoapProfile.resolve("auto", parse(UNICORN_STATUS)));
        assertEquals(CoapProfile.UNICORN, CoapProfile.resolve("auto", parse("{\"D01S04\":\"unicorn\"}")));
        assertEquals(CoapProfile.BASIC_GEN3, CoapProfile.resolve("auto", parse("{\"D01S04\":\"Pegasus\"}")));
        assertEquals(CoapProfile.CLASSIC, CoapProfile.resolve("auto", new JsonObject()));
    }

    @Test
    public void unicornIsDetectedFromTheModel() {
        for (String model : new String[] { "AC2210/10", "AC2220/10", "AC2221/10", "AC3210/12", "AC3220/10", "AC3221/10",
                "AC4220/12", "ac4221/11" }) {
            assertEquals(CoapProfile.UNICORN, CoapProfile.resolve("auto", parse("{\"D01S05\":\"" + model + "\"}")),
                    model);
        }
        assertEquals(CoapProfile.BASIC_GEN3, CoapProfile.resolve("auto", parse("{\"D01S05\":\"AC4228/10\"}")));
    }

    @Test
    public void otherModelProfilesAreDetected() {
        assertEquals(CoapProfile.AC3737, CoapProfile.resolve("auto", parse("{\"D01S05\":\"AC3737/10\"}")));
        assertEquals(CoapProfile.AC3737, CoapProfile.resolve("auto", parse("{\"D01S04\":\"Carnation\"}")));
        assertEquals(CoapProfile.AC1715, CoapProfile.resolve("auto", parse("{\"D01-05\":\"AC1715/10\"}")));
        assertEquals(CoapProfile.AC0850, CoapProfile.resolve("auto", parse("{\"D01-05\":\"AC0850/11\"}")));
        assertEquals(CoapProfile.BASIC_GEN2, CoapProfile.resolve("auto", parse("{\"D01-05\":\"AC9999/10\"}")));
        assertEquals(CoapProfile.BASIC_GEN2, CoapProfile.resolve("auto", parse("{\"D01-03\":\"Room\"}")));
    }

    @Test
    public void configuredProfileOverridesTheDetection() {
        JsonObject pegasus = parse("{\"D01S04\":\"Pegasus\",\"D01S05\":\"AMF765/10\"}");

        assertEquals(CoapProfile.BASIC_GEN3, CoapProfile.resolve("auto", pegasus));
        assertEquals(CoapProfile.BASIC_GEN3, CoapProfile.resolve(null, pegasus));
        assertEquals(CoapProfile.UNICORN, CoapProfile.resolve("unicorn", pegasus));
        assertEquals(CoapProfile.UNICORN, CoapProfile.resolve("UNICORN", pegasus));
        assertEquals(CoapProfile.BASIC_GEN3, CoapProfile.resolve("basic", parse(UNICORN_STATUS)));
    }

    @Test
    public void unknownOrUnfittingConfiguredProfileIsIgnored() {
        assertEquals(CoapProfile.UNICORN, CoapProfile.resolve("toaster", parse(UNICORN_STATUS)));
        // the Unicorn fields do not exist on a device with the other generation of field names
        assertEquals(CoapProfile.AC0850, CoapProfile.resolve("unicorn", parse(GEN2_STATUS)));
        assertEquals(CoapProfile.BASIC_GEN2, CoapProfile.resolve("unicorn", parse("{\"D01-03\":\"Room\"}")));
        assertEquals(CoapProfile.BASIC_GEN3, CoapProfile.resolve("ac1715", parse("{\"D01S03\":\"Room\"}")));
        assertEquals(CoapProfile.CLASSIC, CoapProfile.resolve("unicorn", parse(CLASSIC_STATUS)));
        assertEquals(CoapProfile.CLASSIC, CoapProfile.resolve("basic", parse(CLASSIC_STATUS)));
    }

    @Test
    public void profileOptionsDependOnTheModel() {
        assertEquals(List.of("1", "2", "3", "4", "5", "m", "t"), values(CoapProfile.UNICORN.getFanSpeedOptions()));
        assertEquals(List.of("P", "S"), values(CoapProfile.UNICORN.getModeOptions()));
        assertEquals(13, CoapProfile.UNICORN.getTimerOptions().size());
        assertEquals("0", CoapProfile.UNICORN.getTimerOptions().get(0).getValue());
        assertEquals("12", CoapProfile.UNICORN.getTimerOptions().get(12).getValue());

        assertEquals(List.of("1", "2", "t"), values(CoapProfile.AC3737.getFanSpeedOptions()));
        assertEquals(List.of("1", "2", "t"), values(CoapProfile.AC1715.getFanSpeedOptions()));
        assertEquals(List.of("t"), values(CoapProfile.AC0850.getFanSpeedOptions()));
        for (CoapProfile profile : new CoapProfile[] { CoapProfile.AC3737, CoapProfile.AC1715, CoapProfile.AC0850 }) {
            assertEquals(List.of("P", "S"), values(profile.getModeOptions()), profile.name());
            assertTrue(profile.getTimerOptions().isEmpty(), profile.name());
        }

        for (CoapProfile profile : new CoapProfile[] { CoapProfile.CLASSIC, CoapProfile.BASIC_GEN2,
                CoapProfile.BASIC_GEN3 }) {
            assertTrue(profile.getFanSpeedOptions().isEmpty(), profile.name());
            assertTrue(profile.getModeOptions().isEmpty(), profile.name());
            assertTrue(profile.getTimerOptions().isEmpty(), profile.name());
        }
    }

    @Test
    public void ac3737ModeIsReadFromTheModeField() {
        assertMode(CoapProfile.AC3737, "{\"D0310A\":2,\"D0310C\":0}", "P", null);
        assertMode(CoapProfile.AC3737, "{\"D0310A\":2,\"D0310C\":17}", "S", null);
        assertMode(CoapProfile.AC3737, "{\"D0310A\":2,\"D0310C\":1}", "M", "1");
        assertMode(CoapProfile.AC3737, "{\"D0310A\":2,\"D0310C\":2}", "M", "2");
        assertMode(CoapProfile.AC3737, "{\"D0310A\":3,\"D0310C\":18}", "M", "t");
        // the function field also tells that the device humidifies, which does not change the mode
        assertMode(CoapProfile.AC3737, "{\"D0310A\":4,\"D0310C\":2}", "M", "2");
        assertMode(CoapProfile.AC3737, "{\"D0310A\":2,\"D0310C\":19}", null, null);
    }

    @Test
    public void ac3737ModeCommandIsTranslated() {
        assertEquals(parse("{\"D0310A\":2,\"D0310C\":0,\"D03102\":1}"),
                CoapProfile.AC3737.toDevice(modeCommand("P", null)));
        assertEquals(parse("{\"D0310A\":2,\"D0310C\":17,\"D03102\":1}"),
                CoapProfile.AC3737.toDevice(modeCommand("S", null)));
        assertEquals(parse("{\"D0310A\":2,\"D0310C\":2,\"D03102\":1}"),
                CoapProfile.AC3737.toDevice(modeCommand("M", "2")));
        assertEquals(parse("{\"D0310A\":3,\"D0310C\":18,\"D03102\":1}"),
                CoapProfile.AC3737.toDevice(modeCommand("M", "t")));
        // the speeds of the other models do not exist
        assertTrue(CoapProfile.AC3737.toDevice(modeCommand("M", "3")).isEmpty());
        assertTrue(CoapProfile.AC3737.toDevice(modeCommand("M", "m")).isEmpty());
    }

    @Test
    public void modeCommandDoesNotChangeThePowerCommandItIsSentWith() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setPower("0");
        command.setMode("S");

        assertEquals(parse("{\"D03102\":0,\"D0310A\":2,\"D0310C\":17}"), CoapProfile.AC3737.toDevice(command(command)));
    }

    @Test
    public void gen2ModeIsTranslated() {
        assertMode(CoapProfile.AC1715, "{\"D03-12\":\"Auto General\"}", "P", null);
        assertMode(CoapProfile.AC1715, "{\"D03-12\":\"Sleep\"}", "S", null);
        assertMode(CoapProfile.AC1715, "{\"D03-12\":\"Gentle/Speed 1\"}", "M", "1");
        assertMode(CoapProfile.AC1715, "{\"D03-12\":\"Speed 2\"}", "M", "2");
        assertMode(CoapProfile.AC1715, "{\"D03-12\":\"Turbo\"}", "M", "t");
        assertMode(CoapProfile.AC1715, "{\"D03-12\":\"Allergy Sleep\"}", null, null);
        assertMode(CoapProfile.AC0850, "{\"D03-12\":\"Turbo\"}", "M", "t");
        // the AC0850 has no speeds
        assertMode(CoapProfile.AC0850, "{\"D03-12\":\"Speed 2\"}", null, null);

        assertEquals(parse("{\"D03-12\":\"Auto General\",\"D03-02\":\"ON\"}"),
                CoapProfile.AC1715.toDevice(modeCommand("P", null)));
        assertEquals(parse("{\"D03-12\":\"Gentle/Speed 1\",\"D03-02\":\"ON\"}"),
                CoapProfile.AC1715.toDevice(modeCommand("M", "1")));
        assertEquals(parse("{\"D03-12\":\"Turbo\",\"D03-02\":\"ON\"}"),
                CoapProfile.AC0850.toDevice(modeCommand("M", "t")));
        assertTrue(CoapProfile.AC0850.toDevice(modeCommand("M", "1")).isEmpty());
        assertTrue(CoapProfile.BASIC_GEN2.toDevice(modeCommand("P", null)).isEmpty());
    }

    private void assertMode(CoapProfile profile, String reported, @Nullable String mode, @Nullable String speed) {
        PhilipsAirPurifierDataDTO data = gson.fromJson(profile.toClassic(parse(reported)),
                PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(mode, data.getMode(), reported);
        assertEquals(speed, data.getFanSpeed(), reported);
    }

    @Test
    public void gen2SettingsCommandsAreTranslated() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setChildLock(true);
        command.setAqit(7);
        command.setDisplayIndex("1");

        assertEquals(parse("{\"D03-03\":true,\"D03-44\":7,\"D03-42\":\"PM2.5\"}"),
                CoapProfile.AC1715.toDevice(command(command)));

        command = new PhilipsAirPurifierWritableDataDTO();
        command.setChildLock(false);
        command.setDisplayIndex("0");
        assertEquals(parse("{\"D03-03\":false,\"D03-42\":\"IAI\"}"), CoapProfile.AC0850.toDevice(command(command)));

        assertTrue(CoapProfile.BASIC_GEN2.toDevice(command(command)).isEmpty());
    }

    @Test
    public void humiditySetpointIsOnlySentToModelsThatSupportIt() {
        assertEquals(parse("{\"D03128\":50}"), CoapProfile.AC3737.toDevice(setpointCommand(50)));
        assertEquals(parse("{\"D03128\":70}"), CoapProfile.UNICORN.toDevice(setpointCommand(70)));
        assertTrue(CoapProfile.AC3737.toDevice(setpointCommand(45)).isEmpty());
        assertTrue(CoapProfile.AC3737.toDevice(setpointCommand(80)).isEmpty());
        assertTrue(CoapProfile.AC3737.toDevice(setpointCommand(30)).isEmpty());
        assertTrue(CoapProfile.BASIC_GEN3.toDevice(setpointCommand(50)).isEmpty());
        assertTrue(CoapProfile.AC1715.toDevice(setpointCommand(50)).isEmpty());
    }

    private JsonObject setpointCommand(int percent) {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setHumiditySetpoint(percent);
        return command(command);
    }

    @Test
    public void unicornSettingsAreRead() {
        JsonObject classic = CoapProfile.UNICORN.toClassic(parse("""
                {"D01S04":"Unicorn","D03130":100,"D03134":1,"D03115":0,"D03112":0,"D03105":115,"D03135":2}"""));

        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(Boolean.TRUE, data.getBeep());
        assertEquals(Boolean.TRUE, data.getStandbySensors());
        assertEquals(Boolean.FALSE, data.getAllergySleep());
        // the display is on when the field is 0
        assertEquals(Boolean.TRUE, data.getDisplayOn());
        assertEquals("115", data.getDisplayBrightness());
        assertEquals("2", data.getLampMode());

        classic = CoapProfile.UNICORN
                .toClassic(parse("{\"D01S04\":\"Unicorn\",\"D03130\":0,\"D03112\":1,\"D03105\":0}"));
        data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(Boolean.FALSE, data.getBeep());
        assertEquals(Boolean.FALSE, data.getDisplayOn());
        assertEquals("0", data.getDisplayBrightness());
        assertNull(data.getLampMode());
    }

    @Test
    public void settingsAreOnlyReadForTheModelsThatHaveThem() {
        String status = "{\"D01S04\":\"X\",\"D03130\":100,\"D03134\":1,\"D03115\":1,\"D03112\":0,\"D03105\":50,"
                + "\"D03135\":1}";

        JsonObject classic = CoapProfile.AC3737.toClassic(parse(status));
        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(Boolean.TRUE, data.getAllergySleep());
        assertEquals("50", data.getDisplayBrightness());
        for (String field : new String[] { "beep", "standby", "dispon", "lamp" }) {
            assertFalse(classic.has(field), field);
        }

        classic = CoapProfile.BASIC_GEN3.toClassic(parse(status));
        for (String field : new String[] { "beep", "standby", "allslp", "dispon", "dispbr", "lamp" }) {
            assertFalse(classic.has(field), field);
        }
    }

    @Test
    public void unexpectedSettingValuesAreIgnored() {
        JsonObject classic = CoapProfile.UNICORN.toClassic(parse(
                "{\"D01S04\":\"Unicorn\",\"D03130\":\"loud\",\"D03134\":null,\"D03105\":\"bright\",\"D03135\":\"x\"}"));

        for (String field : new String[] { "beep", "standby", "dispbr", "lamp" }) {
            assertFalse(classic.has(field), field);
        }
    }

    @Test
    public void unicornSettingCommandsAreTranslated() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setBeep(true);
        command.setStandbySensors(false);
        command.setAllergySleep(true);
        command.setDisplayOn(true);
        command.setDisplayBrightness("123");
        command.setLampMode("1");

        assertEquals(parse("{\"D03130\":100,\"D03134\":0,\"D03115\":1,\"D03112\":0,\"D03105\":123,\"D03135\":1}"),
                CoapProfile.UNICORN.toDevice(command(command)));

        command = new PhilipsAirPurifierWritableDataDTO();
        command.setBeep(false);
        command.setDisplayOn(false);
        command.setDisplayBrightness("0");
        assertEquals(parse("{\"D03130\":0,\"D03112\":1,\"D03105\":0}"), CoapProfile.UNICORN.toDevice(command(command)));
    }

    @Test
    public void settingCommandsWithValuesTheModelDoesNotOfferAreDropped() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setDisplayBrightness("50");
        command.setLampMode("3");

        assertTrue(CoapProfile.UNICORN.toDevice(command(command)).isEmpty());

        command = new PhilipsAirPurifierWritableDataDTO();
        command.setDisplayBrightness("50");
        assertEquals(parse("{\"D03105\":50}"), CoapProfile.AC3737.toDevice(command(command)));
        command.setDisplayBrightness("123");
        assertTrue(CoapProfile.AC3737.toDevice(command(command)).isEmpty());
    }

    @Test
    public void settingCommandsAreOnlySentToModelsThatHaveThem() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setBeep(true);
        command.setStandbySensors(true);
        command.setDisplayOn(true);
        command.setLampMode("1");

        assertTrue(CoapProfile.AC3737.toDevice(command(command)).isEmpty());
        assertTrue(CoapProfile.BASIC_GEN3.toDevice(command(command)).isEmpty());
        assertTrue(CoapProfile.AC1715.toDevice(command(command)).isEmpty());

        command = new PhilipsAirPurifierWritableDataDTO();
        command.setAllergySleep(true);
        assertEquals(parse("{\"D03115\":1}"), CoapProfile.AC3737.toDevice(command(command)));
    }

    @Test
    public void settingOptionsDependOnTheModel() {
        assertEquals(List.of("0", "101", "115", "123"), values(CoapProfile.UNICORN.getDisplayBrightnessOptions()));
        assertEquals(List.of("0", "50", "100"), values(CoapProfile.AC3737.getDisplayBrightnessOptions()));
        assertEquals(List.of("0", "1", "2"), values(CoapProfile.UNICORN.getLampModeOptions()));
        assertTrue(CoapProfile.AC3737.getLampModeOptions().isEmpty());
        assertTrue(CoapProfile.BASIC_GEN3.getDisplayBrightnessOptions().isEmpty());
    }

    @Test
    public void ac3737DoesNotTranslateTheUnicornTimer() {
        JsonObject classic = CoapProfile.AC3737.toClassic(parse("{\"D01S05\":\"AC3737/10\",\"D03110\":3}"));
        assertFalse(classic.has("dt"));
        assertTrue(CoapProfile.AC3737.toDevice(timerCommand(2)).isEmpty());
    }

    private static List<String> values(List<StateOption> options) {
        return options.stream().map(StateOption::getValue).toList();
    }

    @Test
    public void classicStatusIsUnchanged() {
        JsonObject status = parse(CLASSIC_STATUS);

        assertEquals(status, CoapProfile.CLASSIC.toClassic(status));
    }

    @Test
    public void gen2StatusIsTranslated() {
        JsonObject classic = CoapProfile.BASIC_GEN2.toClassic(parse(GEN2_STATUS));

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
        JsonObject classic = CoapProfile.BASIC_GEN3.toClassic(parse(GEN3_STATUS));

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
        JsonObject classic = CoapProfile.UNICORN.toClassic(parse(UNICORN_STATUS));

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
        JsonObject classic = CoapProfile.UNICORN.toClassic(parse("{\"D01S04\":\"Unicorn\"," + reported.substring(1)));

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
        JsonObject classic = CoapProfile.UNICORN.toClassic(parse("{\"D01S04\":\"Unicorn\"," + reported.substring(1)));

        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(hours, data.getTimer(), reported);
        assertEquals(minutesLeft, data.getTimerLeft(), reported);
    }

    @Test
    public void modeSpeedAndTimerAreOnlyTranslatedForUnicorn() {
        JsonObject classic = CoapProfile.BASIC_GEN3.toClassic(
                parse("{\"D01S04\":\"Pegasus\",\"D0310C\":18,\"D0310D\":3,\"D03110\":3,\"D03211\":60,\"D0312C\":4}"));

        assertFalse(classic.has("mode"));
        assertFalse(classic.has("om"));
        assertFalse(classic.has("dt"));
        // the threshold and the remaining time are read from every recent model
        assertEquals(4, classic.get("aqit").getAsInt());
        assertEquals(60, classic.get("dtrs").getAsInt());
    }

    @Test
    public void gen3SensorsAndSetpointAreReadFromEveryModel() {
        JsonObject classic = CoapProfile.BASIC_GEN3
                .toClassic(parse("{\"D01S05\":\"AC3737/10\",\"D03122\":2,\"D03128\":50,\"D03211\":90}"));

        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(2, data.getTvoc());
        assertEquals(50, data.getHumiditySetpoint());
        assertEquals(90, data.getTimerLeft());

        classic = CoapProfile.BASIC_GEN3
                .toClassic(parse("{\"D01S05\":\"AC3737/10\",\"D03122\":\"bad\",\"D03128\":null,\"D03211\":\"soon\"}"));
        assertFalse(classic.has("tvoc"));
        assertFalse(classic.has("rhset"));
        assertFalse(classic.has("dtrs"));
    }

    @Test
    public void gen2SettingsAndSensorsAreRead() {
        JsonObject classic = CoapProfile.BASIC_GEN2.toClassic(parse("""
                {"D01-05":"AC1715/10","D03-03":true,"D03-34":2,"D03-36":22,"D03-37":48,"D03-42":"PM2.5",
                "D03-44":4,"D03-64":0}"""));

        PhilipsAirPurifierDataDTO data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(Boolean.TRUE, data.getChildLock());
        assertEquals(2, data.getTvoc());
        assertEquals(22f, data.getTemperature());
        assertEquals(48f, data.getHumidity());
        assertEquals("1", data.getDisplayIndex());
        assertEquals(4, data.getAqit());
        assertEquals(0, data.getErrorCode());

        classic = CoapProfile.BASIC_GEN2.toClassic(parse("{\"D01-05\":\"AC1715/10\",\"D03-03\":0,\"D03-42\":\"IAI\"}"));
        data = gson.fromJson(classic, PhilipsAirPurifierDataDTO.class);
        assertNotNull(data);
        assertEquals(Boolean.FALSE, data.getChildLock());
        assertEquals("0", data.getDisplayIndex());
    }

    @Test
    public void gen2UnexpectedValuesAreIgnored() {
        JsonObject classic = CoapProfile.BASIC_GEN2.toClassic(parse("""
                {"D01-05":"AC1715/10","D03-03":"yes","D03-36":"warm","D03-37":null,"D03-42":"gas","D03-44":"high"}"""));

        for (String field : new String[] { "cl", "temp", "rh", "ddp", "aqit" }) {
            assertFalse(classic.has(field), field);
        }
    }

    @Test
    public void unexpectedValueTypesAreIgnored() {
        JsonObject classic = CoapProfile.BASIC_GEN3
                .toClassic(parse("{\"D01S05\":\"AC3737/10\",\"D03102\":\"on\",\"D03224\":\"hot\",\"D03221\":null}"));

        assertFalse(classic.has("pwr"));
        assertFalse(classic.has("temp"));
        assertFalse(classic.has("pm25"));
    }

    @Test
    public void gen3ErrorAndDisplayIndexAreOnlyTranslatedWhenReported() {
        JsonObject classic = CoapProfile.BASIC_GEN3.toClassic(parse("{\"D01S05\":\"AC3737/10\"}"));
        assertFalse(classic.has("err"));
        assertFalse(classic.has("ddp"));

        classic = CoapProfile.BASIC_GEN3
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

        assertEquals(parse("{\"om\":\"2\",\"mode\":\"M\"}"), CoapProfile.CLASSIC.toDevice(command(command)));
    }

    @Test
    public void powerCommandIsTranslated() {
        PhilipsAirPurifierWritableDataDTO on = new PhilipsAirPurifierWritableDataDTO();
        on.setPower("1");
        PhilipsAirPurifierWritableDataDTO off = new PhilipsAirPurifierWritableDataDTO();
        off.setPower("0");

        assertEquals(parse("{\"D03-02\":\"ON\"}"), CoapProfile.BASIC_GEN2.toDevice(command(on)));
        assertEquals(parse("{\"D03-02\":\"OFF\"}"), CoapProfile.BASIC_GEN2.toDevice(command(off)));
        assertEquals(parse("{\"D03102\":1}"), CoapProfile.BASIC_GEN3.toDevice(command(on)));
        assertEquals(parse("{\"D03102\":0}"), CoapProfile.BASIC_GEN3.toDevice(command(off)));
    }

    @Test
    public void unicornModeCommandIsTranslated() {
        assertEquals(parse("{\"D0310C\":0}"), CoapProfile.UNICORN.toDevice(modeCommand("P", null)));
        assertEquals(parse("{\"D0310C\":17}"), CoapProfile.UNICORN.toDevice(modeCommand("S", null)));
        // the fan speed command selects the manual mode together with the speed
        assertEquals(parse("{\"D0310C\":3}"), CoapProfile.UNICORN.toDevice(modeCommand("M", "3")));
        assertEquals(parse("{\"D0310C\":5}"), CoapProfile.UNICORN.toDevice(modeCommand("M", "5")));
        assertEquals(parse("{\"D0310C\":18}"), CoapProfile.UNICORN.toDevice(modeCommand("M", "t")));
        assertEquals(parse("{\"D0310C\":19}"), CoapProfile.UNICORN.toDevice(modeCommand("M", "m")));
        assertEquals(parse("{\"D0310C\":2}"), CoapProfile.UNICORN.toDevice(modeCommand(null, "2")));
    }

    @Test
    public void unicornModeCommandsWithoutMatchingModeAreDropped() {
        // the manual mode needs a speed, the other modes do not exist on the device
        assertTrue(CoapProfile.UNICORN.toDevice(modeCommand("M", null)).isEmpty());
        assertTrue(CoapProfile.UNICORN.toDevice(modeCommand("A", null)).isEmpty());
        assertTrue(CoapProfile.UNICORN.toDevice(modeCommand("B", "1")).isEmpty());
        assertTrue(CoapProfile.UNICORN.toDevice(modeCommand("M", "0")).isEmpty());
        assertTrue(CoapProfile.UNICORN.toDevice(modeCommand("M", "6")).isEmpty());
        assertTrue(CoapProfile.UNICORN.toDevice(modeCommand("M", "s")).isEmpty());
        assertTrue(CoapProfile.UNICORN.toDevice(modeCommand("M", "10")).isEmpty());
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
        assertEquals(parse("{\"D03110\":0}"), CoapProfile.UNICORN.toDevice(timerCommand(0)));
        assertEquals(parse("{\"D03110\":2}"), CoapProfile.UNICORN.toDevice(timerCommand(1)));
        assertEquals(parse("{\"D03110\":13}"), CoapProfile.UNICORN.toDevice(timerCommand(12)));
        assertTrue(CoapProfile.UNICORN.toDevice(timerCommand(13)).isEmpty());
        assertTrue(CoapProfile.UNICORN.toDevice(timerCommand(-1)).isEmpty());
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
            assertEquals(parse("{\"D0312C\":" + threshold + "}"), CoapProfile.UNICORN.toDevice(command(command)));
        }
        PhilipsAirPurifierWritableDataDTO unknownThreshold = new PhilipsAirPurifierWritableDataDTO();
        unknownThreshold.setAqit(5);
        assertTrue(CoapProfile.UNICORN.toDevice(command(unknownThreshold)).isEmpty());
        PhilipsAirPurifierWritableDataDTO textThreshold = new PhilipsAirPurifierWritableDataDTO();
        textThreshold.setAqit("7");
        assertTrue(CoapProfile.UNICORN.toDevice(command(textThreshold)).isEmpty());

        for (String index : new String[] { "0", "1" }) {
            PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
            command.setDisplayIndex(index);
            assertEquals(parse("{\"D0312A\":" + index + "}"), CoapProfile.UNICORN.toDevice(command(command)));
        }
        PhilipsAirPurifierWritableDataDTO gas = new PhilipsAirPurifierWritableDataDTO();
        gas.setDisplayIndex("2");
        assertTrue(CoapProfile.UNICORN.toDevice(command(gas)).isEmpty());
    }

    @Test
    public void unicornCommandKeepsPowerAndChildLock() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setPower("1");
        command.setChildLock(true);

        assertEquals(parse("{\"D03102\":1,\"D03103\":1}"), CoapProfile.UNICORN.toDevice(command(command)));
    }

    @Test
    public void onlyUnicornTranslatesModeTimerThresholdAndIndexCommands() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setMode("P");
        command.setFanSpeed("2");
        command.setTimer(2);
        command.setAqit(7);
        command.setDisplayIndex("1");

        assertTrue(CoapProfile.BASIC_GEN3.toDevice(command(command)).isEmpty());
        assertTrue(CoapProfile.BASIC_GEN2.toDevice(command(command)).isEmpty());
    }

    @Test
    public void childLockCommandIsTranslated() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setChildLock(true);

        assertEquals(parse("{\"D03103\":1}"), CoapProfile.BASIC_GEN3.toDevice(command(command)));
        assertTrue(CoapProfile.BASIC_GEN2.toDevice(command(command)).isEmpty());
    }

    @Test
    public void unsupportedCommandIsEmpty() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setFanSpeed("2");
        command.setMode("M");

        assertTrue(CoapProfile.BASIC_GEN2.toDevice(command(command)).isEmpty());
        assertTrue(CoapProfile.BASIC_GEN3.toDevice(command(command)).isEmpty());
    }
}
