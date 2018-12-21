/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandCodes;
import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandStructure;
import org.openhab.binding.veluxklf200.internal.components.VeluxCommandInstruction;
import org.openhab.binding.veluxklf200.internal.components.VeluxRunStatus;
import org.openhab.binding.veluxklf200.internal.components.VeluxStatusReply;
import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages sending of commands to the KLF200.
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdSendCommand extends BaseKLFCommand {

    /** Main function point on unit. */
    public static final byte MAIN_PARAMETER = (byte) 0x0000;

    /** Used to tell the unit to stop actuating. */
    public static final short STOP_PARAMETER = (short) 0xD200;

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KlfCmdSendCommand.class);

    /** List of instructions to be sent with this command. */
    private List<VeluxCommandInstruction> commands;

    /**
     * Constructor varient that creates a command with a single instruction to
     * send.
     *
     * @param nodeId
     *                        the node id
     * @param function
     *                        The functional parameter of the device that you intend to
     *                        control. The 'main parameter' on the device is parameter 0.
     * @param nodeCommand
     *                        the node command
     */
    public KlfCmdSendCommand(byte nodeId, byte function, short nodeCommand) {
        super();
        this.commands = new ArrayList<VeluxCommandInstruction>();
        commands.add(new VeluxCommandInstruction(nodeId, function, nodeCommand));
    }

    /**
     * Constructor varient that creates a command with a single instruction to
     * send.
     *
     * @param instruction
     *                        the instruction
     */
    public KlfCmdSendCommand(VeluxCommandInstruction instruction) {
        super();
        this.commands = new ArrayList<VeluxCommandInstruction>();
        commands.add(instruction);
    }

    /**
     * Constructor varient that creates a command with a list of instructions to
     * send to the KLF200.
     *
     * @param instructions
     *                         the list of instructions
     */
    public KlfCmdSendCommand(List<VeluxCommandInstruction> instructions) {
        super();
        this.commands = new ArrayList<VeluxCommandInstruction>();
        commands.addAll(instructions);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#handleResponse(byte[])
     */
    @Override
    public void handleResponse(byte[] data) {
        logger.trace("Handling response: {}", KLFUtils.formatBytes(data));
        short responseCode = KLFUtils.decodeKLFCommand(data);
        switch (responseCode) {
            case KLFCommandCodes.GW_COMMAND_SEND_CFM:
                switch (data[FIRSTBYTE + 2]) {
                    case 0:
                        logger.warn("The command was rejected, marking the command as ERROR.");
                        this.commandStatus = CommandStatus.ERROR;
                        break;
                    case 1:
                        logger.debug("Command accepted for session: {}",
                                KLFUtils.extractTwoBytes(data[FIRSTBYTE], data[FIRSTBYTE + 1]));
                        break;
                    default:
                        logger.error("An unknown confirmation code was recieved: {}, marking the command as ERROR.",
                                data[FIRSTBYTE + 2]);
                        this.commandStatus = CommandStatus.ERROR;
                        break;
                }
                break;
            case KLFCommandCodes.GW_COMMAND_RUN_STATUS_NTF:
                VeluxRunStatus runStatus = VeluxRunStatus.create(data[FIRSTBYTE + 7]);
                VeluxStatusReply statusReply = VeluxStatusReply.create(data[FIRSTBYTE + 8]);
                logger.debug(
                        "Notification for Node {}, relating to function parameter {}, Session: {}, Run status is: {}, Command status is: {} ",
                        data[FIRSTBYTE + 3], KLFUtils.extractTwoBytes(data[FIRSTBYTE], data[FIRSTBYTE + 1]),
                        data[FIRSTBYTE + 4], runStatus, statusReply);
                break;
            case KLFCommandCodes.GW_COMMAND_REMAINING_TIME_NTF:
                logger.debug(
                        "Notification for Node {}, session: {}, relating to function parameter {}, time remaining to complete is {} seconds",
                        data[FIRSTBYTE + 2], KLFUtils.extractTwoBytes(data[FIRSTBYTE], data[FIRSTBYTE + 1]),
                        data[FIRSTBYTE + 3], KLFUtils.extractTwoBytes(data[FIRSTBYTE + 4], data[FIRSTBYTE + 5]));
                break;
            case KLFCommandCodes.GW_SESSION_FINISHED_NTF:
                logger.debug("Processing of the command with session: {} is complete.",
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE], data[FIRSTBYTE + 1]));
                this.commandStatus = CommandStatus.COMPLETE;
                break;
            default:
                // This should not happen. If it does, the most likely cause is that
                // the KLFCommandStructure has not been configured or implemented
                // correctly.
                this.commandStatus = CommandStatus.ERROR;
                logger.error("Processing requested for a KLF response code (command code) that is not supported: {}.",
                        responseCode);
                break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#getKLFCommandStructure
     * ()
     */
    @Override
    public KLFCommandStructure getKLFCommandStructure() {
        return KLFCommandStructure.SEND_NODE_COMMAND;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.velux.klf200.internal.commands.BaseKLFCommand#pack()
     */
    @Override
    protected byte[] pack() {
        byte[] data = new byte[66];
        // Session ID
        data[0] = (byte) (this.getSessionID() >>> 8);
        data[1] = (byte) this.getSessionID();

        // CommandOriginator
        data[2] = CMD_ORIGINATOR_USER;

        // PriorityLevel
        data[3] = CMD_PRIORITY_NORMAL;

        int counter = 0;
        for (Iterator<VeluxCommandInstruction> it = this.commands.iterator(); it.hasNext();) {
            VeluxCommandInstruction cmd = it.next();
            // ParameterActive
            data[4] = cmd.getFunction();

            // FunctionalParameterValueArray
            data[7 + (counter * 2)] = (byte) (cmd.getPosition() >>> 8);
            data[8 + (counter * 2)] = (byte) cmd.getPosition();

            // IndexArray
            data[42 + counter] = cmd.getNodeId();
            counter++;
        }
        data[41] = (byte) counter;
        return data;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#extractSession(byte[])
     */
    @Override
    protected int extractSession(short responseCode, byte[] data) {
        return KLFUtils.extractTwoBytes(data[FIRSTBYTE], data[FIRSTBYTE + 1]);
    }

}
