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
import java.util.List;

import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandCodes;
import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandStructure;
import org.openhab.binding.veluxklf200.internal.components.VeluxNode;
import org.openhab.binding.veluxklf200.internal.components.VeluxNodeType;
import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to query a KLF200 unit and receive information about all of the nodes
 * that it is aware of. The command first executes a
 * GW_GET_ALL_NODES_INFORMATION_REQ and then subsequently expects a
 * GW_GET_ALL_NODES_INFORMATION_CFM to indicate that the command has been
 * accepted. The unit will then response with zero or more
 * GW_GET_ALL_NODES_INFORMATION_NTF with details of each individual node. When
 * all NTF's have been sent, the unit will send a
 * GW_GET_ALL_NODES_INFORMATION_FINISHED_NTF to indicate that it has sent
 * responses for all of its nodes.
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdGetAllNodes extends BaseKLFCommand {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KlfCmdGetAllNodes.class);

    /** Holds all of the nodes that have been discovered. */
    private List<VeluxNode> nodes;

    /**
     * Constructor.
     */
    public KlfCmdGetAllNodes() {
        super();
        this.nodes = new ArrayList<VeluxNode>();
    }

    /**
     * Gets the list of nodes that have been discovered as a result of executing
     * the command.
     *
     * @return List of nodes
     */
    public List<VeluxNode> getNodes() {
        return this.nodes;
    }

    /**
     * Gets the node by type.
     *
     * @param type the type
     * @return the node by type
     */
    public List<VeluxNode> getNodeByType(VeluxNodeType type) {
        ArrayList<VeluxNode> ret = new ArrayList<VeluxNode>();
        for (VeluxNode n : this.nodes) {
            if (n.getNodeType() == type) {
                ret.add(n);
            }
        }
        return ret;
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
            case KLFCommandCodes.GW_GET_ALL_NODES_INFORMATION_CFM:
                if (data[FIRSTBYTE] == 0) {
                    // Command has been accepted by the bridge
                    logger.trace("Command executing, expecting data for {} nodes.", data[FIRSTBYTE + 1]);
                } else {
                    // Command has been rejected by the bridge
                    logger.error("Command has been rejected by the KLF200 unit.");
                    this.commandStatus = CommandStatus.ERROR;
                }
                break;
            case KLFCommandCodes.GW_GET_ALL_NODES_INFORMATION_NTF:
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
                logger.trace("Found node: " + node.getName());
                this.nodes.add(node);
                break;
            case KLFCommandCodes.GW_GET_ALL_NODES_INFORMATION_FINISHED_NTF:
                logger.trace("Command completed, data for all nodes recieved");
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
        return KLFCommandStructure.GET_ALL_NODE_INFORMATION;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.velux.klf200.internal.commands.BaseKLFCommand#pack()
     */
    @Override
    protected byte[] pack() {
        // Command GW_GET_ALL_NODES_INFORMATION_REQ expects no input data. As
        // such, returning an empty array.
        return new byte[] {};
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#extractSession(byte[])
     */
    @Override
    protected int extractSession(short responseCode, byte[] data) {
        return 0;
    }

}
