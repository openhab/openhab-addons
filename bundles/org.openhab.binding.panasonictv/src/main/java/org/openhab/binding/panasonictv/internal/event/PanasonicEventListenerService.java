/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonictv.internal.event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PanasonicEventListenerService} is responsible for
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface PanasonicEventListenerService {
    /**
     * subscribe a listener to the service
     *
     * @param uuid the UUID of the listener
     * @param listener the listener itself
     */
    void subscribeListener(String uuid, PanasonicEventListener listener, String remoteAddress);

    /**
     * unsubscribe a listener from the service
     *
     * @param uuid UUID of the listener
     */
    void unsubscribeListener(String uuid);

    /**
     * class for storing listener information
     */
    class ListenerObject {
        public PanasonicEventListener listener;
        public String remoteAddress;
        public @Nullable String sessionId;

        public ListenerObject(PanasonicEventListener listener, String remoteAddress) {
            this.listener = listener;
            this.remoteAddress = remoteAddress;
        }
    }
}
