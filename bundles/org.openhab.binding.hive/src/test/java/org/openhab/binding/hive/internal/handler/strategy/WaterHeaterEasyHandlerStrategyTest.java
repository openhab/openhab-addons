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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.RefreshType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.TestUtil;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.OnOffMode;
import org.openhab.binding.hive.internal.client.OverrideMode;
import org.openhab.binding.hive.internal.client.WaterHeaterOperatingMode;
import org.openhab.binding.hive.internal.client.feature.Feature;
import org.openhab.binding.hive.internal.client.feature.OnOffDeviceFeature;
import org.openhab.binding.hive.internal.client.feature.TransientModeFeature;
import org.openhab.binding.hive.internal.client.feature.WaterHeaterFeature;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@RunWith(JUnitParamsRunner.class)
@NonNullByDefault
public class WaterHeaterEasyHandlerStrategyTest {
    @NonNullByDefault({})
    @Mock
    private Channel easyOperatingModeChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID easyOperatingModeChannelUid;

    @NonNullByDefault({})
    @Mock
    private Channel easyBoostModeChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID easyBoostModeChannelUid;

    @NonNullByDefault({})
    @Mock
    private Channel easyIsOnStateChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID easyIsOnStateChannelUid;

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
                HiveBindingConstants.CHANNEL_EASY_MODE_OPERATING,
                this.easyOperatingModeChannel,
                this.easyOperatingModeChannelUid
        );

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_EASY_MODE_BOOST,
                this.easyBoostModeChannel,
                this.easyBoostModeChannelUid
        );

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_EASY_STATE_IS_ON,
                this.easyIsOnStateChannel,
                this.easyIsOnStateChannelUid
        );
    }

    @Test
    @Parameters(method = "getUpdateParams")
    @TestCaseName("Test Update [operatingMode: {0}, onOffMode: {3}, isOn: {1}, overrideMode: {2}] = [mode:{4}, boost:{5}]")
    public void testUpdateEasyOperatingMode(
            final WaterHeaterOperatingMode operatingMode,
            final boolean isOn,
            final OverrideMode overrideMode,
            final OnOffMode onOffMode,
            final String expectedEasyOperatingMode,
            final OnOffType expectedBoostMode
    ) {
        /* Given */
        final WaterHeaterEasyHandlerStrategy strategy = new WaterHeaterEasyHandlerStrategy();

        // Create the test node
        final Node node = getGoodNode(
                operatingMode,
                isOn,
                overrideMode,
                onOffMode
        );


        /* When */
        strategy.handleUpdate(
                this.thing,
                this.thingHandlerCallback,
                node
        );


        /* Then */
        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.easyOperatingModeChannelUid),
                eq(new StringType(expectedEasyOperatingMode))
        );

        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.easyBoostModeChannelUid),
                eq(expectedBoostMode)
        );
    }

    public List<List<Object>> getUpdateParams() {
        return Arrays.asList(
                Arrays.asList(
                        WaterHeaterOperatingMode.SCHEDULE,
                        true,
                        OverrideMode.NONE,
                        OnOffMode.ON,
                        HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_SCHEDULE,
                        OnOffType.OFF
                ),
                Arrays.asList(
                        WaterHeaterOperatingMode.ON,
                        true,
                        OverrideMode.NONE,
                        OnOffMode.ON,
                        HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_ON,
                        OnOffType.OFF
                ),
                Arrays.asList(
                        WaterHeaterOperatingMode.ON,
                        false,
                        OverrideMode.NONE,
                        OnOffMode.OFF,
                        HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_OFF,
                        OnOffType.OFF
                ),
                Arrays.asList(
                        WaterHeaterOperatingMode.SCHEDULE,
                        false,
                        OverrideMode.NONE,
                        OnOffMode.OFF,
                        HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_OFF,
                        OnOffType.OFF
                ),
                Arrays.asList(
                        WaterHeaterOperatingMode.SCHEDULE,
                        true,
                        OverrideMode.TRANSIENT,
                        OnOffMode.ON,
                        HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_SCHEDULE,
                        OnOffType.ON
                )
        );
    }

    @Test
    public void testNormalCommandOperatingModeOnFromSchedule() {
        /* Given */
        final WaterHeaterEasyHandlerStrategy strategy = new WaterHeaterEasyHandlerStrategy();

        final WaterHeaterOperatingMode operatingMode = WaterHeaterOperatingMode.SCHEDULE;
        final OnOffMode onOffMode = OnOffMode.ON;

        // Create the test node
        final Node node = getGoodNodeForOperatingModeCommandTest(
                operatingMode,
                onOffMode
        );


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.easyOperatingModeChannelUid,
                new StringType(HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_ON),
                node
        );


        /* Then */
        assertThat(updatedNode).isNotNull();

        final @Nullable WaterHeaterFeature waterHeaterFeature = updatedNode.getFeature(WaterHeaterFeature.class);
        assertThat(waterHeaterFeature).isNotNull();

        final @Nullable OnOffDeviceFeature onOffDeviceFeature = updatedNode.getFeature(OnOffDeviceFeature.class);
        assertThat(onOffDeviceFeature).isNotNull();

        assertThat(waterHeaterFeature.getOperatingMode().getTargetValue()).isEqualTo(WaterHeaterOperatingMode.ON);
        assertThat(onOffDeviceFeature.getMode().getTargetValue()).isEqualTo(OnOffMode.ON);
    }

    @Test
    public void testNormalCommandOperatingModeOnFromOff() {
        /* Given */
        final WaterHeaterEasyHandlerStrategy strategy = new WaterHeaterEasyHandlerStrategy();

        final WaterHeaterOperatingMode operatingMode = WaterHeaterOperatingMode.ON;
        final OnOffMode onOffMode = OnOffMode.OFF;

        // Create the test node
        final Node node = getGoodNodeForOperatingModeCommandTest(
                operatingMode,
                onOffMode
        );


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.easyOperatingModeChannelUid,
                new StringType(HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_ON),
                node
        );


        /* Then */
        assertThat(updatedNode).isNotNull();

        final @Nullable WaterHeaterFeature waterHeaterFeature = updatedNode.getFeature(WaterHeaterFeature.class);
        assertThat(waterHeaterFeature).isNotNull();

        final @Nullable OnOffDeviceFeature onOffDeviceFeature = updatedNode.getFeature(OnOffDeviceFeature.class);
        assertThat(onOffDeviceFeature).isNotNull();

        assertThat(waterHeaterFeature.getOperatingMode().getTargetValue()).isEqualTo(WaterHeaterOperatingMode.ON);
        assertThat(onOffDeviceFeature.getMode().getTargetValue()).isEqualTo(OnOffMode.ON);
    }

    @Test
    public void testNormalCommandOperatingModeSchedule() {
        /* Given */
        final WaterHeaterEasyHandlerStrategy strategy = new WaterHeaterEasyHandlerStrategy();

        final WaterHeaterOperatingMode operatingMode = WaterHeaterOperatingMode.ON;
        final OnOffMode onOffMode = OnOffMode.OFF;

        // Create the test node
        final Node node = getGoodNodeForOperatingModeCommandTest(
                operatingMode,
                onOffMode
        );


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.easyOperatingModeChannelUid,
                new StringType(HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_SCHEDULE),
                node
        );


        /* Then */
        assertThat(updatedNode).isNotNull();

        final @Nullable WaterHeaterFeature waterHeaterFeature = updatedNode.getFeature(WaterHeaterFeature.class);
        assertThat(waterHeaterFeature).isNotNull();

        final @Nullable OnOffDeviceFeature onOffDeviceFeature = updatedNode.getFeature(OnOffDeviceFeature.class);
        assertThat(onOffDeviceFeature).isNotNull();

        assertThat(waterHeaterFeature.getOperatingMode().getTargetValue()).isEqualTo(WaterHeaterOperatingMode.SCHEDULE);
        assertThat(onOffDeviceFeature.getMode().getTargetValue()).isEqualTo(OnOffMode.ON);
    }

    @Test
    public void testNormalCommandOperatingModeOff() {
        /* Given */
        final WaterHeaterEasyHandlerStrategy strategy = new WaterHeaterEasyHandlerStrategy();

        final WaterHeaterOperatingMode operatingMode = WaterHeaterOperatingMode.ON;
        final OnOffMode onOffMode = OnOffMode.ON;

        // Create the test node
        final Node node = getGoodNodeForOperatingModeCommandTest(
                operatingMode,
                onOffMode
        );


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.easyOperatingModeChannelUid,
                new StringType(HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_OFF),
                node
        );


        /* Then */
        assertThat(updatedNode).isNotNull();

        final @Nullable WaterHeaterFeature waterHeaterFeature = updatedNode.getFeature(WaterHeaterFeature.class);
        assertThat(waterHeaterFeature).isNotNull();

        final @Nullable OnOffDeviceFeature onOffDeviceFeature = updatedNode.getFeature(OnOffDeviceFeature.class);
        assertThat(onOffDeviceFeature).isNotNull();

        assertThat(onOffDeviceFeature.getMode().getTargetValue()).isEqualTo(OnOffMode.OFF);
    }

    @Test
    public void testNormalCommandBoostOn() {
        /* Given */
        final WaterHeaterEasyHandlerStrategy strategy = new WaterHeaterEasyHandlerStrategy();


        // Create the test node
        final Node node = getGoodNodeForBoostCommandTest(OverrideMode.NONE);


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.easyBoostModeChannelUid,
                OnOffType.ON,
                node
        );


        /* Then */
        assertThat(updatedNode).isNotNull();

        final @Nullable WaterHeaterFeature waterHeaterFeature = updatedNode.getFeature(WaterHeaterFeature.class);
        assertThat(waterHeaterFeature).isNotNull();

        final @Nullable TransientModeFeature transientModeFeature = updatedNode.getFeature(TransientModeFeature.class);
        assertThat(transientModeFeature).isNotNull();

        assertThat(waterHeaterFeature.getTemporaryOperatingModeOverride().getTargetValue()).isEqualTo(OverrideMode.TRANSIENT);
        assertThat(transientModeFeature.getIsEnabled().getTargetValue()).isEqualTo(true);
    }

    @Test
    public void testNormalCommandBoostOff() {
        /* Given */
        final WaterHeaterEasyHandlerStrategy strategy = new WaterHeaterEasyHandlerStrategy();


        // Create the test node
        final Node node = getGoodNodeForBoostCommandTest(OverrideMode.TRANSIENT);


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.easyBoostModeChannelUid,
                OnOffType.OFF,
                node
        );


        /* Then */
        assertThat(updatedNode).isNotNull();

        final @Nullable WaterHeaterFeature waterHeaterFeature = updatedNode.getFeature(WaterHeaterFeature.class);
        assertThat(waterHeaterFeature).isNotNull();

        final @Nullable TransientModeFeature transientModeFeature = updatedNode.getFeature(TransientModeFeature.class);
        assertThat(transientModeFeature).isNotNull();

        assertThat(waterHeaterFeature.getTemporaryOperatingModeOverride().getTargetValue()).isEqualTo(OverrideMode.NONE);
        assertThat(transientModeFeature.getIsEnabled().getTargetValue()).isEqualTo(false);
    }

    @Test
    public void testNormalRefreshOperatingMode() {
        /* Given */
        final WaterHeaterEasyHandlerStrategy strategy = new WaterHeaterEasyHandlerStrategy();


        // Create the test node
        final Node node = getGoodNodeForRefreshTest();


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.easyOperatingModeChannelUid,
                RefreshType.REFRESH,
                node
        );


        /* Then */
        assertThat(updatedNode).isNull();
    }

    @Test
    public void testNormalRefreshBoost() {
        /* Given */
        final WaterHeaterEasyHandlerStrategy strategy = new WaterHeaterEasyHandlerStrategy();


        // Create the test node
        final Node node = getGoodNodeForRefreshTest();


        /* When */
        final @Nullable Node updatedNode = strategy.handleCommand(
                this.easyBoostModeChannelUid,
                RefreshType.REFRESH,
                node
        );


        /* Then */
        assertThat(updatedNode).isNull();;
    }

    private static Node getGoodNodeForRefreshTest() {
        return getGoodNode(
                WaterHeaterOperatingMode.SCHEDULE,
                false,
                OverrideMode.NONE,
                OnOffMode.ON
        );
    }

    private static Node getGoodNodeForBoostCommandTest(
            final OverrideMode overrideMode
    ) {
        return getGoodNode(
                WaterHeaterOperatingMode.SCHEDULE,
                false,
                overrideMode,
                OnOffMode.ON
        );
    }

    private static Node getGoodNodeForOperatingModeCommandTest(
            final WaterHeaterOperatingMode operatingMode,
            final OnOffMode onOffMode
    ) {
        return getGoodNode(
                operatingMode,
                false,
                OverrideMode.NONE,
                onOffMode
        );
    }

    private static Node getGoodNode(
            final WaterHeaterOperatingMode operatingMode,
            final boolean isOn,
            final OverrideMode overrideMode,
            final OnOffMode onOffMode
    ) {
        final Map<Class<? extends Feature>, Feature> features = new HashMap<>();

        // Create WaterHeaterFeature
        final WaterHeaterFeature waterHeaterFeature = WaterHeaterFeature.builder()
                .operatingMode(TestUtil.createSimpleFeatureAttribute(operatingMode))
                .isOn(TestUtil.createSimpleFeatureAttribute(isOn))
                .temporaryOperatingModeOverride(TestUtil.createSimpleFeatureAttribute(overrideMode))
                .build();
        features.put(WaterHeaterFeature.class, waterHeaterFeature);

        // Create OnOffDeviceFeature
        final OnOffDeviceFeature onOffDeviceFeature = OnOffDeviceFeature.builder()
                .mode(TestUtil.createSimpleFeatureAttribute(onOffMode))
                .build();
        features.put(OnOffDeviceFeature.class, onOffDeviceFeature);

        // Create TransientModeFeature
        final TransientModeFeature transientModeFeature = TransientModeFeature.builder()
                .duration(TestUtil.createSimpleFeatureAttribute(Duration.ofMinutes(30)))
                .isEnabled(TestUtil.createSimpleFeatureAttribute(overrideMode == OverrideMode.TRANSIENT))
                .startDatetime(TestUtil.createSimpleFeatureAttribute(Instant.now().atZone(ZoneId.systemDefault())))
                .endDatetime(TestUtil.createSimpleFeatureAttribute(Instant.now().plus(Duration.ofMinutes(30)).atZone(ZoneId.systemDefault())))
                .build();
        features.put(TransientModeFeature.class, transientModeFeature);

        return TestUtil.getTestNodeWithFeatures(features);
    }
}
