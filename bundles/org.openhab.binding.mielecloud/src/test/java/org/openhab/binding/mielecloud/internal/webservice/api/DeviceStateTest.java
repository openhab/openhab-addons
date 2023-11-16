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
package org.openhab.binding.mielecloud.internal.webservice.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Device;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceIdentLabel;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DryingStep;
import org.openhab.binding.mielecloud.internal.webservice.api.json.EcoFeedback;
import org.openhab.binding.mielecloud.internal.webservice.api.json.EnergyConsumption;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Ident;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Light;
import org.openhab.binding.mielecloud.internal.webservice.api.json.PlateStep;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProgramId;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProgramPhase;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProgramType;
import org.openhab.binding.mielecloud.internal.webservice.api.json.RemoteEnable;
import org.openhab.binding.mielecloud.internal.webservice.api.json.SpinningSpeed;
import org.openhab.binding.mielecloud.internal.webservice.api.json.State;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Status;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Temperature;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Type;
import org.openhab.binding.mielecloud.internal.webservice.api.json.VentilationStep;
import org.openhab.binding.mielecloud.internal.webservice.api.json.WaterConsumption;

/**
 * @author Björn Lange - Initial contribution
 * @author Benjamin Bolte - Add pre-heat finished, plate step, door state, door alarm and info state channels and map
 *         signal flags from API
 * @author Björn Lange - Add elapsed time channel, robotic vacuum cleaner, eco feedback
 */
@NonNullByDefault
public class DeviceStateTest {
    private static final String DEVICE_IDENTIFIER = "mac-f83001f37d45ffff";

    @Test
    public void testGetDeviceIdentifierReturnsDeviceIdentifier() {
        // given:
        Device device = mock(Device.class);
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String deviceId = deviceState.getDeviceIdentifier();

        // then:
        assertEquals(DEVICE_IDENTIFIER, deviceId);
    }

    @Test
    public void testReturnValuesWhenDeviceIsNull() {
        // given:
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, null);

        // when:
        Optional<String> status = deviceState.getStatus();
        Optional<Integer> statusRaw = deviceState.getStatusRaw();
        Optional<StateType> stateType = deviceState.getStateType();
        Optional<String> selectedProgram = deviceState.getSelectedProgram();
        Optional<Long> selectedProgramId = deviceState.getSelectedProgramId();
        Optional<String> programPhase = deviceState.getProgramPhase();
        Optional<Integer> programPhaseRaw = deviceState.getProgramPhaseRaw();
        Optional<String> dryingTarget = deviceState.getDryingTarget();
        Optional<Integer> dryingTargetRaw = deviceState.getDryingTargetRaw();
        Optional<Boolean> hasPreHeatFinished = deviceState.hasPreHeatFinished();
        Optional<Integer> targetTemperature = deviceState.getTargetTemperature(0);
        Optional<Integer> temperature = deviceState.getTemperature(0);
        Optional<Boolean> remoteControlEnabled = deviceState.isRemoteControlEnabled();
        Optional<String> ventilationStep = deviceState.getVentilationStep();
        Optional<Integer> ventilationStepRaw = deviceState.getVentilationStepRaw();
        Optional<Integer> plateStepCount = deviceState.getPlateStepCount();
        Optional<String> plateStep = deviceState.getPlateStep(0);
        Optional<Integer> plateStepRaw = deviceState.getPlateStepRaw(0);
        boolean hasError = deviceState.hasError();
        boolean hasInfo = deviceState.hasInfo();
        Optional<Boolean> doorState = deviceState.getDoorState();
        Optional<Boolean> doorAlarm = deviceState.getDoorAlarm();
        Optional<String> type = deviceState.getType();
        DeviceType rawType = deviceState.getRawType();
        Optional<Integer> batteryLevel = deviceState.getBatteryLevel();

        Optional<String> deviceName = deviceState.getDeviceName();
        Optional<String> fabNumber = deviceState.getFabNumber();
        Optional<String> techType = deviceState.getTechType();
        Optional<Integer> progress = deviceState.getProgress();

