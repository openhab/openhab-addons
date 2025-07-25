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
package org.openhab.binding.mspa.internal.handler;

import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.mspa.internal.MSpaCommandOptionProvider;
import org.openhab.binding.mspa.internal.MSpaUtils;
import org.openhab.binding.mspa.internal.config.MSpaPoolConfiguration;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MSpaPool} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaPool extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MSpaPool.class);
    private final MSpaCommandOptionProvider commandProvider;
    private final UnitProvider unitProvider;

    private MSpaPoolConfiguration config = new MSpaPoolConfiguration();
    private Optional<Map<String, String>> deviceProperties = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<MSpaBaseAccount> account = Optional.empty();
    private String dataCache = (new JSONObject()).toString();

    public MSpaPool(Thing thing, UnitProvider unitProvider, MSpaCommandOptionProvider commandProvider) {
        super(thing);
        this.commandProvider = commandProvider;
        this.unitProvider = unitProvider;
    }

    public Optional<JSONObject> createCommandBody(String channelId, Command command) {
        String commandDetail = UNKNOWN;
        if (command instanceof OnOffType onOff) {
            int on = OnOffType.ON.equals(onOff) ? 1 : 0;
            commandDetail = switch (channelId) {
                case CHANNEL_HEATER -> "\"heater_state\":" + on;
                case CHANNEL_JET_STREAM -> "\"jet_state\":" + on;
                case CHANNEL_BUBBLES -> "\"bubble_state\":" + on;
                case CHANNEL_CIRCULATE -> "\"filter_state\":" + on;
                case CHANNEL_UVC -> "\"uvc_state\":" + on;
                case CHANNEL_OZONE -> "\"ozone_state\":" + on;
                case CHANNEL_LOCK -> "\"safety_lock\":" + on;
                default -> UNKNOWN;
            };
        } else if (command instanceof DecimalType decimal) {
            commandDetail = switch (channelId) {
                case CHANNEL_BUBBLE_LEVEL -> "\"bubble_level\":" + Math.min(Math.max(1, decimal.intValue()), 3);
                default -> UNKNOWN;
            };
        } else if (command instanceof QuantityType<?> qt) {
            /**
             * Water temperature handling by MSpa is proprietary. Values from / to device are based on °C but values
             * are times two! Conversion to °F for command needs separate handling.
             */
            QuantityType<?> temperature = qt.toUnit(SIUnits.CELSIUS);
            if (temperature != null) {
                commandDetail = switch (channelId) {
                    case CHANNEL_WATER_TARGET_TEMPERATURE ->
                        "\"temperature_setting\":" + Math.min(Math.max(20, temperature.intValue()), 40) * 2;
                    default -> UNKNOWN;
                };
            } else {
                logger.warn("Unsupported command with unit {} for channel {}", qt.getUnit(), channelId);
            }
        }

        if (!UNKNOWN.equals(commandDetail)) {
            String commandString = String.format(COMMAND_TEMPLATE, commandDetail);
            return Optional.of(new JSONObject(commandString));
        }
        return Optional.empty();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            distributeData(dataCache);
        } else {
            createCommandBody(channelId, command).ifPresent(commandBody -> {
                account.ifPresent(acc -> {
                    commandBody.put("device_id", config.deviceId);
                    commandBody.put("product_id", config.productId);
                    Request commandRequest = acc.getRequest(HttpMethod.POST, ENDPOINT_COMMAND);
                    commandRequest.content(new StringContentProvider(commandBody.toString(), "utf-8"));
                    try {
                        ContentResponse cr = commandRequest.timeout(10, TimeUnit.SECONDS).send();
                        int status = cr.getStatus();
                        String response = cr.getContentAsString();
                        if (status == 200) {
                            scheduler.schedule(this::updateData, 10, TimeUnit.SECONDS);
                        } else {
                            logger.warn("Error sending command {} - {}", commandBody.toString(), response);
                        }
                    } catch (InterruptedException | TimeoutException | ExecutionException e) {
                        logger.warn("Error sending command {} - {}", commandBody.toString(), e.toString());
                        handlePossibleInterrupt(e);
                    }
                });
            });
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(MSpaPoolConfiguration.class);
        if (config.deviceId.isBlank() || config.productId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.mspa.pool.config-parameter-missing");
            return;
        }
        if (config.refreshInterval < 5) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.mspa.pool.refresh-too-low");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof MSpaBaseAccount accountHandler) {
                account = Optional.of(accountHandler);
                String token = accountHandler.getToken();
                if (!UNKNOWN.equals(token)) {
                    updateStatus(ThingStatus.UNKNOWN);
                    refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::updateData, 2,
                            config.refreshInterval * 60, TimeUnit.SECONDS));
                    setCommandOptions();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/status.mspa.invalid-token");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/status.mspa.pool.wrong-bridge");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.mspa.pool.no-bridge");
        }
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> {
            job.cancel(true);
        });
        deviceProperties = Optional.empty();
    }

    private void updateData() {
        if (account.isPresent()) {
            if (!checkOnline()) {
                return;
            }
            Request dataRequest = account.get().getRequest(HttpMethod.POST, ENDPOINT_DEVICE_SHADOW);
            JSONObject body = new JSONObject();
            body.put("device_id", config.deviceId);
            body.put("product_id", config.productId);
            dataRequest.content(new StringContentProvider(body.toString(), "utf-8"));
            try {
                ContentResponse cr = dataRequest.timeout(10, TimeUnit.SECONDS).send();
                int status = cr.getStatus();
                String response = cr.getContentAsString();
                if (status == 200) {
                    distributeData(response);
                } else {
                    logger.info("Failed to get data - reason {}", response);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Failed to get data - reason {}", e.toString());
                handlePossibleInterrupt(e);
            }
        }
    }

    private boolean checkOnline() {
        Request deviceListRequest = account.get().getRequest(HttpMethod.GET, ENDPOINT_DEVICE_LIST);
        try {
            ContentResponse cr = deviceListRequest.timeout(10, TimeUnit.SECONDS).send();
            int status = cr.getStatus();
            String response = cr.getContentAsString();
            if (status == 200) {
                JSONObject devices = new JSONObject(response);
                if (devices.has("data")) {
                    JSONObject data = devices.getJSONObject("data");
                    if (data.has("list")) {
                        JSONArray list = data.getJSONArray("list");
                        for (Iterator<Object> iter = list.iterator(); iter.hasNext();) {
                            Object entry = iter.next();
                            if (entry instanceof JSONObject jsonEntry) {
                                if (jsonEntry.has("device_id")) {
                                    if (config.deviceId.equals(jsonEntry.getString("device_id"))) {
                                        if (deviceProperties.isEmpty()) {
                                            // update device properties one time after initialization
                                            Map<String, String> devicePropertiesMap = MSpaUtils
                                                    .getDeviceProperties(jsonEntry.toMap());
                                            thing.setProperties(devicePropertiesMap);
                                            deviceProperties = Optional.of(devicePropertiesMap);
                                        }
                                        if (jsonEntry.has("is_online")) {
                                            boolean online = jsonEntry.getBoolean("is_online");
                                            if (online) {
                                                updateStatus(ThingStatus.ONLINE);
                                            } else {
                                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                                        "@text/status.mspa.pool.offline");
                                            }
                                            return online;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.mspa.pool.request-failed [\"" + "@text/status.mspa.pool-missing" + "\"]");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.mspa.pool.request-failed [\"" + status + "\"]");
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/status.mspa.pool.request-failed [\"" + e.toString() + "\"]");
            handlePossibleInterrupt(e);
        }
        return false;
    }

    public void distributeData(String response) {
        JSONObject data = new JSONObject(response);
        dataCache = data.toString();
        if (data.has("data")) {
            JSONObject rawData = data.getJSONObject("data");
            if (rawData.has("heater_state")) {
                updateState(CHANNEL_HEATER, OnOffType.from(rawData.getInt("heater_state") == 1));
            }
            /**
             * Water temperature handling by MSpa is proprietary. Values from / to device are based on °C but values
             * are times two! Conversion to °F for update handled by OH.
             */
            if (rawData.has("water_temperature")) {
                updateState(CHANNEL_WATER_CURRENT_TEMPERATURE,
                        QuantityType.valueOf(rawData.getInt("water_temperature") / 2.0, SIUnits.CELSIUS));
            }
            if (rawData.has("temperature_setting")) {
                updateState(CHANNEL_WATER_TARGET_TEMPERATURE,
                        QuantityType.valueOf(rawData.getInt("temperature_setting") / 2.0, SIUnits.CELSIUS));
            }
            if (rawData.has("jet_state")) {
                updateState(CHANNEL_JET_STREAM, OnOffType.from(rawData.getInt("jet_state") == 1));
            }
            if (rawData.has("bubble_state")) {
                updateState(CHANNEL_BUBBLES, OnOffType.from(rawData.getInt("bubble_state") == 1));
            }
            if (rawData.has("bubble_level")) {
                updateState(CHANNEL_BUBBLE_LEVEL, new DecimalType(rawData.getInt("bubble_level")));
            }
            if (rawData.has("filter_state")) {
                updateState(CHANNEL_CIRCULATE, OnOffType.from(rawData.getInt("filter_state") == 1));
            }
            if (rawData.has("uvc_state")) {
                updateState(CHANNEL_UVC, OnOffType.from(rawData.getInt("uvc_state") == 1));
            }
            if (rawData.has("ozone_state")) {
                updateState(CHANNEL_OZONE, OnOffType.from(rawData.getInt("ozone_state") == 1));
            }
            if (rawData.has("safety_lock")) {
                updateState(CHANNEL_LOCK, OnOffType.from(rawData.getInt("safety_lock") == 1));
            }
        }
    }

    private void setCommandOptions() {
        List<CommandOption> commandOptions = new ArrayList<>();
        Unit<Temperature> temperatureUnit = unitProvider.getUnit(Temperature.class);
        // Set command options for UI from 30 °C (86 °F) to 40 °C (104 °F)
        if (ImperialUnits.FAHRENHEIT.equals(temperatureUnit)) {
            for (int i = 86; i < 105; i++) {
                commandOptions.add(new CommandOption(i + " °F", i + " °F"));
            }
        } else {
            for (int i = 30; i < 41; i++) {
                commandOptions.add(new CommandOption(i + " °C", i + " °C"));
            }
        }
        ChannelUID cuid = new ChannelUID(thing.getUID(), CHANNEL_WATER_TARGET_TEMPERATURE);
        commandProvider.setCommandOptions(cuid, commandOptions);
    }

    private void handlePossibleInterrupt(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
