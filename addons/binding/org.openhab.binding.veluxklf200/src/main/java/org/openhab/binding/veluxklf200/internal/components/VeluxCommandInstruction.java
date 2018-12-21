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
 * Provides the information / parameters required to execute a command.
 *
 * @author MFK - Initial Contribution
 */
public class VeluxCommandInstruction {

    /**
     * Indicates the functional parameter of the device that the command is to
     * be executed on. The 'main parameter' is parameter 0 (zero)
     */
    private byte function;

    /** Indicates the command / possition that should be sent to the function. */
    private short position;

    /** Indicates the node on which the command is to be executed. */
    private byte nodeId;

    /**
     * Instantiates a new velux command instruction.
     *
     * @param nodeId
     *                     the node id
     * @param function
     *                     the function
     * @param position
     *                     the position
     */
    public VeluxCommandInstruction(byte nodeId, byte function, short position) {
        this.nodeId = nodeId;
        this.function = function;
        this.position = position;
    }

    /**
     * Gets the node id.
     *
     * @return the node id
     */
    public byte getNodeId() {
        return nodeId;
    }

    /**
     * Gets the function.
     *
     * @return the function
     */
    public byte getFunction() {
        return function;
    }

    /**
     * Gets the position.
     *
     * @return the position
     */
    public short getPosition() {
        return position;
    }
}
