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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * Tests for {@link DDWRTOpenWrtDevice}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class DDWRTOpenWrtDeviceTest {

    private static final String DEVICE_MAC = "aa:bb:cc:dd:ee:ff";
    private static final String RADIO_LOOKUP_COMMAND = "ubus call network.wireless status | jsonfilter -e "
            + "'@.*.interfaces[@.ifname=\"phy1-ap0\"].config.device[0]'";

    @SuppressWarnings("null")
    @Test
    void testDoesNotEnumerateGeneratedFirewallRules() {
        SshRunner runner = mock(SshRunner.class);
        DDWRTOpenWrtDevice device = new DDWRTOpenWrtDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        assertThat(device.enumerateFirewallRules(runner), empty());
        verifyNoInteractions(runner);
    }

    @SuppressWarnings("null")
    @Test
    void testEnumeratesActualRadioEnabledState() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout("iwinfo")).thenReturn("""
                phy0-ap0 ESSID: "magickingdom"
                         Mode: Master  Channel: 6 (2.437 GHz)
                phy1-ap0 ESSID: "magic5"
                         Mode: Master  Channel: 0 (unknown GHz)
                """);
        when(runner.execStdout("cat /sys/class/net/phy0-ap0/flags")).thenReturn("0x1003\n");
        when(runner.execStdout("cat /sys/class/net/phy1-ap0/flags")).thenReturn("0x1002\n");
        when(runner.execStdout("iwinfo phy0-ap0 info | grep -i channel | head -1 | grep -oE '[0-9]+'"))
                .thenReturn("6\n");
        when(runner.execStdout("iwinfo phy1-ap0 info | grep -i channel | head -1 | grep -oE '[0-9]+'"))
                .thenReturn("0\n");
        List<DDWRTRadio> radios = IwinfoParser.enumerateRadios(mock(Logger.class), runner, DEVICE_MAC);

        assertThat(radios.size(), is(2));
        assertThat(radios.get(0).isEnabled(), is(true));
        assertThat(radios.get(1).isEnabled(), is(false));
    }

    @SuppressWarnings("null")
    @Test
    void testControlsRadioThroughOpenWrtWirelessService() throws IOException {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(RADIO_LOOKUP_COMMAND)).thenReturn("radio1");
        DDWRTOpenWrtDevice device = new DDWRTOpenWrtDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        device.setRadioEnabled(runner, "phy1-ap0", false);
        device.setRadioEnabled(runner, "phy1-ap0", true);

        verify(runner).exec("/sbin/wifi down radio1");
        verify(runner).exec("/sbin/wifi up radio1");
    }

    @SuppressWarnings("null")
    @Test
    void testRejectsRadioControlWithoutOpenWrtWirelessDevice() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.execStdout(RADIO_LOOKUP_COMMAND)).thenReturn("");
        DDWRTOpenWrtDevice device = new DDWRTOpenWrtDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        assertThrows(IOException.class, () -> device.setRadioEnabled(runner, "phy1-ap0", true));
    }
}
