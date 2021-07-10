/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.moonraker.internal;

import static org.openhab.binding.moonraker.internal.MoonrakerBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.CloseException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import tec.uom.se.unit.Units;

/**
 * The {@link MoonrakerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class MoonrakerHandler extends BaseThingHandler implements EventListener {

    private final Logger logger = LoggerFactory.getLogger(MoonrakerHandler.class);

    private @Nullable MoonrakerConfiguration config;
    private @Nullable MoonrakerWebSocket webSocket;
    private @Nullable ScheduledFuture<?> reinitJob;

    /* last refresh time */
    private Instant refreshTime = Instant.now();

    /* state variables */
    private @Nullable String klippy_state = null;
    private @Nullable String klippy_state_message = null;
    private @Nullable String webhooks_state = null;
    private @Nullable String webhooks_state_message = null;
    private @Nullable String print_stats_state = null;
    private @Nullable String print_stats_state_message = null;
    private @Nullable String idle_timeout_state = null;
    private @Nullable String display_status_message = null;
    private int display_status_progress = 0;

    public MoonrakerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Moonraker Handler...");

        config = getConfigAs(MoonrakerConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(this::startWebSocket);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Moonraker handler '{}'", getThing().getUID().getId());
        cancelReinitJob();
        if (webSocket != null) {
            webSocket.stop();
            webSocket = null;
        }

        super.dispose();
        logger.debug("Moonraker handler shut down.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {}, {}", channelUID, command);

        if (command instanceof RefreshType
                && Instant.now().minusSeconds(CHANNEL_REFRESH_INTERVAL_SECONDS).isAfter(refreshTime)) {
            getStartupInfo();
            refreshTime = Instant.now();
        } else if (channelUID.getIdWithoutGroup().equals("emergency_stop")
                || channelUID.getIdWithoutGroup().equals("firmware_restart")
                || channelUID.getIdWithoutGroup().equals("restart")) {
            if (command.equals(OnOffType.ON)) {
                webSocket.sendRequest("printer." + channelUID.getIdWithoutGroup());
                new java.util.Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateState(channelUID, OnOffType.OFF);
                    }
                }, 100);
            }
        } else if (channelUID.getIdWithoutGroup().equals("server_restart")) {
            if (command.equals(OnOffType.ON)) {
                webSocket.sendRequest("server." + channelUID.getIdWithoutGroup());
                new java.util.Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        updateState(channelUID, OnOffType.OFF);
                    }
                }, 100);
            }
        } else if (channelUID.getIdWithoutGroup().equals("pause") || channelUID.getIdWithoutGroup().equals("resume")
                || channelUID.getIdWithoutGroup().equals("cancel")) {
            if (command.equals(OnOffType.ON)) {
                webSocket.sendRequest("printer.print." + channelUID.getIdWithoutGroup());
                new java.util.Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateState(channelUID, OnOffType.OFF);
                    }
                }, 100);
            }
        } else if (channelUID.getIdWithoutGroup().equals("gcode_command") && command instanceof StringType) {

            JsonObject object = new JsonObject();
            object.addProperty("script", command.toString());
            webSocket.sendRequest("printer.gcode.script", object);
        } else if (channelUID.getIdWithoutGroup().equals("gcodeprint_file_command") && command instanceof StringType) {
            JsonObject object = new JsonObject();
            object.addProperty("filename", command.toString());
            webSocket.sendRequest("printer.print.start", object);
        } else if (channelUID.getGroupId().startsWith("power_device")
                && channelUID.getIdWithoutGroup().equals("switch")) {
            String device = channelUID.getGroupId().substring("power_device__".length());

            JsonObject object = new JsonObject();
            object.add(device, JsonNull.INSTANCE);
            if (command.equals(OnOffType.ON)) {
                webSocket.sendRequest("machine.device_power.on", object);
            } else if (command.equals(OnOffType.OFF)) {
                webSocket.sendRequest("machine.device_power.off", object);
            }
        }
    }

    /**
     * Start the websocket connection for receiving permanent update
     * {@link RPCResponse}s from the Moonraker API.
     */
    private void startWebSocket() {
        try {
            String token = null;
            if (config.apikey != null && config.apikey.trim().length() > 0) {
                logger.debug("Obtaining oneshot token.");

                URL url = new URI("http", null, config.host, config.port, "/access/oneshot_token", null, null).toURL();
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("X-Api-Key", config.apikey);
                connection.setConnectTimeout(WEBSOCKET_TIMEOUT_SECONDS * 1000);
                connection.connect();

                try (Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A")) {
                    token = new JsonParser().parse(scanner.next()).getAsJsonObject().getAsJsonPrimitive("result")
                            .getAsString();
                }
            }

            logger.debug("Starting Moonraker websocket.");
            MoonrakerWebSocket localWebSocket = new MoonrakerWebSocket(this, new URI("ws", null, config.host,
                    config.port, "/websocket", token == null ? null : "token=" + token, null),
                    WEBSOCKET_TIMEOUT_SECONDS * 1000);

            if (this.webSocket != null && this.webSocket.isRunning()) {
                this.webSocket.stop();
                this.webSocket = null;
            }

            this.webSocket = localWebSocket;
            localWebSocket.start();
            updateStatus(ThingStatus.ONLINE);
        } catch (final Exception e) { // Catch Exception because websocket start throws Exception
            handleClientException(e);
        }
    }

    /**
     * Checks if the job is already (re-)scheduled.
     * 
     * @param job job to check
     * @return true, when the job is already (re-)scheduled, otherwise false
     */
    private static boolean isAlreadyScheduled(ScheduledFuture<?> job) {
        return job.getDelay(TimeUnit.SECONDS) > 0;
    }

    /**
     * Cancel the running job
     */
    private synchronized void cancelReinitJob() {
        ScheduledFuture<?> reinitJob = this.reinitJob;

        if (reinitJob != null) {
            reinitJob.cancel(true);
            this.reinitJob = null;
        }
    }

    /**
     * Schedules a re-initialization in the given future.
     *
     * @param delayed when it is scheduled delayed, it starts with a delay of
     *            {@link org.openhab.binding.moonraker.internal.MoonRakerConstants#REINITIALIZE_DELAY_SECONDS}
     *            seconds, otherwise it starts directly
     */
    private synchronized void scheduleRestartClient(final boolean delayed) {
        if (!this.isInitialized() || this.getCallback() == null)
            return;

        @Nullable
        final ScheduledFuture<?> localReinitJob = reinitJob;

        if (localReinitJob != null && isAlreadyScheduled(localReinitJob)) {
            logger.debug("Scheduling reinitialize - ignored: already triggered in {} seconds.",
                    localReinitJob.getDelay(TimeUnit.SECONDS));
            return;
        }

        final long seconds = delayed ? REINITIALIZE_DELAY_SECONDS : 0;
        logger.debug("Scheduling reinitialize in {} seconds.", seconds);
        reinitJob = scheduler.schedule(this::startWebSocket, seconds, TimeUnit.SECONDS);
    }

    /**
     * Handles all Exceptions of the client communication. For minor "errors" like
     * an already existing session, it returns true to inform the binding to
     * continue running. In other cases it may e.g. schedule a reinitialization of
     * the binding.
     *
     * @param e the Exception
     * @return boolean true, if binding should continue.
     */
    private boolean handleClientException(final Exception e) {
        boolean isReinitialize = true;
        if (e instanceof IOException) {
            logger.debug("IO error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof TimeoutException
                || e instanceof CloseException && e.getCause() instanceof TimeoutException) {
            logger.debug("WebSocket timeout: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof SocketTimeoutException) {
            logger.debug("Socket timeout: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof InterruptedException) {
            isReinitialize = false;
            Thread.currentThread().interrupt();
        } else if (e instanceof ExecutionException) {
            logger.debug("ExecutionException: {}", getRootCauseMessage(e));
            updateStatus(ThingStatus.OFFLINE);
        } else {
            logger.debug("Unknown exception", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
        if (isReinitialize) {
            scheduleRestartClient(true);
            return true;
        }
        return false;
    }

    /**
     * Try to get startup info after
     * {@link org.openhab.binding.moonraker.internal.MoonRakerConstants#REINITIALIZE_DELAY_SECONDS}
     */
    private void requestStateDelayed() {
        scheduler.schedule(() -> {
            if (webSocket.isRunning()) {
                getStartupInfo();
            }
        }, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void onError(final Throwable cause) {
        if (cause instanceof Exception) {
            handleClientException((Exception) cause);
        } else {
            logger.warn("Unexpected throwable: {}", cause);
        }
    }

    @Override
    public void onConnect() {
        logger.debug("onConnect");
        getStartupInfo();
    }

    @Override
    public void connectionClosed() {
        logger.debug("connectiomClosed");
        scheduleRestartClient(true);
    }

    /**
     * Get all info at initial connection
     */
    private void getStartupInfo() {
        webSocket.sendRequest("printer.info");
        webSocket.sendRequest("server.info");
        webSocket.sendRequest("machine.update.status");
        webSocket.sendRequest("machine.device_power.devices");
    }

    @Override
    public void onEvent(final RPCResponse response) {
        String method = response.method;
        if (method == null)
            method = substringBefore(response.id, ':');

        if (method == null) {
            logger.warn("Invalid response: {}", response);
            return;
        }

        JsonElement result = null;
        if (response.result != null && response.result.isJsonObject()) {
            result = response.result;
        } else if (response.params != null && response.params.isJsonArray()) {
            result = response.params.get(0);
        }

        switch (method) {
            case "notify_klippy_ready":
                updateStatus(ThingStatus.ONLINE);
                webSocket.sendRequest("printer.objects.list");
                return;
            case "notify_klippy_disconnected":
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Klippy disconnected");
                requestStateDelayed();
                return;
            case "notify_klippy_shutdown":
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, "Klippy shutdown");
                requestStateDelayed();
                return;
            case "machine.device_power.on":
            case "machine.device_power.off":
            case "printer.firmware_restart":
            case "printer.restart":
            case "printer.emergence_stop":
            case "printer.gcode.script":
            case "notify_filelist_changed":
            case "notify_update_response":
            case "notify_metadata_update":
            case "notify_update_refreshed":
            case "notify_cpu_throttled":
            case "notify_history_changed":
                // ignore confirmations and extra notifications
                return;
        }

        if (result == null) {
            logger.warn("Result is empty or unknown method in response: {}", response);
            return;
        }

        switch (method) {
            case "printer.info":
                processPrinterInfo(result.getAsJsonObject());
                return;
            case "server.info":
                processServerInfo(result.getAsJsonObject());
                return;
            case "machine.device_power.devices":
                processPowerDeviceInfo(result.getAsJsonObject());
                return;
            case "machine.update.status":
                processUpdateInfo(result.getAsJsonObject());
                return;
            case "printer.objects.list":
                String[] objects = new Gson().fromJson(result.getAsJsonObject().getAsJsonArray("objects"),
                        String[].class);
                subscribeToObjects(objects);
                return;
            case "printer.objects.query":
            case "printer.objects.subscribe":
                updateObjectsInfoChannels(result.getAsJsonObject().getAsJsonObject("status"));
                processObjectsInfo(result.getAsJsonObject().getAsJsonObject("status"));
                return;
            case "notify_status_update":
                processObjectsInfo(result.getAsJsonObject());
                return;
            case "notify_power_changed":
                processPowerChanged(result.getAsJsonObject());
                return;
            case "notify_gcode_response":
                processGcodeResponse(result.getAsString());
                return;
            case "server.files.metadata":
                processFileMetaData(result.getAsJsonObject());
                return;
            default:
                logger.info("Unknown method in response: {}", response);
                return;
        }
    }

    /**
     * Subscribe to notifications for the list of objects
     * 
     * @param objects list of objects to subscribe to
     */
    void subscribeToObjects(@Nullable String @Nullable [] objects) {
        if (objects == null)
            return;

        JsonObject jsonObject = new JsonObject();
        Arrays.stream(objects).filter(Pattern.compile(
                "output_pin|gcode_button|webhooks|mcu|gcode_move|idle_timeout|toolhead|pause_resume|probe|print_stats|virtual_sdcard|display_status|firmware_retraction|"
                        + "((fan|heater|extruder|temperature_sensor|temperature_fan|filament_switch_sensor|output_pin).*)")
                .asPredicate()).forEach(s -> jsonObject.add(s, null));

        JsonObject jsonRootObject = new JsonObject();
        jsonRootObject.add("objects", jsonObject);

        webSocket.sendRequest("printer.objects.subscribe", jsonRootObject);
    }

    /**
     * Creates all {@link Channel}s for the given {@link ChannelGroupTypeUID}.
     *
     * @param channelGroupId the channel group id
     * @param channelGroupTypeUID the {@link ChannelGroupTypeUID}
     * @return a list of all {@link Channel}s for the channel group
     */
    private List<Channel> createChannelsForGroup(String channelGroupId, ChannelGroupTypeUID channelGroupTypeUID) {
        List<Channel> channels = new ArrayList<>();
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            for (ChannelBuilder channelBuilder : callback.createChannelBuilders(
                    new ChannelGroupUID(getThing().getUID(), channelGroupId), channelGroupTypeUID)) {
                channels.add(channelBuilder.build());
            }
        }
        return channels;
    }

    /**
     * Update individual position channel state
     * 
     * @param values array of values
     * @param index index into the array of values
     * @param group GroupId of the channel
     * @param key Id of the channel
     * @param postfix Postfix for the channel name
     */
    private void updatePosition(BigDecimal[] values, int index, String group, String key, String postfix) {
        if (index >= values.length)
            return;
        Channel channel = getChannel(group, key, postfix);
        if (channel == null)
            return;

        this.updateState(channel.getUID(), new QuantityType<>(values[index].scaleByPowerOfTen(-3), Units.METRE));
    }

    /**
     * Get channel
     * 
     * @param group GroupId of the channel
     * @param key Id of the channel
     * @return {@link Channel}
     */
    private @Nullable Channel getChannel(String group, String key) {
        return getChannel(group, key, null);
    }

    /**
     * Get channel
     * 
     * @param group GroupId of the channel
     * @param key Id of the channel
     * @param postfix postfix of the channel name
     * @return {@link Channel}
     */
    private @Nullable Channel getChannel(String group, String key, @Nullable String postfix) {
        postfix = postfix == null ? "" : "__" + postfix;

        Channel channel = this.getThing().getChannel(group + "#" + key + postfix);
        if (channel == null)
            channel = this.getThing().getChannel("general#" + group + "__" + key + postfix);

        if (channel == null)
            logger.debug("Unknown Channel: {}#{}", group, key);

        return channel;
    }

    /**
     * Update global state from various state information
     */
    private void updateState() {
        String state;
        String state_message;

        if (!"ready".equalsIgnoreCase(klippy_state)) {
            state = klippy_state;
            state_message = klippy_state_message;
        } else if (!"ready".equalsIgnoreCase(webhooks_state) || print_stats_state == null
                || print_stats_state.trim().length() == 0 || "ready".equalsIgnoreCase(print_stats_state)) {
            state = webhooks_state;
            state_message = webhooks_state_message;
        } else {
            state = print_stats_state;
            state_message = print_stats_state_message;
            if ((state_message == null || state_message.trim().length() == 0) && state.equals(webhooks_state)) {
                state_message = webhooks_state_message;
            }
        }

        if (!"paused".equalsIgnoreCase(state) && "printing".equalsIgnoreCase(idle_timeout_state)
                && !"printing".equalsIgnoreCase(state)) {
            // The printers idle_timeout changes to printing when it's busy applying
            // some change - but not necessarily printing anything. This state hopefully
            // helps aleviate that confusion.
            state = "busy";
            state_message = "Busy";
        }

        if ("printing".equalsIgnoreCase(state)) {
            state_message = MessageFormat.format("Printing - {0} %", display_status_progress);
        }

        if (!"error".equalsIgnoreCase(state) && display_status_message != null
                && display_status_message.trim().length() > 0) {
            state_message = display_status_message;
        }

        if (state_message == null || state_message.trim().length() == 0) {
            state_message = state.substring(0, 1).toUpperCase() + state.substring(1).toLowerCase();
        }

        updateState("general#state", new StringType(state));
        updateState("general#state_message", new StringType(state_message));
    }

    /**
     * Add or remove channels based on the information available in the object
     * 
     * @param object Result from "printer.objects.query",
     *            "printer.objects.subscribe" or "notify_status_update"
     */
    void updateObjectsInfoChannels(JsonObject object) {
        List<Channel> toBeAddedChannels = new ArrayList<>();
        List<Channel> toBeRemovedChannels = new ArrayList<>();

        for (Channel channel : this.getThing().getChannels()) {
            if (channel.getUID().getGroupId().startsWith("extruder") || channel.getUID().getGroupId().startsWith("fan")
                    || channel.getUID().getGroupId().startsWith("fan_generic")
                    || channel.getUID().getGroupId().startsWith("controller_fan")
                    || channel.getUID().getGroupId().startsWith("heater_fan")
                    || channel.getUID().getGroupId().startsWith("temperature_fan")
                    || channel.getUID().getGroupId().startsWith("heater_bed")
                    || channel.getUID().getGroupId().startsWith("heater_generic")
                    || channel.getUID().getGroupId().startsWith("temperature_sensor")
                    || channel.getUID().getGroupId().startsWith("filament_switch_sensor")
                    || channel.getUID().getGroupId().startsWith("output_pin")
                    || channel.getUID().getGroupId().startsWith("gcode_button"))
                toBeRemovedChannels.add(channel);
        }

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                String groupID = entry.getKey().replace(" ", "__");
                if (groupID.startsWith("extruder"))
                    toBeAddedChannels.addAll(createChannelsForGroup(groupID, CHANNEL_GROUP_TYPE_EXTRUDER));
                else if (groupID.startsWith("temperature_fan"))
                    toBeAddedChannels.addAll(createChannelsForGroup(groupID, CHANNEL_GROUP_TYPE_TEMPERATURE_FAN));
                else if (groupID.startsWith("fan") || groupID.startsWith("fan_generic")
                        || groupID.startsWith("controller_fan") || groupID.startsWith("heater_fan"))
                    toBeAddedChannels.addAll(createChannelsForGroup(groupID, CHANNEL_GROUP_TYPE_FAN));
                else if (groupID.startsWith("heater_bed") || groupID.startsWith("heater_generic"))
                    toBeAddedChannels.addAll(createChannelsForGroup(groupID, CHANNEL_GROUP_TYPE_HEATER));
                else if (groupID.startsWith("temperature_sensor"))
                    toBeAddedChannels.addAll(createChannelsForGroup(groupID, CHANNEL_GROUP_TYPE_TEMPERATURE_SENSOR));
                else if (groupID.startsWith("filament_switch_sensor"))
                    toBeAddedChannels.addAll(createChannelsForGroup(groupID, CHANNEL_GROUP_TYPE_FILAMENT_SENSOR));
                else if (groupID.startsWith("output_pin"))
                    toBeAddedChannels.addAll(createChannelsForGroup(groupID, CHANNEL_GROUP_TYPE_OUTPUT_PIN));
                else if (groupID.startsWith("gcode_button"))
                    toBeAddedChannels.addAll(createChannelsForGroup(groupID, CHANNEL_GROUP_TYPE_GCODE_BUTTON));
            }
        }
        ThingBuilder thingBuilder = this.editThing().withoutChannels(toBeRemovedChannels);

        for (Channel channel : toBeAddedChannels) {
            thingBuilder.withChannel(channel);
        }
        this.updateThing(thingBuilder.build());
    }

    /**
     * Update channel states based on the printer objects info
     * 
     * @param object Result from "printer.objects.query",
     *            "printer.objects.subscribe" or "notify_status_update"
     */
    void processObjectsInfo(JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                if (entry.getKey().equals("heaters"))
                    continue;

                String groupID = entry.getKey().replace(" ", "__");
                for (Map.Entry<String, JsonElement> entry2 : entry.getValue().getAsJsonObject().entrySet()) {
                    if (entry2.getKey().equals("state")) {
                        if (groupID.equals("webhooks")) {
                            webhooks_state = entry2.getValue().getAsString();
                        } else if (groupID.equals("print_stats")) {
                            print_stats_state = entry2.getValue().getAsString();
                        } else if (groupID.equals("idle_timeout")) {
                            idle_timeout_state = entry2.getValue().getAsString();
                        }
                        updateState();
                    } else if (entry2.getKey().equals("state_message")) {
                        if (groupID.equals("webhooks")) {
                            webhooks_state_message = entry2.getValue().isJsonNull() ? null
                                    : entry2.getValue().getAsString();
                        } else if (groupID.equals("print_stats")) {
                            print_stats_state_message = entry2.getValue().isJsonNull() ? null
                                    : entry2.getValue().getAsString();
                        }
                        updateState();
                    } else if (groupID.equals("display_status")) {
                        if (entry2.getKey().equals("progress")) {
                            display_status_progress = (int) (entry2.getValue().getAsFloat() * 100 + 0.5);
                        } else if (entry2.getKey().equals("message")) {
                            display_status_message = entry2.getValue().isJsonNull() ? null
                                    : entry2.getValue().getAsString();
                        }
                        updateState();
                    }

                    if (entry2.getValue().isJsonNull()) {
                        Channel channel = getChannel(groupID, entry2.getKey());
                        if (channel == null)
                            continue;

                        this.updateState(channel.getUID(), UnDefType.NULL);
                    } else if (entry2.getValue().isJsonPrimitive()) {
                        if (entry2.getValue().isJsonPrimitive() && entry2.getKey().equals("mcu_build_versions")
                                || entry2.getKey().equals("mcu_version")) {
                            Map<String, String> properties = this.editProperties();
                            properties.put(entry2.getKey(), entry2.getValue().getAsString());
                            this.updateProperties(properties);
                            continue;
                        }

                        Channel channel = getChannel(groupID, entry2.getKey());
                        if (channel == null)
                            continue;

                        JsonPrimitive value = entry2.getValue().getAsJsonPrimitive();
                        if (value.isNumber()) {
                            switch (entry2.getKey()) {
                                case "power":
                                case "progress":
                                case "speed_factor":
                                case "extrude_factor":
                                case "value":
                                    this.updateState(channel.getUID(), new QuantityType<>(
                                            value.getAsBigDecimal().scaleByPowerOfTen(2), Units.PERCENT));
                                    break;
                                case "pressure_advance":
                                    this.updateState(channel.getUID(), new DecimalType(value.getAsBigDecimal()));
                                    break;
                                case "temperature":
                                case "target":
                                case "measured_min_temp":
                                case "measured_max_temp":
                                    this.updateState(channel.getUID(), new QuantityType<>(
                                            value.getAsBigDecimal().setScale(1, RoundingMode.HALF_UP), Units.CELSIUS));
                                    break;
                                case "rpm":
                                    this.updateState(channel.getUID(), new QuantityType<>(
                                            value.getAsBigDecimal().multiply(new BigDecimal(60)), Units.HERTZ));
                                    break;
                                case "speed":
                                    if (entry.getKey().equals("fan") || entry.getKey().startsWith("temperature_fan")) {
                                        this.updateState(channel.getUID(), new QuantityType<>(
                                                value.getAsBigDecimal().scaleByPowerOfTen(2), Units.PERCENT));
                                        break;
                                    }
                                    // else fall through to default speed handling
                                case "max_velocity":
                                case "square_corner_velocity":
                                case "retract_speed":
                                case "unretract_speed":
                                    this.updateState(channel.getUID(), new QuantityType<>(
                                            value.getAsBigDecimal().scaleByPowerOfTen(-3), Units.METRE_PER_SECOND));
                                    break;
                                case "max_accel":
                                case "max_accel_to_decel":
                                    this.updateState(channel.getUID(),
                                            new QuantityType<>(value.getAsBigDecimal().scaleByPowerOfTen(-3),
                                                    Units.METRE_PER_SQUARE_SECOND));
                                    break;
                                case "print_time":
                                case "print_duration":
                                case "total_duration":
                                case "printing_time":
                                case "estimated_print_time":
                                    this.updateState(channel.getUID(), new QuantityType<>(
                                            value.getAsBigDecimal().setScale(0, RoundingMode.HALF_UP), Units.SECOND));
                                    break;
                                case "smooth_time":
                                    this.updateState(channel.getUID(),
                                            new QuantityType<>(value.getAsBigDecimal(), Units.SECOND));
                                    break;
                                case "retract_length":
                                case "unretract_extra_length":
                                case "last_z_result":
                                    this.updateState(channel.getUID(), new QuantityType<>(
                                            value.getAsBigDecimal().scaleByPowerOfTen(-3), Units.METRE));
                                    break;
                                case "filament_used":
                                    this.updateState(channel.getUID(),
                                            new QuantityType<>(value.getAsBigDecimal(), Units.METRE));
                                    break;
                                case "file_position":
                                    this.updateState(channel.getUID(), new QuantityType<>(value.getAsBigDecimal(),
                                            org.openhab.core.library.unit.Units.BYTE));
                                    break;
                                default:
                                    this.updateState(channel.getUID(), new DecimalType(value.getAsBigDecimal()));
                                    logger.debug("Unknown key for unit: {}#{}: {}", groupID, entry2.getKey(), value);
                                    break;
                            }
                        } else if (value.isString()) {
                            if (entry2.getKey().equals("filename")) {
                                if (value.getAsString() != null && value.getAsString().trim().length() > 0) {
                                    JsonObject object2 = new JsonObject();
                                    object2.addProperty("filename", value.getAsString());
                                    webSocket.sendRequest("server.files.metadata", object2);
                                } else {
                                    for (Channel fileInfoChannel : this.getThing().getChannelsOfGroup("file_info")) {
                                        this.updateState(fileInfoChannel.getUID(), UnDefType.UNDEF);
                                    }
                                }
                            }
                            this.updateState(channel.getUID(), new StringType(value.getAsString()));
                        } else if (value.isBoolean()) {
                            this.updateState(channel.getUID(),
                                    value.getAsBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                        } else {
                            logger.debug("Unknown data type: {}#{}: {}", groupID, entry2.getKey(), value);
                        }
                    } else if (entry2.getValue().isJsonArray()) {
                        if (entry2.getValue().getAsJsonArray().get(0).isJsonPrimitive()
                                && entry2.getValue().getAsJsonArray().get(0).getAsJsonPrimitive().isNumber()) {
                            final BigDecimal[] values = new Gson().fromJson(entry2.getValue(), BigDecimal[].class);
                            if (values != null) {
                                updatePosition(values, 0, groupID, entry2.getKey(), "x");
                                updatePosition(values, 1, groupID, entry2.getKey(), "y");
                                updatePosition(values, 2, groupID, entry2.getKey(), "z");
                            }
                        } else {
                            logger.debug("Unknown data type {}#{}: {}", groupID, entry2.getKey(), entry2.getValue());
                        }
                    } else if (entry2.getValue().isJsonObject()) {
                        if (entry2.getKey().equals("last_stats")) {
                            for (Map.Entry<String, JsonElement> entry3 : entry2.getValue().getAsJsonObject()
                                    .entrySet()) {

                                if (!entry3.getKey().startsWith("bytes_") && !entry3.getKey().equals("freq"))
                                    continue;

                                Channel channel = getChannel(groupID, entry3.getKey());
                                if (channel == null)
                                    continue;

                                JsonPrimitive value = entry3.getValue().getAsJsonPrimitive();
                                if (value.isNumber()) {
                                    switch (entry3.getKey()) {
                                        case "bytes_invalid":
                                        case "bytes_read":
                                        case "bytes_retransmit":
                                        case "bytes_write":
                                            this.updateState(channel.getUID(), new QuantityType<>(
                                                    value.getAsBigDecimal(), org.openhab.core.library.unit.Units.BYTE));
                                            break;
                                        case "freq":
                                            this.updateState(channel.getUID(),
                                                    new QuantityType<>(value.getAsBigDecimal(), Units.HERTZ));
                                            break;
                                        default:
                                            this.updateState(channel.getUID(),
                                                    new DecimalType(value.getAsBigDecimal()));
                                            logger.debug("Unknown key for unit: {}#{}: {}", groupID, entry2.getKey(),
                                                    value);

                                            break;
                                    }
                                } else {
                                    logger.debug("Unknown data type: {}#{}: {}", groupID, entry3.getKey(), value);
                                }
                            }
                        } else if (entry2.getKey().equals("mcu_constants")) {
                            // do nothing
                        } else {
                            logger.debug("Unknown Json object {}#{}: {}", groupID, entry2.getKey(), entry2.getValue());
                        }
                    } else {
                        logger.debug("Unknown Json type {}#{}: {}", groupID, entry2.getKey(), entry2.getValue());
                    }
                }
            }
        }
    }

    /**
     * Update channels for the file metadata
     * 
     * @param object Result from "server.files.metadata"
     */
    private void processFileMetaData(JsonObject object) {
        String groupID = "file_info";

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (entry.getValue().isJsonNull()) {
                Channel channel = getChannel(groupID, entry.getKey());
                if (channel == null)
                    continue;

                this.updateState(channel.getUID(), UnDefType.NULL);
            } else if (entry.getValue().isJsonPrimitive()) {
                Channel channel = getChannel(groupID, entry.getKey());
                if (channel == null)
                    continue;

                JsonPrimitive value = entry.getValue().getAsJsonPrimitive();
                if (value.isNumber()) {
                    switch (entry.getKey()) {
                        case "first_layer_bed_temp":
                        case "first_layer_extr_temp":
                            this.updateState(channel.getUID(), new QuantityType<>(
                                    value.getAsBigDecimal().setScale(1, RoundingMode.HALF_UP), Units.CELSIUS));
                            break;
                        case "estimated_time":
                            this.updateState(channel.getUID(), new QuantityType<>(
                                    value.getAsBigDecimal().setScale(0, RoundingMode.HALF_UP), Units.SECOND));
                            break;
                        case "first_layer_height":
                        case "layer_height":
                        case "object_height":
                        case "filament_total":
                            this.updateState(channel.getUID(),
                                    new QuantityType<>(value.getAsBigDecimal().scaleByPowerOfTen(-3), Units.METRE));
                            break;
                        case "size":
                        case "gcode_start_byte":
                        case "gcode_end_byte":
                            this.updateState(channel.getUID(), new QuantityType<>(value.getAsBigDecimal(),
                                    org.openhab.core.library.unit.Units.BYTE));
                            break;
                        case "modified":
                            this.updateState(channel.getUID(), new QuantityType<>(
                                    value.getAsBigDecimal().setScale(0, RoundingMode.HALF_UP), Units.SECOND));
                            break;

                        default:
                            this.updateState(channel.getUID(), new DecimalType(value.getAsBigDecimal()));
                            logger.debug("Unknown key for unit: {}#{}: {}", groupID, entry.getKey(), value);
                            break;
                    }
                } else if (value.isString()) {
                    this.updateState(channel.getUID(), new StringType(value.getAsString()));
                } else {
                    logger.debug("Unknown data type: {}#{}: {}", groupID, entry.getKey(), value);
                }
            } else if (entry.getValue().isJsonArray()) {
                JsonArray thumbnails = entry.getValue().getAsJsonArray();
                if (entry.getKey().equals("thumbnails") && thumbnails.size() >= 1
                        && thumbnails.get(thumbnails.size() - 1).isJsonObject()) {
                    JsonObject thumbnail = thumbnails.get(thumbnails.size() - 1).getAsJsonObject();
                    Channel channel = getChannel(groupID, "thumbnail");
                    this.updateState(channel.getUID(),
                            new RawType(Base64.getDecoder().decode(thumbnail.get("data").getAsString()), "image/png"));
                } else {
                    logger.debug("Unknown Json object {}#{}: {}", groupID, entry.getKey(), entry.getValue());
                }
            } else {
                logger.debug("Unknown Json object {}#{}: {}", groupID, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Update the Gcode Response channel
     * 
     * @param result Result from "notify_gcode_response"
     */
    private void processGcodeResponse(String result) {
        if (!result.matches("(?i)^(b|t\\d+):\\d+\\.\\d+ \\/\\d+\\.+\\d+.*")) {
            logger.debug("gcode response: {}", result);
            updateState("general#gcode_response", new StringType(result));
        }
    }

    /**
     * Update printer status channels
     * 
     * @param result Result from "printer.info"
     */
    private void processPrinterInfo(JsonObject result) {
        Map<String, String> properties = editProperties();
        for (String key : properties.keySet()) {
            if (result.has(key)) {
                properties.put(key, result.get(key).getAsString());
            }
        }
        updateProperties(properties);

        klippy_state = result.getAsJsonPrimitive("state").getAsString();
        klippy_state_message = result.getAsJsonPrimitive("state_message").getAsString();

        logger.debug("Klippy state: {}", klippy_state);
        switch (klippy_state) {
            case "ready":
                updateStatus(ThingStatus.ONLINE);
                webSocket.sendRequest("printer.objects.list");
                break;
            case "error":
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                        "Klippy error: " + result.getAsJsonObject().getAsJsonPrimitive("state_message").getAsString());
                requestStateDelayed();
                break;
            case "shutdown":
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Klippy shutting down");
                requestStateDelayed();
                break;
            case "startup":
            default:
                requestStateDelayed();
                break;
        }
    }

    /**
     * Update server info channels
     * 
     * @param result Result from "server.info"
     */
    private void processServerInfo(JsonObject result) {
        Map<String, String> properties = editProperties();
        for (String key : properties.keySet()) {
            if (result.has(key)) {
                JsonElement value = result.get(key);
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    properties.put(key, value.getAsString());
                } else if (value.isJsonArray()) {
                    properties.put(key, value.getAsJsonArray().toString());
                }
            }
        }
        updateProperties(properties);
    }

    /**
     * Update power device update channels
     * 
     * @param object Result from "notify_power_changed" or from
     *            "machine.device_power.devices"
     */
    void processPowerChanged(JsonObject object) {
        Channel channel = getChannel("power_device__" + object.get("device").getAsString(), "status");
        if (channel == null)
            return;
        String status = object.get("status").getAsString();
        this.updateState(channel.getUID(), new StringType(status));
        channel = getChannel("power_device__" + object.get("device").getAsString(), "switch");
        if (channel == null)
            return;
        this.updateState(channel.getUID(),
                "on".equals(status) ? OnOffType.ON : ("off".equals(status) ? OnOffType.OFF : UnDefType.UNDEF));
    }

    /**
     * Update power device channels
     * 
     * @param object Result from "machine.device_power.devices"
     */
    private void processPowerDeviceInfo(JsonObject result) {

        List<Channel> toBeAddedChannels = new ArrayList<>();
        List<Channel> toBeRemovedChannels = new ArrayList<>();

        for (Channel channel : this.getThing().getChannels()) {
            if (channel.getUID().getGroupId().startsWith("power_device"))
                toBeRemovedChannels.add(channel);
        }

        for (JsonElement element : result.getAsJsonArray("devices")) {
            JsonObject object = element.getAsJsonObject();
            toBeAddedChannels.addAll(createChannelsForGroup("power_device__" + object.get("device").getAsString(),
                    CHANNEL_GROUP_TYPE_POWER_DEVICE));
        }

        ThingBuilder thingBuilder = this.editThing().withoutChannels(toBeRemovedChannels);
        for (Channel channel : toBeAddedChannels) {
            thingBuilder.withChannel(channel);
        }
        this.updateThing(thingBuilder.build());

        for (JsonElement element : result.getAsJsonArray("devices")) {
            processPowerChanged(element.getAsJsonObject());
        }
    }

    /**
     * Update version information properties
     * 
     * @param result Result from "machine.update.status"
     */
    private void processUpdateInfo(JsonObject result) {
        Map<String, String> properties = editProperties();
        for (String key : result.getAsJsonObject("version_info").keySet()) {
            JsonObject versionInfo = result.getAsJsonObject("version_info").getAsJsonObject(key);
            if (versionInfo.has("version")) {
                properties.put(key + "_version", versionInfo.get("version").getAsString());
            }
        }
        updateProperties(properties);
    }

    /**
     * Gets the part of the string before the seperator, or the whole string if the seperator is not found
     * 
     * @param str string from which to return the first part
     * @param separator the seperator character
     * @return
     */
    private static String substringBefore(@Nullable final String str, final int separator) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        final int pos = str.indexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * Get list of exception causes
     * 
     * @param throwable the exception
     * @return list of exception causes
     */
    private static List<Throwable> getThrowableList(@Nullable Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }

    /**
     * Get the root cause for an exception
     * 
     * @param throwable the exception
     * @return the root cause exception
     */
    private static @Nullable Throwable getRootCause(final @Nullable Throwable throwable) {
        final List<Throwable> list = getThrowableList(throwable);
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    /**
     * Get the message for the root cause exception
     * 
     * @param th the exception
     * @return the message
     */
    private static String getRootCauseMessage(final @Nullable Throwable th) {
        Throwable root = getRootCause(th);
        root = root == null ? th : root;
        return root.getMessage();
    }
}
