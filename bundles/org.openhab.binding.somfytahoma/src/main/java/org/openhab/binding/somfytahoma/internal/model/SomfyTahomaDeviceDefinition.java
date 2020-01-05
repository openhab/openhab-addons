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
package org.openhab.binding.somfytahoma.internal.model;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SomfyTahomaDeviceDefinition} holds information about a device definition.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDeviceDefinition {

    private ArrayList<SomfyTahomaDeviceDefinitionCommand> commands = new ArrayList<>();
    private ArrayList<SomfyTahomaDeviceDefinitionState> states = new ArrayList<>();

    public ArrayList<SomfyTahomaDeviceDefinitionCommand> getCommands() {
        return commands;
    }

    public ArrayList<SomfyTahomaDeviceDefinitionState> getStates() {
        return states;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Commands: { ");
        for (SomfyTahomaDeviceDefinitionCommand cmd : commands) {
            sb.append(cmd.toString()).append("; ");
        }

        sb.append("}\nStates: {");
        for (SomfyTahomaDeviceDefinitionState state : states) {
            sb.append(state.toString()).append("; ");
        }
        sb.append("}");
        return sb.toString();
    }
}
