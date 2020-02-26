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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.TestUtil;
import org.openhab.binding.hive.internal.client.HeatingThermostatOperatingMode;
import org.openhab.binding.hive.internal.client.HeatingThermostatOperatingState;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.OverrideMode;
import org.openhab.binding.hive.internal.client.feature.Feature;
import org.openhab.binding.hive.internal.client.feature.HeatingThermostatFeature;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class HeatingThermostatHandlerStrategyTest {
    @NonNullByDefault({})
    @Mock
    private Channel operatingModeChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID operatingModeChannelUid;

    @NonNullByDefault({})
    @Mock
    private Channel operatingStateChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID operatingStateChannelUid;

    @NonNullByDefault({})
    @Mock
    private Channel targetHeatTemperatureChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID targetHeatTemperatureChannelUid;

    @NonNullByDefault({})
    @Mock
    private Channel overrideModeChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID overrideModeChannelUid;

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
                HiveBindingConstants.CHANNEL_MODE_OPERATING,
                this.operatingModeChannel,
                this.operatingModeChannelUid
        );

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_STATE_OPERATING,
                this.operatingStateChannel,
                this.operatingStateChannelUid
        );

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_TEMPERATURE_TARGET,
                this.targetHeatTemperatureChannel,
                this.targetHeatTemperatureChannelUid
        );

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_MODE_OPERATING_OVERRIDE,
                this.overrideModeChannel,
                this.overrideModeChannelUid
        );
    }

    @Test
    public void testNormalUpdate() {
        /* Given */
        final HeatingThermostatHandlerStrategy strategy = new HeatingThermostatHandlerStrategy();

        final HeatingThermostatOperatingMode operatingMode = HeatingThermostatOperatingMode.SCHEDULE;
        final HeatingThermostatOperatingState operatingState = HeatingThermostatOperatingState.HEAT;
        final Quantity<Temperature> targetHeatTemperature = Quantities.getQuantity(20, Units.CELSIUS);
        final OverrideMode overrideMode = OverrideMode.NONE;

        final HeatingThermostatFeature heatingThermostatFeature = HeatingThermostatFeature.builder()
                .operatingMode(TestUtil.createSimpleFeatureAttribute(operatingMode))
                .operatingState(TestUtil.createSimpleFeatureAttribute(operatingState))
                .targetHeatTemperature(TestUtil.createSimpleFeatureAttribute(targetHeatTemperature))
                .temporaryOperatingModeOverride(TestUtil.createSimpleFeatureAttribute(overrideMode))
                .build();

        final Map<Class<? extends Feature>, Feature> features = new HashMap<>();
        features.put(HeatingThermostatFeature.class, heatingThermostatFeature);

        final Node node = TestUtil.getTestNodeWithFeatures(features);


        /* When */
        strategy.handleUpdate(
                this.thing,
                this.thingHandlerCallback,
                node
        );


        /* Then */
        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.operatingModeChannelUid),
                eq(new StringType(operatingMode.toString()))
        );

        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.operatingStateChannelUid),
                eq(new StringType(operatingState.toString()))
        );

        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.targetHeatTemperatureChannelUid),
                eq(new QuantityType<>(targetHeatTemperature.getValue(), SIUnits.CELSIUS))
        );

        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.overrideModeChannelUid),
                eq(OnOffType.OFF)
        );
    }
}
