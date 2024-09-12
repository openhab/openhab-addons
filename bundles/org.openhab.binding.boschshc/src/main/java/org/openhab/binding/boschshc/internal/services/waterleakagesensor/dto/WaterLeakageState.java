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
package org.openhab.binding.boschshc.internal.services.waterleakagesensor.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

/**
 * Possible states of the water leakage sensor.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public enum WaterLeakageState {
    NO_LEAKAGE,
    LEAKAGE_DETECTED;

    public OnOffType toOnOffType() {
        return OnOffType.from(this == LEAKAGE_DETECTED);
    }
}
