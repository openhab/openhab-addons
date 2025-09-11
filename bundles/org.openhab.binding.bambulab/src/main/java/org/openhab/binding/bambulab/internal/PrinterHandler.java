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
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.stream;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static org.openhab.binding.bambulab.internal.BambuApiException.findSerial;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel.*;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.PrinterChannel.*;
import static org.openhab.binding.bambulab.internal.PrinterConfiguration.CLOUD_MODE_HOSTNAME;
import static org.openhab.binding.bambulab.internal.StateParserHelper.*;
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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.bambulab.internal.BambuLabBindingConstants.PrinterChannel;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
    private static final String LOGIN_URL = "https://api.bambulab.com/v1/user-service/user/login";
    private static final String ACCESS_CODE_VALID_TILL_PROPERTY = "accessCodeValidTill";
    private final HttpClient httpClient;
    private final Gson jsonMapper;
    private Logger logger = LoggerFactory.getLogger(PrinterHandler.class);

    private @Nullable PrinterClient client;
    private @Nullable Camera camera;
    private final AtomicInteger reconnectTimes = new AtomicInteger();
    private final AtomicReference<@Nullable ScheduledFuture<?>> reconnectSchedule = new AtomicReference<>();
    private final PrinterWatcher printerWatcher = new PrinterWatcher();
    private @Nullable PrinterClientConfig config;
    private final Collection<AmsDeviceHandler> amses = synchronizedList(new ArrayList<>());
    private final AtomicReference<@Nullable Report> latestPrinterState = new AtomicReference<>();
    private @Nullable ScheduledFuture<?> validateAccessCodeSchedule;

    public PrinterHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        if (this.httpClient.isStopped()) {
            throw new IllegalStateException("HttpClient is stopped");
        }
        this.jsonMapper = new Gson();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            Optional.of(latestPrinterState)//
                    .map(AtomicReference::get)//
                    .map(Report::print)//
                    .ifPresent(printer -> updateState(channelUID, printer));
        } else if (CHANNEL_LED_CHAMBER_LIGHT.is(channelUID) || CHANNEL_LED_WORK_LIGHT.is(channelUID)) {
            var ledNode = CHANNEL_LED_CHAMBER_LIGHT.is(channelUID) ? CHAMBER_LIGHT : WORK_LIGHT;
            var bambuCommand = "ON".equals(command.toFullString()) ? on(ledNode) : off(ledNode);
            sendCommand(bambuCommand);
        } else if (CHANNEL_GCODE_FILE.is(channelUID)) {
            var bambuCommand = new GCodeFileCommand(command.toString());
            sendCommand(bambuCommand);
        } else if (CHANNEL_SPEED_LEVEL.is(channelUID) && command instanceof StringType) {
            var bambuCommand = PrintSpeedCommand.findByName(command.toString());
            if (bambuCommand.canSend()) {
                sendCommand(bambuCommand);
            } else {
                logger.warn("Cannot send command: {}", bambuCommand);
            }
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

    private void updateState(ChannelUID channelUid, Report.Print print) {
        var someChannel = findChannel(channelUid);
        if (someChannel.isEmpty()) {
            logger.warn("Cannot find channel for {}", channelUid);
            return;
        }
        var channel = someChannel.get();
        updateState(channel, print);
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
        validateAccessCode();

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

    private void validateAccessCode() throws InitializationException {
        var validTill = getThing().getProperties().getOrDefault(ACCESS_CODE_VALID_TILL_PROPERTY, "");
        if (validTill.isEmpty()) {
            return;
        }
        try {
            var parse = LocalDateTime.parse(validTill);
            var now = now();
            if (parse.isBefore(now)) {
                throw new InitializationException(CONFIGURATION_ERROR, "@text/printer.handler.init.accessCodeExpired");
            }
            var duration = between(now.toInstant(UTC), parse.toInstant(UTC));
            try {
                validateAccessCodeSchedule = scheduler
                        .schedule(
                                () -> updateStatus(OFFLINE, CONFIGURATION_ERROR,
                                        "@text/printer.handler.init.accessCodeExpired"),
                                duration.getSeconds(), SECONDS);
            } catch (RejectedExecutionException ex) {
                logger.debug("Task was rejected", ex);
                throw new InitializationException(CONFIGURATION_ERROR, ex);
            }
        } catch (DateTimeParseException e) {
            logger.debug("Invalid access code till date: {}", validTill, e);
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
            closeValidateAccessCodeSchedule();
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

    private void closeValidateAccessCodeSchedule() {
        var validateAccessCodeSchedule = this.validateAccessCodeSchedule;
        this.validateAccessCodeSchedule = null;
        if (validateAccessCodeSchedule != null) {
            validateAccessCodeSchedule.cancel(true);
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
        var print = delta.print();
        if (print == null) {
            return;
        }
        stream(PrinterChannel.values()).forEach(channel -> updateState(channel, print));

        Optional.of(print)//
                .map(Report.Print::ams)//
                .map(Report.Print.Ams::ams)//
                .stream()//
                .flatMap(Collection::stream)//
                .forEach(this::updateAms);

        // if got new Printer state (and not failed) then make sure that thing status in ONLINE
        updateStatus(ONLINE);
    }

    private void updateState(PrinterChannel channel, Report.Print print) {
        var vtray = Optional.of(print).map(Report.Print::vtTray);
        Optional<State> state = switch (channel) {
            // temper
            case CHANNEL_NOZZLE_TEMPERATURE -> parseTemperatureType(print.nozzleTemper());
            case CHANNEL_NOZZLE_TARGET_TEMPERATURE -> parseTemperatureType(print.nozzleTargetTemper());
            case CHANNEL_BED_TEMPERATURE -> parseTemperatureType(print.bedTemper());
            case CHANNEL_BED_TARGET_TEMPERATURE -> parseTemperatureType(print.bedTargetTemper());
            case CHANNEL_CHAMBER_TEMPERATURE -> parseTemperatureType(print.chamberTemper());
            // string
            case CHANNEL_MC_PRINT_STAGE -> parseStringType(print.mcPrintStage());
            case CHANNEL_BED_TYPE -> parseStringType(print.bedType());
            case CHANNEL_GCODE_FILE -> parseStringType(print.gcodeFile());
            case CHANNEL_GCODE_STATE -> parseStringType(print.gcodeState());
            case CHANNEL_REASON -> parseStringType(print.reason());
            case CHANNEL_RESULT -> parseStringType(print.result());
            // percents
            case CHANNEL_MC_PERCENT -> parsePercentType(print.mcPercent());
            case CHANNEL_GCODE_FILE_PREPARE_PERCENT -> parsePercentType(print.gcodeFilePreparePercent());
            // time
            case CHANNEL_MC_REMAINING_TIME -> parseTimeMinutes(print.mcRemainingTime());
            // wifi
            case CHANNEL_WIFI_SIGNAL -> parseWifiChannel(print.wifiSignal());
            // decimal
            case CHANNEL_BIG_FAN_1_SPEED -> parseDecimalType(print.bigFan1Speed());
            case CHANNEL_BIG_FAN_2_SPEED -> parseDecimalType(print.bigFan2Speed());
            case CHANNEL_HEAT_BREAK_FAN_SPEED -> parseDecimalType(print.heatbreakFanSpeed());
            case CHANNEL_LAYER_NUM -> parseDecimalType(print.layerNum());
            // boolean
            case CHANNEL_TIME_LAPS -> parseOnOffType(print.timelapse());
            case CHANNEL_USE_AMS -> parseOnOffType(print.useAms());
            case CHANNEL_VIBRATION_CALIBRATION -> parseOnOffType(print.vibrationCali());
            // lights
            case CHANNEL_LED_CHAMBER_LIGHT -> parseChamberLightType(print.lightsReport());
            case CHANNEL_LED_WORK_LIGHT -> parseWorkLightType(print.lightsReport());
            // vtray
            case CHANNEL_VTRAY_TRAY_TYPE -> //
                vtray.map(Report.Print.VtTray::trayType)//
                        .flatMap(StateParserHelper::parseTrayType);
            case CHANNEL_VTRAY_TRAY_COLOR -> //
                vtray.map(Report.Print.VtTray::trayColor)//
                        .map(StateParserHelper::parseColor);
            case CHANNEL_VTRAY_NOZZLE_TEMPERATURE_MAX -> //
                vtray.map(Report.Print.VtTray::nozzleTempMax)//
                        .flatMap(StateParserHelper::parseTemperatureType);
            case CHANNEL_VTRAY_NOZZLE_TEMPERATURE_MIN -> //
                vtray.map(Report.Print.VtTray::nozzleTempMin)//
                        .flatMap(StateParserHelper::parseTemperatureType);
            case CHANNEL_VTRAY_REMAIN -> //
                vtray.map(Report.Print.VtTray::remain)//
                        .flatMap(StateParserHelper::parsePercentType);
            case CHANNEL_VTRAY_K -> //
                vtray.map(Report.Print.VtTray::k)//
                        .flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_VTRAY_N -> //
                vtray.map(Report.Print.VtTray::n)//
                        .flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_VTRAY_TAG_UUID -> //
                vtray.map(Report.Print.VtTray::trayUuid)//
                        .flatMap(StateParserHelper::parseStringType);
            case CHANNEL_VTRAY_TRAY_ID_NAME -> //
                vtray.map(Report.Print.VtTray::trayIdName)//
                        .flatMap(StateParserHelper::parseStringType);
            case CHANNEL_VTRAY_TRAY_INFO_IDX -> //
                vtray.map(Report.Print.VtTray::trayInfoIdx)//
                        .flatMap(StateParserHelper::parseStringType);
            case CHANNEL_VTRAY_TRAY_SUB_BRANDS -> //
                vtray.map(Report.Print.VtTray::traySubBrands)//
                        .flatMap(StateParserHelper::parseStringType);
            case CHANNEL_VTRAY_TRAY_WEIGHT -> //
                vtray.map(Report.Print.VtTray::trayWeight)//
                        .flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_VTRAY_TRAY_DIAMETER -> //
                vtray.map(Report.Print.VtTray::trayDiameter)//
                        .flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_VTRAY_TRAY_TEMPERATURE -> //
                vtray.map(Report.Print.VtTray::trayTemp)//
                        .flatMap(StateParserHelper::parseTemperatureType);
            case CHANNEL_VTRAY_TRAY_TIME -> //
                vtray.map(Report.Print.VtTray::trayTime)//
                        .flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_VTRAY_BED_TEMPERATURE_TYPE -> //
                vtray.map(Report.Print.VtTray::bedTempType)//
                        .flatMap(StateParserHelper::parseStringType);
            case CHANNEL_VTRAY_BED_TEMPERATURE -> //
                vtray.map(Report.Print.VtTray::bedTemp)//
                        .flatMap(StateParserHelper::parseTemperatureType);
            // ams
            case CHANNEL_AMS_TRAY_NOW -> //
                Optional.of(print)//
                        .map(Report.Print::ams)//
                        .map(Report.Print.Ams::trayNow)//
                        .flatMap(TrayHelper::findStateForTrayLoaded);
            case CHANNEL_AMS_TRAY_PREVIOUS -> //
                Optional.of(print)//
                        .map(Report.Print::ams)//
                        .map(Report.Print.Ams::trayPre)//
                        .flatMap(TrayHelper::findStateForTrayLoaded);
            // misc
            case CHANNEL_SPEED_LEVEL -> parseSpeedLevel(print.spdLvl());
            case CHANNEL_END_DATE -> //
                Optional.ofNullable(print.mcRemainingTime())//
                        .map(time -> ZonedDateTime.now().plusMinutes(time))//
                        .map(StateParserHelper::parseDateTimeType);
            // surrogate channels
            case CHANNEL_COMMAND -> Optional.empty();
            case CHANNEL_CAMERA_IMAGE -> Optional.empty();
            case CHANNEL_CAMERA_RECORD -> Optional.empty();
        };
        state.ifPresent(s -> updateState(channel, s));
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

    private void updateState(PrinterChannel channelID, State state) {
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
        if (!(childHandler instanceof AmsDeviceHandler ams)) {
            return;
        }
        amses.add(ams);
        findLatestAms(ams.getAmsNumber()).ifPresent(ams::updateAms);
    }

    public Optional<Map<String, Object>> findLatestAms(int amsNumber) {
        return Optional.of(latestPrinterState)//
                .map(AtomicReference::get)//
                .map(Report::print).map(Report.Print::ams)//
                .map(Report.Print.Ams::ams)//
                .stream()//
                .flatMap(Collection::stream)//
                .filter(map -> map.containsKey("id"))//
                .filter(map -> map.get("id") != null)//
                // in code, we are using 1-4 ordering; in API 0-3 ordering is used
                .filter(map -> parseInt(map.get("id").toString()) + 1 == amsNumber).findAny();
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (!(childHandler instanceof AmsDeviceHandler ams)) {
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

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(PrinterActions.class);
    }

    public void requestLoginCode(String username, String password) throws BambuApiException {
        var request = httpClient.POST(LOGIN_URL);
        request.timeout(3, SECONDS);
        request.accept("application/json");
        request.content(new StringContentProvider("application/json",
                jsonMapper.toJson(new BambuPasswordRequest(username, password)), UTF_8));
        try {
            var response = request.send();
            var status = response.getStatus();
            var content = response.getContentAsString();
            if (status != 200) {
                // error
                logger.debug("There was an error while trying to request login code, status={}", status);
                var message = parseErrorMessage(content);
                throw new BambuApiException(findSerial(config), message);
            }
            try {
                var bambu = jsonMapper.fromJson(content, BambuResponse.class);
                if (!"verifyCode".equalsIgnoreCase(bambu.loginType)) {
                    throw new BambuApiException(findSerial(config),
                            "Invalid login code (`%s`)".formatted(bambu.loginType));
                }
            } catch (JsonSyntaxException e) {
                throw new BambuApiException(findSerial(config), "Cannot parse to BambuResponse: " + content, e);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new BambuApiException(findSerial(config), "HTTP Exception", e);
        }
    }

    public String requestAccessCode(String username, String code) throws BambuApiException {
        var request = httpClient.POST(LOGIN_URL);
        request.timeout(3, SECONDS);
        request.accept("application/json");
        request.content(new StringContentProvider("application/json",
                jsonMapper.toJson(new BambuCodeRequest(username, code)), UTF_8));
        try {
            var response = request.send();
            var status = response.getStatus();
            var nullableContent = response.getContentAsString();
            var content = nullableContent != null ? nullableContent : "<empty>";
            if (status != 200) {
                // error
                logger.debug("There was an error while trying to request access code, status={}", status);
                var message = parseErrorMessage(content);
                throw new BambuApiException(findSerial(config), message);
            }
            try {
                var bambu = jsonMapper.fromJson(content, BambuResponse.class);
                if (bambu.accessToken == null || bambu.accessToken.isBlank()) {
                    throw new BambuApiException(findSerial(config), "Access token not found in response: " + content);
                }
                {
                    var configuration = editConfiguration();
                    configuration.put("accessCode", bambu.accessToken);
                    configuration.put("hostname", CLOUD_MODE_HOSTNAME);
                    updateConfiguration(configuration);
                }
                {
                    var now = now();
                    var thing = editThing()
                            .withProperty(ACCESS_CODE_VALID_TILL_PROPERTY, now.plusSeconds(bambu.expiresIn).toString())
                            .build();
                    updateThing(thing);
                }
                var message = "Access code was set. Please visit https://makerworld.com/api/v1/design-user-service/my/preference to get username (remember to add `u_` prefix).";
                logger.info(message);
                return message;
            } catch (JsonSyntaxException e) {
                throw new BambuApiException(findSerial(config), "Cannot parse to BambuResponse: " + content, e);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new BambuApiException(findSerial(config), "HTTP Exception", e);
        }
    }

    private String parseErrorMessage(String content) {
        try {
            var error = jsonMapper.fromJson(content, BambuError.class);
            return Objects.requireNonNullElse(error.error, content);
        } catch (JsonSyntaxException e) {
            logger.debug("Cannot parse to BambuError: {}", content, e);
            return content;
        }
    }

    private record BambuResponse(@Nullable String accessToken, int expiresIn, @Nullable String loginType) {
    }

    private record BambuError(int code, @Nullable String error) {
    }

    private record BambuPasswordRequest(@Nullable String account, @Nullable String password) {
    }

    private record BambuCodeRequest(@Nullable String account, @Nullable String code) {
    }
}
