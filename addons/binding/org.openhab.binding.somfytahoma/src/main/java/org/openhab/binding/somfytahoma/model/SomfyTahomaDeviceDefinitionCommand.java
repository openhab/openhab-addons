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
 * The {@link SomfyTahomaDeviceDefinitionCommand} holds information about commands
 * accepted by a device.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaDeviceDefinitionCommand {

    private String commandName;
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
