/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.powermax.internal.message;

import java.util.EventObject;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event for messages received from the Visonic alarm panel
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxMessageEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private final PowermaxBaseMessage message;

    public PowermaxMessageEvent(Object source, PowermaxBaseMessage message) {
        super(source);
        this.message = message;
    }

    /**
     * @return the message object built from the received message
     */
    public PowermaxBaseMessage getMessage() {
        return message;
    }
}
