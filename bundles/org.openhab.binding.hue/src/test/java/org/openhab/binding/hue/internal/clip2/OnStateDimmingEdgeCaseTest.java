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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip2.ColorXy;
import org.openhab.binding.hue.internal.api.dto.clip2.Dimming;
import org.openhab.binding.hue.internal.api.dto.clip2.OnState;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.exceptions.CriticalFieldMissingException;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil.Gamut;

/**
 * JUnit test for edge cases of OnState and Dimming event and cache resources.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class OnStateDimmingEdgeCaseTest {

    @Test
    void getBrightnessStateWhenDimmingMissingReturnNull() throws CriticalFieldMissingException {
        assertThat(createLightResource(true, null).getBrightnessState(), is(equalTo(UnDefType.NULL)));
    }

    @Test
    void getBrightnessStateWhenOnAndDimming75ReturnBrightness75() throws CriticalFieldMissingException {
        assertThat(createLightResource(true, 75.0).getBrightnessState(), is(equalTo(new PercentType(75))));
    }

    @Test
    void getBrightnessStateWhenOnAndDimming125ReturnBrightness100() throws CriticalFieldMissingException {
        assertThat(createLightResource(true, 125.0).getBrightnessState(), is(equalTo(new PercentType(100))));
    }

    @Test
    void getBrightnessStateWhenOffAndDimming100ReturnBrightness0() throws CriticalFieldMissingException {
        assertThat(createLightResource(false, 100.0).getBrightnessState(), is(equalTo(new PercentType(0))));
    }

    @Test
    void getBrightnessStateWhenOnStateMissingAndDimming0Return0() throws CriticalFieldMissingException {
        assertThat(createLightResource(null, 0.0).getBrightnessState(), is(equalTo(PercentType.ZERO)));
    }

    @Test
    void getBrightnessStateWhenOnStateMissingAndDimming100ReturnBrightness100() throws CriticalFieldMissingException {
        assertThat(createLightResource(null, 100.0).getBrightnessState(), is(equalTo(new PercentType(100))));
    }

    @Test
    void getBrightnessStateWhenOnStateMissingAndDimmingMinus1Return0() throws CriticalFieldMissingException {
        assertThat(createLightResource(null, -1.0).getBrightnessState(), is(equalTo(PercentType.ZERO)));
    }

    @Test
    void getBrightnessStateWhenOnAndDimmingMinus1ReturnMinBrightnessWhenOn() throws CriticalFieldMissingException {
        assertThat(createLightResource(true, -1.0).getBrightnessState(), is(equalTo(new PercentType("0.01"))));
    }

    @Test
    void getBrightnessStateWhenOnAndDimming0ReturnMinBrightnessWhenOn() throws CriticalFieldMissingException {
        assertThat(createLightResource(true, 0.0).getBrightnessState(), is(equalTo(new PercentType("0.01"))));
    }

    @Test
    void getBrightnessStateWhenOnAndDimming0AndCustomMinimumBrightnessReturnMinBrightnessWhenOn()
            throws CriticalFieldMissingException {
        assertThat(createLightResource(true, 0.0).getBrightnessState(), is(equalTo(new PercentType("0.01"))));
    }

    @Test
    void getColorStateWhenOnAndDimming0ReturnMinBrightnessWhenOn() throws CriticalFieldMissingException {
        assertThat(createLightResource(true, 0.0).getColorState(), is(equalTo(new HSBType("31.146,87.39200,0.01"))));
    }

    @Test
    void getColorStateWhenOffAndDimming100ReturnBrightness0() throws CriticalFieldMissingException {
        assertThat(createLightResource(false, 100.0).getColorState(), is(equalTo(new HSBType("31.146,87.39200,0"))));
    }

    @Test
    void getTwoStatesWhenOnAndDimming3AndMinimumBrightness2ReturnOn() throws CriticalFieldMissingException {
        // test "soft off": evaluation yields OnOffType.ON state and brightness verbatim
        Resource res = createLightResource(true, 3.0);
        assertThat(res.getBrightnessState(), is(equalTo(new PercentType(3))));
        assertThat(res.getSwitchState(), is(equalTo(OnOffType.ON)));
    }

    @Test
    void getTwoStatesWhenOnAndDimming1ReturnOn() throws CriticalFieldMissingException {
        // test "soft off": evaluation yields OnOffType.ON state and brightness verbatim
        Resource res = createLightResource(true, 1.0);
        assertThat(res.getBrightnessState(), is(equalTo(new PercentType(1))));
        assertThat(res.getSwitchState(), is(equalTo(OnOffType.ON)));
    }

    @Test
    void getTwoStatesWhenOnAndDimming0ReturnOn() throws CriticalFieldMissingException {
        // test "soft off": evaluation yields OnOffType.ON state and brightness verbatim
        Resource res = createLightResource(true, 0.0);
        assertThat(res.getBrightnessState(), is(equalTo(new PercentType("0.01"))));
        assertThat(res.getSwitchState(), is(equalTo(OnOffType.ON)));
    }

    private Resource createLightResource(@Nullable Boolean on, @Nullable Double brightness) {
        Resource resource = new Resource(ResourceType.LIGHT);

        if (on != null) {
            OnState onState = new OnState().setOn(on);
            resource.setOnState(onState);
        }

        if (brightness != null) {
            Dimming dimming = new Dimming().setBrightness(brightness);
            resource.setDimming(dimming);
        }

        var color = new ColorXy().setXY(new double[] { 0.5791, 0.3918 }).setGamut(new Gamut(
                new double[] { 0.6915, 0.3083 }, new double[] { 0.17, 0.7 }, new double[] { 0.1532, 0.0475 }));
        resource.setColorXy(color);

        return resource;
    }
}
