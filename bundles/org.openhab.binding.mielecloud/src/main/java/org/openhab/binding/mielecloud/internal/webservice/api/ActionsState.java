/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.mielecloud.internal.webservice.api.json.Actions;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Light;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;

/**
 * Provides convenient access to the list of actions that can be performed with a device.
 *
 * @author Roland Edelhoff - Initial contribution
 */
@NonNullByDefault
public class ActionsState {

    private final String deviceIdentifier;
    private final Optional<Actions> actions;

    public ActionsState(String deviceIdentifier, @Nullable Actions actions) {
        this.deviceIdentifier = deviceIdentifier;
        this.actions = Optional.ofNullable(actions);
    }

    /**
     * Gets the unique identifier of the device to which this state refers.
     */
    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    /**
     * Gets whether the device can be started.
     */
    public boolean canBeStarted() {
        return actions.map(Actions::getProcessAction).map(a -> a.contains(ProcessAction.START)).orElse(false);
    }

    /**
     * Gets whether the device can be stopped.
     */
    public boolean canBeStopped() {
        return actions.map(Actions::getProcessAction).map(a -> a.contains(ProcessAction.STOP)).orElse(false);
    }

    /**
     * Gets whether the device can be paused.
     */
    public boolean canBePaused() {
        return actions.map(Actions::getProcessAction).map(a -> a.contains(ProcessAction.PAUSE)).orElse(false);
    }

    /**
     * Gets whether supercooling can be controlled.
     */
    public boolean canContolSupercooling() {
        return canStartSupercooling() || canStopSupercooling();
    }

    /**
     * Gets whether supercooling can be started.
     */
    public boolean canStartSupercooling() {
        return actions.map(Actions::getProcessAction).map(a -> a.contains(ProcessAction.START_SUPERCOOLING))
                .orElse(false);
    }

    /**
     * Gets whether supercooling can be stopped.
     */
    public boolean canStopSupercooling() {
        return actions.map(Actions::getProcessAction).map(a -> a.contains(ProcessAction.STOP_SUPERCOOLING))
                .orElse(false);
    }

    /**
     * Gets whether superfreezing can be controlled.
     */
    public boolean canControlSuperfreezing() {
        return canStartSuperfreezing() || canStopSuperfreezing();
    }

    /**
     * Gets whether superfreezing can be started.
     */
    public boolean canStartSuperfreezing() {
        return actions.map(Actions::getProcessAction).map(a -> a.contains(ProcessAction.START_SUPERFREEZING))
                .orElse(false);
    }

    /**
     * Gets whether superfreezing can be stopped.
     */
    public boolean canStopSuperfreezing() {
        return actions.map(Actions::getProcessAction).map(a -> a.contains(ProcessAction.STOP_SUPERFREEZING))
                .orElse(false);
    }

    /**
     * Gets whether light can be enabled.
     */
    public boolean canEnableLight() {
        return actions.map(Actions::getLight).map(a -> a.contains(Light.ENABLE)).orElse(false);
    }

    /**
     * Gets whether light can be disabled.
     */
    public boolean canDisableLight() {
        return actions.map(Actions::getLight).map(a -> a.contains(Light.DISABLE)).orElse(false);
    }

    /**
     * Gets whether the device can be switched on.
     */
    public boolean canBeSwitchedOn() {
        return actions.flatMap(Actions::getPowerOn).map(Boolean.TRUE::equals).orElse(false);
    }

    /**
     * Gets whether the device can be switched off.
     */
    public boolean canBeSwitchedOff() {
        return actions.flatMap(Actions::getPowerOff).map(Boolean.TRUE::equals).orElse(false);
    }

    /**
     * Gets whether the light can be controlled.
     */
    public boolean canControlLight() {
        return canEnableLight() || canDisableLight();
    }

    /**
     * Gets whether the active program can be set.
     */
    public boolean canSetActiveProgramId() {
        return !actions.map(Actions::getProgramId).map(List::isEmpty).orElse(true);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actions, deviceIdentifier);
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
        ActionsState other = (ActionsState) obj;
        return Objects.equals(actions, other.actions) && Objects.equals(deviceIdentifier, other.deviceIdentifier);
    }
}
