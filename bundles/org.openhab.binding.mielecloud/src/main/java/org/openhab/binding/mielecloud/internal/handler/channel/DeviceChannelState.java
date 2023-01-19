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
package org.openhab.binding.mielecloud.internal.handler.channel;

import static org.openhab.binding.mielecloud.internal.webservice.api.PowerStatus.*;
import static org.openhab.binding.mielecloud.internal.webservice.api.ProgramStatus.*;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.CoolingDeviceTemperatureState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.PowerStatus;
import org.openhab.binding.mielecloud.internal.webservice.api.ProgramStatus;
import org.openhab.binding.mielecloud.internal.webservice.api.WineStorageDeviceTemperatureState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * Wrapper for {@link DeviceState} handling the type conversion to {@link State} for directly filling channels.
 *
 * @author Björn Lange - Initial contribution
 * @author Benjamin Bolte - Add pre-heat finished, plate step, door state, door alarm and info state channel and map
 *         signal flags from API
 * @author Björn Lange - Add elapsed time channel, dish warmer and robotic vacuum cleaner thing
 */
@NonNullByDefault
public final class DeviceChannelState {
    private final DeviceState device;
    private final CoolingDeviceTemperatureState coolingTemperature;
    private final WineStorageDeviceTemperatureState wineTemperature;

    public DeviceChannelState(DeviceState device) {
        this.device = device;
        this.coolingTemperature = new CoolingDeviceTemperatureState(device);
        this.wineTemperature = new WineStorageDeviceTemperatureState(device);
    }

    public State getLightSwitch() {
        return ChannelTypeUtil.booleanToState(device.getLightState());
    }

    public State getDoorState() {
        return ChannelTypeUtil.booleanToState(device.getDoorState());
    }

    public State getDoorAlarm() {
        return ChannelTypeUtil.booleanToState(device.getDoorAlarm());
    }

    public State getErrorState() {
        return OnOffType.from(device.hasError());
    }

    public State getInfoState() {
        return OnOffType.from(device.hasInfo());
    }

    public State getPowerOnOff() {
        return new StringType(getPowerStatus().getState());
    }

    public State getProgramElapsedTime() {
        return ChannelTypeUtil.intToState(device.getElapsedTime());
    }

    public State getOperationState() {
        return ChannelTypeUtil.stringToState(device.getStatus());
    }

    public State getOperationStateRaw() {
        return ChannelTypeUtil.intToState(device.getStatusRaw());
    }

    public State getProgramPhase() {
        return ChannelTypeUtil.stringToState(device.getProgramPhase());
    }

    public State getProgramPhaseRaw() {
        return ChannelTypeUtil.intToState(device.getProgramPhaseRaw());
    }

    public State getProgramActive() {
        return ChannelTypeUtil.stringToState(device.getSelectedProgram());
    }

    public State getProgramActiveRaw() {
        return ChannelTypeUtil.longToState(device.getSelectedProgramId());
    }

    public State getProgramActiveId() {
        return ChannelTypeUtil.stringToState(device.getSelectedProgramId().map(Object::toString));
    }

    public State getFridgeSuperCool() {
        return ChannelTypeUtil.booleanToState(isInState(StateType.SUPERCOOLING, StateType.SUPERCOOLING_SUPERFREEZING));
    }

    public State getFreezerSuperFreeze() {
        return ChannelTypeUtil.booleanToState(isInState(StateType.SUPERFREEZING, StateType.SUPERCOOLING_SUPERFREEZING));
    }

    public State getFridgeTemperatureTarget() {
        return ChannelTypeUtil.intToTemperatureState(coolingTemperature.getFridgeTargetTemperature());
    }

    public State getFreezerTemperatureTarget() {
        return ChannelTypeUtil.intToTemperatureState(coolingTemperature.getFreezerTargetTemperature());
    }

    public State getFridgeTemperatureCurrent() {
        return ChannelTypeUtil.intToTemperatureState(coolingTemperature.getFridgeTemperature());
    }

    public State getFreezerTemperatureCurrent() {
        return ChannelTypeUtil.intToTemperatureState(coolingTemperature.getFreezerTemperature());
    }

    public State getProgramStartStop() {
        return new StringType(getProgramStartStopStatus().getState());
    }

    public State getProgramStartStopPause() {
        return new StringType(getProgramStartStopPauseStatus().getState());
    }

