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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;

/**
 * The play controls protocol interface
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Spotify support
 */

public interface InputWithPlayControl extends IStateUpdatable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    Set<String> SUPPORTED_INPUTS = Stream
            .of(YamahaReceiverBindingConstants.INPUT_TUNER, "NET_RADIO", "NET RADIO", "USB", "DOCK", "iPOD_USB", "PC",
                    "Napster", "Pandora", "SIRIUS", "Rhapsody", YamahaReceiverBindingConstants.INPUT_BLUETOOTH, "iPod",
                    "HD_RADIO", YamahaReceiverBindingConstants.INPUT_SPOTIFY)
            .collect(Collectors.toSet());

    /**
     * Start the playback of the content which is usually selected by the means of the Navigation control class or
     * which has been stopped by stop().
     *
     * @throws Exception
     */
    void play() throws IOException, ReceivedMessageParseException;

    /**
     * Stop the currently playing content. Use start() to start again.
     *
     * @throws Exception
     */
    void stop() throws IOException, ReceivedMessageParseException;

    /**
     * Pause the currently playing content. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    void pause() throws IOException, ReceivedMessageParseException;

    /**
     * Skip forward. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    void skipFF() throws IOException, ReceivedMessageParseException;

    /**
     * Skip reverse. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    void skipREV() throws IOException, ReceivedMessageParseException;

    /**
     * Next track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    void nextTrack() throws IOException, ReceivedMessageParseException;

    /**
     * Previous track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    void previousTrack() throws IOException, ReceivedMessageParseException;
}
