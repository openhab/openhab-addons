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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Device;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceIdentLabel;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DryingStep;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Ident;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Light;
import org.openhab.binding.mielecloud.internal.webservice.api.json.PlateStep;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProgramId;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProgramPhase;
import org.openhab.binding.mielecloud.internal.webservice.api.json.RemoteEnable;
import org.openhab.binding.mielecloud.internal.webservice.api.json.SpinningSpeed;
import org.openhab.binding.mielecloud.internal.webservice.api.json.State;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Status;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Temperature;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Type;
import org.openhab.binding.mielecloud.internal.webservice.api.json.VentilationStep;

/**
 * This immutable class provides methods to extract the device state information in a comfortable way.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Introduced null handling
 * @author Benjamin Bolte - Add pre-heat finished, plate step, door state, door alarm, info state channel and map signal
 *         flags from API
 * @author Björn Lange - Add elapsed time channel, dish warmer and robotic vacuum cleaner things
 */
@NonNullByDefault
public class DeviceState {

    private final String deviceIdentifier;

    private final Optional<Device> device;

    public DeviceState(String deviceIdentifier, @Nullable Device device) {
        this.deviceIdentifier = deviceIdentifier;
        this.device = Optional.ofNullable(device);
    }

    /**
     * Gets the unique identifier for this device.
     *
     * @return The unique identifier for this device.
     */
    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    /**
     * Gets the main operation status of the device.
     *
     * @return The main operation status of the device.
     */
    public Optional<String> getStatus() {
        return device.flatMap(Device::getState).flatMap(State::getStatus).flatMap(Status::getValueLocalized);
    }

    /**
     * Gets the raw main operation status of the device.
     *
     * @return The raw main operation status of the device.
     */
    public Optional<Integer> getStatusRaw() {
        return device.flatMap(Device::getState).flatMap(State::getStatus).flatMap(Status::getValueRaw);
    }

    /**
     * Gets the raw operation status of the device parsed to a {@link StateType}.
     *
     * @return The raw operation status of the device parsed to a {@link StateType}.
     */
    public Optional<StateType> getStateType() {
        return device.flatMap(Device::getState).flatMap(State::getStatus).flatMap(Status::getValueRaw)
                .flatMap(StateType::fromCode);
    }

