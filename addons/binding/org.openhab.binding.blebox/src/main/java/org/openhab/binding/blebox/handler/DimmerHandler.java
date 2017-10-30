/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.handler;

import static org.openhab.binding.blebox.BleboxBindingConstants.CHANNEL_BRIGHTNESS;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blebox.internal.devices.Dimmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class DimmerHandler extends BaseHandler {

    private Logger logger = LoggerFactory.getLogger(DimmerHandler.class);
    private Dimmer dimmer;

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_BRIGHTNESS)) {
            if (command instanceof PercentType) {
                dimmer.setBrightness((PercentType) command);
            } else if (command instanceof OnOffType) {
                dimmer.setBrightness((PercentType) ((OnOffType) command).as(PercentType.class));
            }
        }
    }

    @Override
    void initializeDevice(String ipAddress) {
        dimmer = new Dimmer(ipAddress);
    }

    @Override
    void updateDeviceStatus() {
        if (dimmer != null) {
            PercentType brightness = dimmer.getBrightness();

            if (brightness != null) {
                updateState(CHANNEL_BRIGHTNESS, brightness);

                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

}
