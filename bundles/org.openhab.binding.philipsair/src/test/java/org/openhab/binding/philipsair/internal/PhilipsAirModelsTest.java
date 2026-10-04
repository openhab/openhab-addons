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
package org.openhab.binding.philipsair.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;

import com.google.gson.Gson;

/**
 * Tests the knowledge about the models of the devices.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class PhilipsAirModelsTest {

    private final Gson gson = new Gson();

    @Test
    public void gasIndexOfUnknownModelsDependsOnTheGasSensor() {
        PhilipsAirPurifierDeviceDTO unknownModel = gson.fromJson("{\"modelid\":\"XY1234/10\"}",
                PhilipsAirPurifierDeviceDTO.class);

        assertTrue(PhilipsAirModels.supportsGasIndex(unknownModel,
                gson.fromJson("{\"tvoc\":1}", PhilipsAirPurifierDataDTO.class)));
        assertFalse(PhilipsAirModels.supportsGasIndex(unknownModel,
                gson.fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDataDTO.class)));
        assertTrue(PhilipsAirModels
                .supportsGasIndex(gson.fromJson("{\"type\":\"AC4558\"}", PhilipsAirPurifierDeviceDTO.class), null));
    }

    private PhilipsAirPurifierDeviceDTO deviceOf(String json) {
        return Objects.requireNonNull(gson.fromJson(json, PhilipsAirPurifierDeviceDTO.class));
    }

    @ParameterizedTest
    @CsvSource({ "AC4558/10", "AC4550/10", "AC6675/10", "AC5659/10", "AC5660/10", "MS3000/10", "MS4000/10", "ac5659/10",
            "ms3000/10" })
    public void gasIndexIsSupportedByTheGasModels(String modelId) {
        assertTrue(PhilipsAirModels.supportsGasIndex(deviceOf("{\"modelid\":\"" + modelId + "\"}"), null));
    }

    @ParameterizedTest
    @CsvSource({ "AC2889/10", "AC3829/10", "AC4373/10", "AC4375/10", "AC5000/10", "MS2000/10", "ac2889/10" })
    public void gasIndexIsNotSupportedByOtherModels(String modelId) {
        assertFalse(PhilipsAirModels.supportsGasIndex(deviceOf("{\"modelid\":\"" + modelId + "\"}"), null));
    }

    @ParameterizedTest
    @CsvSource({ "AC4373/10", "AC4375/10", "AC4373/11", "ac4373/10", "ac4375/10" })
    public void thresholdsAreTextForTheTextThresholdModels(String modelId) {
        assertTrue(PhilipsAirModels.hasTextThresholds(deviceOf("{\"modelid\":\"" + modelId + "\"}")));
    }

    @ParameterizedTest
    @CsvSource({ "AC4374/10", "AC4558/10", "AC5659/10", "AC2889/10", "AC3829/10", "MS3000/10" })
    public void thresholdsAreNumbersForOtherModels(String modelId) {
        assertFalse(PhilipsAirModels.hasTextThresholds(deviceOf("{\"modelid\":\"" + modelId + "\"}")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " " })
    public void blankModelIdFallsBackToTheType(String modelId) {
        PhilipsAirPurifierDeviceDTO textModel = deviceOf("{\"modelid\":\"" + modelId + "\",\"type\":\"AC4373\"}");
        assertTrue(PhilipsAirModels.hasTextThresholds(textModel));
        assertFalse(PhilipsAirModels.supportsGasIndex(textModel, null));
        assertTrue(PhilipsAirModels.supportsGasIndex(deviceOf("{\"modelid\":\"" + modelId + "\",\"type\":\"AC4558\"}"),
                null));
    }

    @Test
    public void modelIdTakesPrecedenceOverTheType() {
        PhilipsAirPurifierDeviceDTO device = deviceOf("{\"modelid\":\"AC2889/10\",\"type\":\"AC4558\"}");

        assertFalse(PhilipsAirModels.supportsGasIndex(device, null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"modelid\":\"\"}", "{\"modelid\":\" \"}" })
    public void deviceWithoutModelOnlyHasTheGasIndexWithAGasSensor(String json) {
        PhilipsAirPurifierDeviceDTO device = deviceOf(json);

        assertFalse(PhilipsAirModels.hasTextThresholds(device));
        assertFalse(PhilipsAirModels.supportsGasIndex(device, null));
        assertFalse(PhilipsAirModels.supportsGasIndex(device,
                gson.fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDataDTO.class)));
        assertTrue(PhilipsAirModels.supportsGasIndex(device,
                gson.fromJson("{\"tvoc\":1}", PhilipsAirPurifierDataDTO.class)));
    }
}
