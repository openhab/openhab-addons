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
package org.openhab.binding.zwavejs.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.zwavejs.internal.api.dto.Event;

/**
 * The {@link ZwaveNodeListener} interface defines the methods that must be implemented by any class
 * that wishes to receive notifications about changes to the state of a node, the addition
 * or removal of nodes, and other node-related events.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public interface ZwaveNodeListener {

    /*
     * Retrieves the identifier of the node.
     *
     * @return the identifier of the node as an Integer.
     */
    Integer getId();

    /*
     * This method is called when the state of a node changes.
     *
     * @param event the event that contains information about the state change
     * 
     * @return true if the state change was handled successfully, false otherwise
     */
    boolean onNodeStateChanged(Event event);

    /*
     * This method is called when the node is dead
     *
     * @param event the event that contains information about the status change
     */
    void onNodeDead(Event event);

    /*
     * This method is called when the node is alive
     *
     * @param event the event that contains information about the status change
     */
    void onNodeAlive(Event event);

    /*
     * This method is called when a node is removed from the Z-Wave network.
     *
     * @param event the event that contains information about the removed node
     */
    void onNodeRemoved(Event event);
}
