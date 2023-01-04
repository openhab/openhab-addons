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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.mielecloud.internal.util.ResourceUtil.getResourceAsString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Björn Lange - Initial contribution
 * @author Benjamin Bolte - Add plate step
 */
@NonNullByDefault
public class DeviceCollectionTest {
    @Test
    public void testCreateDeviceCollection() throws IOException {
        // given:
        String json = getResourceAsString(
                "/org/openhab/binding/mielecloud/internal/webservice/api/json/deviceCollection.json");

        // when:
        DeviceCollection collection = DeviceCollection.fromJson(json);

        // then:
        assertEquals(1, collection.getDeviceIdentifiers().size());
        Device device = collection.getDevice(collection.getDeviceIdentifiers().iterator().next());

        Ident ident = device.getIdent().get();
        Type type = ident.getType().get();
        assertEquals("Devicetype", type.getKeyLocalized().get());
        assertEquals(DeviceType.HOOD, type.getValueRaw());
        assertEquals("Ventilation Hood", type.getValueLocalized().get());

        assertEquals("My Hood", ident.getDeviceName().get());

        DeviceIdentLabel deviceIdentLabel = ident.getDeviceIdentLabel().get();
        assertEquals("000124430017", deviceIdentLabel.getFabNumber().get());
        assertEquals("00", deviceIdentLabel.getFabIndex().get());
        assertEquals("DA-6996", deviceIdentLabel.getTechType().get());
        assertEquals("10101010", deviceIdentLabel.getMatNumber().get());
        assertEquals(Arrays.asList("4164", "20380", "25226"), deviceIdentLabel.getSwids());

        XkmIdentLabel xkmIdentLabel = ident.getXkmIdentLabel().get();
        assertEquals("EK039W", xkmIdentLabel.getTechType().get());
        assertEquals("02.31", xkmIdentLabel.getReleaseVersion().get());

        State state = device.getState().get();
        Status status = state.getStatus().get();
        assertEquals(Integer.valueOf(StateType.RUNNING.getCode()), status.getValueRaw().get());
        assertEquals("In use", status.getValueLocalized().get());
        assertEquals("State", status.getKeyLocalized().get());

        ProgramType programType = state.getProgramType().get();
        assertEquals(Integer.valueOf(0), programType.getValueRaw().get());
        assertEquals("", programType.getValueLocalized().get());
        assertEquals("Programme", programType.getKeyLocalized().get());

        ProgramPhase programPhase = state.getProgramPhase().get();
        assertEquals(Integer.valueOf(4609), programPhase.getValueRaw().get());
        assertEquals("", programPhase.getValueLocalized().get());
        assertEquals("Phase", programPhase.getKeyLocalized().get());

        assertEquals(Arrays.asList(0, 0), state.getRemainingTime().get());
        assertEquals(Arrays.asList(0, 0), state.getStartTime().get());

        assertEquals(1, state.getTargetTemperature().size());
        Temperature targetTemperature = state.getTargetTemperature().get(0);
        assertNotNull(targetTemperature);
        assertEquals(Integer.valueOf(-32768), targetTemperature.getValueRaw().get());
        assertFalse(targetTemperature.getValueLocalized().isPresent());
        assertEquals("Celsius", targetTemperature.getUnit().get());

        assertEquals(3, state.getTemperature().size());
        Temperature temperature0 = state.getTemperature().get(0);
        assertNotNull(temperature0);
        assertEquals(Integer.valueOf(-32768), temperature0.getValueRaw().get());
        assertFalse(temperature0.getValueLocalized().isPresent());
        assertEquals("Celsius", temperature0.getUnit().get());
        Temperature temperature1 = state.getTemperature().get(1);
        assertNotNull(temperature1);
        assertEquals(Integer.valueOf(-32768), temperature1.getValueRaw().get());
        assertFalse(temperature1.getValueLocalized().isPresent());
        assertEquals("Celsius", temperature1.getUnit().get());
        Temperature temperature2 = state.getTemperature().get(2);
        assertNotNull(temperature2);
        assertEquals(Integer.valueOf(-32768), temperature2.getValueRaw().get());
        assertFalse(temperature2.getValueLocalized().isPresent());
        assertEquals("Celsius", temperature2.getUnit().get());

        assertEquals(false, state.getSignalInfo().get());
        assertEquals(false, state.getSignalFailure().get());
        assertEquals(false, state.getSignalDoor().get());

        RemoteEnable remoteEnable = state.getRemoteEnable().get();
        assertEquals(false, remoteEnable.getFullRemoteControl().get());
        assertEquals(false, remoteEnable.getSmartGrid().get());

        assertEquals(Light.ENABLE, state.getLight());
        assertEquals(new ArrayList<Object>(), state.getElapsedTime().get());

        SpinningSpeed spinningSpeed = state.getSpinningSpeed().get();
        assertEquals(Integer.valueOf(1200), spinningSpeed.getValueRaw().get());
        assertEquals("1200", spinningSpeed.getValueLocalized().get());
        assertEquals("rpm", spinningSpeed.getUnit().get());

        DryingStep dryingStep = state.getDryingStep().get();
        assertFalse(dryingStep.getValueRaw().isPresent());
        assertEquals("", dryingStep.getValueLocalized().get());
        assertEquals("Drying level", dryingStep.getKeyLocalized().get());

        VentilationStep ventilationStep = state.getVentilationStep().get();
        assertEquals(Integer.valueOf(2), ventilationStep.getValueRaw().get());
        assertEquals("2", ventilationStep.getValueLocalized().get());
        assertEquals("Power Level", ventilationStep.getKeyLocalized().get());

        List<PlateStep> plateStep = state.getPlateStep();
        assertEquals(4, plateStep.size());
        assertEquals(Integer.valueOf(0), plateStep.get(0).getValueRaw().get());
        assertEquals("0", plateStep.get(0).getValueLocalized().get());
        assertEquals("Plate Step", plateStep.get(0).getKeyLocalized().get());
        assertEquals(Integer.valueOf(1), plateStep.get(1).getValueRaw().get());
        assertEquals("1", plateStep.get(1).getValueLocalized().get());
        assertEquals("Plate Step", plateStep.get(1).getKeyLocalized().get());
        assertEquals(Integer.valueOf(2), plateStep.get(2).getValueRaw().get());
        assertEquals("1.", plateStep.get(2).getValueLocalized().get());
        assertEquals("Plate Step", plateStep.get(2).getKeyLocalized().get());
        assertEquals(Integer.valueOf(3), plateStep.get(3).getValueRaw().get());
        assertEquals("2", plateStep.get(3).getValueLocalized().get());
        assertEquals("Plate Step", plateStep.get(3).getKeyLocalized().get());

        assertEquals(Integer.valueOf(20), state.getBatteryLevel().get());
    }

