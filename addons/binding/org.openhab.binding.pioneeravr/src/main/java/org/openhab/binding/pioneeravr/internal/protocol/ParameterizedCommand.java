/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.protocol;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.pioneeravr.protocol.AvrCommand;
import org.openhab.binding.pioneeravr.protocol.AvrConnectionException;

/**
 * A command which accept a parameter.
 *
 * @author Antoine Besnard
 *
 */
public class ParameterizedCommand extends SimpleCommand {

    /**
     * List of the commands with a parameter.
     *
     * @author Antoine Besnard
     *
     */
    public enum ParameterizedCommandType implements AvrCommand.CommandType {

        VOLUME_SET("[0-9]{2,3}", "VL", "ZV", "YV", "HZV"),
        INPUT_CHANNEL_SET("[0-9]{2}", "FN", "ZS", "ZT", "ZEA");

        private String[] zoneCommands;
        private String parameterPattern;

        private ParameterizedCommandType(String parameterPattern, String... zoneCommands) {
            this.zoneCommands = zoneCommands;
            this.parameterPattern = parameterPattern;
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

        if (StringUtils.isNotEmpty(parameterPattern) && !parameter.matches(parameterPattern)) {
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
