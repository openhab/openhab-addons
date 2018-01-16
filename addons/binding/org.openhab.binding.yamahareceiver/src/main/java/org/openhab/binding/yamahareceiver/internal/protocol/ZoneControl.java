/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.IOException;

import org.openhab.binding.yamahareceiver.internal.state.ZoneControlState;

/**
 * The zone control protocol interface
 *
 * @author David Graeff - Initial contribution
 */

public interface ZoneControl extends IStateUpdatable {
    /**
     * Switches the zone on/off (off equals network standby here).
     *
     * @param on The new power state
     *
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void setPower(boolean on) throws IOException, ReceivedMessageParseException;

    /**
     * Sets the absolute volume in decibel.
     *
     * @param volume Absolute value in decibel ([-80,+12]).
     * @throws IOException
     */
    void setVolumeDB(float volume) throws IOException, ReceivedMessageParseException;

    /**
     * Sets the volume in percent
     *
     * @param volume
     * @throws IOException
     */
    void setVolume(float volume) throws IOException, ReceivedMessageParseException;

    /**
     * Increase or decrease the volume by the given percentage.
     *
     * @param percent
     * @throws IOException
     */
    void setVolumeRelative(ZoneControlState state, float percent) throws IOException, ReceivedMessageParseException;

    void setMute(boolean mute) throws IOException, ReceivedMessageParseException;

    void setInput(String name) throws IOException, ReceivedMessageParseException;

    void setSurroundProgram(String name) throws IOException, ReceivedMessageParseException;
}
