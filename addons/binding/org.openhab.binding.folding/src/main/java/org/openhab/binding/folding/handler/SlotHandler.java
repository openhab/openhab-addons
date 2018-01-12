/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folding.handler;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thing handler representing a Folding slot.
 *
 * If control of each slot (CPU, GPUs, etc.) is desired, the user can add
 * Slot handlers. The Slot handler exposes the status of a slot, and allows
 * users to start / stop folding.
 *
 * @author Marius Bjørnstad - Initial contribution
 */
public class SlotHandler extends BaseThingHandler implements SlotUpdateListener {

    private Logger logger = LoggerFactory.getLogger(SlotHandler.class);

    public SlotHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        getBridgeHandler().registerSlot(myId(), this);
    }

    private FoldingClientHandler getBridgeHandler() {
        return (FoldingClientHandler) super.getBridge().getHandler();
    }

    private String myId() {
        return (String) getThing().getConfiguration().get("id");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals("run")) {
                if (command == OnOffType.ON) {
                    getBridgeHandler().sendCommand("unpause " + myId());
                } else if (command == OnOffType.OFF) {
                    getBridgeHandler().sendCommand("pause " + myId());
                }
            } else if (channelUID.getId().equals("finish")) {
                if (command == OnOffType.ON) {
                    getBridgeHandler().sendCommand("finish " + myId());
                } else if (command == OnOffType.OFF) {
                    getBridgeHandler().sendCommand("unpause " + myId());
                }
            }
            getBridgeHandler().refresh();
            getBridgeHandler().delayedRefresh();
        } catch (IOException e) {
            logger.debug("Input/output error while handing command to Folding slot", e);
        }
    }

    @Override
    public void refreshed(SlotInfo si) {
        updateStatus(ThingStatus.ONLINE);
        updateState(getThing().getChannel("status").getUID(), new StringType(si.status));
        boolean finishing = "FINISHING".equals(si.status);
        boolean run = finishing || "READY".equals(si.status) || "RUNNING".equals(si.status);
        updateState(getThing().getChannel("finish").getUID(), finishing ? OnOffType.ON : OnOffType.OFF);
        updateState(getThing().getChannel("run").getUID(), run ? OnOffType.ON : OnOffType.OFF);
        updateState(getThing().getChannel("description").getUID(), new StringType(si.description));
    }

}
