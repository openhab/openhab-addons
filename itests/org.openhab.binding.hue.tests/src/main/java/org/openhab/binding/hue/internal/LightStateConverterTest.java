/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.junit.Test;
import org.openhab.binding.hue.internal.State;
import org.openhab.binding.hue.internal.StateUpdate;
import org.openhab.binding.hue.internal.State.ColorMode;
import org.openhab.binding.hue.internal.handler.LightStateConverter;

/**
 *
 * @author Markus Bösling - Initial contribution
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class LightStateConverterTest {

    @Test
    public void brightnessOfZeroIsZero() {
        final State lightState = new State();
        // 0 percent should not be sent to the Hue interface
        StateUpdate stateUpdate = LightStateConverter.toBrightnessLightState(PercentType.ZERO);
        assertThat(stateUpdate.commands.size(), is(1));
        // a brightness of 0 should result in 0 percent
        lightState.bri = 0;
        assertThat(LightStateConverter.toBrightnessPercentType(lightState), is(PercentType.ZERO));
    }

    @Test
    public void brightnessLightStateConverterConversionIsBijective() {
        final State lightState = new State();
        for (int percent = 1; percent <= 100; ++percent) {
            StateUpdate stateUpdate = LightStateConverter.toBrightnessLightState(new PercentType(percent));
            assertThat(stateUpdate.commands.size(), is(2));
            assertThat(stateUpdate.commands.get(1).key, is("bri"));
            lightState.bri = Integer.parseInt(stateUpdate.commands.get(1).value.toString());
            assertThat(LightStateConverter.toBrightnessPercentType(lightState).intValue(), is(percent));
        }
    }

    @Test
    public void brightnessAlwaysGreaterThanZero() {
        final State lightState = new State();
        // a brightness greater than 1 should result in a percentage greater than 1
        for (int brightness = 1; brightness <= 254; ++brightness) {
            lightState.bri = brightness;
            assertTrue(LightStateConverter.toBrightnessPercentType(lightState).intValue() > 0);
        }
    }

    @Test
    public void colorWithBightnessOfZeroIsZero() {
        final State lightState = new State();
        lightState.colormode = ColorMode.CT.toString();
        // 0 percent should not be sent to the Hue interface
        final HSBType hsbType = new HSBType(DecimalType.ZERO, PercentType.ZERO, PercentType.ZERO);
        StateUpdate stateUpdate = LightStateConverter.toColorLightState(hsbType, lightState);
        assertThat(stateUpdate.commands.size(), is(2));
        // a brightness of 0 should result in 0 percent
        lightState.bri = 0;
        assertThat(LightStateConverter.toHSBType(lightState).getBrightness(), is(PercentType.ZERO));
    }

    @Test
    public void colorLightStateConverterForBrightnessConversionIsBijective() {
        final State lightState = new State();
        lightState.colormode = ColorMode.CT.toString();
        for (int percent = 1; percent <= 100; ++percent) {
            final HSBType hsbType = new HSBType(DecimalType.ZERO, PercentType.ZERO, new PercentType(percent));
            StateUpdate stateUpdate = LightStateConverter.toColorLightState(hsbType, lightState);
            assertThat(stateUpdate.commands.size(), is(3));
            assertThat(stateUpdate.commands.get(2).key, is("bri"));
            lightState.bri = Integer.parseInt(stateUpdate.commands.get(2).value.toString());
            assertThat(LightStateConverter.toHSBType(lightState).getBrightness().intValue(), is(percent));
        }
    }

    @Test
    public void hsbBrightnessAlwaysGreaterThanZero() {
        final State lightState = new State();
        lightState.colormode = ColorMode.CT.toString();
        // a brightness greater than 1 should result in a percentage greater than 1
        for (int brightness = 1; brightness <= 254; ++brightness) {
            lightState.bri = brightness;
            assertTrue(LightStateConverter.toHSBType(lightState).getBrightness().intValue() > 0);
        }
    }

    @Test
    public void hsbHueAlwaysGreaterThanZeroAndLessThan360() {
        final State lightState = new State();
        for (int hue = 0; hue <= 65535; ++hue) {
            lightState.hue = hue;
            assertTrue(LightStateConverter.toHSBType(lightState).getHue().intValue() >= 0);
            assertTrue(LightStateConverter.toHSBType(lightState).getHue().intValue() < 360);
        }
    }

    @Test
    public void colorLightStateConverterForSaturationConversionIsBijective() {
        final State lightState = new State();
        lightState.colormode = ColorMode.CT.toString();
        for (int percent = 0; percent <= 100; ++percent) {
            final HSBType hsbType = new HSBType(DecimalType.ZERO, new PercentType(percent), PercentType.HUNDRED);
            StateUpdate stateUpdate = LightStateConverter.toColorLightState(hsbType, lightState);
            assertThat(stateUpdate.commands.size(), is(3));
            assertThat(stateUpdate.commands.get(1).key, is("sat"));
            lightState.sat = Integer.parseInt(stateUpdate.commands.get(1).value.toString());
            assertThat(LightStateConverter.toHSBType(lightState).getSaturation().intValue(), is(percent));
        }
    }

    @Test
    public void colorLightStateConverterForHueConversionIsBijective() {
        final State lightState = new State();
        for (int hue = 0; hue < 360; ++hue) {
            final HSBType hsbType = new HSBType(new DecimalType(hue), PercentType.HUNDRED, PercentType.HUNDRED);
            StateUpdate stateUpdate = LightStateConverter.toColorLightState(hsbType, lightState);
            assertThat(stateUpdate.commands.size(), is(3));
            assertThat(stateUpdate.commands.get(0).key, is("hue"));
            lightState.hue = Integer.parseInt(stateUpdate.commands.get(0).value.toString());
            assertThat(LightStateConverter.toHSBType(lightState).getHue().intValue(), is(hue));
        }
    }

    @Test
    public void hsbSaturationAlwaysGreaterThanZero() {
        final State lightState = new State();
        lightState.colormode = ColorMode.CT.toString();
        // a saturation greater than 1 should result in a percentage greater than 1
        for (int saturation = 1; saturation <= 254; ++saturation) {
            lightState.sat = saturation;
            assertTrue(LightStateConverter.toHSBType(lightState).getSaturation().intValue() > 0);
        }
    }
}
