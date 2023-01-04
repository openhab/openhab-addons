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
package org.openhab.binding.amplipi.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Changes to a current preset The contents of state and commands will be completely replaced if populated. Merging old
 * and new updates seems too complicated and error prone.
 **/
@Schema(description = "Changes to a current preset  The contents of state and commands will be completely replaced if populated. Merging old and new updates seems too complicated and error prone.")
public class PresetUpdate {

    @Schema
    /**
     * Friendly name
     **/
    private String name;

    @Schema
    private PresetState state;

    @Schema
    private List<Command> commands = null;

    /**
     * Friendly name
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PresetUpdate name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get state
     *
     * @return state
     **/
    public PresetState getState() {
        return state;
    }

    public void setState(PresetState state) {
        this.state = state;
    }

    public PresetUpdate state(PresetState state) {
        this.state = state;
        return this;
    }

    /**
     * Get commands
     *
     * @return commands
     **/
    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    public PresetUpdate commands(List<Command> commands) {
        this.commands = commands;
        return this;
    }

    public PresetUpdate addCommandsItem(Command commandsItem) {
        this.commands.add(commandsItem);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PresetUpdate {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    commands: ").append(toIndentedString(commands)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
