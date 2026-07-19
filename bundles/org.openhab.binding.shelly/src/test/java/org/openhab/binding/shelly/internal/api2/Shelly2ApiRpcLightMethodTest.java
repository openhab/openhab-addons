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
package org.openhab.binding.shelly.internal.api2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc.AutoTimerTarget;

/**
 * Tests for {@link Shelly2ApiRpc#lightSetMethod} and {@link Shelly2ApiRpc#autoTimerTarget}, the RPC
 * method/component dispatch helpers used to select the correct Light/CCT/RGBCCT/RGB RPC call for a device profile.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2ApiRpcLightMethodTest {

    @Test
    void lightSetMethodSelectsRgbForRgbBulb() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSCOLORBULB);
        String method = Shelly2ApiRpc.lightSetMethod(profile, SHELLYRPC_METHOD_CCT_SET, SHELLYRPC_METHOD_RGBCCT_SET,
                SHELLYRPC_METHOD_RGB_SET, SHELLYRPC_METHOD_LIGHT_SET);
        assertThat(method, is(SHELLYRPC_METHOD_RGB_SET));
    }

    @Test
    void lightSetMethodSelectsCctForDuoCctOnly() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSDUOBULB);
        profile.isRGBCCT = false;
        String method = Shelly2ApiRpc.lightSetMethod(profile, SHELLYRPC_METHOD_CCT_SET, SHELLYRPC_METHOD_RGBCCT_SET,
                SHELLYRPC_METHOD_RGB_SET, SHELLYRPC_METHOD_LIGHT_SET);
        assertThat(method, is(SHELLYRPC_METHOD_CCT_SET));
    }

    @Test
    void lightSetMethodSelectsRgbcctForDuoRgbcct() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSDUOBULB);
        profile.isRGBCCT = true;
        String method = Shelly2ApiRpc.lightSetMethod(profile, SHELLYRPC_METHOD_CCT_SET, SHELLYRPC_METHOD_RGBCCT_SET,
                SHELLYRPC_METHOD_RGB_SET, SHELLYRPC_METHOD_LIGHT_SET);
        assertThat(method, is(SHELLYRPC_METHOD_RGBCCT_SET));
    }

    @Test
    void lightSetMethodSelectsFallbackForNonBulb() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSRGBWPM);
        String method = Shelly2ApiRpc.lightSetMethod(profile, SHELLYRPC_METHOD_CCT_SET, SHELLYRPC_METHOD_RGBCCT_SET,
                SHELLYRPC_METHOD_RGB_SET, SHELLYRPC_METHOD_LIGHT_SET);
        assertThat(method, is(SHELLYRPC_METHOD_LIGHT_SET));
    }

    @Test
    void autoTimerTargetSelectsRgbForRgbBulb() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSCOLORBULB);
        AutoTimerTarget target = Shelly2ApiRpc.autoTimerTarget(profile);
        assertThat(target.method(), is(SHELLYRPC_METHOD_RGB_SETCONFIG));
        assertThat(target.component(), is("RGB"));
    }

    @Test
    void autoTimerTargetSelectsCctForDuoCctOnly() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSDUOBULB);
        profile.isRGBCCT = false;
        AutoTimerTarget target = Shelly2ApiRpc.autoTimerTarget(profile);
        assertThat(target.method(), is(SHELLYRPC_METHOD_CCT_SETCONFIG));
        assertThat(target.component(), is("CCT"));
    }

    @Test
    void autoTimerTargetSelectsRgbcctForDuoRgbcct() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSDUOBULB);
        profile.isRGBCCT = true;
        AutoTimerTarget target = Shelly2ApiRpc.autoTimerTarget(profile);
        assertThat(target.method(), is(SHELLYRPC_METHOD_RGBCCT_SETCONFIG));
        assertThat(target.component(), is("RGBCCT"));
    }

    @Test
    void autoTimerTargetSelectsLightForOtherLight() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSRGBWPM);
        AutoTimerTarget target = Shelly2ApiRpc.autoTimerTarget(profile);
        assertThat(target.method(), is(SHELLYRPC_METHOD_LIGHT_SETCONFIG));
        assertThat(target.component(), is("Light"));
    }

    @Test
    void autoTimerTargetSelectsSwitchForNonLight() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        AutoTimerTarget target = Shelly2ApiRpc.autoTimerTarget(profile);
        assertThat(target.method(), is(SHELLYRPC_METHOD_SWITCH_SETCONFIG));
        assertThat(target.component(), is("Switch"));
    }
}
