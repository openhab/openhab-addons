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
package org.openhab.binding.boschshc.internal.services.privacymode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

/**
 * Possible privacy mode states of security cameras.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public enum PrivacyModeState {

    /**
     * Privacy mode enabled / camera disabled
     */
    ENABLED,

    /**
     * Privacy mode disabled / camera enabled
     */
    DISABLED;

    /**
     * Converts an {@link OnOffType} state into a {@link PrivacyModeState}.
     * 
     * @param onOff the on/off state
     * @return the corresponding privacy mode state
     */
    public static PrivacyModeState from(OnOffType onOff) {
        return onOff == OnOffType.ON ? ENABLED : DISABLED;
    }

    /**
     * Converts this {@link PrivacyModeState} into an {@link OnOffType}.
     * 
     * @return the on/off state corresponding to the privacy mode state of this enumeration literal
     */
    public OnOffType toOnOffType() {
        return this == ENABLED ? OnOffType.ON : OnOffType.OFF;
    }
}
