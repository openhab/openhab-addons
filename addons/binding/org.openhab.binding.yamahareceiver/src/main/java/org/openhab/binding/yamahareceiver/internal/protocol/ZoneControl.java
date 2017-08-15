/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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

public interface ZoneControl extends IStateUpdateable {
    /**
     * Switches the zone on/off (off equals network standby here).
     *
     * @param power The new power state
     *
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    public void setPower(boolean on) throws IOException, ReceivedMessageParseException;

    /**
     * Sets the absolute volume in decibel.
     *
     * @param volume Absolute value in decibel ([-80,+12]).
     * @throws IOException
     */
    public void setVolumeDB(float volume) throws IOException, ReceivedMessageParseException;

    /**
     * Sets the volume in percent
     *
     * @param volume
     * @throws IOException
     */
    public void setVolume(float volume) throws IOException, ReceivedMessageParseException;

    /**
     * Increase or decrease the volume by the given percentage.
     *
     * @param percent
     * @throws IOException
     */
    public void setVolumeRelative(ZoneControlState state, float percent)
            throws IOException, ReceivedMessageParseException;

    public void setMute(boolean mute) throws IOException, ReceivedMessageParseException;

    public void setInput(String name) throws IOException, ReceivedMessageParseException;

    public void setSurroundProgram(String name) throws IOException, ReceivedMessageParseException;
}
