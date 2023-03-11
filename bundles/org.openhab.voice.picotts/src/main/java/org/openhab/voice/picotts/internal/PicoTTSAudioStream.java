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
package org.openhab.voice.picotts.internal;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;

/**
 * Implementation of {@link AudioStream} for {@link PicoTTSService}
 *
 * @author Florian Schmidt - Initial Contribution
 * @author Laurent Garnier - Extends FileAudioStream + new method to delete the file
 */
@NonNullByDefault
class PicoTTSAudioStream extends FileAudioStream {
    private File file;

    public PicoTTSAudioStream(File file, AudioFormat audioFormat) throws AudioException {
        super(file, audioFormat);
        this.file = file;
    }

    public void deleteFile() {
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
