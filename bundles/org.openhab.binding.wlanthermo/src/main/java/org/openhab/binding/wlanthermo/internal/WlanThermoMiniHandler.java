/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wlanthermo.internal;

import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wlanthermo.internal.api.mini.builtin.App;
import org.openhab.binding.wlanthermo.internal.api.mini.builtin.WlanThermoMiniCommandHandler;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link WlanThermoMiniHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoMiniHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WlanThermoMiniHandler.class);
    private final WlanThermoMiniCommandHandler wlanThermoMiniCommandHandler = new WlanThermoMiniCommandHandler();

    private WlanThermoMiniConfiguration config = new WlanThermoMiniConfiguration();
    private final HttpClient httpClient;
    private @Nullable ScheduledFuture<?> pollingScheduler;
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(WlanThermoBindingConstants.WLANTHERMO_THREAD_POOL);
    private final Gson gson = new Gson();
    private App app = new App();

    public WlanThermoMiniHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing WlanThermo Mini!");
        config = getConfigAs(WlanThermoMiniConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.schedule(this::checkConnection, config.getPollingInterval(), TimeUnit.SECONDS);

        logger.debug("Finished initializing WlanThermo Mini!");
    }

    private void checkConnection() {
        try {
            if (httpClient.GET(config.getUri("/app.php")).getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
                ScheduledFuture<?> oldScheduler = pollingScheduler;
                if (oldScheduler != null) {
                    oldScheduler.cancel(false);
                }
                pollingScheduler = scheduler.scheduleWithFixedDelay(this::update, 0, config.getPollingInterval(),
                        TimeUnit.SECONDS);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "WlanThermo not found under given address.");
            }
        } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Failed to connect.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to WlanThermo at " + config.getIpAddress());
            ScheduledFuture<?> oldScheduler = pollingScheduler;
            if (oldScheduler != null) {
                oldScheduler.cancel(false);
            }
            pollingScheduler = scheduler.schedule(this::checkConnection, config.getPollingInterval(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            State s = wlanThermoMiniCommandHandler.getState(channelUID, app);
            if (s != null)
                updateState(channelUID, s);
        }
        // Mini is read only!
    }

    private void update() {
        try {
            // Update objects with data from device
            String json = httpClient.GET(config.getUri("/app.php")).getContentAsString();
            app = Objects.requireNonNull(gson.fromJson(json, App.class));
            logger.debug("Received at /app.php: {}", json);

            // Update channels
            for (Channel channel : thing.getChannels()) {
                State state = wlanThermoMiniCommandHandler.getState(channel.getUID(), app);
                if (state != null) {
                    updateState(channel.getUID(), state);
                } else {
                    String trigger = wlanThermoMiniCommandHandler.getTrigger(channel.getUID(), app);
                    if (trigger != null) {
                        triggerChannel(channel.getUID(), trigger);
                    }
                }

            }

        } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Update failed, checking connection", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Update failed, reconnecting...");
            ScheduledFuture<?> oldScheduler = pollingScheduler;
            if (oldScheduler != null) {
                oldScheduler.cancel(false);
            }
            for (Channel channel : thing.getChannels()) {
                updateState(channel.getUID(), UnDefType.UNDEF);
            }
            checkConnection();
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> oldScheduler = pollingScheduler;
        if (oldScheduler != null) {
            boolean stopped = oldScheduler.cancel(true);
            logger.debug("Stopped polling: {}", stopped);
        }
        pollingScheduler = null;
    }
}
