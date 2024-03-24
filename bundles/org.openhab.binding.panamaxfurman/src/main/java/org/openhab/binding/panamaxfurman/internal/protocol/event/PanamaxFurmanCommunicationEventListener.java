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
package org.openhab.binding.panamaxfurman.internal.protocol.event;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A listener which is notified when communication events occur with the Power Conditioner
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public interface PanamaxFurmanCommunicationEventListener extends EventListener {
    /**
     * Called when information has been received from the Power Conditioner
     *
     * @param data the data received from the power conditioner
     */
    public void onInformationReceived(String data);

    /**
     * Called when the connection to the Power Conditioner is established or broken
     *
     * @param event details about what happened to the connection
     */
    public void onConnectivityEvent(PanamaxFurmanConnectivityEvent event);
}
