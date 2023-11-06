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
package org.openhab.binding.heos.internal.json.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enum to reference the attributes of the HEOS response
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public enum HeosCommunicationAttribute {
    COMMAND_UNDER_PROCESS("command under process"),
    COUNT("count"),
    CURRENT_POSITION("cur_pos"),
    DURATION("duration"),
    ERROR_ID("eid"),
    GROUP_ID("gid"),
    LEVEL("level"),
    MUTE("mute"),
    PLAYER_ID("pid"),
    REPEAT("repeat"),
    RETURNED("returned"),
    SHUFFLE("shuffle"),
    SIGNED_IN("signed_in"),
    SOURCE_ID("sid"),
    STATE("state"),
    SYSTEM_ERROR_NUMBER("syserrno"),
    USERNAME("un"),
    ERROR("error"),
    TEXT("text");

    private final String label;

    HeosCommunicationAttribute(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
