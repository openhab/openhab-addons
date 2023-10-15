/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static java.util.Map.entry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMFanTimerMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatEcoMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatMode;

/**
 * The {@link SDMCommands} provides classes used for mapping all SDM REST API device command requests and responses.
 *
 * @author Wouter Born - Initial contribution
 *
 * @see <a href="https://developers.google.com/nest/device-access/reference/rest/v1/enterprises.devices/executeCommand">
 *      https://developers.google.com/nest/device-access/reference/rest/v1/enterprises.devices/executeCommand</a>
 */
public class SDMCommands {

    /**
     * Command request parent.
     */
    public abstract static class SDMCommandRequest<T extends SDMCommandResponse> {
        private final String command;
        private final Map<String, Object> params = new LinkedHashMap<>();

        @SafeVarargs
        private SDMCommandRequest(String command, Entry<String, Object>... params) {
            this.command = command;
            for (Entry<String, Object> param : params) {
                this.params.put(param.getKey(), param.getValue());
            }
        }

        public String getCommand() {
            return command;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        @SuppressWarnings("unchecked")
        public Class<T> getResponseClass() {
            return (Class<T>) SDMCommandResponse.class;
        }
    }

    /**
     * Command response parent. This class is also used for responses without additional data.
     */
    public static class SDMCommandResponse {
    }

    // CameraEventImage trait commands

    /**
     * Generates a download URL for the image related to a camera event.
     */
    public static class SDMGenerateCameraImageRequest extends SDMCommandRequest<SDMGenerateCameraImageResponse> {

        /**
         * Event images expire 30 seconds after the event is published. Make sure to download the image prior to
         * expiration.
         */
        public static final Duration EVENT_IMAGE_VALIDITY = Duration.ofSeconds(30);

        /**
         * @param eventId ID of the camera event to request a related image for.
         */
        public SDMGenerateCameraImageRequest(String eventId) {
            super("sdm.devices.commands.CameraEventImage.GenerateImage", entry("eventId", eventId));
        }

        @Override
        public Class<SDMGenerateCameraImageResponse> getResponseClass() {
            return SDMGenerateCameraImageResponse.class;
        }
    }

    public static class SDMGenerateCameraImageResults {
        /**
         * The URL to download the camera image from.
         */
        public String url;

        /**
         * Token to use in the HTTP Authorization header when downloading the camera image.
         */
        public String token;
    }

    public static class SDMGenerateCameraImageResponse extends SDMCommandResponse {
        public SDMGenerateCameraImageResults results;
    }

    // CameraLiveStream trait commands

    /**
     * Request a token to access a camera RTSP live stream URL.
     */
    public static class SDMGenerateCameraRtspStreamRequest
            extends SDMCommandRequest<SDMGenerateCameraRtspStreamResponse> {
        public SDMGenerateCameraRtspStreamRequest() {
            super("sdm.devices.commands.CameraLiveStream.GenerateRtspStream");
        }

        @Override
        public Class<SDMGenerateCameraRtspStreamResponse> getResponseClass() {
            return SDMGenerateCameraRtspStreamResponse.class;
        }
    }

    /**
     * Camera RTSP live stream URLs.
     */
    public static class SDMCameraRtspStreamUrls {
        public String rtspUrl;
    }

    public static class SDMGenerateCameraRtspStreamResults {
        /**
         * Camera RTSP live stream URLs.
         */
        public SDMCameraRtspStreamUrls streamUrls;

        /**
         * Token to use to extend the {@link #streamToken} for an RTSP live stream.
         */
        public String streamExtensionToken;

        /**
         * Token to use to access an RTSP live stream.
         */
        public String streamToken;

        /**
         * Time at which both {@link #streamExtensionToken} and {@link #streamToken} expire.
         */
        public ZonedDateTime expiresAt;
    }

    public static class SDMGenerateCameraRtspStreamResponse extends SDMCommandResponse {
        public SDMGenerateCameraRtspStreamResults results;
    }

    /**
     * Request a new RTSP live stream URL access token to replace a valid RTSP access token before it expires. This is
     * also used to replace a valid RTSP token from a previous ExtendRtspStream command request.
     */
    public static class SDMExtendCameraRtspStreamRequest extends SDMCommandRequest<SDMExtendCameraRtspStreamResponse> {
        /**
         * @param streamExtensionToken Token to use to request an extension to the RTSP streaming token.
         */
        public SDMExtendCameraRtspStreamRequest(String streamExtensionToken) {
            super("sdm.devices.commands.CameraLiveStream.ExtendRtspStream",
                    entry("streamExtensionToken", streamExtensionToken));
        }

        @Override
        public Class<SDMExtendCameraRtspStreamResponse> getResponseClass() {
            return SDMExtendCameraRtspStreamResponse.class;
        }
    }

    public static class SDMExtendCameraRtspStreamResults {
        /**
         * Token to use to view an existing RTSP live stream and to request an extension to the streaming token.
         */
        public String streamExtensionToken;

        /**
         * New token to use to access an existing RTSP live stream.
         */
        public String streamToken;

