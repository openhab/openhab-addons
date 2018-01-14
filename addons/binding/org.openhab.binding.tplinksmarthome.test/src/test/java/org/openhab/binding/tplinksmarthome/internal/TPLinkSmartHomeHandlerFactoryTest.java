/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants;
import org.openhab.binding.tplinksmarthome.handler.SmartHomeHandler;
import org.openhab.binding.tplinksmarthome.internal.device.BulbDevice;
import org.openhab.binding.tplinksmarthome.internal.device.EnergySwitchDevice;
import org.openhab.binding.tplinksmarthome.internal.device.SwitchDevice;

/**
 * Test class for {@link TPLinkSmartHomeHandlerFactory}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@RunWith(value = Parameterized.class)
public class TPLinkSmartHomeHandlerFactoryTest {

    private static final String SMART_HOME_DEVICE_FIELD = "smartHomeDevice";

    private final TPLinkSmartHomeHandlerFactory factory = new TPLinkSmartHomeHandlerFactory();

    // @formatter:off
    private static final List<Object[]> TESTS = Arrays.asList(new Object[][] {
            { "hs100", SwitchDevice.class },
            { "hs110", EnergySwitchDevice.class },
            { "hs200", SwitchDevice.class },
            { "lb100", BulbDevice.class },
            { "lb120", BulbDevice.class },
            { "lb130", BulbDevice.class },
            { "lb230", BulbDevice.class },
            { "re270", SwitchDevice.class },
            { "unknown", null },
    });
    // @formatter:on

    @Mock
    Thing thing;

    private final String name;
    private final Class<?> clazz;

    public TPLinkSmartHomeHandlerFactoryTest(String name, Class<?> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    @Parameters(name = "{0} - {1}")
    public static List<Object[]> data() {
        return TESTS;
    }

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testCorrectClass()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID(TPLinkSmartHomeBindingConstants.BINDING_ID, name));
        SmartHomeHandler handler = (SmartHomeHandler) factory.createHandler(thing);

        if (clazz == null) {
            assertNull(name + " should not return any handler but null", handler);
        } else {
            assertNotNull(name + " should no return null handler", handler);
            Field smartHomeDeviceField = SmartHomeHandler.class.getDeclaredField(SMART_HOME_DEVICE_FIELD);

            smartHomeDeviceField.setAccessible(true);
            assertSame(name + " should return expected device class", clazz,
                    smartHomeDeviceField.get(handler).getClass());
        }
    }
}
