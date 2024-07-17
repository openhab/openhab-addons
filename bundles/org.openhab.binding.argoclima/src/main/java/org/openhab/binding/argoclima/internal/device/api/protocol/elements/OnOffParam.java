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
package org.openhab.binding.argoclima.internal.device.api.protocol.elements;

import java.security.InvalidParameterException;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.ArgoDeviceStatus;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The API element representing ON/OFF knob
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class OnOffParam extends ArgoApiElementBase {
    private Optional<Boolean> currentValue = Optional.empty();
    private static final String VALUE_ON = "1";
    private static final String VALUE_OFF = "0";

    /**
     * C-tor
     *
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     */
    public OnOffParam(IArgoSettingProvider settingsProvider) {
        super(settingsProvider);
    }

    private static State valueToState(Optional<Boolean> value) {
        return Objects.requireNonNull(value.<State> map(v -> OnOffType.from(v)).orElse(UnDefType.UNDEF));
    }

    @Override
    protected void updateFromApiResponseInternal(String responseValue) {
        if (OnOffParam.VALUE_ON.equals(responseValue)) {
            this.currentValue = Optional.of(true);
        } else if (OnOffParam.VALUE_OFF.equals(responseValue)) {
            this.currentValue = Optional.of(false);
        } else if (ArgoDeviceStatus.NO_VALUE.equals(responseValue)) {
            this.currentValue = Optional.empty();
        } else {
            throw new InvalidParameterException(String.format("Invalid value of parameter: {}", responseValue));
        }
    }

    @Override
    public State toState() {
        return valueToState(currentValue);
    }

    @Override
    public String toString() {
        if (currentValue.isEmpty()) {
            return "???";
        }
        return currentValue.get() ? "ON" : "OFF";
    }

    @Override
    protected HandleCommandResult handleCommandInternalEx(Command command) {
        if (command instanceof OnOffType onOffTypeCommand) {
            if (OnOffType.ON.equals(onOffTypeCommand)) {
                var targetValue = Optional.of(true);
                currentValue = targetValue;
                return HandleCommandResult.accepted(VALUE_ON, valueToState(targetValue));
            } else if (OnOffType.OFF.equals(onOffTypeCommand)) {
                var targetValue = Optional.of(false);
                currentValue = targetValue;
                return HandleCommandResult.accepted(VALUE_OFF, valueToState(targetValue));
            }
        }
        return HandleCommandResult.rejected();
    }
}
