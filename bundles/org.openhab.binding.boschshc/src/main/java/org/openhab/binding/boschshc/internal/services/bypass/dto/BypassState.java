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
package org.openhab.binding.boschshc.internal.services.bypass.dto;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * State indicating whether a device is currently bypassed.
 * 
 * @author David Pace - Initial contribution
 *
 */
public enum BypassState {
    BYPASS_INACTIVE,
    BYPASS_ACTIVE,
    UNKNOWN;

    /**
     * Converts this Bosch-specific bypass state to an openHAB-compliant state for a switch.
     * 
     * @return <code>ON</code>, <code>OFF</code> or <code>UNDEF</code>
     */
    public State toOnOffTypeOrUndef() {
        return switch (this) {
            case BYPASS_ACTIVE -> OnOffType.ON;
            case BYPASS_INACTIVE -> OnOffType.OFF;
            default -> UnDefType.UNDEF;
        };
    }
}
