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

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
import org.openhab.binding.robonect.internal.RobonectEndpoint;
import org.openhab.binding.robonect.internal.RobonectPushServlet;
import org.openhab.binding.robonect.internal.config.JobChannelConfig;
import org.openhab.binding.robonect.internal.config.RobonectConfig;
import org.openhab.binding.robonect.internal.model.AccessPoint;
import org.openhab.binding.robonect.internal.model.Address;
import org.openhab.binding.robonect.internal.model.AjaxInfo;
import org.openhab.binding.robonect.internal.model.Battery;
import org.openhab.binding.robonect.internal.model.BatteryCapacity;
import org.openhab.binding.robonect.internal.model.BatteryInfo;
import org.openhab.binding.robonect.internal.model.BladeMotor;
import org.openhab.binding.robonect.internal.model.Door;
import org.openhab.binding.robonect.internal.model.DoorInfo;
import org.openhab.binding.robonect.internal.model.DoorState;
import org.openhab.binding.robonect.internal.model.DriveMotor;
import org.openhab.binding.robonect.internal.model.DriveMotors;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.ExtInfo;
import org.openhab.binding.robonect.internal.model.Extension;
import org.openhab.binding.robonect.internal.model.Extensions;
import org.openhab.binding.robonect.internal.model.Host;
import org.openhab.binding.robonect.internal.model.HourGeneral;
import org.openhab.binding.robonect.internal.model.HourInfo;
import org.openhab.binding.robonect.internal.model.MotorInfo;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.PortalInfo;
import org.openhab.binding.robonect.internal.model.Push;
import org.openhab.binding.robonect.internal.model.PushInfo;
import org.openhab.binding.robonect.internal.model.PushServer;
import org.openhab.binding.robonect.internal.model.PushTriggers;
import org.openhab.binding.robonect.internal.model.Remote;
import org.openhab.binding.robonect.internal.model.RemoteInfo;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Sensor;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.TimerFull;
import org.openhab.binding.robonect.internal.model.TimerInfo;
import org.openhab.binding.robonect.internal.model.TimersInfo;
import org.openhab.binding.robonect.internal.model.VersionInfo;
import org.openhab.binding.robonect.internal.model.Weather;
import org.openhab.binding.robonect.internal.model.WeatherConfig;
import org.openhab.binding.robonect.internal.model.WeatherInfo;
import org.openhab.binding.robonect.internal.model.WeatherService;
import org.openhab.binding.robonect.internal.model.Weekdays;
import org.openhab.binding.robonect.internal.model.WifiStation;
import org.openhab.binding.robonect.internal.model.WireInfo;
import org.openhab.binding.robonect.internal.model.WlanInfo;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand.Operation;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
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
 * @author Nico Brüttner - Ajax UI compatibility
 * @author Nico Brüttner - Dynamic telemetry and push trigger channels
 */
