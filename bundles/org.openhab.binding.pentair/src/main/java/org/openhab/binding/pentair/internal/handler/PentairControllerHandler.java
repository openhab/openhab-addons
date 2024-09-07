/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.actions.PentairControllerActions;
import org.openhab.binding.pentair.internal.handler.helpers.PentairControllerCircuit;
import org.openhab.binding.pentair.internal.handler.helpers.PentairControllerLightMode;
import org.openhab.binding.pentair.internal.handler.helpers.PentairControllerSchedule;
import org.openhab.binding.pentair.internal.handler.helpers.PentairControllerStatus;
import org.openhab.binding.pentair.internal.handler.helpers.PentairHeatStatus;
import org.openhab.binding.pentair.internal.parser.PentairBasePacket;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.openhab.binding.pentair.internal.utils.ExpiringCache;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairControllerHandler} is responsible for implementation of the EasyTouch Controller. It will handle
 * commands sent to a thing and implements the different channels. It also parses of the packets seen on the
 * bus from the controller.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairControllerHandler extends PentairBaseThingHandler {
    private static final int NUM_CIRCUITS = PentairControllerStatus.NUMCIRCUITS;
    private static final int NUM_SCHEDULES = 9;
    private static final int CACHE_EXPIRY = (int) TimeUnit.SECONDS.toMillis(60);
    private static final int CACHE_EXPIRY_LONG = (int) TimeUnit.MINUTES.toMillis(30);

    private static final List<String> CIRCUIT_GROUPS = List.of(GROUP_CONTROLLER_SPACIRCUIT,
            GROUP_CONTROLLER_AUX1CIRCUIT, GROUP_CONTROLLER_AUX2CIRCUIT, GROUP_CONTROLLER_AUX3CIRCUIT,
            GROUP_CONTROLLER_AUX4CIRCUIT, GROUP_CONTROLLER_POOLCIRCUIT, GROUP_CONTROLLER_AUX5CIRCUIT,
            GROUP_CONTROLLER_AUX6CIRCUIT, GROUP_CONTROLLER_AUX7CIRCUIT, GROUP_CONTROLLER_AUX8CIRCUIT,
            GROUP_CONTROLLER_FEATURE1, GROUP_CONTROLLER_FEATURE2, GROUP_CONTROLLER_FEATURE3, GROUP_CONTROLLER_FEATURE4,
            GROUP_CONTROLLER_FEATURE5, GROUP_CONTROLLER_FEATURE6, GROUP_CONTROLLER_FEATURE7, GROUP_CONTROLLER_FEATURE8);

    private List<ChannelUID> circuitSwitchUIDs = new ArrayList<ChannelUID>();

    private boolean serviceMode = false;
    private Unit<Temperature> uom = SIUnits.CELSIUS;

    private final Logger logger = LoggerFactory.getLogger(PentairControllerHandler.class);

    private @Nullable ScheduledFuture<?> syncTimeJob;

    private long lastScheduleTypeWrite;

    private final ExpiringCache<PentairControllerStatus> controllerStatusCache = new ExpiringCache<>(CACHE_EXPIRY);
    private final ExpiringCache<PentairHeatStatus> heatStatusCache = new ExpiringCache<>(CACHE_EXPIRY);

    private int majorrev, minorrev;

    private PentairControllerActions actions = new PentairControllerActions();

    @SuppressWarnings("unchecked")
    private final ExpiringCache<PentairControllerCircuit>[] circuitsCache = new ExpiringCache[NUM_CIRCUITS];
    @SuppressWarnings("unchecked")
    private final ExpiringCache<PentairControllerSchedule>[] schedulesCache = new ExpiringCache[NUM_SCHEDULES];

    private @Nullable PentairControllerLightMode lightMode;

    public PentairControllerHandler(Thing thing) {
        super(thing);

        for (int i = 0; i < NUM_SCHEDULES; i++) {
            schedulesCache[i] = new ExpiringCache<PentairControllerSchedule>(CACHE_EXPIRY_LONG);
        }

        for (int i = 0; i < NUM_CIRCUITS; i++) {
            circuitsCache[i] = new ExpiringCache<PentairControllerCircuit>(CACHE_EXPIRY_LONG);
        }
    }

    @Override
    public void initialize() {
        for (String group : CIRCUIT_GROUPS) {
            circuitSwitchUIDs.add(new ChannelUID(new ChannelGroupUID(this.getThing().getUID(), group),
                    CHANNEL_CONTROLLER_CIRCUITSWITCH));
        }

        super.initialize();
    }

    @Override
    public void goOnline() {
        // Only a single controller is supported on the Pentair bus so prevent multiple controller
        // things being created.
        PentairBaseBridgeHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler == null) { // will not be null here since this is validated in initialize of the super
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.bridge-missing");
            return;
        }

        PentairControllerHandler handler = bridgeHandler.findController();

        if (handler != null && !handler.equals(this)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.duplicate-controller");
        } else {
            super.goOnline();
        }
    }

    @Override
    public void finishOnline() {
        super.finishOnline();
        actions.initialize(Objects.requireNonNull(getBridgeHandler()).getBaseActions(), getPentairID());

        // setup syncTimeJob to run once a day. The initial syncTime is called as part of the initControllerSettings as
        // part of the controller coming online
        syncTimeJob = scheduler.scheduleWithFixedDelay(this::syncTime, 1, 1, TimeUnit.DAYS);

        scheduler.execute(() -> initControllerSettings());
    }

    public void syncTime() {
        boolean synctime = ((boolean) getConfig().get(CONTROLLER_CONFIGSYNCTIME));
        if (synctime) {
            logger.debug("Synchronizing System Time with Pentair controller");
            Calendar now = Calendar.getInstance();

            actions.setClockSettings(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                    now.get(Calendar.DAY_OF_WEEK), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1,
                    now.get(Calendar.YEAR) - 2000);

        }
    }

    public void initControllerSettings() {
        int i;

        actions.getSWVersion();
        actions.getHeatStatus();
        actions.getClockSettings();

        for (i = 1; i <= NUM_CIRCUITS; i++) {
            actions.getCircuitNameFunction(i);
        }

        for (i = 1; i <= NUM_SCHEDULES; i++) {
            actions.getSchedule(i);
        }

        actions.getLightGroups();
        actions.getValves();
        syncTime();
    }

    @Override
    public void goOffline(ThingStatusDetail detail) {
        super.goOffline(detail);

        ScheduledFuture<?> syncTimeJob = this.syncTimeJob;
        if (syncTimeJob != null) {
            syncTimeJob.cancel(true);
        }
    }

    public @Nullable PentairControllerCircuit getCircuitByGroupID(String group) {
        int index = CIRCUIT_GROUPS.indexOf(group);

        if (index == -1) {
            return null;
        }

        return circuitsCache[index].getLastKnownValue();
    }

    public int getScheduleNumber(String name) {
        int scheduleNum;

        scheduleNum = Integer.parseInt(name.substring(GROUP_CONTROLLER_SCHEDULE.length()));

        if (scheduleNum < 1 || scheduleNum > NUM_SCHEDULES) {
            return 0;
        }

        return scheduleNum;
    }

    public @Nullable PentairControllerSchedule getScheduleByGroupID(String groupid) {
        int scheduleNumber = getScheduleNumber(groupid);
        if (scheduleNumber == 0) {
            return null;
        }

        PentairControllerSchedule schedule = schedulesCache[scheduleNumber - 1].getLastKnownValue();

        return schedule;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String group = channelUID.getGroupId();
        if (group == null) {
            return;
        }

        if (command instanceof RefreshType) {
            logger.debug("handleCommand (refresh): {}", channelUID.getId());

            switch (group) {
                case GROUP_CONTROLLER_POOLHEAT:
                case GROUP_CONTROLLER_SPAHEAT:
                    handleRefreshHeatStatusChannel(channelUID);
                    return;
                case GROUP_CONTROLLER_STATUS:
                    handleRefreshStatusChannel(channelUID);
                    return;

                case GROUP_CONTROLLER_POOLCIRCUIT:
                case GROUP_CONTROLLER_SPACIRCUIT:
                case GROUP_CONTROLLER_AUX1CIRCUIT:
                case GROUP_CONTROLLER_AUX2CIRCUIT:
                case GROUP_CONTROLLER_AUX3CIRCUIT:
                case GROUP_CONTROLLER_AUX4CIRCUIT:
                case GROUP_CONTROLLER_AUX5CIRCUIT:
                case GROUP_CONTROLLER_AUX6CIRCUIT:
                case GROUP_CONTROLLER_AUX7CIRCUIT:
                case GROUP_CONTROLLER_AUX8CIRCUIT:
                case GROUP_CONTROLLER_FEATURE1:
                case GROUP_CONTROLLER_FEATURE2:
                case GROUP_CONTROLLER_FEATURE3:
                case GROUP_CONTROLLER_FEATURE4:
                case GROUP_CONTROLLER_FEATURE5:
                case GROUP_CONTROLLER_FEATURE6:
                case GROUP_CONTROLLER_FEATURE7:
                case GROUP_CONTROLLER_FEATURE8:
                    handleRefreshCircuitChannel(channelUID);
                    return;
            }

            if (group.substring(0, GROUP_CONTROLLER_SCHEDULE.length()).equals(GROUP_CONTROLLER_SCHEDULE)) {
                handleRefreshScheduleChannel(channelUID);
                return;
            }

            return;
        }

        logger.debug("handleCommand: {}", channelUID.getId());

        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_CONTROLLER_CIRCUITSWITCH: {
                if (!(command instanceof OnOffType onOffCommand)) {
                    logger.trace("Command is not OnOffType");
                    break;
                }

                int index = CIRCUIT_GROUPS.indexOf(group);
                if (index == -1) {
                    break;
                }

                boolean state = onOffCommand == OnOffType.ON;

                actions.setCircuitSwitch(index + 1, state);

                break;
            }
            case CHANNEL_CONTROLLER_LIGHTMODE: {
                if (!(command instanceof StringType)) {
                    break;
                }
                String str = command.toString();
                PentairControllerLightMode lightMode;

                try {
                    lightMode = PentairControllerLightMode.valueOf(str);
                    actions.setLightMode(lightMode);
                } catch (IllegalArgumentException e) {
                    logger.debug("Invalid light mode: {}", str);
                }
                break;
            }
            case CHANNEL_CONTROLLER_SCHEDULESTRING: {
                if (!(command instanceof StringType)) {
                    break;
                }
                PentairControllerSchedule schedule = getScheduleByGroupID(group);

                if (schedule == null) {
                    break;
                }
                String str = command.toString();

                if (!schedule.fromString(str)) {
                    logger.debug("schedule invalid format: {}", str);
                }
                break;
            }
            case CHANNEL_CONTROLLER_SCHEDULETYPE: {
                if (!(command instanceof StringType)) {
                    break;
                }
                PentairControllerSchedule schedule = getScheduleByGroupID(group);

                if (schedule == null) {
                    break;
                }
                String str = command.toString();
                // In order to prevent accidental programming of schedules by an inadvertent update, make sure the same
                // value is written twice to this field within 5s. Only then will the schedule update command be
                // sent to the controller.
                boolean bUpdate = (str.equals(schedule.getScheduleTypeStr())
                        && ((System.currentTimeMillis() - lastScheduleTypeWrite) < 5000) && schedule.isDirty());
                if (!schedule.setScheduleType(str)) {
                    return;
                }
                lastScheduleTypeWrite = System.currentTimeMillis();
                if (bUpdate) {
                    actions.saveSchedule(schedule);

                    lastScheduleTypeWrite = 0;
                    refreshGroupChannels(group);
                }
                break;
            }
            case CHANNEL_CONTROLLER_SCHEDULESTART: {
                if (!(command instanceof Number numberCommand)) {
                    break;
                }

                PentairControllerSchedule schedule = getScheduleByGroupID(group);

                if (schedule == null) {
                    break;
                }
                int start = numberCommand.intValue();
                schedule.setScheduleStart(start);
                break;
            }
            case CHANNEL_CONTROLLER_SCHEDULEEND: {
                if (!(command instanceof Number numberCommand)) {
                    break;
                }
                PentairControllerSchedule schedule = getScheduleByGroupID(group);
                if (schedule == null) {
                    break;
                }
                int end = numberCommand.intValue();
                schedule.setScheduleEnd(end);
                break;
            }
            case CHANNEL_CONTROLLER_SCHEDULECIRCUIT: {
                if (!(command instanceof Number numberCommand)) {
                    break;
                }
                PentairControllerSchedule schedule = getScheduleByGroupID(group);
                if (schedule == null) {
                    break;
                }
                int circuit = numberCommand.intValue();
                schedule.setScheduleCircuit(circuit);
                break;
            }
            case CHANNEL_CONTROLLER_SCHEDULEDAYS: {
                if (!(command instanceof StringType)) {
                    break;
                }
                PentairControllerSchedule schedule = getScheduleByGroupID(group);
                if (schedule == null) {
                    break;
                }
                String days = command.toString();
                schedule.setDays(days);
                break;
            }
            case CHANNEL_CONTROLLER_SETPOINT: {
                if (!(command instanceof QuantityType<?>)) {
                    break;
                }

                PentairHeatStatus heatStatus = heatStatusCache.getLastKnownValue();
                if (heatStatus == null) {
                    return;
                }

                @SuppressWarnings("unchecked")
                QuantityType<Temperature> newTempQT = (QuantityType<Temperature>) command;
                newTempQT = newTempQT.toUnit(uom); // convert to units for the controller
                if (newTempQT == null) {
                    return;
                }
                int newTemp = newTempQT.intValue();

                switch (group) {
                    case GROUP_CONTROLLER_SPAHEAT:
                        heatStatus.spaSetPoint = newTemp;
                        break;
                    case GROUP_CONTROLLER_POOLHEAT:
                        heatStatus.poolSetPoint = newTemp;
                        break;
                }

                actions.setHeatStatus(heatStatus);

                break;
            }
            case CHANNEL_CONTROLLER_HEATERDELAY: {
                if (!(command instanceof OnOffType onOffCommand)) {
                    break;
                }
                if (onOffCommand != OnOffType.OFF) { // Delay can only be cancelled
                    break;
                }

                actions.cancelDelay();
            }
        }
    }

    @Override
    public void processPacketFrom(PentairBasePacket packet) {
        PentairStandardPacket p = (PentairStandardPacket) packet;

        switch (p.getByte(PentairStandardPacket.ACTION)) {
            case 0x01: // Ack
                logger.trace("[{}] Ack command from device: {}", p.getSource(), p);
                break;
            case 0x02: // Controller Status
                if (p.getPacketLengthHeader() != 29) {
                    logger.debug("Expected length of 29: {}", p);
                    return;
                }

                logger.trace("[{}] Controller Status: {}", p.getSource(), p);

                int preambleByte = p.getByte(PentairStandardPacket.PREAMBLE); // Adjust what byte is used for preamble
                actions.setPreambleByte(preambleByte);

                if (waitStatusForOnline) {
                    finishOnline();
                }

                PentairControllerStatus currentControllerStatus = controllerStatusCache.getLastKnownValue();
                PentairControllerStatus newControllerStatus = new PentairControllerStatus();
                newControllerStatus.parsePacket(p);

                // always update the cached value to reset the expire timer
                controllerStatusCache.putValue(newControllerStatus);

                // Refresh initially when currentControllerStatus is not set - or when status has changed
                if (currentControllerStatus == null || !newControllerStatus.equals(currentControllerStatus)) {
                    logger.debug("[{}] New controller status: {} - {}", p.getSource(), newControllerStatus, p);

                    this.uom = newControllerStatus.uom;
                    this.serviceMode = newControllerStatus.serviceMode;

                    refreshChannelsFromUIDs(circuitSwitchUIDs);
                    refreshGroupChannels(GROUP_CONTROLLER_STATUS);
                    handleRefreshHeatStatusChannel(new ChannelUID(this.getThing().getUID(), GROUP_CONTROLLER_POOLHEAT,
                            CHANNEL_CONTROLLER_TEMPERATURE));
                    handleRefreshHeatStatusChannel(new ChannelUID(this.getThing().getUID(), GROUP_CONTROLLER_SPAHEAT,
                            CHANNEL_CONTROLLER_TEMPERATURE));
                }

                break;
            case 0x04: // Pump control panel on/off - handled in intelliflo controller
                // Controller sends packet often to keep control of the motor
                int data = p.getPacketLengthHeader() > 5 ? p.getByte(PentairStandardPacket.STARTOFDATA) & 0xFF : -1;
                logger.debug("[{}] Pump control panel on/off: {}|{}|{} - {}", p.getSource(),
                        p.getByte(PentairStandardPacket.ACTION), //
                        p.getByte(PentairStandardPacket.LENGTH), data, p);
                break;
            case 0x05: // Current Clock - A5 01 0F 10 05 08 0E 09 02 1D 04 11 00 00 - H M DOW D M YY YY ??
                int hour = p.getByte(0 + PentairStandardPacket.STARTOFDATA);
                int minute = p.getByte(1 + PentairStandardPacket.STARTOFDATA);
                int dow = p.getByte(2 + PentairStandardPacket.STARTOFDATA);
                int day = p.getByte(3 + PentairStandardPacket.STARTOFDATA);
                int month = p.getByte(4 + PentairStandardPacket.STARTOFDATA);
                int year = p.getByte(5 + PentairStandardPacket.STARTOFDATA);

                logger.debug("[{}] System Clock: {}.{}.{} {}:{}, DOW={}", p.getSource(), day, month, year, hour, minute,
                        dow);
                break;
            case 0x06: // Set run mode
                // No action - have not verified these commands, here for documentation purposes and future enhancement
                if (p.getPacketLengthHeader() != 1) {
                    logger.debug("[{}] Expected run mode length of 1: {}", p.getSource(), p);
                    return;
                }
                int run = p.getByte(PentairStandardPacket.STARTOFDATA) & 0xFF;
                String s;
                switch (run) {
                    case 0x04: // off
                        s = "OFF";
                        break;
                    case 0x0A: // on
                        s = "ON";
                        break;
                    default:
                        s = "n/a (" + run + ")";
                }
                logger.debug("[{}] Set run mode for device {}: {} ", p.getSource(), p.getDest(), s);
                break;
            case 0x07: // Pump Status - handled in IntelliFlo handler
                logger.trace("[{}] Pump request status (unseen): {}", p.getSource(), p);
                break;
            case 0x08: // Heat Status - A5 01 0F 10 08 0D 4B 4B 4D 55 5E 07 00 00 58 00 00 00 
                if (p.getPacketLengthHeader() != 0x0D) {
                    logger.debug("Expected length of 13: {}", p);
                    return;
                }

                PentairHeatStatus heatStatus = new PentairHeatStatus(p);
                heatStatusCache.putValue(heatStatus);

                logger.debug("[{}] Heat status: {} - {}", p.getSource(), heatStatus, p);

                refreshGroupChannels(GROUP_CONTROLLER_POOLHEAT);
                refreshGroupChannels(GROUP_CONTROLLER_SPAHEAT);
                break;
            case 0x0A: // Custom Names
                logger.trace("[{}] Get Custom Names (unseen): {}", p.getSource(), p);
                break;
            case 0x0B: // Circuit Names
                int index;

                index = p.getByte(0 + PentairStandardPacket.STARTOFDATA);
                index--; // zero index
                if (index < 0 || index >= NUM_CIRCUITS) {
                    break;
                }
                PentairControllerCircuit circuit = new PentairControllerCircuit(index + 1);
                circuit.setName(p.getByte(2 + PentairStandardPacket.STARTOFDATA));
                circuit.setFunction(p.getByte(1 + PentairStandardPacket.STARTOFDATA));

                circuitsCache[index].putValue(circuit);

                refreshGroupChannels(CIRCUIT_GROUPS.get(index));
                logger.debug("[{}] Circuit Names - Circuit: {}, Function: {}, Name: {}", p.getSource(), circuit.id,
                        circuit.circuitFunction.getFriendlyName(), circuit.circuitName.getFriendlyName());
                break;
            case 0x11: // schedule - A5 1E 0F 10 11 07 01 06 0B 00 0F 00 7F
                PentairControllerSchedule schedule = new PentairControllerSchedule(p);

                if (schedule.id < 1 || schedule.id > NUM_SCHEDULES) {
                    break;
                }
                String groupID = schedule.getGroupID();
                schedulesCache[schedule.id - 1].putValue(schedule);

                refreshGroupChannels(groupID);

                logger.debug(
                        "[{}] Controller Schedule - ID: {}, Name: {}, Type: {}, Circuit: {}, Start Time: {}:{}, End Time: {}:{}, Days: {}",
                        p.getSource(), schedule.id, schedule.type.getName(), schedule.type, schedule.circuit,
                        schedule.start / 60, schedule.start % 60, schedule.end / 60, schedule.end % 60, schedule.days);
                break;
            case 0x12: // IntelliChem
                logger.debug("[{}] IntelliChem status: {}", p.getSource(), p);
                break;
            case 0x19: // Intellichlor status
                logger.trace("[{}] Intellichlor status: {}", p.getSource(), p);
                break;
            case 0x1B: // Pump config (Extended)
                logger.debug("[{}] Pump Config: {}", p.getSource(), p);
                break;
            case 0x1D: // Valves
                logger.debug("[{}] Values: {}", p.getSource(), p);
                break;
            case 0x1E: // High speed circuits
                logger.debug("[{}] High speed circuits: {}", p.getSource(), p);
                break;
            case 0x20: // spa-side is4/is10 remote
            case 0x21: // spa-side quicktouch remotes
                logger.debug("[{}] Spa-side remotes: {}", p.getSource(), p);
                break;
            case 0x22: // Solar/Heat Pump status
                logger.trace("[{}] Solar/Heat Pump status: {}", p.getSource(), p);
                break;
            case 0x23: // Delay status
                logger.debug("[{}] Delay status: {}", p.getSource(), p);
                break;
            case 0x27: // Light Groups/Positions
                logger.trace("[{}] Light Groups/Positions; {}", p.getSource(), p);
                break;
            case 0x28: // Settings? heat mode
                logger.trace("[{}] Settings?: {}", p.getSource(), p);
                break;
            case 0x60: // set intellibrite colors
                logger.trace("[{}] Set intellibrite colors: {}", p.getSource(), p);
                break;
            case 0x86: // Set Curcuit On/Off
                logger.trace("[{}] Set Circuit Function On/Off (unseen): {}", p.getSource(), p);
                break;
            case 0xD2: // Get Intellichem status
                logger.trace("[{}] Get IntelliChem status: {}", p.getSource(), p);
                break;
            case 0xFC: // Status - A5 1E 0F 10 FC 11 00 02 0A 00 00 01 0A 00 00 00 00 00 00 00 00 00 00
                majorrev = p.getByte(1 + PentairStandardPacket.STARTOFDATA);
                minorrev = p.getByte(2 + PentairStandardPacket.STARTOFDATA);

                String version = String.format("%d.%d", majorrev, minorrev);
                updateProperty(PROPERTY_CONTROLLER_FIRMWAREVERSION, version);
                logger.debug("[{}] SW Version - {}", p.getSource(), version);
                break;
            default:
                logger.debug("[{}] Not Implemented {}: {}", p.getSource(), p.getByte(PentairStandardPacket.ACTION), p);

                break;
        }
    }

    /*
     * Helper routines to handle Refresh commands
     */

    private void handleRefreshScheduleChannel(ChannelUID channelUID) {
        String group = channelUID.getGroupId();
        String channel = channelUID.getIdWithoutGroup();

        if (group == null) {
            return;
        }

        PentairControllerSchedule schedule = getScheduleByGroupID(group);
        if (schedule == null) {
            return;
        }

        switch (channel) {
            case CHANNEL_CONTROLLER_SCHEDULESTRING:
                updateChannel(channelUID, schedule.toString());
                return;
            case CHANNEL_CONTROLLER_SCHEDULETYPE:
                String type = schedule.getScheduleTypeStr();
                updateChannel(channelUID, type);
                return;
            case CHANNEL_CONTROLLER_SCHEDULECIRCUIT:
                updateChannel(channelUID, schedule.circuit);
                return;
            case CHANNEL_CONTROLLER_SCHEDULESTART:
                updateChannel(channelUID, schedule.start, Units.MINUTE);
                return;
            case CHANNEL_CONTROLLER_SCHEDULEEND:
                updateChannel(channelUID, schedule.end, Units.MINUTE);
                return;
            case CHANNEL_CONTROLLER_SCHEDULEDAYS:
                updateChannel(channelUID, schedule.getDays());
                return;
        }
    }

    private void handleRefreshStatusChannel(ChannelUID channelUID) {
        String channel = channelUID.getIdWithoutGroup();

        PentairControllerStatus status = controllerStatusCache.getValue(() -> {

            actions.getStatus();

        });

        if (status == null) {
            return;
        }

        switch (channel) {
            case CHANNEL_CONTROLLER_AIRTEMPERATURE:
                updateChannel(channelUID, status.airTemp, uom);
                return;
            case CHANNEL_CONTROLLER_SOLARTEMPERATURE:
                updateChannel(channelUID, status.solarTemp, uom);
                return;
            case CHANNEL_CONTROLLER_SERVICEMODE:
                updateChannel(channelUID, status.serviceMode);
                return;
            case CHANNEL_CONTROLLER_SOLARON:
                updateChannel(channelUID, status.solarOn);
                return;
            case CHANNEL_CONTROLLER_HEATERON:
                updateChannel(channelUID, status.heaterOn);
                return;
            case CHANNEL_CONTROLLER_HEATERDELAY:
                updateChannel(channelUID, status.heaterDelay);
                return;
            case CHANNEL_CONTROLLER_LIGHTMODE:
                PentairControllerLightMode lightMode = this.lightMode;
                if (lightMode != null) {
                    updateChannel(channelUID, lightMode.name());
                }
                return;
        }
    }

    private void handleRefreshHeatStatusChannel(ChannelUID channelUID) {
        String group = channelUID.getGroupId();
        String channel = channelUID.getIdWithoutGroup();

        if (group == null) {
            return;
        }

        boolean poolChannel = group.equals(GROUP_CONTROLLER_POOLHEAT);
        PentairHeatStatus heatStatus = heatStatusCache.getLastKnownValue();

        if (heatStatus == null) {
            return;
        }

        switch (channel) {
            case CHANNEL_CONTROLLER_SETPOINT:
                updateChannel(channelUID, (poolChannel) ? heatStatus.poolSetPoint : heatStatus.spaSetPoint, uom);
                return;
            case CHANNEL_CONTROLLER_HEATMODE:
                updateChannel(channelUID,
                        (poolChannel) ? heatStatus.poolHeatMode.name() : heatStatus.spaHeatMode.name());
                return;
            case CHANNEL_CONTROLLER_TEMPERATURE: {
                PentairControllerStatus status = this.controllerStatusCache.getLastKnownValue();
                if (status == null) {
                    return;
                }

                if (poolChannel) {
                    if (status.pool) {
                        updateChannel(channelUID, status.poolTemp, uom);
                    } else {
                        updateState(channelUID, UnDefType.UNDEF);
                    }
                } else {
                    if (status.spa) {
                        updateChannel(channelUID, status.poolTemp, uom);
                    } else {
                        updateState(channelUID, UnDefType.UNDEF);
                    }
                }
            }
        }
    }

    private void handleRefreshCircuitChannel(ChannelUID channelUID) {
        String group = channelUID.getGroupId();
        String channel = channelUID.getIdWithoutGroup();

        if (group == null) {
            return;
        }

        switch (channel) {
            case CHANNEL_CONTROLLER_CIRCUITNAME:
            case CHANNEL_CONTROLLER_CIRCUITFUNCTION: {
                PentairControllerCircuit circuit = getCircuitByGroupID(group);
                if (circuit == null) {
                    return;
                }

                String circuitString = channel.equals(CHANNEL_CONTROLLER_CIRCUITNAME)
                        ? circuit.circuitName.getFriendlyName()
                        : circuit.circuitFunction.getFriendlyName();

                updateChannel(channelUID, circuitString);
                return;
            }
            case CHANNEL_CONTROLLER_CIRCUITSWITCH: {
                PentairControllerStatus status = controllerStatusCache.getValue(() -> {
                    actions.getStatus();

                });

                int index = CIRCUIT_GROUPS.indexOf(group);

                if (index == -1 || status == null) {
                    return;
                }
                boolean on = status.circuits[index];
                updateChannel(channelUID, on);
                return;
            }
        }
    }

    public boolean getServiceMode() {
        return serviceMode;
    }
}