        /**
         * Time at which both {@link #streamExtensionToken} and {@link #streamToken} expire.
         */
        public ZonedDateTime expiresAt;
    }

    public static class SDMExtendCameraRtspStreamResponse extends SDMCommandResponse {
        public SDMExtendCameraRtspStreamResults results;
    }

    /**
     * Invalidates a valid RTSP access token and stops the RTSP live stream tied to that access token.
     */
    public static class SDMStopCameraRtspStreamRequest extends SDMCommandRequest<SDMCommandResponse> {
        /**
         * @param streamExtensionToken Token to use to invalidate an existing RTSP live stream.
         */
        public SDMStopCameraRtspStreamRequest(String streamExtensionToken) {
            super("sdm.devices.commands.CameraLiveStream.StopRtspStream",
                    entry("streamExtensionToken", streamExtensionToken));
        }
    }

    // Fan trait commands

    /**
     * Change the fan timer.
     */
    public static class SDMSetFanTimerRequest extends SDMCommandRequest<SDMCommandResponse> {
        public SDMSetFanTimerRequest(SDMFanTimerMode timerMode) {
            super("sdm.devices.commands.Fan.SetTimer", entry("timerMode", timerMode.name()));
        }

        /**
         * @param duration Specifies the length of time in seconds that the timer is set to run.
         *            Range: "1s" to "43200s"
         *            Default: "900s"
         */
        public SDMSetFanTimerRequest(SDMFanTimerMode timerMode, Duration duration) {
            super("sdm.devices.commands.Fan.SetTimer", entry("timerMode", timerMode.name()),
                    entry("duration", duration.toSeconds() + "s"));
        }
    }

    // ThermostatEco trait commands

    /**
     * Change the thermostat Eco mode.
     *
     * To change the thermostat mode to HEAT, COOL, or HEATCOOL, use the {@link SDMSetThermostatModeRequest}.
     * <br>
     * <br>
     * This command impacts other traits, based on the current status of, or changes to, the Eco mode:
     * <ul>
     * <li>If Eco mode is OFF, the thermostat mode will default to the last standard mode (HEAT, COOL, HEATCOOL, or OFF)
     * that was active.</li>
     * <li>If Eco mode is MANUAL_ECO:
     * <ul>
     * <li>Commands for the ThermostatTemperatureSetpoint trait are rejected.</li>
     * <li>Temperature setpoints are not returned by the ThermostatTemperatureSetpoint trait.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * Some thermostat models do not support changing the Eco mode when the thermostat mode is OFF, according to the
     * ThermostatMode trait. The thermostat mode must be changed to HEAT, COOL, or HEATCOOL prior to changing the Eco
     * mode.
     */
    public static class SDMSetThermostatEcoModeRequest extends SDMCommandRequest<SDMCommandResponse> {
        public SDMSetThermostatEcoModeRequest(SDMThermostatEcoMode mode) {
            super("sdm.devices.commands.ThermostatEco.SetMode", entry("mode", mode.name()));
        }
    }

    // ThermostatMode trait commands

    /**
     * Change the thermostat mode.
     */
    public static class SDMSetThermostatModeRequest extends SDMCommandRequest<SDMCommandResponse> {
        public SDMSetThermostatModeRequest(SDMThermostatMode mode) {
            super("sdm.devices.commands.ThermostatMode.SetMode", entry("mode", mode.name()));
        }
    }

    // ThermostatTemperatureSetpoint trait commands

    /**
     * Sets the target temperature when the thermostat is in COOL mode.
     */
    public static class SDMSetThermostatCoolSetpointRequest extends SDMCommandRequest<SDMCommandResponse> {
        /**
         * @param temperature the target temperature in degrees Celsius
         */
        public SDMSetThermostatCoolSetpointRequest(BigDecimal temperature) {
            super("sdm.devices.commands.ThermostatTemperatureSetpoint.SetCool", entry("coolCelsius", temperature));
        }
    }

    /**
     * Sets the target temperature when the thermostat is in HEAT mode.
     */
    public static class SDMSetThermostatHeatSetpointRequest extends SDMCommandRequest<SDMCommandResponse> {
        /**
         * @param temperature the target temperature in degrees Celsius
         */
        public SDMSetThermostatHeatSetpointRequest(BigDecimal temperature) {
            super("sdm.devices.commands.ThermostatTemperatureSetpoint.SetHeat", entry("heatCelsius", temperature));
        }
    }

    /**
     * Sets the minimum and maximum temperatures when the thermostat is in HEATCOOL mode.
     */
    public static class SDMSetThermostatRangeSetpointRequest extends SDMCommandRequest<SDMCommandResponse> {
        /**
         * @param minTemperature the minimum target temperature in degrees Celsius
         * @param maxTemperature the maximum target temperature in degrees Celsius
         */
        public SDMSetThermostatRangeSetpointRequest(BigDecimal minTemperature, BigDecimal maxTemperature) {
            super("sdm.devices.commands.ThermostatTemperatureSetpoint.SetRange", entry("heatCelsius", minTemperature),
                    entry("coolCelsius", maxTemperature));
        }
    }
}
