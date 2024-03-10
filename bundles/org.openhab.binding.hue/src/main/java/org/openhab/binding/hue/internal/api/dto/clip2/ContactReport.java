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
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ContactStateType;

/**
 * DTO for CLIP 2 home security alarm contact.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ContactReport {

    private @NonNullByDefault({}) Instant changed;
    private @NonNullByDefault({}) String state;

    public ContactStateType getContactState() throws IllegalArgumentException {
        return ContactStateType.valueOf(state.toUpperCase());
    }

    public Instant getLastChanged() {
        return changed;
    }

    public ContactReport setLastChanged(Instant changed) {
        this.changed = changed;
        return this;
    }

    public ContactReport setContactState(String state) {
        this.state = state;
        return this;
    }
}
