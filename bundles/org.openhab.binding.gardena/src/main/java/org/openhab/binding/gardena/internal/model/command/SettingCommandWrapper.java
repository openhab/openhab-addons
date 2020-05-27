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
