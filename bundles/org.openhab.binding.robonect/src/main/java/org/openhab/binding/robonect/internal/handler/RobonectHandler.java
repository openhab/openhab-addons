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
package org.openhab.binding.robonect.internal.handler;

import static org.openhab.binding.robonect.internal.RobonectBindingConstants.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
import org.openhab.binding.robonect.internal.RobonectEndpoint;
import org.openhab.binding.robonect.internal.config.JobChannelConfig;
import org.openhab.binding.robonect.internal.config.RobonectConfig;
import org.openhab.binding.robonect.internal.model.Address;
import org.openhab.binding.robonect.internal.model.Battery;
import org.openhab.binding.robonect.internal.model.BatteryCapacity;
import org.openhab.binding.robonect.internal.model.BatteryInfo;
import org.openhab.binding.robonect.internal.model.BladeMotor;
import org.openhab.binding.robonect.internal.model.DriveMotor;
import org.openhab.binding.robonect.internal.model.DriveMotors;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.Host;
import org.openhab.binding.robonect.internal.model.HourGeneral;
import org.openhab.binding.robonect.internal.model.HourInfo;
import org.openhab.binding.robonect.internal.model.MotorInfo;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.PortalInfo;
import org.openhab.binding.robonect.internal.model.Remote;
import org.openhab.binding.robonect.internal.model.RemoteInfo;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Sensor;
import org.openhab.binding.robonect.internal.model.TimerFull;
import org.openhab.binding.robonect.internal.model.TimerInfo;
import org.openhab.binding.robonect.internal.model.TimersInfo;
import org.openhab.binding.robonect.internal.model.VersionInfo;
import org.openhab.binding.robonect.internal.model.Weekdays;
import org.openhab.binding.robonect.internal.model.WireInfo;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.util.ThingWebClientUtil;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link RobonectHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * The channels are periodically updated by polling the mower via HTTP in a separate thread.
 *
 * @author Marco Meyer - Initial contribution
 */
