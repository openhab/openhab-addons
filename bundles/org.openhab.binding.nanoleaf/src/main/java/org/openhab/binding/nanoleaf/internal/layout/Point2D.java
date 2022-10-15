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

package org.openhab.binding.nanoleaf.internal.layout;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Simple pair class.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class Point2D {
    private final int x;
    private final int y;

    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Rotates the point a given amount of radians.
     * 
     * @param radians The amount to rotate the point
     * @return A new point which is rotated
     */
    public Point2D rotate(double radians) {
        double sinAngle = Math.sin(radians);
        double cosAngle = Math.cos(radians);

        int newX = (int) (cosAngle * x - sinAngle * y);
        int newY = (int) (sinAngle * x + cosAngle * y);
        return new Point2D(newX, newY);
    }
}
