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

    private @Nullable List<Point> points;
    private @Nullable @SerializedName("points_capable") Integer pointsCapable;

    /**
     * Gets the points of the gradient.
     * 
     * @return a list of {@link PairXy} representing the points of the gradient
     */
    public List<PairXy> getXyPoints() {
        List<Point> points = this.points;
        if (points == null) {
            return List.of();
        }
        return points.stream().map(Point::getColorXy).filter(Objects::nonNull).map(ColorXy::getXY)
                .filter(Objects::nonNull).map(xy -> (PairXy) xy).toList();
    }

    /**
     * Sets the color of the point at the given index. If the gradient has no points yet (e.g. the light is
     * in solid color mode), or has fewer points than needed, the missing points are first filled with
     * copies of the new color so that a complete palette can be sent to the bridge.
     *
     * @param index the index of the point to be set
     * @param pairXy the new color co-ordinate value to set for the point at the specified index
     * @throws IndexOutOfBoundsException if index is outside the light's point capacity
     */
    public Gradient setPointXy(int index, PairXy pairXy) throws IndexOutOfBoundsException {
        Integer capable = this.pointsCapable;
        int capacity = capable != null ? capable : MAX_POINTS;
        return setPointXy(index, pairXy, capacity);
    }

    /**
     * Sets the color of the point at the given index. If the gradient has no points yet (e.g. the light is
     * in solid color mode), or has fewer points than needed, the missing points are first filled with
     * copies of the new color so that a complete palette can be sent to the bridge.
     *
     * @param index the index of the point to be set
     * @param pairXy the new color co-ordinate value to set for the point at the specified index
     * @param capacity the number of points the light can handle
     * @throws IndexOutOfBoundsException if index is outside the light's point capacity
     */
    public Gradient setPointXy(int index, PairXy pairXy, int capacity) throws IndexOutOfBoundsException {
        if (index < 0 || index >= capacity) {
            throw new IndexOutOfBoundsException("index:%d, capacity:%d".formatted(index, capacity));
        }
        double[] xy = pairXy.getXY();
        List<Point> current = this.points;
        int size = current == null || current.isEmpty() ? capacity : Math.max(index + 1, current.size());
        List<Point> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Point point = current != null && i < current.size() ? current.get(i) : null;
            result.add(i != index && point != null && isValid(point) ? point : newPoint(xy));
        }
        this.points = result; // readers never see a half-updated list
        return this;
    }

    private static boolean isValid(Point point) {
        ColorXy colorXy = point.getColorXy();
        PairXy pairXy = colorXy != null ? colorXy.getXY() : null;
        return pairXy != null && pairXy.getXY().length == 2;
    }

    /**
     * Creates a new {@link Point} object with the specified color co-ordinate values.
     *
     * @param xy an array containing the x and y color co-ordinate values
     * @return a new {@link Point} object with the specified color co-ordinate values
     */
    private static Point newPoint(double[] xy) {
        return new Point().setColorXy(new ColorXy().setXY(new PairXy().setXY(new double[] { xy[0], xy[1] })));
    }

    /**
     * Creates a deep copy of the current Gradient object. The read-only 'points_capable' field is not copied.
     *
     * @return a new Gradient object with the same points
     * @throws IllegalStateException if a point has no valid xy value
     */
    public Gradient copy() throws IllegalStateException {
        Gradient result = new Gradient();
        List<Point> points = this.points;
        if (points != null) {
            List<Point> resultPoints = new ArrayList<>(points.size());
            for (Point point : points) {
                ColorXy colorXy = point.getColorXy();
                PairXy pairXy = colorXy != null ? colorXy.getXY() : null;
                double[] xy = pairXy != null ? pairXy.getXY() : null;
                if (xy == null || xy.length != 2) {
                    throw new IllegalStateException("gradient point has no valid xy value");
                }
                resultPoints.add(newPoint(xy));
            }
            result.points = resultPoints;
        }
        return result;
    }

    /**
     * Takes over any missing fields from the other gradient. Capacity is taken over if this one has none. The
     * points are taken over (as a private deep copy) only if this one has none; if this one has points then
     * they are newer, so they are kept.
     *
     * @param other the (cached) gradient to take missing fields from
     */
    public void takeOverMissingFields(Gradient other) {
        if (pointsCapable == null && other.pointsCapable != null) {
            pointsCapable = other.pointsCapable;
        }
        List<Point> mine = this.points;
        if (mine == null && other.points instanceof List<Point> theirs && !theirs.isEmpty()) {
            try {
                this.points = other.copy().points;
            } catch (IllegalStateException e) {
                // invalid cached point: leave the points unset
            }
        }
    }

    public int getPointsCapable() {
        Integer pointsCapable = this.pointsCapable;
        return pointsCapable != null ? pointsCapable : 0;
    }
}
