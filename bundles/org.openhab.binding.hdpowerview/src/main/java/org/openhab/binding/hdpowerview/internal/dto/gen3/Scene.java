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
package org.openhab.binding.hdpowerview.internal.dto.gen3;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO for a scene as returned by an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public class Scene {
    private int id;
    private @NonNullByDefault({}) String name;
    private @NonNullByDefault({}) String ptName;
    private @NonNullByDefault({}) String color;
    private @NonNullByDefault({}) String icon;
    private @NonNullByDefault({}) List<Integer> roomIds;

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return String.join(" ", new String(Base64.getDecoder().decode(name), StandardCharsets.UTF_8), ptName);
    }

    public String getPtName() {
        return ptName;
    }

    public List<Integer> getRoomIds() {
        List<Integer> roomIds = this.roomIds;
        return roomIds != null ? roomIds : List.of();
    }
}