    public State getDelayedStartTime() {
        return ChannelTypeUtil.intToState(device.getStartTime());
    }

    public State getDryingTarget() {
        return ChannelTypeUtil.stringToState(device.getDryingTarget());
    }

    public State getDryingTargetRaw() {
        return ChannelTypeUtil.intToState(device.getDryingTargetRaw());
    }

    public State hasPreHeatFinished() {
        return ChannelTypeUtil.booleanToState(device.hasPreHeatFinished());
    }

    public State getTemperatureTarget() {
        return ChannelTypeUtil.intToTemperatureState(device.getTargetTemperature(0));
    }

    public State getVentilationPower() {
        return ChannelTypeUtil.stringToState(device.getVentilationStep());
    }

    public State getVentilationPowerRaw() {
        return ChannelTypeUtil.intToState(device.getVentilationStepRaw());
    }

    public State getPlateStep(int index) {
        return ChannelTypeUtil.stringToState(device.getPlateStep(index));
    }

    public State getPlateStepRaw(int index) {
        return ChannelTypeUtil.intToState(device.getPlateStepRaw(index));
    }

    public State getTemperatureCurrent() {
        return ChannelTypeUtil.intToTemperatureState(device.getTemperature(0));
    }

    public State getSpinningSpeed() {
        return ChannelTypeUtil.stringToState(device.getSpinningSpeed());
    }

    public State getSpinningSpeedRaw() {
        return ChannelTypeUtil.intToState(device.getSpinningSpeedRaw());
    }

    public State getBatteryLevel() {
        return ChannelTypeUtil.intToState(device.getBatteryLevel());
    }

    public State getWineTemperatureTarget() {
        return ChannelTypeUtil.intToState(wineTemperature.getTargetTemperature());
    }

    public State getWineTemperatureCurrent() {
        return ChannelTypeUtil.intToTemperatureState(wineTemperature.getTemperature());
    }

    public State getWineTopTemperatureTarget() {
        return ChannelTypeUtil.intToTemperatureState(wineTemperature.getTopTargetTemperature());
    }

    public State getWineTopTemperatureCurrent() {
        return ChannelTypeUtil.intToTemperatureState(wineTemperature.getTopTemperature());
    }

    public State getWineMiddleTemperatureTarget() {
        return ChannelTypeUtil.intToTemperatureState(wineTemperature.getMiddleTargetTemperature());
    }

    public State getWineMiddleTemperatureCurrent() {
        return ChannelTypeUtil.intToTemperatureState(wineTemperature.getMiddleTemperature());
    }

    public State getWineBottomTemperatureTarget() {
        return ChannelTypeUtil.intToTemperatureState(wineTemperature.getBottomTargetTemperature());
    }

    public State getWineBottomTemperatureCurrent() {
        return ChannelTypeUtil.intToTemperatureState(wineTemperature.getBottomTemperature());
    }

    /**
     * Determines the status of the currently selected program.
     */
    private PowerStatus getPowerStatus() {
        if (device.isInState(StateType.OFF) || device.isInState(StateType.NOT_CONNECTED)) {
            return POWER_OFF;
        } else {
            return POWER_ON;
        }
    }

    /**
     * Determines the status of the currently selected program respecting the possibilities started and stopped.
     */
    protected ProgramStatus getProgramStartStopStatus() {
        if (device.isInState(StateType.RUNNING)) {
            return PROGRAM_STARTED;
        } else {
            return PROGRAM_STOPPED;
        }
    }

    /**
     * Determines the status of the currently selected program respecting the possibilities started, stopped and paused.
     */
    protected ProgramStatus getProgramStartStopPauseStatus() {
        if (device.isInState(StateType.RUNNING)) {
            return PROGRAM_STARTED;
        } else if (device.isInState(StateType.PAUSE)) {
            return PROGRAM_PAUSED;
        } else {
            return PROGRAM_STOPPED;
        }
    }

    /**
     * Gets whether the device is in one of the given states.
     *
     * @param stateType The states to check.
     * @return An empty {@link Optional} if the raw status is unknown, otherwise an {@link Optional} with a value
     *         indicating whether the device is in one of the given states.
     */
    private Optional<Boolean> isInState(StateType... stateType) {
        return device.getStateType().map(it -> Arrays.asList(stateType).contains(it));
    }
}
