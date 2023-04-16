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
package org.openhab.voice.mimic.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.FileAudioStream;

/**
 * A FileAudioStream that autodelete after it and its clone are closed
 * Useful to not congest temporary directory
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class AutoDeleteFileAudioStream extends FileAudioStream {

    private final File file;
    private final AudioFormat audioFormat;
    private final List<ClonedFileInputStream> clonedAudioStreams = new ArrayList<>(1);
    private boolean isOpen = true;

    public AutoDeleteFileAudioStream(File file, AudioFormat format) throws AudioException {
        super(file, format);
        this.file = file;
        this.audioFormat = format;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.isOpen = false;
        deleteIfPossible();
    }

    protected void deleteIfPossible() {
        boolean aClonedStreamIsOpen = clonedAudioStreams.stream().anyMatch(as -> as.isOpen);
        if (!isOpen && !aClonedStreamIsOpen) {
            file.delete();
        }
    }

    @Override
    public InputStream getClonedStream() throws AudioException {
        ClonedFileInputStream clonedInputStream = new ClonedFileInputStream(this, file, audioFormat);
        clonedAudioStreams.add(clonedInputStream);
        return clonedInputStream;
    }

    private static class ClonedFileInputStream extends FileAudioStream {
        protected boolean isOpen = true;
        private final AutoDeleteFileAudioStream parent;

        public ClonedFileInputStream(AutoDeleteFileAudioStream parent, File file, AudioFormat audioFormat)
                throws AudioException {
            super(file, audioFormat);
            this.parent = parent;
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.isOpen = false;
            parent.deleteIfPossible();
        }
    }
}
