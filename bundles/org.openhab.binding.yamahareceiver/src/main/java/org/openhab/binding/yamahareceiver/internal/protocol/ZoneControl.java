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

    void setDialogueLevel(int level) throws IOException, ReceivedMessageParseException;

    /**
     * Switches the HDMI1 output on or off.
     *
     * @param on The new state
     *
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void setHDMI1Out(boolean on) throws IOException, ReceivedMessageParseException;

    /**
     * Switches the HDMI2 output on or off.
     *
     * @param on The new state
     *
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void setHDMI2Out(boolean on) throws IOException, ReceivedMessageParseException;

    /**
     * Sets the active scene for the zone.
     *
     * @param scene
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void setScene(String scene) throws IOException, ReceivedMessageParseException;
}