    @Test
    public void testCreateDeviceCollectionFromInvalidJsonThrowsMieleSyntaxException() throws IOException {
        // given:
        String invalidJson = getResourceAsString(
                "/org/openhab/binding/mielecloud/internal/webservice/api/json/invalidDeviceCollection.json");

        // when:
        assertThrows(MieleSyntaxException.class, () -> {
            DeviceCollection.fromJson(invalidJson);
        });
    }

    @Test
    public void testCreateDeviceCollectionWithLargeProgramID() throws IOException {
        // given:
        String json = getResourceAsString(
                "/org/openhab/binding/mielecloud/internal/webservice/api/json/deviceCollectionWithLargeProgramID.json");

        // when:
        DeviceCollection collection = DeviceCollection.fromJson(json);

        // then:
        assertEquals(1, collection.getDeviceIdentifiers().size());
        Device device = collection.getDevice(collection.getDeviceIdentifiers().iterator().next());

        Ident ident = device.getIdent().get();
        Type type = ident.getType().get();
        assertEquals("Devicetype", type.getKeyLocalized().get());
        assertEquals(DeviceType.UNKNOWN, type.getValueRaw());
        assertEquals("", type.getValueLocalized().get());

        assertEquals("Some Devicename", ident.getDeviceName().get());

        DeviceIdentLabel deviceIdentLabel = ident.getDeviceIdentLabel().get();
        assertEquals("", deviceIdentLabel.getFabNumber().get());
        assertEquals("", deviceIdentLabel.getFabIndex().get());
        assertEquals("", deviceIdentLabel.getTechType().get());
        assertEquals("", deviceIdentLabel.getMatNumber().get());
        assertEquals(Arrays.asList(), deviceIdentLabel.getSwids());

        XkmIdentLabel xkmIdentLabel = ident.getXkmIdentLabel().get();
        assertEquals("", xkmIdentLabel.getTechType().get());
        assertEquals("", xkmIdentLabel.getReleaseVersion().get());

        State state = device.getState().get();
        ProgramId programId = state.getProgramId().get();
        assertEquals(Long.valueOf(2499805184L), programId.getValueRaw().get());
        assertEquals("", programId.getValueLocalized().get());
        assertEquals("Program Id", programId.getKeyLocalized().get());

        Status status = state.getStatus().get();
        assertEquals(Integer.valueOf(StateType.RUNNING.getCode()), status.getValueRaw().get());
        assertEquals("In use", status.getValueLocalized().get());
        assertEquals("State", status.getKeyLocalized().get());

        ProgramType programType = state.getProgramType().get();
        assertEquals(Integer.valueOf(0), programType.getValueRaw().get());
        assertEquals("Operation mode", programType.getValueLocalized().get());
        assertEquals("Program type", programType.getKeyLocalized().get());

        ProgramPhase programPhase = state.getProgramPhase().get();
        assertEquals(Integer.valueOf(0), programPhase.getValueRaw().get());
        assertEquals("", programPhase.getValueLocalized().get());
        assertEquals("Phase", programPhase.getKeyLocalized().get());

        assertEquals(Arrays.asList(0, 0), state.getRemainingTime().get());
        assertEquals(Arrays.asList(0, 0), state.getStartTime().get());

        assertTrue(state.getTargetTemperature().isEmpty());
        assertTrue(state.getTemperature().isEmpty());

        assertEquals(false, state.getSignalInfo().get());
        assertEquals(false, state.getSignalFailure().get());
        assertEquals(false, state.getSignalDoor().get());

        RemoteEnable remoteEnable = state.getRemoteEnable().get();
        assertEquals(true, remoteEnable.getFullRemoteControl().get());
        assertEquals(false, remoteEnable.getSmartGrid().get());

        assertEquals(Light.NOT_SUPPORTED, state.getLight());
        assertEquals(new ArrayList<Object>(), state.getElapsedTime().get());

        DryingStep dryingStep = state.getDryingStep().get();
        assertFalse(dryingStep.getValueRaw().isPresent());
        assertEquals("", dryingStep.getValueLocalized().get());
        assertEquals("Drying level", dryingStep.getKeyLocalized().get());

        VentilationStep ventilationStep = state.getVentilationStep().get();
        assertFalse(ventilationStep.getValueRaw().isPresent());
        assertEquals("", ventilationStep.getValueLocalized().get());
        assertEquals("Power Level", ventilationStep.getKeyLocalized().get());

        List<PlateStep> plateStep = state.getPlateStep();
        assertEquals(0, plateStep.size());
    }

