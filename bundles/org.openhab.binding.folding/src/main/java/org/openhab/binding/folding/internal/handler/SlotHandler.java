/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.folding.internal.handler;

import java.io.IOException;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
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
            if ("run".equals(channelUID.getId())) {
                if (command == OnOffType.ON) {
                    getBridgeHandler().sendCommand("unpause " + myId());
                } else if (command == OnOffType.OFF) {
                    getBridgeHandler().sendCommand("pause " + myId());
                }
            } else if ("finish".equals(channelUID.getId())) {
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