public class RobonectHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RobonectHandler.class);

    private ScheduledFuture<?> pollingJob;

    private HttpClient httpClient;
    private TimeZoneProvider timeZoneProvider;

    private ZoneId timeZone;

    private RobonectClient robonectClient;

    public RobonectHandler(Thing thing, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider) {
        super(thing);
        httpClient = httpClientFactory
                .createHttpClient(ThingWebClientUtil.buildWebClientConsumerName(thing.getUID(), null));
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                refreshChannels(channelUID);
            } else {
                sendCommand(channelUID, command);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RobonectCommunicationException rce) {
            logger.debug("Failed to communicate with the mower. Taking it offline.", rce);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, rce.getMessage());
        }
    }

    private void sendCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_MOWER_NAME:
                if (command instanceof StringType stringCommand) {
                    updateName(stringCommand);
                } else {
                    logger.debug("Got name update of type {} but StringType is expected.",
                            command.getClass().getName());
                }
                break;

            case CHANNEL_STATUS_MODE:
                if (command instanceof StringType) {
                    setMowerMode(command);
                } else {
                    logger.debug("Got job remote start update of type {} but StringType is expected.",
                            command.getClass().getName());
                }
                break;

            case CHANNEL_MOWER_START:
                if (command instanceof OnOffType onOffCommand) {
                    handleStartStop(onOffCommand);
                } else {
                    logger.debug("Got stopped update of type {} but OnOffType is expected.",
                            command.getClass().getName());
                }
                break;

            case CHANNEL_JOB:
                if (command instanceof OnOffType) {
                    handleJobCommand(channelUID, command);
                } else {
                    logger.debug("Got job update of type {} but OnOffType is expected.", command.getClass().getName());
                }
                break;

            case CHANNEL_RESET_ERROR:
                if (command == OnOffType.ON) {
                    resetErrors();
                } else {
                    logger.debug("Got error reset command {} but On is expected.", command);
                }
                break;
            default:
                if (channelUID.getId().startsWith("timer-")) {
                    updateTimer(channelUID.getId(), command);
                }
                break;
        }
    }

    /** Updates a single setting for one of the fourteen configurable timers. */
    private void updateTimer(String channelId, Command command) {
        String[] parts = channelId.split("-");
        if (parts.length != 3)
            return;
        try {
            int timer = Integer.parseInt(parts[1]);
            String setting = parts[2];
            String value;
            String parameter;
            if (command instanceof OnOffType onOff && switch (setting) {
                case "enabled", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" -> true;
                default -> false;
            }) {
                parameter = switch (setting) {
                    case "enabled" -> "enable";
                    case "monday" -> "mo";
                    case "tuesday" -> "tu";
                    case "wednesday" -> "we";
                    case "thursday" -> "th";
                    case "friday" -> "fr";
                    case "saturday" -> "sa";
                    default -> "su";
                };
                value = onOff == OnOffType.ON ? "1" : "0";
            } else if (command instanceof DecimalType decimal && (setting.equals("start") || setting.equals("end"))) {
                int minutes = decimal.intValue();
                if (minutes < 0 || minutes >= 24 * 60) {
                    logger.debug("Ignoring invalid timer value {}", minutes);
                    return;
                }
                parameter = setting;
                value = "%02d:%02d".formatted(minutes / 60, minutes % 60);
            } else {
                return;
            }
            TimerInfo result = robonectClient.updateTimer(timer, parameter, value);
            if (result.isSuccessful() && result.getTimer() != null)
                updateTimerState(result.getTimer());
            else
                refreshTimersInfo();
        } catch (NumberFormatException e) {
            logger.debug("Ignoring malformed timer channel {}", channelId);
        }
    }

    /** Updates timer channels from a complete timer response. */
    private void updateTimerState(TimerFull timer) {
        if (timer.getId() == null || timer.getId() < 0 || timer.getId() >= 14)
            return;
        String prefix = "timer-" + (timer.getId() + 1) + "-";
        if (timer.getEnabled() != null)
            updateState(prefix + "enabled", OnOffType.from(timer.getEnabled()));
        updateTimerTime(prefix + "start", timer.getStart());
        updateTimerTime(prefix + "end", timer.getEnd());
        Weekdays days = timer.getWeekdays();
        if (days == null)
            return;
        updateTimerDay(prefix, "monday", days.getMo());
        updateTimerDay(prefix, "tuesday", days.getTu());
        updateTimerDay(prefix, "wednesday", days.getWe());
        updateTimerDay(prefix, "thursday", days.getTh());
        updateTimerDay(prefix, "friday", days.getFr());
        updateTimerDay(prefix, "saturday", days.getSa());
        updateTimerDay(prefix, "sunday", days.getSu());
    }

    private void updateTimerTime(String channel, String time) {
        if (time != null && time.matches("\\d{2}:\\d{2}"))
            updateState(channel, new DecimalType(
                    Integer.parseInt(time.substring(0, 2)) * 60 + Integer.parseInt(time.substring(3, 5))));
    }

    private void updateTimerDay(String prefix, String day, Boolean value) {
        if (value != null)
            updateState(prefix + day, OnOffType.from(value));
    }

    /** Refreshes all editable timer configurations. */
    private void refreshTimersInfo() {
        TimersInfo info = robonectClient.getTimersInfo();
        if (info.isSuccessful() && info.getTimers() != null)
            info.getTimers().forEach(this::updateTimerState);
    }

    private void handleJobCommand(ChannelUID channelUID, Command command) {
        JobChannelConfig jobConfig = getThing().getChannel(channelUID.getId()).getConfiguration()
                .as(JobChannelConfig.class);
        if (command == OnOffType.ON) {
            robonectClient.startJob(new RobonectClient.JobSettings()
                    .withAfterMode(ModeCommand.Mode.valueOf(jobConfig.getAfterMode()))
                    .withRemoteStart(ModeCommand.RemoteStart.valueOf(jobConfig.getRemoteStart()))
                    .withDuration(jobConfig.getDuration()).withStart(jobConfig.getStart()).withEnd(jobConfig.getEnd()));
        } else if (command == OnOffType.OFF) {
            robonectClient.stopJob(
                    new RobonectClient.JobSettings().withAfterMode(ModeCommand.Mode.valueOf(jobConfig.getAfterMode())));
        }
    }

    private void refreshChannels(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_MOWER_NAME:
            case CHANNEL_STATUS_BATTERY:
            case CHANNEL_STATUS:
            case CHANNEL_STATUS_DURATION:
            case CHANNEL_STATUS_HOURS:
            case CHANNEL_STATUS_MODE:
            case CHANNEL_MOWER_START:
            case CHANNEL_TIMER_NEXT_TIMER:
            case CHANNEL_TIMER_STATUS:
            case CHANNEL_WLAN_SIGNAL:
            case CHANNEL_JOB:
                refreshMowerInfo();
                break;
            default:
            case CHANNEL_LAST_ERROR_CODE:
            case CHANNEL_LAST_ERROR_DATE:
            case CHANNEL_LAST_ERROR_MESSAGE:
                refreshLastErrorInfo();
                break;
        }
    }

    private void setMowerMode(Command command) {
        String modeStr = command.toFullString();
        ModeCommand.Mode newMode = ModeCommand.Mode.valueOf(modeStr.toUpperCase());
        if (robonectClient.setMode(newMode).isSuccessful()) {
            updateState(CHANNEL_STATUS_MODE, new StringType(newMode.name()));
        } else {
            refreshMowerInfo();
        }
    }

    private void logErrorFromResponse(RobonectAnswer result) {
        if (!result.isSuccessful()) {
            logger.debug("Could not send EOD Trigger. Robonect error message: {}", result.getErrorMessage());
        }
    }

    private void resetErrors() {
        RobonectAnswer answer = robonectClient.resetErrors();
        if (answer.isSuccessful()) {
            refreshMowerInfo();
        } else {
            logger.debug("Could not reset mower errors. Robonect error message: {}", answer.getErrorMessage());
        }
    }

    private void handleStartStop(final OnOffType command) {
        RobonectAnswer answer = command == OnOffType.ON ? robonectClient.start() : robonectClient.stop();
        if (answer.isSuccessful()) {
            updateState(CHANNEL_MOWER_START, command);
        } else {
            logErrorFromResponse(answer);
            refreshMowerInfo();
        }
    }

    private void updateName(StringType command) {
        String newName = command.toFullString();
        Name name = robonectClient.setName(newName);
        if (name.isSuccessful()) {
            updateState(CHANNEL_MOWER_NAME, new StringType(name.getName()));
        } else {
            logErrorFromResponse(name);
            refreshMowerInfo();
        }
    }

    private void refreshMowerInfo() {
        MowerInfo info = robonectClient.getMowerInfo();
        if (info.isSuccessful()) {
            if (info.getError() != null) {
                updateErrorInfo(info.getError());
                refreshLastErrorInfo();
            } else {
                clearErrorInfo();
            }
            updateState(CHANNEL_MOWER_NAME, new StringType(info.getName()));
            if (info.getId() != null) {
                updateState(CHANNEL_MOWER_ID, new StringType(info.getId()));
            }
            updateState(CHANNEL_STATUS_BATTERY, new DecimalType(info.getStatus().getBattery()));
            updateState(CHANNEL_STATUS, new DecimalType(info.getStatus().getStatus().getStatusCode()));
            updateState(CHANNEL_STATUS_DURATION, new QuantityType<>(info.getStatus().getDuration(), Units.SECOND));
            updateState(CHANNEL_STATUS_DISTANCE, new QuantityType<>(info.getStatus().getDistance(), SIUnits.METRE));
            updateState(CHANNEL_STATUS_HOURS, new QuantityType<>(info.getStatus().getHours(), Units.HOUR));
            updateState(CHANNEL_STATUS_MODE, new StringType(info.getStatus().getMode().name()));
            updateState(CHANNEL_MOWER_START, OnOffType.from(!info.getStatus().isStopped()));
            if (info.getHealth() != null) {
                updateState(CHANNEL_HEALTH_TEMP,
                        new QuantityType<>(info.getHealth().getTemperature(), SIUnits.CELSIUS));
                updateState(CHANNEL_HEALTH_HUM, new QuantityType<>(info.getHealth().getHumidity(), Units.PERCENT));
            }
            if (info.getBlades() != null) {
                updateState(CHANNEL_BLADES_QUALITY, new QuantityType<>(info.getBlades().getQuality(), Units.PERCENT));
                updateState(CHANNEL_BLADES_REPL_DAYS, new QuantityType<>(info.getBlades().getDays(), Units.DAY));
                updateState(CHANNEL_BLADES_USAGE_HOURS, new QuantityType<>(info.getBlades().getHours(), Units.HOUR));
            }
            if (info.getClock() != null && info.getClock().getUnix() != null) {
                updateState(CHANNEL_TIMESTAMP, convertUnixToDateTimeType(info.getClock().getUnix()));
            }
            if (info.getTimer() != null) {
                if (info.getTimer().getNext() != null) {
                    updateNextTimer(info);
                }
                updateState(CHANNEL_TIMER_STATUS, new StringType(info.getTimer().getStatus().name()));
            }
            if (info.getWlan() != null && info.getWlan().getSignal() != null) {
                int signal = info.getWlan().getSignal();
                updateState(CHANNEL_WLAN_SIGNAL_STRENGTH, new DecimalType(signal));
                updateState(CHANNEL_WLAN_SIGNAL, new DecimalType(convertSignalStrength(signal)));
            } else {
                updateState(CHANNEL_WLAN_SIGNAL, UnDefType.UNDEF);
                updateState(CHANNEL_WLAN_SIGNAL_STRENGTH, UnDefType.UNDEF);
            }
            updateState(CHANNEL_JOB, OnOffType.from(robonectClient.isJobRunning()));
        } else {
            logger.error("Could not retrieve mower info. Robonect error response message: {}", info.getErrorMessage());
        }
    }

    /**
     * Updates detailed battery channels when the mower supports the battery endpoint.
     */
    private void refreshBatteryInfo() {
        BatteryInfo info = robonectClient.getBatteryInfo();
        List<Battery> batteries = info.getBatteries();
        if (!info.isSuccessful() || batteries == null || batteries.isEmpty()) {
            logger.debug("Could not retrieve detailed battery information: {}", info.getErrorMessage());
            return;
        }
        Battery battery = batteries.getFirst();
        Integer voltage = battery.getVoltage();
        if (voltage != null) {
            updateState(CHANNEL_BATTERY_VOLTAGE, new QuantityType<>(voltage / 1000.0, Units.VOLT));
        }
        Integer current = battery.getCurrent();
        if (current != null) {
            updateState(CHANNEL_BATTERY_CURRENT, new QuantityType<>(current / 1000.0, Units.AMPERE));
        }
        Integer temperature = battery.getTemperature();
        if (temperature != null) {
            updateState(CHANNEL_BATTERY_TEMPERATURE, new QuantityType<>(temperature / 10.0, SIUnits.CELSIUS));
        }
        BatteryCapacity capacity = battery.getCapacity();
        if (capacity != null) {
            if (capacity.getFull() != null) {
                updateState(CHANNEL_BATTERY_CAPACITY, new QuantityType<>(capacity.getFull(), Units.MILLIAMPERE_HOUR));
            }
            if (capacity.getRemaining() != null) {
                updateState(CHANNEL_BATTERY_REMAINING_CAPACITY,
                        new QuantityType<>(capacity.getRemaining(), Units.MILLIAMPERE_HOUR));
            }
        }
    }

    /**
     * Updates motor channels when the mower supports the motor endpoint.
     */
    private void refreshMotorInfo() {
        MotorInfo info = robonectClient.getMotorInfo();
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve motor information: {}", info.getErrorMessage());
            return;
        }
        DriveMotors drive = info.getDrive();
        if (drive != null) {
            updateDriveMotor(CHANNEL_LEFT_DRIVE_POWER, CHANNEL_LEFT_DRIVE_SPEED, CHANNEL_LEFT_DRIVE_CURRENT,
                    drive.getLeft());
            updateDriveMotor(CHANNEL_RIGHT_DRIVE_POWER, CHANNEL_RIGHT_DRIVE_SPEED, CHANNEL_RIGHT_DRIVE_CURRENT,
                    drive.getRight());
        }
        BladeMotor blade = info.getBlade();
        if (blade != null) {
            if (blade.getAverage() != null) {
                updateState(CHANNEL_BLADE_MOTOR_AVERAGE, new DecimalType(blade.getAverage()));
            }
            if (blade.getSpeed() != null) {
                updateState(CHANNEL_BLADE_MOTOR_SPEED, new DecimalType(blade.getSpeed()));
            }
            if (blade.getCurrent() != null) {
                updateState(CHANNEL_BLADE_MOTOR_CURRENT, new QuantityType<>(blade.getCurrent() / 1000.0, Units.AMPERE));
            }
        }
    }

    /**
     * Updates aggregate operating statistics when the mower supports the hour endpoint.
     */
    private void refreshHourInfo() {
        HourInfo info = robonectClient.getHourInfo();
        HourGeneral general = info.getGeneral();
        if (!info.isSuccessful() || general == null) {
            logger.debug("Could not retrieve operating statistics: {}", info.getErrorMessage());
            return;
        }
        updateHours(CHANNEL_STATISTICS_RUNNING_HOURS, general.getRun());
        updateHours(CHANNEL_STATISTICS_MOWING_HOURS, general.getMow());
        updateHours(CHANNEL_STATISTICS_SEARCHING_HOURS, general.getSearch());
        updateHours(CHANNEL_STATISTICS_CHARGING_HOURS, general.getCharge());
        if (general.getCharges() != null) {
            updateState(CHANNEL_STATISTICS_CHARGES, new DecimalType(general.getCharges()));
        }
        if (general.getErrors() != null) {
            updateState(CHANNEL_STATISTICS_ERRORS, new DecimalType(general.getErrors()));
        }
    }

    /**
     * Updates the five remote-start point configurations when available.
     */
    private void refreshRemoteInfo() {
        RemoteInfo info = robonectClient.getRemoteInfo();
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve remote-start information: {}", info.getErrorMessage());
            return;
        }
        updateRemoteStart(1, info.getRemoteStart1());
        updateRemoteStart(2, info.getRemoteStart2());
        updateRemoteStart(3, info.getRemoteStart3());
        updateRemoteStart(4, info.getRemoteStart4());
        updateRemoteStart(5, info.getRemoteStart5());
    }

    /**
     * Updates the boundary-wire sensor measurements exposed by the mower.
     */
    private void refreshWireInfo() {
        WireInfo info = robonectClient.getWireInfo();
        List<Sensor> sensors = info.getSensors();
        if (!info.isSuccessful() || sensors == null) {
            logger.debug("Could not retrieve boundary-wire information: {}", info.getErrorMessage());
            return;
        }
        for (int index = 0; index < Math.min(4, sensors.size()); index++) {
            updateWireSensor(index + 1, sensors.get(index));
        }
    }

    /**
     * Updates remote portal status when supported by the module.
     */
    private void refreshPortalInfo() {
        PortalInfo info = robonectClient.getPortalInfo();
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve portal information: {}", info.getErrorMessage());
            return;
        }
        if (info.getEnabled() != null)
            updateState(CHANNEL_PORTAL_ENABLED, OnOffType.from(info.getEnabled()));
        if (info.getConnected() != null)
            updateState(CHANNEL_PORTAL_CONNECTED, OnOffType.from(info.getConnected()));
        Address address = info.getAddress();
        if (address != null) {
            if (address.getUrl() != null)
                updateState(CHANNEL_PORTAL_URL, new StringType(address.getUrl()));
            Host host = address.getHost();
            if (host != null && host.getFriendly() != null)
                updateState(CHANNEL_PORTAL_HOST, new StringType(host.getFriendly()));
        }
    }

    private void updateWireSensor(int number, Sensor sensor) {
        String prefix = "wire-sensor-" + number + "-";
        if (sensor.getDescription() != null)
            updateState(prefix + "label", new StringType(sensor.getDescription()));
        if (sensor.getQuality() != null)
            updateState(prefix + "quality", new QuantityType<>(sensor.getQuality(), Units.PERCENT));
        if (sensor.getA() != null)
            updateState(prefix + "a", new DecimalType(sensor.getA()));
        if (sensor.getN() != null)
            updateState(prefix + "n", new DecimalType(sensor.getN()));
        if (sensor.getF() != null)
            updateState(prefix + "f", new DecimalType(sensor.getF()));
        if (sensor.getG1() != null)
            updateState(prefix + "g1", new DecimalType(sensor.getG1()));
    }

    private void updateRemoteStart(int number, @Nullable Remote remote) {
        if (remote == null) {
            return;
        }
        String prefix = "remote-" + number + "-";
        if (remote.getName() != null)
            updateState(prefix + "name", new StringType(remote.getName()));
        if (remote.getVisible() != null)
            updateState(prefix + "visible", OnOffType.from(remote.getVisible()));
        if (remote.getPath() != null)
            updateState(prefix + "path", new DecimalType(remote.getPath()));
        if (remote.getProportion() != null)
            updateState(prefix + "proportion", new QuantityType<>(remote.getProportion(), Units.PERCENT));
        if (remote.getDistance() != null)
            updateState(prefix + "distance", new QuantityType<>(remote.getDistance(), SIUnits.METRE));
        if (remote.getCorridorcut() != null)
            updateState(prefix + "corridor-cut", OnOffType.from(remote.getCorridorcut()));
    }

    private void updateHours(String channel, Integer value) {
        if (value != null) {
            updateState(channel, new QuantityType<>(value, Units.HOUR));
        }
    }

    /**
     * Updates the channels for one drive motor.
     *
     * @param powerChannel the channel for motor power
     * @param speedChannel the channel for motor speed
     * @param currentChannel the channel for motor current
     * @param motor the API result for the motor
     */
    private void updateDriveMotor(String powerChannel, String speedChannel, String currentChannel,
            @Nullable DriveMotor motor) {
        if (motor == null) {
            return;
        }
        if (motor.getPower() != null) {
            updateState(powerChannel, new QuantityType<>(motor.getPower(), Units.PERCENT));
        }
        if (motor.getSpeed() != null) {
            updateState(speedChannel, new QuantityType<>(motor.getSpeed() / 100.0, Units.METRE_PER_SECOND));
        }
        if (motor.getCurrent() != null) {
            updateState(currentChannel, new QuantityType<>(motor.getCurrent() / 1000.0, Units.AMPERE));
        }
    }

    private void clearErrorInfo() {
        updateState(CHANNEL_ERROR_DATE, UnDefType.UNDEF);
        updateState(CHANNEL_ERROR_CODE, UnDefType.UNDEF);
        updateState(CHANNEL_ERROR_MESSAGE, UnDefType.UNDEF);
    }

    private int convertSignalStrength(int signal) {
        if (signal >= -70) {
            return 4;
        }
        if (signal >= -78) {
            return 3;
        }
        if (signal >= -87) {
            return 2;
        }
        return signal >= -120 ? 1 : 0;
    }

    private void updateErrorInfo(ErrorEntry error) {
        if (error.getErrorMessage() != null) {
            updateState(CHANNEL_ERROR_MESSAGE, new StringType(error.getErrorMessage()));
        }
        if (error.getErrorCode() != null) {
            updateState(CHANNEL_ERROR_CODE, new DecimalType(error.getErrorCode().intValue()));
        }
        if (error.getDate() != null) {
            State dateTime = convertUnixToDateTimeType(error.getUnix());
            updateState(CHANNEL_ERROR_DATE, dateTime);
        }
    }

    private void updateNextTimer(MowerInfo info) {
        State dateTime = convertUnixToDateTimeType(info.getTimer().getNext().getUnix());
        updateState(CHANNEL_TIMER_NEXT_TIMER, dateTime);
    }

    private State convertUnixToDateTimeType(String unixTimeSec) {
        // the value in unixTimeSec represents the time on the robot in its configured timezone. However, it does not
        // provide which zone this is. Thus we have to add the zone information from the Thing configuration in order to
        // provide correct results.
        Instant rawInstant = Instant.ofEpochMilli(Long.valueOf(unixTimeSec) * 1000);

        ZoneId timeZoneOfThing = timeZone;
        if (timeZoneOfThing == null) {
            timeZoneOfThing = timeZoneProvider.getTimeZone();
        }
        ZoneOffset offsetToConfiguredZone = timeZoneOfThing.getRules().getOffset(rawInstant);
        long adjustedTime = rawInstant.getEpochSecond() - offsetToConfiguredZone.getTotalSeconds();
        Instant adjustedInstant = Instant.ofEpochMilli(adjustedTime * 1000);

        return new DateTimeType(adjustedInstant);
    }

    private void refreshVersionInfo() {
        VersionInfo info = robonectClient.getVersionInfo();
        if (info.isSuccessful()) {
            Map<String, String> properties = editProperties();
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, info.getRobonect().getSerial());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, info.getRobonect().getVersion());
            properties.put(PROPERTY_COMPILED, info.getRobonect().getCompiled());
            properties.put(PROPERTY_COMMENT, info.getRobonect().getComment());
            updateProperties(properties);
        } else {
            logger.debug("Could not retrieve mower version info. Robonect error response message: {}",
                    info.getErrorMessage());
        }
    }

    private void refreshLastErrorInfo() {
        ErrorList errorList = robonectClient.errorList();
        if (errorList.isSuccessful()) {
            List<ErrorEntry> errors = errorList.getErrors();
            if (errors != null && !errors.isEmpty()) {
                ErrorEntry lastErrorEntry = errors.get(0);
                updateLastErrorChannels(lastErrorEntry);
            }
        } else {
            logger.debug("Could not retrieve mower error list. Robonect error response message: {}",
                    errorList.getErrorMessage());
        }
    }

    private void updateLastErrorChannels(ErrorEntry error) {
        if (error.getErrorMessage() != null) {
            updateState(CHANNEL_LAST_ERROR_MESSAGE, new StringType(error.getErrorMessage()));
        }
        if (error.getErrorCode() != null) {
            updateState(CHANNEL_LAST_ERROR_CODE, new DecimalType(error.getErrorCode().intValue()));
        }
        if (error.getDate() != null) {
            State dateTime = convertUnixToDateTimeType(error.getUnix());
            updateState(CHANNEL_LAST_ERROR_DATE, dateTime);
        }
    }

    @Override
    public void initialize() {
        RobonectConfig robonectConfig = getConfigAs(RobonectConfig.class);
        RobonectEndpoint endpoint = new RobonectEndpoint(robonectConfig.getHost(), robonectConfig.getUser(),
                robonectConfig.getPassword());

        String timeZoneString = robonectConfig.getTimezone();
        try {
            if (timeZoneString != null) {
                timeZone = ZoneId.of(timeZoneString);
            } else {
                logger.warn("No timezone provided, falling back to the default timezone configured in openHAB: '{}'",
                        timeZoneProvider.getTimeZone());
            }
        } catch (DateTimeException e) {
            logger.warn("Error setting timezone '{}', falling back to the default timezone configured in openHAB: '{}'",
                    timeZoneString, timeZoneProvider.getTimeZone(), e);
        }

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("Exception while trying to start HTTP client", e);
            throw new IllegalStateException("Could not create HttpClient");
        }

        robonectClient = new RobonectClient(httpClient, endpoint);
        Runnable runnable = new MowerChannelPoller(TimeUnit.SECONDS.toMillis(robonectConfig.getOfflineTimeout()),
                robonectConfig.isDetailedInfo(), robonectConfig.getDetailedInfoInterval());
        int pollInterval = robonectConfig.getPollInterval();
        pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, pollInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }

        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("Exception while trying to stop HTTP client", e);
        }
    }

    /**
     * method to inject the robonect client to be used in test cases to allow mocking.
     *
     * @param robonectClient
     */
    protected void setRobonectClient(RobonectClient robonectClient) {
        this.robonectClient = robonectClient;
    }

    private class MowerChannelPoller implements Runnable {

        private long offlineSince;
        private long offlineTriggerDelay;
        private boolean offlineTimeoutTriggered;
        private boolean loadVersionInfo = true;
        private final boolean detailedInfo;
        private final int detailedInfoInterval;
        private int detailedInfoPollCount;

        public MowerChannelPoller(long offlineTriggerDelay, boolean detailedInfo, int detailedInfoInterval) {
            offlineSince = -1;
            this.offlineTriggerDelay = offlineTriggerDelay;
            offlineTimeoutTriggered = false;
            this.detailedInfo = detailedInfo;
            this.detailedInfoInterval = Math.max(1, detailedInfoInterval);
        }

        @Override
        public void run() {
            try {
                if (loadVersionInfo) {
                    refreshVersionInfo();
                    loadVersionInfo = false;
                }
                refreshMowerInfo();
                if (detailedInfo && detailedInfoPollCount++ % detailedInfoInterval == 0) {
                    refreshBatteryInfo();
                    refreshMotorInfo();
                    refreshHourInfo();
                    refreshRemoteInfo();
                    refreshWireInfo();
                    refreshPortalInfo();
                    refreshTimersInfo();
                }
                updateStatus(ThingStatus.ONLINE);
                offlineSince = -1;
                offlineTimeoutTriggered = false;
            } catch (RobonectCommunicationException rce) {
                if (offlineSince < 0) {
                    offlineSince = System.currentTimeMillis();
                }
                if (!offlineTimeoutTriggered && System.currentTimeMillis() - offlineSince > offlineTriggerDelay) {
                    // trigger offline
                    updateState(CHANNEL_MOWER_STATUS_OFFLINE_TRIGGER, new StringType("OFFLINE_TIMEOUT"));
                    offlineTimeoutTriggered = true;
                }
                logger.debug("Failed to communicate with the mower. Taking it offline.", rce);
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, rce.getMessage());
                loadVersionInfo = true;
            } catch (JsonSyntaxException jse) {
                // the module sporadically sends invalid json responses. As this is usually recovered with the
                // next poll interval, we just log it to debug here.
                logger.debug("Failed to parse response.", jse);
            }
        }
    }
}
