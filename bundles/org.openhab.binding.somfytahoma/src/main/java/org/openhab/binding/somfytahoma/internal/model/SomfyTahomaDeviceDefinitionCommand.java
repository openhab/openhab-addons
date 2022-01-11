/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * The {@link SomfyTahomaDeviceDefinitionCommand} holds information about commands
 * accepted by a device.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDeviceDefinitionCommand {

    private String commandName = "";
    private int nparams;

    public String getCommandName() {
        return commandName;
    }

    public int getNparams() {
        return nparams;
    }

    @Override
    public String toString() {
        return commandName + " (params: " + nparams + ")";
    }
}
