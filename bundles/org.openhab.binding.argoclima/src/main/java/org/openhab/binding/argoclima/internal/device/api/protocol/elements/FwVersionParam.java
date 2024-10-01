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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Read-only element communicating the unit firmware version
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class FwVersionParam extends ArgoApiElementBase {
    private Optional<String> currentValue = Optional.empty();

    /**
     * C-tor
     *
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     */
    public FwVersionParam(IArgoSettingProvider settingsProvider) {
        super(settingsProvider);
    }

    private static State valueToState(Optional<String> value) {
        if (value.isEmpty()) {
            return UnDefType.UNDEF;
        }
        return new StringType("0" + value.get());
    }

    @Override
    protected void updateFromApiResponseInternal(String responseValue) {
        this.currentValue = Optional.of(responseValue);
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
        return "0" + currentValue.get();
    }

    @Override
    protected HandleCommandResult handleCommandInternalEx(Command command) {
        return HandleCommandResult.rejected(); // this is not handling any commands
    }
}
