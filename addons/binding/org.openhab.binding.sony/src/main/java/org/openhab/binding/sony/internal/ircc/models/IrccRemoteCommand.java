/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccRemoteCommand.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccRemoteCommand {

    /** The name. */
    private final String name;

    /** The type. */
    private final String type;

    /** The cmd. */
    private final String cmd;

    /**
     * Instantiates a new ircc remote command.
     *
     * @param name the name
     * @param type the type
     * @param cmd the cmd
     */
    public IrccRemoteCommand(String name, String type, String cmd) {
        this.name = name;
        this.type = type;
        this.cmd = cmd;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the cmd.
     *
     * @return the cmd
     */
    public String getCmd() {
        return cmd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name + " (" + type + "): " + cmd;
    }
}