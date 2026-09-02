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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @SuppressWarnings("null")
    @Test
    void testDoesNotEnumerateGeneratedFirewallRules() {
        SshRunner runner = mock(SshRunner.class);
        DDWRTOpenWrtDevice device = new DDWRTOpenWrtDevice(new DDWRTDeviceConfiguration(), mock(Logger.class));

        assertThat(device.enumerateFirewallRules(runner), empty());
        verifyNoInteractions(runner);
    }
}
