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
package org.openhab.binding.powermax.internal.state;

import java.util.EventObject;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event for state received from the Visonic alarm panel
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxStateEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private final PowermaxState state;

    public PowermaxStateEvent(Object source, PowermaxState state) {
        super(source);
        this.state = state;
    }

    /**
     * @return the state object built from the received message
     */
    public PowermaxState getState() {
        return state;
    }
}
