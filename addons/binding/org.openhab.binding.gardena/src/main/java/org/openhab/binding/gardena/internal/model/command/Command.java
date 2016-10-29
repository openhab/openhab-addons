/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public abstract class Command {

    @JsonProperty(value = "name")
    protected String command;
    @JsonProperty
    protected CommandParameters parameters;

    public Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public CommandParameters getParameters() {
        return parameters;
    }

    public void setParameters(CommandParameters parameters) {
        this.parameters = parameters;
    }

    @JsonInclude(Include.NON_NULL)
    public class CommandParameters {
        @JsonProperty
        private String duration;
        @JsonProperty(value = "manual_override")
        private String manualOverride;

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getManualOverride() {
            return manualOverride;
        }

        public void setManualOverride(String manualOverride) {
            this.manualOverride = manualOverride;
        }
    }

}
