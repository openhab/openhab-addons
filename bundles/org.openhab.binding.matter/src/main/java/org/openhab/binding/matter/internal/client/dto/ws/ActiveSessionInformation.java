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
package org.openhab.binding.matter.internal.client.dto.ws;

import java.math.BigInteger;

/**
 * ActiveSessionInformation describes an active session with a peer.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ActiveSessionInformation {
    public String name;
    public BigInteger nodeId;
    public String peerNodeId;
    public ExposedFabricInformation fabric;
    public boolean isPeerActive;
    public boolean secure;
    public Long lastInteractionTimestamp; // Use Long for undefined (null) values
    public Long lastActiveTimestamp; // Use Long for undefined (null) values
    public int numberOfActiveSubscriptions;
}
