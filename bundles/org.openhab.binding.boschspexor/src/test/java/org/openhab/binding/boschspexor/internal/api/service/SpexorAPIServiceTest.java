/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.boschspexor.internal.api.model.SensorValue.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.boschspexor.internal.api.model.Connection;
import org.openhab.binding.boschspexor.internal.api.model.Connection.ConnectionType;
import org.openhab.binding.boschspexor.internal.api.model.Energy;
import org.openhab.binding.boschspexor.internal.api.model.Energy.EnergyMode;
import org.openhab.binding.boschspexor.internal.api.model.Firmware;
import org.openhab.binding.boschspexor.internal.api.model.Firmware.FirmwareState;
import org.openhab.binding.boschspexor.internal.api.model.ObservationChangeStatus;
import org.openhab.binding.boschspexor.internal.api.model.ObservationChangeStatus.StatusCode;
import org.openhab.binding.boschspexor.internal.api.model.ObservationStatus;
import org.openhab.binding.boschspexor.internal.api.model.ObservationStatus.SensorMode;
import org.openhab.binding.boschspexor.internal.api.model.Profile.ProfileType;
import org.openhab.binding.boschspexor.internal.api.model.SensorValue;
import org.openhab.binding.boschspexor.internal.api.model.Spexor;
import org.openhab.binding.boschspexor.internal.api.model.SpexorInfo;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService;

import com.nimbusds.common.contenttype.ContentType;

