/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.handler;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.hdpowerview.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.Shade;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles commands for an HD Power View shade
 *
 * @author Andy Lintner - Initial contribution
 */
public class HDPowerViewShadeHandler extends AbstractHubbedThingHandler {

    private static final int MAX_POSITION = 65535;
    private static final int MAX_VANE = 32767;

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewShadeHandler.class);

    public HDPowerViewShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        getBridgeHandler().pollNow();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case HDPowerViewBindingConstants.CHANNEL_SHADE_POSITION:
                if (command instanceof PercentType) {
                    setPosition(((PercentType) command).intValue());
                } else if (command instanceof UpDownType) {
                    setPosition(((UpDownType) command).equals(UpDownType.UP) ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    logger.warn("PowerView shades do not support StopMove commands");
                }
                break;

            case HDPowerViewBindingConstants.CHANNEL_SHADE_VANE:
                if (command instanceof PercentType) {
                    setVane(((PercentType) command).intValue());
                } else if (command instanceof OnOffType) {
                    setPosition(ShadePosition.forVane(((OnOffType) command).equals(OnOffType.ON) ? MAX_VANE : 0));
                }
                break;
        }
    }

    void onReceiveUpdate(Shade shade) {
        updateStatus(ThingStatus.ONLINE);
        updatePosition(shade.positions);
        updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_LOW_BATTERY,
                shade.batteryStatus < 2 ? OnOffType.ON : OnOffType.OFF);
    }

    private void updatePosition(ShadePosition pos) {
        if (pos != null) {
            updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_POSITION,
                    new PercentType(100 - (int) Math.round(((double) pos.getPosition()) / MAX_POSITION * 100)));
            updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_VANE,
                    new PercentType((int) Math.round(((double) pos.getVane()) / MAX_VANE * 100)));
        } else {
            updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_POSITION, UnDefType.UNDEF);
            updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_VANE, UnDefType.UNDEF);
        }
    }

    private void setPosition(int percent) {
        ShadePosition position = ShadePosition
                .forPosition(MAX_POSITION - (int) Math.round(percent / 100d * MAX_POSITION));
        setPosition(position);
    }

    private void setVane(int value) {
        ShadePosition position = ShadePosition.forVane((int) Math.round(value / 100d * MAX_VANE));
        setPosition(position);
    }

    private void setPosition(ShadePosition position) {
        HDPowerViewHubHandler bridge;
        if ((bridge = getBridgeHandler()) == null) {
            return;
        }
        String shadeId = getShadeId();
        Response response;
        try {
            response = bridge.getWebTargets().moveShade(shadeId, position);
        } catch (IOException e) {
            logger.error("{}", e.getMessage(), e);
            return;
        }
        if (response != null) {
            updatePosition(position);
        }
    }

    private String getShadeId() {
        return getConfigAs(HDPowerViewShadeConfiguration.class).id;
    }

}