        // then:
        assertFalse(status.isPresent());
        assertFalse(statusRaw.isPresent());
        assertFalse(stateType.isPresent());
        assertFalse(selectedProgram.isPresent());
        assertFalse(selectedProgramId.isPresent());
        assertFalse(programPhase.isPresent());
        assertFalse(programPhaseRaw.isPresent());
        assertFalse(dryingTarget.isPresent());
        assertFalse(dryingTargetRaw.isPresent());
        assertFalse(hasPreHeatFinished.isPresent());
        assertFalse(targetTemperature.isPresent());
        assertFalse(temperature.isPresent());
        assertFalse(remoteControlEnabled.isPresent());
        assertFalse(ventilationStep.isPresent());
        assertFalse(ventilationStepRaw.isPresent());
        assertFalse(plateStepCount.isPresent());
        assertFalse(plateStep.isPresent());
        assertFalse(plateStepRaw.isPresent());
        assertFalse(hasError);
        assertFalse(hasInfo);
        assertFalse(doorState.isPresent());
        assertFalse(doorAlarm.isPresent());
        assertFalse(type.isPresent());
        assertEquals(DeviceType.UNKNOWN, rawType);
        assertFalse(deviceName.isPresent());
        assertFalse(fabNumber.isPresent());
        assertFalse(techType.isPresent());
        assertFalse(progress.isPresent());
        assertFalse(batteryLevel.isPresent());
    }

    @Test
    public void testReturnValuesWhenStateIsNull() {
        // given:
        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.empty());
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> status = deviceState.getStatus();
        Optional<Integer> statusRaw = deviceState.getStatusRaw();
        Optional<StateType> stateType = deviceState.getStateType();
        Optional<String> selectedProgram = deviceState.getSelectedProgram();
        Optional<Long> selectedProgramId = deviceState.getSelectedProgramId();
        Optional<String> programPhase = deviceState.getProgramPhase();
        Optional<Integer> programPhaseRaw = deviceState.getProgramPhaseRaw();
        Optional<String> dryingTarget = deviceState.getDryingTarget();
        Optional<Integer> dryingTargetRaw = deviceState.getDryingTargetRaw();
        Optional<Boolean> hasPreHeatFinished = deviceState.hasPreHeatFinished();
        Optional<Integer> targetTemperature = deviceState.getTargetTemperature(0);
        Optional<Integer> temperature = deviceState.getTemperature(0);
        Optional<Boolean> remoteControlEnabled = deviceState.isRemoteControlEnabled();
        Optional<Integer> progress = deviceState.getProgress();
        Optional<String> ventilationStep = deviceState.getVentilationStep();
        Optional<Integer> ventilationStepRaw = deviceState.getVentilationStepRaw();
        Optional<Integer> plateStepCount = deviceState.getPlateStepCount();
        Optional<String> plateStep = deviceState.getPlateStep(0);
        Optional<Integer> plateStepRaw = deviceState.getPlateStepRaw(0);
        Boolean hasError = deviceState.hasError();
        Optional<Boolean> doorState = deviceState.getDoorState();
        Optional<Boolean> doorAlarm = deviceState.getDoorAlarm();
        Optional<Integer> batteryLevel = deviceState.getBatteryLevel();

        // then:
        assertFalse(status.isPresent());
        assertFalse(statusRaw.isPresent());
        assertFalse(stateType.isPresent());
        assertFalse(selectedProgram.isPresent());
        assertFalse(selectedProgramId.isPresent());
        assertFalse(programPhase.isPresent());
        assertFalse(programPhaseRaw.isPresent());
        assertFalse(dryingTarget.isPresent());
        assertFalse(dryingTargetRaw.isPresent());
        assertFalse(hasPreHeatFinished.isPresent());
        assertFalse(targetTemperature.isPresent());
        assertFalse(temperature.isPresent());
        assertFalse(remoteControlEnabled.isPresent());
        assertFalse(progress.isPresent());
        assertFalse(ventilationStep.isPresent());
        assertFalse(ventilationStepRaw.isPresent());
        assertFalse(plateStepCount.isPresent());
        assertFalse(plateStep.isPresent());
        assertFalse(plateStepRaw.isPresent());
        assertFalse(hasError);
        assertFalse(doorState.isPresent());
        assertFalse(doorAlarm.isPresent());
        assertFalse(batteryLevel.isPresent());
    }

    @Test
    public void testReturnValuesWhenStatusIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> status = deviceState.getStatus();
        Optional<Integer> statusRaw = deviceState.getStatusRaw();
        Optional<StateType> stateType = deviceState.getStateType();

        // then:
        assertFalse(status.isPresent());
        assertFalse(statusRaw.isPresent());
        assertFalse(stateType.isPresent());
    }

    @Test
    public void testReturnValuesWhenStatusValuesAreEmpty() {
        // given:
        Status statusMock = mock(Status.class);
        when(statusMock.getValueLocalized()).thenReturn(Optional.empty());
        when(statusMock.getValueRaw()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(statusMock));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> status = deviceState.getStatus();
        Optional<Integer> statusRaw = deviceState.getStatusRaw();
        Optional<StateType> stateType = deviceState.getStateType();

        // then:
        assertFalse(status.isPresent());
        assertFalse(statusRaw.isPresent());
        assertFalse(stateType.isPresent());
    }

    @Test
    public void testReturnValuesWhenStatusValueLocalizedIsNotNull() {
        // given:
        Status statusMock = mock(Status.class);
        when(statusMock.getValueLocalized()).thenReturn(Optional.of("Not connected"));
        when(statusMock.getValueRaw()).thenReturn(Optional.of(StateType.NOT_CONNECTED.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(statusMock));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String status = deviceState.getStatus().get();
        int statusRaw = deviceState.getStatusRaw().get();
        StateType stateType = deviceState.getStateType().get();

        // then:
        assertEquals("Not connected", status);
        assertEquals(StateType.NOT_CONNECTED.getCode(), statusRaw);
        assertEquals(StateType.NOT_CONNECTED, stateType);
    }

    @Test
    public void testReturnValuesWhenStatusValueRawIsNotNull() {
        // given:
        Status statusMock = mock(Status.class);
        when(statusMock.getValueRaw()).thenReturn(Optional.of(StateType.END_PROGRAMMED.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(statusMock));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        StateType stateType = deviceState.getStateType().get();

        // then:
        assertEquals(StateType.END_PROGRAMMED, stateType);
    }

    @Test
    public void testReturnValuesWhenProgramTypeIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getProgramType()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> selectedProgram = deviceState.getSelectedProgram();
        Optional<Long> selectedProgramId = deviceState.getSelectedProgramId();

        // then:
        assertFalse(selectedProgram.isPresent());
        assertFalse(selectedProgramId.isPresent());
    }

    @Test
    public void testReturnValuesWhenProgramTypeValueLocalizedIsEmpty() {
        // given:
        ProgramType programType = mock(ProgramType.class);
        when(programType.getValueLocalized()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getProgramType()).thenReturn(Optional.of(programType));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> selectedProgram = deviceState.getSelectedProgram();
        Optional<Long> selectedProgramId = deviceState.getSelectedProgramId();

        // then:
        assertFalse(selectedProgram.isPresent());
        assertFalse(selectedProgramId.isPresent());
    }

    @Test
    public void testReturnValuesWhenProgramTypeValueLocalizedIsNotNull() {
        // given:
        ProgramId programId = mock(ProgramId.class);
        when(programId.getValueRaw()).thenReturn(Optional.of(3L));
        when(programId.getValueLocalized()).thenReturn(Optional.of("Washing"));

        State state = mock(State.class);
        Status status = mock(Status.class);
        Device device = mock(Device.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getProgramId()).thenReturn(Optional.of(programId));
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String selectedProgram = deviceState.getSelectedProgram().get();
        long selectedProgramId = deviceState.getSelectedProgramId().get();

        // then:
        assertEquals("Washing", selectedProgram);
        assertEquals(3L, selectedProgramId);
    }

    @Test
    public void testReturnValuesWhenProgramPhaseIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getProgramPhase()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> programPhase = deviceState.getProgramPhase();
        Optional<Integer> programPhaseRaw = deviceState.getProgramPhaseRaw();

        // then:
        assertFalse(programPhase.isPresent());
        assertFalse(programPhaseRaw.isPresent());
    }

    @Test
    public void testReturnValuesWhenProgramPhaseValueLocalizedIsEmpty() {
        // given:
        ProgramPhase programPhaseMock = mock(ProgramPhase.class);
        when(programPhaseMock.getValueLocalized()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getProgramPhase()).thenReturn(Optional.of(programPhaseMock));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> programPhase = deviceState.getProgramPhase();
        Optional<Integer> programPhaseRaw = deviceState.getProgramPhaseRaw();

        // then:
        assertFalse(programPhase.isPresent());
        assertFalse(programPhaseRaw.isPresent());
    }

    @Test
    public void testReturnValuesWhenProgramPhaseValueLocalizedIsNotNull() {
        // given:
        ProgramPhase programPhaseMock = mock(ProgramPhase.class);
        when(programPhaseMock.getValueLocalized()).thenReturn(Optional.of("Spülen"));
        when(programPhaseMock.getValueRaw()).thenReturn(Optional.of(4));

        State state = mock(State.class);
        Status status = mock(Status.class);
        Device device = mock(Device.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getProgramPhase()).thenReturn(Optional.of(programPhaseMock));
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String programPhase = deviceState.getProgramPhase().get();
        int programPhaseRaw = deviceState.getProgramPhaseRaw().get();

        // then:
        assertEquals("Spülen", programPhase);
        assertEquals(4, programPhaseRaw);
    }

    @Test
    public void testReturnValuesWhenDryingStepIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getDryingStep()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> dryingTarget = deviceState.getDryingTarget();
        Optional<Integer> dryingTargetRaw = deviceState.getDryingTargetRaw();

        // then:
        assertFalse(dryingTarget.isPresent());
        assertFalse(dryingTargetRaw.isPresent());
    }

    @Test
    public void testReturnValuesWhenDryingStepValueLocalizedIsEmpty() {
        // given:
        DryingStep dryingStep = mock(DryingStep.class);
        when(dryingStep.getValueLocalized()).thenReturn(Optional.empty());
        when(dryingStep.getValueRaw()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getDryingStep()).thenReturn(Optional.of(dryingStep));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> dryingTarget = deviceState.getDryingTarget();
        Optional<Integer> dryingTargetRaw = deviceState.getDryingTargetRaw();

        // then:
        assertFalse(dryingTarget.isPresent());
        assertFalse(dryingTargetRaw.isPresent());
    }

    @Test
    public void testReturnValuesWhenDryingStepValueLocalizedIsNotNull() {
        // given:
        DryingStep dryingStep = mock(DryingStep.class);
        when(dryingStep.getValueLocalized()).thenReturn(Optional.of("Hot"));
        when(dryingStep.getValueRaw()).thenReturn(Optional.of(5));

        State state = mock(State.class);
        Device device = mock(Device.class);
        Status status = mock(Status.class);
        when(state.getDryingStep()).thenReturn(Optional.of(dryingStep));
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String dryingTarget = deviceState.getDryingTarget().get();
        int dryingTargetRaw = deviceState.getDryingTargetRaw().get();

        // then:
        assertEquals("Hot", dryingTarget);
        assertEquals(5, dryingTargetRaw);
    }

    @Test
    public void testReturnValuesPreHeatFinishedWhenStateIsNotRunning() {
        // given:
        Temperature targetTemperature = mock(Temperature.class);
        when(targetTemperature.getValueLocalized()).thenReturn(Optional.of(0));
        Temperature currentTemperature = mock(Temperature.class);
        when(currentTemperature.getValueLocalized()).thenReturn(Optional.of(0));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getTargetTemperature()).thenReturn(Arrays.asList(targetTemperature));
        when(state.getTemperature()).thenReturn(Arrays.asList(currentTemperature));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> hasPreHeatFinished = deviceState.hasPreHeatFinished();

        // then:
        assertFalse(hasPreHeatFinished.get());
    }

    @Test
    public void testReturnValuesPreHeatFinishedWhenTargetTemperatureIsEmpty() {
        // given:
        Temperature targetTemperature = mock(Temperature.class);
        when(targetTemperature.getValueLocalized()).thenReturn(Optional.empty());
        Temperature currentTemperature = mock(Temperature.class);
        when(currentTemperature.getValueLocalized()).thenReturn(Optional.of(180));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));

        State state = mock(State.class);
        when(state.getTargetTemperature()).thenReturn(Arrays.asList(targetTemperature));
        when(state.getTemperature()).thenReturn(Arrays.asList(currentTemperature));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> hasPreHeatFinished = deviceState.hasPreHeatFinished();

        // then:
        assertFalse(hasPreHeatFinished.isPresent());
    }

    @Test
    public void testReturnValuesPreHeatFinishedWhenCurrentTemperatureIsEmpty() {
        // given:
        Temperature targetTemperature = mock(Temperature.class);
        when(targetTemperature.getValueLocalized()).thenReturn(Optional.of(180));
        Temperature currentTemperature = mock(Temperature.class);
        when(currentTemperature.getValueLocalized()).thenReturn(Optional.empty());

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));

        State state = mock(State.class);
        when(state.getTargetTemperature()).thenReturn(Arrays.asList(targetTemperature));
        when(state.getTemperature()).thenReturn(Arrays.asList(currentTemperature));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> hasPreHeatFinished = deviceState.hasPreHeatFinished();

        // then:
        assertFalse(hasPreHeatFinished.isPresent());
    }

    @Test
    public void testReturnValuesPreHeatFinishedWhenPreHeatingHasFinished() {
        // given:
        Temperature targetTemperature = mock(Temperature.class);
        when(targetTemperature.getValueLocalized()).thenReturn(Optional.of(180));
        Temperature currentTemperature = mock(Temperature.class);
        when(currentTemperature.getValueLocalized()).thenReturn(Optional.of(180));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));

        State state = mock(State.class);
        when(state.getTargetTemperature()).thenReturn(Arrays.asList(targetTemperature));
        when(state.getTemperature()).thenReturn(Arrays.asList(currentTemperature));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> hasPreHeatFinished = deviceState.hasPreHeatFinished();

        // then:
        assertTrue(hasPreHeatFinished.get());
    }

    @Test
    public void testReturnValuesPreHeatFinishedWhenPreHeatingHasNotFinished() {
        // given:
        Temperature targetTemperature = mock(Temperature.class);
        when(targetTemperature.getValueLocalized()).thenReturn(Optional.of(180));
        Temperature currentTemperature = mock(Temperature.class);
        when(currentTemperature.getValueLocalized()).thenReturn(Optional.of(179));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));

        State state = mock(State.class);
        when(state.getTargetTemperature()).thenReturn(Arrays.asList(targetTemperature));
        when(state.getTemperature()).thenReturn(Arrays.asList(currentTemperature));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> hasPreHeatFinished = deviceState.hasPreHeatFinished();

        // then:
        assertFalse(hasPreHeatFinished.get());
    }

    @Test
    public void testReturnValuesWhenTargetTemperatureIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getTargetTemperature()).thenReturn(Collections.emptyList());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> targetTemperature = deviceState.getTargetTemperature(0);

        // then:
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testReturnValuesWhenTargetTemperatureIndexIsOutOfRange() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getTargetTemperature()).thenReturn(new LinkedList<>());
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> targetTemperature = deviceState.getTargetTemperature(0);

        // then:
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testReturnValuesWhenTargetTemperatureValueLocalizedIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        Temperature temperature = mock(Temperature.class);
        when(temperature.getValueLocalized()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getTargetTemperature()).thenReturn(Arrays.asList(temperature));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> targetTemperature = deviceState.getTargetTemperature(0);

        // then:
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testReturnValuesWhenTargetTemperatureValueLocalizedIsValid() {
        // given:
        Temperature temperature = mock(Temperature.class);
        when(temperature.getValueLocalized()).thenReturn(Optional.of(20));

        State state = mock(State.class);
        Status status = mock(Status.class);
        Device device = mock(Device.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getTargetTemperature()).thenReturn(Arrays.asList(temperature));
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Integer targetTemperature = deviceState.getTargetTemperature(0).get();

        // then:
        assertEquals(Integer.valueOf(20), targetTemperature);
    }

    @Test
    public void testReturnValuesWhenTemperatureIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getTemperature()).thenReturn(Collections.emptyList());
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> temperature = deviceState.getTemperature(0);

        // then:
        assertFalse(temperature.isPresent());
    }

    @Test
    public void testReturnValuesWhenVentilationStepIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getVentilationStep()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> ventilationStep = deviceState.getVentilationStep();
        Optional<Integer> ventilationStepRaw = deviceState.getVentilationStepRaw();

        // then:
        assertFalse(ventilationStep.isPresent());
        assertFalse(ventilationStepRaw.isPresent());
    }

    @Test
    public void testReturnValuesWhenTemperatureIndexIsOutOfRange() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getTemperature()).thenReturn(new LinkedList<>());
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> temperature = deviceState.getTemperature(-1);

        // then:
        assertFalse(temperature.isPresent());
    }

    @Test
    public void testReturnValuesWhenTemperatureValueLocalizedIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        Temperature temperatureMock = mock(Temperature.class);
        when(temperatureMock.getValueLocalized()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getTemperature()).thenReturn(Arrays.asList(temperatureMock));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> temperature = deviceState.getTemperature(0);

        // then:
        assertFalse(temperature.isPresent());
    }

    @Test
    public void testReturnValuesWhenTemperatureValueLocalizedIsValid() {
        // given:
        Temperature temperatureMock = mock(Temperature.class);
        when(temperatureMock.getValueLocalized()).thenReturn(Optional.of(10));

        State state = mock(State.class);
        Device device = mock(Device.class);
        Status status = mock(Status.class);
        when(state.getTemperature()).thenReturn(Arrays.asList(temperatureMock));
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Integer temperature = deviceState.getTemperature(0).get();

        // then:
        assertEquals(Integer.valueOf(10), temperature);
    }

    @Test
    public void testReturnValuesWhenPlatStepIndexIsOutOfRange() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getPlateStep()).thenReturn(Collections.emptyList());
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        int plateStepCount = deviceState.getPlateStepCount().get();
        Optional<String> plateStep = deviceState.getPlateStep(0);
        Optional<Integer> plateStepRaw = deviceState.getPlateStepRaw(0);

        // then:
        assertEquals(0, plateStepCount);
        assertFalse(plateStep.isPresent());
        assertFalse(plateStepRaw.isPresent());
    }

    @Test
    public void testReturnValuesWhenPlateStepValueIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        PlateStep plateStepMock = mock(PlateStep.class);
        when(plateStepMock.getValueRaw()).thenReturn(Optional.empty());
        when(plateStepMock.getValueLocalized()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getPlateStep()).thenReturn(List.of(plateStepMock));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        int plateStepCount = deviceState.getPlateStepCount().get();
        Optional<String> plateStep = deviceState.getPlateStep(0);
        Optional<Integer> plateStepRaw = deviceState.getPlateStepRaw(0);

        // then:
        assertEquals(1, plateStepCount);
        assertFalse(plateStep.isPresent());
        assertFalse(plateStepRaw.isPresent());
    }

    @Test
    public void testReturnValuesWhenPlateStepValueIsValid() {
        // given:
        PlateStep plateStepMock = mock(PlateStep.class);
        when(plateStepMock.getValueRaw()).thenReturn(Optional.of(2));
        when(plateStepMock.getValueLocalized()).thenReturn(Optional.of("1."));

        State state = mock(State.class);
        Status status = mock(Status.class);
        Device device = mock(Device.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getPlateStep()).thenReturn(Arrays.asList(plateStepMock));
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        int plateStepCount = deviceState.getPlateStepCount().get();
        String plateStep = deviceState.getPlateStep(0).get();
        int plateStepRaw = deviceState.getPlateStepRaw(0).get();

        // then:
        assertEquals(1, plateStepCount);
        assertEquals("1.", plateStep);
        assertEquals(2, plateStepRaw);
    }

    @Test
    public void testReturnValuesWhenRemainingTimeIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getRemainingTime()).thenReturn(Optional.empty());
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(0, 0)));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> progress = deviceState.getProgress();

        // then:
        assertFalse(progress.isPresent());
    }

    @Test
    public void testReturnValuesWhenRemainingTimeSizeIsNotTwo() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getRemainingTime()).thenReturn(Optional.of(Arrays.asList(2)));
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(0, 0)));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> progress = deviceState.getProgress();

        // then:
        assertFalse(progress.isPresent());
    }

    @Test
    public void testReturnValuesWhenRemoteEnableIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getRemoteEnable()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> remoteControlEnabled = deviceState.isRemoteControlEnabled();

        // then:
        assertFalse(remoteControlEnabled.isPresent());
    }

    @Test
    public void testReturnValuesWhenFullRemoteControlIsEmpty() {
        // given:
        RemoteEnable remoteEnable = mock(RemoteEnable.class);
        when(remoteEnable.getFullRemoteControl()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getRemoteEnable()).thenReturn(Optional.of(remoteEnable));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> remoteControlEnabled = deviceState.isRemoteControlEnabled();

        // then:
        assertFalse(remoteControlEnabled.isPresent());
    }

    @Test
    public void testReturnValuesWhenFullRemoteControlIsNotNull() {
        // given:
        RemoteEnable remoteEnable = mock(RemoteEnable.class);
        when(remoteEnable.getFullRemoteControl()).thenReturn(Optional.of(true));

        State state = mock(State.class);
        when(state.getRemoteEnable()).thenReturn(Optional.of(remoteEnable));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Boolean remoteControlEnabled = deviceState.isRemoteControlEnabled().get();

        // then:
        assertTrue(remoteControlEnabled);
    }

    @Test
    public void testReturnValuesWhenElapsedTimeIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getElapsedTime()).thenReturn(Optional.empty());
        when(state.getRemainingTime()).thenReturn(Optional.of(Arrays.asList(0, 0)));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> progress = deviceState.getProgress();

        // then:
        assertFalse(progress.isPresent());
    }

    @Test
    public void testReturnValuesWhenElapsedTimeSizeIsNotTwo() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(0)));
        when(state.getRemainingTime()).thenReturn(Optional.of(Arrays.asList(0, 0)));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> progress = deviceState.getProgress();

        // then:
        assertFalse(progress.isPresent());
    }

    @Test
    public void testReturnValuesWhenElapsedTimeAndRemainingTimeIsZero() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(0, 0)));
        when(state.getRemainingTime()).thenReturn(Optional.of(Arrays.asList(0, 0)));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> progress = deviceState.getProgress();

        // then:
        assertFalse(progress.isPresent());
    }

    @Test
    public void whenElapsedTimeIsNotPresentThenEmptyIsReturned() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getElapsedTime()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> elapsedTime = deviceState.getElapsedTime();

        // then:
        assertFalse(elapsedTime.isPresent());
    }

    @Test
    public void whenElapsedTimeIsAnEmptyListThenEmptyIsReturned() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getElapsedTime()).thenReturn(Optional.of(Collections.emptyList()));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> elapsedTime = deviceState.getElapsedTime();

        // then:
        assertFalse(elapsedTime.isPresent());
    }

    @Test
    public void whenElapsedTimeHasOnlyOneElementThenEmptyIsReturned() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getElapsedTime()).thenReturn(Optional.of(List.of(2)));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> elapsedTime = deviceState.getElapsedTime();

        // then:
        assertFalse(elapsedTime.isPresent());
    }

    @Test
    public void whenElapsedTimeHasThreeElementsThenEmptyIsReturned() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(1, 2, 3)));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> elapsedTime = deviceState.getElapsedTime();

        // then:
        assertFalse(elapsedTime.isPresent());
    }

    @Test
    public void whenElapsedTimeHasTwoElementsThenTheTotalNumberOfSecondsIsReturned() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(1, 2)));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> elapsedTime = deviceState.getElapsedTime();

        // then:
        assertTrue(elapsedTime.isPresent());
        assertEquals(Integer.valueOf((60 + 2) * 60), elapsedTime.get());
    }

    @Test
    public void whenDeviceIsInOffStateThenElapsedTimeIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.OFF.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(1, 2)));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Integer> elapsedTime = deviceState.getElapsedTime();

        // then:
        assertFalse(elapsedTime.isPresent());
    }

    @Test
    public void testReturnValuesWhenProgressIs50Percent() {
        // given:
        State state = mock(State.class);
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(0, 45)));
        when(state.getRemainingTime()).thenReturn(Optional.of(Arrays.asList(0, 45)));

        Device device = mock(Device.class);
        Status status = mock(Status.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Integer progress = deviceState.getProgress().get();

        // then:
        assertEquals(Integer.valueOf(50), progress);
    }

    @Test
    public void testReturnValuesWhenProgressIs25Percent() {
        // given:
        State state = mock(State.class);
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(0, 15)));
        when(state.getRemainingTime()).thenReturn(Optional.of(Arrays.asList(0, 45)));

        Device device = mock(Device.class);
        Status status = mock(Status.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Integer progress = deviceState.getProgress().get();

        // then:
        assertEquals(Integer.valueOf(25), progress);
    }

    @Test
    public void testReturnValuesWhenProgressIs0Percent() {
        // given:
        State state = mock(State.class);
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(0, 0)));
        when(state.getRemainingTime()).thenReturn(Optional.of(Arrays.asList(0, 45)));

        Device device = mock(Device.class);
        Status status = mock(Status.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Integer progress = deviceState.getProgress().get();

        // then:
        assertEquals(Integer.valueOf(0), progress);
    }

    @Test
    public void testReturnValuesWhenSignalDoorIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getSignalDoor()).thenReturn(Optional.empty());

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> doorState = deviceState.getDoorState();
        Optional<Boolean> doorAlarm = deviceState.getDoorAlarm();

        // then:
        assertFalse(doorState.isPresent());
        assertFalse(doorAlarm.isPresent());
    }

    @Test
    public void testReturnValuesWhenSignalDoorIsTrue() {
        // given:
        State state = mock(State.class);
        when(state.getSignalDoor()).thenReturn(Optional.of(true));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> doorState = deviceState.getDoorState();

        // then:
        assertTrue(doorState.get());
    }

    @Test
    public void testReturnValuesWhenSignalDoorIsFalse() {
        // given:
        State state = mock(State.class);
        when(state.getSignalDoor()).thenReturn(Optional.of(false));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> doorState = deviceState.getDoorState();

        // then:
        assertFalse(doorState.get());
    }

    @Test
    public void testReturnValuesWhenSignalFailureIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getSignalDoor()).thenReturn(Optional.of(true));
        when(state.getSignalFailure()).thenReturn(Optional.empty());

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> doorAlarm = deviceState.getDoorAlarm();

        // then:
        assertFalse(doorAlarm.isPresent());
    }

    @Test
    public void testReturnValuesWhenDoorAlarmIsActive() {
        // given:
        State state = mock(State.class);
        when(state.getSignalDoor()).thenReturn(Optional.of(true));
        when(state.getSignalFailure()).thenReturn(Optional.of(true));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> doorAlarm = deviceState.getDoorAlarm();

        // then:
        assertTrue(doorAlarm.get());
    }

    @Test
    public void testReturnValuesWhenDoorAlarmIsNotActiveBecauseOfNoDoorSignal() {
        // given:
        State state = mock(State.class);
        when(state.getSignalDoor()).thenReturn(Optional.of(false));
        when(state.getSignalFailure()).thenReturn(Optional.of(true));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> doorAlarm = deviceState.getDoorAlarm();

        // then:
        assertFalse(doorAlarm.get());
    }

    @Test
    public void testReturnValuesWhenDoorAlarmIsNotActiveBecauseOfNoFailureSignal() {
        // given:
        State state = mock(State.class);
        when(state.getSignalDoor()).thenReturn(Optional.of(true));
        when(state.getSignalFailure()).thenReturn(Optional.of(false));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> doorAlarm = deviceState.getDoorAlarm();

        // then:
        assertFalse(doorAlarm.get());
    }

    @Test
    public void testReturnValuesWhenIdentIsEmpty() {
        // given:
        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.empty());
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> type = deviceState.getType();
        DeviceType rawType = deviceState.getRawType();
        Optional<String> deviceName = deviceState.getDeviceName();
        Optional<String> fabNumber = deviceState.getFabNumber();
        Optional<String> techType = deviceState.getTechType();

        // then:
        assertFalse(type.isPresent());
        assertEquals(DeviceType.UNKNOWN, rawType);
        assertFalse(deviceName.isPresent());
        assertFalse(fabNumber.isPresent());
        assertFalse(techType.isPresent());
    }

    @Test
    public void testReturnValuesWhenTypeIsEmpty() {
        // given:
        Ident ident = mock(Ident.class);
        when(ident.getType()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> type = deviceState.getType();
        DeviceType rawType = deviceState.getRawType();

        // then:
        assertFalse(type.isPresent());
        assertEquals(DeviceType.UNKNOWN, rawType);
    }

    @Test
    public void testReturnValuesWhenTypeValueLocalizedIsEmpty() {
        // given:
        Type typeMock = mock(Type.class);
        when(typeMock.getValueLocalized()).thenReturn(Optional.empty());

        Ident ident = mock(Ident.class);
        when(ident.getType()).thenReturn(Optional.of(typeMock));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> type = deviceState.getType();

        // then:
        assertFalse(type.isPresent());
    }

    @Test
    public void testReturnValuesWhenTypeValueLocalizedIsNotNull() {
        // given:
        Type typeMock = mock(Type.class);
        when(typeMock.getValueLocalized()).thenReturn(Optional.of("Hood"));

        Ident ident = mock(Ident.class);
        when(ident.getType()).thenReturn(Optional.of(typeMock));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String type = deviceState.getType().get();

        // then:
        assertEquals("Hood", type);
    }

    @Test
    public void testReturnValuesWhenTypeValueRawIsNotNull() {
        // given:
        Type typeMock = mock(Type.class);
        when(typeMock.getValueRaw()).thenReturn(DeviceType.COFFEE_SYSTEM);

        Ident ident = mock(Ident.class);
        when(ident.getType()).thenReturn(Optional.of(typeMock));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        DeviceType rawType = deviceState.getRawType();

        // then:
        assertEquals(DeviceType.COFFEE_SYSTEM, rawType);
    }

    @Test
    public void testReturnValuesWhenDeviceNameIsEmpty() {
        // given:
        Ident ident = mock(Ident.class);
        when(ident.getDeviceName()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> deviceName = deviceState.getDeviceName();

        // then:
        assertFalse(deviceName.isPresent());
    }

    @Test
    public void testReturnValuesWhenDeviceNameIsEmptyString() {
        // given:
        Ident ident = mock(Ident.class);
        when(ident.getDeviceName()).thenReturn(Optional.of(""));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> deviceName = deviceState.getDeviceName();

        // then:
        assertFalse(deviceName.isPresent());
    }

    @Test
    public void testReturnValuesWhenDeviceNameIsValid() {
        // given:
        Ident ident = mock(Ident.class);
        when(ident.getDeviceName()).thenReturn(Optional.of("MyWashingMachine"));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> deviceName = deviceState.getDeviceName();

        // then:
        assertEquals(Optional.of("MyWashingMachine"), deviceName);
    }

    @Test
    public void testReturnValuesWhenFabNumberIsNotNull() {
        // given:
        DeviceIdentLabel deviceIdentLabel = mock(DeviceIdentLabel.class);
        when(deviceIdentLabel.getFabNumber()).thenReturn(Optional.of("000061431659"));

        Ident ident = mock(Ident.class);
        when(ident.getDeviceIdentLabel()).thenReturn(Optional.of(deviceIdentLabel));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String fabNumber = deviceState.getFabNumber().get();

        // then:
        assertEquals("000061431659", fabNumber);
    }

    @Test
    public void testReturnValuesWhenTechTypeIsNotNull() {
        // given:
        DeviceIdentLabel deviceIdentLabel = mock(DeviceIdentLabel.class);
        when(deviceIdentLabel.getTechType()).thenReturn(Optional.of("XKM3100WEC"));

        Ident ident = mock(Ident.class);
        when(ident.getDeviceIdentLabel()).thenReturn(Optional.of(deviceIdentLabel));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String techType = deviceState.getTechType().get();

        // then:
        assertEquals("XKM3100WEC", techType);
    }

    @Test
    public void whenDeviceIsInFailureStateThenItHasAnError() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.FAILURE.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        boolean hasError = deviceState.hasError();

        // then:
        assertTrue(hasError);
    }

    @Test
    public void whenDeviceIsInRunningStateAndDoesNotSignalAFailureThenItHasNoError() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        boolean hasError = deviceState.hasError();

        // then:
        assertFalse(hasError);
    }

    @Test
    public void whenDeviceSignalsAFailureThenItHasAnError() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.RUNNING.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getSignalFailure()).thenReturn(Optional.of(true));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        boolean hasError = deviceState.hasError();

        // then:
        assertTrue(hasError);
    }

    @Test
    public void testReturnValuesForHasInfoWhenSignalInfoIsEmpty() {
        // given:
        State state = mock(State.class);
        when(state.getSignalInfo()).thenReturn(Optional.empty());

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        boolean hasInfo = deviceState.hasInfo();

        // then:
        assertFalse(hasInfo);
    }

    @Test
    public void whenDeviceSignalsAnInfoThenItHasAnInfo() {
        // given:
        State state = mock(State.class);
        when(state.getSignalInfo()).thenReturn(Optional.of(true));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        boolean hasInfo = deviceState.hasInfo();

        // then:
        assertTrue(hasInfo);
    }

    @Test
    public void whenDeviceSignalsNoInfoThenItHasNoInfo() {
        // given:
        State state = mock(State.class);
        when(state.getSignalInfo()).thenReturn(Optional.of(false));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        boolean hasInfo = deviceState.hasInfo();

        // then:
        assertFalse(hasInfo);
    }

    @Test
    public void testReturnValuesForVentilationStep() {
        // given:
        VentilationStep ventilationStepMock = mock(VentilationStep.class);
        when(ventilationStepMock.getValueLocalized()).thenReturn(Optional.of("Step 1"));
        when(ventilationStepMock.getValueRaw()).thenReturn(Optional.of(1));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getVentilationStep()).thenReturn(Optional.of(ventilationStepMock));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String ventilationStep = deviceState.getVentilationStep().get();
        int ventilationStepRaw = deviceState.getVentilationStepRaw().get();

        // then:
        assertEquals("Step 1", ventilationStep);
        assertEquals(1, ventilationStepRaw);
    }

    @Test
    public void testProgramPhaseWhenDeviceIsInOffState() {
        // given:
        ProgramPhase programPhase = mock(ProgramPhase.class);
        when(programPhase.getValueLocalized()).thenReturn(Optional.of("Washing"));
        when(programPhase.getValueRaw()).thenReturn(Optional.of(3));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.OFF.getCode()));

        State state = mock(State.class);
        when(state.getProgramPhase()).thenReturn(Optional.of(programPhase));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> phase = deviceState.getProgramPhase();
        Optional<Integer> phaseRaw = deviceState.getProgramPhaseRaw();

        // then:
        assertFalse(phase.isPresent());
        assertFalse(phaseRaw.isPresent());
    }

    @Test
    public void testDryingTargetWhenDeviceIsInOffState() {
        // given:
        DryingStep dryingStep = mock(DryingStep.class);
        when(dryingStep.getValueLocalized()).thenReturn(Optional.of("Schranktrocken"));
        when(dryingStep.getValueRaw()).thenReturn(Optional.of(3));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.OFF.getCode()));

        State state = mock(State.class);
        when(state.getDryingStep()).thenReturn(Optional.of(dryingStep));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> dryingTarget = deviceState.getDryingTarget();
        Optional<Integer> dryingTargetRaw = deviceState.getDryingTargetRaw();

        // then:
        assertFalse(dryingTarget.isPresent());
        assertFalse(dryingTargetRaw.isPresent());
    }

    @Test
    public void testVentilationStepWhenDeviceIsInOffState() {
        // given:
        VentilationStep ventilationStep = mock(VentilationStep.class);
        when(ventilationStep.getValueLocalized()).thenReturn(Optional.of("Stufe 1"));
        when(ventilationStep.getValueRaw()).thenReturn(Optional.of(1));

        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.OFF.getCode()));

        State state = mock(State.class);
        when(state.getVentilationStep()).thenReturn(Optional.of(ventilationStep));
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> step = deviceState.getVentilationStep();
        Optional<Integer> stepRaw = deviceState.getVentilationStepRaw();

        // then:
        assertFalse(step.isPresent());
        assertFalse(stepRaw.isPresent());
    }

    @Test
    public void testReturnValuesWhenDeviceIsInOffState() {
        // given:
        Device device = mock(Device.class);
        State state = mock(State.class);
        Status status = mock(Status.class);

        when(device.getState()).thenReturn(Optional.of(state));
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.OFF.getCode()));
        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // Test SelectedProgram:
        ProgramId programId = mock(ProgramId.class);
        when(state.getProgramId()).thenReturn(Optional.of(programId));
        when(programId.getValueLocalized()).thenReturn(Optional.of("Washing"));
        // when:
        Optional<String> selectedProgram = deviceState.getSelectedProgram();
        // then:
        assertFalse(selectedProgram.isPresent());

        // Test TargetTemperature:
        Temperature targetTemperatureMock = mock(Temperature.class);
        when(state.getTargetTemperature()).thenReturn(List.of(targetTemperatureMock));
        when(targetTemperatureMock.getValueLocalized()).thenReturn(Optional.of(200));
        // when:
        Optional<Integer> targetTemperature = deviceState.getTargetTemperature(0);
        // then:
        assertFalse(targetTemperature.isPresent());

        // Test Temperature:
        Temperature temperature = mock(Temperature.class);
        when(state.getTemperature()).thenReturn(List.of(temperature));
        when(temperature.getValueLocalized()).thenReturn(Optional.of(200));
        // when:
        Optional<Integer> t = deviceState.getTemperature(0);
        // then:
        assertFalse(t.isPresent());

        // Test Progress:
        when(state.getElapsedTime()).thenReturn(Optional.of(Arrays.asList(0, 5)));
        when(state.getRemainingTime()).thenReturn(Optional.of(Arrays.asList(1, 5)));
        // when:
        Optional<Integer> progress = deviceState.getProgress();
        // then:
        assertFalse(progress.isPresent());
    }

    @Test
    public void testWhenDeviceIsInOffStateThenGetSpinningSpeedReturnsNull() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.OFF.getCode()));

        SpinningSpeed spinningSpeed = mock(SpinningSpeed.class);
        when(spinningSpeed.getValueRaw()).thenReturn(Optional.of(800));
        when(spinningSpeed.getValueLocalized()).thenReturn(Optional.of("800"));
        when(spinningSpeed.getUnit()).thenReturn(Optional.of("rpm"));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getSpinningSpeed()).thenReturn(Optional.of(spinningSpeed));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> speed = deviceState.getSpinningSpeed();
        Optional<Integer> speedRaw = deviceState.getSpinningSpeedRaw();

        // then:
        assertFalse(speed.isPresent());
        assertFalse(speedRaw.isPresent());
    }

    @Test
    public void testGetSpinningSpeedReturnsNullWhenSpinningSpeedIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getSpinningSpeed()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> spinningSpeed = deviceState.getSpinningSpeed();
        Optional<Integer> spinningSpeedRaw = deviceState.getSpinningSpeedRaw();

        // then:
        assertFalse(spinningSpeed.isPresent());
        assertFalse(spinningSpeedRaw.isPresent());
    }

    @Test
    public void testGetSpinningSpeedReturnsNullWhenSpinningSpeedRawValueIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        SpinningSpeed spinningSpeedMock = mock(SpinningSpeed.class);
        when(spinningSpeedMock.getValueRaw()).thenReturn(Optional.empty());
        when(spinningSpeedMock.getValueLocalized()).thenReturn(Optional.of("1200"));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getSpinningSpeed()).thenReturn(Optional.of(spinningSpeedMock));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<String> spinningSpeed = deviceState.getSpinningSpeed();
        Optional<Integer> spinningSpeedRaw = deviceState.getSpinningSpeedRaw();

        // then:
        assertFalse(spinningSpeed.isPresent());
        assertFalse(spinningSpeedRaw.isPresent());
    }

    @Test
    public void testGetSpinningSpeedReturnsValidValueWhenSpinningSpeedRawValueIsNotNull() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        SpinningSpeed spinningSpeedMock = mock(SpinningSpeed.class);
        when(spinningSpeedMock.getValueRaw()).thenReturn(Optional.of(1200));
        when(spinningSpeedMock.getValueLocalized()).thenReturn(Optional.of("1200"));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getSpinningSpeed()).thenReturn(Optional.of(spinningSpeedMock));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        String spinningSpeed = deviceState.getSpinningSpeed().get();
        int spinningSpeedRaw = deviceState.getSpinningSpeedRaw().get();

        // then:
        assertEquals("1200", spinningSpeed);
        assertEquals(1200, spinningSpeedRaw);
    }

    @Test
    public void testGetLightStateWhenDeviceIsOff() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.OFF.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> lightState = deviceState.getLightState();

        // then:
        assertFalse(lightState.isPresent());
    }

    @Test
    public void testGetLightStateWhenLightIsUnknown() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getLight()).thenReturn(Light.UNKNOWN);

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> lightState = deviceState.getLightState();

        // then:
        assertFalse(lightState.isPresent());
    }

    @Test
    public void testGetLightStateWhenLightIsEnabled() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getLight()).thenReturn(Light.ENABLE);

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Boolean lightState = deviceState.getLightState().get();

        // then:
        assertEquals(Boolean.valueOf(true), lightState);
    }

    @Test
    public void testGetLightStateWhenLightIsDisabled() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getLight()).thenReturn(Light.DISABLE);

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Boolean lightState = deviceState.getLightState().get();

        // then:
        assertEquals(Boolean.valueOf(false), lightState);
    }

    @Test
    public void testGetLightStateWhenLightIsNotSupported() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getLight()).thenReturn(Light.NOT_SUPPORTED);

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Boolean> lightState = deviceState.getLightState();

        // then:
        assertFalse(lightState.isPresent());
    }

    @Test
    public void testGetCurrentWaterConsumptionWhenEcoFeedbackIsNotPresent() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Quantity> waterConsumption = deviceState.getCurrentWaterConsumption();

        // then:
        assertFalse(waterConsumption.isPresent());
    }

    @Test
    public void testGetCurrentWaterConsumptionWhenCurrentWaterConsumptionIsNotPresent() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentWaterConsumption()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Quantity> waterConsumption = deviceState.getCurrentWaterConsumption();

        // then:
        assertFalse(waterConsumption.isPresent());
    }

    @Test
    public void testGetCurrentWaterConsumptionWhenCurrentWaterConsumptionIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        WaterConsumption currentWaterConsumption = mock(WaterConsumption.class);
        when(currentWaterConsumption.getUnit()).thenReturn(Optional.empty());
        when(currentWaterConsumption.getValue()).thenReturn(Optional.empty());

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentWaterConsumption()).thenReturn(Optional.of(currentWaterConsumption));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Quantity> waterConsumption = deviceState.getCurrentWaterConsumption();

        // then:
        assertFalse(waterConsumption.isPresent());
    }

    @Test
    public void testGetCurrentWaterConsumptionWhenValueIsNotPresent() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        WaterConsumption currentWaterConsumption = mock(WaterConsumption.class);
        when(currentWaterConsumption.getUnit()).thenReturn(Optional.of("l"));
        when(currentWaterConsumption.getValue()).thenReturn(Optional.empty());

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentWaterConsumption()).thenReturn(Optional.of(currentWaterConsumption));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Quantity> waterConsumption = deviceState.getCurrentWaterConsumption();

        // then:
        assertFalse(waterConsumption.isPresent());
    }

    @Test
    public void testGetCurrentWaterConsumptionWhenUnitIsNotPresent() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        WaterConsumption currentWaterConsumption = mock(WaterConsumption.class);
        when(currentWaterConsumption.getUnit()).thenReturn(Optional.empty());
        when(currentWaterConsumption.getValue()).thenReturn(Optional.of(0.5));

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentWaterConsumption()).thenReturn(Optional.of(currentWaterConsumption));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Quantity waterConsumption = deviceState.getCurrentWaterConsumption().get();

        // then:
        assertEquals(0.5, waterConsumption.getValue());
        assertFalse(waterConsumption.getUnit().isPresent());
    }

    @Test
    public void testGetCurrentWaterConsumption() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        WaterConsumption currentWaterConsumption = mock(WaterConsumption.class);
        when(currentWaterConsumption.getUnit()).thenReturn(Optional.of("l"));
        when(currentWaterConsumption.getValue()).thenReturn(Optional.of(0.5));

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentWaterConsumption()).thenReturn(Optional.of(currentWaterConsumption));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Quantity waterConsumption = deviceState.getCurrentWaterConsumption().get();

        // then:
        assertEquals(0.5, waterConsumption.getValue());
        assertEquals(Optional.of("l"), waterConsumption.getUnit());
    }

    @Test
    public void testGetCurrentEnergyConsumptionWhenEcoFeedbackIsNotPresent() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.empty());

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Quantity> energyConsumption = deviceState.getCurrentEnergyConsumption();

        // then:
        assertFalse(energyConsumption.isPresent());
    }

    @Test
    public void testGetCurrentEnergyConsumptionWhenCurrentEnergyConsumptionIsNotPresent() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentEnergyConsumption()).thenReturn(Optional.empty());

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Quantity> energyConsumption = deviceState.getCurrentEnergyConsumption();

        // then:
        assertFalse(energyConsumption.isPresent());
    }

    @Test
    public void testGetCurrentEnergyConsumptionWhenCurrentEnergyConsumptionIsEmpty() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        EnergyConsumption currentEnergyConsumption = mock(EnergyConsumption.class);
        when(currentEnergyConsumption.getUnit()).thenReturn(Optional.empty());
        when(currentEnergyConsumption.getValue()).thenReturn(Optional.empty());

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentEnergyConsumption()).thenReturn(Optional.of(currentEnergyConsumption));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Quantity> energyConsumption = deviceState.getCurrentEnergyConsumption();

        // then:
        assertFalse(energyConsumption.isPresent());
    }

    @Test
    public void testGetCurrentEnergyConsumptionWhenValueIsNotPresent() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        EnergyConsumption currentEnergyConsumption = mock(EnergyConsumption.class);
        when(currentEnergyConsumption.getUnit()).thenReturn(Optional.of("kWh"));
        when(currentEnergyConsumption.getValue()).thenReturn(Optional.empty());

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentEnergyConsumption()).thenReturn(Optional.of(currentEnergyConsumption));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Optional<Quantity> energyConsumption = deviceState.getCurrentEnergyConsumption();

        // then:
        assertFalse(energyConsumption.isPresent());
    }

    @Test
    public void testGetCurrentEnergyConsumptionWhenUnitIsNotPresent() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        EnergyConsumption currentEnergyConsumption = mock(EnergyConsumption.class);
        when(currentEnergyConsumption.getUnit()).thenReturn(Optional.empty());
        when(currentEnergyConsumption.getValue()).thenReturn(Optional.of(0.5));

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentEnergyConsumption()).thenReturn(Optional.of(currentEnergyConsumption));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Quantity energyConsumption = deviceState.getCurrentEnergyConsumption().get();

        // then:
        assertEquals(0.5, energyConsumption.getValue());
        assertFalse(energyConsumption.getUnit().isPresent());
    }

    @Test
    public void testGetCurrentEnergyConsumption() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        EnergyConsumption currentEnergyConsumption = mock(EnergyConsumption.class);
        when(currentEnergyConsumption.getUnit()).thenReturn(Optional.of("kWh"));
        when(currentEnergyConsumption.getValue()).thenReturn(Optional.of(0.5));

        EcoFeedback ecoFeedback = mock(EcoFeedback.class);
        when(ecoFeedback.getCurrentEnergyConsumption()).thenReturn(Optional.of(currentEnergyConsumption));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getEcoFeedback()).thenReturn(Optional.of(ecoFeedback));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Quantity energyConsumption = deviceState.getCurrentEnergyConsumption().get();

        // then:
        assertEquals(0.5, energyConsumption.getValue());
        assertEquals(Optional.of("kWh"), energyConsumption.getUnit());
    }

    @Test
    public void testGetBatteryLevel() {
        // given:
        Status status = mock(Status.class);
        when(status.getValueRaw()).thenReturn(Optional.of(StateType.ON.getCode()));

        State state = mock(State.class);
        when(state.getStatus()).thenReturn(Optional.of(status));
        when(state.getBatteryLevel()).thenReturn(Optional.of(4));

        Device device = mock(Device.class);
        when(device.getState()).thenReturn(Optional.of(state));

        DeviceState deviceState = new DeviceState(DEVICE_IDENTIFIER, device);

        // when:
        Integer batteryLevel = deviceState.getBatteryLevel().get();

        // then:
        assertEquals(Integer.valueOf(4), batteryLevel);
    }
}
