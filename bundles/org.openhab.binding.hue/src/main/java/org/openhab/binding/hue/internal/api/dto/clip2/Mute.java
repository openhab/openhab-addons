/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.MuteType;

/**
 * DTO for mute state of a sound.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Mute {
    private @Nullable String mute;

    public @Nullable MuteType getMuteType() {
        return mute instanceof String m ? MuteType.of(m) : null;
    }

    public Mute setMuteType(MuteType muteType) {
        mute = muteType.name().toLowerCase();
        return this;
    }
}
