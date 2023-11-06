/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType.HS300;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;
import org.openhab.binding.tplinksmarthome.internal.model.SetRelayState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;

/**
 * Test class for {@link PowerStripDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class PowerStripDeviceTest extends DeviceTestBase<PowerStripDevice> {

    private static final ChannelUID CHANNEL_OUTLET_1 = ChannelUIDConstants.createChannel(HS300,
            CHANNEL_OUTLET_GROUP_PREFIX + '1', CHANNEL_SWITCH);
    private static final ChannelUID CHANNEL_OUTLET_2 = ChannelUIDConstants.createChannel(HS300,
            CHANNEL_OUTLET_GROUP_PREFIX + '2', CHANNEL_SWITCH);
    private static final ChannelUID CHANNEL_ENERGY_CURRENT_OUTLET_2 = ChannelUIDConstants.createChannel(HS300,
            CHANNEL_OUTLET_GROUP_PREFIX + '2', CHANNEL_ENERGY_CURRENT);
    private static final String[] REALTIME_INPUTS = IntStream.range(0, 6).mapToObj(i -> "hs300_get_realtime")
            .collect(Collectors.toList()).toArray(new String[0]);
    private static final String[] REALTIME_RESPONSES = IntStream.range(0, 6).mapToObj(i -> "plug_get_realtime_response")
            .collect(Collectors.toList()).toArray(new String[0]);

    public PowerStripDeviceTest() throws IOException {
        super(new PowerStripDevice(HS300), "hs300_get_sysinfo_response");
    }

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        final AtomicInteger inputCounter = new AtomicInteger(0);
        final Function<String, String> inputWrapper = s -> s.replace("001", "00" + inputCounter.incrementAndGet());

        assertInput(inputWrapper, Function.identity(), REALTIME_INPUTS);
        setSocketReturnAssert(REALTIME_RESPONSES);
        device.refreshedDeviceState(deviceState);
    }

    @Test
    public void testHandleCommandSwitchChannel2() throws IOException {
        Function<String, String> normalize = s -> ModelTestUtil.GSON
                .toJson(ModelTestUtil.GSON.fromJson(s, SetRelayState.class));
        assertInput(normalize, normalize, "hs300_set_relay_state");
        setSocketReturnAssert("hs300_set_relay_state_response");
        assertTrue(device.handleCommand(CHANNEL_OUTLET_2, OnOffType.ON), "Outlet channel 2 should be handled");
    }

    @Test
    public void testUpdateChannelOutlet1() {
        assertSame(OnOffType.ON, device.updateChannel(CHANNEL_OUTLET_1, deviceState), "Outlet 1 should be on");
    }

    @Test
    public void testUpdateChannelOutlet2() {
        assertSame(OnOffType.OFF, device.updateChannel(CHANNEL_OUTLET_2, deviceState), "Outlet 2 should be off");
    }

    @Test
    public void testUpdateChannelEnergyCurrent() {
        assertEquals(1,
                ((QuantityType<?>) device.updateChannel(CHANNEL_ENERGY_CURRENT_OUTLET_2, deviceState)).intValue(),
                "Energy current should have valid state value");
    }
}
