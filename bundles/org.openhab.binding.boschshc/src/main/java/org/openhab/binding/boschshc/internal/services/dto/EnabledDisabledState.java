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
package org.openhab.binding.boschshc.internal.services.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

/**
 * State that is serialized as either <code>ENABLED</code> or <code>DISABLED</code>.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public enum EnabledDisabledState {
    ENABLED,
    DISABLED;

    /**
     * Converts an {@link OnOffType} state into a {@link EnabledDisabledState}.
     * 
     * @param onOff the on/off state
     * @return the corresponding enabled/disabled state
     */
    public static EnabledDisabledState from(OnOffType onOff) {
        return onOff == OnOffType.ON ? ENABLED : DISABLED;
    }

    /**
     * Converts this {@link EnabledDisabledState} into an {@link OnOffType}.
     * 
     * @return the on/off state corresponding to the enabled/disabled state of this enumeration literal
     */
    public OnOffType toOnOffType() {
        return OnOffType.from(this == ENABLED);
    }
}
