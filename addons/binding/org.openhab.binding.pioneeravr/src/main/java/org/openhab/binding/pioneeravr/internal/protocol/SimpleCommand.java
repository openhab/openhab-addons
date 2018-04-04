/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

        POWER_ON("PO", "APO", "BPO"),
        POWER_OFF("PF", "APF", "BPF"),
        POWER_QUERY("?P", "?AP", "?BP"),
        VOLUME_UP("VU", "ZU", "YU"),
        VOLUME_DOWN("VD", "ZD", "YD"),
        VOLUME_QUERY("?V", "?ZV", "?YV"),
        MUTE_ON("MO", "Z2MO", "Z3MO"),
        MUTE_OFF("MF", "Z2MF", "Z3MF"),
        MUTE_QUERY("?M", "?Z2M", "?Z3M"),
        INPUT_CHANGE_CYCLIC("FU"),
        INPUT_CHANGE_REVERSE("FD"),
        INPUT_QUERY("?F", "?ZS", "?ZT");

        private String zoneCommands[];

        private SimpleCommandType(String... command) {
            this.zoneCommands = command;
        }

        @Override
        public String getCommand(int zone) {
            return zoneCommands[zone - 1];
        }
    }

    private CommandType commandType;
    private int zone;

    public SimpleCommand(CommandType commandType, int zone) {
        this.commandType = commandType;
        this.zone = zone;
    }

    @Override
    public String getCommand() {
        return commandType.getCommand(zone) + "\r";
    }

    @Override
    public CommandType getCommandType() {
        return commandType;
    }

    @Override
    public int getZone() {
        return zone;
    }

}
