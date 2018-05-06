/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.voicerss.internal;

import java.io.File;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FileAudioStream;

/**
 * Implementation of the {@link AudioStream} interface for the
 * {@link VoiceRSSTTSService}. It simply uses a {@link FileAudioStream} which is
 * doing all the necessary work, e.g. supporting MP3 and WAV files with fixed
 * stream length.
 *
 * @author Jochen Hiller - Initial contribution and API
 */
class VoiceRSSAudioStream extends FileAudioStream {

    public VoiceRSSAudioStream(File audioFile, AudioFormat format) throws AudioException {
        super(audioFile, format);
    }

}
