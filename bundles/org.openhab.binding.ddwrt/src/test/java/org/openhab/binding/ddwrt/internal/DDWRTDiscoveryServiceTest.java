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
package org.openhab.binding.ddwrt.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ddwrt.internal.api.DDWRTRadio;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link DDWRTDiscoveryService}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class DDWRTDiscoveryServiceTest {

    @Test
    void testVirtualRadioInterfaceCreatesValidThingUid() {
        DDWRTRadio radio = new DDWRTRadio("24:f5:a2:c6:16:59", "wlan0.1");
        ThingUID bridgeUid = new ThingUID(DDWRTBindingConstants.BRIDGE_TYPE_NETWORK, "test");

        ThingUID thingUid = DDWRTDiscoveryService.createRadioThingUID(bridgeUid, radio.getParentDeviceMac(),
                radio.getIfaceName());

        assertThat(thingUid.getId(), is("24-f5-a2-c6-16-59-wlan0-1"));
        assertThat(radio.getInterfaceId(), is("24:f5:a2:c6:16:59:wlan0.1"));
    }
}
