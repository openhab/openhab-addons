/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal.handler;

import static org.openhab.binding.hydrawise.internal.HydrawiseBindingConstants.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCommandException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.local.HydrawiseLocalApiClient;
import org.openhab.binding.hydrawise.internal.api.local.dto.LocalScheduleResponse;
import org.openhab.binding.hydrawise.internal.api.local.dto.Relay;
import org.openhab.binding.hydrawise.internal.api.local.dto.Running;
import org.openhab.binding.hydrawise.internal.config.HydrawiseLocalConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HydrawiseLocalHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HydrawiseLocalHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HydrawiseLocalHandler.class);
    protected final Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());
    protected final Map<String, Relay> relayMap = Collections.synchronizedMap(new HashMap<>());
    private @Nullable ScheduledFuture<?> pollFuture;

    /**
     * value observed being used by the Hydrawise clients as a max time value,
     */
    private static final long MAX_RUN_TIME = 157680000;

    /**
     * Minimum amount of time we can poll for updates
     */
    protected static final int MIN_REFRESH_SECONDS = 5;

    /**
     * Minimum amount of time we can poll after a command
     */
    protected static final int COMMAND_REFRESH_SECONDS = 5;

    /**
     * Our poll rate
     */
    protected int refresh;

    /**
     * Future to poll for updated
     */

    HydrawiseLocalApiClient client;

    public HydrawiseLocalHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        client = new HydrawiseLocalApiClient(httpClient);
    }

    @Override
    public void initialize() {
        scheduler.schedule(this::configureInternal, 0, TimeUnit.SECONDS);
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

    @SuppressWarnings({ "null", "unused" }) // compiler does not like relayMap.get can return null
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Controller is NOT ONLINE and is not responding to commands");
            return;
        }

        // remove our cached state for this, will be safely updated on next poll
        stateMap.remove(channelUID.getAsString());

        if (command instanceof RefreshType) {
            // we already removed this from the cache
            return;
        }

        String group = channelUID.getGroupId();
        String channelId = channelUID.getIdWithoutGroup();
        boolean allCommand = CHANNEL_GROUP_ALLZONES.equals(group);

        Relay relay = relayMap.get(group);
        if (!allCommand && relay == null) {
            logger.debug("Zone not found {}", group);
            return;
        }

        try {
            clearPolling();
            switch (channelId) {
                case CHANNEL_ZONE_RUN_CUSTOM:
                    if (!(command instanceof QuantityType<?>)) {
                        logger.warn("Invalid command type for run custom {}", command.getClass().getName());
                        return;
                    }
                    if (allCommand) {
                        client.runAllRelays(((QuantityType<?>) command).intValue());
                    } else {
                        client.runRelay(((QuantityType<?>) command).intValue(), relay.relay);
                    }
                    break;
                case CHANNEL_ZONE_RUN:
                    if (!(command instanceof OnOffType)) {
                        logger.warn("Invalid command type for run {}", command.getClass().getName());
                        return;
                    }
                    if (allCommand) {
                        if (command == OnOffType.ON) {
                            client.runAllRelays();
                        } else {
                            client.stopAllRelays();
                        }
                    } else {
                        if (command == OnOffType.ON) {
                            client.runRelay(relay.relay);
                        } else {
                            client.stopRelay(relay.relay);
                        }
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
            configureInternal();
        }
    }

    protected void updateZones(LocalScheduleResponse status) {
        ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        status.relays.forEach(r -> {
            String group = "zone" + r.getRelayNumber();
            relayMap.put(group, r);
            logger.trace("Updateing Zone {} {} ", group, r.name);
            updateGroupState(group, CHANNEL_ZONE_NAME, new StringType(r.name));
            updateGroupState(group, CHANNEL_ZONE_TYPE, new DecimalType(r.type));
            updateGroupState(group, CHANNEL_ZONE_STARTTIME,
                    r.runSeconds != null ? new QuantityType<>(r.runSeconds, Units.SECOND) : UnDefType.UNDEF);
            if (r.time >= MAX_RUN_TIME) {
                updateGroupState(group, CHANNEL_ZONE_NEXT_RUN_TIME_TIME, UnDefType.UNDEF);
            } else {
                updateGroupState(group, CHANNEL_ZONE_NEXT_RUN_TIME_TIME,
                        new DateTimeType(now.plusSeconds(r.time).truncatedTo(ChronoUnit.MINUTES)));
            }

            Optional<Running> running = status.running.stream()
                    .filter(z -> Integer.parseInt(z.relayId) == r.relayId.intValue()).findAny();
            if (running.isPresent()) {
                updateGroupState(group, CHANNEL_ZONE_RUN, OnOffType.ON);
                updateGroupState(group, CHANNEL_ZONE_TIME_LEFT,
                        new QuantityType<>(running.get().timeLeft, Units.SECOND));
                logger.debug("{} Time Left {}", r.name, running.get().timeLeft);

            } else {
                updateGroupState(group, CHANNEL_ZONE_RUN, OnOffType.OFF);
                updateGroupState(group, CHANNEL_ZONE_TIME_LEFT, new QuantityType<>(0, Units.SECOND));
            }

            updateGroupState(CHANNEL_GROUP_ALLZONES, CHANNEL_ZONE_RUN,
                    status.running.size() > 0 ? OnOffType.ON : OnOffType.OFF);
        });
    }

    @SuppressWarnings("serial")
    protected class NotConfiguredException extends Exception {
        NotConfiguredException(String message) {
            super(message);
        }
    }

    private boolean isFutureValid(@Nullable ScheduledFuture<?> future) {
        return future != null && !future.isCancelled();
    }

    private void configureInternal() {
        clearPolling();
        stateMap.clear();
        relayMap.clear();
        try {
            HydrawiseLocalConfiguration configuration = getConfig().as(HydrawiseLocalConfiguration.class);
            this.refresh = Math.max(configuration.refresh, MIN_REFRESH_SECONDS);
            logger.trace("Connecting to host {}", configuration.host);
            client.setCredentials(configuration.host, configuration.username, configuration.password);
            LocalScheduleResponse response = client.getLocalSchedule();
            if (response != null) {
                updateZones(response);
                initPolling(refresh);
            } else {
                logger.debug("Could not connect to service");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid response from service");
            }
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
        pollFuture = scheduler.scheduleWithFixedDelay(this::pollControllerInternal, initalDelay, refresh,
                TimeUnit.SECONDS);
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

    /**
     * Poll the controller for updates.
     */
    private void pollControllerInternal() {
        try {
            LocalScheduleResponse response = client.getLocalSchedule();
            if (response != null) {
                updateZones(response);
            }
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (HydrawiseConnectionException e) {
            // poller will continue to run, set offline until next run
            logger.debug("Exception polling", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (HydrawiseAuthenticationException e) {
            // if are creds are not valid, we need to try re authorizing again
            logger.debug("Authorization exception during polling", e);
            configureInternal();
        }
    }

    private void updateGroupState(String group, String channelID, State state) {
        String channelName = group + "#" + channelID;
        State oldState = stateMap.put(channelName, state);
        if (!state.equals(oldState)) {
            ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelName);
            logger.debug("updateState updating {} {}", channelUID, state);
            updateState(channelUID, state);
        }
    }
}
