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
package org.openhab.binding.boschshc.internal.services.displayedtemperatureconfiguration.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

/**
 * Possible values for displayed temperature configurations.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public enum DisplayedTemperatureState {
    MEASURED,
    SETPOINT;

    public OnOffType toOnOffCommand() {
        return OnOffType.from(this == SETPOINT);
    }

    public static DisplayedTemperatureState fromOnOffCommand(OnOffType onOffCommand) {
        return onOffCommand == OnOffType.ON ? SETPOINT : MEASURED;
    }
}
