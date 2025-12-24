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
package org.openhab.binding.tasmotaplug.internal.handler;

import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.tasmotaplug.internal.TasmotaPlugBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tasmotaplug.dto.TasmotaDTO;
import org.openhab.binding.tasmotaplug.dto.TasmotaDTO.Energy;
import org.openhab.binding.tasmotaplug.internal.TasmotaPlugConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TasmotaPlugHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class TasmotaPlugHandler extends BaseThingHandler {
    private static final long REQUEST_TIMEOUT_MS = 5000;

    private static final String PASSWORD_REGEX = "&password=(.*)&";
    private static final String PASSWORD_MASK = "&password=xxxx&";

    private final Logger logger = LoggerFactory.getLogger(TasmotaPlugHandler.class);
    private final HttpClient httpClient;
    private final Gson gson;

    private @Nullable ScheduledFuture<?> refreshJob;

    private String plugHost = BLANK;
    private int refreshPeriod = DEFAULT_REFRESH_PERIOD_SEC;
    private int numChannels = DEFAULT_NUM_CHANNELS;
    private boolean isAuth = false;
    private String user = BLANK;
    private String pass = BLANK;

    public TasmotaPlugHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
        gson = new GsonBuilder().serializeNulls().create();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing TasmotaPlug handler.");
        TasmotaPlugConfiguration config = getConfigAs(TasmotaPlugConfiguration.class);

        final String hostName = config.hostName;
        final String username = config.username;
        final String password = config.password;
        refreshPeriod = config.refresh;
        numChannels = config.numChannels;

        if (hostName.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-hostname");
            return;
        }

        if (!username.isBlank() && !password.isBlank()) {
            isAuth = true;
            user = username;
            pass = password;
        }

        plugHost = "http://" + hostName;

        // remove the channels we are not using
        if (this.numChannels < CONTROL_CHANNEL_IDS.size()) {
            List<Channel> channels = new ArrayList<>(this.getThing().getChannels());

            List<Integer> channelsToRemove = IntStream.range(this.numChannels + 1, CONTROL_CHANNEL_IDS.size() + 1)
                    .boxed().toList();

            channelsToRemove.forEach(channel -> {
                channels.removeIf(c -> (c.getUID().getId().equals(POWER + channel)));
                channels.removeIf(c -> (c.getUID().getId().equals(PULSE_TIME + channel)));
            });
            updateThing(editThing().withChannels(channels).build());
        }

        updateStatus(ThingStatus.UNKNOWN);
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the TasmotaPlug handler.");

        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // gets the tasmota command from the channel id to command map
        final String tasmotaChannel = COMMAND_MAP.get(channelUID.getId());

        if (tasmotaChannel == null) {
            logger.debug("Unsupported channelId: {}", channelUID.getId());
            return;
        }

        if (channelUID.getId().contains(POWER)) {
            if (command instanceof OnOffType) {
                getCommand(tasmotaChannel, command.toString());
            } else {
                updateChannelState(channelUID.getId());
            }
        } else if (channelUID.getId().contains(PULSE_TIME) && command instanceof DecimalType decimalCommand) {
            getCommand(tasmotaChannel, String.valueOf(decimalCommand.intValue()));
        } else {
            logger.debug("Unsupported command: {} for channel: {}", command.toString(), channelUID.getId());
        }
    }

    /**
     * Start the job to periodically update the state of the plug
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            refreshJob = null;
            this.refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                CONTROL_CHANNEL_IDS.stream().limit(numChannels).forEach(channelId -> {
                    updateChannelState(channelId);
                });

                if (ENERGY_CHANNEL_IDS.stream().anyMatch(energyCh -> isLinked(energyCh))) {
                    updateEnergyChannels();
                }
            }, 0, refreshPeriod, TimeUnit.SECONDS);
        }
    }

    private void updateChannelState(String channelId) {
        final String plugState = getCommand(COMMAND_MAP.getOrDefault(channelId, ""), null);
        if (plugState.contains(ON)) {
            updateState(channelId, OnOffType.ON);
        } else if (plugState.contains(OFF)) {
            updateState(channelId, OnOffType.OFF);
        }
    }

    private void updateEnergyChannels() {
        final Energy energyDto;
        final String json = getCommand(STATUS, STATUS_CMD);
        if (!json.isBlank()) {
            try {
                final TasmotaDTO dto = gson.fromJson(json, TasmotaDTO.class);
                if (dto != null) {
                    energyDto = dto.getStatus().getEnergy();
                } else {
                    logger.debug("TasmotaDTO was null for JSON: '{}'", json);
                    return;
                }
            } catch (JsonSyntaxException e) {
                logger.debug("Error parsing Tasmota status JSON: '{}' Exception: {}", json, e.getMessage());
                return;
            }
        } else {
            return;
        }

        if (isLinked(VOLTAGE)) {
            updateState(VOLTAGE, new QuantityType<>(energyDto.getVoltage(), Units.VOLT));
        }
        if (isLinked(CURRENT)) {
            updateState(CURRENT, new QuantityType<>(energyDto.getCurrent(), Units.AMPERE));
        }
        if (isLinked(WATTS)) {
            updateState(WATTS, new QuantityType<>(energyDto.getActivePower(), Units.WATT));
        }
        if (isLinked(VOLT_AMPERE)) {
            updateState(VOLT_AMPERE, new QuantityType<>(energyDto.getApparentPower(), Units.VOLT_AMPERE));
        }
        if (isLinked(VOLT_AMPERE_REACTIVE)) {
            updateState(VOLT_AMPERE_REACTIVE, new QuantityType<>(energyDto.getReactivePower(), Units.VAR));
        }
        if (isLinked(POWER_FACTOR)) {
            updateState(POWER_FACTOR, new DecimalType(energyDto.getPowerFactor()));
        }
        if (isLinked(ENERGY_TODAY)) {
            updateState(ENERGY_TODAY, new QuantityType<>(energyDto.getEnergyToday(), Units.KILOWATT_HOUR));
        }
        if (isLinked(ENERGY_YESTERDAY)) {
            updateState(ENERGY_YESTERDAY, new QuantityType<>(energyDto.getEnergyYesterday(), Units.KILOWATT_HOUR));
        }
        if (isLinked(ENERGY_TOTAL)) {
            updateState(ENERGY_TOTAL, new QuantityType<>(energyDto.getEnergyTotal(), Units.KILOWATT_HOUR));
        }
        if (isLinked(ENERGY_TOTAL_START) && !energyDto.getEnergyTotalStart().isBlank()) {
            updateState(ENERGY_TOTAL_START, new DateTimeType(energyDto.getEnergyTotalStart()));
        }
    }

    private String getCommand(String command, @Nullable String commandArg) {
        String url;

        if (isAuth) {
            url = String.format(CMD_URI_AUTH, user, pass, command);
        } else {
            url = String.format(CMD_URI, command);
        }

        if (commandArg != null) {
            url += "%20" + commandArg;
        }

        try {
            logger.trace("Sending GET request to {}{}", plugHost, maskPassword(url));
            ContentResponse contentResponse = httpClient.newRequest(plugHost + url).method(HttpMethod.GET)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
            logger.trace("Response: {}", contentResponse.getContentAsString());

            if (contentResponse.getStatus() != OK_200) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error.http-failure [\"" + contentResponse.getStatus() + "\"]");
                return BLANK;
            }

            updateStatus(ThingStatus.ONLINE);
            return contentResponse.getContentAsString();
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Error executing Tasmota GET request: '{}{}', {}", plugHost, maskPassword(url),
                    e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (InterruptedException e) {
            logger.debug("InterruptedException executing Tasmota GET request: '{}{}', {}", plugHost, maskPassword(url),
                    e.getMessage());
            Thread.currentThread().interrupt();
        }
        return BLANK;
    }

    private String maskPassword(String input) {
        return isAuth ? input.replaceAll(PASSWORD_REGEX, PASSWORD_MASK) : input;
    }
}
