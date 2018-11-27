/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.openhabcloud.internal;

/**
 * This interface provides callbacks from CloudClient
 *
 * @author Victor Belov - Initial contribution
 * @author Kai Kreuzer - migrated code to ESH APIs
 *
 */

public interface CloudClientListener {
    /**
     * This method receives command for an item from the openHAB Cloud client and should post it
     * into openHAB
     *
     * @param item the {@link String} containing item name
     * @param command the {@link String} containing a command
     */
    public void sendCommand(String item, String command);
}
