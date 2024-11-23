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
package org.openhab.binding.mycroft.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;

/**
 * Informs about received messages
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public interface MycroftMessageListener<T extends BaseMessage> {
    /**
     * A new message was received
     *
     * @param message The received message
     */
    void messageReceived(T message);

    @SuppressWarnings("unchecked")
    default void baseMessageReceived(BaseMessage baseMessage) {
        try {
            messageReceived(((T) baseMessage));
        } catch (ClassCastException cce) {
            throw new ClassCastException("Incorrect use of message in Mycroft binding");
        }
    }
}
