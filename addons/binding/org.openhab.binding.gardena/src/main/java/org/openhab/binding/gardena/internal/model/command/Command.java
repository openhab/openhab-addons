/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model.command;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for a Gardena command with parameters.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class Command {

    @SerializedName(value = "name")
    protected String command;
    protected CommandParameters parameters;

    /**
     * Creates a command with the given name.
     */
    public Command(String command) {
        this.command = command;
    }

    /**
     * Returns the command name.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the parameters of the command.
     */
    public CommandParameters getParameters() {
        return parameters;
    }

    /**
     * Sets the parameters of the command.
     */
    public void setParameters(CommandParameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Class to hold the command parameters.
     *
     * @author Gerhard Riegler - Initial contribution
     */
    public class CommandParameters {
        private String duration;
        @SerializedName("manual_override")
        private String manualOverride;

        /**
         * Returns the duration parameter.
         */
        public String getDuration() {
            return duration;
        }

        /**
         * Sets the duration parameter.
         */
        public void setDuration(String duration) {
            this.duration = duration;
        }

        /**
         * Returns the manual override parameter.
         */
        public String getManualOverride() {
            return manualOverride;
        }

        /**
         * Sets the manual override parameter.
         */
        public void setManualOverride(String manualOverride) {
            this.manualOverride = manualOverride;
        }
    }

}
