/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.List;
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
import org.json.JSONObject;
import org.openhab.binding.mspa.internal.MSpaCommandOptionProvider;
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

    private MSpaPoolConfiguration config = new MSpaPoolConfiguration();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<MSpaBaseAccount> account = Optional.empty();
    private String dataCache = (new JSONObject()).toString();
    private final MSpaCommandOptionProvider commandProvider;
    private final UnitProvider unitProvider;

    public MSpaPool(Thing thing, UnitProvider unitProvider, MSpaCommandOptionProvider commandProvider) {
        super(thing);
        this.commandProvider = commandProvider;
        this.unitProvider = unitProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            distributeData(dataCache);
        } else {
            // prepare for command execution
            JSONObject commandBody = new JSONObject();
            if (command instanceof OnOffType onOff) {
                int on = OnOffType.ON.equals(onOff) ? 1 : 0;
                String commandDetail = UNKNOWN;
                switch (channelId) {
                    case HEATER:
                        commandDetail = "\"heater_state\":" + on;
                        break;
                    case JET_STREAM:
                        commandDetail = "\"jet_state\":" + on;
                        break;
                    case BUBBLES:
                        commandDetail = "\"bubble_state\":" + on;
                        break;
                    case CIRCULATE:
                        commandDetail = "\"filter_state\":" + on;
                        break;
                    case UVC:
                        commandDetail = "\"uvc_state\":" + on;
                        break;
                    case OZONE:
                        commandDetail = "\"ozone_state\":" + on;
                        break;
                    case LOCK:
                        commandDetail = "\"safety_lock\":" + on;
                        break;
                }
                if (!UNKNOWN.equals(commandDetail)) {
                    String commandString = String.format(COMMAND_TEMPLATE, commandDetail);
                    commandBody = new JSONObject(commandString);
                }
            } else if (command instanceof DecimalType decimal) {
                String commandDetail = UNKNOWN;
                switch (channelId) {
                    case BUBBLE_LEVEL:
                        int adjustedLevel = Math.min(Math.max(1, decimal.intValue()), 3);
                        commandDetail = "\"bubble_level\":" + adjustedLevel;
                        break;
                }
                if (!UNKNOWN.equals(commandDetail)) {
                    String commandString = String.format(COMMAND_TEMPLATE, commandDetail);
                    commandBody = new JSONObject(commandString);
                }
            } else if (command instanceof QuantityType qt) {
                // still to investigate target values
                String commandDetail = UNKNOWN;
                switch (channelId) {
                    case WATER_TARGET_TEMPERATURE:
                        int adjustedTemperature = Math.min(Math.max(10, qt.intValue()), 40);
                        commandDetail = "\"temperature_setting\":" + adjustedTemperature * 2;
                        break;
                }
                if (!UNKNOWN.equals(commandDetail)) {
                    String commandString = String.format(COMMAND_TEMPLATE, commandDetail);
                    commandBody = new JSONObject(commandString);
                }
            }
            // command is calculated so send
            if (!commandBody.isEmpty() && !account.isEmpty()) {
                commandBody.put("device_id", config.deviceId);
                commandBody.put("product_id", config.productId);
                Request commandRequest = account.get().getRequest(POST, COMMAND_ENDPOINT);
                commandRequest.content(new StringContentProvider(commandBody.toString(), "utf-8"));
                try {
                    ContentResponse cr = commandRequest.timeout(10, TimeUnit.SECONDS).send();
                    int status = cr.getStatus();
                    String response = cr.getContentAsString();
                    if (status == 200) {
                        logger.trace("Command Response {}", response);
                        scheduler.schedule(this::updateData, 5, TimeUnit.SECONDS);
                    } else {
                        logger.warn("Error sending command {} - {}", commandBody.toString(), response);
                    }
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.warn("Error sending command {} - {}", commandBody.toString(), e.getMessage());
                }
            } else {
                logger.info("Either command ({}) or account ({}) empty", commandBody, account.isEmpty());
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(MSpaPoolConfiguration.class);
        if (UNKNOWN.equals(config.deviceId) || UNKNOWN.equals(config.productId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing configuration parameters");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge != null) {
            updateStatus(ThingStatus.UNKNOWN);
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof MSpaBaseAccount accountHandler) {
                account = Optional.of(accountHandler);
                String token = accountHandler.getToken();
                if (!UNKNOWN.equals(token)) {
                    refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::updateData, 2,
                            config.refreshInterval * 60, TimeUnit.SECONDS));
                    updateStatus(ThingStatus.ONLINE);
                    setCommandOptions();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Token invalid");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Wrong bridge configured");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        }
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> {
            job.cancel(true);
        });
    }

    private void updateData() {
        if (account.isPresent()) {
            Request dataRequest = account.get().getRequest(POST, DEVICE_SHADOW_ENDPOINT);

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
                    logger.warn("Failed to get data - reason {}", response);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Failed to get data - reason {}", e.getMessage());
            }

        }
    }

    public void distributeData(String response) {
        JSONObject data = new JSONObject(response);
        dataCache = data.toString();
        if (data.has("data")) {
            JSONObject rawData = data.getJSONObject("data");
            if (rawData.has("heater_state")) {
                updateState(HEATER, OnOffType.from(rawData.getInt("heater_state") == 1));
            }
            if (rawData.has("water_temperature")) {
                updateState(WATER_CURRENT_TEMPERATURE,
                        QuantityType.valueOf(rawData.getInt("water_temperature") / 2.0, SIUnits.CELSIUS));
            }
            if (rawData.has("temperature_setting")) {
                updateState(WATER_TARGET_TEMPERATURE,
                        QuantityType.valueOf(rawData.getInt("temperature_setting") / 2.0, SIUnits.CELSIUS));
            }
            if (rawData.has("jet_state")) {
                updateState(JET_STREAM, OnOffType.from(rawData.getInt("jet_state") == 1));
            }
            if (rawData.has("bubble_state")) {
                updateState(BUBBLES, OnOffType.from(rawData.getInt("bubble_state") == 1));
            }
            if (rawData.has("bubble_level")) {
                updateState(BUBBLE_LEVEL, new DecimalType(rawData.getInt("bubble_level")));
            }
            if (rawData.has("filter_state")) {
                updateState(CIRCULATE, OnOffType.from(rawData.getInt("filter_state") == 1));
            }
            if (rawData.has("uvc_state")) {
                updateState(UVC, OnOffType.from(rawData.getInt("uvc_state") == 1));
            }
            if (rawData.has("ozone_state")) {
                updateState(OZONE, OnOffType.from(rawData.getInt("ozone_state") == 1));
            }
            if (rawData.has("safety_lock")) {
                updateState(LOCK, OnOffType.from(rawData.getInt("safety_lock") == 1));
            }
        }
    }

    private void setCommandOptions() {
        List<CommandOption> commandOptions = new ArrayList<>();
        Unit<Temperature> temperatureUnit = unitProvider.getUnit(Temperature.class);
        if (ImperialUnits.FAHRENHEIT.equals(temperatureUnit)) {
            for (int i = 86; i < 105; i++) {
                commandOptions.add(new CommandOption(i + " °F", i + " °F"));
            }
        } else {
            for (int i = 30; i < 41; i++) {
                commandOptions.add(new CommandOption(i + " °C", i + " °C"));
            }
        }
        ChannelUID cuid = new ChannelUID(thing.getUID(), WATER_TARGET_TEMPERATURE);
        commandProvider.setCommandOptions(cuid, commandOptions);
    }
}
