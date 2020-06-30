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
package org.openhab.binding.hdpowerview.internal.handler;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;
import static org.openhab.binding.hdpowerview.internal.api.PosKind.*;
import static org.openhab.binding.hdpowerview.internal.api.PosSeq.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.api.PosKind;
import org.openhab.binding.hdpowerview.internal.api.PosSeq;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.responses.Shade;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles commands for an HD Power View shade
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
@NonNullByDefault
public class HDPowerViewShadeHandler extends AbstractHubbedThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewShadeHandler.class);

    private static final int REFRESH_DELAY = 10;
    private @Nullable ScheduledFuture<?> refreshFuture = null;

    public HDPowerViewShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            getShadeId();
        } catch (NumberFormatException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration 'id' not a valid integer");
            return;
        }
        if (getBridgeHandler() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hub not configured");
            return;
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            requestRefreshShade();
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_SHADE_POSITION:
                if (command instanceof PercentType) {
                    moveShade(PRIMARY, REGULAR, ((PercentType) command).intValue());
                } else if (command instanceof UpDownType) {
                    moveShade(PRIMARY, REGULAR, UpDownType.UP.equals(command) ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    if (StopMoveType.STOP.equals(command)) {
                        stopShade();
                    } else {
                        logger.warn("Unexpected StopMoveType command");
                    }
                }
                break;

            case CHANNEL_SHADE_VANE:
                if (command instanceof PercentType) {
                    moveShade(PRIMARY, VANE, ((PercentType) command).intValue());
                } else if (command instanceof OnOffType) {
                    moveShade(PRIMARY, VANE, OnOffType.ON.equals(command) ? 100 : 0);
                }
                break;

            case CHANNEL_SHADE_SECONDARY_POSITION:
                if (command instanceof PercentType) {
                    moveShade(SECONDARY, INVERTED, ((PercentType) command).intValue());
                } else if (command instanceof UpDownType) {
                    moveShade(SECONDARY, INVERTED, UpDownType.UP.equals(command) ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    if (StopMoveType.STOP.equals(command)) {
                        stopShade();
                    } else {
                        logger.warn("Unexpected StopMoveType command");
                    }
                }
                break;
        }
    }

    void onReceiveUpdate(@Nullable ShadeData shadeData) {
        if (shadeData != null) {
            updateStatus(ThingStatus.ONLINE);
            updateBindingStates(shadeData.positions);
            updateState(CHANNEL_SHADE_LOW_BATTERY, shadeData.batteryStatus < 2 ? OnOffType.ON : OnOffType.OFF);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private void updateBindingStates(@Nullable ShadePosition shadePos) {
        if (shadePos != null) {
            updateState(CHANNEL_SHADE_POSITION, shadePos.getState(PRIMARY, REGULAR));
            updateState(CHANNEL_SHADE_VANE, shadePos.getState(PRIMARY, VANE));
            updateState(CHANNEL_SHADE_SECONDARY_POSITION, shadePos.getState(SECONDARY, INVERTED));
        } else {
            updateState(CHANNEL_SHADE_POSITION, UnDefType.UNDEF);
            updateState(CHANNEL_SHADE_VANE, UnDefType.UNDEF);
            updateState(CHANNEL_SHADE_SECONDARY_POSITION, UnDefType.UNDEF);
        }
    }

    private void moveShade(PosSeq seq, PosKind kind, int percent) {
        try {
            HDPowerViewHubHandler bridge;
            if ((bridge = getBridgeHandler()) == null) {
                throw new ProcessingException("Missing bridge handler");
            }
            HDPowerViewWebTargets webTargets = bridge.getWebTargets();
            if (webTargets == null) {
                throw new ProcessingException("Web targets not initialized");
            }
            int shadeId = getShadeId();
            switch (seq) {
                case PRIMARY:
                    webTargets.moveShade(shadeId, ShadePosition.create(kind, percent));
                    break;
                case SECONDARY:
                    webTargets.moveShade(shadeId, ShadePosition.create(REGULAR, 100, INVERTED, percent));
            }
        } catch (ProcessingException | NumberFormatException e) {
            logger.warn("Unexpected error: {}", e.getMessage());
            return;
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
            return;
        }
    }

    private int getShadeId() throws NumberFormatException {
        return Integer.parseInt(getConfigAs(HDPowerViewShadeConfiguration.class).id);
    }

    private void stopShade() {
        try {
            HDPowerViewHubHandler bridge;
            if ((bridge = getBridgeHandler()) == null) {
                throw new ProcessingException("Missing bridge handler");
            }
            HDPowerViewWebTargets webTargets = bridge.getWebTargets();
            if (webTargets == null) {
                throw new ProcessingException("Web targets not initialized");
            }
            int shadeId = getShadeId();
            webTargets.stopShade(shadeId);
        } catch (ProcessingException | NumberFormatException e) {
            logger.warn("Unexpected error: {}", e.getMessage());
            return;
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
            return;
        }
    }

    public synchronized void requestRefreshShade() {
        if (refreshFuture == null) {
            refreshFuture = scheduler.schedule(this::doRefreshShade, REFRESH_DELAY, TimeUnit.SECONDS);
        }
    }

    private void doRefreshShade() {
        try {
            HDPowerViewHubHandler bridge;
            if ((bridge = getBridgeHandler()) == null) {
                throw new ProcessingException("Missing bridge handler");
            }
            HDPowerViewWebTargets webTargets = bridge.getWebTargets();
            if (webTargets == null) {
                throw new ProcessingException("Web targets not initialized");
            }
            int shadeId = getShadeId();
            Shade shade = webTargets.refreshShade(shadeId);
            if (shade != null) {
                ShadeData shadeData = shade.shade;
                if (shadeData != null) {
                    if (Boolean.TRUE.equals(shadeData.timedOut)) {
                        logger.warn("Shade {} wireless refresh time out", shadeId);
                    }
                }
            }
        } catch (ProcessingException | NumberFormatException e) {
            logger.warn("Unexpected error: {}", e.getMessage());
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
        }
        refreshFuture = null;
    }
}
