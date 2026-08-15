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
package org.openhab.binding.ddwrt.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * Tests for {@link DDWRTBroadcomDevice}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class DDWRTBroadcomDeviceTest {

    @SuppressWarnings("null")
    @Test
    void testEnumerateRadiosRefreshesVapsAndRetriesPhysicalInterfaces() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(anyString())).thenReturn("");
        when(runner.execStdout("nvram get wl0_ifname; nvram get wl1_ifname; nvram get wl2_ifname; "
                + "nvram get wl0_vifs; nvram get wl1_vifs; nvram get wl2_vifs"))
                .thenReturn("eth1 eth2", "eth1 eth2 wl0.1 wl0.2", "eth1 wl0.1 wl0.2");
        when(runner.execStdout("wl -i eth1 ssid | awk -F'\"' '{print $2}'")).thenReturn("main-24");
        when(runner.execStdout("wl -i eth2 ssid | awk -F'\"' '{print $2}'")).thenReturn("", "main-5", "", "main-5");
        when(runner.execStdout("wl -i wl0.1 ssid | awk -F'\"' '{print $2}'")).thenReturn("guest");
        when(runner.execStdout("wl -i wl0.2 ssid | awk -F'\"' '{print $2}'")).thenReturn("iot");

        DDWRTBroadcomDevice device = new DDWRTBroadcomDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));
        device.mac = "c0:ff:d4:a5:61:e6";

        List<DDWRTRadio> initialRadios = device.enumerateRadios(runner);
        List<DDWRTRadio> radiosAfterInitialPhysicalInterfaceRecovers = device.enumerateRadios(runner);
        List<DDWRTRadio> radiosDuringCachedPhysicalInterfaceFailure = device.enumerateRadios(runner);
        List<DDWRTRadio> radiosAfterCachedPhysicalInterfaceRecovers = device.enumerateRadios(runner);

        assertThat(initialRadios.stream().map(DDWRTRadio::getIfaceName).toList(), contains("eth1"));
        assertThat(radiosAfterInitialPhysicalInterfaceRecovers.stream().map(DDWRTRadio::getIfaceName).toList(),
                contains("eth1", "eth2", "wl0.1", "wl0.2"));
        assertThat(radiosDuringCachedPhysicalInterfaceFailure.stream().map(DDWRTRadio::getIfaceName).toList(),
                contains("eth1", "wl0.1", "wl0.2"));
        assertThat(radiosAfterCachedPhysicalInterfaceRecovers.stream().map(DDWRTRadio::getIfaceName).toList(),
                contains("eth1", "eth2", "wl0.1", "wl0.2"));
    }
}
