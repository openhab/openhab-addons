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
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.somneo.internal.model.AlarmSchedulesData;
import org.openhab.binding.somneo.internal.model.AlarmSettingsData;
import org.openhab.binding.somneo.internal.model.AlarmStateData;
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
import org.openhab.core.library.types.DateTimeType;
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
import org.openhab.core.types.UnDefType;
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

    private final Pattern alarmPattern;

    /**
     * Job to poll data from the device.
     */
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> pollingJobExtended;

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
        this.alarmPattern = Objects.requireNonNull(Pattern.compile(CHANNEL_ALARM_PREFIX_REGEX));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        logger.debug("Handle command '{}' for channel {}", command, channelId);

        try {
            final SomneoHttpConnector connector = this.connector;
            if (connector == null) {
                return;
            }

            final Matcher matcher = alarmPattern.matcher(channelId);
            int alarmPosition = 0;
            if (matcher.matches()) {
                // Replace alarm channel index with string format placeholder to match
                // constants.
                alarmPosition = Integer.parseInt(matcher.group(1));
                channelId = channelId.replace(alarmPosition + "#", "%d#");
            }

            if (command instanceof RefreshType) {
                if (channelId.equals(CHANNEL_ALARM_SNOOZE)) {
                    final State snooze = connector.fetchSnoozeDuration();
                    updateState(CHANNEL_ALARM_SNOOZE, snooze);
                } else if (channelId.startsWith("alarm")) {
                    updateAlarmExtended(alarmPosition);
                } else if (channelId.startsWith("sensor")) {
                    updateSensors();
                } else if (channelId.startsWith("light")) {
                    updateLights();
                } else if (channelId.equals(CHANNEL_RELAX_REMAINING_TIME)
                        || channelId.equals(CHANNEL_SUNSET_REMAINING_TIME)) {
                    updateRemainingTimer();
                } else if (channelId.equals(CHANNEL_AUDIO_FREQUENCY)) {
                    updateFrequency();
                } else if (channelId.startsWith("audio")) {
                    updateLights();
                } else if (channelId.startsWith("sunset")) {
                    updateSunset();
                } else if (channelId.startsWith("relax")) {
                    updateRelax();
                } else {
                    this.poll();
                }
                return;
            }

            switch (channelId) {
                case CHANNEL_AUDIO_AUX:
                    if (command instanceof OnOffType onOff) {
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
                    if (command instanceof PlayPauseType playPause) {
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
                    if (command instanceof PercentType percent) {
                        connector.setAudioVolume(percent.intValue());
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
                    if (command instanceof PercentType percent) {
                        int level = percent.intValue();

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
                    if (command instanceof DecimalType decimal) {
                        connector.setRelaxBreathingRate(decimal.intValue());
                    }
                    break;
                case CHANNEL_RELAX_DURATION:
                    if (command instanceof QuantityType quantity) {
                        connector.setRelaxDuration(quantity.intValue());
                    }
                    break;
                case CHANNEL_RELAX_GUIDANCE_TYPE:
                    if (command instanceof DecimalType decimal) {
                        connector.setRelaxGuidanceType(decimal.intValue());
                    }
                    break;
                case CHANNEL_RELAX_LIGHT_INTENSITY:
                    if (command instanceof PercentType percent) {
                        connector.setRelaxLightIntensity(percent.intValue());
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
                    if (command instanceof PercentType percent) {
                        connector.setRelaxVolume(percent.intValue());
                    }
                    break;
                case CHANNEL_SUNSET_AMBIENT_NOISE:
                    if (command instanceof StringType) {
                        connector.setSunsetAmbientNoise(command.toFullString());
                    }
                    break;
                case CHANNEL_SUNSET_COLOR_SCHEMA:
                    if (command instanceof DecimalType decimal) {
                        connector.setSunsetColorSchema(decimal.intValue());
                    }
                    break;
                case CHANNEL_SUNSET_DURATION:
                    if (command instanceof QuantityType quantity) {
                        connector.setSunsetDuration(quantity.intValue());
                    }
                    break;
                case CHANNEL_SUNSET_LIGHT_INTENSITY:
                    if (command instanceof PercentType percent) {
                        connector.setSunsetLightIntensity(percent.intValue());
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
                    if (command instanceof PercentType percent) {
                        connector.setSunsetVolume(percent.intValue());
                    }
                    break;
                case CHANNEL_ALARM_SNOOZE:
                    if (command instanceof QuantityType quantity) {
                        connector.setAlarmSnooze(quantity.intValue());
                    }
                    break;
                case CHANNEL_ALARM_CONFIGURED:
                    if (alarmPosition > 2) {
                        if (command instanceof OnOffType onOff) {
                            connector.toggleAlarmConfiguration(alarmPosition, onOff);

                            if (OnOffType.ON.equals(command)) {
                                updateAlarmExtended(alarmPosition);
                            } else {
                                resetAlarm(alarmPosition);
                            }
                        }
                    } else {
                        logger.info("Alarm 1 and 2 can not be unset");
                    }
                    break;
                case CHANNEL_ALARM_SWITCH:
                    if (command instanceof OnOffType onOff) {
                        connector.toggleAlarm(alarmPosition, onOff);
                        updateAlarmExtended(alarmPosition);
                    }
                    break;
                case CHANNEL_ALARM_TIME:
                    if (command instanceof DateTimeType decimal) {
                        connector.setAlarmTime(alarmPosition, decimal);
                    }
                    break;
                case CHANNEL_ALARM_REPEAT_DAY:
                    if (command instanceof DecimalType decimal) {
                        connector.setAlarmRepeatDay(alarmPosition, decimal);
                    }
                    break;
                case CHANNEL_ALARM_POWER_WAKE:
                    if (command instanceof OnOffType onOff) {
                        connector.toggleAlarmPowerWake(alarmPosition, onOff);
                        if (OnOffType.OFF.equals(command)) {
                            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_POWER_WAKE_DELAY, alarmPosition),
                                    QuantityType.valueOf(0, Units.MINUTE));
                        }
                    }
                    break;
                case CHANNEL_ALARM_POWER_WAKE_DELAY:
                    if (command instanceof QuantityType quantity) {
                        connector.setAlarmPowerWakeDelay(alarmPosition, quantity);
                        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_POWER_WAKE, alarmPosition), OnOffType.ON);
                    }
                    break;
                case CHANNEL_ALARM_SUNRISE_DURATION:
                    if (command instanceof QuantityType quantity) {
                        connector.setAlarmSunriseDuration(alarmPosition, quantity);
                    }
                    break;
                case CHANNEL_ALARM_SUNRISE_BRIGHTNESS:
                    if (command instanceof PercentType percent) {
                        connector.setAlarmSunriseBrightness(alarmPosition, percent);
                    }
                    break;
                case CHANNEL_ALARM_SUNRISE_SCHEMA:
                    if (command instanceof DecimalType decimal) {
                        connector.setAlarmSunriseSchema(alarmPosition, decimal);
                    }
                    break;
                case CHANNEL_ALARM_SOUND:
                    if (command instanceof StringType) {
                        connector.setAlarmSound(alarmPosition, (StringType) command);
                    }
                    break;
                case CHANNEL_ALARM_VOLUME:
                    if (command instanceof PercentType percent) {
                        connector.setAlarmVolume(alarmPosition, percent);
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

        final SomneoConfiguration config = getConfigAs(SomneoConfiguration.class);
        final int refreshInterval = config.refreshInterval;
        logger.debug("Start default polling job at interval {}s", refreshInterval);
        this.pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, refreshInterval, TimeUnit.SECONDS);

        final int refreshIntervalAlarmExtended = config.refreshIntervalAlarmExtended;
        logger.debug("Start extended alarm polling job at interval {}s", refreshIntervalAlarmExtended);
        this.pollingJobExtended = scheduler.scheduleWithFixedDelay(this::pollAlarmExtended, 0,
                refreshIntervalAlarmExtended, TimeUnit.SECONDS);
    }

    private void stopPolling() {
        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }

        final ScheduledFuture<?> pollingJobExtended = this.pollingJobExtended;
        if (pollingJobExtended != null) {
            pollingJobExtended.cancel(true);
            this.pollingJobExtended = null;
        }

        logger.debug("HTTP polling stopped.");
    }

    private void poll() {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        try {
            updateSensors();

            updateLights();

            updateSunset();

            updateRelax();

            updateAudio();

            updateFrequency();

            updateAlarm();

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

    private void updateAudio() throws TimeoutException, InterruptedException, ExecutionException {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }
        final AudioData audioData = connector.fetchAudioData();
        updateState(CHANNEL_AUDIO_RADIO, audioData.getRadioState());
        updateState(CHANNEL_AUDIO_AUX, audioData.getAuxState());
        updateState(CHANNEL_AUDIO_VOLUME, audioData.getVolumeState());
        updateState(CHANNEL_AUDIO_PRESET, audioData.getPresetState());
    }

    private void updateRelax() throws TimeoutException, InterruptedException, ExecutionException {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }
        final RelaxData relaxData = connector.fetchRelaxData();
        updateState(CHANNEL_RELAX_SWITCH, relaxData.getSwitchState());
        updateState(CHANNEL_RELAX_BREATHING_RATE, relaxData.getBreathingRate());
        updateState(CHANNEL_RELAX_DURATION, relaxData.getDurationInMin());
        updateState(CHANNEL_RELAX_GUIDANCE_TYPE, relaxData.getGuidanceType());
        updateState(CHANNEL_RELAX_LIGHT_INTENSITY, relaxData.getLightIntensity());
        updateState(CHANNEL_RELAX_VOLUME, relaxData.getSoundVolume());
    }

    private void updateSunset() throws TimeoutException, InterruptedException, ExecutionException {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }
        final SunsetData sunsetData = connector.fetchSunsetData();
        updateState(CHANNEL_SUNSET_SWITCH, sunsetData.getSwitchState());
        updateState(CHANNEL_SUNSET_LIGHT_INTENSITY, sunsetData.getLightIntensity());
        updateState(CHANNEL_SUNSET_DURATION, sunsetData.getDurationInMin());
        updateState(CHANNEL_SUNSET_COLOR_SCHEMA, sunsetData.getColorSchema());
        updateState(CHANNEL_SUNSET_AMBIENT_NOISE, sunsetData.getAmbientNoise());
        updateState(CHANNEL_SUNSET_VOLUME, sunsetData.getSoundVolume());
    }

    private void updateLights() throws TimeoutException, InterruptedException, ExecutionException {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }
        final LightData lightData = connector.fetchLightData();
        updateState(CHANNEL_LIGHT_MAIN, lightData.getMainLightState());
        updateState(CHANNEL_LIGHT_NIGHT, lightData.getNightLightState());
        lastLightBrightness = lightData.getMainLightLevel();
    }

    private void updateSensors() throws TimeoutException, InterruptedException, ExecutionException {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        final SensorData sensorData = connector.fetchSensorData();
        updateState(CHANNEL_SENSOR_HUMIDITY, sensorData.getCurrentHumidity());
        updateState(CHANNEL_SENSOR_ILLUMINANCE, sensorData.getCurrentIlluminance());
        updateState(CHANNEL_SENSOR_NOISE, sensorData.getCurrentNoise());
        updateState(CHANNEL_SENSOR_TEMPERATURE, sensorData.getCurrentTemperature());
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

    private void updateAlarm() throws TimeoutException, InterruptedException, ExecutionException {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        final State snooze = connector.fetchSnoozeDuration();
        updateState(CHANNEL_ALARM_SNOOZE, snooze);

        final AlarmStateData alarmState = connector.fetchAlarmStateData();
        final AlarmSchedulesData alarmSchedulesData = connector.fetchAlarmScheduleData();

        for (int i = 1; i <= alarmState.getAlarmCount(); i++) {
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_CONFIGURED, i), alarmState.getConfiguredState(i));

            if (OnOffType.ON.equals(alarmState.getConfiguredState(i))) {
                updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SWITCH, i), alarmState.getEnabledState(i));
                updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_TIME, i),
                        alarmSchedulesData.getAlarmTimeState(i));
                updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_REPEAT_DAY, i),
                        alarmSchedulesData.getRepeatDayState(i));
                updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_POWER_WAKE, i), alarmState.getPowerWakeState(i));
                updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_POWER_WAKE_DELAY, i),
                        alarmState.getPowerWakeDelayState(i, alarmSchedulesData.getAlarmTime(i)));
            } else {
                resetAlarm(i);
            }
        }
    }

    private void pollAlarmExtended() {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        try {
            final AlarmStateData alarmState = connector.fetchAlarmStateData();

            for (int i = 1; i <= alarmState.getAlarmCount(); i++) {
                if (OnOffType.ON.equals(alarmState.getConfiguredState(i))) {
                    updateAlarmExtended(i);
                } else {
                    resetAlarm(i);
                }
            }
        } catch (InterruptedException e) {
            logger.debug("Polling extended alarm data interrupted");
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

    private void updateAlarmExtended(int position) throws TimeoutException, InterruptedException, ExecutionException {
        final SomneoHttpConnector connector = this.connector;
        if (connector == null) {
            return;
        }

        final AlarmSettingsData alarmSettings = connector.fetchAlarmSettingsData(position);

        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_CONFIGURED, position),
                alarmSettings.getConfiguredState());

        if (OnOffType.ON.equals(alarmSettings.getConfiguredState())) {
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SWITCH, position), alarmSettings.getEnabledState());
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_POWER_WAKE, position),
                    alarmSettings.getPowerWakeState());
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_POWER_WAKE_DELAY, position),
                    alarmSettings.getPowerWakeDelayState());
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_TIME, position), alarmSettings.getAlarmTimeState());
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_REPEAT_DAY, position),
                    alarmSettings.getRepeatDayState());
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SUNRISE_DURATION, position),
                    alarmSettings.getSunriseDurationInMin());
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SUNRISE_BRIGHTNESS, position),
                    alarmSettings.getSunriseBrightness());
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SUNRISE_SCHEMA, position),
                    alarmSettings.getSunriseSchema());
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SOUND, position), alarmSettings.getSound());
            updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_VOLUME, position), alarmSettings.getSoundVolume());
        } else {
            resetAlarm(position);
        }
    }

    private void resetAlarm(int position) throws TimeoutException, InterruptedException, ExecutionException {
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SWITCH, position), UnDefType.UNDEF);
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_POWER_WAKE, position), UnDefType.UNDEF);
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_POWER_WAKE_DELAY, position), UnDefType.UNDEF);
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_TIME, position), UnDefType.UNDEF);
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_REPEAT_DAY, position), UnDefType.UNDEF);
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SUNRISE_DURATION, position), UnDefType.UNDEF);
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SUNRISE_BRIGHTNESS, position), UnDefType.UNDEF);
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SUNRISE_SCHEMA, position), UnDefType.UNDEF);
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_SOUND, position), UnDefType.UNDEF);
        updateState(formatAlarmChannelIdByIndex(CHANNEL_ALARM_VOLUME, position), UnDefType.UNDEF);
    }

    private String formatAlarmChannelIdByIndex(String channelId, int index) {
        final String channelIdFormated = String.format(channelId, index);
        if (channelIdFormated == null) {
            return "";
        }
        return channelIdFormated;
    }
}
