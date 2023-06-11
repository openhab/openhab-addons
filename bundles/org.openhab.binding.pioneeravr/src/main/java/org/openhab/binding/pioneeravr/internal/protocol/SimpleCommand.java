/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.pioneeravr.internal.protocol;

import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrCommand;

/**
 * A simple command without parameters.
 *
 * @author Antoine Besnard - Initial contribution
 * @author Leroy Foerster - Listening Mode, Playing Listening Mode
 */
public class SimpleCommand implements AvrCommand {

    /**
     * List of the simple command types.
     */
    public enum SimpleCommandType implements AvrCommand.CommandType {

        POWER_ON("PO", "APO", "BPO", "ZEO"),
        POWER_OFF("PF", "APF", "BPF", "ZEF"),
        POWER_QUERY("?P", "?AP", "?BP", "?ZEP"),
        VOLUME_UP("VU", "ZU", "YU", "HZU"),
        VOLUME_DOWN("VD", "ZD", "YD", "HZD"),
        VOLUME_QUERY("?V", "?ZV", "?YV", "?HZV"),
        MUTE_ON("MO", "Z2MO", "Z3MO", "HZMO"),
        MUTE_OFF("MF", "Z2MF", "Z3MF", "HZMF"),
        MUTE_QUERY("?M", "?Z2M", "?Z3M", "?HZM"),
        INPUT_CHANGE_CYCLIC("FU"),
        INPUT_CHANGE_REVERSE("FD"),
        LISTENING_MODE_CHANGE_CYCLIC("0010SR"),
        LISTENING_MODE_QUERY("?S"),
        INPUT_QUERY("?F", "?ZS", "?ZT", "?ZEA"),
        MCACC_MEMORY_CHANGE_CYCLIC("0MC"),
        MCACC_MEMORY_QUERY("?MC");

        private String zoneCommands[];

        private SimpleCommandType(String... command) {
            this.zoneCommands = command;
        }

        @Override
        public String getCommand() {
            return zoneCommands[0];
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

    public SimpleCommand(CommandType commandType) {
        this(commandType, 0);
    }

    @Override
    public String getCommand() {
        if (zone == 0) {
            return commandType.getCommand() + "\r";
        }
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
