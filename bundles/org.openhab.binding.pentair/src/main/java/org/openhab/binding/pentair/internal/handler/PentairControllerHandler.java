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
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.pentair.internal.PentairControllerCircuit;
import org.openhab.binding.pentair.internal.PentairControllerConstants;
import org.openhab.binding.pentair.internal.PentairControllerSchedule;
import org.openhab.binding.pentair.internal.PentairPacket;
import org.openhab.binding.pentair.internal.PentairPacketHeatSetPoint;
import org.openhab.binding.pentair.internal.PentairPacketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.Units;

/**
 * The {@link PentairControllerHandler} is responsible for implementation of the EasyTouch Controller. It will handle
 * commands sent to a thing and implements the different channels. It also parses of the packets seen on the
 * bus from the controller.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairControllerHandler extends PentairBaseThingHandler {

    protected static final int NUMCIRCUITS = 18;
    protected static final int NUMSCHEDULES = 9;

    // only one controller can be online at a time, used to validate only one is online & to access status
    @Nullable
    public static PentairControllerHandler onlineController;
    public boolean servicemode = false;

    private final Logger logger = LoggerFactory.getLogger(PentairControllerHandler.class);
    @Nullable
    protected ScheduledFuture<?> syncTimeJob;
    @Nullable
    protected ScheduledFuture<?> updateMinsRunJob;
    private int preambleByte = -1; // Byte to use after 0xA5 in communicating to controller. Not sure why this changes,
                                   // but it requires to be in sync and up-to-date
    private boolean waitStatusForOnline = false; // To manage online status, only go online when we received first
                                                 // status command
    private long lastScheduleTypeWrite;

    /**
     * current/last status packet recieved, used to compare new packet values to determine if status needs to be updated
     */
    @Nullable
    protected PentairPacketStatus p29cur = new PentairPacketStatus();
    @Nullable
    protected PentairPacketStatus p29old;
    /** current/last heat set point packet, used to determine if status in framework should be updated */
    protected PentairPacketHeatSetPoint phspcur = new PentairPacketHeatSetPoint();

    private int majorrev, minorrev;

    protected PentairControllerCircuit[] circuits = new PentairControllerCircuit[NUMCIRCUITS];
    protected PentairControllerSchedule[] schedules = new PentairControllerSchedule[NUMSCHEDULES];

    protected int lightmode;

    public PentairControllerHandler(Thing thing) {
        super(thing);

        for (int i = 0; i < NUMSCHEDULES; i++) {
            schedules[i] = new PentairControllerSchedule();
        }

        for (int i = 0; i < NUMCIRCUITS; i++) {
            circuits[i] = new PentairControllerCircuit(i + 1);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Controller - Thing ID: {}.", this.getThing().getUID());

        goOnline();
    }

    @Override
    public void dispose() {

        logger.debug("Thing {} disposed.", getThing().getUID());
        try {
            throw (new Exception("dispose"));
        } catch (Exception e) {
            logger.debug("dispose {}", e.getStackTrace());
        }

        goOffline(ThingStatusDetail.NONE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("PentairControllerHandler: bridgeStatusChanged: {}", bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            goOffline(ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            goOnline();
        }
    }

    public void goOnline() {
        logger.debug("Thing {} goOnline.", getThing().getUID());

        this.waitStatusForOnline = false;

        if (onlineController != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Another Controller controller is already configured.");
        }

        id = ((BigDecimal) getConfig().get("id")).intValue();

        // make sure bridge exists and is online
        Bridge bridge = this.getBridge();
        if (bridge == null) {
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

        waitStatusForOnline = true; // Wait for first status response to go online
    }

    public void finishOnline() {
        onlineController = this;

        // setup timer to sync time
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                boolean synctime = ((boolean) getConfig().get("synctime"));
                if (synctime) {
                    logger.info("Synchronizing System Time");
                    Calendar now = Calendar.getInstance();
                    setClockSettings(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                            now.get(Calendar.DAY_OF_WEEK), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1,
                            now.get(Calendar.YEAR) - 2000);
                }
            }
        };

        // setup syncTimeJob to run once a day, initial time to sync is 3 minutes after controller goes online. This is
        // to prevent collision with main thread queries on initial startup
        syncTimeJob = scheduler.scheduleWithFixedDelay(runnable, 3, 24 * 60 * 60, TimeUnit.MINUTES);

        Runnable countMinsRun = new Runnable() {
            @Override
            public void run() {
                logger.debug("Incrementing minutes run");
                for (int i = 0; i < NUMCIRCUITS; i++) {
                    if (circuits[i].on) {
                        circuits[i].minsrun++;
                        updateState(circuits[i].getGroup() + "#" + CONTROLLER_CIRCUITMINSRUN,
                                new QuantityType<>(circuits[i].minsrun, Units.MINUTE));
                    }
                }
            }
        };

        updateMinsRunJob = scheduler.scheduleAtFixedRate(countMinsRun, 1, 1, TimeUnit.MINUTES);

        Runnable queryinfo = new Runnable() {
            @Override
            public void run() {
                int i;

                getSWVersion();

                getHeat();

                getClockSettings();

                for (i = 1; i <= NUMCIRCUITS; i++) {
                    getCircuitNameFunction(i);
                }

                for (i = 1; i <= NUMSCHEDULES; i++) {
                    getSchedule(i);
                }

                getLightGroups();

                getValves();

                getSWVersion();
            }
        };
        Thread thread = new Thread(queryinfo);
        thread.start();

        updateStatus(ThingStatus.ONLINE);
    }

    public void goOffline(ThingStatusDetail detail) {
        logger.debug("Thing {} goOffline.", getThing().getUID());

        if (syncTimeJob != null) {
            syncTimeJob.cancel(true);
        }

        if (updateMinsRunJob != null) {
            updateMinsRunJob.cancel(true);
        }

        onlineController = null;
        updateStatus(ThingStatus.OFFLINE, detail);
    }

    public int getCircuitNumber(String name) {
        return PentairControllerCircuit.GROUPNAME_INV.get(name);
    }

    public int getScheduleNumber(String name) {
        int i;

        for (i = 1; i <= NUMSCHEDULES; i++) {
            String str = String.format(CONTROLLER_SCHEDULE, i);
            if (str.equals(name)) {
                return i;
            }
        }

        return 0;
    }

    public @Nullable PentairControllerSchedule getPPCS(String groupid) {
        int schedule = getScheduleNumber(groupid);
        if (schedule == 0) {
            return null;
        }

        PentairControllerSchedule ppcs = schedules[schedule - 1];

        return ppcs;
    }

    public @Nullable PentairControllerCircuit getPCC(String groupid) {
        int circuit = getCircuitNumber(groupid);
        if (circuit == 0) {
            return null;
        }

        PentairControllerCircuit pcc = circuits[circuit - 1];

        return pcc;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String groupId = channelUID.getGroupId();

        if (groupId == null) {
            return;
        }

        if (command instanceof RefreshType) {
            logger.debug("handleCommand (refresh): {}", channelUID.getId());

            switch (channelUID.getIdWithoutGroup()) {
                case CONTROLLER_LIGHTMODE:
                    updateState(channelUID, new StringType(PentairControllerConstants.LIGHTMODES.get(lightmode)));
                    break;

                case CONTROLLER_SETPOINT:
                    if (groupId == CONTROLLER_POOLHEAT) {
                        updateChannelTemp(groupId, channelUID.getIdWithoutGroup(), phspcur.poolsetpoint);
                    } else { // groupId == CONTROLLER_SPAHEAT
                        updateChannelTemp(groupId, channelUID.getIdWithoutGroup(), phspcur.spasetpoint);
                    }

                    break;
                case CONTROLLER_SCHEDULESTRING: {
                    PentairControllerSchedule pcs = getPPCS(groupId);

                    if (pcs != null) {
                        updateState(channelUID, new StringType(pcs.toString()));
                    }
                    break;
                }
                case CONTROLLER_SCHEDULETYPE: {
                    PentairControllerSchedule ppcs = getPPCS(groupId);

                    if (ppcs != null) {
                        updateState(channelUID, new StringType(ppcs.getScheduleTypeStr()));
                    }
                    break;
                }
                case CONTROLLER_SCHEDULESTART: {
                    PentairControllerSchedule ppcs = getPPCS(groupId);

                    if (ppcs != null) {
                        updateState(channelUID, new DecimalType(ppcs.start));
                    }
                    break;
                }
                case CONTROLLER_SCHEDULEEND: {
                    PentairControllerSchedule ppcs = getPPCS(groupId);

                    if (ppcs != null) {
                        updateState(channelUID, new DecimalType(ppcs.end));
                    }
                    break;
                }
                case CONTROLLER_SCHEDULECIRCUIT: {
                    PentairControllerSchedule ppcs = getPPCS(groupId);

                    if (ppcs != null) {
                        updateState(channelUID, new DecimalType(ppcs.circuit));
                    }
                    break;
                }
                case CONTROLLER_SCHEDULEDAYS: {
                    PentairControllerSchedule ppcs = getPPCS(groupId);

                    if (ppcs != null) {
                        updateState(channelUID, new StringType(ppcs.getDays()));
                    }
                    break;
                }
                case CONTROLLER_CIRCUITNAME: {
                    PentairControllerCircuit pcc = getPCC(groupId);
                    if (pcc != null) {
                        updateState(channelUID, new StringType(pcc.getNameStr()));
                    }
                    break;
                }
                case CONTROLLER_CIRCUITFUNCTION: {
                    PentairControllerCircuit pcc = getPCC(groupId);
                    if (pcc != null) {
                        updateState(channelUID, new StringType(pcc.getFunctionStr()));
                    }
                    break;
                }
                case CONTROLLER_CIRCUITMINSRUN: {
                    PentairControllerCircuit pcc = getPCC(groupId);
                    if (pcc != null) {
                        updateState(channelUID, new QuantityType<>(pcc.getMinsRun(), Units.MINUTE));
                    }
                    break;
                }
            }

            return;
        }

        logger.debug("handleCommand: {}", channelUID.getId());

        switch (channelUID.getIdWithoutGroup()) {
            case CONTROLLER_CIRCUITSWITCH: {
                int circuit = getCircuitNumber(groupId);

                boolean state = ((OnOffType) command) == OnOffType.ON;
                circuitSwitch(circuit, state);

                break;
            }
            case CONTROLLER_CIRCUITMINSRUN: {
                int circuit = getCircuitNumber(groupId);

                BigDecimal mins = ((QuantityType<?>) command).toBigDecimal();
                circuits[circuit - 1].minsrun = mins.intValue();

                break;
            }
            case CONTROLLER_LIGHTMODE: {

                String str = ((StringType) command).toString();

                int mode = PentairControllerConstants.LIGHTMODES_INV.get(str);
                lightmode = mode;
                setLightMode(mode);

                // not sure why this doesn't autoupdate
                updateState(channelUID, (State) command);

                break;
            }
            case CONTROLLER_SCHEDULESTRING: {
                PentairControllerSchedule ppcs = getPPCS(groupId);

                if (ppcs == null) {
                    break;
                }

                String str = ((StringType) command).toString();

                if (!ppcs.fromString(str)) {
                    logger.debug("schedule invalid format: {}", str);
                }

                break;
            }
            case CONTROLLER_SCHEDULETYPE: {
                PentairControllerSchedule ppcs = getPPCS(groupId);

                if (ppcs == null) {
                    break;
                }

                String str = ((StringType) command).toString();
                // save schedule only if a double update to the same value occurs within 5s of the last update
                boolean bUpdate = (str.equals(ppcs.getScheduleTypeStr())
                        && ((System.currentTimeMillis() - lastScheduleTypeWrite) < 5000) && ppcs.isDirty());
                if (!ppcs.setScheduleType(str)) {
                    return;
                }
                lastScheduleTypeWrite = System.currentTimeMillis();

                if (bUpdate) {
                    saveSchedule(ppcs);
                    lastScheduleTypeWrite = 0;

                    updateScheduleChannels(groupId, ppcs);
                }

                break;
            }
            case CONTROLLER_SCHEDULESTART: {
                PentairControllerSchedule ppcs = getPPCS(groupId);

                if (ppcs == null) {
                    break;
                }

                int start = ((Number) command).intValue();

                ppcs.setScheduleStart(start);

                break;
            }
            case CONTROLLER_SCHEDULEEND: {
                PentairControllerSchedule ppcs = getPPCS(groupId);

                if (ppcs == null) {
                    break;
                }

                int end = ((Number) command).intValue();

                ppcs.setScheduleEnd(end);

                break;
            }
            case CONTROLLER_SCHEDULECIRCUIT: {
                PentairControllerSchedule ppcs = getPPCS(groupId);

                if (ppcs == null) {
                    break;
                }

                int circuit = ((Number) command).intValue();

                ppcs.setScheduleCircuit(circuit);

                break;
            }
            case CONTROLLER_SCHEDULEDAYS: {
                PentairControllerSchedule ppcs = getPPCS(groupId);

                if (ppcs == null) {
                    break;
                }

                String days = ((StringType) command).toString();

                ppcs.setDays(days);

                break;
            }

            case CONTROLLER_SETPOINT: {
                if (!(command instanceof QuantityType<?>)) {
                    break;
                }

                int sp = ((QuantityType<?>) command).toBigDecimal().intValue();

                if (sp == 0) {
                    return;
                }

                switch (groupId) {
                    case CONTROLLER_SPAHEAT:
                        setPoint(false, sp);
                        break;
                    case CONTROLLER_POOLHEAT:
                        setPoint(true, sp);
                        break;
                }

                break;
            }
        }

    }

    /* Commands to send to Controller */

    /**
     * Method to turn on/off a circuit in response to a command from the framework
     *
     * @param circuit circuit number
     * @param state
     */
    public boolean circuitSwitch(int circuit, boolean state) {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0x86,
                (byte) 0x02, (byte) circuit, (byte) ((state) ? 1 : 0) };

        logger.info("circuit Switch: {}, {}", circuit, state);

        if (!writePacket(packet, 0x01, 1)) {
            logger.debug("circuitSwitch: Timeout");

            return false;
        }

        return true;
    }

    /**
     * Method to request clock
     */
    public boolean getClockSettings() { // A5 01 10 20 C5 01 00
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0xC5,
                (byte) 0x01, (byte) 0x00 };

        logger.info("Request clock settings");
        if (!writePacket(packet, 0x05, 1)) {
            logger.debug("getClockSetting: Timeout");

            return false;
        }

        return true;
    }

    public void getControllerStatus() { // A5 01 10 20 02 01 00
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0x02,
                (byte) 0x01, (byte) 0x00 };

        logger.info("Request controller status");

        if (!writePacket(packet, 0x02, 1)) {
            logger.debug("getControllerStatus: Timeout");
        }
    }

    public void getLightGroups() {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0xE7,
                (byte) 0x01, (byte) 0x00 };

        logger.info("Get Light Groups");

        if (!writePacket(packet, 0x27, 1)) {
            logger.debug("getLightGroups: Timeout");
        }
    }

    public void setLightMode(int mode) {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0x60,
                (byte) 0x02, (byte) mode, (byte) 0x00 };

        logger.info("setLightMode: {}", mode);

        if (!writePacket(packet, 0x01, 1)) {
            logger.debug("setLightMode: Timeout");
        }
    }

    public void getValves() {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0xDD,
                (byte) 0x01, (byte) 0x00 };

        logger.info("getValves");

        if (!writePacket(packet, 29, 1)) {
            logger.debug("getValves: Timeout");
        }
    }

    public boolean getCircuitNameFunction(int circuit) {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0xCB,
                (byte) 0x01, (byte) circuit };

        logger.info("getCircuitNameFunction: {}", circuit);

        if (!writePacket(packet, 0x0B, 1)) {
            logger.debug("getCircuitNameFunction: Timeout");

            return false;
        }
        return true;
    }

    public boolean getSchedule(int num) {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0xD1,
                (byte) 0x01, (byte) num };

        logger.info("getSchedule: {}", num);

        if (!writePacket(packet, 0x11, 1)) {
            logger.debug("getSchedule: Timeout");

            return false;
        }

        return true;
    }

    /**
     * Method to update the schedule to the controller
     *
     * @param p
     */
    public boolean saveSchedule(PentairControllerSchedule schedule) {
        PentairPacket p;

        p = schedule.getWritePacket(id, preambleByte);

        logger.debug("saveSchedule: {}", p.toString());
        schedule.setDirty(false);

        if (!writePacket(p, 0x01, 1)) {
            logger.debug("saveSchedule: Timeout");

            return false;
        }

        return true;
    }

    public boolean getSWVersion() {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0xFD,
                (byte) 0x01, (byte) 0x00 };

        logger.info("getSWVersion");

        if (!writePacket(packet, 0xFC, 1)) {
            return false;
        }

        /*
         * String version = String.format("%d.%d", majorrev, minorrev);
         *
         * this causes thingUpdated to be updated - default implementation is to dispose and re-initialize which is
         * extreme! I don't want to override in case I break some expected behavior in the framework. So removing this
         * property
         * update since it is for informational purposes only.
         * Map<String, String> editProperties = editProperties();
         * editProperties.put(CONTROLLER_PROPERTYFWVERSION, version);
         * updateProperties(editProperties);
         */

        return true;
    }

    /**
     * Method to set clock
     *
     */
    public void setClockSettings(int hour, int min, int dow, int day, int month, int year) { // A5 01 10 20 85 08 0D 2A
                                                                                             // 02 1D 04 11 00 00

        logger.info("Set Clock Settings {}:{} {} {}/{}/{}", hour, min, dow, day, month, year);

        if (hour > 23) {
            throw new IllegalArgumentException("hour not in range [0..23]: " + hour);
        }
        if (min > 59) {
            throw new IllegalArgumentException("hour not in range [0..59]: " + min);
        }
        if (dow > 7 || dow < 1) {
            throw new IllegalArgumentException("hour not in range [1..7]: " + dow);
        }
        if (day > 31 || day < 1) {
            throw new IllegalArgumentException("hour not in range [1..31]: " + day);
        }
        if (month > 12 || month < 1) {
            throw new IllegalArgumentException("hour not in range [1..12]: " + month);
        }
        if (year > 99) {
            throw new IllegalArgumentException("hour not in range [0..99]: " + year);
        }

        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0x85,
                (byte) 0x08, (byte) hour, (byte) min, (byte) dow, (byte) day, (byte) month, (byte) year, (byte) 0x00,
                (byte) 0x00 };

        writePacket(packet);
    }

    public void getHeat() { // A5 01 10 20 C8 01 00
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0xC8,
                (byte) 0x01, (byte) 0 };

        logger.info("Get heat settings");

        if (!writePacket(packet, 0x08, 1)) {
            logger.debug("getHeat: Timeout");
        }
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

        if (temp < 50 || temp > 105) {
            return;
        }

        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0x88,
                (byte) 0x04, (byte) poolset, (byte) spaset, (byte) heatmode, (byte) 0 };

        logger.info("Set {} temperature: {}", (pool) ? "Pool" : "Spa", temp);

        writePacket(packet);
    }

    @SuppressWarnings("null")
    @Override
    public void processPacketFrom(PentairPacket p) {

        switch (p.getAction()) {
            case 1: // Ack
                logger.debug("Ack command from device: {} - {}", p.getByte(0), p);
                break;
            case 2: // Controller Status
                if (p.getLength() != 29) {
                    logger.debug("Expected length of 29: {}", p);
                    return;
                }

                logger.trace("Controller Status: {}", p);

                preambleByte = p.getPreambleByte(); // Adjust what byte is used for preamble
                if (waitStatusForOnline) {
                    waitStatusForOnline = false;
                    finishOnline();
                }

                p29cur = new PentairPacketStatus(p);

                // only update packet of value has changed
                if (p29cur.equals(p29old)) {
                    return;
                }
                p29old = p29cur;

                for (int i = 0; i < NUMCIRCUITS; i++) {
                    PentairControllerCircuit circuit = circuits[i];

                    circuit.setOnOROff(p29cur.circuits[i]);

                    updateChannel(circuits[i].getGroup(), CONTROLLER_CIRCUITSWITCH, circuits[i].on);
                }

                updateChannelTemp(CONTROLLER_POOLHEAT, CONTROLLER_TEMPERATURE, (p29cur.pool) ? p29cur.pooltemp : 999);
                updateChannelTemp(CONTROLLER_SPAHEAT, CONTROLLER_TEMPERATURE, (p29cur.spa) ? p29cur.spatemp : 999);

                updateChannelTemp(CONTROLLER_STATUS, CONTROLLER_AIRTEMPERATURE, p29cur.airtemp);
                updateChannelTemp(CONTROLLER_STATUS, CONTROLLER_SOLARTEMPERATURE, p29cur.solartemp);
                updateChannel(CONTROLLER_STATUS, CONTROLLER_UOM, (p29cur.uom) ? "CELCIUS" : "FAHRENHEIT");
                updateChannel(CONTROLLER_STATUS, CONTROLLER_SERVICEMODE, p29cur.servicemode);
                servicemode = p29cur.servicemode;

                updateChannel(CONTROLLER_STATUS, CONTROLLER_SOLARON, p29cur.solaron);
                updateChannel(CONTROLLER_STATUS, CONTROLLER_HEATERON, p29cur.heateron);

                break;
            case 4: // Pump control panel on/off - handled in intelliflo controller
                // Controller sends packet often to keep control of the motor
                logger.debug("Pump control panel on/of {}: {}", p.getDest(), p.getByte(0));

                break;
            case 5: // Current Clock - A5 01 0F 10 05 08 0E 09 02 1D 04 11 00 00 - H M DOW D M YY YY ??
                int hour = p.getByte(0);
                int minute = p.getByte(1);
                int dow = p.getByte(2);
                int day = p.getByte(3);
                int month = p.getByte(4);
                int year = p.getByte(5);

                logger.debug("System Clock: {}:{} {} {}/{}/{}", hour, minute, dow, day, month, year);

                break;
            case 6: // Set run mode
                // No action - have not verified these commands, here for documentation purposes and future enhancement
                logger.debug("Set run mode {}: {}", p.getDest(), p.getByte(0));

                break;
            case 7: // Pump Status - handled in IntelliFlo handler
                // No action - have not verified these commands, here for documentation purposes and future enhancement
                logger.debug("Pump request status (unseen): {}", p);
                break;
            case 8: // Heat Status - A5 01 0F 10 08 0D 4B 4B 4D 55 5E 07 00 00 58 00 00 00 
                if (p.getLength() != 0x0D) {
                    logger.debug("Expected length of 13: {}", p);
                    return;
                }

                phspcur = new PentairPacketHeatSetPoint(p);

                updateChannelTemp(CONTROLLER_POOLHEAT, CONTROLLER_SETPOINT, phspcur.poolsetpoint);
                updateChannelTemp(CONTROLLER_SPAHEAT, CONTROLLER_SETPOINT, phspcur.spasetpoint);

                updateChannel(CONTROLLER_POOLHEAT, CONTROLLER_HEATMODE,
                        PentairControllerConstants.HEATMODE.get(phspcur.poolheatmode));
                updateChannel(CONTROLLER_SPAHEAT, CONTROLLER_HEATMODE,
                        PentairControllerConstants.HEATMODE.get(phspcur.spaheatmode));

                logger.debug("Heat set point: {}, {}, {}", p, phspcur.poolsetpoint, phspcur.spasetpoint);
                break;
            case 10: // Custom Names
                logger.debug("Get Custom Names (unseen): {}", p);
                break;
            case 11: // Circuit Names
                int index;

                index = p.getByte(0);
                if (index < 1 || index > NUMCIRCUITS) {
                    break;
                }

                PentairControllerCircuit pcc = circuits[index - 1];

                pcc.setName(p.getByte(2));
                pcc.setFunction(p.getByte(1));

                updateCircuitChannels(pcc.getGroup(), pcc);

                logger.debug("Circuit Names - Circuit: {}, Function: {}, Name: {}", pcc.id, pcc.getFunctionStr(),
                        pcc.getNameStr());
                break;
            case 17: // schedule - A5 1E 0F 10 11 07 01 06 0B 00 0F 00 7F
                PentairControllerSchedule ppcs;
                int id;

                id = p.getByte(PentairControllerSchedule.ID);
                if (id < 1 || id > NUMSCHEDULES) {
                    break;
                }

                ppcs = schedules[id - 1];
                ppcs.parsePacket(p);

                String group = String.format(CONTROLLER_SCHEDULE, ppcs.id);

                logger.debug("Controller schedule group: {}", group);

                updateScheduleChannels(group, ppcs);

                logger.debug(
                        "Controller Schedule - ID: {}, Type: {}, Circuit: {}, Start Time: {}:{}, End Time: {}:{}, Days: {}",
                        ppcs.id, ppcs.type, ppcs.circuit, ppcs.start / 60, ppcs.start % 60, ppcs.end / 60,
                        ppcs.end % 60, ppcs.days);
                break;
            case 18: // IntelliChem
                logger.debug("IntelliChem status: {}", p);
                break;
            case 25: // Intellichlor status
                logger.debug("Intellichlor status: {}", p);
                break;
            case 27: // Pump config (Extended)
                logger.debug("Pump Config: {}", p);
                break;
            case 29: // Valves
                logger.debug("Values: {}", p);
                break;
            case 30: // High speed circuits
                logger.debug("High speed circuits: {}", p);
                break;
            case 32: // spa-side is4/is10 remote
            case 33: // spa-side quicktouch remotes
                logger.debug("Spa-side remotes: {}", p);
                break;
            case 34: // Solar/Heat Pump status
                logger.debug("Solar/Heat Pump status: {}", p);
                break;
            case 35: // Delay status
                logger.debug("Delay status: {}", p);
                break;
            case 39: // Light Groups/Positions
                logger.debug("Light Groups/Positions; {}", p);
                break;
            case 40: // Settings? heat mode
                logger.debug("Settings?: {}", p);
                break;
            case 96: // set intellebrite colors
                logger.debug("Set intellebrite colors: {}", p);
                break;
            case 134: // Set Curcuit On/Off
                logger.debug("Set Circuit Function On/Off (unseen): {}", p);
                break;
            case 252: // Status - A5 1E 0F 10 FC 11 00 02 0A 00 00 01 0A 00 00 00 00 00 00 00 00 00 00
                majorrev = p.getByte(1);
                minorrev = p.getByte(2);
                logger.debug("SW Version - {}:{}", majorrev, minorrev);
                break;
            default:
                logger.debug("Not Implemented {}: {}", p.getAction(), p);
                break;
        }
    }

    /**
     * Helper function to update channel.
     */
    public void updateChannel(String group, String channel, boolean value) {
        updateState(group + "#" + channel, (value) ? OnOffType.ON : OnOffType.OFF);
    }

    @SuppressWarnings("null")
    public void updateChannelTemp(String group, String channel, int value) {
        if (value != 999) {
            updateState(group + "#" + channel,
                    new QuantityType<>(value, (p29cur.uom) ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT));
        } else {
            updateState(group + "#" + channel, UnDefType.UNDEF);
        }
    }

    public void updateChannel(String group, String channel, int value) {
        updateState(group + "#" + channel, new DecimalType(value));
    }

    public void updateChannel(String group, String channel, @Nullable String value) {
        if (value == null) {
            updateState(group + "#" + channel, UnDefType.NULL);
        } else {
            updateState(group + "#" + channel, new StringType(value));
        }
    }

    public void updateScheduleChannels(String group, PentairControllerSchedule pcs) {
        updateChannel(group, CONTROLLER_SCHEDULESTRING, pcs.toString());

        updateChannel(group, CONTROLLER_SCHEDULETYPE, pcs.getScheduleTypeStr());
        updateChannel(group, CONTROLLER_SCHEDULECIRCUIT, pcs.circuit);
        updateChannel(group, CONTROLLER_SCHEDULEDAYS, pcs.days);
        updateChannel(group, CONTROLLER_SCHEDULESTART, pcs.start);
        updateChannel(group, CONTROLLER_SCHEDULEEND, pcs.end);
        updateChannel(group, CONTROLLER_SCHEDULEDAYS, pcs.getDays());

        logger.debug(
                "Controller Schedule - ID: {}, Type: {}, Circuit: {}, Start Time: {}:{}, End Time: {}:{}, Days: {}",
                pcs.id, pcs.type, pcs.circuit, pcs.start / 60, pcs.start % 60, pcs.end / 60, pcs.end % 60,
                pcs.getDays());
    }

    public void updateCircuitChannels(String group, PentairControllerCircuit pcc) {
        updateChannel(group, CONTROLLER_CIRCUITSWITCH, pcc.on);
        updateChannel(group, CONTROLLER_CIRCUITMINSRUN, pcc.minsrun);
        updateChannel(group, CONTROLLER_CIRCUITNAME, pcc.getNameStr());
        updateChannel(group, CONTROLLER_CIRCUITFUNCTION, pcc.getFunctionStr());
    }
}
