/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.snmp.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.snmp4j.CommandResponder;
import org.snmp4j.PDU;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;

/**
 * The {@link SnmpService} is responsible for SNMP communication
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public interface SnmpService {

    /**
     * Add a listener for received PDUs to the service
     *
     * @param listener the listener
     */
    void addCommandResponder(CommandResponder listener);

    /**
     * Remove a listener for received PDUs from the service
     *
     * @param listener the listener
     */
    void removeCommandResponder(CommandResponder listener);

    /**
     * Send a PDU to the given target
     *
     * @param pdu the PDU
     * @param target the target
     * @param userHandle an optional user-handle to identify the request
     * @param listener the listener for the response (always called, even in case of timeout)
     * @throws IOException when an error occurs
     */
    void send(PDU pdu, Target<?> target, @Nullable Object userHandle, ResponseListener listener) throws IOException;

    /**
     * Add a user to the service for a given engine id (v3 only)
     *
     * @param user the {@link UsmUser} that should be added
     * @param engineId the engine id
     */
    void addUser(UsmUser user, OctetString engineId);

    /**
     * Remove a user from the service and clear the context engine id for this address (v3 only)
     *
     * @param address the remote address
     * @param user the user
     * @param engineId the engine id
     */
    void removeUser(Address address, UsmUser user, OctetString engineId);

    /**
     * Get the engine id of a remote system for a given address (v3 only)
     *
     * @param address the address of the remote system
     * @return the engine id or {@code null} when engine id could not be determined
     */
    byte @Nullable [] getEngineId(Address address);
}
