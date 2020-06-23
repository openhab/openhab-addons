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

import javax.ws.rs.ProcessingException;

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
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.ShadePositionKind;
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
public class HDPowerViewShadeHandler extends AbstractHubbedThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewShadeHandler.class);

    public HDPowerViewShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            HDPowerViewHubHandler bridge = getBridgeHandler();
            if (bridge != null) {
                HDPowerViewWebTargets webTargets = bridge.getWebTargets();
                if (webTargets != null) {
                    try {
                        webTargets.refreshShade(getShadeId());
                    } catch (ProcessingException e) {
                        logger.warn("Unexpected error: {}", e.getMessage());
                    } catch (HubMaintenanceException e) {
                        logger.debug("Hub temporariliy down for maintenance");
                    }
                }
            }
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_SHADE_POSITION:
                if (command instanceof PercentType) {
                    setShadePercentPosition(ShadePositionKind.PRIMARY, ((PercentType) command).intValue());
                } else if (command instanceof UpDownType) {
                    setShadePercentPosition(ShadePositionKind.PRIMARY,
                            ((UpDownType) command).equals(UpDownType.UP) ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    logger.warn("PowerView shades do not support StopMove commands");
                }
                break;

            case CHANNEL_SHADE_SECONDARY_POSITION:
                if (command instanceof PercentType) {
                    setShadePercentPosition(ShadePositionKind.SECONDARY, ((PercentType) command).intValue());
                } else if (command instanceof UpDownType) {
                    setShadePercentPosition(ShadePositionKind.SECONDARY,
                            ((UpDownType) command).equals(UpDownType.UP) ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    logger.warn("PowerView shades do not support StopMove commands");
                }
                break;

            case CHANNEL_SHADE_VANE:
                if (command instanceof PercentType) {
                    setShadePercentPosition(ShadePositionKind.VANE, ((PercentType) command).intValue());
                } else if (command instanceof OnOffType) {
                    setShadePercentPosition(ShadePositionKind.VANE,
                            ((OnOffType) command).equals(OnOffType.ON) ? 100 : 0);
                }
                break;
        }
    }

    void onReceiveUpdate(ShadeData shadeData) {
        if (shadeData != null) {
            updateStatus(ThingStatus.ONLINE);
            updateBindingPercentPositions(shadeData.positions);
            updateState(CHANNEL_SHADE_LOW_BATTERY, shadeData.batteryStatus < 2 ? OnOffType.ON : OnOffType.OFF);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private void updateBindingPercentPositions(ShadePosition shadePositions) {
        if (shadePositions != null) {
            updateState(CHANNEL_SHADE_POSITION, shadePositions.getPercent(ShadePositionKind.PRIMARY));
            updateState(CHANNEL_SHADE_SECONDARY_POSITION, shadePositions.getPercent(ShadePositionKind.SECONDARY));
            updateState(CHANNEL_SHADE_VANE, shadePositions.getPercent(ShadePositionKind.VANE));
        } else {
            updateState(CHANNEL_SHADE_POSITION, UnDefType.UNDEF);
            updateState(CHANNEL_SHADE_SECONDARY_POSITION, UnDefType.UNDEF);
            updateState(CHANNEL_SHADE_VANE, UnDefType.UNDEF);
        }
    }

    private void setShadePercentPosition(ShadePositionKind kind, int percent) {
        HDPowerViewHubHandler bridge;
        if ((bridge = getBridgeHandler()) == null) {
            return;
        }
        HDPowerViewWebTargets webTargets = bridge.getWebTargets();
        String shadeId = getShadeId();
        @Nullable
        ShadePosition newPos = null;
        try {
            if (kind == ShadePositionKind.SECONDARY) {
                @Nullable
                Shade oldShade = webTargets.refreshShade(shadeId);
                if (oldShade != null) {
                    @Nullable
                    ShadeData oldData = oldShade.shade;
                    if (oldData != null && oldData.positions != null) {
                        newPos = ShadePosition.create(kind, 0, percent).copyPrimaryFrom(oldData.positions);
                        webTargets.moveShade(shadeId, newPos);
                    }
                }
            } else {
                newPos = ShadePosition.create(kind, percent);
                webTargets.moveShade(shadeId, newPos);
            }
        } catch (ProcessingException e) {
            logger.warn("Unexpected error: {}", e.getMessage());
            return;
        } catch (HubMaintenanceException e) {
            logger.debug("Hub temporariliy down for maintenance");
            return;
        }
        updateBindingPercentPositions(newPos);
    }

    private String getShadeId() {
        return getConfigAs(HDPowerViewShadeConfiguration.class).id;
    }
}
