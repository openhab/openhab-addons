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
import java.util.Set;

import com.google.common.collect.Sets;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;

/**
 * The play controls protocol interface
 *
 * @author David Graeff - Initial contribution
 */

public interface InputWithPlayControl extends IStateUpdateable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    public static Set<String> supportedInputs = Sets.newHashSet("TUNER",
            "NET_RADIO", "USB", "DOCK", "iPOD_USB", "PC",
            "Napster", "Pandora", "SIRIUS", "Rhapsody", "Bluetooth", "iPod", "HD_RADIO");

    /**
     * Start the playback of the content which is usually selected by the means of the Navigation control class or
     * which has been stopped by stop().
     *
     * @throws Exception
     */
    public void play() throws IOException, ReceivedMessageParseException;

    /**
     * Stop the currently playing content. Use start() to start again.
     *
     * @throws Exception
     */
    public void stop() throws IOException, ReceivedMessageParseException;

    /**
     * Pause the currently playing content. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void pause() throws IOException, ReceivedMessageParseException;

    /**
     * Skip forward. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void skipFF() throws IOException, ReceivedMessageParseException;

    /**
     * Skip reverse. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void skipREV() throws IOException, ReceivedMessageParseException;

    /**
     * Next track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void nextTrack() throws IOException, ReceivedMessageParseException;

    /**
     * Previous track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void previousTrack() throws IOException, ReceivedMessageParseException;
}
