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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable POJO representing the state of a device. Queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 * @author Benjamin Bolte - Add plate step
 * @author Björn Lange - Add elapsed time channel, add eco feedback
 */
@NonNullByDefault
public class State {
    @Nullable
    private Status status;
    /**
     * Currently used by Miele webservice.
     */
    @Nullable
    private ProgramId ProgramID;
    /**
     * Planned to be used in the future.
     */
    @Nullable
    private ProgramId programId;
    @Nullable
    private ProgramType programType;
    @Nullable
    private ProgramPhase programPhase;
    @Nullable
    private final List<Integer> remainingTime = null;
    @Nullable
    private final List<Integer> startTime = null;
    @Nullable
    private final List<Temperature> targetTemperature = null;
    @Nullable
    private final List<Temperature> temperature = null;
    @Nullable
    private Boolean signalInfo;
    @Nullable
    private Boolean signalFailure;
    @Nullable
    private Boolean signalDoor;
    @Nullable
    private RemoteEnable remoteEnable;
    @Nullable
    private Integer light;
    @Nullable
    private final List<Integer> elapsedTime = null;
    @Nullable
    private SpinningSpeed spinningSpeed;
    @Nullable
    private DryingStep dryingStep;
    @Nullable
    private VentilationStep ventilationStep;
    @Nullable
    private final List<PlateStep> plateStep = null;
    @Nullable
    private EcoFeedback ecoFeedback;
    @Nullable
    private Integer batteryLevel;

    public Optional<Status> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<ProgramId> getProgramId() {
        // There is a typo for the program ID in the Miele Cloud API, which will be corrected in the future.
        // For the sake of robustness, we currently support both upper and lower case.
        return Optional.ofNullable(programId != null ? programId : ProgramID);
    }

    public Optional<ProgramType> getProgramType() {
        return Optional.ofNullable(programType);
    }

    public Optional<ProgramPhase> getProgramPhase() {
        return Optional.ofNullable(programPhase);
    }

    /**
     * Gets the remaining time encoded as {@link List} of {@link Integer} values.
     *
     * @return The remaining time encoded as {@link List} of {@link Integer} values.
     */
    public Optional<List<Integer>> getRemainingTime() {
        if (remainingTime == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(Collections.unmodifiableList(remainingTime));
    }

    /**
     * Gets the start time encoded as {@link List} of {@link Integer} values.
     *
     * @return The start time encoded as {@link List} of {@link Integer} values.
     */
    public Optional<List<Integer>> getStartTime() {
        if (startTime == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(Collections.unmodifiableList(startTime));
    }

    public List<Temperature> getTargetTemperature() {
        if (targetTemperature == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(targetTemperature);
    }

    public List<Temperature> getTemperature() {
        if (temperature == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(temperature);
    }

    public Optional<Boolean> getSignalInfo() {
        return Optional.ofNullable(signalInfo);
    }

    public Optional<Boolean> getSignalFailure() {
        return Optional.ofNullable(signalFailure);
    }

    public Optional<Boolean> getSignalDoor() {
        return Optional.ofNullable(signalDoor);
    }

    public Optional<RemoteEnable> getRemoteEnable() {
        return Optional.ofNullable(remoteEnable);
    }

    public Light getLight() {
        return Light.fromId(light);
    }

    /**
     * Gets the elapsed time encoded as {@link List} of {@link Integer} values.
     *
     * @return The elapsed time encoded as {@link List} of {@link Integer} values.
     */
    public Optional<List<Integer>> getElapsedTime() {
        if (elapsedTime == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(Collections.unmodifiableList(elapsedTime));
    }

    public Optional<SpinningSpeed> getSpinningSpeed() {
        return Optional.ofNullable(spinningSpeed);
    }

    public Optional<DryingStep> getDryingStep() {
        return Optional.ofNullable(dryingStep);
    }

    public Optional<VentilationStep> getVentilationStep() {
        return Optional.ofNullable(ventilationStep);
    }

    public List<PlateStep> getPlateStep() {
        if (plateStep == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(plateStep);
    }

    public Optional<EcoFeedback> getEcoFeedback() {
        return Optional.ofNullable(ecoFeedback);
    }

    public Optional<Integer> getBatteryLevel() {
        return Optional.ofNullable(batteryLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dryingStep, elapsedTime, light, programPhase, ProgramID, programId, programType,
                remainingTime, remoteEnable, signalDoor, signalFailure, signalInfo, startTime, status,
                targetTemperature, temperature, ventilationStep, plateStep, ecoFeedback, batteryLevel);
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
        State other = (State) obj;
        return Objects.equals(dryingStep, other.dryingStep) && Objects.equals(elapsedTime, other.elapsedTime)
                && Objects.equals(light, other.light) && Objects.equals(programPhase, other.programPhase)
                && Objects.equals(ProgramID, other.ProgramID) && Objects.equals(programId, other.programId)
                && Objects.equals(programType, other.programType) && Objects.equals(remainingTime, other.remainingTime)
                && Objects.equals(remoteEnable, other.remoteEnable) && Objects.equals(signalDoor, other.signalDoor)
                && Objects.equals(signalFailure, other.signalFailure) && Objects.equals(signalInfo, other.signalInfo)
                && Objects.equals(startTime, other.startTime) && Objects.equals(status, other.status)
                && Objects.equals(targetTemperature, other.targetTemperature)
                && Objects.equals(temperature, other.temperature)
                && Objects.equals(ventilationStep, other.ventilationStep) && Objects.equals(plateStep, other.plateStep)
                && Objects.equals(ecoFeedback, other.ecoFeedback) && Objects.equals(batteryLevel, other.batteryLevel);
    }

    @Override
    public String toString() {
        return "State [status=" + status + ", programId=" + getProgramId() + ", programType=" + programType
                + ", programPhase=" + programPhase + ", remainingTime=" + remainingTime + ", startTime=" + startTime
                + ", targetTemperature=" + targetTemperature + ", temperature=" + temperature + ", signalInfo="
                + signalInfo + ", signalFailure=" + signalFailure + ", signalDoor=" + signalDoor + ", remoteEnable="
                + remoteEnable + ", light=" + light + ", elapsedTime=" + elapsedTime + ", dryingStep=" + dryingStep
                + ", ventilationStep=" + ventilationStep + ", plateStep=" + plateStep + ", ecoFeedback=" + ecoFeedback
                + ", batteryLevel=" + batteryLevel + "]";
    }
}
