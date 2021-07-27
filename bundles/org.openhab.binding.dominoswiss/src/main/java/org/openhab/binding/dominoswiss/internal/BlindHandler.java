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
package org.openhab.binding.dominoswiss.internal;

import static org.openhab.binding.dominoswiss.internal.dominoswissBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlindHandler} is responsible for handling commands, which are
 * sent to one of the channels.The class defines common constants, which are
 * used across the whole binding
 *
 * @author Frieso Aeschbacher - Initial contribution
 */
@NonNullByDefault
public class blindHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(blindHandler.class);

    private @Nullable eGateHandler dominoswissHandler;

    private String id = "";

    public blindHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Blind got command: {} and ChannelUID: {} ", command.toFullString(),
                channelUID.getIdWithoutGroup());
        dominoswissHandler = (eGateHandler) getBridge().getHandler();
        if (dominoswissHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "EGate not available");
            logger.debug("Blind thing {} has no server configured, ignoring command: {}", getThing().getUID(), command);
            return;
        }

        // Some of the code below is not designed to handle REFRESH
        if (command == RefreshType.REFRESH) {
            return;
        }
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_PULSEUP:
                if (command instanceof Number) {
                    dominoswissHandler.pulseUp(id);
                }
                break;
            case CHANNEL_PULSEDOWN:
                if (command instanceof Number) {
                    dominoswissHandler.pulseDown(id);
                }
                break;
            case CHANNEL_CONTINOUSUP:
                if (command instanceof Number) {
                    dominoswissHandler.continuousUp(id);
                }
                break;
            case CHANNEL_CONTINOUSDOWN:
                if (command instanceof Number) {
                    dominoswissHandler.continuousDown(id);
                }
                break;
            case CHANNEL_STOP:
                if (command instanceof Number) {
                    dominoswissHandler.stop(id);
                }
                break;
            case UP:
                if (command instanceof Number) {
                    dominoswissHandler.continuousUp(id);
                }
                break;
            case DOWN:
                if (command instanceof Number) {
                    dominoswissHandler.continuousDown(id);
                }
                break;
            case SHUTTER:
                if (command.toFullString() == DOWN) {
                    dominoswissHandler.continuousDown(id);
                } else if (command.toFullString() == UP) {
                    dominoswissHandler.continuousUp(id);
                } else if (command.toFullString() == CHANNEL_STOP) {
                    dominoswissHandler.stop(id);
                } else {
                    logger.debug("Blind got command but nothing executed: {}  and ChannelUID: {}",
                            command.toFullString(), channelUID.getIdWithoutGroup());
                }

            case TILTDOWN:
                if (command instanceof Number) {
                    try {
                        dominoswissHandler.tiltDown(id);
                    } catch (InterruptedException e) {
                        logger.error("EGate tiltDown error: {} ", e.toString());
                    }
                }
                break;

            case TILTUP:
                if (command instanceof Number) {
                    try {
                        dominoswissHandler.tiltUp(id);
                    } catch (InterruptedException e) {
                        logger.error("EGate tiltUP error: {} ", e.toString());
                    }
                }
                break;

            case SHUTTERTILT:
                if (command.toFullString() == UP) {
                    dominoswissHandler.pulseUp(id);
                } else if (command.toFullString() == DOWN) {
                    dominoswissHandler.pulseDown(id);
                } else if (command.toFullString() == CHANNEL_STOP) {
                    dominoswissHandler.stop(id);
                } else {
                    logger.debug("Blind got command but nothing executed: {}  and ChannelUID: {}",
                            command.toFullString(), channelUID.getIdWithoutGroup());
                }

            default:
                break;
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        this.id = getConfig().as(blindConfig.class).id;
        dominoswissHandler = (eGateHandler) getBridge().getHandler();
        dominoswissHandler.registerBlind(this.id, getThing().getUID());
        try {
            ThingStatus bridgeStatus = getBridge().getStatus();
            if (bridgeStatus == ThingStatus.ONLINE && getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                dominoswissHandler = (eGateHandler) getBridge().getHandler();
            } else if (bridgeStatus == ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } catch (Exception e) {
            logger.error("Could not update ThingStatus ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.toString());

        }
    }

    /*
     * Gets the ID of this Blind
     */
    public String getID() {
        return this.id;
    }
}
