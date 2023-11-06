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
package org.openhab.binding.energenie.internal.handler;

import static org.openhab.binding.energenie.internal.EnergenieBindingConstants.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energenie.internal.EnergenieProtocolEnum;
import org.openhab.binding.energenie.internal.config.EnergenieConfiguration;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergenieHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class EnergenieHandler extends BaseThingHandler {

    private static final String CHANNEL_SOCKET_PREFIX = "socket";
    private final Logger logger = LoggerFactory.getLogger(EnergenieHandler.class);

    /**
     * Use cache for refresh command to not update again when call is made within 4 seconds of previous call.
     */
    private final ExpiringCache<Boolean> refreshCache = new ExpiringCache<>(Duration.ofSeconds(5), this::refreshState);
    private final String statusOn;

    private @Nullable EnergenieSocket energenieSocket;
    private @Nullable ScheduledFuture<?> refreshJob;

    private int refreshInterval;
    private @Nullable String host;

    public EnergenieHandler(final Thing thing, final EnergenieProtocolEnum protocol) {
        super(thing);
        this.statusOn = protocol.getStatusOn();
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            refreshCache.getValue();
        } else if (command instanceof OnOffType) {
            final byte[] ctrl = { DONT_SWITCH, DONT_SWITCH, DONT_SWITCH, DONT_SWITCH };
            final Matcher matcher = CHANNEL_SOCKET.matcher(channelUID.getId());

            try {
                boolean found = false;

                if (matcher.find()) {
                    final int index = Integer.parseInt(matcher.group(1)) - 1;

                    if (index >= 0 && index < SOCKET_COUNT) {
                        ctrl[index] = OnOffType.ON.equals(command) ? SWITCH_ON : SWITCH_OFF;
                        stateUpdate(energenieSocket.sendCommand(ctrl));
                        found = true;
                    }
                }
                if (!found) {
                    logger.debug("Invalid channel id {}, should be value between 1 and {}", channelUID, SOCKET_COUNT);
                }
            } catch (final IOException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("Couldn't get I/O for the connection to: {}:{}.", host, TCP_PORT, e);
            }
        }
    }

    @Override
    public void initialize() {
        final EnergenieConfiguration config = getConfigAs(EnergenieConfiguration.class);

        if (!config.host.isEmpty() && !config.password.isEmpty()) {
            refreshInterval = EnergenieConfiguration.DEFAULT_REFRESH_INTERVAL;
            host = config.host;
            logger.debug("Initializing EnergenieHandler for Host '{}'", config.host);
            energenieSocket = new EnergenieSocket(config.host, config.password);

            updateStatus(ThingStatus.UNKNOWN);
            refreshJob = scheduler.scheduleWithFixedDelay(this::refreshState, 6, refreshInterval, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access device , IP-Address or password not set");
        }
    }

    @Override
    public void dispose() {
        logger.debug("EnergenieHandler disposed.");
        final ScheduledFuture<?> refreshJob = this.refreshJob;

        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    private boolean refreshState() {
        final EnergenieSocket socket = this.energenieSocket;

        if (socket != null) {
            try {
                stateUpdate(socket.retrieveStatus());
                if (thing.getStatus() != ThingStatus.ONLINE && ThingHandlerHelper.isHandlerInitialized(thing)) {
                    updateStatus(ThingStatus.ONLINE);
                }
                return true;
            } catch (final UnknownHostException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Can't find host: " + e.getMessage());
            } catch (final IOException e) {
                logger.debug("Couldn't get I/O for the connection to: {}:{}.", host, TCP_PORT, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Couldn't get I/O for the connection");
            } catch (final RuntimeException e) {
                logger.debug("Unexpected error", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            }
        }
        return false;
    }

    public void stateUpdate(final byte[] status) {
        for (int i = 0; i < 4; i++) {
            final String socket = CHANNEL_SOCKET_PREFIX + (i + 1);
            final String stringStatus = String.format("0x%02x", status[i]);
            updateState(socket, OnOffType.from(stringStatus.equals(statusOn)));
        }
    }
}
