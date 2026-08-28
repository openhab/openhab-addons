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
package org.openhab.binding.dreame.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Boundary and mowing-zone polygons for one map.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public record DreameMapGeometry(int mapId, int minX, int minY, int maxX, int maxY, List<DreameMapZoneGeometry> zones,
        List<DreameMapZoneGeometry> forbiddenAreas, List<DreameMapPathGeometry> paths) {
    public DreameMapGeometry {
        zones = List.copyOf(zones);
        forbiddenAreas = List.copyOf(forbiddenAreas);
        paths = List.copyOf(paths);
    }
}
