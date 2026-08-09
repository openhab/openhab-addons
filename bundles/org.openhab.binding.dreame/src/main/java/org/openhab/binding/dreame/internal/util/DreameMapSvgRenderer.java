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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.model.DreameMapGeometry;
import org.openhab.binding.dreame.internal.model.DreameMapPathGeometry;
import org.openhab.binding.dreame.internal.model.DreameMapPoint;
import org.openhab.binding.dreame.internal.model.DreameMapZoneGeometry;
import org.openhab.binding.dreame.internal.model.DreameMowerPose;

/** Renders Dreame vector map metadata without external graphics dependencies. */
@NonNullByDefault
public final class DreameMapSvgRenderer {
    private static final int PADDING = 300;

    private DreameMapSvgRenderer() {
    }

    public static @Nullable String render(List<DreameMapGeometry> geometries, int currentMapId,
            @Nullable DreameMowerPose pose) {
        DreameMapGeometry geometry = geometries.stream().filter(map -> map.mapId() == currentMapId).findFirst()
                .orElse(geometries.isEmpty() ? null : geometries.get(0));
        if (geometry == null || geometry.maxX() <= geometry.minX() || geometry.maxY() <= geometry.minY()) {
            return null;
        }
        int x = geometry.minX() - PADDING;
        int y = -geometry.maxY() - PADDING;
        int width = geometry.maxX() - geometry.minX() + 2 * PADDING;
        int height = geometry.maxY() - geometry.minY() + 2 * PADDING;
        StringBuilder svg = new StringBuilder(4096);
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"").append(x).append(' ').append(y).append(' ')
                .append(width).append(' ').append(height).append("\">").append("<rect x=\"").append(x).append("\" y=\"")
                .append(y).append("\" width=\"").append(width).append("\" height=\"").append(height)
                .append("\" fill=\"#eef3ee\"/>");
        svg.append(
                "<g fill=\"none\" stroke=\"#c58b2a\" stroke-width=\"90\" stroke-linecap=\"round\" stroke-dasharray=\"120 80\">");
        for (DreameMapPathGeometry path : geometry.paths()) {
            svg.append("<polyline points=\"");
            appendPoints(svg, geometry, path.points());
            svg.append("\"/>");
        }
        svg.append("</g><g fill=\"#70b77e\" stroke=\"#245c35\" stroke-width=\"35\" stroke-linejoin=\"round\">");
        for (DreameMapZoneGeometry zone : geometry.zones()) {
            svg.append("<polygon points=\"");
            appendPoints(svg, geometry, zone.points());
            svg.append("\"/>");
        }
        svg.append(
                "</g><g fill=\"#ef7777\" fill-opacity=\"0.78\" stroke=\"#a61111\" stroke-width=\"35\" stroke-linejoin=\"round\">");
        for (DreameMapZoneGeometry forbiddenArea : geometry.forbiddenAreas()) {
            svg.append("<polygon points=\"");
            appendPoints(svg, geometry, forbiddenArea.points());
            svg.append("\"/>");
        }
        svg.append("</g>");
        DreameMapPoint station = stationPoint(geometry.paths());
        if (station != null) {
            int stationX = rotatedX(geometry, station.x());
            int stationY = rotatedY(geometry, station.y());
            svg.append("<g transform=\"translate(").append(stationX).append(' ').append(stationY).append(
                    ")\"><rect x=\"-170\" y=\"-125\" width=\"340\" height=\"250\" rx=\"45\" fill=\"#3678c9\" stroke=\"#123c70\" stroke-width=\"35\"/>")
                    .append("<path d=\"M -65 -55 H 40 V -105 L 125 0 L 40 105 V 55 H -65 Z\" fill=\"#ffffff\"/></g>");
        }
        if (pose != null) {
            int mowerX = rotatedX(geometry, pose.x());
            int mowerY = rotatedY(geometry, pose.y());
            double mowerHeading = 180.0 - pose.heading();
            svg.append("<g transform=\"translate(").append(mowerX).append(' ').append(mowerY).append(") rotate(")
                    .append(mowerHeading).append(")\">")
                    .append("<circle r=\"145\" fill=\"#ffffff\" stroke=\"#152238\" stroke-width=\"35\"/>")
                    .append("<path d=\"M 220 0 L -105 -120 L -55 0 L -105 120 Z\" fill=\"#e53935\" stroke=\"#7f1010\" stroke-width=\"25\" stroke-linejoin=\"round\"/>")
                    .append("</g>");
        }
        return svg.append("</svg>").toString();
    }

    private static void appendPoints(StringBuilder svg, DreameMapGeometry geometry, List<DreameMapPoint> points) {
        for (DreameMapPoint point : points) {
            svg.append(rotatedX(geometry, point.x())).append(',').append(rotatedY(geometry, point.y())).append(' ');
        }
    }

    private static int rotatedX(DreameMapGeometry geometry, int x) {
        return geometry.minX() + geometry.maxX() - x;
    }

    private static int rotatedY(DreameMapGeometry geometry, int y) {
        return y - geometry.minY() - geometry.maxY();
    }

    private static @Nullable DreameMapPoint stationPoint(List<DreameMapPathGeometry> paths) {
        return paths.stream().filter(path -> path.type() == 1).flatMap(path -> path.points().stream())
                .min(java.util.Comparator
                        .comparingLong(point -> (long) point.x() * point.x() + (long) point.y() * point.y()))
                .orElse(null);
    }
}
