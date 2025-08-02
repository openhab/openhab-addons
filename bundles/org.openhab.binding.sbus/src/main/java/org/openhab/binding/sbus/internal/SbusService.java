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
package org.openhab.binding.sbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import ro.ciprianpascu.sbus.msg.SbusRequest;
import ro.ciprianpascu.sbus.msg.SbusResponse;
import ro.ciprianpascu.sbus.net.SbusMessageListener;

/**
 * The {@link SbusService} defines a minimal facade interface that mirrors SbusAdapter.
 * This provides a clean abstraction layer for OpenHAB handlers while keeping the
 * core transaction logic in the j2sbus library.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public interface SbusService {

    /**
     * Executes a synchronous SBUS transaction (request/response).
     * This is the core method for all SBUS communication.
     *
     * @param request The SBUS request to send
     * @return The SBUS response received
     * @throws Exception If the transaction fails or times out
     */
    SbusResponse executeTransaction(SbusRequest request) throws Exception;

    /**
     * Adds a message listener for unsolicited SBUS messages.
     * The listener will be notified when messages arrive that are not
     * part of a synchronous request/response transaction.
     *
     * @param listener the listener to add for unsolicited messages
     * @throws Exception if the connection is not established
     */
    void addMessageListener(SbusMessageListener listener) throws Exception;

    /**
     * Removes a previously registered message listener.
     *
     * @param listener the listener to remove
     * @throws Exception if the connection is not established
     */
    void removeMessageListener(SbusMessageListener listener);

    /**
     * Initializes the service with connection parameters.
     *
     * @param host the host address of the Sbus device
     * @param port the port number to use
     * @throws Exception if initialization fails
     */
    void initialize(String host, int port) throws Exception;

    /**
     * Closes the service and releases resources.
     */
    void close();
}
