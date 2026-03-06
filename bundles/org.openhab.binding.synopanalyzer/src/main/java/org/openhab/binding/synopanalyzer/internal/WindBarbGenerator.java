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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WindBarbGenerator} is responsible to generate a winbarb representation
 * of wind conditions
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WindBarbGenerator {
    private static final double KMH_TO_KNOT = 1.0 / 1.852;

    /**
     * Génère un wind barb SVG style aviation avec cercle plein.
     *
     * @param speedKmh vitesse du vent en km/h
     * @param directionDeg direction du vent (0° = vent venant du nord)
     * @param size taille du SVG en pixels
     * @param color couleur du symbole
     * @return SVG en string
     */
    public static String generate(double speedKmh, double directionDeg, int size, String color) {
        double knots = speedKmh * KMH_TO_KNOT;
        int rounded = (int) (5 * Math.round(knots / 5.0));

        int flags50 = rounded / 50;
        int remainder = rounded % 50;
        int barbs10 = remainder / 10;
        remainder %= 10;
        int barbs5 = remainder / 5;

        double half = size / 2.0;
        double radius = half * 0.9;

        // Cercle station
        double stationRadius = radius * 0.15;

        // Hampe et barbes proportionnées
        double staff = radius - stationRadius; // hampe part du bord du cercle
        double barbSpacing = stationRadius * 1.2; // espacement entre barbes/fanions
        double barbLength = stationRadius * 3.0; // longueur des barbes/fanions

        // Angle de rotation en radians
        double angleRad = Math.toRadians(directionDeg);
        // vecteur unitaire hampe
        double dx = Math.sin(angleRad);
        double dy = -Math.cos(angleRad);

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ").append("width=\"").append(size).append("\" ")
                .append("height=\"").append(size).append("\" ").append("viewBox=\"").append(-half).append(" ")
                .append(-half).append(" ").append(size).append(" ").append(size).append("\">");

        // Cercle plein de la station
        svg.append("<circle cx=\"0\" cy=\"0\" r=\"").append(stationRadius).append("\" fill=\"").append(color)
                .append("\"/>");

        // Vent calme : juste le cercle
        if (rounded == 0) {
            svg.append("</svg>");
            return svg.toString();
        }

        // Extrémité de la hampe
        double xStart = dx * stationRadius;
        double yStart = dy * stationRadius;
        double xEnd = dx * (stationRadius + staff);
        double yEnd = dy * (stationRadius + staff);

        // Hampe
        svg.append("<line x1=\"").append(xStart).append("\" y1=\"").append(yStart).append("\" x2=\"").append(xEnd)
                .append("\" y2=\"").append(yEnd).append("\" stroke=\"").append(color).append("\" stroke-width=\"2\"/>");

        // Dessin des barbes/fanions le long de la hampe, côté droit
        double pos = 0; // distance le long de la hampe depuis l'extrémité

        for (int i = 0; i < flags50; i++) {
            double y0 = yEnd + dy * pos;
            double x0 = xEnd + dx * pos;

            // vecteur perpendiculaire droite
            double px = -dy * barbLength;
            double py = dx * barbLength;

            // triangle fanion
            double x1 = x0;
            double y1 = y0;
            double x2 = x0 + px;
            double y2 = y0 + py;
            double x3 = x0;
            double y3 = y0 + dy * barbSpacing * 2;

            svg.append("<polygon points=\"").append(x1).append(",").append(y1).append(" ").append(x2).append(",")
                    .append(y2).append(" ").append(x3).append(",").append(y3).append("\" fill=\"").append(color)
                    .append("\"/>");

            pos += barbSpacing * 2;
        }

        for (int i = 0; i < barbs10; i++) {
            double y0 = yEnd + dy * pos;
            double x0 = xEnd + dx * pos;

            double px = -dy * barbLength;
            double py = dx * barbLength;

            svg.append("<line x1=\"").append(x0).append("\" y1=\"").append(y0).append("\" x2=\"").append(x0 + px)
                    .append("\" y2=\"").append(y0 + py).append("\" stroke=\"").append(color)
                    .append("\" stroke-width=\"2\"/>");

            pos += barbSpacing;
        }

        for (int i = 0; i < barbs5; i++) {
            double y0 = yEnd + dy * pos;
            double x0 = xEnd + dx * pos;

            double px = -dy * barbLength * 0.6;
            double py = dx * barbLength * 0.6;

            svg.append("<line x1=\"").append(x0).append("\" y1=\"").append(y0).append("\" x2=\"").append(x0 + px)
                    .append("\" y2=\"").append(y0 + py).append("\" stroke=\"").append(color)
                    .append("\" stroke-width=\"2\"/>");

            pos += barbSpacing * 0.6;
        }

        svg.append("</svg>");
        return svg.toString();
    }

}
