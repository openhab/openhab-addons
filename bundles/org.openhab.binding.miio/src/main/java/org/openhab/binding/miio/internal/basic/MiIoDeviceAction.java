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
package org.openhab.binding.miio.internal.basic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping actions from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoDeviceAction {

    @SerializedName("command")
    @Expose
    private @Nullable String command;
    @SerializedName("parameterType")
    @Expose
    private CommandParameterType commandParameterType = CommandParameterType.EMPTY;
    @SerializedName("parameters")
    @Expose
    private @Nullable JsonArray parameters;
    @SerializedName("siid")
    @Expose
    private @Nullable Integer siid;
    @SerializedName("aiid")
    @Expose
    private @Nullable Integer aiid;
    @SerializedName("condition")
    @Expose
    private @Nullable MiIoDeviceActionCondition condition;

    public JsonArray getParameters() {
        final @Nullable JsonArray parameter = this.parameters;
        return parameter != null ? parameter : new JsonArray();
    }

    public void setParameters(JsonArray parameters) {
        this.parameters = parameters;
    }

    public String getCommand() {
        final @Nullable String command = this.command;
        return command != null ? command : "";
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public CommandParameterType getparameterType() {
        return commandParameterType;
    }

    public void setparameterType(CommandParameterType type) {
        this.commandParameterType = type;
    }

    public void setparameterType(String type) {
        this.commandParameterType = org.openhab.binding.miio.internal.basic.CommandParameterType.fromString(type);
    }

    public int getSiid() {
        final Integer siid = this.siid;
        if (siid != null) {
            return siid.intValue();
        } else {
            return 0;
        }
    }

    public void setSiid(Integer siid) {
        this.siid = siid;
    }

    public int getAiid() {
        final Integer aiid = this.aiid;
        if (aiid != null) {
            return aiid.intValue();
        } else {
            return 0;
        }
    }

    public void setAiid(Integer aiid) {
        this.aiid = aiid;
    }

    public boolean isMiOtAction() {
        return aiid != null && siid != null && (getAiid() != 0 || getSiid() != 0);
    }

    public @Nullable MiIoDeviceActionCondition getCondition() {
        return condition;
    }

    public void setCondition(@Nullable MiIoDeviceActionCondition condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "MiIoDeviceAction [command=" + command + ", commandParameterType=" + commandParameterType
                + (parameters != null ? ", parameters=" + getParameters().toString() : "") + "]";
    }
}
