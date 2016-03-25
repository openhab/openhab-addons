/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.protocol;

import org.openhab.binding.pioneeravr.protocol.AvrCommand;

/**
 * A simple command without parameters.
 * 
 * @author Antoine Besnard
 *
 */
public class SimpleCommand implements AvrCommand {

    /**
     * List of the simple command types.
     * 
     * @author Antoine Besnard
     *
     */
    public enum SimpleCommandType implements AvrCommand.CommandType {

        POWER_ON("PO"),
        POWER_OFF("PF"),
        POWER_QUERY("?P"),
        VOLUME_UP("VU"),
        VOLUME_DOWN("VD"),
        VOLUME_QUERY("?V"),
        MUTE_ON("MO"),
        MUTE_OFF("MF"),
        MUTE_QUERY("?M"),
        INPUT_CHANGE_CYCLIC("FU"),
        INPUT_CHANGE_REVERSE("FD"),
        INPUT_QUERY("?F");

        private String command;

        private SimpleCommandType(String command) {
            this.command = command;
        }

        @Override
        public String getCommand() {
            return command;
        }
    }

    private CommandType commandType;

    public SimpleCommand(CommandType commandType) {
        this.commandType = commandType;
    }

    @Override
    public String getCommand() {
        return commandType.getCommand() + "\r";
    }

    @Override
    public CommandType getCommandType() {
        return commandType;
    }

}
