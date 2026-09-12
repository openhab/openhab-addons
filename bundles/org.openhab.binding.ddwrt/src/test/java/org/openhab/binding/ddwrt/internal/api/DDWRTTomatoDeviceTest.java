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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * Tests for {@link DDWRTTomatoDevice}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class DDWRTTomatoDeviceTest {

    private static final String GET_CONFIGURED_IFACES_COMMAND = "nvram get wl_ifnames; nvram get wl0_ifname; "
            + "nvram get wl1_ifname; nvram get wl2_ifname";

    @SuppressWarnings("null")
    @Test
    void testEnumerateRadiosUsesConfiguredInterfaceAndReadsState() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(anyString())).thenReturn("");
        when(runner.execStdout(GET_CONFIGURED_IFACES_COMMAND)).thenReturn("eth1");
        when(runner.execStdout("wl -i eth1 ssid")).thenReturn("Current SSID: \"FreshTomato24\"");
        when(runner.execStdout("wl -i eth1 status | awk '/Control channel:/ {print $3; exit}'")).thenReturn("6");
        when(runner.execStdout("wl -i eth1 radio")).thenReturn("0x0000");

        DDWRTTomatoDevice device = new DDWRTTomatoDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));
        device.mac = "6c:b0:ce:ba:b2:6e";

        List<DDWRTRadio> radios = device.enumerateRadios(runner);

        assertThat(radios.stream().map(DDWRTRadio::getIfaceName).toList(), contains("eth1"));
        assertThat(radios.getFirst().getSsid(), is("FreshTomato24"));
        assertThat(radios.getFirst().getChannel(), is(6));
        assertThat(radios.getFirst().isEnabled(), is(true));
    }

    @SuppressWarnings("null")
    @Test
    void testEnumerateRadiosFallsBackToCurrentChannel() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(anyString())).thenReturn("");
        when(runner.execStdout(GET_CONFIGURED_IFACES_COMMAND)).thenReturn("eth1");
        when(runner.execStdout("wl -i eth1 ssid")).thenReturn("Current SSID: \"legacy\"");
        when(runner.execStdout("wl -i eth1 channel | grep 'current' | awk '{print $NF}'")).thenReturn("11");

        DDWRTTomatoDevice device = new DDWRTTomatoDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));
        device.mac = "6c:b0:ce:ba:b2:6e";

        assertThat(device.enumerateRadios(runner).getFirst().getChannel(), is(11));
    }

    @SuppressWarnings("null")
    @Test
    void testEnumerateRadiosReadsDisabledState() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(anyString())).thenReturn("");
        when(runner.execStdout(GET_CONFIGURED_IFACES_COMMAND)).thenReturn("eth1");
        when(runner.execStdout("wl -i eth1 ssid")).thenReturn("Current SSID: \"FreshTomato24\"");
        when(runner.execStdout("wl -i eth1 radio")).thenReturn("0x0001");

        DDWRTTomatoDevice device = new DDWRTTomatoDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));
        device.mac = "6c:b0:ce:ba:b2:6e";

        assertThat(device.enumerateRadios(runner).getFirst().isEnabled(), is(false));
    }

    @SuppressWarnings("null")
    @Test
    void testSetRadioEnabledUsesCheckedExecution() throws IOException {
        SshRunner runner = mock(SshRunner.class);
        DDWRTTomatoDevice device = new DDWRTTomatoDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        device.setRadioEnabled(runner, "eth1", false);
        device.setRadioEnabled(runner, "eth1", true);

        verify(runner).exec("wl -i eth1 radio off");
        verify(runner).exec("wl -i eth1 radio on");
    }

    @SuppressWarnings("null")
    @Test
    void testSetRadioEnabledPropagatesCommandFailure() throws IOException {
        SshRunner runner = mock(SshRunner.class);
        when(runner.exec("wl -i eth1 radio off")).thenThrow(new IOException("command failed"));
        DDWRTTomatoDevice device = new DDWRTTomatoDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        assertThrows(IOException.class, () -> device.setRadioEnabled(runner, "eth1", false));
    }
}
