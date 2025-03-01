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
package org.openhab.binding.senseenergy.internal.api;

import java.net.SocketAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SenseEnergyDatagramListener} Interface for callback when a Sense Energy packet is received
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public interface SenseEnergyDatagramListener {

    /**
     * called when a datagram from the Sense Energy monitor is recevied
     * 
     * @param socketAddress the socket address for the Sense Energy monitor
     */
    void requestReceived(SocketAddress socketAddress);
}
