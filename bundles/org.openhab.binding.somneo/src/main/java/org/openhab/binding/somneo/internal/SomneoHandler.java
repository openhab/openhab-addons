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
package org.openhab.binding.somneo.internal;

import static org.openhab.binding.somneo.internal.SomneoBindingConstants.*;

import java.io.EOFException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomneoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class SomneoHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomneoHandler.class);

    private final HttpClientProvider httpClientProvider;

    private final SomneoPresetStateDescriptionProvider provider;

    /**
     * Job to poll data from the device.
     */
    private @Nullable ScheduledFuture<?> pollingJob;

    /**
     * Job to count down the remaining program time.
     */
    private @Nullable ScheduledFuture<?> remainingTimerJob;

    private @Nullable SomneoHttpConnector connector;

    /**
     * Cache the last brightness level in order to know the correct level when the
     * ON command is given.
     */
    private volatile int lastLightBrightness;

    private volatile int remainingTimeRelax;

    private volatile int remainingTimeSunset;

    public SomneoHandler(Thing thing, HttpClientProvider httpClientProvider,
            SomneoPresetStateDescriptionProvider provider) {
        super(thing);
        this.httpClientProvider = httpClientProvider;
        this.provider = provider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        logger.debug("Handle command '{}' for channel {}", command, channelId);

        if (command instanceof RefreshType) {
            this.poll();
            return;
        }

        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        try {
            switch (channelId) {
                case CHANNEL_AUDIO_AUX:
                    if (command instanceof OnOffType) {
                        boolean isOn = OnOffType.ON.equals(command);
                        connector.switchAux(isOn);

                        if (isOn) {
                            updateState(CHANNEL_AUDIO_RADIO, PlayPauseType.PAUSE);
                            updateState(CHANNEL_RELAX_SWITCH, OnOffType.OFF);
                            updateState(CHANNEL_SUNSET_SWITCH, OnOffType.OFF);
                        }
                    }
                    break;
                case CHANNEL_AUDIO_PRESET:
                    if (command instanceof StringType) {
                        connector.setRadioChannel(command.toFullString());

                        updateState(CHANNEL_AUDIO_RADIO, PlayPauseType.PLAY);
                        updateState(CHANNEL_AUDIO_AUX, OnOffType.OFF);
                        updateState(CHANNEL_RELAX_SWITCH, OnOffType.OFF);
                        updateState(CHANNEL_SUNSET_SWITCH, OnOffType.OFF);

                        updateFrequency();
                    }
                    break;
                case CHANNEL_AUDIO_RADIO:
                    if (command instanceof PlayPauseType) {
                        boolean isPlaying = PlayPauseType.PLAY.equals(command);
                        connector.switchRadio(isPlaying);

                        if (isPlaying) {
                            updateState(CHANNEL_AUDIO_AUX, OnOffType.OFF);
                            updateState(CHANNEL_RELAX_SWITCH, OnOffType.OFF);
                            updateState(CHANNEL_SUNSET_SWITCH, OnOffType.OFF);
                        }
                    } else if (command instanceof NextPreviousType && NextPreviousType.NEXT.equals(command)) {
                        connector.radioSeekUp();

                        updateFrequency();
                    } else if (command instanceof NextPreviousType && NextPreviousType.PREVIOUS.equals(command)) {
                        connector.radioSeekDown();

                        updateFrequency();
                    }
                    break;
                case CHANNEL_AUDIO_VOLUME:
                    if (command instanceof PercentType) {
                        connector.setAudioVolume(Integer.parseInt(command.toFullString()));
                    }
                    break;
                case CHANNEL_LIGHT_MAIN:
                    if (command instanceof OnOffType) {
                        boolean isOn = OnOffType.ON.equals(command);
                        connector.switchMainLight(isOn);

                        if (isOn) {
                            updateState(CHANNEL_LIGHT_MAIN, new PercentType(lastLightBrightness));
                            updateState(CHANNEL_LIGHT_NIGHT, OnOffType.OFF);
                            updateState(CHANNEL_RELAX_SWITCH, OnOffType.OFF);
                            updateState(CHANNEL_SUNSET_SWITCH, OnOffType.OFF);
                        }
                    }
                    if (command instanceof PercentType) {
                        int level = Integer.parseInt(command.toFullString());

                        if (level > 0) {
                            connector.setMainLightDimmer(level);
                            lastLightBrightness = level;

                            updateState(CHANNEL_LIGHT_NIGHT, OnOffType.OFF);
                            updateState(CHANNEL_RELAX_SWITCH, OnOffType.OFF);
                            updateState(CHANNEL_SUNSET_SWITCH, OnOffType.OFF);
                        } else {
                            connector.switchMainLight(false);
                        }
                    }
                    break;
                case CHANNEL_LIGHT_NIGHT:
                    if (command instanceof OnOffType) {
                        boolean isOn = OnOffType.ON.equals(command);
                        connector.switchNightLight(isOn);

                        if (isOn) {
                            updateState(CHANNEL_LIGHT_MAIN, OnOffType.OFF);
                            updateState(CHANNEL_RELAX_SWITCH, OnOffType.OFF);
                            updateState(CHANNEL_SUNSET_SWITCH, OnOffType.OFF);
                        }
                    }
                    break;
                case CHANNEL_RELAX_BREATHING_RATE:
                    if (command instanceof DecimalType) {
                        connector.setRelaxBreathingRate(Integer.parseInt(command.toFullString()));
                    }
                    break;
                case CHANNEL_RELAX_DURATION:
                    if (command instanceof DecimalType) {
                        connector.setRelaxDuration(Integer.parseInt(command.toFullString()));
                    }
                    break;
                case CHANNEL_RELAX_GUIDANCE_TYPE:
                    if (command instanceof DecimalType) {
                        connector.setRelaxGuidanceType(Integer.parseInt(command.toFullString()));
                    }
                    break;
                case CHANNEL_RELAX_LIGHT_INTENSITY:
                    if (command instanceof PercentType) {
                        connector.setRelaxLightIntensity(Integer.parseInt(command.toFullString()));
                    }
                    break;
                case CHANNEL_RELAX_SWITCH:
                    if (command instanceof OnOffType) {
                        boolean isOn = OnOffType.ON.equals(command);
                        connector.switchRelaxProgram(isOn);

                        updateRemainingTimer();

                        if (isOn) {
                            updateState(CHANNEL_AUDIO_AUX, OnOffType.OFF);
                            updateState(CHANNEL_AUDIO_RADIO, PlayPauseType.PAUSE);
                            updateState(CHANNEL_LIGHT_MAIN, OnOffType.OFF);
                            updateState(CHANNEL_LIGHT_NIGHT, OnOffType.OFF);
                            updateState(CHANNEL_SUNSET_SWITCH, OnOffType.OFF);
                        }
                    }
                    break;
                case CHANNEL_RELAX_VOLUME:
                    if (command instanceof PercentType) {
                        connector.setRelaxVolume(Integer.parseInt(command.toFullString()));
                    }
                    break;
                case CHANNEL_SUNSET_AMBIENT_NOISE:
                    if (command instanceof StringType) {
                        connector.setSunsetAmbientNoise(command.toFullString());
                    }
                    break;
                case CHANNEL_SUNSET_COLOR_SCHEMA:
                    if (command instanceof DecimalType) {
                        connector.setSunsetColorSchema(Integer.parseInt(command.toFullString()));
                    }
                    break;
                case CHANNEL_SUNSET_DURATION:
                    if (command instanceof DecimalType) {
                        connector.setSunsetDuration(Integer.parseInt(command.toFullString()));
                    }
                    break;
                case CHANNEL_SUNSET_LIGHT_INTENSITY:
                    if (command instanceof PercentType) {
                        connector.setSunsetLightIntensity(Integer.parseInt(command.toFullString()));
                    }
                    break;
                case CHANNEL_SUNSET_SWITCH:
                    if (command instanceof OnOffType) {
                        boolean isOn = OnOffType.ON.equals(command);
                        connector.switchSunsetProgram(isOn);

                        updateRemainingTimer();

                        if (isOn) {
                            updateState(CHANNEL_AUDIO_AUX, OnOffType.OFF);
                            updateState(CHANNEL_AUDIO_RADIO, PlayPauseType.PAUSE);
                            updateState(CHANNEL_LIGHT_MAIN, OnOffType.OFF);
                            updateState(CHANNEL_LIGHT_NIGHT, OnOffType.OFF);
                            updateState(CHANNEL_RELAX_SWITCH, OnOffType.OFF);
                        }
                    }
                    break;
                case CHANNEL_SUNSET_VOLUME:
                    if (command instanceof PercentType) {
                        connector.setSunsetVolume(Integer.parseInt(command.toFullString()));
                    }
                    break;
                default:
                    logger.warn("Received unknown channel {}", channelId);
                    break;
            }
        } catch (InterruptedException e) {
            logger.debug("Handle command interrupted");
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            if (e.getCause() instanceof EOFException) {
                // Occurs on parallel mobile app access
                logger.debug("EOF: {}", e.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        initConnector();
        updateThingProperties();
        startPolling();
    }

    @Override
    public void dispose() {
        stopPolling();
        stopRemainingTimer();

        super.dispose();
    }

    private void initConnector() {
        if (connector == null) {
            SomneoConfiguration config = getConfigAs(SomneoConfiguration.class);
            HttpClient httpClient;
            if (config.ignoreSSLErrors) {
                logger.info("Using the insecure client for thing '{}'.", thing.getUID());
                httpClient = httpClientProvider.getInsecureClient();
            } else {
                logger.info("Using the secure client for thing '{}'.", thing.getUID());
                httpClient = httpClientProvider.getSecureClient();
            }

            connector = new SomneoHttpConnector(config, httpClient);
        }
    }

    private void updateThingProperties() {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_VENDOR, PROPERTY_VENDOR_NAME);

        try {
            final DeviceData deviceData = connector.fetchDeviceData();
            String value = deviceData.getModelId();
            if (value != null) {
                properties.put(Thing.PROPERTY_MODEL_ID, value);
            }
            value = deviceData.getSerial();
            if (value != null) {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, value);
            }

            final WifiData wifiData = connector.fetchWifiData();
            value = wifiData.getMacAddress();
            if (value != null) {
                properties.put(Thing.PROPERTY_MAC_ADDRESS, value);
            }

            final FirmwareData firmwareData = connector.fetchFirmwareData();
            value = firmwareData.getVersion();
            if (value != null) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, value);
            }

            updateProperties(properties);
        } catch (InterruptedException e) {
            logger.debug("Update properties interrupted");
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            if (e.getCause() instanceof EOFException) {
                // Occurs on parallel mobile app access
                logger.debug("EOF: {}", e.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Set up the connection to the receiver by starting to poll the HTTP API.
     */
    private void startPolling() {
        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null && !pollingJob.isCancelled()) {
            return;
        }

        int refreshInterval = getConfigAs(SomneoConfiguration.class).refreshInterval;
        logger.debug("Start polling job at interval {}s", refreshInterval);
        this.pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, refreshInterval, TimeUnit.SECONDS);
    }

    private void stopPolling() {
        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob == null || pollingJob.isCancelled()) {
            return;
        }

        pollingJob.cancel(true);
        this.pollingJob = null;
        logger.debug("HTTP polling stopped.");
    }

    private void poll() {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        try {
            final SensorData sensorData = connector.fetchSensorData();
            updateState(CHANNEL_SENSOR_HUMIDITY, sensorData.getCurrentHumidity());
            updateState(CHANNEL_SENSOR_ILLUMINANCE, sensorData.getCurrentIlluminance());
            updateState(CHANNEL_SENSOR_NOISE, sensorData.getCurrentNoise());
            updateState(CHANNEL_SENSOR_TEMPERATURE, sensorData.getCurrentTemperature());

            final LightData lightData = connector.fetchLightData();
            updateState(CHANNEL_LIGHT_MAIN, lightData.getMainLightState());
            updateState(CHANNEL_LIGHT_NIGHT, lightData.getNightLightState());
            lastLightBrightness = lightData.getMainLightLevel();

            final SunsetData sunsetData = connector.fetchSunsetData();
            updateState(CHANNEL_SUNSET_SWITCH, sunsetData.getSwitchState());
            updateState(CHANNEL_SUNSET_LIGHT_INTENSITY, sunsetData.getLightIntensity());
            updateState(CHANNEL_SUNSET_DURATION, sunsetData.getDurationInMin());
            updateState(CHANNEL_SUNSET_COLOR_SCHEMA, sunsetData.getColorSchema());
            updateState(CHANNEL_SUNSET_AMBIENT_NOISE, sunsetData.getAmbientNoise());
            updateState(CHANNEL_SUNSET_VOLUME, sunsetData.getSoundVolume());

            final RelaxData relaxData = connector.fetchRelaxData();
            updateState(CHANNEL_RELAX_SWITCH, relaxData.getSwitchState());
            updateState(CHANNEL_RELAX_BREATHING_RATE, relaxData.getBreathingRate());
            updateState(CHANNEL_RELAX_DURATION, relaxData.getDurationInMin());
            updateState(CHANNEL_RELAX_GUIDANCE_TYPE, relaxData.getGuidanceType());
            updateState(CHANNEL_RELAX_LIGHT_INTENSITY, relaxData.getLightIntensity());
            updateState(CHANNEL_RELAX_VOLUME, relaxData.getSoundVolume());

            final AudioData audioData = connector.fetchAudioData();
            updateState(CHANNEL_AUDIO_RADIO, audioData.getRadioState());
            updateState(CHANNEL_AUDIO_AUX, audioData.getAuxState());
            updateState(CHANNEL_AUDIO_VOLUME, audioData.getVolumeState());
            updateState(CHANNEL_AUDIO_PRESET, audioData.getPresetState());

            updateFrequency();

            updateRemainingTimer();

            updateStatus(ThingStatus.ONLINE);
        } catch (InterruptedException e) {
            logger.debug("Polling data interrupted");
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            if (e.getCause() instanceof EOFException) {
                // Occurs on parallel mobile app access
                logger.debug("EOF: {}", e.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void updateFrequency() throws TimeoutException, InterruptedException, ExecutionException {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        RadioData radioData = connector.getRadioData();
        updateState(CHANNEL_AUDIO_FREQUENCY, radioData.getFrequency());

        final PresetData presetData = connector.fetchPresetData();
        final Channel presetChannel = getThing().getChannel(CHANNEL_AUDIO_PRESET);
        if (presetChannel != null) {
            provider.setStateOptions(presetChannel.getUID(), presetData.createPresetOptions());
        }
    }

    private void updateRemainingTimer() throws TimeoutException, InterruptedException, ExecutionException {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        TimerData timerData = connector.fetchTimerData();

        remainingTimeRelax = timerData.remainingTimeRelax();
        remainingTimeSunset = timerData.remainingTimeSunset();

        if (remainingTimeRelax > 0 || remainingTimeSunset > 0) {
            startRemainingTimer();
        } else {
            State state = new QuantityType<>(0, Units.SECOND);
            updateState(CHANNEL_RELAX_REMAINING_TIME, state);
            updateState(CHANNEL_SUNSET_REMAINING_TIME, state);
        }
    }

    private void startRemainingTimer() {
        final ScheduledFuture<?> remainingTimerJob = this.remainingTimerJob;
        if (remainingTimerJob != null && !remainingTimerJob.isCancelled()) {
            return;
        }

        logger.debug("Start remaining timer ticker job");
        this.remainingTimerJob = scheduler.scheduleWithFixedDelay(this::remainingTimerTick, 0, 1, TimeUnit.SECONDS);
    }

    private void stopRemainingTimer() {
        final ScheduledFuture<?> remainingTimerJob = this.remainingTimerJob;
        if (remainingTimerJob == null || remainingTimerJob.isCancelled()) {
            return;
        }

        remainingTimerJob.cancel(true);
        this.remainingTimerJob = null;
        logger.debug("Remaining timer ticker stopped.");
    }

    private void remainingTimerTick() {
        if (remainingTimeRelax > 0) {
            remainingTimeRelax--;

            State state = new QuantityType<>(remainingTimeRelax, Units.SECOND);
            updateState(CHANNEL_RELAX_REMAINING_TIME, state);
        }

        if (remainingTimeSunset > 0) {
            remainingTimeSunset--;

            State state = new QuantityType<>(remainingTimeSunset, Units.SECOND);
            updateState(CHANNEL_SUNSET_REMAINING_TIME, state);
        }

        if (remainingTimeRelax <= 0 && remainingTimeSunset <= 0) {
            stopRemainingTimer();
        }
    }
}
