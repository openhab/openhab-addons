/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.IOException;

/**
 * The system control protocol interface. This is basically just power.
 *
 * @author David Graeff - Initial contribution
 */
public interface SystemControl extends IStateUpdatable {
    /**
     * Switches the AVR on/off (off equals network standby here).
     *
     * @param power The new power state
     *
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void setPower(boolean power) throws IOException, ReceivedMessageParseException;

    /**
     * Enables party mode.
     *
     * @param on
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void setPartyMode(boolean on) throws IOException, ReceivedMessageParseException;

    /**
     * Enables mute for party mode.
     *
     * @param on
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void setPartyModeMute(boolean on) throws IOException, ReceivedMessageParseException;

    /**
     * Increment or decrement the volume for party mode.
     *
     * @param increment
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void setPartyModeVolume(boolean increment) throws IOException, ReceivedMessageParseException;

}
