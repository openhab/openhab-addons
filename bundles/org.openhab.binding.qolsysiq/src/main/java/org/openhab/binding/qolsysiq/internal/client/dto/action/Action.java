/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.qolsysiq.internal.client.dto.action;

import com.google.gson.annotations.SerializedName;

/**
 * The base type for various action messages sent to a panel
 *
 * @author Dan Cunningham - Initial contribution
 */
public abstract class Action {
    @SerializedName("action")
    public ActionType type;
    public Integer version = 0;
    public String source = "C4";
    public String token;

    public Action(ActionType type) {
        this(type, "");
    }

    public Action(ActionType type, String token) {
        this.type = type;
        this.token = token;
    }
}
