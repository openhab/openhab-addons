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
package org.openhab.binding.hue.internal.clip2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip2.Gradient;
import org.openhab.binding.hue.internal.api.dto.clip2.PairXy;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.dto.clip2.helper.Setters;
import org.openhab.core.library.types.HSBType;

import com.google.gson.Gson;

/**
 * JUnit tests for {@link Gradient} and {@link Setters#setResource}.
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class GradientCacheTest {

    private static final Gson GSON = new Gson();

    /**
     * Creates a gradient with the given number of points, where point i has x = base + i / 100.0 and y = 0.5.
     *
     * @param count the number of points; if negative the 'points' element is omitted (solid color mode)
     * @param capable the 'points_capable' value, or null to omit it
     */
    private static Gradient createGradient(int count, @Nullable Integer capable, double base) {
        StringBuilder json = new StringBuilder("{");
        if (count >= 0) {
            json.append("\"points\":[");
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append("{\"color\":{\"xy\":{\"x\":%s,\"y\":0.5}}}".formatted(base + i / 100.0));
            }
            json.append("]");
        }
        if (capable != null) {
            json.append(count >= 0 ? "," : "").append("\"points_capable\":").append(capable);
        }
        json.append("}");
        return Objects.requireNonNull(GSON.fromJson(json.toString(), Gradient.class));
    }

    private static Resource light(Gradient gradient) {
        return new Resource(ResourceType.LIGHT).setId("light-id").setGradient(gradient);
    }

    private static double x(Gradient gradient, int index) {
        return gradient.getXyPoints().get(index).getXY()[0];
    }

    private static Gradient gradientOf(Resource resource) {
        return Objects.requireNonNull(resource.getGradient());
    }

    @Test
    void sparseIncomingTakesOverCachedPoints() {
        Resource cached = light(createGradient(5, 5, 0.10));
        Resource incoming = light(createGradient(-1, null, 0)); // gradient element without points

        Setters.setResource(incoming, cached);

        Gradient result = gradientOf(incoming);
        assertEquals(5, result.getXyPoints().size());
        assertEquals(0.10, x(result, 0), 0.0001);
        assertEquals(0.14, x(result, 4), 0.0001);
        assertEquals(5, result.getPointsCapable());
    }

    @Test
    void incomingPointsWinOverCachedPoints() {
        Resource cached = light(createGradient(5, 5, 0.10));
        Resource incoming = light(createGradient(5, null, 0.30));

        Setters.setResource(incoming, cached);

        Gradient result = gradientOf(incoming);
        assertEquals(5, result.getXyPoints().size());
        assertEquals(0.30, x(result, 0), 0.0001);
        assertEquals(0.34, x(result, 4), 0.0001);
        assertEquals(5, result.getPointsCapable()); // capacity is still taken over
    }

    @Test
    void incomingCapacityIsKept() {
        Resource cached = light(createGradient(5, 5, 0.10));
        Resource incoming = light(createGradient(3, 3, 0.30));

        Setters.setResource(incoming, cached);

        assertEquals(3, gradientOf(incoming).getPointsCapable());
        assertEquals(3, gradientOf(incoming).getXyPoints().size());
    }

    @Test
    void nullTargetGradientTakesPrivateCopy() {
        Resource cached = light(createGradient(5, 5, 0.10));
        Resource incoming = new Resource(ResourceType.LIGHT).setId("light-id");

        Setters.setResource(incoming, cached);

        Gradient result = gradientOf(incoming);
        assertNotSame(gradientOf(cached), result);
        assertEquals(5, result.getXyPoints().size());
        assertEquals(5, result.getPointsCapable());

        // changing the copy must not change the cache
        result.setPointXy(2, new PairXy().setXY(new double[] { 0.9, 0.9 }));
        assertEquals(0.9, x(result, 2), 0.0001);
        assertEquals(0.12, x(gradientOf(cached), 2), 0.0001);
    }

    @Test
    void noSourceGradientLeavesTargetUntouched() {
        Resource cached = new Resource(ResourceType.LIGHT).setId("light-id");
        Resource incoming = light(createGradient(2, 5, 0.30));

        Setters.setResource(incoming, cached);

        assertEquals(2, gradientOf(incoming).getXyPoints().size());
        assertEquals(0.30, x(gradientOf(incoming), 0));
    }

    @Test
    void setPointXyOnCachedFullSetChangesOnlyThatPoint() {
        Gradient gradient = createGradient(5, 5, 0.10);

        gradient.setPointXy(1, new PairXy().setXY(new double[] { 0.9, 0.8 }));

        List<PairXy> points = gradient.getXyPoints();
        assertEquals(5, points.size());
        assertEquals(0.10, points.get(0).getXY()[0], 0.0001);
        assertEquals(0.9, points.get(1).getXY()[0], 0.0001);
        assertEquals(0.8, points.get(1).getXY()[1], 0.0001);
        assertEquals(0.12, points.get(2).getXY()[0], 0.0001);
    }

    @Test
    void setPointXyOnEmptyGradientPadsToCapacity() {
        Gradient gradient = createGradient(-1, 5, 0); // solid color mode, capacity 5

        gradient.setPointXy(2, new PairXy().setXY(new double[] { 0.7, 0.6 }));

        List<PairXy> points = gradient.getXyPoints();
        assertEquals(5, points.size());
        for (PairXy point : points) {
            assertEquals(0.7, point.getXY()[0], 0.0001); // all points start as the new color
        }
    }

    @Test
    void setPointXyOutsideCapacityThrows() {
        Gradient gradient = createGradient(5, 5, 0.10);
        PairXy xy = new PairXy().setXY(new double[] { 0.5, 0.3 });

        assertThrows(IndexOutOfBoundsException.class, () -> gradient.setPointXy(-1, xy));
        assertThrows(IndexOutOfBoundsException.class, () -> gradient.setPointXy(5, xy));
    }

    @Test
    void copyOmitsCapacityAndIsIndependent() {
        Gradient original = createGradient(3, 5, 0.10);

        Gradient copy = original.copy();

        assertEquals(0, copy.getPointsCapable());
        assertEquals(3, copy.getXyPoints().size());
        assertFalse(GSON.toJson(copy).contains("points_capable"));

        copy.setPointXy(0, new PairXy().setXY(new double[] { 0.9, 0.9 }));
        assertEquals(0.10, x(original, 0));
    }

    @Test
    void setGradientPointColorXyOutsideCapacityReturnsNull() {
        Resource cache = light(createGradient(5, 5, 0.10));
        Resource target = new Resource(ResourceType.LIGHT);

        assertNull(Setters.setGradientPointColorXy(target, new HSBType("120,100,100"), 5, cache));
    }

    @Test
    void setGradientPointColorXyUpdatesCacheAndSendsFullSetWithoutCapacity() {
        Resource cache = light(createGradient(5, 5, 0.10));
        Resource target = new Resource(ResourceType.LIGHT);

        Resource result = Setters.setGradientPointColorXy(target, new HSBType("120,100,100"), 1, cache);

        assertNotNull(result);
        Gradient sent = gradientOf(Objects.requireNonNull(result));
        assertEquals(5, sent.getXyPoints().size());
        assertEquals(0, sent.getPointsCapable());
        assertNotSame(gradientOf(cache), sent);
        // cache has the changed point, and the other four points unchanged
        assertNotEquals(0.11, x(gradientOf(cache), 1));
        assertEquals(0.10, x(gradientOf(cache), 0), 0.0001);
        assertEquals(0.12, x(gradientOf(cache), 2), 0.0001);
    }
}
