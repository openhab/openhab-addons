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
package org.openhab.binding.pioneeravr.internal.protocol.event;

/**
 * A listener which is notified when an AVR is disconnected.
 *
 * @author Antoine Besnard - Initial contribution
 */
public interface AvrDisconnectionListener {

    /**
     * Called when an AVR is disconnected.
     *
     * @param event
     */
    void onDisconnection(AvrDisconnectionEvent event);
}
