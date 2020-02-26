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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.RefreshType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.TestUtil;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.TransientModeHeatingActionsFeature;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class HeatingTransientModeHandlerStrategyTest {
    @NonNullByDefault({})
    @Mock
    private Channel boostTargetTemperatureChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID boostTargetTemperatureChannelUid;

    @NonNullByDefault({})
    @Mock
    private Thing thing;

    @NonNullByDefault({})
    @Mock
    private ThingHandlerCallback thingHandlerCallback;

    @Before
    public void setUp() {
        initMocks(this);

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_TEMPERATURE_TARGET_BOOST,
                this.boostTargetTemperatureChannel,
                this.boostTargetTemperatureChannelUid
        );
    }

    @Test
    public void testNormalUpdate() {
        /* Given */
        final HeatingTransientModeHandlerStrategy strategy = new HeatingTransientModeHandlerStrategy();

        final Quantity<Temperature> boostTargetTemperature = Quantities.getQuantity(25, Units.CELSIUS);

        // Create the test node
        final TransientModeHeatingActionsFeature transientModeHeatingActionsFeature = TransientModeHeatingActionsFeature.builder()
                .boostTargetTemperature(TestUtil.createSimpleFeatureAttribute(boostTargetTemperature))
                .build();

        final Node node = TestUtil.getTestNodeWithFeatures(Collections.singletonMap(
                TransientModeHeatingActionsFeature.class, transientModeHeatingActionsFeature)
        );


        /* When */
        strategy.handleUpdate(
                this.thing,
                this.thingHandlerCallback,
                node
        );


        /* Then */
        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.boostTargetTemperatureChannelUid),
                eq(new QuantityType<>(boostTargetTemperature.getValue(), boostTargetTemperature.getUnit()))
        );
    }

    @Test
    public void testMissingFeatureUpdate() {
        /* Given */
        final HeatingTransientModeHandlerStrategy strategy = new HeatingTransientModeHandlerStrategy();

        // Create the test node
        final Node node = TestUtil.getTestNodeWithFeatures(Collections.emptyMap());


        /* When */
        strategy.handleUpdate(
                this.thing,
                this.thingHandlerCallback,
                node
        );


        /* Then */
        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.boostTargetTemperatureChannelUid),
                eq(new QuantityType<>(
                        HeatingTransientModeHandlerStrategy.DEFAULT_BOOST_TEMPERATURE.getValue(),
                        HeatingTransientModeHandlerStrategy.DEFAULT_BOOST_TEMPERATURE.getUnit()
                ))
        );
    }

    @Test
    public void testNormalCommand() {
        /* Given */
        final HeatingTransientModeHandlerStrategy strategy = new HeatingTransientModeHandlerStrategy();

        final Quantity<Temperature> boostTargetTemperature = Quantities.getQuantity(25, Units.CELSIUS);
        final Quantity<Temperature> newBoostTargetTemperature = Quantities.getQuantity(24, Units.CELSIUS);

        // Create the test node
        final TransientModeHeatingActionsFeature transientModeHeatingActionsFeature = TransientModeHeatingActionsFeature.builder()
                .boostTargetTemperature(TestUtil.createSimpleFeatureAttribute(boostTargetTemperature))
                .build();

        final Node node = TestUtil.getTestNodeWithFeatures(Collections.singletonMap(
                TransientModeHeatingActionsFeature.class, transientModeHeatingActionsFeature)
        );


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.boostTargetTemperatureChannelUid,
                new QuantityType<>(
                        newBoostTargetTemperature.getValue(),
                        newBoostTargetTemperature.getUnit()
                ),
                node
        );


        /* Then */
        assertThat(updatedNode).isNotNull();

        final @Nullable TransientModeHeatingActionsFeature newTransientModeHeatingActionsFeature = updatedNode.getFeature(TransientModeHeatingActionsFeature.class);
        assertThat(newTransientModeHeatingActionsFeature).isNotNull();

        assertThat(newTransientModeHeatingActionsFeature.getBoostTargetTemperature().getTargetValue())
                .isEqualTo(newBoostTargetTemperature);
    }

    @Test
    public void testNormalRefresh() {
        /* Given */
        final HeatingTransientModeHandlerStrategy strategy = new HeatingTransientModeHandlerStrategy();

        // Create the test node
        final Node node = TestUtil.getTestNodeWithFeatures(Collections.emptyMap());


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.boostTargetTemperatureChannelUid,
                RefreshType.REFRESH,
                node
        );


        /* Then */
        assertThat(updatedNode).isNull();
    }
}
