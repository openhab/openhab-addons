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
import org.openhab.binding.veluxklf200.internal.components.VeluxNode;
import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class KLFCMD_GetNode.
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdGetNode extends BaseKLFCommand {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KlfCmdGetNode.class);

    /** The node id. */
    private byte nodeId;

    /** The node. */
    private VeluxNode node;

    /**
     * Instantiates a new KLFCM D get node.
     *
     * @param nodeId the node id
     */
    public KlfCmdGetNode(byte nodeId) {
        super();
        this.nodeId = nodeId;
    }

    /**
     * Gets the node id.
     *
     * @return the node id
     */
    public byte getNodeId() {
        return this.nodeId;
    }

    /**
     * Gets the node.
     *
     * @return the node
     */
    public VeluxNode getNode() {
        return this.node;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand#handleResponse(byte[])
     */
    @Override
    public void handleResponse(byte[] data) {
        logger.trace("Handling response: {}", KLFUtils.formatBytes(data));
        short responseCode = KLFUtils.decodeKLFCommand(data);
        switch (responseCode) {
            case KLFCommandCodes.GW_GET_NODE_INFORMATION_CFM:
                if (data[FIRSTBYTE] == 0) {
                    // Command has been accepted by the bridge
                    logger.trace("Command executing, expecting data for node Id: {}.", data[FIRSTBYTE + 1]);
                } else {
                    // Command has been rejected by the bridge
                    logger.error("Command has been rejected by the KLF200 unit.");
                    this.commandStatus = CommandStatus.ERROR;
                }
                break;
            case KLFCommandCodes.GW_GET_NODE_INFORMATION_NTF:
                logger.trace("Get Node: {}", KLFUtils.formatBytes(data));
                VeluxNode node = new VeluxNode(KLFUtils.extractOneByte(data[FIRSTBYTE]), // NodeID
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE + 1], data[FIRSTBYTE + 2]), // Order
                        KLFUtils.extractOneByte(data[FIRSTBYTE + 3]), // Placement
                        KLFUtils.extractUTF8String(data, FIRSTBYTE + 4, FIRSTBYTE + 67), // Name
                        KLFUtils.extractOneByte(data[FIRSTBYTE + 68]), // Velocity
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE + 69], data[FIRSTBYTE + 70]), // nodeType
                        KLFUtils.extractOneByte(data[FIRSTBYTE + 75]), // buildNumber
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE + 85], data[FIRSTBYTE + 86]), // currentPosition
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE + 87], data[FIRSTBYTE + 88]), // targetPosition
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE + 89], data[FIRSTBYTE + 90]), // FP1
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE + 91], data[FIRSTBYTE + 92]), // FP2
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE + 93], data[FIRSTBYTE + 94]), // FP3
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE + 95], data[FIRSTBYTE + 96]), // FP4
                        KLFUtils.extractTwoBytes(data[FIRSTBYTE + 97], data[FIRSTBYTE + 98]), // remainingTime
                        KLFUtils.extractUnsignedInt32(data[FIRSTBYTE + 99], data[FIRSTBYTE + 100],
                                data[FIRSTBYTE + 101], data[FIRSTBYTE + 102]), // lastCommand
                        KLFUtils.extractOneByte(data[FIRSTBYTE + 71]), // Product
                                                                       // group
                        KLFUtils.extractOneByte(data[FIRSTBYTE + 72]), // Product
                                                                       // type
                        KLFUtils.extractOneByte(data[FIRSTBYTE + 73]), // Node
                                                                       // variation
                        KLFUtils.extractOneByte(data[FIRSTBYTE + 74]), // Power Mode
                        KLFUtils.extractOneByte(data[FIRSTBYTE + 84]), // State
                        String.valueOf(KLFUtils.extractFourBytes(data[FIRSTBYTE + 76], data[FIRSTBYTE + 77],
                                data[FIRSTBYTE + 78], data[FIRSTBYTE + 79])) // serial
                                                                             // number
                );
                logger.trace("Retrieved information successfully for node '" + node.getName() + "'.");
                this.node = node;
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
     * @see org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand#getKLFCommandStructure()
     */
    @Override
    public KLFCommandStructure getKLFCommandStructure() {
        return KLFCommandStructure.GET_NODE_INFORMATION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand#pack()
     */
    @Override
    protected byte[] pack() {
        setMainNode(this.nodeId);
        return new byte[] { this.nodeId };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand#extractSession(short, byte[])
     */
    @Override
    protected int extractSession(short responseCode, byte[] data) {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand#extractNode(byte[])
     */
    @Override
    protected byte extractNode(short responseCode, byte[] data) {
        switch (responseCode) {
            case KLFCommandCodes.GW_GET_NODE_INFORMATION_CFM:
                return data[FIRSTBYTE + 1];
            case KLFCommandCodes.GW_GET_NODE_INFORMATION_NTF:
                return data[FIRSTBYTE];
            default:
                return BaseKLFCommand.NOT_REQUIRED;
        }

    }

}
