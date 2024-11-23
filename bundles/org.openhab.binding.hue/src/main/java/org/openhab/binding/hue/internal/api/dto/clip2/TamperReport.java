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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.TamperStateType;

/**
 * DTO for CLIP 2 home security tamper switch.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class TamperReport {

    private @NonNullByDefault({}) Instant changed;
    private @NonNullByDefault({}) String state;

    public Instant getLastChanged() {
        return changed;
    }

    public TamperStateType getTamperState() throws IllegalArgumentException {
        return TamperStateType.valueOf(state.toUpperCase());
    }

    public TamperReport setLastChanged(Instant changed) {
        this.changed = changed;
        return this;
    }

    public TamperReport setTamperState(String state) {
        this.state = state;
        return this;
    }
}
