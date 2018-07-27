/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.picotts.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.voice.Voice;

/**
 * Implementation of {@link AudioStream} for {@link PicoTTSService}
 *
 * @author Florian Schmidt - Initial Contribution
 */
class PicoTTSAudioStream extends FixedLengthAudioStream {
    private final Voice voice;
    private final String text;
    private final AudioFormat audioFormat;
    private final InputStream inputStream;

    private long length;
    private File file;

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
            file = new File(outputFile);
            this.length = file.length();
            return getFileInputStream(file);
        } catch (IOException e) {
            throw new AudioException("Error while executing '" + command + "'", e);
        } catch (InterruptedException e) {
            throw new AudioException("The '" + command + "' has been interrupted", e);
        }
    }

    private InputStream getFileInputStream(File file) throws AudioException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
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
            File tempFile = File.createTempFile(Integer.toString(text.hashCode()), ".wav");
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
        if (file != null) {
            return getFileInputStream(file);
        } else {
            throw new AudioException("No temporary audio file available.");
        }
    }
}
