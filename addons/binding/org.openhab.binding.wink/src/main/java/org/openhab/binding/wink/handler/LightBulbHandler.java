/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import static org.openhab.binding.wink.WinkBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.WinkSupportedDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LightBulbHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastien Marchand - Initial contribution
 */
public class LightBulbHandler extends WinkBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LightBulbHandler.class);

    public LightBulbHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleWinkCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            if (command instanceof Number) {
                logger.debug("Setting brightness {}", command);
                int level = ((Number) command).intValue();
                setLightLevel(level);
            } else if (command.equals(OnOffType.ON)) {
                logger.debug("Setting full power");
                setLightLevel(100);
            } else if (command.equals(OnOffType.OFF)) {
                logger.debug("Turning off light");
                setLightLevel(0);
            } else if (command instanceof RefreshType) {
                logger.debug("Refreshing state");
                updateDeviceState(getDevice());
            }
        } else if (channelUID.getId().equals(CHANNEL_LIGHTSTATE)) {
            if (command.equals(OnOffType.ON)) {
                logger.debug("Turning Light On");
                bridgeHandler.switchOnDevice(getDevice());
            } else {
                logger.debug("Turning Light Off");
                bridgeHandler.switchOffDevice(getDevice());
            }
        }
    }

    private void setLightLevel(int level) {
        if (level > 0) {
            bridgeHandler.setDeviceDimmerLevel(getDevice(), level);
            bridgeHandler.switchOnDevice(getDevice());
        } else {
            bridgeHandler.switchOffDevice(getDevice());
        }
    }

    @Override
    protected WinkSupportedDevice getDeviceType() {
        return WinkSupportedDevice.DIMMABLE_LIGHT;
    }

    @Override
    protected void updateDeviceState(IWinkDevice device) {
        final String desiredBrightness = device.getDesiredState().get("brightness");
        final String currentBrightness = device.getCurrentState().get("brightness");
        if (desiredBrightness != null && desiredBrightness.equals(currentBrightness)) {
            Float brightness = Float.valueOf(currentBrightness) * 100;
            logger.debug("New brightness state: {}", brightness);
            updateState(CHANNEL_LIGHTLEVEL, new PercentType(brightness.intValue()));
        }
        final String desiredPowerState = device.getDesiredState().get("powered");
        final String currentPowerState = device.getCurrentState().get("powered");

        if (desiredPowerState == null || desiredPowerState.equals(currentPowerState)) {
            if (currentPowerState.equals("true")) {
                logger.debug("New Light State: ON");
                updateState(CHANNEL_LIGHTSTATE, OnOffType.ON);
            } else {
                logger.debug("New Light State: OFF");
                updateState(CHANNEL_LIGHTSTATE, OnOffType.OFF);
            }
        }
    }
}
