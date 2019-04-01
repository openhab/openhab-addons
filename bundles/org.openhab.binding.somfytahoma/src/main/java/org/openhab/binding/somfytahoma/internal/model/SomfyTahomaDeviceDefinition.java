/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaDeviceDefinition} holds information about a device definition.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDeviceDefinition {

    private ArrayList<SomfyTahomaDeviceDefinitionCommand> commands = new ArrayList<>();

    public ArrayList<SomfyTahomaDeviceDefinitionCommand> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for( SomfyTahomaDeviceDefinitionCommand cmd: commands) {
            sb.append(cmd.toString()).append("; ");
        }

        sb.append("}");
        return sb.toString();
    }
}
