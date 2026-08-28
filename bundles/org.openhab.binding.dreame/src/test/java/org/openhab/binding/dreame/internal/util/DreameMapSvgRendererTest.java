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
package org.openhab.binding.dreame.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameMapGeometry;
import org.openhab.binding.dreame.internal.model.DreameMapPoint;
import org.openhab.binding.dreame.internal.model.DreameMapZoneGeometry;
import org.openhab.binding.dreame.internal.model.DreameMowerPose;

@NonNullByDefault
class DreameMapSvgRendererTest {

    private static final DreameMapGeometry GEOMETRY = new DreameMapGeometry(1, 0, 0, 100, 200,
            List.of(new DreameMapZoneGeometry(1, "Garden",
                    List.of(new DreameMapPoint(0, 0), new DreameMapPoint(100, 0), new DreameMapPoint(100, 200)))),
            List.of(), List.of());

    @Test
    void preservesDreameOrientation() {
        String svg = DreameMapSvgRenderer.render(List.of(GEOMETRY), 1, new DreameMowerPose(20, 30, 40), false);

        assertNotNull(svg);
        assertTrue(svg.contains("viewBox=\"-300 -500 700 800\""));
        assertTrue(svg.contains("points=\"100,-200 0,-200 0,0 "));
        assertTrue(svg.contains("translate(80 -170) rotate(140.0)"));
    }

    @Test
    void rotatesMovaMapClockwise() {
        String svg = DreameMapSvgRenderer.render(List.of(GEOMETRY), 1, new DreameMowerPose(20, 30, 40), true);

        assertNotNull(svg);
        assertTrue(svg.contains("viewBox=\"-300 -300 800 700\""));
        assertTrue(svg.contains("points=\"200,100 200,0 0,0 "));
        assertTrue(svg.contains("translate(170 80) rotate(230.0)"));
    }
}
