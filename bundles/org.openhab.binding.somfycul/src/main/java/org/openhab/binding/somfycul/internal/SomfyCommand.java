/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.somfycul.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@code SomfyCommand} provides the available commands due to Somfy's RTS protocol.
 *
 * http://culfw.de/commandref.html#cmd_Y
 *
 * @author Marc Klasser - Initial contribution
 */
@NonNullByDefault
public enum SomfyCommand {
    MY("1"),
    UP("2"),
    DOWN("4"),
    PROG("8");

    private String actionKey;

    private SomfyCommand(String actionKey) {
        this.actionKey = actionKey;
    }

    /**
     * Returns the action key which is used for communicating with the CUL device.
     *
     * @return the action key
     */
    public String getActionKey() {
        return actionKey;
    }
}
