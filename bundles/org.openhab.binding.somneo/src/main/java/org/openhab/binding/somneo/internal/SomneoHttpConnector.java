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
package org.openhab.binding.somneo.internal;

import static org.openhab.binding.somneo.internal.SomneoBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.somneo.internal.model.AudioData;
import org.openhab.binding.somneo.internal.model.DeviceData;
import org.openhab.binding.somneo.internal.model.FirmwareData;
import org.openhab.binding.somneo.internal.model.LightData;
import org.openhab.binding.somneo.internal.model.PresetData;
import org.openhab.binding.somneo.internal.model.RadioData;
import org.openhab.binding.somneo.internal.model.RelaxData;
import org.openhab.binding.somneo.internal.model.SensorData;
import org.openhab.binding.somneo.internal.model.SunsetData;
import org.openhab.binding.somneo.internal.model.TimerData;
import org.openhab.binding.somneo.internal.model.WifiData;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SomneoHttpConnector} is responsible for sending commands.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class SomneoHttpConnector {

    private Logger logger = LoggerFactory.getLogger(SomneoHttpConnector.class);

    private static final int REQUEST_TIMEOUT_MS = 5000;

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private final Gson gson = new Gson();

    private final HttpClient httpClient;

    private final String urlBase;

    public SomneoHttpConnector(SomneoConfiguration config, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.urlBase = String.format("https://%s:%d/di/v1/products", config.hostname, config.port);
    }

    public SensorData fetchSensorData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", SENSORS_ENDPOINT, SensorData.class);
    }

    public LightData fetchLightData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", LIGHT_ENDPOINT, LightData.class);
    }

    public SunsetData fetchSunsetData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", SUNSET_ENDPOINT, SunsetData.class);
    }

    public void switchMainLight(boolean state) throws TimeoutException, InterruptedException, ExecutionException {
        final LightData data = new LightData();
        data.setMainLight(state);
        data.setNightLight(false);
        data.setPreviewLight(false);

        executeUrl("PUT", LIGHT_ENDPOINT, data);
    }

    public void setMainLightDimmer(int level) throws TimeoutException, InterruptedException, ExecutionException {
        final LightData data = new LightData();
        data.setMainLightLevel(level);
        data.setMainLight(true);
        data.setNightLight(false);
        data.setPreviewLight(false);

        executeUrl("PUT", LIGHT_ENDPOINT, data);
    }

    public void switchNightLight(boolean state) throws TimeoutException, InterruptedException, ExecutionException {
        final LightData data = new LightData();
        data.setMainLight(false);
        data.setNightLight(state);
        data.setPreviewLight(false);

        executeUrl("PUT", LIGHT_ENDPOINT, data);
    }

    public void switchSunsetProgram(boolean state) throws TimeoutException, InterruptedException, ExecutionException {
        final SunsetData data = new SunsetData();
        data.setState(state);

        executeUrl("PUT", SUNSET_ENDPOINT, data);
    }

    public void setSunsetLightIntensity(int percent) throws TimeoutException, InterruptedException, ExecutionException {
        final SunsetData data = new SunsetData();
        data.setLightIntensity(percent);

        executeUrl("PUT", SUNSET_ENDPOINT, data);
    }

    public void setSunsetDuration(int duration) throws TimeoutException, InterruptedException, ExecutionException {
        final SunsetData data = new SunsetData();
        data.setDurationInMin(duration);

        executeUrl("PUT", SUNSET_ENDPOINT, data);
    }

    public void setSunsetColorSchema(int value) throws TimeoutException, InterruptedException, ExecutionException {
        final SunsetData data = new SunsetData();
        data.setColorSchema(value);

        executeUrl("PUT", SUNSET_ENDPOINT, data);
    }

    public void setSunsetAmbientNoise(String option) throws TimeoutException, InterruptedException, ExecutionException {
        final SunsetData data = new SunsetData();
        data.setAmbientNoise(option);

        executeUrl("PUT", SUNSET_ENDPOINT, data);
    }

    public void setSunsetVolume(int percent) throws TimeoutException, InterruptedException, ExecutionException {
        final SunsetData data = new SunsetData();
        data.setSoundVolume(percent);

        executeUrl("PUT", SUNSET_ENDPOINT, data);
    }

    public RelaxData fetchRelaxData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", RELAX_ENDPOINT, RelaxData.class);
    }

    public void setRelaxVolume(int percent) throws TimeoutException, InterruptedException, ExecutionException {
        final RelaxData data = new RelaxData();
        data.setSoundVolume(percent);

        executeUrl("PUT", RELAX_ENDPOINT, data);
    }

    public void setRelaxLightIntensity(int percent) throws TimeoutException, InterruptedException, ExecutionException {
        final RelaxData data = new RelaxData();
        data.setLightIntensity(percent);

        executeUrl("PUT", RELAX_ENDPOINT, data);
    }

    public void switchRelaxProgram(boolean state) throws TimeoutException, InterruptedException, ExecutionException {
        final RelaxData data = new RelaxData();
        data.setState(state);

        executeUrl("PUT", RELAX_ENDPOINT, data);
    }

    public void setRelaxBreathingRate(int value) throws TimeoutException, InterruptedException, ExecutionException {
        final RelaxData data = new RelaxData();
        data.setBreathingRate(value);

        executeUrl("PUT", RELAX_ENDPOINT, data);
    }

    public void setRelaxDuration(int value) throws TimeoutException, InterruptedException, ExecutionException {
        final RelaxData data = new RelaxData();
        data.setDurationInMin(value);

        executeUrl("PUT", RELAX_ENDPOINT, data);
    }

    public void setRelaxGuidanceType(int value) throws TimeoutException, InterruptedException, ExecutionException {
        final RelaxData data = new RelaxData();
        data.setGuidanceType(value);

        executeUrl("PUT", RELAX_ENDPOINT, data);
    }

    public AudioData fetchAudioData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", AUDIO_ENDPOINT, AudioData.class);
    }

    public void switchRadio(boolean state) throws TimeoutException, InterruptedException, ExecutionException {
        final AudioData data = new AudioData();
        if (state) {
            data.enableRadio();
        } else {
            data.disableAudio();
        }

        executeUrl("PUT", AUDIO_ENDPOINT, data);
    }

    public void switchAux(boolean state) throws TimeoutException, InterruptedException, ExecutionException {
        final AudioData data = new AudioData();
        if (state) {
            data.enableAux();
        } else {
            data.disableAudio();
        }

        executeUrl("PUT", AUDIO_ENDPOINT, data);
    }

    public void setAudioVolume(int percent) throws TimeoutException, InterruptedException, ExecutionException {
        final AudioData data = new AudioData();
        data.setVolume(percent);

        executeUrl("PUT", AUDIO_ENDPOINT, data);
    }

    public void setRadioChannel(String preset) throws TimeoutException, InterruptedException, ExecutionException {
        final AudioData data = new AudioData();
        data.enableRadio();
        data.setRadioPreset(preset);

        executeUrl("PUT", AUDIO_ENDPOINT, data);
    }

    public RadioData getRadioData() throws TimeoutException, InterruptedException, ExecutionException {
        RadioData data = new RadioData();
        int loops = 0;
        do {
            if (loops > 20) {
                break;
            }
            if (loops > 0) {
                loops++;
                Thread.sleep(250);
            }
            data = executeUrl("GET", RADIO_ENDPOINT, RadioData.class);
        } while (data.isSeeking()); // Wait until seek is finished

        return data;
    }

    public void radioSeekUp() throws TimeoutException, InterruptedException, ExecutionException {
        final RadioData data = new RadioData();
        data.setCmdSeekUp();

        executeUrl("PUT", RADIO_ENDPOINT, data);
    }

    public void radioSeekDown() throws TimeoutException, InterruptedException, ExecutionException {
        final RadioData data = new RadioData();
        data.setCmdSeekDown();

        executeUrl("PUT", RADIO_ENDPOINT, data);
    }

    public DeviceData fetchDeviceData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", DEVICE_ENDPOINT, DeviceData.class);
    }

    public WifiData fetchWifiData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", WIFI_ENDPOINT, WifiData.class);
    }

    public FirmwareData fetchFirmwareData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", FIRMWARE_ENDPOINT, FirmwareData.class);
    }

    public TimerData fetchTimerData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", TIMER_ENDPOINT, TimerData.class);
    }

    public PresetData fetchPresetData() throws TimeoutException, InterruptedException, ExecutionException {
        return executeUrl("GET", PRESET_ENDPOINT, PresetData.class);
    }

    private <T> T executeUrl(String httpMethod, String endpoint, Class<T> classOfT)
            throws TimeoutException, InterruptedException, ExecutionException {
        final String responseBody = executeUrl("GET", endpoint, (String) null);
        final T data = gson.fromJson(responseBody, classOfT);
        return data;
    }

    private void executeUrl(String httpMethod, String endpoint, Object data)
            throws TimeoutException, InterruptedException, ExecutionException {
        final String content = gson.toJson(data);
        executeUrl(httpMethod, endpoint, content);
    }

    /**
     * Executes the given <code>url</code> with the given <code>httpMethod</code>
     *
     * @param httpMethod the HTTP method to use
     * @param endpoint the url endpoint
     * @param content the content to be sent to the given <code>url</code> or
     *            <code>null</code> if no content should be sent.
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     * @throws Exception when the request execution failed, timed out or it was interrupted
     */
    private String executeUrl(String httpMethod, String endpoint, @Nullable String content)
            throws TimeoutException, InterruptedException, ExecutionException {
        final String url = urlBase + endpoint;
        final HttpMethod method = HttpUtil.createHttpMethod(httpMethod);

        final Request request = httpClient.newRequest(url).method(method).timeout(REQUEST_TIMEOUT_MS,
                TimeUnit.MILLISECONDS);

        if (content != null && (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method))) {
            final StringContentProvider stringContentProvider = new StringContentProvider(content,
                    StandardCharsets.UTF_8);
            request.content(stringContentProvider, DEFAULT_CONTENT_TYPE);

            logger.trace("Request for url '{}':\r\n{}", url, content);
        } else {
            logger.trace("Request for url '{}'", url);
        }

        final ContentResponse response = request.send();
        final int statusCode = response.getStatus();
        if (logger.isDebugEnabled() && statusCode >= HttpStatus.BAD_REQUEST_400) {
            String statusLine = statusCode + " " + response.getReason();
            logger.debug("Method failed: {}", statusLine);
        }

        final String encoding = response.getEncoding() != null ? response.getEncoding().replaceAll("\"", "").trim()
                : StandardCharsets.UTF_8.name();

        try {
            String responseBody = new String(response.getContent(), encoding);
            logger.trace("Response for url '{}':\r\n{}", url, responseBody);
            return responseBody;
        } catch (UnsupportedEncodingException e) {
            logger.warn("Get response content failed!", e);
            return "";
        }
    }
}
