/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.camel.internal.endpoint;

/**
 * The {@link CamelCallback} defines the callback interface for receiving updates from
 * the Apache Camel routes.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
public interface CamelCallback {
    /**
     * This method receives command for an item from Apache Camel route and should post it
     * into openHAB
     *
     * @param item the {@link String} containing item name
     * @param command the {@link String} containing a command
     */
    public void sendCommand(String item, String command);

    /**
     * This method receives update for an item from Apache Camel route and should post it
     * into openHAB
     *
     * @param item the {@link String} containing item name
     * @param command the {@link String} containing a command
     */
    public void sendStatusUpdate(String item, String statusUpdate);
}
