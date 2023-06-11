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
package org.openhab.binding.lirc.internal.messages;

/**
 * Represents a button event that was received from the LIRC server
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCButtonEvent {

    private final String code;
    private final int repeats;
    private final String button;
    private final String remote;

    public LIRCButtonEvent(String remote, String button, int repeats, String code) {
        this.code = code;
        this.repeats = repeats;
        this.button = button;
        this.remote = remote;
    }

    /**
     * Gets the number of times this event was repeated.
     *
     * @return number of repeats
     */
    public int getRepeats() {
        return repeats;
    }

    /**
     * Gets the name of the button that was pressed
     *
     * @return the name of the button
     */
    public String getButton() {
        return button;
    }

    /**
     * Gets the name of the remote that generated this event
     *
     * @return the name of the remote
     */
    public String getRemote() {
        return remote;
    }

    /**
     * Gets the raw hex code of the button pressed
     *
     * @return the hex code
     */
    public String getCode() {
        return code;
    }
}
