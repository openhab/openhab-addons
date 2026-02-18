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
package org.openhab.binding.roborock.internal.map;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Parsed Roborock RR map content.
 *
 * @author OpenHAB project contribution
 */
@NonNullByDefault
public record RRMapData(int imageWidth, int imageHeight, int top, int left, byte[] imageData, @Nullable Integer robotX,
        @Nullable Integer robotY, @Nullable Integer robotAngle, @Nullable Integer chargerX, @Nullable Integer chargerY,
        List<MapPoint> basicPath, List<MapZone> cleanedZones) {

    public record MapPoint(int x, int y) {
    }

    public record MapZone(int x0, int y0, int x1, int y1) {
    }
}
