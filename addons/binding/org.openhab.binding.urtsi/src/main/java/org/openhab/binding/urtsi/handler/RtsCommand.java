/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.urtsi.handler;

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
