/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.handler;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.openhab.binding.blebox.internal.devices.LightBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LightBoxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class LightBoxHandler extends BaseHandler {
    private Logger logger = LoggerFactory.getLogger(LightBoxHandler.class);
    private LightBox lightBox;

    public LightBoxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case BleboxBindingConstants.CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        lightBox.setColor(HSBType.BLACK);
                    } else {
                        lightBox.setColor(hsbCommand);
                    }
                } else if (command instanceof PercentType) {
                    lightBox.setWhiteBrightness((PercentType) command);
                } else if (command instanceof OnOffType) {
                    if (((OnOffType) command) == OnOffType.ON) {
                        lightBox.setColor(lightBox.LastKnownColor != null ? lightBox.LastKnownColor : HSBType.BLUE);
                    } else {
                        lightBox.setColor(HSBType.BLACK);
                    }
                }
                break;
            case BleboxBindingConstants.CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    lightBox.setWhiteBrightness((PercentType) command);
                } else if (command instanceof OnOffType) {
                    lightBox.setWhiteBrightness((OnOffType) command);
                }
                break;
        }
    }

    @Override
    void initializeDevice(String ipAddress) {
        lightBox = new LightBox(ipAddress);
    }

    @Override
    void updateDeviceStatus() {
        if (lightBox != null) {
            LightBox.StateResponse state = lightBox.getStatus();

            if (state != null) {
                updateState(BleboxBindingConstants.CHANNEL_BRIGHTNESS, state.getWhiteBrightness());
                updateState(BleboxBindingConstants.CHANNEL_COLOR, state.getColor());

                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }
}
