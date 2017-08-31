/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.model;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaDeviceDefinition} holds information about a device definition.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaDeviceDefinition {

    private ArrayList<SomfyTahomaDeviceDefinitionCommand> commands;

    public ArrayList<SomfyTahomaDeviceDefinitionCommand> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("commands: { ");
        for( SomfyTahomaDeviceDefinitionCommand cmd: commands) {
            sb.append(cmd.toString()).append("; ");
        }

        sb.append("}");
        return sb.toString();
    }
}
