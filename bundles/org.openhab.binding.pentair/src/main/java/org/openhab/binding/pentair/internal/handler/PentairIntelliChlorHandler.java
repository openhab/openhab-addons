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
package org.openhab.binding.pentair.internal.handler;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import org.openhab.binding.pentair.internal.PentairBindingConstants;
import org.openhab.binding.pentair.internal.PentairPacket;
import org.openhab.binding.pentair.internal.PentairPacketIntellichlor;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairIntelliChlorHandler} is responsible for implementation of the Intellichlor Salt generator. It will
 * process
 * Intellichlor commands and set the appropriate channel states. There are currently no commands implemented for this
 * Thing to receive from the framework.
 *
 * @author Jeff James - Initial contribution
 */
public class PentairIntelliChlorHandler extends PentairBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(PentairIntelliChlorHandler.class);

    protected PentairPacketIntellichlor pic3cur = new PentairPacketIntellichlor();
    protected PentairPacketIntellichlor pic4cur = new PentairPacketIntellichlor();

    public PentairIntelliChlorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing IntelliChlor - Thing ID: {}.", this.getThing().getUID());

        id = 0; // Intellichlor doesn't have ID

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("IntelliChlor received refresh command");
            updateChannel(channelUID.getId(), null);
        }
    }

    @Override
    public void processPacketFrom(PentairPacket p) {
        PentairPacketIntellichlor pic = (PentairPacketIntellichlor) p;

        switch (pic.getLength()) {
            case 3:
                if (pic.getCmd() != 0x11) { // only packets with 0x11 have valid saltoutput numbers.
                    break;
                }

                PentairPacketIntellichlor pic3Old = pic3cur;
                pic3cur = pic;

                updateChannel(INTELLICHLOR_SALTOUTPUT, pic3Old);

                break;
            case 4:
                if (pic.getCmd() != 0x12) {
                    break;
                }

                PentairPacketIntellichlor pic4Old = pic4cur;
                pic4cur = pic;

                updateChannel(INTELLICHLOR_SALINITY, pic4Old);

                break;
        }

        logger.debug("Intellichlor command: {}", pic);
    }

    /**
     * Helper function to compare and update channel if needed. The class variables p29_cur and phsp_cur are used to
     * determine the appropriate state of the channel.
     *
     * @param channel name of channel to be updated, corresponds to channel name in {@link PentairBindingConstants}
     * @param p Packet representing the former state. If null, no compare is done and state is updated.
     */
    public void updateChannel(String channel, PentairPacket p) {
        PentairPacketIntellichlor pic = (PentairPacketIntellichlor) p;

        switch (channel) {
            case INTELLICHLOR_SALINITY:
                if (pic == null || (pic.salinity != pic4cur.salinity)) {
                    updateState(channel, new DecimalType(pic4cur.salinity));
                }
                break;
            case INTELLICHLOR_SALTOUTPUT:
                if (pic == null || (pic.saltoutput != pic3cur.saltoutput)) {
                    updateState(channel, new DecimalType(pic3cur.saltoutput));
                }
                break;
        }
    }
}
