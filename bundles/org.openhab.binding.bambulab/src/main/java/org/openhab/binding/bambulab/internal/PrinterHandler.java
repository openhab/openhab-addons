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

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel.*;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.Channel.*;
import static org.openhab.binding.bambulab.internal.StateParserHelper.*;
import static org.openhab.binding.bambulab.internal.TrayHelper.updateTrayLoaded;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatus.UNKNOWN;
import static org.openhab.core.thing.ThingStatusDetail.*;
import static org.openhab.core.types.UnDefType.UNDEF;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.LedNode.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PushingCommand.defaultPushingCommand;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClientConfig.requiredFields;

import java.net.URI;
import java.util.ArrayList;
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.grzeslowski.jbambuapi.mqtt.ConnectionCallback;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.GCodeFileCommand;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PrintSpeedCommand;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClientConfig;
import pl.grzeslowski.jbambuapi.mqtt.PrinterWatcher;
import pl.grzeslowski.jbambuapi.mqtt.Report;

/**
 * The {@link PrinterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class PrinterHandler extends BaseBridgeHandler
        implements PrinterWatcher.StateSubscriber, BambuHandler, ConnectionCallback {
    private static final String INTERNAL_COMMAND_PREFIX = ">";
    private Logger logger = LoggerFactory.getLogger(PrinterHandler.class);

    private @Nullable PrinterClient client;
    private @Nullable Camera camera;
    private final AtomicInteger reconnectTimes = new AtomicInteger();
    private final AtomicReference<@Nullable ScheduledFuture<?>> reconnectSchedule = new AtomicReference<>();
    private final PrinterWatcher printerWatcher = new PrinterWatcher();
    private @Nullable PrinterClientConfig config;
    private final Collection<AmsDeviceHandlerFactory> amses = synchronizedList(new ArrayList<>());
    private final AtomicReference<@Nullable Report> latestPrinterState = new AtomicReference<>();

    public PrinterHandler(Bridge bridge) {
        super(bridge);
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
        } else if (CHANNEL_SPEED_LEVEL.is(channelUID) && command instanceof StringType) {
            stream(PrintSpeedCommand.values())//
                    .filter(type -> type.name().equalsIgnoreCase(command.toString()))//
                    .findAny()//
                    .ifPresent(this::sendCommand);
        } else if (CHANNEL_CAMERA_RECORD.is(channelUID) && command instanceof OnOffType onOffCommand) {
            requireNonNull(camera).handleCommand(onOffCommand);
        } else if (CHANNEL_COMMAND.is(channelUID) && command instanceof StringType) {
            var commandString = command.toString();
            if (commandString.startsWith(INTERNAL_COMMAND_PREFIX)) {
                logger.debug("Command {} updates just the state, ignoring...", commandString);
                return;
            }
            String result;
            try {
                sendCommand(commandString);
                result = INTERNAL_COMMAND_PREFIX + " SUCCESS: " + commandString;
            } catch (Exception ex) {
                result = INTERNAL_COMMAND_PREFIX + " ERROR: " + ex.getLocalizedMessage();
            }
            updateState(channelUID, StringType.valueOf(result));
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

        // turn off the command channel
        updateState(CHANNEL_COMMAND, StringType.valueOf(INTERNAL_COMMAND_PREFIX + " Ready to accept commands"));

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
        var message = throwable != null ? throwable.getLocalizedMessage() : "<no message" + INTERNAL_COMMAND_PREFIX;
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
        var currentReconnections = reconnectTimes.getAndAdd(1);
        if (currentReconnections >= configNotNull.reconnectMax) {
            logger.warn("Do not reconnecting any more, because max reconnections was reach!");
            return;
        }
        logger.debug("Will reconnect in {} seconds...", configNotNull.reconnectTime);
        try {
            reconnectSchedule.set(//
                    scheduler.schedule(() -> {
                        logger.debug("Reconnecting...");
                        reconnectSchedule.set(null);
                        dispose();
                        initialize();
                    }, configNotNull.reconnectTime, SECONDS));
        } catch (RejectedExecutionException ex) {
            logger.debug("Task was rejected", ex);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, ex.getLocalizedMessage());
        }
    }

    private PrinterClient buildLocalClient(URI uri, PrinterConfiguration config) throws InitializationException {
        try {
            var localConfig = this.config = requiredFields(uri, config.username, config.serial,
                    config.accessCode.toCharArray());
            return new PrinterClient(localConfig);
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
            latestPrinterState.set(null);
            printerWatcher.close();
            amses.clear();
            closeConfig();
            closeReconnectSchedule();
            closeCamera();
            closeClient();
        } finally {
            logger = LoggerFactory.getLogger(PrinterHandler.class);
        }
    }

    private void closeConfig() {
        var localConfig = config;
        config = null;
        if (localConfig != null) {
            localConfig.close();
        }
    }

    private void closeClient() {
        var localClient = client;
        client = null;
        if (localClient != null) {
            try {
                localClient.close();
            } catch (Exception e) {
                logger.warn("Could not correctly dispose PrinterClient", e);
            }
        }
    }

    private void closeCamera() {
        var localCamera = camera;
        camera = null;
        if (localCamera != null) {
            localCamera.close();
        }
    }

    private void closeReconnectSchedule() {
        var localReconnectSchedule = reconnectSchedule.get();
        reconnectSchedule.set(null);
        if (localReconnectSchedule != null) {
            localReconnectSchedule.cancel(true);
        }
    }

    @Override
    public void newState(@Nullable Report delta, @Nullable Report fullState) {
        logger.trace("New Printer state from delta {}", delta);
        if (fullState != null) {
            latestPrinterState.set(fullState);
        }
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
        parseTimeMinutes(print.mcRemainingTime())//
                .ifPresent(time -> updateState(CHANNEL_MC_REMAINING_TIME, time));
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
                .flatMap(StateParserHelper::parseStringType)//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_TYPE, trayType));
        Optional.ofNullable(vtTray.trayColor())//
                .map(Object::toString)//
                .map(StateParserHelper::parseColor)//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_COLOR, trayType));
        parseTemperatureType(vtTray.nozzleTempMax())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_NOZZLE_TEMPERATURE_MAX, trayType));
        parseTemperatureType(vtTray.nozzleTempMin())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_NOZZLE_TEMPERATURE_MIN, trayType));
        Optional.ofNullable(vtTray.remain())//
                .flatMap(StateParserHelper::parsePercentType)//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_REMAIN, trayType));
        parseDecimalType(vtTray.k())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_K, trayType));
        parseDecimalType(vtTray.n())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_N, trayType));
        parseStringType(vtTray.tagUid())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TAG_UUID, trayType));
        parseStringType(vtTray.trayIdName())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_ID_NAME, trayType));
        parseStringType(vtTray.trayInfoIdx())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_INFO_IDX, trayType));
        parseStringType(vtTray.traySubBrands())
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_SUB_BRANDS, trayType));
        parseDecimalType(vtTray.trayWeight())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_WEIGHT, trayType));
        parseDecimalType(vtTray.trayDiameter())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_DIAMETER, trayType));
        parseTemperatureType(vtTray.trayTemp())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_TEMPERATURE, trayType));
        parseDecimalType(vtTray.trayTime())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_TRAY_TIME, trayType));
        parseStringType(vtTray.bedTempType())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_BED_TEMPERATURE_TYPE, trayType));
        parseTemperatureType(vtTray.bedTemp())//
                .or(StateParserHelper::undef)//
                .ifPresent(trayType -> updateState(CHANNEL_VTRAY_BED_TEMPERATURE, trayType));
    }

    private void updateAms(Map<String, Object> amsMap) {
        Optional.of(amsMap)//
                .map(map -> map.get("id"))//
                .map(Object::toString)//
                .map(Integer::parseInt)//
                // in code, we are using 1-4 ordering; in API 0-3 ordering is used
                .map(id -> id + 1) //
                .filter(id -> id >= MIN_AMS)//
                .filter(id -> id <= MAX_AMS)//
                .map(id -> amses.stream().filter(ams -> ams.getAmsNumber() == id))//
                .stream()//
                .flatMap(identity())//
                .forEach(ams -> ams.updateAms(amsMap));
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
        parsePercentType(integer).ifPresent(state -> updateState(channelId, state));
    }

    private void updatePercentState(BambuLabBindingConstants.Channel channelId, @Nullable String integer) {
        parsePercentType(integer).ifPresent(state -> updateState(channelId, state));
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

    public void sendCommand(String command) {
        sendCommand(CommandParser.parseCommand(command));
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

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (!(childHandler instanceof AmsDeviceHandlerFactory ams)) {
            return;
        }
        amses.add(ams);
        Optional.of(latestPrinterState)//
                .map(AtomicReference::get)//
                .map(Report::print).map(Report.Print::ams)//
                .map(Report.Print.Ams::ams)//
                .stream()//
                .flatMap(Collection::stream)//
                .filter(map -> map.containsKey("id"))//
                .filter(map -> map.get("id") != null)//
                // in code, we are using 1-4 ordering; in API 0-3 ordering is used
                .filter(map -> parseInt(map.get("id").toString()) + 1 == ams.getAmsNumber())//
                .forEach(ams::updateAms);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (!(childHandler instanceof AmsDeviceHandlerFactory ams)) {
            return;
        }
        var removed = amses.remove(ams);
        if (!removed) {
            logger.warn("Did not remove ams handler {}", ams);
        }
    }

    public String getSerialNumber() {
        return requireNonNull(config).serial();
    }
}
