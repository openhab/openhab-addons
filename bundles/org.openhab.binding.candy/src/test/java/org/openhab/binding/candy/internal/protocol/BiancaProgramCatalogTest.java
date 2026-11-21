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
package org.openhab.binding.candy.internal.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog.ProgramOption;

import com.google.gson.JsonParser;

/**
 * Checks server catalog boundaries and independent command vectors against the documented app contract.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class BiancaProgramCatalogTest {
    private Map<String, String> defaults() {
        return new HashMap<>(Map.of("selector_position", "1", "pr_code", "65", "default_temperature", "60",
                "default_spin_speed", "1000", "minimum_soil_level", "1", "maximum_soil_level", "3",
                "default_soil_level", "3", "steam", "5", "dry", "0"));
    }

    @Test
    void serializesStartInAppOrderWithNoOptionalFeaturesOrDelay() throws Exception {
        assertEquals(
                "Write=1&StSt=1&DelVl=0&PrNm=1&PrCode=65&PrStr=Cottons&TmpTgt=60&SLevTgt=3&SpdTgt=10"
                        + "&OptMsk1=0&OptMsk2=0&Lang=1&Stm=0&Dry=0&ED=0&RecipeId=0&StartCheckUp=0&DispTestOn=1",
                BiancaCommands.start("Cottons", defaults()));
        assertEquals("Write=1&StSt=0&PrNm=1&DelVl=0", BiancaCommands.stop("1", true));
        assertEquals("Write=1&StSt=0", BiancaCommands.stop("", false));
    }

    @Test
    void preservesAppSentinelsAndFixedSteamDefault() throws Exception {
        Map<String, String> parameters = defaults();
        parameters.put("default_temperature", "255");
        parameters.put("default_spin_speed", "255");
        parameters.put("steam", "4");
        String result = BiancaCommands.start("Rinse", parameters);
        assertFalse(result.contains("TmpTgt"));
        assertTrue(result.contains("&SpdTgt=0&"));
        assertTrue(result.contains("&Stm=1&"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "selector_position", "pr_code", "default_temperature", "default_spin_speed",
            "minimum_soil_level", "maximum_soil_level", "default_soil_level", "steam", "dry" })
    void missingRequiredDefaultsCannotStart(String name) {
        Map<String, String> parameters = defaults();
        parameters.remove(name);
        assertThrows(CandyProtocolException.class, () -> BiancaCommands.start("Cottons", parameters));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Cottons&StSt=0", "Cottons%26", "MAINTENANCE", "maintenance", "AUTOCLEAN", "TUMBLING",
            "Pranie łagodne" })
    void rejectsUnverifiedNamesAndSpecialCommands(String name) {
        assertThrows(CandyProtocolException.class, () -> BiancaCommands.start(name, defaults()));
    }

    @Test
    void listsIncompleteProgramsWithoutInventingDefaultsOrExposingUnrelatedFields() throws Exception {
        var programs = BiancaProgramCatalog.parse(JsonParser.parseString("""
                [{"program":{"id":"synthetic","name":"DUAL_WM_WD_PROGRAM_NAME_COTTONS",
                "encryption_key":"private","command_parameters":[
                {"command_parameter":{"name":"default_temperature","validation":"60"}},
                {"command_parameter":{"name":"password","validation":"secret"}}]}}]
                """));
        assertEquals("Cottons", programs.getFirst().label());
        assertTrue(programs.getFirst().startPayload().isEmpty());
        var options = BiancaProgramCatalog.options(programs);
        var option = options.getFirst();
        assertEquals("synthetic", option.id());
        assertEquals("Cottons", option.label());
        assertFalse(option.startable());
        assertEquals(Map.of("default_temperature", "60"), option.parameters());
        assertThrows(UnsupportedOperationException.class, options::clear);
        assertThrows(UnsupportedOperationException.class, option.parameters()::clear);
        String json = BiancaProgramCatalog.json(programs);
        assertTrue(json.contains("default_temperature"));
        assertFalse(json.contains("private"));
        assertFalse(json.contains("secret"));
        assertFalse(json.contains("password"));
        assertThrows(UnsupportedOperationException.class, () -> programs.clear());
        assertThrows(UnsupportedOperationException.class, () -> programs.getFirst().parameters().clear());
    }

    @Test
    void programOptionsDefensivelyCopyTheirParameters() {
        Map<String, String> parameters = new HashMap<>(Map.of("default_temperature", "60"));
        var option = new ProgramOption("cottons", "Cottons", true, parameters);
        parameters.put("default_temperature", "90");
        assertEquals(Map.of("default_temperature", "60"), option.parameters());
        assertThrows(UnsupportedOperationException.class, option.parameters()::clear);
    }

    @Test
    void duplicateIdsAndMalformedContainersAreRejected() {
        assertThrows(CandyProtocolException.class, () -> BiancaProgramCatalog.parse(JsonParser.parseString("{}")));
        assertThrows(CandyProtocolException.class, () -> BiancaProgramCatalog.parse(JsonParser.parseString("""
                [{"id":"same","name":"Cottons"},{"id":"same","name":"Rinse"}]
                """)));
    }
}
