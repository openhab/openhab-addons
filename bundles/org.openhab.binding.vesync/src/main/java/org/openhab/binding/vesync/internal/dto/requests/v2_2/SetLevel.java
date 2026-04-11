/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal.dto.requests.v2_2;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SetLevel} class is used as a DTO to hold a payload for the
 * managed device bypass requests to set a type of switch to a certain level. e.g. fan to 3
 *
 * @author David Goodyear - Initial contribution
 */
public class SetLevel extends EmptyPayload {

    public SetLevel(final int id, final String type, final int level) {
        this.id = id;
        this.type = type;
        this.level = level;
    }

    @SerializedName("id")
    public int id = -1;

    @SerializedName("level")
    public int level = -1;

    @SerializedName("type")
    public String type = "";
}
