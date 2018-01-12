/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model.command;

import com.google.gson.annotations.SerializedName;

/**
 * SettingComand wrapper for valid Gardena JSON serialization.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SettingCommandWrapper {
    @SerializedName("settings")
    private SettingCommand command;

    public SettingCommandWrapper(SettingCommand command) {
        this.command = command;
    }

    /**
     * Returns the setting command.
     */
    public SettingCommand getCommand() {
        return command;
    }

}
