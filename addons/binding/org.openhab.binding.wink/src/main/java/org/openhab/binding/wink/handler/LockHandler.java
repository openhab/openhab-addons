/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import static org.openhab.binding.wink.WinkBindingConstants.CHANNEL_LOCKSTATE;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.WinkSupportedDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles locks connected to the wink hub
 *
 * @author Shawn Crosby
 *
 */
public class LockHandler extends WinkBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LockHandler.class);

    public LockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleWinkCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LOCKSTATE)) {
            if (command.equals(OnOffType.ON)) {
                logger.debug("Locking Thing: {}", this.thing.getLabel());
                setLock(true);
            } else if (command.equals(OnOffType.OFF)) {
                logger.debug("UnLocking Thing: {}", this.thing.getLabel());
                setLock(false);
            } else if (command instanceof RefreshType) {
                logger.debug("Refreshing state of Thing: {}", this.thing.getLabel());
                updateDeviceState(getDevice());
            }
        }
    }

    private void setLock(boolean lock) {
        IWinkDevice device = getDevice();
        if (lock) {
            bridgeHandler.lockDevice(device);
        } else {
            bridgeHandler.unLockDevice(device);
        }
    }

    @Override
    protected void updateDeviceState(IWinkDevice device) {
        String desired = device.getDesiredState().get("locked");
        String current = device.getCurrentState().get("locked");
        if (desired == null || desired.equals(current)) {
            if (current.equals("true")) {
                logger.debug("LOCKSTATE is ON");
                updateState(CHANNEL_LOCKSTATE, OnOffType.ON);
            } else {
                logger.debug("LOCKSTATE is OFF");
                updateState(CHANNEL_LOCKSTATE, OnOffType.OFF);
            }
        }
    }

    @Override
    protected WinkSupportedDevice getDeviceType() {
        return WinkSupportedDevice.LOCK;
    }

}
