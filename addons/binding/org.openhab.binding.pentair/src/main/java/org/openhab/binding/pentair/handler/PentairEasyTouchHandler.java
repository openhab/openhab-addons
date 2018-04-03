/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.handler;

import static org.openhab.binding.pentair.PentairBindingConstants.*;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.pentair.PentairBindingConstants;
import org.openhab.binding.pentair.internal.PentairPacket;
import org.openhab.binding.pentair.internal.PentairPacketHeatSetPoint;
import org.openhab.binding.pentair.internal.PentairPacketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairEasyTouchHandler} is responsible for implementation of the EasyTouch Controller. It will handle
 * commands sent to a thing and implements the different channels. It also parses/disposes of the packets seen on the
 * bus from the controller.
 *
 * @author Jeff James - Initial contribution
 */
public class PentairEasyTouchHandler extends PentairBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PentairEasyTouchHandler.class);

    /**
     * current/last status packet recieved, used to compare new packet values to determine if status needs to be updated
     */
    protected PentairPacketStatus p29cur = new PentairPacketStatus();
    /** current/last heat set point packet, used to determine if status in framework should be updated */
    protected PentairPacketHeatSetPoint phspcur = new PentairPacketHeatSetPoint();

    public PentairEasyTouchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing EasyTouch - Thing ID: {}.", this.getThing().getUID());

        id = ((BigDecimal) getConfig().get("id")).intValue();

        // make sure there are no exisitng EasyTouch controllers
        PentairBaseBridgeHandler bh = (PentairBaseBridgeHandler) this.getBridge().getHandler();
        List<Thing> things = bh.getThing().getThings();

        for (Thing t : things) {
            if (t.getUID().equals(this.getThing().getUID())) {
                continue;
            }
            if (t.getThingTypeUID().equals(EASYTOUCH_THING_TYPE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Another EasyTouch controller is already configured.");
                return;
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // When channel gets a refresh request, sending a null as the PentairPacket to updateChannel will force an
        // updateState, regardless of previous packet value
        if (command instanceof RefreshType) {
            logger.debug("EasyTouch received refresh command");

            updateChannel(channelUID.getId(), null);

            return;
        }

        if (command instanceof OnOffType) {
            boolean state = ((OnOffType) command) == OnOffType.ON;

            switch (channelUID.getId()) {
                case EASYTOUCH_POOL:
                    circuitSwitch(6, state);
                    break;
                case EASYTOUCH_SPA:
                    circuitSwitch(1, state);
                    break;
                case EASYTOUCH_AUX1:
                    circuitSwitch(2, state);
                    break;
                case EASYTOUCH_AUX2:
                    circuitSwitch(3, state);
                    break;
                case EASYTOUCH_AUX3:
                    circuitSwitch(4, state);
                    break;
                case EASYTOUCH_AUX4:
                    circuitSwitch(5, state);
                    break;
                case EASYTOUCH_AUX5:
                    circuitSwitch(7, state);
                    break;
                case EASYTOUCH_AUX6:
                    circuitSwitch(8, state);
                    break;
                case EASYTOUCH_AUX7: // A5 01 10 20 86 02 09 01
                    circuitSwitch(9, state);
                    break;
            }
        } else if (command instanceof DecimalType) {
            int sp = ((DecimalType) command).intValue();

            switch (channelUID.getId()) {
                case EASYTOUCH_SPASETPOINT:
                    setPoint(false, sp);
                    break;
                case EASYTOUCH_POOLSETPOINT:
                    setPoint(true, sp);
                    break;
            }
        }
    }

    /**
     * Method to turn on/off a circuit in response to a command from the framework
     *
     * @param circuit circuit number
     * @param state
     */
    public void circuitSwitch(int circuit, boolean state) {
        byte[] packet = { (byte) 0xA5, (byte) 0x01, (byte) id, (byte) 0x00 /* source */, (byte) 0x86, (byte) 0x02,
                (byte) circuit, (byte) ((state) ? 1 : 0) };

        PentairPacket p = new PentairPacket(packet);

        PentairBaseBridgeHandler bbh = (PentairBaseBridgeHandler) this.getBridge().getHandler();
        bbh.writePacket(p);
    }

    /**
     * Method to set heat point for pool (true) of spa (false)
     *
     * @param Pool pool=true, spa=false
     * @param temp
     */
    public void setPoint(boolean pool, int temp) {
        // [16,34,136,4,POOL HEAT Temp,SPA HEAT Temp,Heat Mode,0,2,56]
        // [165, preambleByte, 16, 34, 136, 4, currentHeat.poolSetPoint, parseInt(req.params.temp), updateHeatMode, 0]
        int spaset = (!pool) ? temp : phspcur.spasetpoint;
        int poolset = (pool) ? temp : phspcur.poolsetpoint;
        int heatmode = (phspcur.spaheatmode << 2) | phspcur.poolheatmode;

        byte[] packet = { (byte) 0xA5, (byte) 0x01, (byte) id, (byte) 0x00 /* source */, (byte) 0x88, (byte) 0x04,
                (byte) poolset, (byte) spaset, (byte) heatmode, (byte) 0 };

        logger.info("Set {} temperature: {}", (pool) ? "Pool" : "Spa", temp);

        PentairPacket p = new PentairPacket(packet);

        PentairBaseBridgeHandler bbh = (PentairBaseBridgeHandler) this.getBridge().getHandler();
        bbh.writePacket(p);
    }

    @Override
    public void processPacketFrom(PentairPacket p) {
        switch (p.getAction()) {
            case 1: // Write command to pump
                logger.trace("Write command to pump (unimplemented): {}", p);
                break;
            case 2:
                if (p.getLength() != 29) {
                    logger.debug("Expected length of 29: {}", p);
                    return;
                }

                /*
                 * Save the previous state of the packet (p29cur) into a temp variable (p29old)
                 * Update the current state to the new packet we just received.
                 * Then call updateChannel which will compare the previous state (now p29old) to the new state (p29cur)
                 * to determine if updateState needs to be called
                 */
                PentairPacketStatus p29Old = p29cur;
                p29cur = new PentairPacketStatus(p);

                updateChannel(EASYTOUCH_POOL, p29Old);
                updateChannel(EASYTOUCH_POOLTEMP, p29Old);
                updateChannel(EASYTOUCH_SPATEMP, p29Old);
                updateChannel(EASYTOUCH_AIRTEMP, p29Old);
                updateChannel(EASYTOUCH_SOLARTEMP, p29Old);
                updateChannel(EASYTOUCH_HEATACTIVE, p29Old);
                updateChannel(EASYTOUCH_POOL, p29Old);
                updateChannel(EASYTOUCH_SPA, p29Old);
                updateChannel(EASYTOUCH_AUX1, p29Old);
                updateChannel(EASYTOUCH_AUX2, p29Old);
                updateChannel(EASYTOUCH_AUX3, p29Old);
                updateChannel(EASYTOUCH_AUX4, p29Old);
                updateChannel(EASYTOUCH_AUX5, p29Old);
                updateChannel(EASYTOUCH_AUX6, p29Old);
                updateChannel(EASYTOUCH_AUX7, p29Old);
                updateChannel(DIAG, p29Old);

                break;
            case 4: // Pump control panel on/off
                // No action - have not verified these commands, here for documentation purposes and future enhancement
                logger.trace("Pump control panel on/of {}: {}", p.getDest(), p.getByte(PentairPacket.STARTOFDATA));

                break;
            case 5: // Set pump mode
                // No action - have not verified these commands, here for documentation purposes and future enhancement
                logger.trace("Set pump mode {}: {}", p.getDest(), p.getByte(PentairPacket.STARTOFDATA));

                break;
            case 6: // Set run mode
                // No action - have not verified these commands, here for documentation purposes and future enhancement
                logger.trace("Set run mode {}: {}", p.getDest(), p.getByte(PentairPacket.STARTOFDATA));

                break;
            case 7:
                // No action - have not verified these commands, here for documentation purposes and future enhancement
                logger.trace("Pump request status (unseen): {}", p);
                break;
            case 8: // A5 01 0F 10 08 0D 4B 4B 4D 55 5E 07 00 00 58 00 00 00 
                if (p.getLength() != 0x0D) {
                    logger.debug("Expected length of 13: {}", p);
                    return;
                }

                /*
                 * Save the previous state of the packet (phspcur) into a temp variable (phspOld)
                 * Update the current state to the new packet we just received.
                 * Then call updateChannel which will compare the previous state (now phspold) to the new state
                 * (phspcur) to determine if updateState needs to be called
                 */
                PentairPacketHeatSetPoint phspOld = phspcur;
                phspcur = new PentairPacketHeatSetPoint(p);

                updateChannel(EASYTOUCH_POOLSETPOINT, phspOld);
                updateChannel(EASYTOUCH_SPASETPOINT, phspOld);
                updateChannel(EASYTOUCH_SPAHEATMODE, phspOld);
                updateChannel(EASYTOUCH_SPAHEATMODESTR, phspOld);
                updateChannel(EASYTOUCH_POOLHEATMODE, phspOld);
                updateChannel(EASYTOUCH_POOLHEATMODESTR, phspOld);

                logger.debug("Heat set point: {}, {}, {}", p, phspcur.poolsetpoint, phspcur.spasetpoint);
                break;
            case 10:
                logger.debug("Get Custom Names (unseen): {}", p);
                break;
            case 11:
                logger.debug("Get Ciruit Names (unseen): {}", p);
                break;
            case 17:
                logger.debug("Get Schedules (unseen): {}", p);
                break;
            case 134:
                logger.debug("Set Circuit Function On/Off (unseen): {}", p);
                break;
            default:
                logger.debug("Not Implemented: {}", p);
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
        PentairPacketStatus p29 = null;
        PentairPacketHeatSetPoint phsp = null;

        if (p != null) {
            if (p.getLength() == 29) {
                p29 = (PentairPacketStatus) p;
            } else if (p.getLength() == 13) {
                phsp = (PentairPacketHeatSetPoint) p;
            }
        }

        switch (channel) {
            case EASYTOUCH_POOL:
                if (p29 == null || (p29.pool != p29cur.pool)) {
                    updateState(channel, (p29cur.pool) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case EASYTOUCH_SPA:
                if (p29 == null || (p29.spa != p29cur.spa)) {
                    updateState(channel, (p29cur.spa) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case EASYTOUCH_AUX1:
                if (p29 == null || (p29.aux1 != p29cur.aux1)) {
                    updateState(channel, (p29cur.aux1) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case EASYTOUCH_AUX2:
                if (p29 == null || (p29.aux2 != p29cur.aux2)) {
                    updateState(channel, (p29cur.aux2) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case EASYTOUCH_AUX3:
                if (p29 == null || (p29.aux3 != p29cur.aux3)) {
                    updateState(channel, (p29cur.aux3) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case EASYTOUCH_AUX4:
                if (p29 == null || (p29.aux4 != p29cur.aux4)) {
                    updateState(channel, (p29cur.aux4) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case EASYTOUCH_AUX5:
                if (p29 == null || (p29.aux5 != p29cur.aux5)) {
                    updateState(channel, (p29cur.aux5) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case EASYTOUCH_AUX6:
                if (p29 == null || (p29.aux6 != p29cur.aux6)) {
                    updateState(channel, (p29cur.aux6) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case EASYTOUCH_AUX7:
                if (p29 == null || (p29.aux7 != p29cur.aux7)) {
                    updateState(channel, (p29cur.aux7) ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case EASYTOUCH_POOLTEMP:
                if (p29 == null || (p29.pooltemp != p29cur.pooltemp)) {
                    if (p29cur.pool) {
                        updateState(channel, new DecimalType(p29cur.pooltemp));
                    } else {
                        updateState(channel, UnDefType.UNDEF);
                    }
                }
                break;
            case EASYTOUCH_SPATEMP:
                if (p29 == null || (p29.spatemp != p29cur.spatemp)) {
                    if (p29cur.spa) {
                        updateState(channel, new DecimalType(p29cur.spatemp));
                    } else {
                        updateState(channel, UnDefType.UNDEF);
                    }
                }
                break;
            case EASYTOUCH_AIRTEMP:
                if (p29 == null || (p29.airtemp != p29cur.airtemp)) {
                    updateState(channel, new DecimalType(p29cur.airtemp));
                }
                break;
            case EASYTOUCH_SOLARTEMP:
                if (p29 == null || (p29.solartemp != p29cur.solartemp)) {
                    updateState(channel, new DecimalType(p29cur.solartemp));
                }
                break;
            case EASYTOUCH_SPAHEATMODE:
                if (phsp == null || (phsp.spaheatmode != phspcur.spaheatmode)) {
                    updateState(channel, new DecimalType(phspcur.spaheatmode));
                }
                break;
            case EASYTOUCH_SPAHEATMODESTR:
                if (phsp == null || (phsp.spaheatmodestr != phspcur.spaheatmodestr)) {
                    if (phspcur.spaheatmodestr != null) {
                        updateState(channel, new StringType(phspcur.spaheatmodestr));
                    }
                }
                break;
            case EASYTOUCH_POOLHEATMODE:
                if (phsp == null || (phsp.poolheatmode != phspcur.poolheatmode)) {
                    updateState(channel, new DecimalType(phspcur.poolheatmode));
                }
                break;
            case EASYTOUCH_POOLHEATMODESTR:
                if (phsp == null || (phsp.poolheatmodestr != phspcur.poolheatmodestr)) {
                    if (phspcur.poolheatmodestr != null) {
                        updateState(channel, new StringType(phspcur.poolheatmodestr));
                    }
                }
                break;
            case EASYTOUCH_HEATACTIVE:
                if (p29 == null || (p29.heatactive != p29cur.heatactive)) {
                    updateState(channel, new DecimalType(p29cur.heatactive));
                }
                break;
            case EASYTOUCH_POOLSETPOINT:
                if (phsp == null || (phsp.poolsetpoint != phspcur.poolsetpoint)) {
                    updateState(channel, new DecimalType(phspcur.poolsetpoint));
                }
                break;
            case EASYTOUCH_SPASETPOINT:
                if (phsp == null || (phsp.spasetpoint != phspcur.spasetpoint)) {
                    updateState(channel, new DecimalType(phspcur.spasetpoint));
                }
                break;
            case DIAG:
                if (p29 == null || (p29.diag != p29cur.diag)) {
                    updateState(channel, new DecimalType(p29cur.diag));
                }
                break;
        }
    }
}
