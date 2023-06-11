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
package org.openhab.binding.monopriceaudio.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.monopriceaudio.internal.MonopriceAudioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create a default MonopriceAudioConnector before initialization is complete.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the MonopriceAudio binding
 */
@NonNullByDefault
public class MonopriceAudioDefaultConnector extends MonopriceAudioConnector {

    private final Logger logger = LoggerFactory.getLogger(MonopriceAudioDefaultConnector.class);

    @Override
    public void open() throws MonopriceAudioException {
        logger.warn(
                "MonopriceAudio binding incorrectly configured. Please configure for Serial or IP over serial connection");
        setConnected(false);
    }

    @Override
    public void close() {
        setConnected(false);
    }

    @Override
    public void sendCommand(MonopriceAudioZone zone, MonopriceAudioCommand cmd, @Nullable Integer value) {
        logger.warn(
                "MonopriceAudio binding incorrectly configured. Please configure for Serial or IP over serial connection");
        setConnected(false);
    }
}
