/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.components;

/**
 * Indicates the end state of a command that was executed.
 *
 * @author MFK - Initial Contribution
 */
public enum VeluxStatusReply {

    /** The unknown status reply. */
    UNKNOWN_STATUS_REPLY((byte) 0x00),

    /** The command completed ok. */
    COMMAND_COMPLETED_OK((byte) 0x01),

    /** The no contact. */
    NO_CONTACT((byte) 0x02),

    /** The manually operated. */
    MANUALLY_OPERATED((byte) 0x03),

    /** The blocked. */
    BLOCKED((byte) 0x04),

    /** The wrong systemkey. */
    WRONG_SYSTEMKEY((byte) 0x05),

    /** The priority level locked. */
    PRIORITY_LEVEL_LOCKED((byte) 0x06),

    /** The reached wrong position. */
    REACHED_WRONG_POSITION((byte) 0x07),

    /** The error during execution. */
    ERROR_DURING_EXECUTION((byte) 0x08),

    /** The no execution. */
    NO_EXECUTION((byte) 0x09),

    /** The power consumption too high. */
    POWER_CONSUMPTION_TOO_HIGH((byte) 0x0C),

    /** The lock position open. */
    LOCK_POSITION_OPEN((byte) 0x0D),

    /** The mttl communiction needed. */
    MTTL_COMMUNICTION_NEEDED((byte) 0x0E),

    /** The thermal protection. */
    THERMAL_PROTECTION((byte) 0x0F),

    /** The battery level. */
    BATTERY_LEVEL((byte) 0x12),

    /** The target modified. */
    TARGET_MODIFIED((byte) 0x13),

    /** The mode not implemented. */
    MODE_NOT_IMPLEMENTED((byte) 0x14),

    /** The command incompatible. */
    COMMAND_INCOMPATIBLE((byte) 0x15),

    /** The user action. */
    USER_ACTION((byte) 0x16),

    /** The dead bolt error. */
    DEAD_BOLT_ERROR((byte) 0x17),

    /** The auto cycle engaged. */
    AUTO_CYCLE_ENGAGED((byte) 0x18),

    /** The wrong load connected. */
    WRONG_LOAD_CONNECTED((byte) 0x19),

    /** The colour not reachable. */
    COLOUR_NOT_REACHABLE((byte) 0x1A),

    /** The target not reachable. */
    TARGET_NOT_REACHABLE((byte) 0x1B),

    /** The bad index received. */
    BAD_INDEX_RECEIVED((byte) 0x1C),

    /** The command overruled. */
    COMMAND_OVERRULED((byte) 0x1D),

    /** The node waiting for power. */
    NODE_WAITING_FOR_POWER((byte) 0x1E),

    /** The information code. */
    INFORMATION_CODE((byte) 0xDF),

    /** The parameter limited. */
    PARAMETER_LIMITED((byte) 0xE0),

    /** The limitation by local user. */
    LIMITATION_BY_LOCAL_USER((byte) 0xE1),

    /** The limitation by user. */
    LIMITATION_BY_USER((byte) 0xE2),

    /** The limitation by rain. */
    LIMITATION_BY_RAIN((byte) 0xE3),

    /** The limitation by timer. */
    LIMITATION_BY_TIMER((byte) 0xE4),

    /** The limitation by ups. */
    LIMITATION_BY_UPS((byte) 0xE6),

    /** The limitation by unknown. */
    LIMITATION_BY_UNKNOWN((byte) 0xE7),

    /** The limitation by saac. */
    LIMITATION_BY_SAAC((byte) 0xEA),

    /** The limitation by wind. */
    LIMITATION_BY_WIND((byte) 0xEB),

    /** The limitation by myself. */
    LIMITATION_BY_MYSELF((byte) 0xEC),

    /** The limitation by auto cycle. */
    LIMITATION_BY_AUTO_CYCLE((byte) 0xED),

    /** The limitation by emergency. */
    LIMITATION_BY_EMERGENCY((byte) 0xEE);

    /** The status code. */
    private byte statusCode;

    /**
     * Instantiates a new velux status reply.
     *
     * @param code
     *                 the code
     */
    private VeluxStatusReply(byte code) {
        this.statusCode = code;
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public byte getStatusCode() {
        return this.statusCode;
    }

    /**
     * Creates the.
     *
     * @param code
     *                 the code
     * @return the velux status reply
     */
    public static VeluxStatusReply create(byte code) {
        switch (code) {
            case (byte) 0x00:
                return UNKNOWN_STATUS_REPLY;
            case (byte) 0x01:
                return COMMAND_COMPLETED_OK;
            case (byte) 0x02:
                return NO_CONTACT;
            case (byte) 0x03:
                return MANUALLY_OPERATED;
            case (byte) 0x04:
                return BLOCKED;
            case (byte) 0x05:
                return WRONG_SYSTEMKEY;
            case (byte) 0x06:
                return PRIORITY_LEVEL_LOCKED;
            case (byte) 0x07:
                return REACHED_WRONG_POSITION;
            case (byte) 0x08:
                return ERROR_DURING_EXECUTION;
            case (byte) 0x09:
                return NO_EXECUTION;
            case (byte) 0x0C:
                return POWER_CONSUMPTION_TOO_HIGH;
            case (byte) 0x0D:
                return LOCK_POSITION_OPEN;
            case (byte) 0x0E:
                return MTTL_COMMUNICTION_NEEDED;
            case (byte) 0x0F:
                return THERMAL_PROTECTION;
            case (byte) 0x12:
                return BATTERY_LEVEL;
            case (byte) 0x13:
                return TARGET_MODIFIED;
            case (byte) 0x14:
                return MODE_NOT_IMPLEMENTED;
            case (byte) 0x15:
                return COMMAND_INCOMPATIBLE;
            case (byte) 0x16:
                return USER_ACTION;
            case (byte) 0x17:
                return DEAD_BOLT_ERROR;
            case (byte) 0x18:
                return AUTO_CYCLE_ENGAGED;
            case (byte) 0x19:
                return WRONG_LOAD_CONNECTED;
            case (byte) 0x1A:
                return COLOUR_NOT_REACHABLE;
            case (byte) 0x1B:
                return TARGET_NOT_REACHABLE;
            case (byte) 0x1C:
                return BAD_INDEX_RECEIVED;
            case (byte) 0x1D:
                return COMMAND_OVERRULED;
            case (byte) 0x1E:
                return NODE_WAITING_FOR_POWER;
            case (byte) 0xDF:
                return INFORMATION_CODE;
            case (byte) 0xE0:
                return PARAMETER_LIMITED;
            case (byte) 0xE1:
                return LIMITATION_BY_LOCAL_USER;
            case (byte) 0xE2:
                return LIMITATION_BY_USER;
            case (byte) 0xE3:
                return LIMITATION_BY_RAIN;
            case (byte) 0xE4:
                return LIMITATION_BY_TIMER;
            case (byte) 0xE6:
                return LIMITATION_BY_UPS;
            case (byte) 0xE7:
                return LIMITATION_BY_UNKNOWN;
            case (byte) 0xEA:
                return LIMITATION_BY_SAAC;
            case (byte) 0xEB:
                return LIMITATION_BY_WIND;
            case (byte) 0xEC:
                return LIMITATION_BY_MYSELF;
            case (byte) 0xED:
                return LIMITATION_BY_AUTO_CYCLE;
            case (byte) 0xEE:
                return LIMITATION_BY_EMERGENCY;
            default:
                return UNKNOWN_STATUS_REPLY;
        }
    }

}
