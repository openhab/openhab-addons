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
import org.openhab.binding.zwavejs.internal.api.dto.messages.BaseMessage;
import org.openhab.binding.zwavejs.internal.discovery.NodeDiscoveryService;

/**
 * Implementations of this {@link ZwaveEventListener} interface can be registered with the {@link ZWaveJSClient}
 * to receive notifications about various Z-Wave events such as node updates, value changes,
 * and other Z-Wave network events.
 * 
 * 
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public interface ZwaveEventListener {

    /*
     * Register {@link NodeDiscoveryService} to bridge handler
     *
     * @param listener the discovery service
     * 
     * @return {@code true} if the new discovery service is accepted
     */
    boolean registerDiscoveryListener(NodeDiscoveryService listener);

    /*
     * Unregister {@link NodeDiscoveryService} from bridge handler
     *
     * @return {@code true} if the discovery service was removed
     */
    boolean unregisterDiscoveryListener();

    /*
     * Registers a listener for node events.
     *
     * @param nodeListener the listener to be registered
     * 
     */
    void registerNodeListener(ZwaveNodeListener nodeListener);

    /*
     * Unregisters a previously registered node listener.
     *
     * @param nodeListener the node listener to unregister
     * 
     * @return true if the listener was successfully unregistered, false otherwise
     */
    boolean unregisterNodeListener(ZwaveNodeListener nodeListener);

    /*
     * Handles an event when a message is received.
     *
     * @param message the message that was received
     */
    void onEvent(BaseMessage message);

    /*
     * This method is called when there is a connection error.
     *
     * @param message the error message describing the connection issue
     */
    void onConnectionError(String message);
}
