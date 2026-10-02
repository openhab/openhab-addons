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

    private final Gson gson = new Gson();

    private static JsonObject parse(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    public void schemeIsDetectedFromFieldNames() {
        assertEquals(CoapKeyScheme.CLASSIC, CoapKeyScheme.detect(parse(CLASSIC_STATUS)));
        assertEquals(CoapKeyScheme.GEN2, CoapKeyScheme.detect(parse(GEN2_STATUS)));
        assertEquals(CoapKeyScheme.GEN3, CoapKeyScheme.detect(parse(GEN3_STATUS)));
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
