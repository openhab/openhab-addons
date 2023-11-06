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
package org.openhab.voice.voicerss.internal;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;

/**
 * Implementation of the {@link AudioStream} interface for the
 * {@link VoiceRSSTTSService}. It simply uses a {@link FileAudioStream} which is
 * doing all the necessary work, e.g. supporting MP3 and WAV files with fixed
 * stream length.
 *
 * @author Jochen Hiller - Initial contribution and API
 */
@NonNullByDefault
class VoiceRSSAudioStream extends FileAudioStream {

    public VoiceRSSAudioStream(File audioFile, AudioFormat format) throws AudioException {
        super(audioFile, format);
    }
}
