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
package org.openhab.binding.synopanalyzer.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WindBarbGenerator} generates a SVG representation of wind speed
 * and direction
 * Inspired by https://github.com/spatialsparks/Leaflet.windbarb
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WindBarbGenerator {
    private static final String FILL_COLOR = "#2B85C7";
    private static final int POINT_RADIUS = 4; // station circle radius
    private static final int STROKE_WIDTH = 1;
    private static final int STROKE_LENGTH = 45; // hamp length
    private static final int BARB_SPACING = 5;
    private static final int BARB_HEIGHT = 15;
    private static final int VIEW_SIZE = 120;

    public String generateSVG(double speedKnots, double directionDeg) {
        int s = (int) (5 * Math.round(speedKnots / 5.0));
        int f50 = s / 50;
        int f10 = (s % 50) / 10;
        int f5 = (s % 10) / 5;

        double mid = VIEW_SIZE / 2.0;

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 ").append(VIEW_SIZE).append(" ")
                .append(VIEW_SIZE).append("' width='").append(VIEW_SIZE).append("' height='").append(VIEW_SIZE)
                .append("'>");

        // Rotation depending on wind direction
        sb.append("<g transform='rotate(").append(directionDeg).append(", ").append(mid).append(", ").append(mid)
                .append(")'>");

        // Station circle
        sb.append("<circle cx='").append(mid).append("' cy='").append(mid).append("' r='").append(POINT_RADIUS)
                .append("' fill='").append(FILL_COLOR).append("' stroke='black' stroke-width='").append(STROKE_WIDTH)
                .append("'/>");

        // Hamp
        double lineTopY = mid - POINT_RADIUS - STROKE_LENGTH;
        sb.append("<line x1='").append(mid).append("' y1='").append(mid - POINT_RADIUS).append("' x2='").append(mid)
                .append("' y2='").append(lineTopY).append("' stroke='black' stroke-width='").append(STROKE_WIDTH)
                .append("' stroke-linecap='butt'/>");

        // Start at the end of the hamp
        double currentY = lineTopY;

        for (int i = 0; i < f50; i++) { // 50 kt pennants
            currentY += BARB_SPACING;

            String points = String.format(Locale.US, "%.1f,%.1f %.1f,%.1f %.1f,%.1f", mid, currentY, mid + BARB_HEIGHT,
                    currentY - BARB_SPACING, mid, currentY - BARB_SPACING);

            sb.append("<polygon points='").append(points).append("' fill='black' stroke='black' ")
                    .append("stroke-width='").append(STROKE_WIDTH * 1).append("' stroke-linejoin='round'/>");

            currentY += 2;
        }

        if (f50 > 0) {
            currentY += (BARB_SPACING / 2.0);
        }

        for (int i = 0; i < f10; i++) { // 10 kt barbs
            appendBarb(sb, mid, currentY, BARB_HEIGHT, BARB_SPACING);
            currentY += BARB_SPACING;
        }

        if (f5 == 1) { // 5 kt barb
            if (s == 5) {
                currentY += BARB_SPACING;
            }
            appendBarb(sb, mid, currentY, BARB_HEIGHT * 0.5, BARB_SPACING * 0.5);
        }

        sb.append("</g></svg>");
        return sb.toString();
    }

    private void appendBarb(StringBuilder sb, double mid, double currentY, double barbHeight, double barbSpacing) {
        sb.append("<line x1='").append(mid).append("' y1='").append(currentY).append("' x2='").append(mid + barbHeight)
                .append("' y2='").append(currentY - barbSpacing).append("' stroke='black' stroke-width='")
                .append(STROKE_WIDTH).append("' stroke-linecap='round'/>");
    }
}
