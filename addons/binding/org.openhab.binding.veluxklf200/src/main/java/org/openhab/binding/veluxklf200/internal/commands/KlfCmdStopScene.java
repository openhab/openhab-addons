/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.commands;

import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandCodes;
import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandStructure;
import org.openhab.binding.veluxklf200.internal.components.VeluxRunStatus;
import org.openhab.binding.veluxklf200.internal.components.VeluxStatusReply;
import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stops a scene that is running on the KLF 200 unit.
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdStopScene extends BaseKLFCommand {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KlfCmdStopScene.class);

    /** The scene id for the scene to be run. */
    private byte sceneId;

    /**
     * Constructor.
     *
     * @param sceneId the scene id
     */
    public KlfCmdStopScene(byte sceneId) {
        super();
        this.sceneId = sceneId;
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
            case KLFCommandCodes.GW_STOP_SCENE_CFM:
                switch (data[FIRSTBYTE]) {
                    case 0:
                        logger.info("Request to stop scene accepted");
                        break;
                    case 1:
                        logger.error("Request to stop scene rejected - Invalid Parameter");
                        this.commandStatus = CommandStatus.ERROR
                                .setErrorDetail("Request to execute scene rejected - Invalid Parameter");
                        break;
                    case 2:
                        logger.error("Request to stop scene rejected - Request Rejected");
                        this.commandStatus = CommandStatus.ERROR
                                .setErrorDetail("Request to execute scene rejected - Request Rejected");
                        break;
                }
                break;
            case KLFCommandCodes.GW_SESSION_FINISHED_NTF:
                logger.info("Finished stopping the scene");
                this.commandStatus = CommandStatus.COMPLETE;
                break;
            /*
             * case KLFCommandCodes.GW_NODE_STATE_POSITION_CHANGED_NTF:
             * logger.trace(
             * "Node {} position changed, state: {}, current position: {} open, target position:{} open, time remaining: {} seconds."
             * ,
             * data[FIRSTBYTE], VeluxState.create(data[FIRSTBYTE + 1]),
             * VeluxPosition.create(data[FIRSTBYTE + 2], data[FIRSTBYTE + 3]).getPercentageOpen(),
             * VeluxPosition.create(data[FIRSTBYTE + 4], data[FIRSTBYTE + 5]).getPercentageOpen(),
             * KLFUtils.extractTwoBytes(data[FIRSTBYTE + 14], data[FIRSTBYTE + 15]));
             * break;
             */
            case KLFCommandCodes.GW_COMMAND_RUN_STATUS_NTF:
                VeluxRunStatus runStatus = VeluxRunStatus.create(data[FIRSTBYTE + 7]);
                VeluxStatusReply statusReply = VeluxStatusReply.create(data[FIRSTBYTE + 8]);
                logger.trace(
                        "Notification for Node {}, relating to function parameter {}, Run status is: {}, Command status is: {} ",
                        data[FIRSTBYTE + 3], data[FIRSTBYTE + 4], runStatus, statusReply);
                break;
            /*
             * case KLFCommandCodes.GW_COMMAND_REMAINING_TIME_NTF:
             * logger.trace(
             * "Notification for Node {}, relating to function parameter {}, time remaining to complete is {} seconds",
             * data[FIRSTBYTE + 2], data[FIRSTBYTE + 3],
             * KLFUtils.extractTwoBytes(data[FIRSTBYTE + 4], data[FIRSTBYTE + 5]));
             * break;
             */
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
        return KLFCommandStructure.STOP_SCENE;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.velux.klf200.internal.commands.BaseKLFCommand#pack()
     */
    @Override
    protected byte[] pack() {
        byte[] data = new byte[5];
        data[0] = (byte) (this.getSessionID() >>> 8);
        data[1] = (byte) this.getSessionID();
        data[2] = CMD_ORIGINATOR_USER;
        data[3] = CMD_PRIORITY_NORMAL;
        data[4] = this.sceneId;
        return data;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#extractSession(short,
     * byte[])
     */
    @Override
    protected int extractSession(short responseCode, byte[] data) {
        switch (responseCode) {
            case KLFCommandCodes.GW_SESSION_FINISHED_NTF:
                // case KLFCommandCodes.GW_COMMAND_REMAINING_TIME_NTF:
            case KLFCommandCodes.GW_COMMAND_RUN_STATUS_NTF:
                return KLFUtils.extractTwoBytes(data[FIRSTBYTE], data[FIRSTBYTE + 1]);
            // case KLFCommandCodes.GW_NODE_STATE_POSITION_CHANGED_NTF:
            // This command does not include a session parameter, so just return
            // our own current session ID instead
            // return this.getSessionID();
            case KLFCommandCodes.GW_STOP_SCENE_CFM:
            default:
                return KLFUtils.extractTwoBytes(data[FIRSTBYTE + 1], data[FIRSTBYTE + 2]);
        }
    }

}
