/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yeelight.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.yeelight.internal.YeelightBindingConstants.PARAMETER_DEVICE_ID;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.openhab.binding.yeelight.internal.handler.*;

/**
 * Unit tests for {@link YeelightHandlerFactory}
 *
 * @author Viktor Koop - Initial contribution
 * @author Nikita Pogudalov - Added YeelightCeilingWithNightHandler for Ceiling 1
 */
@RunWith(value = Parameterized.class)
public class YeelightHandlerFactoryTest {

    private static final List<Object[]> TESTS = Arrays.asList(
            new Object[][] { { "dolphin", YeelightWhiteHandler.class }, { "ct_bulb", YeelightWhiteHandler.class },
                    { "wonder", YeelightColorHandler.class }, { "stripe", YeelightStripeHandler.class },
                    { "ceiling", YeelightCeilingHandler.class }, { "ceiling3", YeelightCeilingHandler.class },
                    { "ceiling1", YeelightCeilingWithNightHandler.class }, { "desklamp", YeelightCeilingHandler.class },
                    { "ceiling4", YeelightCeilingWithAmbientHandler.class }, { "unknown", null } });

    private final YeelightHandlerFactory factory = new YeelightHandlerFactory();

    @Mock
    private Thing thing;

    private final String name;
    private final Class<?> clazz;

    public YeelightHandlerFactoryTest(String name, Class<?> clazz) {
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
        Configuration configuration = new Configuration();
        configuration.put(PARAMETER_DEVICE_ID, "");

        when(thing.getConfiguration()).thenReturn(configuration);
    }

    @Test
    public void testCorrectClass() {
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID(YeelightBindingConstants.BINDING_ID, name));
        ThingHandler handler = factory.createHandler(thing);

        if (null == clazz) {
            assertNull(name + " should not return any handler but null", handler);
        } else {
            assertNotNull(name + " should no return null handler", handler);
            assertEquals(" should be correct matcher", clazz, handler.getClass());

            assertEquals(thing, handler.getThing());
        }
    }
}
