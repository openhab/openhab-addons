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
package org.openhab.io.openhabcloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This interface provides callbacks from CloudClient
 *
 * @author Victor Belov - Initial contribution
 * @author Kai Kreuzer - migrated code to ESH APIs
 *
 */
@NonNullByDefault
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
