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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for points of {@link ColorXy} of a gradient of alight.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Gradient {

    public static final int MAX_POINTS = 5;

    private @Nullable List<Color2> points;
    private @SerializedName("points_capable") int pointsCapable;

    /**
     * Gets the points of the gradient.
     * 
     * @return a list of {@link PairXy} representing the points of the gradient
     */
    public List<PairXy> getXyPoints() {
        List<Color2> points = this.points;
        if (points == null) {
            return List.of();
        }
        return points.stream().map(Color2::getColorXy).filter(Objects::nonNull).map(ColorXy::getXY)
                .filter(Objects::nonNull).map(xy -> (PairXy) xy).toList();
    }

    /**
     * Sets the points of the gradient.
     * 
     * @param index the index of the point to be set
     * @param xy the new color co-ordinate value to set for the point at the specified index
     * @throws IllegalStateException
     * @throws IndexOutOfBoundsException
     */
    public void setPointXy(int index, PairXy xy) throws IllegalStateException, IndexOutOfBoundsException {
        List<Color2> points = this.points;
        if (points == null) {
            throw new IllegalStateException("'points' not initialized");
        }
        if (index < 0 || index >= points.size()) {
            throw new IndexOutOfBoundsException("index:%d, size:%d".formatted(index, points.size()));
        }
        ColorXy color = points.get(index).getColorXy();
        if (color == null) {
            throw new IllegalStateException("'color' not initialized");
        }
        color.setXY(xy);
    }

    /**
     * Creates a deep copy of the current Gradient object. Purges all null fields.
     * 
     * @return a new Gradient object with the same (non null) points
     */
    public Gradient copy() {
        Gradient result = new Gradient();
        List<Color2> points = this.points;
        if (points != null) {
            List<Color2> resultPoints = new ArrayList<>(points.size());
            for (Color2 point : points) {
                ColorXy colorXy = point.getColorXy();
                if (colorXy == null) {
                    continue;
                }
                PairXy pairXy = colorXy.getXY();
                if (pairXy == null) {
                    continue;
                }
                double[] xy = pairXy.getXY();
                if (xy.length != 2) {
                    continue;
                }
                // deep copy
                resultPoints.add(new Color2()
                        .setColorXy(new ColorXy().setXY(new PairXy().setXY(new double[] { xy[0], xy[1] }))));
            }
            result.points = resultPoints;
        }
        return result;
    }

    public int getPointsCapable() {
        return pointsCapable;
    }
}
