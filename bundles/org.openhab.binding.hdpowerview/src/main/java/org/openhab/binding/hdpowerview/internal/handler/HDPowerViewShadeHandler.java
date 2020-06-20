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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.ShadePositionKind;
import org.openhab.binding.hdpowerview.internal.api.responses.ShadeSingleton;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.Shade;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles commands for an HD Power View shade
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 * 
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
        switch (channelUID.getId()) {
            case CHANNEL_SHADE_POSITION:
                if (command instanceof PercentType) {
                    setPosition(ShadePositionKind.PRIMARY, ((PercentType) command).intValue());
                } else if (command instanceof UpDownType) {
                    setPosition(ShadePositionKind.PRIMARY, ((UpDownType) command).equals(UpDownType.UP) ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    logger.warn("PowerView shades do not support StopMove commands");
                }
                break;

            case CHANNEL_SHADE_SECONDARY_POSITION:
                if (command instanceof PercentType) {
                    setPosition(ShadePositionKind.SECONDARY, ((PercentType) command).intValue());
                } else if (command instanceof UpDownType) {
                    setPosition(ShadePositionKind.SECONDARY, ((UpDownType) command).equals(UpDownType.UP) ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    logger.warn("PowerView shades do not support StopMove commands");
                }
                break;

            case CHANNEL_SHADE_VANE:
                if (command instanceof PercentType) {
                    setPosition(ShadePositionKind.VANE, ((PercentType) command).intValue());
                } else if (command instanceof OnOffType) {
                    setPosition(ShadePositionKind.VANE, ((OnOffType) command).equals(OnOffType.ON) ? 100 : 0);
                }
                break;
        }
    }

    void onReceiveUpdate(Shade shade) {
        updateStatus(ThingStatus.ONLINE);
        updatePosition(shade.positions);
        updateState(CHANNEL_SHADE_LOW_BATTERY, shade.batteryStatus < 2 ? OnOffType.ON : OnOffType.OFF);
    }

    private void updatePosition(ShadePosition pos) {
        if (pos != null) {
            updateState(CHANNEL_SHADE_POSITION, pos.getPercent(ShadePositionKind.PRIMARY));
            updateState(CHANNEL_SHADE_SECONDARY_POSITION, pos.getPercent(ShadePositionKind.SECONDARY));
            updateState(CHANNEL_SHADE_VANE, pos.getPercent(ShadePositionKind.VANE));
        } else {
            updateState(CHANNEL_SHADE_POSITION, UnDefType.UNDEF);
            updateState(CHANNEL_SHADE_SECONDARY_POSITION, UnDefType.UNDEF);
            updateState(CHANNEL_SHADE_VANE, UnDefType.UNDEF);
        }
    }

    private void setPosition(ShadePositionKind kind, int posValue) {
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
                ShadeSingleton oldShade = webTargets.refreshShade(shadeId);
                if (oldShade != null) {
                    newPos = ShadePosition.create(kind, 0, posValue).copyPrimaryFrom(oldShade.shadeData.positions);
                    webTargets.moveShade(shadeId, newPos);
                }
            } else {
                newPos = ShadePosition.create(kind, posValue);
                webTargets.moveShade(shadeId, newPos);
            }
        } catch (ProcessingException e) {
            logger.warn("Unexpected error: {}", e.getMessage());
            return;
        } catch (HubMaintenanceException e) {
            logger.debug("Hub temporariliy down for maintenance");
            return;
        }
        updatePosition(newPos);
    }

    private String getShadeId() {
        return getConfigAs(HDPowerViewShadeConfiguration.class).id;
    }
}
