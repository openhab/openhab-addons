/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.handler;

import static org.openhab.binding.pentair.PentairBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.pentair.PentairBindingConstants;
import org.openhab.binding.pentair.internal.PentairPacket;
import org.openhab.binding.pentair.internal.PentairPacketPumpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairIntelliFloHandler} is responsible for implementation of the Intelliflo Pump. This will
 * parse/dispose of
 * status packets to set the stat for various channels.
 *
 * @author Jeff James - Initial contribution
 */
public class PentairIntelliFloHandler extends PentairBaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(PentairIntelliFloHandler.class);
    protected PentairPacketPumpStatus ppscur = new PentairPacketPumpStatus();

    public PentairIntelliFloHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Intelliflo - Thing ID: {}.", this.getThing().getUID());

        id = ((BigDecimal) getConfig().get("id")).intValue();

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Intellflo received refresh command");
            updateChannel(channelUID.getId(), null);
        }
    }

    @Override
    public void processPacketFrom(PentairPacket p) {
        switch (p.getAction()) {
            case 1: // Pump command - A5 00 10 60 01 02 00 20
                logger.trace("Pump command (ack): {}: ", p);
                break;
            case 4: // Pump control panel on/off
                logger.trace("Turn pump control panel (ack) {}: {} - {}", p.getSource(),
                        p.getByte(PentairPacket.STARTOFDATA), p);
                break;
            case 5: // Set pump mode
                logger.trace("Set pump mode (ack) {}: {} - {}", p.getSource(), p.getByte(PentairPacket.STARTOFDATA), p);
                break;
            case 6: // Set run mode
                logger.trace("Set run mode (ack) {}: {} - {}", p.getSource(), p.getByte(PentairPacket.STARTOFDATA), p);
                break;
            case 7: // Pump status (after a request)
                if (p.getLength() != 15) {
                    logger.debug("Expected length of 15: ", p);
                    return;
                }

                /*
                 * P: A500 d=10 s=60 c=07 l=0f 0A0602024A08AC120000000A000F22 <028A>
                 * RUN 0a Started
                 * MOD 06 Feature 1
                 * PMP 02 ? drive state
                 * PWR 024a 586 WATT
                 * RPM 08ac 2220 RPM
                 * GPM 12 18 GPM
                 * PPC 00 0 %
                 * b09 00 ?
                 * ERR 00 ok
                 * b11 0a ?
                 * TMR 00 0 MIN
                 * CLK 0f22 15:34
                 */

                logger.debug("Pump status: {}", p);

                /*
                 * Save the previous state of the packet (p29cur) into a temp variable (p29old)
                 * Update the current state to the new packet we just received.
                 * Then call updateChannel which will compare the previous state (now p29old) to the new state (p29cur)
                 * to determine if updateState needs to be called
                 */
                PentairPacketPumpStatus ppsOld = ppscur;
                ppscur = new PentairPacketPumpStatus(p);

                updateChannel(INTELLIFLO_RUN, ppsOld);
                updateChannel(INTELLIFLO_MODE, ppsOld);
                updateChannel(INTELLIFLO_DRIVESTATE, ppsOld);
                updateChannel(INTELLIFLO_POWER, ppsOld);
                updateChannel(INTELLIFLO_RPM, ppsOld);
                updateChannel(INTELLIFLO_PPC, ppsOld);
                updateChannel(INTELLIFLO_ERROR, ppsOld);
                updateChannel(INTELLIFLO_TIMER, ppsOld);

                break;
            default:
                logger.debug("Unhandled Intelliflo command: {}", p.toString());
                break;
        }
    }

    /**
     * Helper function to compare and update channel if needed. The class variables p29_cur and phsp_cur are used to
     * determine the appropriate state of the channel.
     *
     * @param channel name of channel to be updated, corresponds to channel name in {@link PentairBindingConstants}
     * @param p Packet representing the former state. If null, no compare is done and state is updated.
     */
    public void updateChannel(String channel, PentairPacket p) {
        // Only called from this class's processPacketFrom, so we are confident this will be a PentairPacketPumpStatus
        PentairPacketPumpStatus pps = (PentairPacketPumpStatus) p;

        switch (channel) {
            case INTELLIFLO_RUN:
                if (pps == null || (pps.run != ppscur.run)) {
                    updateState(channel, (ppscur.run) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case INTELLIFLO_MODE:
                if (pps == null || (pps.mode != ppscur.mode)) {
                    updateState(channel, new DecimalType(ppscur.mode));
                }
                break;
            case INTELLIFLO_DRIVESTATE:
                if (pps == null || (pps.drivestate != ppscur.drivestate)) {
                    updateState(channel, new DecimalType(ppscur.drivestate));
                }
                break;
            case INTELLIFLO_POWER:
                if (pps == null || (pps.power != ppscur.power)) {
                    updateState(channel, new DecimalType(ppscur.power));
                }
                break;
            case INTELLIFLO_RPM:
                if (pps == null || (pps.rpm != ppscur.rpm)) {
                    updateState(channel, new DecimalType(ppscur.rpm));
                }
                break;
            case INTELLIFLO_PPC:
                if (pps == null || (pps.ppc != ppscur.ppc)) {
                    updateState(channel, new DecimalType(ppscur.ppc));
                }
                break;
            case INTELLIFLO_ERROR:
                if (pps == null || (pps.error != ppscur.error)) {
                    updateState(channel, new DecimalType(ppscur.error));
                }
                break;
            case INTELLIFLO_TIMER:
                if (pps == null || (pps.timer != ppscur.timer)) {
                    updateState(channel, new DecimalType(ppscur.timer));
                }
                break;
        }
    }
}
