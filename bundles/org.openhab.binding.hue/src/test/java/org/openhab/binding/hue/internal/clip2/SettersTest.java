/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hue.internal.api.dto.clip2.ColorTemperature;
import org.openhab.binding.hue.internal.api.dto.clip2.Dimming;
import org.openhab.binding.hue.internal.api.dto.clip2.Effects;
import org.openhab.binding.hue.internal.api.dto.clip2.OnState;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.dto.clip2.helper.Setters;
import org.openhab.binding.hue.internal.exceptions.DTOPresentButEmptyException;

/**
 * Tests for {@link Setters}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SettersTest {

    /**
     * Tests merging of on state and dimming for same resource.
     *
     * Input:
     * - Resource 1: type=light/grouped_light, sparse, id=1, on=on
     * - Resource 2: type=light/grouped_light, sparse, id=1, dimming=50
     *
     * Expected output:
     * - Resource 1: type=light/grouped_light, sparse, id=1, on=on, dimming=50
     * 
     * @throws DTOPresentButEmptyException
     */
    @ParameterizedTest
    @MethodSource("provideLightResourceTypes")
    void mergeLightResourcesMergeOnStateAndDimmingWhenSparseAndSameId(ResourceType resourceType)
            throws DTOPresentButEmptyException {
        List<Resource> resources = new ArrayList<>();

        Resource resource1 = createResource(resourceType, "1");
        resource1.setOnState(createOnState(true));
        resources.add(resource1);

        Resource resource2 = createResource(resourceType, "1");
        resource2.setDimming(createDimming(50));
        resources.add(resource2);

        Setters.mergeLightResources(resources);

        assertThat(resources.size(), is(equalTo(1)));
        Resource mergedResource = resources.get(0);
        assertThat(mergedResource.getId(), is(equalTo(resource1.getId())));
        assertThat(mergedResource.getType(), is(equalTo(resourceType)));
        assertThat(mergedResource.hasFullState(), is(false));
        OnState actualOnState = mergedResource.getOnState();
        assertThat(actualOnState, is(notNullValue()));
        if (actualOnState != null) {
            assertThat(actualOnState.isOn(), is(true));
        }
        Dimming actualDimming = mergedResource.getDimming();
        assertThat(actualDimming, is(notNullValue()));
        if (actualDimming != null) {
            assertThat(actualDimming.getBrightness(), is(equalTo(50.0)));
        }
    }

    private static Stream<Arguments> provideLightResourceTypes() {
        return Stream.of(Arguments.of(ResourceType.LIGHT), Arguments.of(ResourceType.GROUPED_LIGHT));
    }

    /**
     * Tests merging of dimming for same resource where last value wins.
     *
     * Input:
     * - Resource 1: type=light, sparse, id=1, dimming=49
     * - Resource 2: type=light, sparse, id=1, dimming=50
     *
     * Expected output:
     * - Resource 1: type=light, sparse, id=1, dimming=50
     * 
     * @throws DTOPresentButEmptyException
     */
    @Test
    void mergeLightResourcesMergeDimmingToLatestValueWhenSparseAndSameId() throws DTOPresentButEmptyException {
        List<Resource> resources = new ArrayList<>();

        Resource resource1 = createResource(ResourceType.LIGHT, "1");
        resource1.setDimming(createDimming(49));
        resources.add(resource1);

        Resource resource2 = createResource(ResourceType.LIGHT, "1");
        resource2.setDimming(createDimming(50));
        resources.add(resource2);

        Setters.mergeLightResources(resources);

        assertThat(resources.size(), is(equalTo(1)));
        Resource mergedResource = resources.get(0);
        assertThat(mergedResource.hasFullState(), is(false));
        Dimming actualDimming = mergedResource.getDimming();
        assertThat(actualDimming, is(notNullValue()));
        if (actualDimming != null) {
            assertThat(actualDimming.getBrightness(), is(equalTo(50.0)));
        }
    }

    /**
     * Tests merging of HSB type fields while keeping resource with effects.
     *
     * Input:
     * - Resource 1: type=light, sparse, id=1, dimming=49
     * - Resource 2: type=light, sparse, id=1, on=on, dimming=50, effect=xxx
     *
     * Expected output:
     * - Resource 1: type=light, sparse, id=1, on=on, dimming=50
     * - Resource 2: type=light, sparse, id=1, effect=xxx
     * 
     * @throws DTOPresentButEmptyException
     */
    @Test
    void mergeLightResourcesMergeHSBFieldsDoNotRemoveResourceWithEffect() throws DTOPresentButEmptyException {
        List<Resource> resources = new ArrayList<>();

        Resource resource1 = createResource(ResourceType.LIGHT, "1");
        resource1.setDimming(createDimming(49));
        resources.add(resource1);

        Resource resource2 = createResource(ResourceType.LIGHT, "1");
        resource2.setDimming(createDimming(50));
        resource2.setOnState(createOnState(true));
        resource2.setFixedEffects(new Effects());
        resources.add(resource2);

        Setters.mergeLightResources(resources);

        assertThat(resources.size(), is(equalTo(2)));
        Resource mergedResource = resources.get(0);
        assertThat(mergedResource.hasFullState(), is(false));
        OnState actualOnState = mergedResource.getOnState();
        assertThat(actualOnState, is(notNullValue()));
        if (actualOnState != null) {
            assertThat(actualOnState.isOn(), is(true));
        }
        Dimming actualDimming = mergedResource.getDimming();
        assertThat(actualDimming, is(notNullValue()));
        if (actualDimming != null) {
            assertThat(actualDimming.getBrightness(), is(equalTo(50.0)));
        }

        Resource effectsResource = resources.get(1);
        assertThat(effectsResource.hasFullState(), is(false));
        assertThat(effectsResource.getFixedEffects(), is(notNullValue()));
    }

    /**
     * Tests leaving different resources separated.
     *
     * Input:
     * - Resource 1: type=light, sparse, id=1, on=on
     * - Resource 2: type=light, sparse, id=2, dimming=50
     *
     * Expected output:
     * - Resource 1: type=light, sparse, id=1, on=on
     * - Resource 2: type=light, sparse, id=2, dimming=50
     * 
     * @throws DTOPresentButEmptyException
     */
    @Test
    void mergeLightResourcesDoNotMergeOnStateAndDimmingWhenSparseAndDifferentId() throws DTOPresentButEmptyException {
        List<Resource> resources = new ArrayList<>();

        Resource resource1 = createResource(ResourceType.LIGHT, "1");
        resource1.setOnState(createOnState(true));
        resources.add(resource1);

        Resource resource2 = createResource(ResourceType.LIGHT, "2");
        resource2.setDimming(createDimming(50));
        resources.add(resource2);

        Setters.mergeLightResources(resources);

        assertThat(resources.size(), is(equalTo(2)));
        Resource firstResource = resources.get(0);
        OnState actualOnState = firstResource.getOnState();
        assertThat(actualOnState, is(notNullValue()));
        if (actualOnState != null) {
            assertThat(actualOnState.isOn(), is(true));
        }
        assertThat(firstResource.getDimming(), is(nullValue()));

        Resource secondResource = resources.get(1);
        assertThat(secondResource.getOnState(), is(nullValue()));
        Dimming actualDimming = secondResource.getDimming();
        assertThat(actualDimming, is(notNullValue()));
        if (actualDimming != null) {
            assertThat(actualDimming.getBrightness(), is(equalTo(50.0)));
        }
    }

    /**
     * Tests merging of on state and dimming for same resource when full is first.
     *
     * Input:
     * - Resource 1: type=light, full, id=1, on=on
     *
     * Expected output:
     * - Exception thrown, full state is not supported/expected.
     * 
     * @throws DTOPresentButEmptyException
     */
    @Test
    void mergeLightResourcesMergeOnStateAndDimmingWhenFullStateFirstAndSameId() throws DTOPresentButEmptyException {
        List<Resource> resources = new ArrayList<>();

        Resource resource = new Resource(ResourceType.LIGHT);
        resource.setId("1");

        resources.add(resource);

        assertThrows(IllegalStateException.class, () -> Setters.mergeLightResources(resources));
    }

    /**
     * Tests leaving resources with on state and color temperature separated.
     * In this case they could be merged, but it's not needed.
     *
     * Input:
     * - Resource 1: type=light, sparse, id=1, on=on
     * - Resource 2: type=light, sparse, id=1, color temperature=370 mirek
     *
     * Expected output:
     * - Resource 1: type=light, sparse, id=1, on=on
     * - Resource 2: type=light, sparse, id=1, color temperature=370 mirek
     * 
     * @throws DTOPresentButEmptyException
     */
    @Test
    void mergeLightResourcesDoNotMergeOnStateAndColorTemperatureWhenSparseAndSameId()
            throws DTOPresentButEmptyException {
        List<Resource> resources = new ArrayList<>();

        Resource resource1 = createResource(ResourceType.LIGHT, "1");
        resource1.setOnState(createOnState(true));
        resources.add(resource1);

        Resource resource2 = createResource(ResourceType.LIGHT, "1");
        resource2.setColorTemperature(createColorTemperature(370));
        resources.add(resource2);

        Setters.mergeLightResources(resources);

        assertThat(resources.size(), is(equalTo(2)));
        Resource firstResource = resources.get(0);
        OnState actualOnState = firstResource.getOnState();
        assertThat(actualOnState, is(notNullValue()));
        if (actualOnState != null) {
            assertThat(actualOnState.isOn(), is(true));
        }
        assertThat(firstResource.getColorTemperature(), is(nullValue()));

        Resource secondResource = resources.get(1);
        assertThat(secondResource.getOnState(), is(nullValue()));
        ColorTemperature actualColorTemperature = secondResource.getColorTemperature();
        assertThat(actualColorTemperature, is(notNullValue()));
        if (actualColorTemperature != null) {
            assertThat(actualColorTemperature.getMirek(), is(equalTo(370L)));
        }
    }

    /**
     * Tests merging resources with on state/color and leaving color temperature separated.
     * In this case they could be merged, but it's not needed.
     *
     * Input:
     * - Resource 1: type=light, sparse, id=1, on=on
     * - Resource 2: type=light, sparse, id=1, dimming=50, color temperature=370 mirek
     *
     * Expected output:
     * - Resource 1: type=light, sparse, id=1, on=on, dimming=50
     * - Resource 2: type=light, sparse, id=1, color temperature=370 mirek
     * 
     * @throws DTOPresentButEmptyException
     */
    @Test
    void mergeLightResourcesMergeOnStateAndDimmingButNotColorTemperatureWhenSparseAndSameId()
            throws DTOPresentButEmptyException {
        List<Resource> resources = new ArrayList<>();

        Resource resource1 = createResource(ResourceType.LIGHT, "1");
        resource1.setOnState(createOnState(true));
        resources.add(resource1);

        Resource resource2 = createResource(ResourceType.LIGHT, "1");
        resource2.setDimming(createDimming(50));
        resource2.setColorTemperature(createColorTemperature(370));
        resources.add(resource2);

        Setters.mergeLightResources(resources);

        assertThat(resources.size(), is(equalTo(2)));
        Resource firstResource = resources.get(0);
        OnState actualOnState = firstResource.getOnState();
        assertThat(actualOnState, is(notNullValue()));
        if (actualOnState != null) {
            assertThat(actualOnState.isOn(), is(true));
        }
        Dimming actualDimming = firstResource.getDimming();
        assertThat(actualDimming, is(notNullValue()));
        if (actualDimming != null) {
            assertThat(actualDimming.getBrightness(), is(equalTo(50.0)));
        }
        assertThat(firstResource.getColorTemperature(), is(nullValue()));

        Resource secondResource = resources.get(1);
        assertThat(secondResource.getOnState(), is(nullValue()));
        assertThat(secondResource.getDimming(), is(nullValue()));
        ColorTemperature actualColorTemperature = secondResource.getColorTemperature();
        assertThat(actualColorTemperature, is(notNullValue()));
        if (actualColorTemperature != null) {
            assertThat(actualColorTemperature.getMirek(), is(equalTo(370L)));
        }
    }

    /**
     * Tests preserving resource with on state and color temperature.
     *
     * Input:
     * - Resource 1: type=light, sparse, id=1, on=on, color temperature=370
     *
     * Expected output:
     * - Resource 1: type=light, sparse, id=1, on=on, color temperature=370
     * 
     * @throws DTOPresentButEmptyException
     */
    @Test
    void mergeLightResourcesSeparateOnStateAndColorTemperatureWhenSparseAndSameId() throws DTOPresentButEmptyException {
        List<Resource> resources = new ArrayList<>();

        Resource resource = createResource(ResourceType.LIGHT, "1");
        resource.setOnState(createOnState(true));
        resource.setColorTemperature(createColorTemperature(370));
        resources.add(resource);

        Setters.mergeLightResources(resources);

        assertThat(resources.size(), is(equalTo(1)));

        Resource firstResource = resources.get(0);
        OnState actualOnState = firstResource.getOnState();
        assertThat(actualOnState, is(notNullValue()));
        if (actualOnState != null) {
            assertThat(actualOnState.isOn(), is(true));
        }
        ColorTemperature actualColorTemperature = firstResource.getColorTemperature();
        assertThat(actualColorTemperature, is(notNullValue()));
        if (actualColorTemperature != null) {
            assertThat(actualColorTemperature.getMirek(), is(equalTo(370L)));
        }
    }

    /**
     * Tests that resources that are not light or grouped_light will not throw.
     *
     * Input:
     * - Resource 1: type=motion, sparse, id=1
     *
     * Expected output:
     * - Resource 1: type=motion, sparse, id=1
     * 
     * @throws DTOPresentButEmptyException
     */
    @Test
    void mergeLightResourcesNonLightResourceShouldNotThrow() throws DTOPresentButEmptyException {
        List<Resource> resources = new ArrayList<>();

        Resource resource = createResource(ResourceType.MOTION, "1");
        resources.add(resource);

        Setters.mergeLightResources(resources);

        assertThat(resources.size(), is(equalTo(1)));

        Resource firstResource = resources.get(0);
        assertThat(firstResource.getType(), is(equalTo(ResourceType.MOTION)));
    }

    private OnState createOnState(boolean on) {
        OnState onState = new OnState();
        onState.setOn(on);

        return onState;
    }

    private Dimming createDimming(double brightness) {
        Dimming dimming = new Dimming();
        dimming.setBrightness(brightness);

        return dimming;
    }

    private ColorTemperature createColorTemperature(double mirek) {
        ColorTemperature colorTemperature = new ColorTemperature();
        colorTemperature.setMirek(mirek);

        return colorTemperature;
    }

    private Resource createResource(ResourceType resourceType, String id) {
        Resource resource = new Resource(resourceType);
        resource.setId(id);
        resource.markAsSparse();

        return resource;
    }
}