public class RobonectHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RobonectHandler.class);

    private ScheduledFuture<?> pollingJob;

    private HttpClient httpClient;
    private TimeZoneProvider timeZoneProvider;

    private ZoneId timeZone;

    private RobonectClient robonectClient;
    private final @Nullable RobonectPushServlet pushServlet;
    private boolean ajaxUiScraping;

    private static final Set<String> JOB_TIMER_LABELS = Set.of("Auftrag", "Job", "Travail", "Ordine", "Jobb");
    private static final Set<String> END_OF_DAY_LABELS = Set.of("Feierabend", "End of day", "Fin de journée de travail",
            "Fine lavoro giornata", "Slutet av dagen");
    private static final Pattern JOB_TIME_PATTERN = Pattern.compile("^(?:(\\d{1,2}:\\d{2})\\D+)?(\\d{1,2}:\\d{2})$");
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2})$");
    private static final Pattern BATTERY_CHANNEL_PATTERN = Pattern.compile("battery-\\d+-.+");
    private static final Pattern TIMER_CHANNEL_PATTERN = Pattern.compile("timer-\\d+-.+");
    private static final Pattern WIRE_CHANNEL_PATTERN = Pattern.compile("wire-sensor-\\d+-.+");
    private static final Pattern PUSH_TRIGGER_CHANNEL_PATTERN = Pattern
            .compile("push-trigger-(\\d+)-(name|enter|leave)");
    private static final Pattern REMOTE_CHANNEL_PATTERN = Pattern.compile("remote-([1-5])-(name|distance|proportion)");

    public RobonectHandler(Thing thing, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider) {
        this(thing, httpClientFactory, timeZoneProvider, null);
    }

    public RobonectHandler(Thing thing, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider,
            @Nullable RobonectPushServlet pushServlet) {
        super(thing);
        httpClient = httpClientFactory
                .createHttpClient(ThingWebClientUtil.buildWebClientConsumerName(thing.getUID(), null));
        this.timeZoneProvider = timeZoneProvider;
        this.pushServlet = pushServlet;
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
            case CHANNEL_SERVICE_REBOOT:
                performServiceOperation(command, Operation.REBOOT);
                break;
            case CHANNEL_SERVICE_SHUTDOWN:
                performServiceOperation(command, Operation.SHUTDOWN);
                break;
            case CHANNEL_SERVICE_SLEEP:
                performServiceOperation(command, Operation.SLEEP);
                break;
            case CHANNEL_DOOR_STATE:
                handleDoorCommand(command);
                break;
            case CHANNEL_WEATHER_ENABLED:
            case CHANNEL_WEATHER_INTERVAL:
            case CHANNEL_WEATHER_MAX_RAIN:
            case CHANNEL_WEATHER_MIN_TEMPERATURE:
            case CHANNEL_WEATHER_MAX_TEMPERATURE:
            case CHANNEL_WEATHER_MIN_HUMIDITY:
            case CHANNEL_WEATHER_MAX_HUMIDITY:
            case CHANNEL_WEATHER_SKIP_DAY:
            case CHANNEL_WEATHER_SKIP_NIGHT:
                updateWeather(channelUID.getId(), command);
                break;
            case CHANNEL_PUSH_URL:
            case CHANNEL_PUSH_USERNAME:
            case CHANNEL_PUSH_INTERVAL:
                updatePush(channelUID.getId(), command);
                break;
            default:
                if (REMOTE_CHANNEL_PATTERN.matcher(channelUID.getId()).matches()) {
                    updateRemote(channelUID.getId(), command);
                } else if (channelUID.getId().startsWith("timer-")) {
                    updateTimer(channelUID.getId(), command);
                } else if (channelUID.getId().startsWith("push-trigger-")) {
                    updatePushTrigger(channelUID.getId(), command);
                }
                break;
        }
    }

    /** Sends a remote-start point setting and refreshes all values returned by the API. */
    private void updateRemote(String channelId, Command command) {
        Matcher matcher = REMOTE_CHANNEL_PATTERN.matcher(channelId);
        if (!matcher.matches()) {
            return;
        }
        int remoteStart = Integer.parseInt(matcher.group(1));
        String setting = matcher.group(2);
        String name = null;
        Integer distance = null;
        Integer proportion = null;
        if (setting.equals("name") && command instanceof StringType string) {
            name = string.toString();
            if (name.length() > 16) {
                logger.warn("Remote start {} name exceeds 16 characters; truncating it", remoteStart);
                name = name.substring(0, 16);
            }
        } else if ((setting.equals("distance") || setting.equals("proportion"))
                && (command instanceof QuantityType<?> || command instanceof DecimalType)) {
            try {
                BigDecimal numericValue = command instanceof QuantityType<?> quantity ? quantity.toBigDecimal()
                        : ((DecimalType) command).toBigDecimal();
                int value = numericValue.intValueExact();
                if (setting.equals("distance")) {
                    if (value < 0 || value > 300) {
                        logger.warn("Ignoring invalid remote start distance {}", value);
                        return;
                    }
                    distance = value;
                } else {
                    if (value < 0 || value > 100) {
                        logger.warn("Ignoring invalid remote start proportion {}", value);
                        return;
                    }
                    proportion = value;
                }
            } catch (ArithmeticException e) {
                logger.warn("Ignoring non-integer remote start {} value", setting);
                return;
            }
        } else {
            return;
        }
        RemoteInfo result = robonectClient.updateRemoteStart(remoteStart, name, distance, proportion);
        if (result.isSuccessful()) {
            refreshRemoteInfo();
        }
    }

    /** Updates a single setting for a configurable timer. */
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
            } else if (command instanceof DateTimeType dateTime && (setting.equals("start") || setting.equals("end"))) {
                LocalTime time = dateTime.getZonedDateTime(ZoneOffset.UTC).toLocalTime();
                parameter = setting;
                value = time.format(DateTimeFormatter.ofPattern("HH:mm"));
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
        Integer id = timer.getId();
        if (id == null || id < 0)
            return;
        String prefix = "timer-" + id + "-";
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
        if (time != null && time.matches("\\d{2}:\\d{2}")) {
            int hours = Integer.parseInt(time.substring(0, 2));
            int minutes = Integer.parseInt(time.substring(3, 5));
            if (hours >= 24 || minutes >= 60) {
                return;
            }
            updateState(channel, toTimeOnlyDateTime(LocalTime.of(hours, minutes)));
        }
    }

    private void updateTimerDay(String prefix, String day, Boolean value) {
        if (value != null)
            updateState(prefix + day, OnOffType.from(value));
    }

    /** Refreshes all editable timer configurations. */
    private void refreshTimersInfo() {
        TimersInfo info = robonectClient.getTimersInfo();
        @Nullable
        List<TimerFull> timers = info.getTimers();
        if (info.isSuccessful() && timers != null) {
            updateTimerChannels(timers);
            timers.forEach(this::updateTimerState);
        }
    }

    private void updateTimerChannels(List<TimerFull> timers) {
        Set<Integer> timerIds = timers.stream().map(TimerFull::getId).filter(id -> id != null && id >= 0)
                .collect(Collectors.toSet());
        List<Channel> channels = new ArrayList<>();
        for (Integer id : timerIds.stream().sorted().toList()) {
            String prefix = "timer-" + id + "-";
            channels.add(createChannel(prefix + "enabled", "Switch", "timerEnabledType"));
            channels.add(createChannel(prefix + "start", "DateTime", "timerTimeType"));
            channels.add(createChannel(prefix + "end", "DateTime", "timerTimeType"));
            for (String day : List.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")) {
                channels.add(createChannel(prefix + day, "Switch", "timerDayType"));
            }
        }
        updateDynamicChannels(TIMER_CHANNEL_PATTERN, channels);
    }

    private Channel createChannel(String id, String itemType, String channelType) {
        return ChannelBuilder.create(new ChannelUID(getThing().getUID(), id), itemType)
                .withType(new ChannelTypeUID(BINDING_ID, channelType)).build();
    }

    private void updateDynamicChannels(Pattern channelPattern, List<Channel> desiredChannels) {
        List<Channel> existingChannels = getThing().getChannels();
        Set<ChannelUID> current = existingChannels.stream()
                .filter(channel -> channelPattern.matcher(channel.getUID().getId()).matches()).map(Channel::getUID)
                .collect(Collectors.toSet());
        Set<ChannelUID> desired = desiredChannels.stream().map(Channel::getUID).collect(Collectors.toSet());
        if (current.equals(desired)) {
            return;
        }
        List<Channel> channels = new ArrayList<>(existingChannels.stream()
                .filter(channel -> !channelPattern.matcher(channel.getUID().getId()).matches()).toList());
        channels.addAll(desiredChannels);
        updateThing(editThing().withChannels(channels).build());
    }

    /**
     * Updates the weather measurements and current mowing restriction.
     */
    private void refreshWeatherInfo() {
        WeatherInfo info = robonectClient.getWeatherInfo();
        Weather weather = info.getWeather();
        if (!info.isSuccessful() || weather == null) {
            return;
        }
        updateSwitch(CHANNEL_WEATHER_BREAK, weather.getWeatherBreak());
        updateWeatherNumber(CHANNEL_WEATHER_RAIN, weather.getRain());
        Integer temperature = weather.getTemperature();
        if (temperature != null)
            updateState(CHANNEL_WEATHER_TEMPERATURE, new QuantityType<>(temperature, SIUnits.CELSIUS));
        Integer humidity = weather.getHumidity();
        if (humidity != null)
            updateState(CHANNEL_WEATHER_HUMIDITY, new QuantityType<>(humidity, Units.PERCENT));
        updateSwitch(CHANNEL_WEATHER_DAY, weather.getDay());
        if (weather.getCity() != null)
            updateState(CHANNEL_WEATHER_CITY, new StringType(weather.getCity()));
        Integer remaining = weather.getRemaining();
        if (remaining != null)
            updateState(CHANNEL_WEATHER_REMAINING, new QuantityType<>(remaining, Units.SECOND));
        WeatherService service = info.getService();
        if (service != null) {
            updateSwitch(CHANNEL_WEATHER_ENABLED, service.getEnable());
            Integer interval = service.getInterval();
            if (interval != null)
                updateState(CHANNEL_WEATHER_INTERVAL, new QuantityType<>(interval, Units.MINUTE));
            WeatherConfig config = service.getConfig();
            if (config != null) {
                updateWeatherNumber(CHANNEL_WEATHER_MAX_RAIN, config.getMaxrain());
                updateWeatherTemperature(CHANNEL_WEATHER_MIN_TEMPERATURE, config.getMintemp());
                updateWeatherTemperature(CHANNEL_WEATHER_MAX_TEMPERATURE, config.getMaxtemp());
                updateWeatherNumber(CHANNEL_WEATHER_MIN_HUMIDITY, config.getMinhumidity());
                updateWeatherNumber(CHANNEL_WEATHER_MAX_HUMIDITY, config.getMaxhumidity());
                updateSwitch(CHANNEL_WEATHER_SKIP_DAY, config.getDontmowduringday());
                updateSwitch(CHANNEL_WEATHER_SKIP_NIGHT, config.getDontmowduringnight());
            }
        }
    }

    /** Sends a supported weather-service setting to the mower. */
    private void updateWeather(String channelId, Command command) {
        String parameter;
        String value;
        if (command instanceof OnOffType onOff) {
            parameter = switch (channelId) {
                case CHANNEL_WEATHER_ENABLED -> "service";
                case CHANNEL_WEATHER_SKIP_DAY -> "day";
                case CHANNEL_WEATHER_SKIP_NIGHT -> "night";
                default -> null;
            };
            value = onOff == OnOffType.ON ? "1" : "0";
        } else if (command instanceof QuantityType<?> quantity) {
            parameter = switch (channelId) {
                case CHANNEL_WEATHER_INTERVAL -> "interval";
                case CHANNEL_WEATHER_MAX_RAIN -> "rain";
                case CHANNEL_WEATHER_MIN_TEMPERATURE -> "mintemp";
                case CHANNEL_WEATHER_MAX_TEMPERATURE -> "maxtemp";
                case CHANNEL_WEATHER_MIN_HUMIDITY -> "minhum";
                case CHANNEL_WEATHER_MAX_HUMIDITY -> "maxhum";
                default -> null;
            };
            value = quantity.toBigDecimal().toPlainString();
        } else {
            return;
        }
        if (parameter == null) {
            return;
        }
        WeatherInfo result = robonectClient.updateWeather(parameter, value);
        if (result.isSuccessful()) {
            refreshWeatherInfo();
        }
    }

    private void updateWeatherTemperature(String channel, @Nullable Integer value) {
        if (value != null)
            updateState(channel, new QuantityType<>(value, SIUnits.CELSIUS));
    }

    /** Updates non-sensitive WLAN and access-point state. */
    private void refreshWlanInfo() {
        WlanInfo info = robonectClient.getWlanInfo();
        if (!info.isSuccessful()) {
            return;
        }
        WifiStation station = info.getStation();
        if (station != null) {
            if (station.getEnable() != null)
                updateState(CHANNEL_WLAN_ENABLED, OnOffType.from(station.getEnable()));
            if (station.getMac() != null)
                updateState(CHANNEL_WLAN_MAC, new StringType(station.getMac()));
            if (station.getSsid() != null)
                updateState(CHANNEL_WLAN_SSID, new StringType(station.getSsid()));
            if (station.getIp() != null)
                updateState(CHANNEL_WLAN_IP, new StringType(station.getIp()));
            if (station.getDhcp() != null)
                updateState(CHANNEL_WLAN_DHCP, OnOffType.from(station.getDhcp()));
            if (station.getPing() != null)
                updateState(CHANNEL_WLAN_PING, OnOffType.from(station.getPing()));
            if (station.getPingtime() != null)
                updateState(CHANNEL_WLAN_PING_TIME, new StringType(station.getPingtime()));
            if (station.getNetmask() != null)
                updateState(CHANNEL_WLAN_NETMASK, new StringType(station.getNetmask()));
            if (station.getGateway() != null)
                updateState(CHANNEL_WLAN_GATEWAY, new StringType(station.getGateway()));
        }
        AccessPoint ap = info.getAp();
        if (ap != null) {
            if (ap.getEnable() != null)
                updateState(CHANNEL_WLAN_AP_ENABLED, OnOffType.from(ap.getEnable()));
            if (ap.getMac() != null)
                updateState(CHANNEL_WLAN_AP_MAC, new StringType(ap.getMac()));
            if (ap.getHidden() != null)
                updateState(CHANNEL_WLAN_AP_HIDDEN, OnOffType.from(ap.getHidden()));
            if (ap.getSsid() != null)
                updateState(CHANNEL_WLAN_AP_SSID, new StringType(ap.getSsid()));
            Integer channel = ap.getChannel();
            if (channel != null)
                updateState(CHANNEL_WLAN_AP_CHANNEL, new DecimalType(channel));
            if (ap.getEncryption() != null)
                updateState(CHANNEL_WLAN_AP_ENCRYPTION, new StringType(ap.getEncryption()));
            Integer maxConnections = ap.getMaxconn();
            if (maxConnections != null)
                updateState(CHANNEL_WLAN_AP_MAX_CONNECTIONS, new DecimalType(maxConnections));
            if (ap.getIp() != null)
                updateState(CHANNEL_WLAN_AP_IP, new StringType(ap.getIp()));
            if (ap.getNetmask() != null)
                updateState(CHANNEL_WLAN_AP_NETMASK, new StringType(ap.getNetmask()));
            if (ap.getGateway() != null)
                updateState(CHANNEL_WLAN_AP_GATEWAY, new StringType(ap.getGateway()));
            if (ap.getDhcp() != null) {
                if (ap.getDhcp().getEnable() != null)
                    updateState(CHANNEL_WLAN_AP_DHCP, OnOffType.from(ap.getDhcp().getEnable()));
                if (ap.getDhcp().getStart() != null)
                    updateState(CHANNEL_WLAN_AP_DHCP_START, new StringType(ap.getDhcp().getStart()));
                if (ap.getDhcp().getEnd() != null)
                    updateState(CHANNEL_WLAN_AP_DHCP_END, new StringType(ap.getDhcp().getEnd()));
            }
        }
    }

    /** Updates optional GPIO and output extension state. */
    private void refreshExtensionInfo() {
        ExtInfo info = robonectClient.getExtInfo();
        Extensions extensions = info.getExt();
        if (!info.isSuccessful() || extensions == null) {
            return;
        }
        updateExtension("extension-gpio-1-", extensions.getGpio1());
        updateExtension("extension-gpio-2-", extensions.getGpio2());
        updateExtension("extension-output-1-", extensions.getOut1());
        updateExtension("extension-output-2-", extensions.getOut2());
    }

    /** Updates non-sensitive push-service settings. */
    private void refreshPushInfo() {
        PushInfo info = robonectClient.getPushInfo();
        Push push = info.getPush();
        if (!info.isSuccessful() || push == null) {
            return;
        }
        PushServer server = push.getServer();
        if (server != null) {
            if (server.getUrl() != null)
                updateState(CHANNEL_PUSH_URL, new StringType(server.getUrl()));
            if (server.getName() != null)
                updateState(CHANNEL_PUSH_USERNAME, new StringType(server.getName()));
        }
        PushTriggers triggers = push.getTrigger();
        if (triggers != null) {
            Integer interval = triggers.getInterval();
            if (interval != null)
                updateState(CHANNEL_PUSH_INTERVAL, new QuantityType<>(interval, Units.SECOND));
            List<Channel> channels = new ArrayList<>();
            for (String number : triggers.getTriggers().keySet()) {
                String prefix = "push-trigger-" + number + "-";
                channels.add(createChannel(prefix + "name", "String", "pushTriggerNameType"));
                channels.add(createChannel(prefix + "enter", "Switch", "pushTriggerSwitchType"));
                channels.add(createChannel(prefix + "leave", "Switch", "pushTriggerSwitchType"));
            }
            updateDynamicChannels(PUSH_TRIGGER_CHANNEL_PATTERN, channels);
            for (Map.Entry<String, PushTriggers.Trigger> entry : triggers.getTriggers().entrySet()) {
                String prefix = "push-trigger-" + entry.getKey() + "-";
                PushTriggers.Trigger trigger = entry.getValue();
                if (trigger.getName() != null) {
                    updateState(prefix + "name", new StringType(trigger.getName()));
                }
                updateSwitch(prefix + "enter", trigger.getEnter());
                updateSwitch(prefix + "leave", trigger.getLeave());
            }
        }
    }

    private void updatePushTrigger(String channelId, Command command) {
        Matcher matcher = PUSH_TRIGGER_CHANNEL_PATTERN.matcher(channelId);
        if (!matcher.matches() || !(command instanceof OnOffType onOff) || matcher.group(2).equals("name")) {
            return;
        }
        PushInfo info = robonectClient.getPushInfo();
        Push push = info.getPush();
        PushTriggers triggers = push == null ? null : push.getTrigger();
        PushTriggers.Trigger trigger = triggers == null ? null : triggers.getTriggers().get(matcher.group(1));
        if (!info.isSuccessful() || trigger == null || trigger.getEnter() == null || trigger.getLeave() == null) {
            return;
        }
        boolean enter = matcher.group(2).equals("enter") ? onOff == OnOffType.ON : trigger.getEnter();
        boolean leave = matcher.group(2).equals("leave") ? onOff == OnOffType.ON : trigger.getLeave();
        int value = (enter ? 1 : 0) | (leave ? 2 : 0);
        if (robonectClient.updatePush("trigger" + matcher.group(1), Integer.toString(value)).isSuccessful()) {
            refreshPushInfo();
        }
    }

    private void updatePush(String channelId, Command command) {
        String parameter;
        String value;
        if (command instanceof StringType string) {
            parameter = channelId.equals(CHANNEL_PUSH_URL) ? "url"
                    : channelId.equals(CHANNEL_PUSH_USERNAME) ? "name" : null;
            value = string.toFullString();
        } else if (command instanceof QuantityType<?> quantity && channelId.equals(CHANNEL_PUSH_INTERVAL)) {
            parameter = "interval";
            value = quantity.toBigDecimal().toPlainString();
        } else {
            return;
        }
        if (parameter != null && robonectClient.updatePush(parameter, value).isSuccessful())
            refreshPushInfo();
    }

    private void updateExtension(String prefix, @Nullable Extension extension) {
        if (extension == null) {
            return;
        }
        updateSwitch(prefix + "flash", extension.getFlashonerror());
        updateSwitch(prefix + "inverted", extension.getInverted());
        updateSwitch(prefix + "status", extension.getStatus());
    }

    private void updateSwitch(String channel, @Nullable Boolean value) {
        if (value != null)
            updateState(channel, OnOffType.from(value));
    }

    private void updateWeatherNumber(String channel, @Nullable Integer value) {
        if (value != null)
            updateState(channel, new DecimalType(value));
    }

    /** Updates door channels from the optional door-system response. */
    private void refreshDoorInfo() {
        DoorInfo info = robonectClient.getDoorInfo();
        Door door = info.getDoor();
        if (!info.isSuccessful() || door == null) {
            return;
        }
        if (door.getEnabled() != null) {
            updateState(CHANNEL_DOOR_ENABLED, OnOffType.from(door.getEnabled()));
        }
        if (door.getArrested() != null) {
            updateState(CHANNEL_DOOR_ARRESTED, OnOffType.from(door.getArrested()));
        }
        updateDoorState(door.getState(), door.getPending());
    }

    private void updateDoorState(@Nullable DoorState state, @Nullable DoorState pending) {
        if (pending != null && Boolean.TRUE.equals(pending.getOpen()) && !Boolean.TRUE.equals(pending.getClose())) {
            updateState(CHANNEL_DOOR_STATE, new PercentType(75));
        } else if (pending != null && !Boolean.TRUE.equals(pending.getOpen())
                && Boolean.TRUE.equals(pending.getClose())) {
            updateState(CHANNEL_DOOR_STATE, new PercentType(25));
        } else if (state != null && Boolean.TRUE.equals(state.getOpen()) && !Boolean.TRUE.equals(state.getClose())) {
            updateState(CHANNEL_DOOR_STATE, PercentType.ZERO);
        } else if (state != null && !Boolean.TRUE.equals(state.getOpen()) && Boolean.TRUE.equals(state.getClose())) {
            updateState(CHANNEL_DOOR_STATE, PercentType.HUNDRED);
        } else {
            updateState(CHANNEL_DOOR_STATE, UnDefType.UNDEF);
        }
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
        String channelId = channelUID.getId();
        if (channelId.startsWith("timer-") && !channelId.equals(CHANNEL_TIMER_NEXT_TIMER)
                && !channelId.equals(CHANNEL_TIMER_STATUS)) {
            refreshTimersInfo();
            return;
        }
        if (BATTERY_CHANNEL_PATTERN.matcher(channelId).matches() || channelId.startsWith("battery-")) {
            refreshBatteryInfo();
            return;
        }
        if (WIRE_CHANNEL_PATTERN.matcher(channelId).matches()) {
            refreshWireInfo();
            return;
        }
        if (PUSH_TRIGGER_CHANNEL_PATTERN.matcher(channelId).matches() || channelId.startsWith("push-")) {
            refreshPushInfo();
            return;
        }
        if (channelId.startsWith("statistics-")) {
            refreshHourInfo();
            return;
        }
        switch (channelId) {
            case CHANNEL_MOWER_NAME:
            case CHANNEL_STATUS_BATTERY:
            case CHANNEL_STATUS:
            case CHANNEL_STATUS_PREVENT:
            case CHANNEL_STATUS_HOME:
            case CHANNEL_STATUS_DURATION:
            case CHANNEL_STATUS_HOURS:
            case CHANNEL_STATUS_MODE:
            case CHANNEL_MOWER_START:
            case CHANNEL_TIMER_NEXT_TIMER:
            case CHANNEL_TIMER_STATUS:
            case CHANNEL_WLAN_SIGNAL:
                refreshMowerInfo();
                break;
            case CHANNEL_WLAN_ENABLED:
            case CHANNEL_WLAN_SSID:
            case CHANNEL_WLAN_IP:
            case CHANNEL_WLAN_AP_ENABLED:
            case CHANNEL_WLAN_AP_SSID:
                refreshWlanInfo();
                break;
            case CHANNEL_JOB:
                refreshMowerInfo();
                break;
            case CHANNEL_DOOR_ENABLED:
            case CHANNEL_DOOR_STATE:
            case CHANNEL_DOOR_ARRESTED:
                refreshDoorInfo();
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

    /** Performs a service operation when the corresponding switch is turned on. */
    private void performServiceOperation(Command command, Operation operation) {
        if (command != OnOffType.ON) {
            logger.debug("Got service operation command {} but On is expected.", command);
            return;
        }
        RobonectAnswer answer = robonectClient.performServiceOperation(operation);
        if (!answer.isSuccessful()) {
            logger.debug("Could not perform service operation {}. Robonect error message: {}", operation,
                    answer.getErrorMessage());
        }
    }

    /** Controls the optional door system. */
    private void handleDoorCommand(Command command) {
        DoorInfo answer;
        if (command == UpDownType.UP) {
            answer = robonectClient.setDoorDirection(Direction.OPEN);
        } else if (command == UpDownType.DOWN) {
            answer = robonectClient.setDoorDirection(Direction.CLOSE);
        } else if (command == StopMoveType.STOP) {
            answer = robonectClient.releaseDoor();
        } else {
            logger.debug("Got door command of type {} but Up, Down, or Stop is expected.",
                    command.getClass().getName());
            return;
        }
        if (answer.isSuccessful()) {
            refreshDoorInfo();
        } else {
            logger.debug("Could not control the door. Robonect error message: {}", answer.getErrorMessage());
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
        Status status = info.getStatus();
        if (info.isSuccessful() && status != null) {
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
            updateState(CHANNEL_STATUS_BATTERY, new DecimalType(status.getBattery()));
            Integer prevent = status.getPrevent();
            if (prevent != null) {
                updateState(CHANNEL_STATUS_PREVENT, new DecimalType(prevent));
            }
            Boolean home = status.getHome();
            if (home != null) {
                updateState(CHANNEL_STATUS_HOME, OnOffType.from(home));
            }
            if (status.getStatus() != null) {
                updateState(CHANNEL_STATUS, new DecimalType(status.getStatus().getStatusCode()));
            }
            updateState(CHANNEL_STATUS_DURATION, new QuantityType<>(status.getDuration(), Units.SECOND));
            updateState(CHANNEL_STATUS_DISTANCE, new QuantityType<>(status.getDistance(), SIUnits.METRE));
            updateState(CHANNEL_STATUS_HOURS, new QuantityType<>(status.getHours(), Units.HOUR));
            updateMowerModeAndJob(status.getMode());
            updateState(CHANNEL_MOWER_START, OnOffType.from(!status.isStopped()));
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
                if (info.getTimer().getStatus() != null) {
                    updateState(CHANNEL_TIMER_STATUS, new StringType(info.getTimer().getStatus().name()));
                }
            }
            if (info.getWlan() != null) {
                Integer signal = info.getWlan().getSignal();
                if (signal != null) {
                    updateState(CHANNEL_WLAN_SIGNAL_STRENGTH, new DecimalType(signal));
                    updateState(CHANNEL_WLAN_SIGNAL, new DecimalType(convertSignalStrength(signal)));
                } else {
                    updateState(CHANNEL_WLAN_SIGNAL, UnDefType.UNDEF);
                    updateState(CHANNEL_WLAN_SIGNAL_STRENGTH, UnDefType.UNDEF);
                }
            } else {
                updateState(CHANNEL_WLAN_SIGNAL, UnDefType.UNDEF);
                updateState(CHANNEL_WLAN_SIGNAL_STRENGTH, UnDefType.UNDEF);
            }
        } else {
            logger.debug("Could not retrieve complete mower information: {}", info.getErrorMessage());
        }
    }

    /**
     * Updates the job and mode channels using the optional localized web-interface response.
     *
     * The public JSON API does not expose end-of-day mode or the active job time window. UI scraping is therefore
     * opt-in and falls back to the JSON API when the UI response changes or cannot be read.
     *
     * @param mowerMode mode reported by the JSON status endpoint
     */
    private void updateMowerModeAndJob(@Nullable MowerMode mowerMode) {
        if (!ajaxUiScraping) {
            if (mowerMode != null) {
                updateState(CHANNEL_STATUS_MODE, new StringType(mowerMode.name()));
            }
            updateState(CHANNEL_JOB, OnOffType.from(robonectClient.isJobRunning()));
            updateState(CHANNEL_JOB_START, UnDefType.NULL);
            updateState(CHANNEL_JOB_END, UnDefType.NULL);
            return;
        }

        try {
            AjaxInfo ajaxInfo = robonectClient.getAjaxInfo();
            if (ajaxInfo == null) {
                throw new JsonSyntaxException("Robonect web-interface response is empty");
            }
            String controlText = ajaxInfo.getControlMenuText();
            String timerText = ajaxInfo.getTimerMenuText();
            if (timerText != null && JOB_TIMER_LABELS.contains(timerText)) {
                updateState(CHANNEL_JOB, OnOffType.ON);
                updateJobTimes(controlText);
            } else {
                updateState(CHANNEL_JOB, OnOffType.OFF);
                updateState(CHANNEL_JOB_START, UnDefType.NULL);
                updateState(CHANNEL_JOB_END, UnDefType.NULL);
            }
            if (mowerMode != null) {
                String mode = mowerMode == MowerMode.HOME && END_OF_DAY_LABELS.contains(controlText) ? "EOD"
                        : mowerMode.name();
                updateState(CHANNEL_STATUS_MODE, new StringType(mode));
            }
        } catch (RobonectCommunicationException | JsonSyntaxException | IllegalArgumentException e) {
            logger.debug("Could not retrieve Robonect web-interface status. Falling back to the JSON API.", e);
            if (mowerMode != null) {
                updateState(CHANNEL_STATUS_MODE, new StringType(mowerMode.name()));
            }
            updateState(CHANNEL_JOB, OnOffType.from(robonectClient.isJobRunning()));
            updateState(CHANNEL_JOB_START, UnDefType.NULL);
            updateState(CHANNEL_JOB_END, UnDefType.NULL);
        }
    }

    /**
     * Updates the active job start and end channels from the localized menu text.
     *
     * @param controlText localized menu text that contains the job time window
     */
    private void updateJobTimes(@Nullable String controlText) {
        if (controlText == null) {
            updateState(CHANNEL_JOB_START, UnDefType.UNDEF);
            updateState(CHANNEL_JOB_END, UnDefType.UNDEF);
            return;
        }
        Matcher matcher = JOB_TIME_PATTERN.matcher(controlText);
        if (!matcher.matches()) {
            updateState(CHANNEL_JOB_START, UnDefType.UNDEF);
            updateState(CHANNEL_JOB_END, UnDefType.UNDEF);
            return;
        }
        updateJobTime(CHANNEL_JOB_START, matcher.group(1));
        updateJobTime(CHANNEL_JOB_END, matcher.group(2));
    }

    /**
     * Updates one job time channel using the Unix epoch date and UTC to represent the time of day.
     *
     * @param channelId job time channel identifier
     * @param value UI value in {@code HH:mm} format, or {@code null}
     */
    private void updateJobTime(String channelId, @Nullable String value) {
        if (value == null) {
            updateState(channelId, UnDefType.UNDEF);
            return;
        }
        Matcher matcher = TIME_PATTERN.matcher(value);
        if (!matcher.matches()) {
            updateState(channelId, UnDefType.UNDEF);
            return;
        }
        int hours = Integer.parseInt(matcher.group(1));
        int minutes = Integer.parseInt(matcher.group(2));
        if (hours >= 24 || minutes >= 60) {
            updateState(channelId, UnDefType.UNDEF);
            return;
        }
        updateState(channelId, toTimeOnlyDateTime(LocalTime.of(hours, minutes)));
    }

    private DateTimeType toTimeOnlyDateTime(LocalTime time) {
        return new DateTimeType(Instant.ofEpochSecond(time.toSecondOfDay()));
    }

    /**
     * Updates detailed battery channels when the mower supports the battery endpoint.
     */
    private void refreshBatteryInfo() {
        BatteryInfo info = robonectClient.getBatteryInfo();
        List<Battery> batteries = info.getBatteries();
        if (!info.isSuccessful() || batteries == null) {
            logger.debug("Could not retrieve detailed battery information: {}", info.getErrorMessage());
            return;
        }
        List<Channel> channels = new ArrayList<>();
        for (int index = 0; index < batteries.size(); index++) {
            String prefix = "battery-" + (index + 1) + "-";
            channels.add(createChannel(prefix + "id", "Number", "batteryIdType"));
            channels.add(createChannel(prefix + "charge", "Number:Dimensionless", "batteryChargeType"));
            channels.add(createChannel(prefix + "voltage", "Number:ElectricPotential", "batteryVoltageType"));
            channels.add(createChannel(prefix + "current", "Number:ElectricCurrent", "batteryCurrentType"));
            channels.add(createChannel(prefix + "temperature", "Number:Temperature", "batteryTemperatureType"));
            channels.add(createChannel(prefix + "capacity", "Number:ElectricCharge", "batteryCapacityType"));
            channels.add(createChannel(prefix + "remaining-capacity", "Number:ElectricCharge",
                    "batteryRemainingCapacityType"));
        }
        updateDynamicChannels(BATTERY_CHANNEL_PATTERN, channels);
        for (int index = 0; index < batteries.size(); index++) {
            updateBatteryState("battery-" + (index + 1) + "-", batteries.get(index));
        }
        if (!batteries.isEmpty()) {
            updateBatteryState("battery-", batteries.getFirst());
        } else {
            for (String channel : List.of(CHANNEL_BATTERY_ID, CHANNEL_BATTERY_CHARGE, CHANNEL_BATTERY_VOLTAGE,
                    CHANNEL_BATTERY_CURRENT, CHANNEL_BATTERY_TEMPERATURE, CHANNEL_BATTERY_CAPACITY,
                    CHANNEL_BATTERY_REMAINING_CAPACITY)) {
                updateState(channel, UnDefType.UNDEF);
            }
        }
    }

    private void updateBatteryState(String prefix, Battery battery) {
        Integer id = battery.getId();
        if (id != null) {
            updateState(prefix + "id", new DecimalType(id));
        }
        Integer charge = battery.getCharge();
        if (charge != null) {
            updateState(prefix + "charge", new QuantityType<>(charge, Units.PERCENT));
        }
        Integer voltage = battery.getVoltage();
        if (voltage != null) {
            updateState(prefix + "voltage", new QuantityType<>(voltage / 1000.0, Units.VOLT));
        }
        Integer current = battery.getCurrent();
        if (current != null) {
            updateState(prefix + "current", new QuantityType<>(current / 1000.0, Units.AMPERE));
        }
        Integer temperature = battery.getTemperature();
        if (temperature != null) {
            updateState(prefix + "temperature", new QuantityType<>(temperature / 10.0, SIUnits.CELSIUS));
        }
        BatteryCapacity capacity = battery.getCapacity();
        if (capacity != null) {
            Integer full = capacity.getFull();
            if (full != null) {
                updateState(prefix + "capacity", new QuantityType<>(full, Units.MILLIAMPERE_HOUR));
            }
            Integer remainingCapacity = capacity.getRemaining();
            if (remainingCapacity != null) {
                updateState(prefix + "remaining-capacity",
                        new QuantityType<>(remainingCapacity, Units.MILLIAMPERE_HOUR));
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
            Integer average = blade.getAverage();
            if (average != null) {
                updateState(CHANNEL_BLADE_MOTOR_AVERAGE, new DecimalType(average));
            }
            Integer speed = blade.getSpeed();
            if (speed != null) {
                updateState(CHANNEL_BLADE_MOTOR_SPEED, new DecimalType(speed));
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
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve operating statistics: {}", info.getErrorMessage());
            return;
        }
        HourGeneral general = info.getGeneral();
        if (general != null) {
            updateHours(CHANNEL_STATISTICS_RUNNING_HOURS, general.getRun());
            updateHours(CHANNEL_STATISTICS_MOWING_HOURS, general.getMow());
            updateHours(CHANNEL_STATISTICS_SEARCHING_HOURS, general.getSearch());
            updateHours(CHANNEL_STATISTICS_CHARGING_HOURS, general.getCharge());
            Integer charges = general.getCharges();
            if (charges != null) {
                updateState(CHANNEL_STATISTICS_CHARGES, new DecimalType(charges));
            }
            Integer errors = general.getErrors();
            if (errors != null) {
                updateState(CHANNEL_STATISTICS_ERRORS, new DecimalType(errors));
            }
        }
        updateRecentDurations(CHANNEL_STATISTICS_SEEK, info.getSeek());
        updateRecentDurations(CHANNEL_STATISTICS_MOWING, info.getMowing());
    }

    private void updateRecentDurations(String channelId, @Nullable List<HourInfo.DurationEntry> entries) {
        if (entries == null) {
            return;
        }
        List<Integer> minutes = entries.stream().map(HourInfo.DurationEntry::getDuration).filter(value -> value != null)
                .toList();
        updateState(channelId, new StringType(minutes.stream().map(String::valueOf).collect(Collectors.joining(", "))));
        if (minutes.isEmpty()) {
            updateState(channelId + "-min", UnDefType.UNDEF);
            updateState(channelId + "-max", UnDefType.UNDEF);
            updateState(channelId + "-average", UnDefType.UNDEF);
            return;
        }
        IntSummaryStatistics statistics = minutes.stream().mapToInt(Integer::intValue).summaryStatistics();
        updateState(channelId + "-min", new QuantityType<>(statistics.getMin(), Units.MINUTE));
        updateState(channelId + "-max", new QuantityType<>(statistics.getMax(), Units.MINUTE));
        updateState(channelId + "-average", new QuantityType<>(statistics.getAverage(), Units.MINUTE));
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
        List<Channel> channels = new ArrayList<>();
        for (int index = 0; index < sensors.size(); index++) {
            String prefix = "wire-sensor-" + (index + 1) + "-";
            channels.add(createChannel(prefix + "label", "String", "wireSensorLabelType"));
            channels.add(createChannel(prefix + "quality", "Number:Dimensionless", "wireSensorQualityType"));
            for (String measurement : List.of("a", "n", "f", "g1")) {
                channels.add(createChannel(prefix + measurement, "Number", "wireSensorValueType"));
            }
        }
        updateDynamicChannels(WIRE_CHANNEL_PATTERN, channels);
        for (int index = 0; index < sensors.size(); index++) {
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
        Integer quality = sensor.getQuality();
        if (quality != null)
            updateState(prefix + "quality", new QuantityType<>(quality, Units.PERCENT));
        Integer a = sensor.getA();
        if (a != null)
            updateState(prefix + "a", new DecimalType(a));
        Integer n = sensor.getN();
        if (n != null)
            updateState(prefix + "n", new DecimalType(n));
        Integer f = sensor.getF();
        if (f != null)
            updateState(prefix + "f", new DecimalType(f));
        Integer g1 = sensor.getG1();
        if (g1 != null)
            updateState(prefix + "g1", new DecimalType(g1));
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
        Integer path = remote.getPath();
        if (path != null)
            updateState(prefix + "path", new DecimalType(path));
        Integer proportion = remote.getProportion();
        if (proportion != null)
            updateState(prefix + "proportion", new QuantityType<>(proportion, Units.PERCENT));
        Integer distance = remote.getDistance();
        if (distance != null)
            updateState(prefix + "distance", new QuantityType<>(distance, SIUnits.METRE));
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
        Integer power = motor.getPower();
        if (power != null) {
            updateState(powerChannel, new QuantityType<>(power, Units.PERCENT));
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
            if (info.getApplication() != null) {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, valueOrNotAvailable(info.getSerial()));
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, valueOrNotAvailable(info.getRobonect().getVersion()));
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                        valueOrNotAvailable(info.getApplication().getVersion()));
                properties.put(PROPERTY_COMPILED, valueOrNotAvailable(info.getApplication().getCompiled()));
                properties.put(PROPERTY_COMMENT, valueOrNotAvailable(info.getApplication().getComment()));
            } else {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, valueOrNotAvailable(info.getRobonect().getSerial()));
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, VersionInfo.NOT_AVAILABLE);
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, valueOrNotAvailable(info.getRobonect().getVersion()));
                properties.put(PROPERTY_COMPILED, valueOrNotAvailable(info.getRobonect().getCompiled()));
                properties.put(PROPERTY_COMMENT, valueOrNotAvailable(info.getRobonect().getComment()));
            }
            updateProperties(properties);
        } else {
            logger.debug("Could not retrieve mower version info. Robonect error response message: {}",
                    info.getErrorMessage());
        }
    }

    private String valueOrNotAvailable(@Nullable String value) {
        return value == null ? VersionInfo.NOT_AVAILABLE : value;
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
        ajaxUiScraping = robonectConfig.isAjaxUiScraping();
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
        if (pushServlet != null) {
            pushServlet.registerHandler(getThing().getUID(), this);
        }
        Runnable runnable = new MowerChannelPoller(TimeUnit.SECONDS.toMillis(robonectConfig.getOfflineTimeout()),
                robonectConfig.isDetailedInfo(), robonectConfig.getDetailedInfoInterval());
        int pollInterval = robonectConfig.getPollInterval();
        pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, pollInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (pushServlet != null) {
            pushServlet.unregisterHandler(getThing().getUID());
        }
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

    /** Requests an immediate status refresh after a Robonect push notification. */
    public void handlePushNotification() {
        scheduler.execute(() -> {
            try {
                refreshMowerInfo();
            } catch (RobonectCommunicationException e) {
                logger.debug("Could not refresh mower state after push notification: {}", e.getMessage());
            }
        });
    }

    /**
     * method to inject the robonect client to be used in test cases to allow mocking.
     *
     * @param robonectClient
     */
    protected void setRobonectClient(RobonectClient robonectClient) {
        this.robonectClient = robonectClient;
    }

    /**
     * Allows tests to enable UI scraping without initializing a Thing configuration.
     *
     * @param ajaxUiScraping whether UI scraping is enabled
     */
    protected void setAjaxUiScraping(boolean ajaxUiScraping) {
        this.ajaxUiScraping = ajaxUiScraping;
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
                    refreshWeatherInfo();
                    refreshExtensionInfo();
                    refreshPushInfo();
                    refreshDoorInfo();
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