    @Test
    public void testCreateDeviceCollectionWithSpinningSpeedObject() throws IOException {
        // given:
        String json = getResourceAsString(
                "/org/openhab/binding/mielecloud/internal/webservice/api/json/deviceCollectionWithSpinningSpeedObject.json");

        // when:
        DeviceCollection collection = DeviceCollection.fromJson(json);

        // then:
        assertEquals(1, collection.getDeviceIdentifiers().size());
        Device device = collection.getDevice(collection.getDeviceIdentifiers().iterator().next());

        State state = device.getState().get();
        SpinningSpeed spinningSpeed = state.getSpinningSpeed().get();
        assertNotNull(spinningSpeed);
        assertEquals(Integer.valueOf(1600), spinningSpeed.getValueRaw().get());
        assertEquals("1600", spinningSpeed.getValueLocalized().get());
        assertEquals("U/min", spinningSpeed.getUnit().get());
    }

    @Test
    public void testCreateDeviceCollectionWithFloatingPointTemperature() throws IOException {
        // given:
        String json = getResourceAsString(
                "/org/openhab/binding/mielecloud/internal/webservice/api/json/deviceCollectionWithFloatingPointTargetTemperature.json");

        // when:
        DeviceCollection collection = DeviceCollection.fromJson(json);

        // then:
        assertEquals(1, collection.getDeviceIdentifiers().size());
        Device device = collection.getDevice(collection.getDeviceIdentifiers().iterator().next());

        State state = device.getState().get();
        List<Temperature> targetTemperatures = state.getTargetTemperature();
        assertEquals(1, targetTemperatures.size());

        Temperature targetTemperature = targetTemperatures.get(0);
        assertEquals(Integer.valueOf(80), targetTemperature.getValueRaw().get());
        assertEquals(Integer.valueOf(0), targetTemperature.getValueLocalized().get());
        assertEquals("Celsius", targetTemperature.getUnit().get());
    }
}
