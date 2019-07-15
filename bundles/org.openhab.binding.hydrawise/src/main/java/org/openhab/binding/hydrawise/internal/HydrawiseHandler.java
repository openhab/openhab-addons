/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal;

import static org.openhab.binding.hydrawise.internal.HydrawiseBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCloudApiClient;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCommandException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.model.Controller;
import org.openhab.binding.hydrawise.internal.api.model.Relay;
import org.openhab.binding.hydrawise.internal.api.model.Running;
import org.openhab.binding.hydrawise.internal.api.model.StatusScheduleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HydrawiseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
// @NonNullByDefault
public class HydrawiseHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HydrawiseHandler.class);

    private @Nullable HydrawiseConfiguration config;

    /**
     * Minimum amount of time we can poll for updates
     */
    private static final int MIN_REFRESH_SECONDS = 5;

    /**
     * Minimum amount of time we can poll after a command
     */
    private static final int COMMAND_REFRESH_SECONDS = 5;

    /**
     * Our poll rate
     */
    private int refresh;

    /**
     * Future to poll for updated
     */
    private @Nullable ScheduledFuture<?> pollFuture;

    private Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Relay> relayMap = Collections.synchronizedMap(new HashMap<>());

    HydrawiseCloudApiClient client;
    int controllerId;
    HttpClient httpClient;

    public HydrawiseHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Controller is NOT ONLINE and is not responding to commands");
            return;
        }

        stateMap.remove(channelUID.getAsString());
        if (command instanceof RefreshType) {
            stateMap.clear();
        }
        String group = channelUID.getGroupId();
        String channelId = channelUID.getIdWithoutGroup();

        Relay relay = relayMap.get(group);
        if (relay == null) {
            logger.debug("Zone not found {}", group);
            return;
        }

        try {
            clearPolling();
            switch (channelId) {
                case CHANNEL_ZONE_RUN_CUSTOM:
                    if (!(command instanceof DecimalType)) {
                        logger.warn("Invalid command type for run custom {}", command.getClass().getName());
                        return;
                    }
                    client.runRelay(((DecimalType) command).intValue(), relay.getRelayId());
                    break;
                case CHANNEL_ZONE_RUN:
                    if (!(command instanceof OnOffType)) {
                        logger.warn("Invalid command type for run {}", command.getClass().getName());
                        return;
                    }
                    if (command == OnOffType.ON) {
                        client.runRelay(relay.getRelayId());
                    } else {
                        client.stopRelay(relay.getRelayId());
                    }
                    break;
            }
            initPolling(COMMAND_REFRESH_SECONDS);
        } catch (HydrawiseCommandException | HydrawiseConnectionException e) {
            logger.debug("Could not issue command", e);
            initPolling(COMMAND_REFRESH_SECONDS);
        } catch (HydrawiseAuthenticationException e) {
            logger.debug("Credentials not valid");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Credentials not valid");
            configure();
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        configure();
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        clearPolling();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // clear our cached value so the new channel gets updated on the next poll
        stateMap.remove(channelUID.getId());

    }

    private void configure() {
        clearPolling();
        stateMap.clear();
        relayMap.clear();
        HydrawiseCloudConfiguration configuration = getConfig().as(HydrawiseCloudConfiguration.class);
        String confApiKey = configuration.apiKey;

        if (StringUtils.isBlank(confApiKey)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key not be empty");
            return;
        }

        this.refresh = configuration.refresh.intValue() > MIN_REFRESH_SECONDS ? configuration.refresh.intValue()
                : MIN_REFRESH_SECONDS;

        try {
            client = new HydrawiseCloudApiClient(confApiKey, httpClient);
            Controller controller = client.getCustomerDetails().getControllers().get(0);
            controllerId = controller.getControllerId().intValue();
            updateController(controller);
            logger.debug("Controller id {}", controllerId);
            initPolling(0);

        } catch (HydrawiseConnectionException e) {
            logger.debug("Could not connect to service");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (HydrawiseAuthenticationException e) {
            logger.debug("Credentials not valid");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Credentials not valid");
        }
    }

    /**
     * Starts/Restarts polling with an initial delay. This allows changes in the poll cycle for when commands are sent
     * and we need to poll sooner then the next refresh cycle.
     */
    private synchronized void initPolling(int initalDelay) {
        clearPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(this::pollController, initalDelay, refresh, TimeUnit.SECONDS);
    }

    /**
     * Stops/clears this thing's polling future
     */
    private void clearPolling() {
        ScheduledFuture<?> localFuture = pollFuture;
        if (isFutureValid(localFuture)) {
            if (localFuture != null) {
                localFuture.cancel(false);
            }
        }
    }

    private boolean isFutureValid(@Nullable ScheduledFuture<?> future) {
        return future != null && !future.isCancelled();
    }

    /**
     * Poll the controller for updates.
     */
    private void pollController() {
        // ScheduledFuture<?> localFuture = pollFuture;

        try {
            StatusScheduleResponse status = client.getStatusSchedule(controllerId);
            logger.debug("Controller {} last contact {} seconds ago, {} total zones", status.getControllerId(),
                    status.getLastContact(), status.getRelays().size());
            status.getRelays().forEach(r -> {
                String group = "zone" + r.getRelayNumber();
                relayMap.put(group, r);
                logger.trace("Updateing Zone {} {} ", group, r.getName());
                updateState(group, CHANNEL_ZONE_TIME,
                        r.getRunTimeSeconds() != null ? new DecimalType(r.getRunTimeSeconds()) : UnDefType.UNDEF);
                updateState(group, CHANNEL_ZONE_ICON, new StringType(r.getIcon()));
                updateState(group, CHANNEL_ZONE_LAST_WATER, new StringType(r.getLastwater()));
                ZonedDateTime time = ZonedDateTime.now();
                updateState(group, CHANNEL_ZONE_NEXT_RUN_TIME_TIME, new DateTimeType(time.plusSeconds(r.getTime())));
                Optional<Running> running = status.getRunning().stream().filter(z -> z.getRelayId() == r.getRelayId())
                        .findAny();
                if (running.isPresent()) {
                    updateState(group, CHANNEL_ZONE_RUN, OnOffType.ON);
                    updateState(group, CHANNEL_ZONE_TIME_LEFT, new DecimalType(running.get().getTimeLeft()));

                } else {
                    updateState(group, CHANNEL_ZONE_RUN, OnOffType.OFF);
                    updateState(group, CHANNEL_ZONE_TIME_LEFT, new DecimalType(0));

                }
            });
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

        } catch (HydrawiseConnectionException e) {
            // poller will continue to run, set offline until next run
            logger.debug("Exception polling", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (HydrawiseAuthenticationException e) {
            // if are creds are not valid, we need to try re authorizing again
            logger.debug("Authorization Exception during polling", e);
            clearPolling();
            configure();
        }

    }

    private void updateController(Controller controller) {
        updateState("system", CHANNEL_SYSTEM_LASTCONTACT, new DecimalType(controller.getLastContact()));
        updateState("system", CHANNEL_SYSTEM_STATUS, new StringType(controller.getStatus()));
        updateState("system", CHANNEL_SYSTEM_ONLINE,
                controller.getOnline().booleanValue() ? OnOffType.ON : OnOffType.OFF);
    }

    private void updateState(String group, String channelID, State state) {
        String channelName = group + "#" + channelID;
        State oldState = stateMap.put(channelName, state);
        if (!state.equals(oldState)) {
            // logger.debug("updateState updating {} {}", channel, state);
            // updateState(channel, state);
            ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelName);
            logger.debug("updateState updating {} {}", channelUID, state);
            updateState(channelUID, state);
        }
    }
}
