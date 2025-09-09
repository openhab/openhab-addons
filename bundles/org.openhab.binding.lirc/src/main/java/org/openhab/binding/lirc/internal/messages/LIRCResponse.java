/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lirc.internal.messages;

/**
 * Represents a response received from the LIRC server
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCResponse {

    private final String command;
    private final boolean success;
    private final String[] data;

    public LIRCResponse(String command, boolean success, String[] data) {
        this.command = command;
        this.success = success;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public boolean isSuccess() {
        return success;
    }

    public String[] getData() {
        return data;
    }
}
