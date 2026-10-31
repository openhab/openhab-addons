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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip2.ColorXy;
import org.openhab.binding.hue.internal.api.dto.clip2.Gradient;
import org.openhab.binding.hue.internal.api.dto.clip2.PairXy;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ContentType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.dto.clip2.helper.Setters;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;

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
    void setGradientPointColorXySendsFullSetWithoutCapacityAndLeavesCacheUntouched() {
        Resource cache = light(createGradient(5, 5, 0.10));
        Resource target = new Resource(ResourceType.LIGHT);

        Resource result = Setters.setGradientPointColorXy(target, new HSBType("120,100,100"), 1, cache);

        assertNotNull(result);
        Gradient sent = gradientOf(Objects.requireNonNull(result));
        assertEquals(5, sent.getXyPoints().size());
        assertEquals(0, sent.getPointsCapable());
        assertNotSame(gradientOf(cache), sent);

        // sent gradient has the changed point, and the other points unchanged
        assertNotEquals(0.11, x(sent, 1), 0.0001);
        assertEquals(0.10, x(sent, 0), 0.0001);
        assertEquals(0.12, x(sent, 2), 0.0001);

        // cache is not modified by the setter; the handler updates it afterwards
        assertEquals(0.10, x(gradientOf(cache), 0), 0.0001);
        assertEquals(0.11, x(gradientOf(cache), 1), 0.0001);
        assertEquals(0.12, x(gradientOf(cache), 2), 0.0001);
    }

    @Test
    void mergeMovesGradientIntoFirstResource() {
        Gradient gradient = createGradient(5, 5, 0.10);
        Resource first = new Resource(ResourceType.LIGHT).setId("light-id").setContentType(ContentType.UPDATE);
        first.setOnOff(OnOffType.ON);
        Resource second = new Resource(ResourceType.LIGHT).setId("light-id").setContentType(ContentType.UPDATE)
                .setGradient(gradient);
        second.setColorXy(new ColorXy().setXY(new double[] { 0.3, 0.3 }));

        List<Resource> resources = new ArrayList<>(List.of(first, second));
        Collection<Resource> merged = Setters.mergeLightResources(resources);

        assertEquals(1, merged.size());
        assertSame(gradient, first.getGradient());
        assertNull(second.getGradient());
        assertNotNull(first.getColorXy());
    }

    @Test
    void mergeKeepsSeparateLightsApart() {
        Resource front1 = new Resource(ResourceType.LIGHT).setId("front").setContentType(ContentType.UPDATE);
        front1.setOnOff(OnOffType.ON);
        Resource front2 = new Resource(ResourceType.LIGHT).setId("front").setContentType(ContentType.UPDATE)
                .setGradient(createGradient(5, 5, 0.10));
        front2.setColorXy(new ColorXy().setXY(new double[] { 0.3, 0.3 }));
        Resource back1 = new Resource(ResourceType.LIGHT).setId("back").setContentType(ContentType.UPDATE);
        back1.setOnOff(OnOffType.OFF);
        Resource back2 = new Resource(ResourceType.LIGHT).setId("back").setContentType(ContentType.UPDATE)
                .setGradient(createGradient(5, 5, 0.50));
        back2.setColorXy(new ColorXy().setXY(new double[] { 0.4, 0.4 }));

        Collection<Resource> merged = Setters
                .mergeLightResources(new ArrayList<>(List.of(front1, back1, front2, back2)));

        assertEquals(2, merged.size());
        assertEquals(0.10, x(gradientOf(front1), 0), 0.0001);
        assertEquals(0.50, x(gradientOf(back1), 0), 0.0001);
        assertNull(front2.getGradient());
        assertNull(back2.getGradient());
    }

    @Test
    void setResourceWithInvalidCachedPointDoesNotThrow() {
        // second point has no color => copy() throws IllegalStateException
        Gradient invalid = Objects.requireNonNull(GSON.fromJson(
                "{\"points\":[{\"color\":{\"xy\":{\"x\":0.1,\"y\":0.5}}},{}],\"points_capable\":5}", Gradient.class));
        Resource cached = light(invalid);

        // incoming without any gradient
        Resource incomingNone = new Resource(ResourceType.LIGHT).setId("light-id");
        assertDoesNotThrow(() -> Setters.setResource(incomingNone, cached));
        assertEquals(5, gradientOf(incomingNone).getPointsCapable());
        assertTrue(gradientOf(incomingNone).getXyPoints().isEmpty());

        // incoming with a sparse gradient (no points)
        Resource incomingSparse = light(createGradient(-1, null, 0));
        assertDoesNotThrow(() -> Setters.setResource(incomingSparse, cached));
        assertEquals(5, gradientOf(incomingSparse).getPointsCapable());
        assertTrue(gradientOf(incomingSparse).getXyPoints().isEmpty());
    }

    @Test
    void setGradientPointColorXyRepairsInvalidCachedPoint() {
        Gradient invalid = Objects.requireNonNull(GSON.fromJson(
                "{\"points\":[{\"color\":{\"xy\":{\"x\":0.1,\"y\":0.5}}},{}],\"points_capable\":5}", Gradient.class));
        Resource cache = light(invalid);

        Resource result = Setters.setGradientPointColorXy(new Resource(ResourceType.LIGHT), new HSBType("120,100,100"),
                0, cache);

        assertNotNull(result);
        assertEquals(5, gradientOf(Objects.requireNonNull(result)).getXyPoints().size());
        assertEquals(1, gradientOf(cache).getXyPoints().size());
    }

    @Test
    void setGradientPointColorXyOnEmptyPaletteUsesReportedCapacity() {
        Resource cache = light(createGradient(-1, 3, 0)); // solid color mode, capacity 3

        Resource result = Setters.setGradientPointColorXy(new Resource(ResourceType.LIGHT), new HSBType("120,100,100"),
                1, cache);

        assertNotNull(result);
        Gradient sent = gradientOf(Objects.requireNonNull(result));
        assertEquals(3, sent.getXyPoints().size());
        assertEquals(0, sent.getPointsCapable());
        assertFalse(GSON.toJson(sent).contains("points_capable"));
    }

    @Test
    void setGradientPointColorXyRepairPathUsesReportedCapacity() {
        Gradient invalid = Objects.requireNonNull(GSON.fromJson(
                "{\"points\":[{\"color\":{\"xy\":{\"x\":0.1,\"y\":0.5}}},{}],\"points_capable\":3}", Gradient.class));

        Resource result = Setters.setGradientPointColorXy(new Resource(ResourceType.LIGHT), new HSBType("120,100,100"),
                0, light(invalid));

        assertNotNull(result);
        assertEquals(3, gradientOf(Objects.requireNonNull(result)).getXyPoints().size());
    }
}
