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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.nest.internal.sdm.dto.SDMDataUtil.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMCameraRtspStreamUrls;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMExtendCameraRtspStreamRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMExtendCameraRtspStreamResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMExtendCameraRtspStreamResults;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraImageRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraImageResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraImageResults;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraRtspStreamRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraRtspStreamResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraRtspStreamResults;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetFanTimerRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatCoolSetpointRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatEcoModeRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatHeatSetpointRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatModeRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMSetThermostatRangeSetpointRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMStopCameraRtspStreamRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMFanTimerMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatEcoMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatMode;

/**
 * Tests (de)serialization of {@link org.openhab.binding.nest.internal.sdm.dto.SDMCommands} requests
 * and responses from/to JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMCommandsTest {

    @Test
    public void deserializeExtendCameraRtspStreamResponse() throws IOException {
        SDMExtendCameraRtspStreamResponse response = fromJson("extend-camera-rtsp-stream-response.json",
                SDMExtendCameraRtspStreamResponse.class);
        assertThat(response, is(notNullValue()));

        SDMExtendCameraRtspStreamResults results = response.results;
        assertThat(results, is(notNullValue()));

        assertThat(results.streamExtensionToken, is("dGNUlTU2CjY5Y3VKaTZwR3o4Y1..."));
        assertThat(results.streamToken, is("g.0.newStreamingToken"));
        assertThat(results.expiresAt, is(ZonedDateTime.parse("2018-01-04T18:30:00.000Z")));
    }

    @Test
    public void deserializeGenerateCameraImageResponse() throws IOException {
        SDMGenerateCameraImageResponse response = fromJson("generate-camera-image-response.json",
                SDMGenerateCameraImageResponse.class);
        assertThat(response, is(notNullValue()));

        SDMGenerateCameraImageResults results = response.results;
        assertThat(results, is(notNullValue()));
        assertThat(results.url, is("https://domain/sdm_resource/dGNUlTU2CjY5Y3VKaTZwR3o4Y1..."));
        assertThat(results.token, is("g.0.eventToken"));
    }

    @Test
    public void deserializeGenerateCameraRtspStreamResponse() throws IOException {
        SDMGenerateCameraRtspStreamResponse response = fromJson("generate-camera-rtsp-stream-response.json",
                SDMGenerateCameraRtspStreamResponse.class);
        assertThat(response, is(notNullValue()));

        SDMGenerateCameraRtspStreamResults results = response.results;
        assertThat(results, is(notNullValue()));

        SDMCameraRtspStreamUrls streamUrls = results.streamUrls;
        assertThat(streamUrls, is(notNullValue()));
        assertThat(streamUrls.rtspUrl, is("rtsps://someurl.com/CjY5Y3VKaTZwR3o4Y19YbTVfMF...?auth=g.0.streamingToken"));

        assertThat(results.streamExtensionToken, is("CjY5Y3VKaTZwR3o4Y19YbTVfMF..."));
        assertThat(results.streamToken, is("g.0.streamingToken"));
        assertThat(results.expiresAt, is(ZonedDateTime.parse("2018-01-04T18:30:00.000Z")));
    }

    @Test
    public void serializeExtendCameraRtspStreamRequest() throws IOException {
        String json = toJson(new SDMExtendCameraRtspStreamRequest("CjY5Y3VKaTZwR3o4Y19YbTVfMF..."));
        assertThat(json, is(fromFile("extend-camera-rtsp-stream-request.json")));
    }

    @Test
    public void serializeGenerateCameraImageRequest() throws IOException {
        String json = toJson(new SDMGenerateCameraImageRequest("FWWVQVUdGNUlTU2V4MGV2aTNXV..."));
        assertThat(json, is(fromFile("generate-camera-image-request.json")));
    }

    @Test
    public void serializeGenerateCameraRtspStreamRequest() throws IOException {
        String json = toJson(new SDMGenerateCameraRtspStreamRequest());
        assertThat(json, is(fromFile("generate-camera-rtsp-stream-request.json")));
    }

    @Test
    public void serializeSetFanTimerRequestWithDuration() throws IOException {
        String json = toJson(new SDMSetFanTimerRequest(SDMFanTimerMode.ON, Duration.ofSeconds(3600)));
        assertThat(json, is(fromFile("set-fan-timer-request-with-duration.json")));
    }

    @Test
    public void serializeSetFanTimerRequestWithoutDuration() throws IOException {
        String json = toJson(new SDMSetFanTimerRequest(SDMFanTimerMode.ON));
        assertThat(json, is(fromFile("set-fan-timer-request-without-duration.json")));
    }

    @Test
    public void serializeSetThermostatCoolSetpointRequest() throws IOException {
        String json = toJson(new SDMSetThermostatCoolSetpointRequest(new BigDecimal("20.0")));
        assertThat(json, is(fromFile("set-thermostat-cool-setpoint-request.json")));
    }

    @Test
    public void serializeSetThermostatEcoModeRequest() throws IOException {
        String json = toJson(new SDMSetThermostatEcoModeRequest(SDMThermostatEcoMode.MANUAL_ECO));
        assertThat(json, is(fromFile("set-thermostat-eco-mode-request.json")));
    }

    @Test
    public void serializeSetThermostatHeatSetpointRequest() throws IOException {
        String json = toJson(new SDMSetThermostatHeatSetpointRequest(new BigDecimal("15.0")));
        assertThat(json, is(fromFile("set-thermostat-heat-setpoint-request.json")));
    }

    @Test
    public void serializeSetThermostatModeRequest() throws IOException {
        String json = toJson(new SDMSetThermostatModeRequest(SDMThermostatMode.HEATCOOL));
        assertThat(json, is(fromFile("set-thermostat-mode-request.json")));
    }

    @Test
    public void serializeSetThermostatRangeSetpointRequest() throws IOException {
        String json = toJson(new SDMSetThermostatRangeSetpointRequest(new BigDecimal("15.0"), new BigDecimal("20.0")));
        assertThat(json, is(fromFile("set-thermostat-range-setpoint-request.json")));
    }

    @Test
    public void serializeStopCameraRtspStreamRequest() throws IOException {
        String json = toJson(new SDMStopCameraRtspStreamRequest("CjY5Y3VKaTZwR3o4Y19YbTVfMF..."));
        assertThat(json, is(fromFile("stop-camera-rtsp-stream-request.json")));
    }
}
