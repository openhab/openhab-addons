/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bambulab.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jbambuapi.mqtt.ConnectionCallback;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.GCodeFileCommand;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PrintSpeedCommand;
import pl.grzeslowski.jbambuapi.mqtt.PrinterWatcher;
import pl.grzeslowski.jbambuapi.mqtt.Report;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel.*;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.Channel.*;
import static org.openhab.binding.bambulab.internal.TrayHelper.updateTrayLoaded;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatus.UNKNOWN;
import static org.openhab.core.thing.ThingStatusDetail.*;
import static org.openhab.core.types.UnDefType.UNDEF;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.LedNode.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PushingCommand.defaultPushingCommand;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClientConfig.requiredFields;

/**
 * The {@link PrinterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class PrinterHandler extends BaseThingHandler
        implements PrinterWatcher.StateSubscriber, BambuHandler, ConnectionCallback {
    private static final Pattern DBM_PATTERN = Pattern.compile("^(-?\\d+)dBm$");
    private Logger logger = LoggerFactory.getLogger(PrinterHandler.class);

    private @Nullable PrinterClient client;
    private @Nullable Camera camera;
    private final AtomicInteger reconnectTimes = new AtomicInteger();
    private final AtomicReference<@Nullable ScheduledFuture<?>> reconnectSchedule = new AtomicReference<>();

    public PrinterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_LED_CHAMBER_LIGHT.is(channelUID) || CHANNEL_LED_WORK_LIGHT.is(channelUID)) {
            var ledNode = CHANNEL_LED_CHAMBER_LIGHT.is(channelUID) ? CHAMBER_LIGHT : WORK_LIGHT;
            var bambuCommand = "ON".equals(command.toFullString()) ? on(ledNode) : off(ledNode);
            sendCommand(bambuCommand);
        } else if (CHANNEL_GCODE_FILE.is(channelUID)) {
            var bambuCommand = new GCodeFileCommand(command.toString());
            sendCommand(bambuCommand);
        } else if (CHANNEL_SPEED_LEVEL.is(channelUID)) {
            var bambuCommand = PrintSpeedCommand.valueOf(command.toString());
            sendCommand(bambuCommand);
        } else if (CHANNEL_CAMERA_RECORD.is(channelUID) && command instanceof OnOffType onOffCommand) {
            requireNonNull(camera).handleCommand(onOffCommand);
        }
    }

    @Override
    public void initialize() {
        try {
            internalInitialize();
        } catch (InitializationException e) {
            updateStatus(OFFLINE, e.getThingStatusDetail(), e.getDescription());
        }
    }

    private void internalInitialize() throws InitializationException {
        var config = getConfigAs(PrinterConfiguration.class);

        config.validateSerial();
        logger = LoggerFactory.getLogger(PrinterHandler.class.getName() + "." + config.serial);
        config.validateHostname();
        config.validateAccessCode();
        config.validateUsername();
        var uri = config.buildUri();

        // always turn off camera recording when starting thing
        camera = new Camera(config, this);
        updateState(CHANNEL_CAMERA_RECORD, OnOffType.OFF);
        updateState(CHANNEL_CAMERA_IMAGE, UNDEF);

        var localClient = client = buildLocalClient(uri, config);

        // the status will be unknown until the first message form MQTT arrives
        updateStatus(UNKNOWN);
        try {
            scheduler.execute(() -> initMqtt(localClient, config));
        } catch (RejectedExecutionException ex) {
            logger.debug("Task was rejected", ex);
            throw new InitializationException(CONFIGURATION_ERROR, ex);
        }
    }

    private void initMqtt(PrinterClient client, PrinterConfiguration config) {
        try {
            logger.debug("Trying to connect to the printer broker");
            client.connect(this);
            var printerWatcher = new PrinterWatcher();
            client.subscribe(printerWatcher);
            printerWatcher.subscribe(this);
            // update to online done in `connectComplete`
        } catch (Exception e) {
            logger.debug("Cannot connect to MQTT client", e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getLocalizedMessage());
            reconnect(config);
        }
    }

    @Override
    public void connectComplete(boolean reconnect) {
        logger.debug("connectComplete({})", reconnect);
        // send request to update all channels
        scheduler.schedule(this::refreshChannels, 1, SECONDS);
        reconnectTimes.set(0);
        updateStatus(ONLINE);
    }

    @Override
    public void connectionLost(@Nullable Throwable throwable) {
        logger.debug("Connection lost. Restarting thing", throwable);
        var message = throwable != null ? throwable.getLocalizedMessage() : "<no message>";
        var description = "@text/printer.handler.init.connectionLost [\"%s\"]".formatted(message);
        updateStatus(OFFLINE, COMMUNICATION_ERROR, description);
        reconnect(null);
    }

    private void reconnect(@Nullable PrinterConfiguration config) {
        if (reconnectSchedule.get() != null) {
            logger.warn("Already reconnecting!");
            return;
        }
        var configNotNull = Optional.ofNullable(config)//
                .orElseGet(() -> getConfigAs(PrinterConfiguration.class));
        var reconnectTime = configNotNull.reconnectTime;
        var reconnectMax = configNotNull.reconnectMax;
        var currentReconnections = reconnectTimes.getAndAdd(1);
        if (currentReconnections >= reconnectMax) {
            logger.warn("Do not reconnecting any more, because max reconnections was reach!");
            return;
        }
        reconnectSchedule.set(//
                scheduler.schedule(() -> {
                    logger.debug("Reconnecting...");
                    reconnectSchedule.set(null);
                    dispose();
                    initialize();
                }, reconnectTime, SECONDS));
    }

    private PrinterClient buildLocalClient(URI uri, PrinterConfiguration config) throws InitializationException {
        try {
            return new PrinterClient(
                    requiredFields(uri, config.username, config.serial, config.accessCode.toCharArray()));
        } catch (Exception e) {
            logger.debug("Cannot create MQTT client", e);
            throw new InitializationException(COMMUNICATION_ERROR, e);
        }
    }

    void refreshChannels() {
        sendCommand(defaultPushingCommand());
    }

    @Override
    public void dispose() {
        try {
            var localClient = client;
            client = null;
            if (localClient != null) {
                try {
                    localClient.close();
                } catch (Exception e) {
                    logger.warn("Could not correctly dispose PrinterClient", e);
                }
            }
            var localCamera = camera;
            camera = null;
            if (localCamera != null) {
                localCamera.close();
            }
            var localReconnectSchedule = reconnectSchedule.get();
            reconnectSchedule.set(null);
            if (localReconnectSchedule != null) {
                localReconnectSchedule.cancel(true);
            }
        } finally {
            logger = LoggerFactory.getLogger(PrinterHandler.class);
        }
    }

    @Override
    public void newState(@Nullable Report delta, @Nullable Report fullState) {
        logger.trace("New Printer state from delta {}", delta);
        // only need to update channels from delta
        // do not need to use full state, because at some point in past channels was already updated with its values
        if (delta == null) {
            return;
        }
        updatePrinterChannels(delta);
        // if got new Printer state (and not failed) then make sure that thing status in ONLINE
        updateStatus(ONLINE);
    }

    private void updatePrinterChannels(Report state) {
        // Print
        var print = state.print();
        if (print == null) {
            return;
        }
        // tempers
        updateCelsiusState(CHANNEL_NOZZLE_TEMPERATURE, print.nozzleTemper());
        updateCelsiusState(CHANNEL_NOZZLE_TARGET_TEMPERATURE, print.nozzleTargetTemper());
        updateCelsiusState(CHANNEL_BED_TEMPERATURE, print.bedTemper());
        updateCelsiusState(CHANNEL_BED_TARGET_TEMPERATURE, print.bedTargetTemper());
        updateCelsiusState(CHANNEL_CHAMBER_TEMPERATURE, print.chamberTemper());
        // string
        updateStringState(CHANNEL_MC_PRINT_STAGE, print.mcPrintStage());
        updateStringState(CHANNEL_BED_TYPE, print.bedType());
        updateStringState(CHANNEL_GCODE_FILE, print.gcodeFile());
        updateStringState(CHANNEL_GCODE_STATE, print.gcodeState());
        updateStringState(CHANNEL_REASON, print.reason());
        updateStringState(CHANNEL_RESULT, print.result());
        // percent
        updatePercentState(CHANNEL_MC_PERCENT, print.mcPercent());
        updatePercentState(CHANNEL_GCODE_FILE_PREPARE_PERCENT, print.gcodeFilePreparePercent());
        // decimal
        updateDecimalState(CHANNEL_MC_REMAINING_TIME, print.mcRemainingTime());
        updateDecimalState(CHANNEL_BIG_FAN_1_SPEED, print.bigFan1Speed());
        updateDecimalState(CHANNEL_BIG_FAN_2_SPEED, print.bigFan2Speed());
        updateDecimalState(CHANNEL_HEAT_BREAK_FAN_SPEED, print.heatbreakFanSpeed());
        updateDecimalState(CHANNEL_LAYER_NUM, print.layerNum());
        if (print.spdLvl() != null) {
            var speedLevel = PrintSpeedCommand.findByLevel(print.spdLvl());
            updateState(CHANNEL_SPEED_LEVEL, new StringType(speedLevel.toString()));
        }
        // boolean
        updateBooleanState(CHANNEL_TIME_LAPS, print.timelapse());
        updateBooleanState(CHANNEL_USE_AMS, print.useAms());
        updateBooleanState(CHANNEL_VIBRATION_CALIBRATION, print.vibrationCali());
        // lights
        updateLightState("chamber_light", CHANNEL_LED_CHAMBER_LIGHT, print.lightsReport());
        updateLightState("work_light", CHANNEL_LED_WORK_LIGHT, print.lightsReport());
        // other
        if (print.wifiSignal() != null) {
            updateState(CHANNEL_WIFI_SIGNAL, parseWifiChannel(print.wifiSignal()));
        }
        // ams
        Optional.of(print)//
                .map(Report.Print::ams)//
                .ifPresent(ams -> {
                    updateState(CHANNEL_AMS_TRAY_NOW, updateTrayLoaded(ams.trayNow()));
                    updateState(CHANNEL_AMS_TRAY_PREVIOUS, updateTrayLoaded(ams.trayPre()));
                });
        Optional.of(print)//
                .map(Report.Print::ams)//
                .map(Report.Print.Ams::ams)//
                .stream()//
                .flatMap(Collection::stream)//
                .forEach(this::updateAms);
        // vtray
        Optional.of(print)//
                .map(Report.Print::vtTray)//
                .ifPresent(this::updateVtray);
    }

    private void updateVtray(Report.Print.VtTray vtTray) {
        Optional.ofNullable(vtTray.trayType())//
                .map(Object::toString)//
                .flatMap(TrayType::findTrayType)//
                .map(Enum::name)//
                .map(value -> (State) StringType.valueOf(value))//
                .or(undef())//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_TYPE, trayType));
        Optional.ofNullable(vtTray.trayColor())//
                .map(Object::toString)//
                .filter(value -> value.length() >= 6)//
                .map(PrinterHandler::parseColor)//
                .or(undef())//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_COLOR, trayType));
        parseTemperatureType(vtTray.nozzleTempMax())
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_NOZZLE_TEMPERATURE_MAX, trayType));
        parseTemperatureType(vtTray.nozzleTempMin())
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_NOZZLE_TEMPERATURE_MIN, trayType));
        Optional.ofNullable(vtTray.remain())//
                .map(value -> (State) new PercentType(value))//
                .or(undef())//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_REMAIN, trayType));
        parseDecimalType(vtTray.k()).ifPresent(trayType -> updateState(CHANNEL_VTRAY_K, trayType));
        parseDecimalType(vtTray.n()).ifPresent(trayType -> updateState(CHANNEL_VTRAY_N, trayType));
        parseStringType(vtTray.tagUid()).ifPresent(trayType -> updateState(CHANNEL_VTRAY_TAG_UUID, trayType));
        parseStringType(vtTray.trayIdName()).ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_ID_NAME, trayType));
        parseStringType(vtTray.trayInfoIdx()).ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_INFO_IDX, trayType));
        parseStringType(vtTray.traySubBrands())
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_SUB_BRANDS, trayType));
        parseDecimalType(vtTray.trayWeight()).ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_WEIGHT, trayType));
        parseDecimalType(vtTray.trayDiameter())
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_DIAMETER, trayType));
        parseTemperatureType(vtTray.trayTemp())
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_TEMPERATURE, trayType));
        parseDecimalType(vtTray.trayTime()).ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_TIME, trayType));
        parseStringType(vtTray.bedTempType())
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_BED_TEMPERATURE_TYPE, trayType));
        parseTemperatureType(vtTray.bedTemp())
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_BED_TEMPERATURE, trayType));
    }

    private static State parseColor(String colorHex) {
        int r = parseInt(colorHex.substring(0, 2), 16);
        int g = parseInt(colorHex.substring(2, 4), 16);
        int b = parseInt(colorHex.substring(4, 6), 16);
        return ColorUtil.rgbToHsb(new int[]{r, g, b});
    }

    private Optional<State> parseDecimalType(@Nullable Number number) {
        return Optional.ofNullable(number).map(DecimalType::new);
    }

    private Optional<State> parseStringType(@Nullable String string) {
        return Optional.ofNullable(string).map(StringType::valueOf);
    }

    private void updateAms(Map<String, Object> ams) {
        var someAmsChannel = Optional.of(ams)//
                .map(map -> map.get("id"))//
                .map(Object::toString)//
                .map(Integer::parseInt)//
                .filter(id -> id >= MIN_AMS)//
                .filter(id -> id < MAX_AMS)//
                .map(AmsChannel::new);
        if (someAmsChannel.isEmpty()) {
            return;
        }
        var amsChannel = someAmsChannel.get();
        Optional.of(ams)//
                .map(map -> map.get("tray"))//
                .filter(obj -> obj instanceof Collection<?>)//
                .map(obj -> (Collection<?>) obj)//
                .stream()//
                .flatMap(Collection::stream)//
                .filter(obj -> obj instanceof Map<?, ?>)//
                .map(obj -> (Map<?, ?>) obj)//
                .forEach(map -> updateAmsTray(amsChannel, map));
    }

    private void updateAmsTray(AmsChannel amsChannel, Map<?, ?> map) {
        var someId = findKey(map, "id")//
                .map(Object::toString)//
                .map(Integer::parseInt);
        if (someId.isEmpty()) {
            logger.warn("There is no tray ID in {}", map);
            return;
        }
        int trayId = someId.get();
        if (trayId > MAX_AMS_TRAYS) {
            logger.warn("Tray ID needs to be lower that {}. Was {}", MAX_AMS_TRAYS, trayId);
            return;
        }

        findKey(map, "tray_type")//
                .map(Object::toString)//
                .flatMap(TrayType::findTrayType)//
                .map(Enum::name)//
                .map(value -> (State) StringType.valueOf(value))//
                .or(undef())//
                .ifPresent(trayType -> updateState(amsChannel.getTrayTypeChannel(trayId), trayType));
        findKey(map, "tray_color")//
                .map(Object::toString)//
                .filter(value -> value.length() >= 6)//
                .map(PrinterHandler::parseColor)//
                .or(undef())//
                .ifPresent(trayType -> updateState(amsChannel.getTrayColorChannel(trayId), trayType));
        parseTemperatureType(map, "nozzle_temp_max")
                .ifPresent(trayType -> updateState(amsChannel.getNozzleTemperatureMaxChannel(trayId), trayType));
        parseTemperatureType(map, "nozzle_temp_min")
                .ifPresent(trayType -> updateState(amsChannel.getNozzleTemperatureMinChannel(trayId), trayType));
        findKey(map, "remain")//
                .map(Object::toString)//
                .map(value -> (State) PercentType.valueOf(value))//
                .or(undef())//
                .ifPresent(trayType -> updateState(amsChannel.getRemainChannel(trayId), trayType));
        parseDecimalType(map, "k").ifPresent(trayType -> updateState(amsChannel.getKChannel(trayId), trayType));
        parseDecimalType(map, "n").ifPresent(trayType -> updateState(amsChannel.getNChannel(trayId), trayType));
        parseStringType(map, "tag_uuid")
                .ifPresent(trayType -> updateState(amsChannel.getTagUuidChannel(trayId), trayType));
        parseStringType(map, "tray_id_name")
                .ifPresent(trayType -> updateState(amsChannel.getTrayIdNameChannel(trayId), trayType));
        parseStringType(map, "tray_info_idx")
                .ifPresent(trayType -> updateState(amsChannel.getTrayInfoIdxChannel(trayId), trayType));
        parseStringType(map, "tray_sub_brands")
                .ifPresent(trayType -> updateState(amsChannel.getTraySubBrandsChannel(trayId), trayType));
        parseDecimalType(map, "tray_weight")
                .ifPresent(trayType -> updateState(amsChannel.getTrayWeightChannel(trayId), trayType));
        parseDecimalType(map, "tray_diameter")
                .ifPresent(trayType -> updateState(amsChannel.getTrayDiameterChannel(trayId), trayType));
        parseTemperatureType(map, "tray_temp")
                .ifPresent(trayType -> updateState(amsChannel.getTrayTemperatureChannel(trayId), trayType));
        parseDecimalType(map, "tray_time")
                .ifPresent(trayType -> updateState(amsChannel.getTrayTimeChannel(trayId), trayType));
        parseStringType(map, "bed_temp_type")
                .ifPresent(trayType -> updateState(amsChannel.getBedTemperatureTypeChannel(trayId), trayType));
        parseTemperatureType(map, "bed_temp")
                .ifPresent(trayType -> updateState(amsChannel.getBedTemperatureChannel(trayId), trayType));
        parseDecimalType(map, "ctype").ifPresent(trayType -> updateState(amsChannel.getCtypeChannel(trayId), trayType));
    }

    private Optional<State> parseTemperatureType(Map<?, ?> map, String key) {
        return findKey(map, key)//
                .map(Object::toString)//
                .flatMap(this::parseTemperatureType)//
                .or(undef());
    }

    private Optional<State> parseTemperatureType(String value) {
        try {
            var d = Double.parseDouble(value);
            return Optional.of(new QuantityType<>(d, CELSIUS));
        } catch (NumberFormatException ex) {
            logger.debug("Cannot parse: {}", value, ex);
            return Optional.empty();
        }
    }

    private Optional<State> parseDecimalType(Map<?, ?> map, String key) {
        return findKey(map, key)//
                .map(Object::toString)//
                .flatMap(this::parseDecimalType)//
                .or(undef());
    }

    private Optional<State> parseDecimalType(String value) {
        try {
            return Optional.of(DecimalType.valueOf(value));
        } catch (NumberFormatException ex) {
            logger.debug("Cannot parse {}", value, ex);
            return Optional.empty();
        }
    }

    private Optional<State> parseStringType(Map<?, ?> map, String key) {
        return findKey(map, key)//
                .map(Object::toString)//
                .map(value -> (State) StringType.valueOf(value))//
                .or(undef());
    }

    private static Supplier<Optional<? extends State>> undef() {
        return () -> Optional.of(UNDEF);
    }

    private static Optional<?> findKey(Map<?, ?> map, String key) {
        return Optional.of(map).map(m -> m.get(key));
    }

    private void updateCelsiusState(BambuLabBindingConstants.Channel channelId, @Nullable Double temperature) {
        if (temperature == null) {
            return;
        }
        updateState(channelId, new QuantityType<>(temperature, CELSIUS));
    }

    private void updateStringState(BambuLabBindingConstants.Channel channelId, @Nullable String string) {
        if (string == null) {
            return;
        }
        updateState(channelId, new StringType(string));
    }

    private void updateDecimalState(BambuLabBindingConstants.Channel channelId, @Nullable Number number) {
        if (number == null) {
            return;
        }
        updateState(channelId, new DecimalType(number));
    }

    private void updateDecimalState(BambuLabBindingConstants.Channel channelId, @Nullable String number) {
        if (number == null) {
            return;
        }
        try {
            var state = new DecimalType(Double.parseDouble(number));
            updateState(channelId, state);
        } catch (NumberFormatException e) {
            logger.debug("Cannot parse decimal number {}", number, e);
            updateState(channelId, UNDEF);
        }
    }

    private void updateBooleanState(BambuLabBindingConstants.Channel channelId, @Nullable Boolean bool) {
        if (bool == null) {
            return;
        }
        updateState(channelId, OnOffType.from(bool));
    }

    private void updatePercentState(BambuLabBindingConstants.Channel channelId, @Nullable Integer integer) {
        if (integer == null) {
            return;
        }
        updateState(channelId, new PercentType(integer));
    }

    private void updatePercentState(BambuLabBindingConstants.Channel channelId, @Nullable String integer) {
        if (integer == null) {
            return;
        }
        try {
            var state = new PercentType(integer);
            updateState(channelId, state);
        } catch (NumberFormatException e) {
            logger.debug("Cannot parse percent number {}", integer, e);
            updateState(channelId, UNDEF);
        }
    }

    private void updateLightState(String lightName, BambuLabBindingConstants.Channel channel,
                                  @Nullable List<Map<String, String>> lights) {
        Optional.ofNullable(lights)//
                .stream()//
                .flatMap(Collection::stream)//
                .filter(map -> lightName.equalsIgnoreCase(map.get("node")))//
                .map(map -> map.get("mode"))//
                .filter(Objects::nonNull)//
                .map(OnOffType::from)//
                .findAny()//
                .ifPresent(command -> updateState(channel, command));
    }

    private State parseWifiChannel(String wifi) {
        var matcher = DBM_PATTERN.matcher(wifi);
        if (!matcher.matches()) {
            return UNDEF;
        }

        var integer = matcher.group(1);
        try {
            var value = parseInt(integer);
            return new QuantityType<>(value, DECIBEL_MILLIWATTS);
        } catch (NumberFormatException e) {
            logger.debug("Cannot parse integer {} from wifi {}", integer, wifi, e);
            return UNDEF;
        }
    }

    public void sendCommand(PrinterClient.Channel.Command command) {
        logger.debug("Sending command {}", command);
        var localClient = client;
        if (localClient == null) {
            logger.warn("Client not connected. Cannot send command {}", command);
            return;
        }
        try {
            localClient.getChannel().sendCommand(command);
        } catch (Exception e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    private void updateState(BambuLabBindingConstants.Channel channelID, State state) {
        updateState(channelID.getName(), state);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public void updateState(String channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