/**
 * SpexorAPI Tests for the Bosch spexor backend
 *
 * @author Marc Fischer - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class SpexorAPIServiceTest {

    private @Mock SpexorAuthorizationService authService;
    private @Mock Request request;
    private @Mock ContentResponse response;

    @Test
    void testSpexors() throws InterruptedException, TimeoutException, ExecutionException {
        String testResponse = "[{\"id\":\"860906043381800\",\"name\":\"dev\",\"profile\":{\"name\":\"Vehicle\",\"profileType\":\"Car\"},\"status\":{\"lastConnected\":\"2021-12-15T09:38:58.750Z\",\"online\":true,\"version\":\"10.6.3\",\"stateOfCharge\":94,\"updateAvailable\":false,\"observationStatus\":[{\"observationType\":\"Burglary\",\"sensorMode\":\"Deactivated\"},{\"observationType\":\"CO\",\"sensorMode\":\"Deactivated\"},{\"observationType\":\"Fire\",\"sensorMode\":\"Activated\"}]}}]";
        when(authService.newRequest(any())).thenReturn(Optional.of(request));
        when(request.send()).thenReturn(response);
        when(response.getContentAsString()).thenReturn(testResponse);
        when(response.getContent()).thenReturn(testResponse.getBytes(StandardCharsets.UTF_8));
        SpexorAPIService apiService = new SpexorAPIService(authService);

        List<Spexor> spexors = apiService.getSpexors();
        assertNotNull(spexors);
        assertEquals(1, spexors.size());
        Spexor actual = spexors.get(0);
        assertEquals("860906043381800", actual.getId());
        assertEquals("dev", actual.getName());
        assertEquals("Vehicle", actual.getProfile().getName());
        assertEquals(ProfileType.Car, actual.getProfile().getProfileType());
        assertEquals("2021-12-15T09:38:58.750Z", actual.getStatus().getLastConnected());
        assertTrue(actual.getStatus().isOnline());
        assertEquals("10.6.3", actual.getStatus().getVersion());
        assertEquals(94, actual.getStatus().getStateOfCharge());
        assertFalse(actual.getStatus().isUpdateAvailable());
        assertEquals(3, actual.getStatus().getObservationStatus().size());
        ObservationStatus burglaryStatus = actual.getStatus().getObservationStatus().get(0);
        assertEquals("Burglary", burglaryStatus.getObservationType());
        assertEquals(SensorMode.Deactivated, burglaryStatus.getSensorMode());
        ObservationStatus coStatus = actual.getStatus().getObservationStatus().get(1);
        assertEquals(SensorMode.Deactivated, coStatus.getSensorMode());
        ObservationStatus fireStatus = actual.getStatus().getObservationStatus().get(2);
        assertEquals("Fire", fireStatus.getObservationType());
        assertEquals(SensorMode.Activated, fireStatus.getSensorMode());
    }

    @Test
    void testSpexorByID() throws InterruptedException, TimeoutException, ExecutionException {
        String testResponse = "{\"id\":\"860906043381800\",\"name\":\"dev\",\"profile\":{\"name\":\"Vehicle\",\"profileType\":\"Car\"},\"status\":{\"energy\":{\"stateOfCharge\":{\"value\":94,\"unit\":\"%\"},\"energyMode\":\"EnergySavingOff\",\"isPowered\":true},\"connection\":{\"lastConnected\":\"2021-12-15T11:12:41.738Z\",\"online\":false,\"connectionType\":\"Wifi\"},\"firmware\":{\"currentVersion\":\"10.6.3\",\"state\":\"UpToDate\",\"availableVersion\":\"10.6.3\"},\"observation\":[{\"observationType\":\"Burglary\",\"sensorMode\":\"Deactivated\"},{\"observationType\":\"Fire\",\"sensorMode\":\"Activated\"}]},\"sensors\":[\"AirQuality\",\"AirQualityLevel\",\"Temperature\",\"Pressure\",\"Acceleration\",\"Light\",\"Gas\",\"Humidity\",\"Microphone\",\"PassiveInfrared\",\"CO\"]}";
        when(authService.newRequest(any())).thenReturn(Optional.of(request));
        when(request.send()).thenReturn(response);
        when(response.getContentAsString()).thenReturn(testResponse);
        when(response.getContent()).thenReturn(testResponse.getBytes(StandardCharsets.UTF_8));
        SpexorAPIService apiService = new SpexorAPIService(authService);

        SpexorInfo spexor = apiService.getSpexor("860906043381800");
        assertNotNull(spexor);

        assertEquals("860906043381800", spexor.getId());
        assertEquals("dev", spexor.getName());
        assertEquals("Vehicle", spexor.getProfile().getName());
        assertEquals(ProfileType.Car, spexor.getProfile().getProfileType());

        Energy energy = spexor.getStatus().getEnergy();
        assertEquals(EnergyMode.EnergySavingOff, energy.getEnergyMode());
        assertTrue(energy.isPowered());
        assertEquals(94, energy.getStateOfCharge().getValue());

        Firmware firmware = spexor.getStatus().getFirmware();
        assertEquals("10.6.3", firmware.getCurrentVersion());
        assertEquals("10.6.3", firmware.getAvailableVersion());
        assertEquals(FirmwareState.UpToDate, firmware.getState());

        Connection connection = spexor.getStatus().getConnection();

        assertEquals("2021-12-15T11:12:41.738Z", connection.getLastConnected());
        assertFalse(connection.isOnline());
        assertEquals(ConnectionType.Wifi, connection.getConnectionType());

        assertEquals(2, spexor.getStatus().getObservation().size());
        ObservationStatus burglaryStatus = spexor.getStatus().getObservation().get(0);
        assertEquals("Burglary", burglaryStatus.getObservationType());
        assertEquals(SensorMode.Deactivated, burglaryStatus.getSensorMode());

        ObservationStatus fireStatus = spexor.getStatus().getObservation().get(1);
        assertEquals("Fire", fireStatus.getObservationType());
        assertEquals(SensorMode.Activated, fireStatus.getSensorMode());
    }

    @Test
    void testSpexorSensorID() throws InterruptedException, TimeoutException, ExecutionException {
        String testResponse = "[{\"key\":\"AirQuality\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":212,\"minValue\":211,\"maxValue\":212,\"unit\":\"IAQ Index Table\"},{\"key\":\"AirQualityLevel\",\"timestamp\":\"2021-12-15T21:05:48.000Z\",\"value\":3,\"unit\":\"Air Quality Level\"},{\"key\":\"Temperature\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":22,\"minValue\":21,\"maxValue\":22,\"unit\":\"°C\"},{\"key\":\"Pressure\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":96650,\"minValue\":96650,\"maxValue\":96650,\"unit\":\"Pa\"},{\"key\":\"Acceleration\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":1006,\"minValue\":975,\"maxValue\":1012,\"unit\":\"mG\"},{\"key\":\"Light\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":6,\"minValue\":6,\"maxValue\":6,\"unit\":\"Light index\"},{\"key\":\"Humidity\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":50303,\"minValue\":49677,\"maxValue\":50303,\"unit\":\"% r.H.\"},{\"key\":\"Microphone\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":0,\"minValue\":0,\"maxValue\":0,\"unit\":\"Volume %\"},{\"key\":\"PassiveInfrared\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":2,\"unit\":\"event counter\"}]";
        when(authService.newRequest(any(), any(), any(), any())).thenReturn(Optional.of(request));
        when(request.send()).thenReturn(response);
        when(response.getContentAsString()).thenReturn(testResponse);
        when(response.getContent()).thenReturn(testResponse.getBytes(StandardCharsets.UTF_8));
        SpexorAPIService apiService = new SpexorAPIService(authService);

        Map<@NonNull String, @NonNull SensorValue<?>> sensorValues = apiService.getSensorValues("123456",
                Arrays.asList(TYPE_ACCELERATION, TYPE_AIR_QUALITY, TYPE_AIR_QUALITY_LEVEL, TYPE_FIRE, TYPE_GAS,
                        TYPE_HUMIDITY, TYPE_LIGHT, TYPE_MICROPHONE, TYPE_PASSIVE_INFRARED, TYPE_PRESSURE,
                        TYPE_TEMPERATURE));
        assertNotNull(sensorValues);
        assertTrue(sensorValues.size() > 0);
    }

    @Test
    void testSetObservationFailedResponse() throws InterruptedException, TimeoutException, ExecutionException {
        String testResponse = "[{\"key\":\"AirQuality\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":212,\"minValue\":211,\"maxValue\":212,\"unit\":\"IAQ Index Table\"},{\"key\":\"AirQualityLevel\",\"timestamp\":\"2021-12-15T21:05:48.000Z\",\"value\":3,\"unit\":\"Air Quality Level\"},{\"key\":\"Temperature\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":22,\"minValue\":21,\"maxValue\":22,\"unit\":\"°C\"},{\"key\":\"Pressure\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":96650,\"minValue\":96650,\"maxValue\":96650,\"unit\":\"Pa\"},{\"key\":\"Acceleration\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":1006,\"minValue\":975,\"maxValue\":1012,\"unit\":\"mG\"},{\"key\":\"Light\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":6,\"minValue\":6,\"maxValue\":6,\"unit\":\"Light index\"},{\"key\":\"Humidity\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":50303,\"minValue\":49677,\"maxValue\":50303,\"unit\":\"% r.H.\"},{\"key\":\"Microphone\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":0,\"minValue\":0,\"maxValue\":0,\"unit\":\"Volume %\"},{\"key\":\"PassiveInfrared\",\"timestamp\":\"2021-12-15T21:05:54.000Z\",\"value\":2,\"unit\":\"event counter\"}]";
        when(authService.newRequest(any(), eq("123456"), any())).thenReturn(Optional.of(request));
        when(request.send()).thenReturn(response);
        when(response.getContentAsString()).thenReturn(testResponse);
        when(response.getContent()).thenReturn(testResponse.getBytes(StandardCharsets.UTF_8));
        SpexorAPIService apiService = new SpexorAPIService(authService);

        ObservationChangeStatus observationChange = apiService.setObservation("123456", "Burglary", true);
        assertNotNull(observationChange);
        assertEquals("Burglary", observationChange.getObservationType());
        assertEquals(SensorMode.Deactivated, observationChange.getSensorMode());
        assertEquals(StatusCode.FAILURE, observationChange.getStatusCode());
        verify(request).method(HttpMethod.PATCH);
        verify(request, times(2)).accept(MimeTypes.Type.APPLICATION_JSON.toString());
        verify(request).content(
                argThat(matches("[{\"observationType\":\"Burglary\", \"sensorMode\":\"Activated\"}]", "UTF-8")),
                eq(ContentType.APPLICATION_JSON.toString()));
    }

    @Test
    void testGetSensorValues() throws InterruptedException, TimeoutException, ExecutionException {
        String testResponse = "[{\"key\":\"AirQuality\", \"timestamp\":\"2022-02-02T20:46:05.763Z\",\"value\":815, \"minValue\": null, \"maxValue\": 2, \"unit\": \"fake\"},"
                + "{\"key\":\"Fire\", \"timestamp\":\"2022-02-02T20:46:05.764Z\",\"value\":50, \"minValue\": 49, \"unit\": \"fake2\"}]";
        when(authService.newRequest(any(), eq("123456"), any(), eq("AirQuality,Fire,AirQualityLevel")))
                .thenReturn(Optional.of(request));
        when(request.send()).thenReturn(response);
        when(response.getContentAsString()).thenReturn(testResponse);
        when(response.getContent()).thenReturn(testResponse.getBytes(StandardCharsets.UTF_8));
        SpexorAPIService apiService = new SpexorAPIService(authService);

        Map<@NonNull String, @NonNull SensorValue<?>> sensorValues = apiService.getSensorValues("123456",
                Arrays.asList(TYPE_AIR_QUALITY, TYPE_FIRE, TYPE_AIR_QUALITY_LEVEL));
        assertNotNull(sensorValues);

        assertTrue(sensorValues.containsKey(TYPE_AIR_QUALITY));
        assertTrue(sensorValues.containsKey(TYPE_FIRE));
        assertFalse(sensorValues.containsKey(TYPE_AIR_QUALITY_LEVEL));

        assertNotNull(sensorValues.get(TYPE_AIR_QUALITY));
        assertEquals("fake", sensorValues.get(TYPE_AIR_QUALITY).getUnit());
        assertEquals(815, sensorValues.get(TYPE_AIR_QUALITY).getValue());
        assertFalse(sensorValues.get(TYPE_AIR_QUALITY).hasMinValue());
        assertEquals(2, sensorValues.get(TYPE_AIR_QUALITY).getMaxValue());
        assertEquals("2022-02-02T20:46:05.763Z", sensorValues.get(TYPE_AIR_QUALITY).getTimestamp());

        assertNotNull(sensorValues.get(TYPE_FIRE));
        assertEquals("fake2", sensorValues.get(TYPE_FIRE).getUnit());
        assertEquals(50, sensorValues.get(TYPE_FIRE).getValue());
        assertEquals(49, sensorValues.get(TYPE_FIRE).getMinValue());
        assertFalse(sensorValues.get(TYPE_FIRE).hasMaxValue());
        assertEquals("2022-02-02T20:46:05.764Z", sensorValues.get(TYPE_FIRE).getTimestamp());
    }

    @Test
    void testSetObservationSuccess() throws InterruptedException, TimeoutException, ExecutionException {
        String testResponse = "[{\"observationType\":\"Burglary\", \"sensorMode\":\"InActivation\",\"statusCode\":\"SUCCESS\", \"message\": null}]";
        when(authService.newRequest(any(), eq("123456"), any())).thenReturn(Optional.of(request));
        when(request.send()).thenReturn(response);
        when(response.getContentAsString()).thenReturn(testResponse);
        when(response.getContent()).thenReturn(testResponse.getBytes(StandardCharsets.UTF_8));
        SpexorAPIService apiService = new SpexorAPIService(authService);

        ObservationChangeStatus observationChange = apiService.setObservation("123456", "Burglary", true);
        assertNotNull(observationChange);
        assertEquals("Burglary", observationChange.getObservationType());
        assertEquals(SensorMode.InActivation, observationChange.getSensorMode());
        assertEquals(StatusCode.SUCCESS, observationChange.getStatusCode());
        verify(request).method(HttpMethod.PATCH);
        verify(request, times(2)).accept(MimeTypes.Type.APPLICATION_JSON.toString());

        verify(request).content(
                argThat(matches("[{\"observationType\":\"Burglary\", \"sensorMode\":\"Activated\"}]", "UTF-8")),
                eq(ContentType.APPLICATION_JSON.toString()));
    }

    private ArgumentMatcher<StringContentProvider> matches(String json, String encoding) {
        StringContentProvider expectedContent = new StringContentProvider(json, encoding);
        return new ArgumentMatcher<StringContentProvider>() {

            @Override
            public boolean matches(StringContentProvider actualObj) {
                boolean result = actualObj.getContentType().contentEquals(expectedContent.getContentType());
                Iterator<ByteBuffer> actualItr = actualObj.iterator();
                Iterator<ByteBuffer> expectedItr = expectedContent.iterator();
                while (result && actualItr.hasNext() && expectedItr.hasNext()) {
                    ByteBuffer actual = actualItr.next();
                    ByteBuffer expected = expectedItr.next();
                    result = actual.equals(expected) && actualItr.hasNext() == expectedItr.hasNext();
                }
                return result;
            }
        };
    }
}
