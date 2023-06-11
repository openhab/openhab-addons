/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nuvo.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the different kinds of commands
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum NuvoCommand {
    GET_CONTROLLER_VERSION("VER"),
    ALLMUTE_ON("MUTE1"),
    ALLMUTE_OFF("MUTE0"),
    ALLOFF("ALLOFF"),
    PAGE_ON("PAGE1"),
    PAGE_OFF("PAGE0"),
    CFGTIME("CFGTIME"),
    STATUS("STATUS"),
    EQ_QUERY("EQ?"),
    DISPINFO("DISPINFO"),
    DISPLINE("DISPLINE"),
    DISPLINE1("DISPLINE1"),
    DISPLINE2("DISPLINE2"),
    DISPLINE3("DISPLINE3"),
    DISPLINE4("DISPLINE4"),
    NAME("NAME"),
    ON("ON"),
    OFF("OFF"),
    SOURCE("SRC"),
    FAVORITE("FAV"),
    VOLUME("VOL"),
    MUTE_ON("MUTEON"),
    MUTE_OFF("MUTEOFF"),
    TREBLE("TREB"),
    BASS("BASS"),
    BALANCE("BAL"),
    LOUDNESS("LOUDCMP"),
    PLAYPAUSE("PLAYPAUSE"),
    PREV("PREV"),
    NEXT("NEXT"),
    DND_ON("DNDON"),
    DND_OFF("DNDOFF"),
    PARTY_ON("PARTY1"),
    PARTY_OFF("PARTY0");

    private final String value;

    NuvoCommand(String value) {
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
