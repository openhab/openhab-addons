/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.lifx.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightCommunicationHandler;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;

/**
 * The {@link LifxResponsePacketListener} is notified when the {@link LifxLightCommunicationHandler} receives a response
 * packet.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public interface LifxResponsePacketListener {

    /**
     * Called when the {@link LifxLightCommunicationHandler} receives a response packet.
     *
     * @param packet the received packet
     */
    public void handleResponsePacket(Packet packet);
}
