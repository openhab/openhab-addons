/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ekey.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;

/**
 * The {@link EkeyPacketListener} is in interface for a Ekey packet received consumer
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public interface EkeyPacketListener {
    /**
     * This method will be called in case a message was received.
     *
     */
    void messageReceived(byte[] message);

    /**
     * This method will be called in case the connection status has changed.
     *
     */
    void connectionStatusChanged(ThingStatus status, byte @Nullable [] message);
}
