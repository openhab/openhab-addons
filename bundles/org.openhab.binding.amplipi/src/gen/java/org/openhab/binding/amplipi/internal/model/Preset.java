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

import com.google.gson.annotations.SerializedName;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A partial controller configuration the can be loaded on demand. In addition to most of the configuration found in
 * Status, this can contain commands as well that configure the state of different streaming services.
 **/
@Schema(description = "A partial controller configuration the can be loaded on demand. In addition to most of the configuration found in Status, this can contain commands as well that configure the state of different streaming services.")
public class Preset {

    @Schema
    /**
     * Unique identifier
     **/
    private Integer id;

    @Schema(required = true)
    /**
     * Friendly name
     **/
    private String name;

    @Schema(required = true)
    private PresetState state;

    @Schema
    private List<Command> commands = null;

    @Schema
    @SerializedName("last_used")
    private Integer lastUsed;

    /**
     * Unique identifier
     *
     * @return id
     **/
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Preset id(Integer id) {
        this.id = id;
        return this;
    }

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

    public Preset name(String name) {
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

    public Preset state(PresetState state) {
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

    public Preset commands(List<Command> commands) {
        this.commands = commands;
        return this;
    }

    public Preset addCommandsItem(Command commandsItem) {
        this.commands.add(commandsItem);
        return this;
    }

    /**
     * Get lastUsed
     *
     * @return lastUsed
     **/
    public Integer getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Integer lastUsed) {
        this.lastUsed = lastUsed;
    }

    public Preset lastUsed(Integer lastUsed) {
        this.lastUsed = lastUsed;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Preset {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    commands: ").append(toIndentedString(commands)).append("\n");
        sb.append("    lastUsed: ").append(toIndentedString(lastUsed)).append("\n");
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
