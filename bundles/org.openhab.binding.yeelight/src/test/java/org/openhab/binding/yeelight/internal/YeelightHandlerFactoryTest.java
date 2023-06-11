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
package org.openhab.binding.yeelight.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.openhab.binding.yeelight.internal.YeelightBindingConstants.PARAMETER_DEVICE_ID;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.yeelight.internal.handler.YeelightCeilingHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightCeilingWithAmbientHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightCeilingWithNightHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightColorHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightStripeHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightWhiteHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Unit tests for {@link YeelightHandlerFactory}
 *
 * @author Viktor Koop - Initial contribution
 * @author Nikita Pogudalov - Added YeelightCeilingWithNightHandler for Ceiling 1
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class YeelightHandlerFactoryTest {

    private static final List<Object[]> TESTS = Arrays.asList(
            new Object[][] { { "dolphin", YeelightWhiteHandler.class }, { "ct_bulb", YeelightWhiteHandler.class },
                    { "wonder", YeelightColorHandler.class }, { "stripe", YeelightStripeHandler.class },
                    { "ceiling", YeelightCeilingHandler.class }, { "ceiling3", YeelightCeilingWithNightHandler.class },
                    { "ceiling1", YeelightCeilingWithNightHandler.class }, { "desklamp", YeelightCeilingHandler.class },
                    { "ceiling4", YeelightCeilingWithAmbientHandler.class }, { "unknown", null } });

    private final YeelightHandlerFactory factory = new YeelightHandlerFactory();

    private @Mock Thing thing;

    public static List<Object[]> data() {
        return TESTS;
    }

    @BeforeEach
    public void setUp() {
        Configuration configuration = new Configuration();
        configuration.put(PARAMETER_DEVICE_ID, "");

        when(thing.getConfiguration()).thenReturn(configuration);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testCorrectClass(String name, Class<?> clazz) {
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID(YeelightBindingConstants.BINDING_ID, name));
        ThingHandler handler = factory.createHandler(thing);

        if (null == clazz) {
            assertNull(handler, name + " should not return any handler but null");
        } else {
            assertNotNull(handler, name + " should no return null handler");
            assertEquals(clazz, handler.getClass(), " should be correct matcher");

            assertEquals(thing, handler.getThing());
        }
    }
}
