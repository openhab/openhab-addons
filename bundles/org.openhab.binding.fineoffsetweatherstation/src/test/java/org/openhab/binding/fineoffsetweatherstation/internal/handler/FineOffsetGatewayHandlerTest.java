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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetSensorConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants;
import org.openhab.binding.fineoffsetweatherstation.internal.discovery.FineOffsetGatewayDiscoveryService;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.MeasureType;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Sensor;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.service.GatewayQueryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.UnDefType;

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

    private static final int REMOVAL_THRESHOLD = FineOffsetGatewayHandler.GATEWAY_MISSING_VALUE_REMOVAL_THRESHOLD;
    private static final int FAILED_POLLS_UNTIL_OFFLINE = new FineOffsetGatewayConfiguration().failedPollsUntilOffline;

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

    /**
     * Emulates the real TranslationProvider: formats defaultText with the varargs via MessageFormat, and returns null
     * when defaultText is null (as the real provider does for missing descriptions).
     */
    private void stubTranslationProvider() {
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
    }

    private Thing wh51Ch1SensorThing() {
        Thing sensorThing = org.mockito.Mockito.mock(Thing.class);
        FineOffsetSensorHandler sensorHandler = org.mockito.Mockito.mock(FineOffsetSensorHandler.class);
        Configuration sensorConfig = new Configuration(
                Map.of(FineOffsetSensorConfiguration.SENSOR, SensorGatewayBinding.WH51_CH1.name()));
        when(sensorThing.getThingTypeUID()).thenReturn(THING_TYPE_SENSOR);
        when(sensorThing.getConfiguration()).thenReturn(sensorConfig);
        when(sensorThing.getHandler()).thenReturn(sensorHandler);
        when(sensorThing.getUID()).thenReturn(new ThingUID(THING_TYPE_SENSOR, BRIDGE_UID.getId(), "WH51_CH1"));
        return sensorThing;
    }

    private @Nullable String soilChannelDescription() {
        return ((Bridge) handler.getThing()).getChannels().stream()
                .filter(c -> "moisture-soil-channel-1".equals(c.getUID().getId())).findFirst().orElseThrow()
                .getDescription();
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
        stubTranslationProvider();
        Thing sensorThing = wh51Ch1SensorThing();
        when(bridgeMock.getThings()).thenReturn(List.of(sensorThing));

        MeasuredValue soilMoisture = taggedValue("moisture-soil-channel", CHANNEL_TYPE_HUMIDITY, Sensor.WH51, 1);
        liveDataReturns(soilMoisture);
        updateLiveData();

        // The description of the created gateway channel must reference the full ChannelUID
        String expectedUid = new ChannelUID(sensorThing.getUID(), "moisture-soil-channel").getAsString();
        assertThat(soilChannelDescription(), containsString(expectedUid));

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

    @Test
    void failedPollMarksSensorsUndefinedWithoutAdvancingRemovalDebounce() throws Exception {
        Thing sensorThing = wh51Ch1SensorThing();
        FineOffsetSensorHandler sensorHandler = (FineOffsetSensorHandler) sensorThing.getHandler();
        when(bridgeMock.getThings()).thenReturn(List.of(sensorThing));
        Channel humidityChannel = ChannelBuilder.create(new ChannelUID(BRIDGE_UID, "humidity"))
                .withType(CHANNEL_TYPE_HUMIDITY).build();
        when(bridgeMock.getChannels()).thenReturn(List.of(humidityChannel));

        when(gatewayQueryServiceMock.getMeasuredValues()).thenReturn(null);
        pollTimes(REMOVAL_THRESHOLD);

        // the first failures are tolerated, all later ones mark the values undefined
        int undefinedPolls = REMOVAL_THRESHOLD - (FAILED_POLLS_UNTIL_OFFLINE - 1);
        verify(sensorHandler, times(undefinedPolls)).markMeasuredValuesUndefined();
        verify(sensorHandler, never()).updateMeasuredValues(any());
        verify(callbackMock, times(undefinedPolls)).stateUpdated(humidityChannel.getUID(), UnDefType.UNDEF);

        // the first successful poll without humidity must count as its first miss, not its eleventh
        liveDataReturns(temperature);
        updateLiveData();
        assertThat(currentChannelIds(), containsInAnyOrder("temperature", "humidity"));
    }

    @Test
    void persistedChannelWithoutDeprecationNoteIsRefreshed() throws Exception {
        stubTranslationProvider();
        Thing sensorThing = wh51Ch1SensorThing();
        when(bridgeMock.getThings()).thenReturn(List.of(sensorThing));
        // a gateway channel persisted before the deprecation note existed
        when(bridgeMock.getChannels())
                .thenReturn(List.of(ChannelBuilder.create(new ChannelUID(BRIDGE_UID, "moisture-soil-channel-1"))
                        .withType(CHANNEL_TYPE_HUMIDITY).build()));

        liveDataReturns(taggedValue("moisture-soil-channel", CHANNEL_TYPE_HUMIDITY, Sensor.WH51, 1));
        updateLiveData();

        String expectedUid = new ChannelUID(sensorThing.getUID(), "moisture-soil-channel").getAsString();
        assertThat(soilChannelDescription(), containsString(expectedUid));

        // an up-to-date description must not trigger further Thing updates
        ((BridgeImpl) handler.getThing()).addThing(sensorThing);
        updateLiveData();
        verify(callbackMock, times(1)).thingUpdated(any());
    }

    @Test
    void noTargetNoteIsReplacedOnceSensorThingIsAdded() throws Exception {
        stubTranslationProvider();
        liveDataReturns(taggedValue("moisture-soil-channel", CHANNEL_TYPE_HUMIDITY, Sensor.WH51, 1));
        updateLiveData();
        assertThat(soilChannelDescription(), not(containsString("channel:")));

        Thing sensorThing = wh51Ch1SensorThing();
        ((BridgeImpl) handler.getThing()).addThing(sensorThing);
        updateLiveData();

        String expectedUid = new ChannelUID(sensorThing.getUID(), "moisture-soil-channel").getAsString();
        assertThat(soilChannelDescription(), containsString(expectedUid));
    }

    /** Runs the refresh job's task as the current session would. */
    private void refresh() throws Exception {
        refresh(session().get());
    }

    private void refresh(long session) throws Exception {
        Method method = FineOffsetGatewayHandler.class.getDeclaredMethod("refresh", long.class);
        method.setAccessible(true);
        method.invoke(handler, session);
    }

    /** The session counter, which initialize() and dispose() advance. */
    private AtomicLong session() throws Exception {
        Field field = FineOffsetGatewayHandler.class.getDeclaredField("session");
        field.setAccessible(true);
        return (AtomicLong) Objects.requireNonNull(field.get(handler));
    }

    private void invokePrivate(String name) throws Exception {
        Method method = FineOffsetGatewayHandler.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(handler);
    }

    @Test
    void failedSensorReadKeepsTheLastKnownSensorState() throws Exception {
        Thing sensorThing = wh51Ch1SensorThing();
        FineOffsetSensorHandler sensorHandler = (FineOffsetSensorHandler) sensorThing.getHandler();
        when(bridgeMock.getThings()).thenReturn(List.of(sensorThing));
        SensorDevice device = new SensorDevice(1, SensorGatewayBinding.WH51_CH1,
                SensorGatewayBinding.WH51_CH1.getBatteryStatus((byte) 0), 4);

        when(gatewayQueryServiceMock.getRegisteredSensors()).thenReturn(Map.of(SensorGatewayBinding.WH51_CH1, device));
        invokePrivate("fetchAndUpdateSensors");
        verify(sensorHandler, times(1)).updateSensorState(device);

        // a failed read is no evidence that the sensor was unpaired: it must not be marked as gone
        when(gatewayQueryServiceMock.getRegisteredSensors()).thenReturn(null);
        invokePrivate("fetchAndUpdateSensors");
        verify(sensorHandler, never()).updateSensorState(null);
        verify(sensorHandler, times(1)).updateSensorState(any());
    }

    @Test
    void unexpectedExceptionDoesNotEscapeTheRefreshJob() throws Exception {
        // an exception escaping a fixed-delay task would cancel all further polls
        when(gatewayQueryServiceMock.getMeasuredValues()).thenThrow(new IllegalStateException("malformed"));
        refresh();
        refresh();
        verify(gatewayQueryServiceMock, times(2)).getMeasuredValues();
    }

    @Test
    void pollAfterDisposeDoesNotUpdateStates() throws Exception {
        Channel humidityChannel = ChannelBuilder.create(new ChannelUID(BRIDGE_UID, "humidity"))
                .withType(CHANNEL_TYPE_HUMIDITY).build();
        when(bridgeMock.getChannels()).thenReturn(List.of(humidityChannel));
        handler.dispose();

        updateLiveData();

        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    @Test
    void singleFailedPollsAreTolerated() throws Exception {
        liveDataReturns(temperature);
        updateLiveData();
        verify(callbackMock).statusUpdated(any(), argThat(status -> status.getStatus() == ThingStatus.ONLINE));

        // a slow link loses single responses: keep the status and the last values
        when(gatewayQueryServiceMock.getMeasuredValues()).thenReturn(null);
        pollTimes(FAILED_POLLS_UNTIL_OFFLINE - 1);
        verify(callbackMock, never()).statusUpdated(any(),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        verify(callbackMock, never()).stateUpdated(any(), eq(UnDefType.UNDEF));

        // a successful poll starts counting anew
        liveDataReturns(temperature);
        updateLiveData();
        when(gatewayQueryServiceMock.getMeasuredValues()).thenReturn(null);
        pollTimes(FAILED_POLLS_UNTIL_OFFLINE - 1);
        verify(callbackMock, never()).statusUpdated(any(),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    void consecutiveFailedPollsReportTheLastError() throws Exception {
        Method onQueryStatus = FineOffsetGatewayHandler.class.getDeclaredMethod("onQueryStatus", ThingStatus.class,
                ThingStatusDetail.class, String.class);
        onQueryStatus.setAccessible(true);
        // the query service reports the error of the request it fails
        when(gatewayQueryServiceMock.getMeasuredValues()).thenAnswer(invocation -> {
            onQueryStatus.invoke(handler, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Request timed out");
            return null;
        });
        pollTimes(FAILED_POLLS_UNTIL_OFFLINE);

        verify(callbackMock).statusUpdated(any(),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR
                        && "Request timed out".equals(status.getDescription())));
    }

    @Test
    void failureWithoutReportedErrorIsNotDescribedByAnOldOne() throws Exception {
        Method onQueryStatus = FineOffsetGatewayHandler.class.getDeclaredMethod("onQueryStatus", ThingStatus.class,
                ThingStatusDetail.class, String.class);
        onQueryStatus.setAccessible(true);
        when(gatewayQueryServiceMock.getMeasuredValues()).thenAnswer(invocation -> {
            onQueryStatus.invoke(handler, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Request timed out");
            return null;
        });
        updateLiveData();
        // later polls fail without a reported error (e.g. a response that is no valid JSON)
        when(gatewayQueryServiceMock.getMeasuredValues()).thenReturn(null);
        pollTimes(FAILED_POLLS_UNTIL_OFFLINE);

        verify(callbackMock, never()).statusUpdated(any(),
                argThat(status -> "Request timed out".equals(status.getDescription())));
        verify(callbackMock, atLeastOnce()).statusUpdated(any(),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && "@text/offline.comm-error.no-valid-response".equals(status.getDescription())));
    }

    @Test
    void firmwareUpdateIsPickedUpWithTheNextSensorRead() throws Exception {
        // the gateway restarts after a firmware update, usually without a poll failing
        liveDataReturns(temperature);
        when(gatewayQueryServiceMock.getRegisteredSensors()).thenReturn(Map.of());
        when(gatewayQueryServiceMock.getFirmwareVersion()).thenReturn("GW1100B_V2.3.4");
        refresh();
        assertThat(handler.getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("2.3.4"));

        when(gatewayQueryServiceMock.getFirmwareVersion()).thenReturn("GW1100B_V2.4.6");
        refresh();

        assertThat(handler.getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("2.4.6"));
        verify(gatewayQueryServiceMock, times(2)).getFirmwareVersion();
    }

    @Test
    void unreachableGatewayIsOnlyAskedForLiveData() throws Exception {
        // one request per poll: the gateway information and the sensor list wait until the gateway answers
        when(gatewayQueryServiceMock.getMeasuredValues()).thenReturn(null);
        refresh();
        refresh();

        verify(gatewayQueryServiceMock, times(2)).getMeasuredValues();
        verify(gatewayQueryServiceMock, never()).getFirmwareVersion();
        verify(gatewayQueryServiceMock, never()).getRegisteredSensors();
    }

    @Test
    void failedGatewayInformationIsRetriedPerProperty() throws Exception {
        liveDataReturns(temperature);
        when(gatewayQueryServiceMock.getFirmwareVersion()).thenReturn("GW1100B_V2.3.4");
        when(gatewayQueryServiceMock.fetchSystemInfo()).thenReturn(null);
        invokePrivate("updateBridgeInfo");
        invokePrivate("updateBridgeInfo");

        // the system information is read again, the firmware version is not
        verify(gatewayQueryServiceMock, times(1)).getFirmwareVersion();
        verify(gatewayQueryServiceMock, times(2)).fetchSystemInfo();
        assertThat(handler.getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("2.3.4"));
    }

    @Test
    void unsupportedGatewayInformationIsNotRequestedForever() throws Exception {
        // e.g. ELV gateways do not answer every request
        when(gatewayQueryServiceMock.getFirmwareVersion()).thenReturn("GW1100B_V2.3.4");
        when(gatewayQueryServiceMock.fetchSystemInfo()).thenReturn(null);
        for (int i = 0; i < 20; i++) {
            invokePrivate("updateBridgeInfo");
        }
        // a few quick attempts, then slow ones (a lost request looks the same as an unsupported one), then none
        verify(gatewayQueryServiceMock, times(12)).fetchSystemInfo();
    }

    @Test
    void runOfAnEarlierSessionDoesNothing() throws Exception {
        // a run scheduled before dispose() and initialize() that only got the lock afterwards
        long earlierSession = session().get();
        session().incrementAndGet();
        refresh(earlierSession);
        verify(gatewayQueryServiceMock, never()).getMeasuredValues();
    }

    @Test
    void runOutdatedDuringItsRequestStops() throws Exception {
        // a configuration change re-initializes the handler while a slow poll is in progress
        long oldSession = session().get();
        when(gatewayQueryServiceMock.getMeasuredValues()).thenAnswer(invocation -> {
            session().incrementAndGet();
            return List.of(temperature);
        });
        refresh(oldSession);

        verify(callbackMock, never()).statusUpdated(any(), any());
        verify(callbackMock, never()).thingUpdated(any());
        verify(gatewayQueryServiceMock, never()).getFirmwareVersion();
        verify(gatewayQueryServiceMock, never()).getRegisteredSensors();
    }

    @Test
    void firmwareIsNotReadAgainIfTheGatewayNeverReportedIt() throws Exception {
        liveDataReturns(temperature);
        when(gatewayQueryServiceMock.getRegisteredSensors()).thenReturn(Map.of());
        when(gatewayQueryServiceMock.getFirmwareVersion()).thenReturn(null);
        refresh();
        refresh();
        // read once for the gateway information; the sensor read does not ask again
        verify(gatewayQueryServiceMock, times(1)).getFirmwareVersion();
    }
}
