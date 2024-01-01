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
package org.openhab.binding.lgwebos.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVSocket.State;
import org.openhab.binding.lgwebos.internal.handler.core.CommandConfirmation;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Power Control Command.
 * Note: Connect SDK only supports powering OFF for most devices.
 *
 * @author Sebastian Prehn - Initial contribution
 */
@NonNullByDefault
public class PowerControlPower extends BaseChannelHandler<CommandConfirmation> {
    private static final int WOL_PACKET_RETRY_COUNT = 10;
    private static final int WOL_PACKET_RETRY_DELAY_MILLIS = 100;

    private final Logger logger = LoggerFactory.getLogger(PowerControlPower.class);
    private final ConfigProvider configProvider;
    private final ScheduledExecutorService scheduler;

    public PowerControlPower(ConfigProvider configProvider, ScheduledExecutorService scheduler) {
        this.configProvider = configProvider;
        this.scheduler = scheduler;
    }

    @Override
    public void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command) {
        final State state = handler.getSocket().getState();
        if (RefreshType.REFRESH == command) {
            handler.postUpdate(channelId, OnOffType.from(state == State.REGISTERED));
        } else if (OnOffType.ON == command) {
            switch (state) {
                case CONNECTING:
                case REGISTERING:
                    logger.debug("Received ON - TV is currently connecting.");
                    handler.postUpdate(channelId, OnOffType.OFF);
                    break;
                case REGISTERED:
                    logger.debug("Received ON - TV is already on.");
                    break;
                case DISCONNECTING: // WOL will not stop the shutdown process, but we must not update the state to ON
                case DISCONNECTED:
                    String macAddress = configProvider.getMacAddress();
                    if (macAddress.isEmpty()) {
                        logger.debug("""
                                Received ON - Turning TV on via API is not supported by LG WebOS TVs. \
                                You may succeed using wake on lan (WOL). \
                                Please set the macAddress config value in Thing configuration to enable this.\
                                """);
                        handler.postUpdate(channelId, OnOffType.OFF);
                    } else {
                        for (int i = 0; i < WOL_PACKET_RETRY_COUNT; i++) {
                            scheduler.schedule(() -> {
                                try {
                                    WakeOnLanUtility.sendWOLPacket(macAddress);
                                } catch (IllegalArgumentException e) {
                                    logger.debug("Failed to send WOL packet: {}", e.getMessage());
                                }
                            }, i * WOL_PACKET_RETRY_DELAY_MILLIS, TimeUnit.MILLISECONDS);
                        }
                    }
                    break;
            }
        } else if (OnOffType.OFF == command) {
            switch (state) {
                case CONNECTING:
                case REGISTERING:
                    // in both states no message will sent to TV, thus the operation won't have an effect
                    logger.debug("Received OFF - TV is currently connecting.");
                    break;
                case REGISTERED:
                    handler.getSocket().powerOff(getDefaultResponseListener());
                    break;
                case DISCONNECTING:
                case DISCONNECTED:
                    logger.debug("Received OFF - TV is already off.");
                    break;
            }
        } else {
            logger.info("Only accept OnOffType, RefreshType. Type was {}.", command.getClass());
        }
    }

    @Override
    public void onDeviceReady(String channelId, LGWebOSHandler handler) {
        handler.postUpdate(channelId, OnOffType.ON);
    }

    @Override
    public void onDeviceRemoved(String channelId, LGWebOSHandler handler) {
        handler.postUpdate(channelId, OnOffType.OFF);
    }

    public interface ConfigProvider {
        String getMacAddress();
    }
}
