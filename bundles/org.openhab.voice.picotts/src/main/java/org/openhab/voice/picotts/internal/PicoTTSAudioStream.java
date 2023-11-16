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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.common.Disposable;
import org.openhab.core.voice.Voice;

/**
 * Implementation of {@link AudioStream} for {@link PicoTTSService}
 *
 * @author Florian Schmidt - Initial Contribution
 */
@NonNullByDefault
class PicoTTSAudioStream extends FixedLengthAudioStream implements Disposable {

    private final Voice voice;
    private final String text;
    private final AudioFormat audioFormat;
    private final InputStream inputStream;

    private long length;
    private @Nullable File file;

    public PicoTTSAudioStream(String text, Voice voice, AudioFormat audioFormat) throws AudioException {
        this.text = text;
        this.voice = voice;
        this.audioFormat = audioFormat;
        this.inputStream = createInputStream();
    }

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    private InputStream createInputStream() throws AudioException {
        String outputFile = generateOutputFilename();
        String[] command = getCommand(outputFile);

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            File file = new File(outputFile);
            this.file = file;
            this.length = file.length();
            return getFileInputStream(file);
        } catch (IOException e) {
            throw new AudioException("Error while executing '" + command + "'", e);
        } catch (InterruptedException e) {
            throw new AudioException("The '" + command + "' has been interrupted", e);
        }
    }

    private InputStream getFileInputStream(File file) throws AudioException {
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new AudioException("Cannot open temporary audio file '" + file.getName() + ".");
            }
        } else {
            throw new AudioException("Temporary file '" + file.getName() + "' not found!");
        }
    }

    /**
     * Generates a unique, absolute output filename
     *
     * @return Unique, absolute output filename
     */
    private String generateOutputFilename() throws AudioException {
        try {
            File tempFile = Files.createTempFile(Integer.toString(text.hashCode()), ".wav").toFile();
            tempFile.deleteOnExit();
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new AudioException("Unable to create temp file.", e);
        }
    }

    /**
     * Gets the command used to generate an audio file {@code outputFile}
     *
     * @param outputFile The absolute filename of the command's output
     * @return The command used to generate the audio file {@code outputFile}
     */
    private String[] getCommand(String outputFile) {
        return new String[] { "pico2wave", "-l=" + this.voice.getLabel(), "-w=" + outputFile, this.text };
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public InputStream getClonedStream() throws AudioException {
        File file = this.file;
        if (file != null) {
            return getFileInputStream(file);
        } else {
            throw new AudioException("No temporary audio file available.");
        }
    }

    @Override
    public void dispose() throws IOException {
        File localFile = file;
        if (localFile != null && localFile.exists()) {
            try {
                if (!localFile.delete()) {
                    throw new IOException("Failed to delete the file " + localFile.getAbsolutePath());
                }
            } catch (SecurityException e) {
                throw new IOException("Failed to delete the file " + localFile.getAbsolutePath(), e);
            }
        }
    }
}
