/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.monopriceaudio.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents the different kinds of commands
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum MonopriceAudioCommand {

    QUERY("?"),
    BEGIN_CMD("<"),
    END_CMD("\r"),

    POWER_ON("PR01"),
    POWER_OFF("PR00"),
    SOURCE("CH"),
    VOLUME("VO"),
    MUTE_ON("MU01"),
    MUTE_OFF("MU00"),
    TREBLE("TR"),
    BASS("BS"),
    BALANCE("BL"),
    DND_ON("DT01"),
    DND_OFF("DT00");

    private @Nullable String value;

    MonopriceAudioCommand(String value) {
        this.value = value;
    }

    /**
     * Get the command name
     *
     * @return the command name
     */
    public @Nullable String getValue() {
        return value;
    }
}
