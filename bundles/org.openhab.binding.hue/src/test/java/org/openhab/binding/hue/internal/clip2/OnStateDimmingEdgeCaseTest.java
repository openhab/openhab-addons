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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip2.Dimming;
import org.openhab.binding.hue.internal.api.dto.clip2.OnState;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.UnDefType;

/**
 * JUnit test for edge cases of OnState and Dimming event and cache resources.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class OnStateDimmingEdgeCaseTest {

    @Test
    void getBrightnessStateWhenDimmingMissingReturnNull() {
        assertThat(createLightResource(true, null).getBrightnessState(), is(equalTo(UnDefType.NULL)));
    }

    @Test
    void getBrightnessStateWhenOnAndDimming75ReturnBrightness75() {
        assertThat(createLightResource(true, 75.0).getBrightnessState(), is(equalTo(new PercentType(75))));
    }

    @Test
    void getBrightnessStateWhenOnAndDimming125ReturnBrightness100() {
        assertThat(createLightResource(true, 125.0).getBrightnessState(), is(equalTo(new PercentType(100))));
    }

    @Test
    void getBrightnessStateWhenOffAndDimming100ReturnBrightness0() {
        assertThat(createLightResource(false, 100.0).getBrightnessState(), is(equalTo(new PercentType(0))));
    }

    @Test
    void getBrightnessStateWhenOnStateMissingAndDimming0ReturnMinimumBrightness0() {
        assertThat(createLightResource(null, 0.0).getBrightnessState(),
                is(equalTo(new PercentType(new BigDecimal(Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL)))));
    }

    @Test
    void getBrightnessStateWhenOnStateMissingAndDimming100ReturnBrightness100() {
        assertThat(createLightResource(null, 100.0).getBrightnessState(), is(equalTo(new PercentType(100))));
    }

    @Test
    void getBrightnessStateWhenOnStateMissingAndDimmingMinus1ReturnMinimumBrightness() {
        assertThat(createLightResource(null, -1.0).getBrightnessState(),
                is(equalTo(new PercentType(new BigDecimal(Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL)))));
    }

    @Test
    void getBrightnessStateWhenOnAndDimmingMinus1ReturnMinimumBrightness() {
        assertThat(createLightResource(true, -1.0).getBrightnessState(),
                is(equalTo(new PercentType(new BigDecimal(Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL)))));
    }

    @Test
    void getBrightnessStateWhenOnAndDimming0ReturnMinimumBrightness() {
        assertThat(createLightResource(true, 0.0).getBrightnessState(),
                is(equalTo(new PercentType(new BigDecimal(Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL)))));
    }

    @Test
    void getBrightnessStateWhenOnAndDimming0ReturnCustomMinimumBrightness() {
        assertThat(createLightResource(true, 0.0, 2.0).getBrightnessState(), is(equalTo(new PercentType(2))));
    }

    private Resource createLightResource(@Nullable Boolean on, @Nullable Double brightness) {
        return createLightResource(on, brightness, null);
    }

    private Resource createLightResource(@Nullable Boolean on, @Nullable Double brightness,
            @Nullable Double minimumDimmingLevel) {
        Resource resource = new Resource(ResourceType.LIGHT);

        if (on != null) {
            OnState onState = new OnState();
            onState.setOn(on);
            resource.setOnState(onState);
        }

        if (brightness != null) {
            Dimming dimming = new Dimming();
            dimming.setBrightness(brightness);

            if (minimumDimmingLevel != null) {
                dimming.setMinimumDimmingLevel(minimumDimmingLevel);
            }

            resource.setDimming(dimming);
        }

        return resource;
    }
}
