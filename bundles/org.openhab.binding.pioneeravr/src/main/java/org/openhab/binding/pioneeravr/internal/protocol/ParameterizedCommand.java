/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrConnectionException;

/**
 * A command which accept a parameter.
 *
 * @author Antoine Besnard - Initial contribution
 * @author Leroy Foerster - Listening Mode, Playing Listening Mode
 */
public class ParameterizedCommand extends SimpleCommand {

    /**
     * List of the commands with a parameter.
     */
    public enum ParameterizedCommandType implements AvrCommand.CommandType {

        VOLUME_SET("[0-9]{2,3}", "VL", "ZV", "YV", "HZV"),
        INPUT_CHANNEL_SET("[0-9]{2}", "FN", "ZS", "ZT", "ZEA"),
        LISTENING_MODE_SET("[0-9]{4}", "SR"),
        MCACC_MEMORY_SET("[1-6]{1}", "MC");

        private String[] zoneCommands;
        private String parameterPattern;

        private ParameterizedCommandType(String parameterPattern, String... zoneCommands) {
            this.zoneCommands = zoneCommands;
            this.parameterPattern = parameterPattern;
        }

        @Override
        public String getCommand() {
            return zoneCommands[0];
        }

        @Override
        public String getCommand(int zone) {
            return zoneCommands[zone - 1];
        }

        public String getParameterPattern() {
            return parameterPattern;
        }
    }

    private String parameter;

    private String parameterPattern;

    protected ParameterizedCommand(ParameterizedCommandType command) {
        this(command, 0);
    }

    protected ParameterizedCommand(ParameterizedCommandType command, int zone) {
        super(command, zone);
        this.parameterPattern = command.getParameterPattern();
    }

    /**
     * Return the command to send to the AVR with the parameter value configured.
     *
     * throws {@link AvrConnectionException} if the parameter is null, empty or has a bad format.
     */
    @Override
    public String getCommand() throws AvrConnectionException {
        if (parameter == null) {
            throw new AvrConnectionException(
                    "The parameter of the command " + super.getCommand() + " must not be null.");
        }

        if (parameterPattern != null && !parameterPattern.isEmpty() && !parameter.matches(parameterPattern)) {
            throw new AvrConnectionException("The parameter value " + parameter + " of the command "
                    + super.getCommand() + " does not match the pattern " + parameterPattern);
        }

        return parameter + super.getCommand();
    }

    public ParameterizedCommand setParameter(String parameter) {
        this.parameter = parameter;
        return this;
    }

    public String getParameter() {
        return this.parameter;
    }

    public String getParameterPattern() {
        return parameterPattern;
    }
}
