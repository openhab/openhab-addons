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
package org.openhab.binding.ventaair.internal.message.dto;

import org.openhab.binding.ventaair.internal.message.action.Action;

import com.google.gson.annotations.SerializedName;

/**
 * Message containing a command to be send to the device
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class CommandMessage extends Message {
    @SerializedName(value = "Action")
    private Action action;

    public CommandMessage(Action action, Header header) {
        super(header);
        this.action = action;
    }
}
