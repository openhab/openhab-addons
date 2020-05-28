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
package org.openhab.binding.hive.internal.handler.strategy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.TestUtil;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.PhysicalDeviceFeature;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class PhysicalDeviceHandlerStrategyTest {
    @NonNullByDefault({})
    @Mock
    private Thing thing;

    @NonNullByDefault({})
    @Mock
    private ThingHandlerCallback thingHandlerCallback;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testUpdate() {
        /* Given */
        final String manufacturer = "TestManufacturer";
        final String model = "TestModel";
        final String softwareVersion = "TestSoftwareVersion";
        final String hardwareIdentifier = "TestHardwareIdentifier";

        final PhysicalDeviceHandlerStrategy strategy = new PhysicalDeviceHandlerStrategy();

        // Create the test node
        final PhysicalDeviceFeature physicalDeviceFeature = PhysicalDeviceFeature.builder()
                .manufacturer(TestUtil.createSimpleFeatureAttribute(manufacturer))
                .model(TestUtil.createSimpleFeatureAttribute(model))
                .softwareVersion(TestUtil.createSimpleFeatureAttribute(softwareVersion))
                .hardwareIdentifier(TestUtil.createSimpleFeatureAttribute(hardwareIdentifier))
                .build();

        final Node node = TestUtil.getTestNodeWithFeatures(Collections.singletonMap(
                PhysicalDeviceFeature.class,
                physicalDeviceFeature
        ));


        /* When */
        strategy.handleUpdate(
                this.thing,
                this.thingHandlerCallback,
                node
        );


        /* Then */
        verify(this.thing).setProperty(
                eq(Thing.PROPERTY_VENDOR),
                eq(manufacturer)
        );

        verify(this.thing).setProperty(
                eq(Thing.PROPERTY_MODEL_ID),
                eq(model)
        );

        verify(this.thing).setProperty(
                eq(Thing.PROPERTY_FIRMWARE_VERSION),
                eq(softwareVersion)
        );

        verify(this.thing).setProperty(
                eq(Thing.PROPERTY_SERIAL_NUMBER),
                eq(hardwareIdentifier)
        );
    }
}
