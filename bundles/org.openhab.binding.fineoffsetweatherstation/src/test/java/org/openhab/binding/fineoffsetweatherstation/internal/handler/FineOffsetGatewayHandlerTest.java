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
package org.openhab.binding.fineoffsetweatherstation.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_HUMIDITY;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_MAX_WIND_SPEED;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_TEMPERATURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_GATEWAY;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_SENSOR;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetSensorConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants;
import org.openhab.binding.fineoffsetweatherstation.internal.discovery.FineOffsetGatewayDiscoveryService;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.MeasureType;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Sensor;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.service.GatewayQueryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Tests for the dynamic channel handling of the {@link FineOffsetGatewayHandler}.
 *
 * @author Andreas Berger - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class FineOffsetGatewayHandlerTest {

    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_GATEWAY, "testGateway");

    // must match FineOffsetGatewayHandler.MISSING_MEASURAND_REMOVAL_THRESHOLD
    private static final int REMOVAL_THRESHOLD = 10;

    private @Mock @NonNullByDefault({}) Bridge bridgeMock;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callbackMock;
    private @Mock @NonNullByDefault({}) FineOffsetGatewayDiscoveryService discoveryServiceMock;
    private @Mock @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistryMock;
    private @Mock @NonNullByDefault({}) TranslationProvider translationProviderMock;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProviderMock;
    private @Mock @NonNullByDefault({}) GatewayQueryService gatewayQueryServiceMock;

    private @NonNullByDefault({}) FineOffsetGatewayHandler handler;

    private final MeasuredValue temperature = measuredValue("temperature", CHANNEL_TYPE_TEMPERATURE);
    private final MeasuredValue humidity = measuredValue("humidity", CHANNEL_TYPE_HUMIDITY);
    private final MeasuredValue wind = measuredValue("wind-speed", CHANNEL_TYPE_MAX_WIND_SPEED);

    @BeforeEach
    public void setUp() throws Exception {
        when(bridgeMock.getUID()).thenReturn(BRIDGE_UID);
        when(bridgeMock.getThingTypeUID()).thenReturn(THING_TYPE_GATEWAY);
        when(bridgeMock.getConfiguration()).thenReturn(new Configuration());
        when(bridgeMock.getProperties()).thenReturn(Map.of());
        when(bridgeMock.getChannels()).thenReturn(List.of());
        when(bridgeMock.getThings()).thenReturn(List.of());

        handler = new FineOffsetGatewayHandler(bridgeMock, discoveryServiceMock, channelTypeRegistryMock,
                translationProviderMock, localeProviderMock);
        handler.setCallback(callbackMock);

        // inject the mocked query service, bypassing initialize()
        Field queryServiceField = FineOffsetGatewayHandler.class.getDeclaredField("gatewayQueryService");
        queryServiceField.setAccessible(true);
        queryServiceField.set(handler, gatewayQueryServiceMock);
    }

    private void updateLiveData() throws Exception {
        Method method = FineOffsetGatewayHandler.class.getDeclaredMethod("updateLiveData");
        method.setAccessible(true);
        method.invoke(handler);
    }

    private void liveDataReturns(MeasuredValue... values) {
        when(gatewayQueryServiceMock.getMeasuredValues()).thenReturn(List.of(values));
    }

    private void pollTimes(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            updateLiveData();
        }
    }

    private List<String> currentChannelIds() {
        return ((Bridge) handler.getThing()).getChannels().stream().map(Channel::getUID).map(uid -> uid.getId())
                .collect(Collectors.toList());
    }

    private MeasuredValue measuredValue(String channelPrefix, ChannelTypeUID channelTypeUID) {
        return new MeasuredValue(MeasureType.TEMPERATURE, channelPrefix, null, channelTypeUID, new DecimalType(1),
                channelPrefix, null);
    }

    private MeasuredValue taggedValue(String channelPrefix, ChannelTypeUID channelTypeUID, Sensor sensor,
            Integer channelNumber) {
        return new MeasuredValue(MeasureType.TEMPERATURE, channelPrefix, channelNumber, channelTypeUID,
                new DecimalType(1), channelPrefix, sensor);
    }

    @Test
    void newChannelInLaterPollDoesNotWipeExistingChannels() throws Exception {
        // Regression test for the original bug: a measurand appearing in a later poll than the first must not
        // wipe the previously created channels.
        liveDataReturns(temperature, humidity);
        updateLiveData();
        assertThat(currentChannelIds(), containsInAnyOrder("temperature", "humidity"));

        liveDataReturns(temperature, humidity, wind);
        updateLiveData();
        assertThat(currentChannelIds(), containsInAnyOrder("temperature", "humidity", "wind-speed"));
    }

    @Test
    void firmwareUpdateAddingMeasurandIsReflectedImmediately() throws Exception {
        // A gateway firmware update may start reporting an additional measurand for an already known sensor.
        // The new channel is added right away, without removing anything.
        liveDataReturns(temperature);
        updateLiveData();
        assertThat(currentChannelIds(), containsInAnyOrder("temperature"));

        liveDataReturns(temperature, wind);
        updateLiveData();
        assertThat(currentChannelIds(), containsInAnyOrder("temperature", "wind-speed"));
    }

    @Test
    void intermittentlyMissingMeasurandIsKeptBelowThreshold() throws Exception {
        liveDataReturns(temperature, humidity);
        updateLiveData();

        // humidity missing for one less than the threshold -> channel must be kept
        liveDataReturns(temperature);
        pollTimes(REMOVAL_THRESHOLD - 1);

        assertThat(currentChannelIds(), containsInAnyOrder("temperature", "humidity"));
    }

    @Test
    void permanentlyMissingMeasurandIsRemovedAfterThreshold() throws Exception {
        liveDataReturns(temperature, humidity);
        updateLiveData();

        // humidity missing for the full threshold of consecutive polls -> channel is removed
        liveDataReturns(temperature);
        pollTimes(REMOVAL_THRESHOLD);

        assertThat(currentChannelIds(), containsInAnyOrder("temperature"));
    }

    @Test
    void reappearingMeasurandResetsTheMissCounter() throws Exception {
        liveDataReturns(temperature, humidity);
        updateLiveData();

        // almost reach the threshold...
        liveDataReturns(temperature);
        pollTimes(REMOVAL_THRESHOLD - 1);

        // ...then the measurand reappears, which must reset the counter
        liveDataReturns(temperature, humidity);
        updateLiveData();

        // almost reach the threshold again - still kept because the counter was reset
        liveDataReturns(temperature);
        pollTimes(REMOVAL_THRESHOLD - 1);

        assertThat(currentChannelIds(), containsInAnyOrder("temperature", "humidity"));
    }

    @Test
    void firstPollStateIsPostedImmediately() throws Exception {
        // When a channel is created for the first time, its initial state must be posted right away —
        // not deferred until the next poll.
        liveDataReturns(temperature);
        updateLiveData();

        ChannelUID expectedUID = new ChannelUID(BRIDGE_UID, "temperature");
        verify(callbackMock).stateUpdated(expectedUID, new DecimalType(1));
    }

    @Test
    void taggedValuesAreDispatchedToTheMatchingSensorHandler() throws Exception {
        // a child WH51 channel-1 sensor Thing with a mocked handler
        Thing sensorThing = org.mockito.Mockito.mock(Thing.class);
        FineOffsetSensorHandler sensorHandler = org.mockito.Mockito.mock(FineOffsetSensorHandler.class);
        Configuration sensorConfig = new Configuration(
                Map.of(FineOffsetSensorConfiguration.SENSOR, SensorGatewayBinding.WH51_CH1.name()));
        when(sensorThing.getThingTypeUID()).thenReturn(FineOffsetWeatherStationBindingConstants.THING_TYPE_SENSOR);
        when(sensorThing.getConfiguration()).thenReturn(sensorConfig);
        when(sensorThing.getHandler()).thenReturn(sensorHandler);
        when(bridgeMock.getThings()).thenReturn(List.of(sensorThing));

        MeasuredValue soilMoisture = taggedValue("moisture-soil-channel", CHANNEL_TYPE_HUMIDITY, Sensor.WH51, 1);
        liveDataReturns(temperature, soilMoisture);
        updateLiveData();

        // the gateway still creates a channel for EVERY value (incl. the tagged one)
        assertThat(currentChannelIds(), containsInAnyOrder("temperature", "moisture-soil-channel-1"));

        // and the WH51-CH1 sensor handler received exactly its one tagged value
        verify(sensorHandler).updateMeasuredValues(argThat(values -> values.size() == 1
                && "moisture-soil-channel-1".equals(values.iterator().next().getChannelId())));
    }

    @Test
    void deprecationNoteContainsFullSensorChannelUid() throws Exception {
        // Stub getText to emulate the real TranslationProvider: format defaultText with varargs via MessageFormat.
        // Return null when defaultText is null (mirrors real provider behaviour for missing descriptions).
        when(translationProviderMock.getText(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            String defaultText = invocation.getArgument(2);
            if (defaultText == null) {
                return null;
            }
            Object[] args = invocation.getArguments();
            // args[4..] are the varargs passed after locale
            Object[] msgArgs = new Object[args.length - 4];
            for (int i = 0; i < msgArgs.length; i++) {
                msgArgs[i] = args[i + 4];
            }
            return msgArgs.length > 0 ? MessageFormat.format(defaultText, msgArgs) : defaultText;
        });

        ThingUID sensorThingUID = new ThingUID(THING_TYPE_SENSOR, BRIDGE_UID.getId(), "WH51_CH1");
        Thing sensorThing = org.mockito.Mockito.mock(Thing.class);
        FineOffsetSensorHandler sensorHandler = org.mockito.Mockito.mock(FineOffsetSensorHandler.class);
        Configuration sensorConfig = new Configuration(
                Map.of(FineOffsetSensorConfiguration.SENSOR, SensorGatewayBinding.WH51_CH1.name()));
        when(sensorThing.getThingTypeUID()).thenReturn(THING_TYPE_SENSOR);
        when(sensorThing.getConfiguration()).thenReturn(sensorConfig);
        when(sensorThing.getHandler()).thenReturn(sensorHandler);
        when(sensorThing.getUID()).thenReturn(sensorThingUID);
        when(bridgeMock.getThings()).thenReturn(List.of(sensorThing));

        MeasuredValue soilMoisture = taggedValue("moisture-soil-channel", CHANNEL_TYPE_HUMIDITY, Sensor.WH51, 1);
        liveDataReturns(soilMoisture);
        updateLiveData();

        // The description of the created gateway channel must reference the full ChannelUID
        String expectedUid = new ChannelUID(sensorThingUID, "moisture-soil-channel").getAsString();
        List<Channel> channels = ((Bridge) handler.getThing()).getChannels();
        Channel soilChannel = channels.stream().filter(c -> "moisture-soil-channel-1".equals(c.getUID().getId()))
                .findFirst().orElseThrow();
        assertThat(soilChannel.getDescription(), containsString(expectedUid));

        // Verify the correct i18n key was used
        verify(translationProviderMock).getText(any(), eq("gateway.dynamic-channel.deprecation-note"), any(), any(),
                eq(expectedUid));
    }

    @Test
    void deprecationNoteNoTargetWhenNoMatchingSensorThingPresent() throws Exception {
        // No child sensor Things at all -> sensorChannelUid() returns null
        when(bridgeMock.getThings()).thenReturn(List.of());

        MeasuredValue soilMoisture = taggedValue("moisture-soil-channel", CHANNEL_TYPE_HUMIDITY, Sensor.WH51, 1);
        liveDataReturns(soilMoisture);
        updateLiveData();

        // Verify the no-target i18n key was used (no UID argument)
        verify(translationProviderMock).getText(any(), eq("gateway.dynamic-channel.deprecation-note-no-target"), any(),
                any());
        // Verify the with-target key was NOT used (double-call regression guard)
        verify(translationProviderMock, never()).getText(any(), eq("gateway.dynamic-channel.deprecation-note"), any(),
                any(), any());
    }
}
