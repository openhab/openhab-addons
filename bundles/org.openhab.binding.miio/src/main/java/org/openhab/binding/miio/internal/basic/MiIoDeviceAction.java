/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
    @SerializedName("preCommandParameter1")
    @Expose
    private @Nullable String preCommandParameter1;
    @SerializedName("parameter1")
    @Expose
    private @Nullable String parameter1;
    @SerializedName("parameter2")
    @Expose
    private @Nullable String parameter2;
    @SerializedName("parameter3")
    @Expose
    private @Nullable String parameter3;

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

    public @Nullable String getPreCommandParameter1() {
        return preCommandParameter1;
    }

    public void setPreCommandParameter1(String preCommandParameter1) {
        this.preCommandParameter1 = preCommandParameter1;
    }

    public @Nullable String getParameter1() {
        return parameter1;
    }

    public void setParameter1(String parameter1) {
        this.parameter1 = parameter1;
    }

    public @Nullable String getParameter2() {
        return parameter2;
    }

    public void setParameter2(String parameter2) {
        this.parameter1 = parameter2;
    }

    public @Nullable String getParameter3() {
        return parameter3;
    }

    public void setParameter3(String parameter3) {
        this.parameter1 = parameter3;
    }

    @Override
    public String toString() {
        return "MiIoDeviceAction [command=" + command + ", commandParameterType=" + commandParameterType
                + ", preCommandParameter1=" + preCommandParameter1 + ", parameter1=" + parameter1 + ", parameter2="
                + parameter2 + ", parameter3=" + parameter3 + "]";
    }
}