    /**
     * Gets the currently selected program type of the device.
     *
     * @return The currently selected program type of the device.
     */
    public Optional<String> getSelectedProgram() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getProgramId).flatMap(ProgramId::getValueLocalized);
    }

    /**
     * Gets the selected program ID.
     *
     * @return The selected program ID.
     */
    public Optional<Long> getSelectedProgramId() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getProgramId).flatMap(ProgramId::getValueRaw);
    }

    /**
     * Gets the currently active phase of the active program.
     *
     * @return The currently active phase of the active program.
     */
    public Optional<String> getProgramPhase() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getProgramPhase)
                .flatMap(ProgramPhase::getValueLocalized);
    }

    /**
     * Gets the currently active raw phase of the active program.
     *
     * @return The currently active raw phase of the active program.
     */
    public Optional<Integer> getProgramPhaseRaw() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getProgramPhase).flatMap(ProgramPhase::getValueRaw);
    }

    /**
     * Gets the currently selected drying step.
     *
     * @return The currently selected drying step.
     */
    public Optional<String> getDryingTarget() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getDryingStep).flatMap(DryingStep::getValueLocalized);
    }

    /**
     * Gets the currently selected raw drying step.
     *
     * @return The currently selected raw drying step.
     */
    public Optional<Integer> getDryingTargetRaw() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getDryingStep).flatMap(DryingStep::getValueRaw);
    }

    /**
     * Calculates if pre-heating the oven has finished.
     *
     * @return Whether pre-heating the oven has finished.
     */
    public Optional<Boolean> hasPreHeatFinished() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }

        Optional<Integer> targetTemperature = getTargetTemperature(0);
        Optional<Integer> currentTemperature = getTemperature(0);

        if (!targetTemperature.isPresent() || !currentTemperature.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(isInState(StateType.RUNNING) && currentTemperature.get() >= targetTemperature.get());
    }

    /**
     * Gets the target temperature with the given index.
     *
     * @return The target temperature with the given index.
     */
    public Optional<Integer> getTargetTemperature(int index) {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).map(State::getTargetTemperature).flatMap(l -> getOrNull(l, index))
                .flatMap(Temperature::getValueLocalized);
    }

    /**
     * Gets the current temperature of the device for the given index.
     *
     * @param index The index of the device zone for which the temperature shall be obtained.
     * @return The target temperature if available.
     */
    public Optional<Integer> getTemperature(int index) {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }

        return device.flatMap(Device::getState).map(State::getTemperature).flatMap(l -> getOrNull(l, index))
                .flatMap(Temperature::getValueLocalized);
    }

    /**
     * Gets the remaining time of the active program.
     *
     * @return The remaining time in seconds.
     */
    public Optional<Integer> getRemainingTime() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getRemainingTime).flatMap(this::toSeconds);
    }

    /**
     * Gets the elapsed time of the active program.
     *
     * @return The elapsed time in seconds.
     */
    public Optional<Integer> getElapsedTime() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getElapsedTime).flatMap(this::toSeconds);
    }

    /**
     * Gets the relative start time of the active program.
     *
     * @return The delayed start time in seconds.
     */
    public Optional<Integer> getStartTime() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getStartTime).flatMap(this::toSeconds);
    }

    /**
     * Gets the "fullRemoteControl" state information of the device. If this flag is true ALL remote control actions
     * of the device can be triggered.
     *
     * @return Whether the device can be remote controlled.
     */
    public Optional<Boolean> isRemoteControlEnabled() {
        return device.flatMap(Device::getState).flatMap(State::getRemoteEnable)
                .flatMap(RemoteEnable::getFullRemoteControl);
    }

    /**
     * Calculates the program process.
     *
     * @return The progress of the active program in percent.
     */
    public Optional<Integer> getProgress() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }

        Optional<Double> elapsedTime = device.flatMap(Device::getState).flatMap(State::getElapsedTime)
                .flatMap(this::toSeconds).map(Integer::doubleValue);
        Optional<Double> remainingTime = device.flatMap(Device::getState).flatMap(State::getRemainingTime)
                .flatMap(this::toSeconds).map(Integer::doubleValue);

        if (elapsedTime.isPresent() && remainingTime.isPresent()
                && (elapsedTime.get() != 0 || remainingTime.get() != 0)) {
            return Optional.of((int) ((elapsedTime.get() / (elapsedTime.get() + remainingTime.get())) * 100.0));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Integer> toSeconds(List<Integer> time) {
        if (time.size() != 2) {
            return Optional.empty();
        }
        return Optional.of((time.get(0) * 60 + time.get(1)) * 60);
    }

    /**
     * Gets the spinning speed.
     *
     * @return The spinning speed.
     */
    public Optional<String> getSpinningSpeed() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getSpinningSpeed).flatMap(SpinningSpeed::getValueRaw)
                .map(String::valueOf);
    }

    /**
     * Gets the raw spinning speed.
     *
     * @return The raw spinning speed.
     */
    public Optional<Integer> getSpinningSpeedRaw() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getSpinningSpeed).flatMap(SpinningSpeed::getValueRaw);
    }

    /**
     * Gets the ventilation step.
     *
     * @return The ventilation step.
     */
    public Optional<String> getVentilationStep() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getVentilationStep)
                .flatMap(VentilationStep::getValueLocalized).map(Object::toString);
    }

    /**
     * Gets the raw ventilation step.
     *
     * @return The raw ventilation step.
     */
    public Optional<Integer> getVentilationStepRaw() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).flatMap(State::getVentilationStep)
                .flatMap(VentilationStep::getValueRaw);
    }

    /**
     * Gets the plate power step of the device for the given index.
     *
     * @param index The index of the device plate for which the power step shall be obtained.
     * @return The plate power step if available.
     */
    public Optional<String> getPlateStep(int index) {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).map(State::getPlateStep).flatMap(l -> getOrNull(l, index))
                .flatMap(PlateStep::getValueLocalized);
    }

    /**
     * Gets the raw plate power step of the device for the given index.
     *
     * @param index The index of the device plate for which the power step shall be obtained.
     * @return The raw plate power step if available.
     */
    public Optional<Integer> getPlateStepRaw(int index) {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }
        return device.flatMap(Device::getState).map(State::getPlateStep).flatMap(l -> getOrNull(l, index))
                .flatMap(PlateStep::getValueRaw);
    }

    /**
     * Gets the number of available plate steps.
     *
     * @return The number of available plate steps.
     */
    public Optional<Integer> getPlateStepCount() {
        return device.flatMap(Device::getState).map(State::getPlateStep).map(List::size);
    }

    /**
     * Indicates if the device has an error that requires a user action.
     *
     * @return Whether the device has an error that requires a user action.
     */
    public boolean hasError() {
        return isInState(StateType.FAILURE)
                || device.flatMap(Device::getState).flatMap(State::getSignalFailure).orElse(false);
    }

    /**
     * Indicates if the device has a user information.
     *
     * @return Whether the device has a user information.
     */
    public boolean hasInfo() {
        if (deviceIsInOffState()) {
            return false;
        }
        return device.flatMap(Device::getState).flatMap(State::getSignalInfo).orElse(false);
    }

    /**
     * Gets the state of the light attached to the device.
     *
     * @return An {@link Optional} with value {@code true} if the light is turned on, {@code false} if the light is
     *         turned off or an empty {@link Optional} if light is not supported or no state is available.
     */
    public Optional<Boolean> getLightState() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }

        Optional<Light> light = device.flatMap(Device::getState).map(State::getLight);
        if (light.isPresent()) {
            if (light.get().equals(Light.ENABLE)) {
                return Optional.of(true);
            } else if (light.get().equals(Light.DISABLE)) {
                return Optional.of(false);
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the state of the door attached to the device.
     *
     * @return Whether the device door is open.
     */
    public Optional<Boolean> getDoorState() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }

        return device.flatMap(Device::getState).flatMap(State::getSignalDoor);
    }

    /**
     * Gets the state of the device's door alarm.
     *
     * @return Whether the device door alarm was triggered.
     */
    public Optional<Boolean> getDoorAlarm() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }

        Optional<Boolean> doorState = getDoorState();
        Optional<Boolean> failure = device.flatMap(Device::getState).flatMap(State::getSignalFailure);

        if (!doorState.isPresent() || !failure.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(doorState.get() && failure.get());
    }

    /**
     * Gets the battery level.
     *
     * @return The battery level.
     */
    public Optional<Integer> getBatteryLevel() {
        if (deviceIsInOffState()) {
            return Optional.empty();
        }

        return device.flatMap(Device::getState).flatMap(State::getBatteryLevel);
    }

    /**
     * Gets the device type.
     *
     * @return The device type as human readable value.
     */
    public Optional<String> getType() {
        return device.flatMap(Device::getIdent).flatMap(Ident::getType).flatMap(Type::getValueLocalized)
                .filter(type -> !type.isEmpty());
    }

    /**
     * Gets the raw device type.
     *
     * @return The raw device type.
     */
    public DeviceType getRawType() {
        return device.flatMap(Device::getIdent).flatMap(Ident::getType).map(Type::getValueRaw)
                .orElse(DeviceType.UNKNOWN);
    }

    /**
     * Gets the user-defined name of the device.
     *
     * @return The user-defined name of the device.
     */
    public Optional<String> getDeviceName() {
        return device.flatMap(Device::getIdent).flatMap(Ident::getDeviceName).filter(name -> !name.isEmpty());
    }

    /**
     * Gets the fabrication (=serial) number of the device.
     *
     * @return The serial number of the device.
     */
    public Optional<String> getFabNumber() {
        return device.flatMap(Device::getIdent).flatMap(Ident::getDeviceIdentLabel)
                .flatMap(DeviceIdentLabel::getFabNumber).filter(fabNumber -> !fabNumber.isEmpty());
    }

    /**
     * Gets the tech type of the device.
     *
     * @return The tech type of the device.
     */
    public Optional<String> getTechType() {
        return device.flatMap(Device::getIdent).flatMap(Ident::getDeviceIdentLabel)
                .flatMap(DeviceIdentLabel::getTechType).filter(techType -> !techType.isEmpty());
    }

    private <T> Optional<T> getOrNull(List<T> list, int index) {
        if (index < 0 || index >= list.size()) {
            return Optional.empty();
        }

        return Optional.ofNullable(list.get(index));
    }

    private boolean deviceIsInOffState() {
        return getStateType().map(StateType.OFF::equals).orElse(true);
    }

    public boolean isInState(StateType stateType) {
        return getStateType().map(stateType::equals).orElse(false);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, deviceIdentifier);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviceState other = (DeviceState) obj;
        return Objects.equals(device, other.device) && Objects.equals(deviceIdentifier, other.deviceIdentifier);
    }
}
