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
package org.openhab.binding.urtsi.internal.handler;

/**
 * The {@code RtsCommand} provides the available commands due to Somfy's RTS protocol.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public enum RtsCommand {
    UP("U"),
    DOWN("D"),
    STOP("S");

    private String actionKey;

    private RtsCommand(String actionKey) {
        this.actionKey = actionKey;
    }

    /**
     * Returns the action key which is used for communicating with the URTSI II device.
     *
     * @return the action key
     */
    public String getActionKey() {
        return actionKey;
    }
}
