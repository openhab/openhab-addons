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
package org.openhab.binding.nest.internal.sdm.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A resource name uniquely identifies a structure, room or device.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMResourceName {

    public enum SDMResourceNameType {
        DEVICE,
        ROOM,
        STRUCTURE,
        UNKNOWN
    }

    private static final Pattern PATTERN = Pattern
            .compile("^enterprises/([^/]+)(/devices/([^/]+)|/structures/([^/]+)(/rooms/([^/]+))?)$");

    public static final SDMResourceName NAMELESS = new SDMResourceName("");

    public final String name;
    public final String projectId;
    public final String deviceId;
    public final String structureId;
    public final String roomId;
    public final SDMResourceNameType type;

    public SDMResourceName(String name) {
        this.name = name;

        Matcher matcher = PATTERN.matcher(name);
        if (matcher.matches()) {
            projectId = matcher.group(1);
            deviceId = matcher.group(3) == null ? "" : matcher.group(3);
            structureId = matcher.group(4) == null ? "" : matcher.group(4);
            roomId = matcher.group(6) == null ? "" : matcher.group(6);

            if (!deviceId.isEmpty()) {
                type = SDMResourceNameType.DEVICE;
            } else if (!roomId.isEmpty()) {
                type = SDMResourceNameType.ROOM;
            } else if (!structureId.isEmpty()) {
                type = SDMResourceNameType.STRUCTURE;
            } else {
                type = SDMResourceNameType.UNKNOWN;
            }
        } else {
            projectId = "";
            deviceId = "";
            structureId = "";
            roomId = "";
            type = SDMResourceNameType.UNKNOWN;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return name.equals(((SDMResourceName) obj).name);
    }

    @Override
    public String toString() {
        return name;
    }
}
