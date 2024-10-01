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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.handler.PentairIntelliFloHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairIntelliFloActions } class to be used as base for all action commands to send on Pentair bus
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairIntelliFloActions extends PentairBaseActions {
    private final Logger logger = LoggerFactory.getLogger(PentairIntelliFloActions.class);

    public enum PumpCommand {
        GET_STATUS(0x07, 0x07),
        SET_LOCAL_OR_REMOTE_CONTROL(0x04, 0x04),
        SET_ON_OR_OFF(0x06, 0x06),
        SET_RPM(0x01, 0x01),
        SET_RUN_PROGRAM(0x01, 0x06);

        public int send, response;

        PumpCommand(int send, int response) {
            this.send = send;
            this.response = response;
        }
    }

    @Nullable
    private PentairIntelliFloHandler handler;

    public void setHandler(PentairIntelliFloHandler handler) {
        this.handler = handler;
    }

    public PentairIntelliFloHandler getHandler() {
        return Objects.requireNonNull(handler);
    }

    /* Commands to send to IntelliFlo */
    private boolean coreGetStatus() {
        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */,
                (byte) PumpCommand.GET_STATUS.send, (byte) 0x00 };

        if (!getWriter().writePacket(packet, PumpCommand.GET_STATUS.response, 1)) {
            logger.debug("sendRequestStatus: Timeout");
            return false;
        }
        return true;
    }

    public boolean getStatus() {
        boolean success = setLocalORRemoteControl(false);
        success &= coreGetStatus();
        return success;
    }

    public boolean setLocalORRemoteControl(boolean bLocal) {
        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */,
                (byte) PumpCommand.SET_LOCAL_OR_REMOTE_CONTROL.send, (byte) 0x01,
                (bLocal) ? (byte) 0x00 : (byte) 0xFF };

        if (!getWriter().writePacket(packet, PumpCommand.SET_LOCAL_OR_REMOTE_CONTROL.response, 1)) {
            logger.debug("sendLocalOrRemoteControl: Timeout");
            return false;
        }
        return true;
    }

    public boolean coreSetOnOROff(boolean bOn) {
        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */,
                (byte) PumpCommand.SET_ON_OR_OFF.send, (byte) 0x01, (bOn) ? (byte) 0x0A : (byte) 0x04 };

        getHandler().setRunMode(bOn);
        if (!getWriter().writePacket(packet, PumpCommand.SET_ON_OR_OFF.response, 1)) {
            logger.trace("sendPumpOnOROff: Timeout");
            return false;
        }
        return true;
    }

    public boolean setOnOrOff(boolean bOn) {
        boolean success = setLocalORRemoteControl(false);
        success &= coreSetOnOROff(bOn);
        success &= coreGetStatus();
        success &= setLocalORRemoteControl(true);
        return success;
    }

    // sendPumpRPM - low-level call to send to pump the RPM command
    private boolean coreSetRPM(int rpm) {
        int rpmH, rpmL;

        rpmH = rpm / 256;
        rpmL = rpm % 256;

        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */,
                (byte) PumpCommand.SET_RPM.send, (byte) 0x04, (byte) 0x02, (byte) 0xC4, (byte) rpmH, (byte) rpmL };

        if (rpm < 400 || rpm > 3450) {
            throw new IllegalArgumentException("rpm not in range [400..3450]: " + rpm);
        }

        getHandler().setRunMode(true);
        if (!getWriter().writePacket(packet, PumpCommand.SET_RPM.response, 1)) {
            logger.debug("sendPumpRPM: timeout");
            return false;
        }
        return true;
    }

    // setPumpRPM - high-level call that includes wrapper commands and delay functions
    public boolean setRPM(int rpm) {
        boolean success = setLocalORRemoteControl(false);
        success &= coreSetRPM(rpm);
        success &= coreSetOnOROff(true);
        success &= coreGetStatus();
        success &= setLocalORRemoteControl(true);
        return success;
    }

    // sendRunProgram - low-level call to send the command to pump
    private boolean coreSetRunProgram(int program) {
        if (program < 1 || program > 4) {
            return false;
        }

        byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) id, (byte) 0x00 /* source */,
                (byte) PumpCommand.SET_RUN_PROGRAM.send, (byte) 0x04, (byte) 0x03, (byte) 0x21, (byte) 0x00,
                (byte) (program << 3) };

        getHandler().setRunMode(true);
        if (!getWriter().writePacket(packet, PumpCommand.SET_RUN_PROGRAM.response, 1)) {
            logger.debug("sendRunProgram: Timeout");
            return false;
        }
        return true;
    }

    // setRunProgram - high-level call to run program - including wrapper calls
    public boolean setRunProgram(int program) {
        boolean success = setLocalORRemoteControl(false);
        success &= coreSetRunProgram(program);
        success &= coreSetOnOROff(true);
        success &= coreGetStatus();
        success &= setLocalORRemoteControl(true);
        return success;
    }
}
