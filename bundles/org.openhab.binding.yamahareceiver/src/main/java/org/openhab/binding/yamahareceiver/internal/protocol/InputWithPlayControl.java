/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static java.util.stream.Collectors.toSet;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Inputs.*;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The play controls protocol interface
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Spotify support, adding Server to supported preset inputs
 */
public interface InputWithPlayControl extends IStateUpdatable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    Set<String> SUPPORTED_INPUTS = Stream.of(INPUT_NET_RADIO, INPUT_NET_RADIO_LEGACY, INPUT_USB, INPUT_IPOD_USB,
            INPUT_IPOD, INPUT_DOCK, INPUT_PC, INPUT_NAPSTER, INPUT_PANDORA, INPUT_SIRIUS, INPUT_RHAPSODY,
            INPUT_BLUETOOTH, INPUT_SPOTIFY, INPUT_SERVER, INPUT_HD_RADIO).collect(toSet());

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
