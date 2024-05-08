/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.iotawatt.internal.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.iotawatt.internal.service.FetchDataService.INPUT_CHANNEL_ID_PREFIX;
import static org.openhab.binding.iotawatt.internal.service.FetchDataService.OUTPUT_CHANNEL_ID_PREFIX;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.iotawatt.internal.IoTaWattBindingConstants;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClient;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClientCommunicationException;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClientConfigurationException;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClientException;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClientInterruptedException;
import org.openhab.binding.iotawatt.internal.model.StatusResponse;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * @author Peter Rosenberg - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class FetchDataServiceTest {
    @Mock
    @NonNullByDefault({})
    private DeviceHandlerCallback deviceHandlerCallback;
    @Mock
    @NonNullByDefault({})
    private IoTaWattClient ioTaWattClient;
    @InjectMocks
    @NonNullByDefault({})
    private FetchDataService service;

    private final ThingUID thingUID = new ThingUID(IoTaWattBindingConstants.BINDING_ID, "d231dea2e4");

    @Test
    void pollDevice_whenAllSupportedInputTypes_updateAllChannels() throws IoTaWattClientInterruptedException,
            IoTaWattClientCommunicationException, IoTaWattClientConfigurationException, IoTaWattClientException {
        // given
        service.setIoTaWattClient(ioTaWattClient);
        final Float voltageRms = 259.1f;
        final Float hertz = 50.1f;
        final Float phase0 = 0.1f;
        final Float wattsValue = 1.1f;
        final Float phase1 = 0.2f;
        final Float powerFactor = 0.3f;
        when(deviceHandlerCallback.getThingUID()).thenReturn(thingUID);
        final List<StatusResponse.Input> inputs = List.of(
                new StatusResponse.Input(0, voltageRms, hertz, phase0, null, null),
                new StatusResponse.Input(1, null, null, phase1, wattsValue, powerFactor));
        final StatusResponse statusResponse = new StatusResponse(inputs, List.of());
        when(ioTaWattClient.fetchStatus()).thenReturn(Optional.of(statusResponse));

        // when
        service.pollDevice();

        // then
        verify(deviceHandlerCallback).updateStatus(ThingStatus.ONLINE);
        verify(deviceHandlerCallback).updateState(createInputChannelUID("00", "voltage"),
                createState(voltageRms, Units.VOLT));
        verify(deviceHandlerCallback).updateState(createInputChannelUID("00", "frequency"),
                createState(hertz, Units.HERTZ));
        verify(deviceHandlerCallback).updateState(createInputChannelUID("00", "phase"), createState(phase0, Units.ONE));
        verify(deviceHandlerCallback).updateState(createInputChannelUID("01", "watts"),
                createState(wattsValue, Units.WATT));
        verify(deviceHandlerCallback).updateState(createInputChannelUID("01", "phase"), createState(phase1, Units.ONE));
        verify(deviceHandlerCallback).updateState(createInputChannelUID("01", "power-factor"),
                createState(powerFactor, Units.ONE));
    }

    @Test
    void pollDevice_whenAllSupportedOutputTypes_updateAllChannels() throws IoTaWattClientInterruptedException,
            IoTaWattClientCommunicationException, IoTaWattClientConfigurationException, IoTaWattClientException {
        // given
        service.setIoTaWattClient(ioTaWattClient);
        when(deviceHandlerCallback.getThingUID()).thenReturn(thingUID);
        final List<StatusResponse.Output> outputs = List.of(new StatusResponse.Output("name_amps", "Amps", 1.01f),
                new StatusResponse.Output("name_hz", "Hz", 1.02f), new StatusResponse.Output("name_pf", "PF", 1.03f),
                new StatusResponse.Output("name_va", "VA", 1.04f), new StatusResponse.Output("name_var", "VAR", 1.05f),
                new StatusResponse.Output("name_varh", "VARh", 1.06f),
                new StatusResponse.Output("name_volts", "Volts", 1.07f),
                new StatusResponse.Output("name_watts", "Watts", 1.08f));
        final StatusResponse statusResponse = new StatusResponse(List.of(), outputs);
        when(ioTaWattClient.fetchStatus()).thenReturn(Optional.of(statusResponse));

        // when
        service.pollDevice();

        // then
        verify(deviceHandlerCallback).updateStatus(ThingStatus.ONLINE);
        verify(deviceHandlerCallback).updateState(createOutputChannelUID("00", "name_amps"),
                createState(1.01f, Units.AMPERE));
        verify(deviceHandlerCallback).updateState(createOutputChannelUID("01", "name_hz"),
                createState(1.02f, Units.HERTZ));
        verify(deviceHandlerCallback).updateState(createOutputChannelUID("02", "name_pf"),
                createState(1.03f, Units.ONE));
        verify(deviceHandlerCallback).updateState(createOutputChannelUID("03", "name_va"),
                createState(1.04f, Units.VOLT_AMPERE));
        verify(deviceHandlerCallback).updateState(createOutputChannelUID("04", "name_var"),
                createState(1.05f, Units.VAR));
        verify(deviceHandlerCallback).updateState(createOutputChannelUID("05", "name_varh"),
                createState(1.06f, Units.VAR_HOUR));
        verify(deviceHandlerCallback).updateState(createOutputChannelUID("06", "name_volts"),
                createState(1.07f, Units.VOLT));
        verify(deviceHandlerCallback).updateState(createOutputChannelUID("07", "name_watts"),
                createState(1.08f, Units.WATT));
    }

    @Test
    void pollDevice_whenResponseWithNoChannels_updateStatusToOnlineAndDoNotUpdateChannels()
            throws IoTaWattClientInterruptedException, IoTaWattClientCommunicationException,
            IoTaWattClientConfigurationException, IoTaWattClientException {
        // given
        service.setIoTaWattClient(ioTaWattClient);
        when(ioTaWattClient.fetchStatus()).thenReturn(Optional.empty());

        // when
        service.pollDevice();

        // then
        verify(deviceHandlerCallback).updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        verify(deviceHandlerCallback, never()).updateState(any(), any());
    }

    @Test
    void pollDevice_whenExceptionWithCase_useCauseMessage() throws IoTaWattClientInterruptedException,
            IoTaWattClientCommunicationException, IoTaWattClientConfigurationException, IoTaWattClientException {
        // given
        final String exceptionMessage = "test message";
        service.setIoTaWattClient(ioTaWattClient);
        final Throwable exception = new IoTaWattClientConfigurationException(new Throwable(exceptionMessage));
        when(ioTaWattClient.fetchStatus()).thenThrow(exception);

        // when
        service.pollDevice();

        // then
        verify(deviceHandlerCallback).updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                exceptionMessage);
        verify(deviceHandlerCallback, never()).updateState(any(), any());
    }

    @Test
    void pollDevice_whenEmptyResponse_updateStatusToOffline() {
        // given
        // do not set service.setIoTaWattClient(ioTaWattClient);

        // when
        service.pollDevice();

        // then
        verify(deviceHandlerCallback).updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
        verify(deviceHandlerCallback, never()).updateState(any(), any());
    }

    @Test
    void pollDevice_whenNotInitialised_fail() throws IoTaWattClientInterruptedException,
            IoTaWattClientCommunicationException, IoTaWattClientConfigurationException, IoTaWattClientException {
        // given
        service.setIoTaWattClient(ioTaWattClient);
        final StatusResponse statusResponse = new StatusResponse(List.of(), List.of());
        when(ioTaWattClient.fetchStatus()).thenReturn(Optional.of(statusResponse));

        // when
        service.pollDevice();

        // then
        verify(deviceHandlerCallback).updateStatus(ThingStatus.ONLINE);
        verify(deviceHandlerCallback, never()).updateState(any(), any());
    }

    @ParameterizedTest
    @MethodSource("provideParamsForThrowCases")
    void pollDevice_whenApiRequestThrowsInterruptedException_updateStatusAccordingly(Class<Throwable> throwableClass,
            ThingStatusDetail thingStatusDetail, boolean withErrorMessage) throws IoTaWattClientInterruptedException,
            IoTaWattClientCommunicationException, IoTaWattClientConfigurationException, IoTaWattClientException {
        // given
        final String errorMessage = "Error message";
        service.setIoTaWattClient(ioTaWattClient);
        final Throwable thrownThrowable = mock(throwableClass);
        if (withErrorMessage) {
            when(thrownThrowable.getMessage()).thenReturn(errorMessage);
        }
        when(ioTaWattClient.fetchStatus()).thenThrow(thrownThrowable);

        // when
        service.pollDevice();

        // then
        if (withErrorMessage) {
            verify(deviceHandlerCallback).updateStatus(ThingStatus.OFFLINE, thingStatusDetail, errorMessage);
        } else {
            verify(deviceHandlerCallback).updateStatus(ThingStatus.OFFLINE, thingStatusDetail);
        }
        verify(deviceHandlerCallback, never()).updateState(any(), any());
    }

    private static Stream<Arguments> provideParamsForThrowCases() {
        return Stream.of(Arguments.of(IoTaWattClientInterruptedException.class, ThingStatusDetail.NOT_YET_READY, false),
                Arguments.of(IoTaWattClientCommunicationException.class, ThingStatusDetail.COMMUNICATION_ERROR, false),
                Arguments.of(IoTaWattClientConfigurationException.class, ThingStatusDetail.CONFIGURATION_ERROR, true),
                Arguments.of(IoTaWattClientException.class, ThingStatusDetail.NONE, true));
    }

    private ChannelUID createInputChannelUID(String channelNumberStr, String channelName) {
        return new ChannelUID(thingUID, INPUT_CHANNEL_ID_PREFIX + channelNumberStr + "#" + channelName);
    }

    private ChannelUID createOutputChannelUID(String channelNumber, String channelName) {
        return new ChannelUID(thingUID, OUTPUT_CHANNEL_ID_PREFIX + channelNumber + "#" + channelName);
    }

    private State createState(Float value, Unit<?> unit) {
        return new QuantityType<>(value, unit);
    }
}
