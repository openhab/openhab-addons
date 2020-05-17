/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.pentair.internal.PentairPacket;
import org.openhab.binding.pentair.internal.PentairPacketPumpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairIntelliFloHandler} is responsible for implementation of the Intelliflo Pump. This will
 * parse of status packets to set the stat for various channels.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairIntelliFloHandler extends PentairBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PentairIntelliFloHandler.class);
    protected PentairPacketPumpStatus ppscur = new PentairPacketPumpStatus();

    private boolean waitStatusForOnline = false;
    // runmode is used to send watchdog to pump when running
    private boolean runmode = false;

    /** polling job for pump status */
    @Nullable
    static protected ScheduledFuture<?> pollingjob;

    public PentairIntelliFloHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Intelliflo - Thing ID: {}.", this.getThing().getUID());

        Configuration config = getConfig();

        id = ((BigDecimal) config.get("id")).intValue();

        goOnline();
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
        goOffline(ThingStatusDetail.NONE);
    }

    public void goOnline() {
        logger.debug("Thing {} goOnline.", getThing().getUID());

        this.waitStatusForOnline = false;

        // make sure bridge exists and is online
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            logger.debug("PentaiIntelliFloHandler: goOnline - bridge == null");
            return;
        }
        PentairBaseBridgeHandler bh = (PentairBaseBridgeHandler) bridge.getHandler();
        if (bh == null) {
            logger.debug("Bridge does not exist");
            return;
        }

        ThingStatus ts = bh.getThing().getStatus();
        if (!ts.equals(ThingStatus.ONLINE)) {
            logger.debug("Bridge is not online");
            return;
        }

        waitStatusForOnline = true;
        // requestPumpStatus(); // pump doesn't go online until it sees first response on the RS485
    }

    public void finishOnline() {
        if (pollingjob == null) {
            pollingjob = scheduler.scheduleWithFixedDelay(new PumpStatus(), 10, 30, TimeUnit.SECONDS);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void goOffline(ThingStatusDetail detail) {
        logger.debug("Thing {} goOffline.", getThing().getUID());

        if (pollingjob != null) {
            pollingjob.cancel(true);
        }
        pollingjob = null;

        updateStatus(ThingStatus.OFFLINE, detail);
    }

    /**
     * Job to send pump query status packages to all Intelliflo Pump things in order to see the status.
     * Note: From the internet is seems some FW versions of EasyTouch controllers send this automatically and this the
     * pump status packets can just be snooped, however my controller version does not do this. No harm in sending.
     *
     * @author Jeff James
     *
     */
    class PumpStatus implements Runnable {
        @Override
        public void run() {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return;
            }

            List<Thing> things = bridge.getThings();

            for (Thing t : things) {
                if (!t.getThingTypeUID().equals(INTELLIFLO_THING_TYPE)) {
                    continue;
                }

                PentairIntelliFloHandler handler = (PentairIntelliFloHandler) t.getHandler();
                if (handler == null) {
                    return;
                }

                logger.debug("pump runmode = {}", runmode);

                if (handler.runmode == true) {
                    logger.debug("Sending watchdog to pump");
                    handler.sendPumpOnOROff(true);
                } else {
                    handler.requestPumpStatus();
                }
            }
        }
    };

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.trace("PentairIntelliFloHandler: bridgeStatusChanged");

        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            goOffline(ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            goOnline();
        }
    }

    // checkOtherMaster - check to make sure the system does not have a controller OR that the controller is in
    // servicemode
    protected boolean checkOtherMaster() {
        PentairControllerHandler pch = PentairControllerHandler.onlineController;

        if (pch != null && pch.servicemode == false) {
            return true;
        }

        return false;
    }

    /* Commands to send to IntelliFlo */

    public void sendRequestPumpStatus() {
        logger.debug("sendRequestPumpStatus");
        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */, (byte) 0x07, (byte) 0x00 };

        if (!writePacket(packet, 0x07, 1)) {
            logger.debug("sendRequestStatus: Timeout");
        }
    }

    public void requestPumpStatus() {
        logger.debug("requestPumpStatus");

        sendLocalORRemoteControl(false);
        sendRequestPumpStatus();
    }

    public void sendLocalORRemoteControl(boolean bLocal) {
        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */, (byte) 0x04, (byte) 0x01,
                (bLocal) ? (byte) 0x00 : (byte) 0xFF };

        logger.debug("sendLocalORRemoteControl: {}", bLocal);

        if (!writePacket(packet, 0x04, 1)) {
            logger.debug("sendLocalOrRemoteControl: Timeout");
        }
    }

    public void sendPumpOnOROff(boolean bOn) {
        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */, (byte) 0x06, (byte) 0x01,
                (bOn) ? (byte) 0x0A : (byte) 0x04 };

        logger.debug("sendPumpOnOROff: {}", bOn);
        if (checkOtherMaster()) {
            logger.info("Unable to send command to pump as there is another master in the system");
            return;
        }

        if (!writePacket(packet, 0x06, 1)) {
            logger.debug("sendPumpOnOROff: Timeout");
        }
    }

    public void setPumpOnOROff(boolean bOn) {
        logger.debug("setPumpOnOROff: {}", bOn);

        if (!bOn) {
            helperClearPrograms(0);
        }

        runmode = bOn;

        sendLocalORRemoteControl(false);
        sendPumpOnOROff(bOn);
        sendRequestPumpStatus();
        sendLocalORRemoteControl(true);
    }

    // sendPumpRPM - low-level call to send to pump the RPM command
    public void sendPumpRPM(int rpm) {
        int rpmH, rpmL;

        logger.debug("sendPumpRPM: {}", rpm);
        if (checkOtherMaster()) {
            logger.info("Unable to send command to pump as there is another master in the system");
            return;
        }

        rpmH = rpm / 256;
        rpmL = rpm % 256;

        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */, (byte) 0x01, (byte) 0x04,
                (byte) 0x02, (byte) 0xC4, (byte) rpmH, (byte) rpmL };

        if (rpm < 400 || rpm > 3450) {
            throw new IllegalArgumentException("rpm not in range [400..3450]: " + rpm);
        }

        if (!writePacket(packet, 0x01, 1)) {
            logger.debug("sendPumpRPM: timeout");
        }
    }

    // setPumpRPM - high-level call that includes wrapper commands and delay functions
    public void setPumpRPM(int rpm) {
        logger.debug("setPumpRPM: {}", rpm);

        helperClearPrograms(0);

        runmode = true;

        sendLocalORRemoteControl(false);
        sendPumpRPM(rpm);
        sendPumpOnOROff(true);
        sendRequestPumpStatus();
        sendLocalORRemoteControl(true);
    }

    // sendRunProgram - low-level call to send the command to pump
    public void sendRunProgram(int program) {
        logger.debug("sendRunProgram: {}", program);

        if (checkOtherMaster()) {
            logger.info("Unable to send command to pump as there is another master in the system");
            return;
        }

        if (program < 1 || program > 4) {
            return;
        }

        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */, (byte) 0x01, (byte) 0x04,
                (byte) 0x03, (byte) 0x21, (byte) 0x00, (byte) (program << 3) };

        if (!writePacket(packet, 0x06, 1)) {
            logger.debug("sendRunProgram: Timeout");
        }
    }

    // setRunProgram - high-level call to run program - including wrapper calls
    public void setRunProgram(int program) {
        logger.debug("setRunProgram: {}", program);

        helperClearPrograms(program);

        runmode = true;

        sendLocalORRemoteControl(false);
        sendRunProgram(program);
        sendPumpOnOROff(true);
        sendRequestPumpStatus();
        sendLocalORRemoteControl(true);
    }

    // helperClearPrograms - turns off any other channels/items that were used to start the pump
    public void helperClearPrograms(int program) {
        logger.debug("helperClearProgram = {}", program);

        if (program != 1) {
            logger.debug("Turn off program1");
            updateState(INTELLIFLO_PROGRAM1, OnOffType.OFF);
        } else {
            updateState(INTELLIFLO_PROGRAM1, OnOffType.ON);
        }

        if (program != 2) {
            logger.debug("Turn off program2");
            updateState(INTELLIFLO_PROGRAM2, OnOffType.OFF);
        } else {
            updateState(INTELLIFLO_PROGRAM2, OnOffType.ON);
        }

        if (program != 3) {
            logger.debug("Turn off program3");
            updateState(INTELLIFLO_PROGRAM3, OnOffType.OFF);
        } else {
            updateState(INTELLIFLO_PROGRAM3, OnOffType.ON);
        }

        if (program != 4) {
            logger.debug("Turn off program4");
            updateState(INTELLIFLO_PROGRAM4, OnOffType.OFF);
        } else {
            updateState(INTELLIFLO_PROGRAM4, OnOffType.ON);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, {}, {}", channelUID, command);
        if (command instanceof OnOffType) {
            boolean state = ((OnOffType) command) == OnOffType.ON;

            switch (channelUID.getId()) {
                case INTELLIFLO_RUN:
                case INTELLIFLO_RPM:
                    setPumpOnOROff(state);
                    break;
                case INTELLIFLO_PROGRAM1:
                    if (state) {
                        setRunProgram(1);
                    } else {
                        setPumpOnOROff(false);
                    }
                    break;
                case INTELLIFLO_PROGRAM2:
                    if (state) {
                        setRunProgram(2);
                    } else {
                        setPumpOnOROff(false);
                    }
                    break;
                case INTELLIFLO_PROGRAM3:
                    if (state) {
                        setRunProgram(3);
                    } else {
                        setPumpOnOROff(false);
                    }
                    break;
                case INTELLIFLO_PROGRAM4:
                    if (state) {
                        setRunProgram(4);
                    } else {
                        setPumpOnOROff(false);
                    }
                    break;
            }
        } else if (command instanceof DecimalType) {
            int num = ((DecimalType) command).intValue();

            switch (channelUID.getId()) {
                case INTELLIFLO_RPM:
                    setPumpRPM(num);
                    break;
            }
        }
    }

    @Override
    public void processPacketFrom(PentairPacket p) {
        if (waitStatusForOnline) {
            finishOnline();
            waitStatusForOnline = false;
        }

        switch (p.getAction()) {
            case 1: // Pump command - A5 00 10 60 01 02 00 20
                logger.debug("Pump command (ack): {}: ", p.toString());
                break;
            case 4: // Pump control panel on/off
                boolean remotemode;

                remotemode = p.getByte(0) == (byte) 0xFF;
                logger.debug("Pump control panel (ack) {}: {} - {}", p.getSource(), remotemode, p);

                break;
            case 5: // Set pump mode ack
                logger.debug("Set pump mode (ack) {}: {} - {}", p.getSource(), p.getByte(0), p);
                break;
            case 6: // Set run mode ack
                logger.debug("Set run mode (ack) {}: {} - {}", p.getSource(), p.getByte(0), p);
                break;
            case 7: // Pump status (after a request)
                if (p.getLength() != 15) {
                    logger.debug("Expected length of 15: {}", p);
                    return;
                }

                PentairPacketPumpStatus pps = new PentairPacketPumpStatus(p);

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

                updateChannel(INTELLIFLO_RUN, pps.run);
                updateChannelPower(INTELLIFLO_POWER, pps.power);
                updateChannel(INTELLIFLO_RPM, pps.rpm);
                updateChannel(INTELLIFLO_GPM, pps.gpm);
                updateChannel(INTELLIFLO_ERROR, pps.error);
                updateChannel(INTELLIFLO_STATUS1, pps.status11);
                updateChannel(INTELLIFLO_STATUS2, pps.status12);

                break;
            default:
                logger.debug("Unhandled Intelliflo command: {}", p.toString());
                break;
        }
    }

    /**
     * Helper function to update channel.
     */
    public void updateChannel(String channel, boolean value) {
        updateState(channel, (value) ? OnOffType.ON : OnOffType.OFF);
    }

    public void updateChannel(String channel, int value) {
        updateState(channel, new DecimalType(value));
    }

    public void updateChannel(String channel, String value) {
        updateState(channel, new StringType(value));
    }

    public void updateChannelPower(String channel, int value) {
        updateState(channel, new QuantityType<>(value, SmartHomeUnits.WATT));
    }
}
