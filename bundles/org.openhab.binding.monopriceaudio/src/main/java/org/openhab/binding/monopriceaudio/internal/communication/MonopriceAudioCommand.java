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
package org.openhab.binding.monopriceaudio.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the different kinds of commands
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum MonopriceAudioCommand {
    QUERY("?"),
    POWER("PR"),
    SOURCE("CH"),
    VOLUME("VO"),
    MUTE("MU"),
    TREBLE("TR"),
    BASS("BS"),
    BALANCE("BL"),
    DND("DT");

    private final String value;

    MonopriceAudioCommand(String value) {
        this.value = value;
    }

    /**
     * Get the command name
     *
     * @return the command name
     */
    public String getValue() {
        return value;
    }
}
