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
 * The {@link SomfyTahomaAction} holds information about a list of
 * commands for one concrete device.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaAction {

    private String deviceURL = "";
    private ArrayList<SomfyTahomaCommand> commands = new ArrayList<SomfyTahomaCommand>();

    public String getDeviceURL() {
        return deviceURL;
    }

    public void setDeviceURL(String deviceURL) {
        this.deviceURL = deviceURL;
    }

    public ArrayList<SomfyTahomaCommand> getCommands() {
        return commands;
    }

    public void addCommand(SomfyTahomaCommand command) {
        this.commands.add(command);
    }
}
