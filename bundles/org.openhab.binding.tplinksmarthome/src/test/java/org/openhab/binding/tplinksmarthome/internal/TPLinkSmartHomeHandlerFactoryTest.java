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
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tplinksmarthome.internal.device.BulbDevice;
import org.openhab.binding.tplinksmarthome.internal.device.DimmerDevice;
import org.openhab.binding.tplinksmarthome.internal.device.EnergySwitchDevice;
import org.openhab.binding.tplinksmarthome.internal.device.PowerStripDevice;
import org.openhab.binding.tplinksmarthome.internal.device.RangeExtenderDevice;
import org.openhab.binding.tplinksmarthome.internal.device.SwitchDevice;
import org.openhab.binding.tplinksmarthome.internal.handler.SmartHomeHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Test class for {@link TPLinkSmartHomeHandlerFactory}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class TPLinkSmartHomeHandlerFactoryTest {

    private static final String SMART_HOME_DEVICE_FIELD = "smartHomeDevice";

    private final TPLinkSmartHomeHandlerFactory factory = new TPLinkSmartHomeHandlerFactory();

    // @formatter:off
    private static final List<Object[]> TESTS = Arrays.asList(new Object[][] {
            { "hs100", SwitchDevice.class },
            { "hs110", EnergySwitchDevice.class },
            { "hs200", SwitchDevice.class },
            { "hs220", DimmerDevice.class },
            { "hs300", PowerStripDevice.class },
            { "lb100", BulbDevice.class },
            { "lb120", BulbDevice.class },
            { "lb130", BulbDevice.class },
            { "lb230", BulbDevice.class },
            { "kl110", BulbDevice.class },
            { "kl120", BulbDevice.class },
            { "kl130", BulbDevice.class },
            { "re270", RangeExtenderDevice.class },
            { "unknown", null },
    });
    // @formatter:on

    private @Mock Thing thing;

    public static List<Object[]> data() {
        return TESTS;
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testCorrectClass(String name, Class<?> clazz)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID(TPLinkSmartHomeBindingConstants.BINDING_ID, name));
        SmartHomeHandler handler = (SmartHomeHandler) factory.createHandler(thing);

        if (clazz == null) {
            assertNull(handler, name + " should not return any handler but null");
        } else {
            assertNotNull(handler, name + " should no return null handler");
            Field smartHomeDeviceField = SmartHomeHandler.class.getDeclaredField(SMART_HOME_DEVICE_FIELD);

            smartHomeDeviceField.setAccessible(true);
            assertSame(clazz, smartHomeDeviceField.get(handler).getClass(),
                    name + " should return expected device class");
        }
    }
}
