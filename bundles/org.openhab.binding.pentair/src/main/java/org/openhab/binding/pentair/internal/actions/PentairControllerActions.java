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
package org.openhab.binding.pentair.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pentair.internal.handler.helpers.PentairControllerLightMode;
import org.openhab.binding.pentair.internal.handler.helpers.PentairControllerSchedule;
import org.openhab.binding.pentair.internal.handler.helpers.PentairHeatStatus;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairControllerActions } class to be used as base for all action commands to send on Pentair bus
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairControllerActions extends PentairBaseActions {
    private final Logger logger = LoggerFactory.getLogger(PentairControllerActions.class);

    public enum ControllerCommand {
        GET_STATUS(0x02, 0x02),
        GET_CLOCK_SETTINGS(0xC5, 0x05),
        SET_CLOCK_SETTINGS(0x85, -1),
        SET_CIRCUIT_SWITCH(0x86, 0x01),
        GET_LIGHT_GROUPS(0xE7, 0x27),
        SET_LIGHT_MODE(0x60, 0x01),
        GET_VALVES(0xDD, 0x1D),
        GET_CIRCUIT_NAME_FUNCTION(0xCB, 0x0B),
        SAVE_SCHEDULE(0x91, 0x01),
        GET_SCHEDULE(0xD1, 0x11),
        GET_SW_VERSION(0xFD, 0xFC),
        DELAY_CANCEL(0x83, 0x01),
        GET_HEAT_STATUS(0xC8, 0x08),
        SET_HEAT_STATUS(0x88, 0x01);

        public int send, response;

        ControllerCommand(int send, int response) {
            this.send = send;
            this.response = response;
        }
    }

    // Byte to use after 0xA5 in communicating to controller. Not sure why this changes,
    // but it requires to be in sync and up-to-date
    private int preambleByte = -1;

    public void setPreambleByte(int preambleByte) {
        this.preambleByte = preambleByte;
    }

    /**
     * Method to turn on/off a circuit in response to a command from the framework
     *
     * @param circuit circuit number
     * @param state
     */
    public boolean setCircuitSwitch(int circuit, boolean state) {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.SET_CIRCUIT_SWITCH.send, (byte) 0x02, (byte) circuit,
                (byte) ((state) ? 1 : 0) };

        if (!getWriter().writePacket(packet, ControllerCommand.SET_CIRCUIT_SWITCH.response, 1)) {
            logger.trace("setCircuitSwitch: Timeout");
            return false;
        }
        return true;
    }

    /**
     * Method to request clock
     */
    public boolean getClockSettings() { // A5 01 10 20 C5 01 00
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.GET_CLOCK_SETTINGS.send, (byte) 0x01, (byte) 0x00 };

        if (!getWriter().writePacket(packet, ControllerCommand.GET_CLOCK_SETTINGS.response, 1)) {
            logger.trace("getClockSettings: Timeout");
            return false;
        }
        return true;
    }

    /**
     * Method to request controller status
     * Note the controller regularly sends out status, so this rarely needs to be called
     *
     */
    public boolean getStatus() { // A5 01 10 20 02 01 00
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.GET_STATUS.send, (byte) 0x01, (byte) 0x00 };

        if (!getWriter().writePacket(packet, ControllerCommand.GET_STATUS.response, 1)) {
            logger.trace("requestControllerStatus: Timeout");
            return false;
        }
        return true;
    }

    public boolean getLightGroups() {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.GET_LIGHT_GROUPS.send, (byte) 0x01, (byte) 0x00 };

        if (!getWriter().writePacket(packet, ControllerCommand.GET_LIGHT_GROUPS.response, 1)) {
            logger.trace("getLightGroups: Timeout");
            return false;
        }
        return true;
    }

    public boolean setLightMode(int mode) {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */, (byte) 0x60,
                (byte) 0x02, (byte) mode, (byte) 0x00 };

        if (!getWriter().writePacket(packet, 0x01, 1)) {
            logger.trace("setLightMode: Timeout");
            return false;
        }
        return true;
    }

    public boolean setLightMode(PentairControllerLightMode lightMode) {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.SET_LIGHT_MODE.send, (byte) 0x02, (byte) lightMode.getModeNumber(),
                (byte) 0x00 };

        if (!getWriter().writePacket(packet, ControllerCommand.SET_LIGHT_MODE.response, 1)) {
            logger.trace("setLightMode: Timeout");
            return false;
        }
        return true;
    }

    public boolean getValves() {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.GET_VALVES.send, (byte) 0x01, (byte) 0x00 };

        if (!getWriter().writePacket(packet, ControllerCommand.GET_VALVES.response, 1)) {
            logger.trace("getValves: Timeout");
            return false;
        }
        return true;
    }

    public boolean getCircuitNameFunction(int circuit) {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.GET_CIRCUIT_NAME_FUNCTION.send, (byte) 0x01, (byte) circuit };

        if (!getWriter().writePacket(packet, ControllerCommand.GET_CIRCUIT_NAME_FUNCTION.response, 1)) {
            logger.trace("getCircuitNameFunction: Timeout");
            return false;
        }
        return true;
    }

    public boolean getSchedule(int num) {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.GET_SCHEDULE.send, (byte) 0x01, (byte) num };

        if (!getWriter().writePacket(packet, ControllerCommand.GET_SCHEDULE.response, 1)) {
            logger.trace("getSchedule: Timeout");
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
        PentairStandardPacket p;

        p = schedule.getWritePacket(id, preambleByte);
        if (p == null) {
            logger.debug("Schedule {} type is unknown.", id);
            return false;
        }

        schedule.setDirty(false);

        if (!getWriter().writePacket(p, 0x01, 1)) {
            logger.trace("saveSchedule: Timeout");
            return false;
        }
        return true;
    }

    public boolean getSWVersion() {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.GET_SW_VERSION.send, (byte) 0x01, (byte) 0x00 };

        if (!getWriter().writePacket(packet, ControllerCommand.GET_SW_VERSION.response, 1)) {
            logger.trace("requestSWVersion: Timeout");
            return false;
        }
        return true;
    }

    public boolean cancelDelay() {
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.DELAY_CANCEL.send, (byte) 0x01, (byte) 0x00 };

        if (!getWriter().writePacket(packet, ControllerCommand.DELAY_CANCEL.response, 1)) {
            logger.trace("cancelDelay: Timeout");
            return false;
        }
        return true;
    }

    /**
     * Method to set clock
     */
    public boolean setClockSettings(int hour, int min, int dow, int day, int month, int year) {
        // A5 01 10 20 85 08 0D 2A 02 1D 04 11 00 00

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

        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.SET_CLOCK_SETTINGS.send, (byte) 0x08, (byte) hour, (byte) min, (byte) dow,
                (byte) day, (byte) month, (byte) year, (byte) 0x00, (byte) 0x00 };

        getWriter().writePacket(packet);
        return true;
    }

    public boolean getHeatStatus() { // A5 01 10 20 C8 01 00
        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.GET_HEAT_STATUS.send, (byte) 0x01, (byte) 0 };

        if (!getWriter().writePacket(packet, ControllerCommand.GET_HEAT_STATUS.response, 1)) {
            logger.trace("getHeatStatus: Timeout");
            return false;
        }
        return true;
    }

    /**
     * Method to set heat point for pool (true) of spa (false)
     *
     * @param Pool pool=true, spa=false
     * @param temp
     */
    public boolean setHeatStatus(PentairHeatStatus pentairHeatStatus) {
        // [16,34,136,4,POOL HEAT Temp,SPA HEAT Temp,Heat Mode,0,2,56]
        // [165, preambleByte, 16, 34, 136, 4, currentHeat.poolSetPoint, parseInt(req.params.temp), updateHeatMode, 0]
        int heatmode = (pentairHeatStatus.spaHeatMode.getCode() << 2) | pentairHeatStatus.poolHeatMode.getCode();

        byte[] packet = { (byte) 0xA5, (byte) preambleByte, (byte) id, (byte) 0x00 /* source */,
                (byte) ControllerCommand.SET_HEAT_STATUS.send, (byte) 0x04, (byte) pentairHeatStatus.poolSetPoint,
                (byte) pentairHeatStatus.spaSetPoint, (byte) heatmode, (byte) 0 };

        if (!getWriter().writePacket(packet, ControllerCommand.SET_HEAT_STATUS.response, 1)) {
            logger.trace("setHeatStatus: Timeout");
            return false;
        }
        return true;
    }
}
