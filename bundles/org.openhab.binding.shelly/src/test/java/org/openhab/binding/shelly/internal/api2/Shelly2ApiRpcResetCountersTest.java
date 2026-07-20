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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;

@NonNullByDefault
public class Shelly2ApiRpcResetCountersTest {

    @Test
    void threePhaseUsesEmDataResetRegardlessOfRelays() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.is3EM = true;
        profile.hasRelays = true;

        assertEquals(SHELLYRPC_METHOD_EMDATARESET, Shelly2ApiRpc.resetCountersMethod(profile));
    }

    @Test
    void relayPmUsesSwitchReset() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.hasRelays = true;

        assertEquals(SHELLYRPC_METHOD_SWITCH_RESETCOUNTERS, Shelly2ApiRpc.resetCountersMethod(profile));
    }

    @Test
    void clampMeterUsesEm1DataReset() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.isEM1 = true;

        assertEquals(SHELLYRPC_METHOD_EM1DATARESET, Shelly2ApiRpc.resetCountersMethod(profile));
    }

    @Test
    void clampMeterWithRelayStillUsesEm1DataReset() {
        // Pro EM-50 exposes both em1-clamp meters and a dry-contact relay; isEM1 must win over hasRelays.
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.isEM1 = true;
        profile.hasRelays = true;

        assertEquals(SHELLYRPC_METHOD_EM1DATARESET, Shelly2ApiRpc.resetCountersMethod(profile));
    }

    @Test
    void rollerModeUsesCoverReset() {
        // Gen2 sets hasRelays=true for roller-mode 2PM devices too; isRoller must win over hasRelays
        // because they expose cover components, not switch components.
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.isRoller = true;
        profile.hasRelays = true;

        assertEquals(SHELLYRPC_METHOD_COVER_RESETCOUNTERS, Shelly2ApiRpc.resetCountersMethod(profile));
    }

    @Test
    void standalonePowerMeterUsesPm1Reset() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);

        assertEquals(SHELLYRPC_METHOD_PM1_RESETCOUNTERS, Shelly2ApiRpc.resetCountersMethod(profile));
    }
}
