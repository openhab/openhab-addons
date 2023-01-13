/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.ws;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;

/**
 * Defines a Websocket notification
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WebSocketResponse {
    private boolean success;
    private String action = "";
    private String event = "";
    private String source = "";

    private @Nullable JsonElement result;

    public boolean isSuccess() {
        return success;
    }

    public String getAction() {
        return action;
    }

    public String getEvent() {
        return source + "_" + event;
    }

    public @Nullable JsonElement getResult() {
        return result;
    }
}
