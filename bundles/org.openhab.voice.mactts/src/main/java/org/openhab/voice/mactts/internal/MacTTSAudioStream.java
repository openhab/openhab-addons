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
package org.openhab.voice.mactts.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.voice.Voice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link AudioStream} interface for the {@link MacTTSService}
 *
 * @author Kelly Davis - Initial contribution and API
 * @author Kai Kreuzer - Refactored to use AudioStream and fixed audio format to produce
 */
class MacTTSAudioStream extends FixedLengthAudioStream {

    private final Logger logger = LoggerFactory.getLogger(MacTTSAudioStream.class);

    /**
     * {@link Voice} this {@link AudioStream} speaks in
     */
    private final Voice voice;

    /**
     * Text spoken in this {@link AudioStream}
     */
    private final String text;

    /**
     * {@link AudioFormat} of this {@link AudioStream}
     */
    private final AudioFormat audioFormat;

    /**
     * The raw input stream
     */
    private InputStream inputStream;

    private long length;
    private File file;

    /**
     * Constructs an instance with the passed properties.
     *
     * It is assumed that the passed properties have been validated.
     *
     * @param text The text spoken in this {@link AudioStream}
     * @param voice The {@link Voice} used to speak this instance's text
     * @param audioFormat The {@link AudioFormat} of this {@link AudioStream}
     * @throws AudioException if stream cannot be created
     */
    public MacTTSAudioStream(String text, Voice voice, AudioFormat audioFormat) throws AudioException {
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
        String command = getCommand(outputFile);
        logger.debug("Executing on command line: {}", command);

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            file = new File(outputFile);
            if (!file.exists()) {
                throw new AudioException("Generated file '" + outputFile + "' does not exist.'");
            }
            this.length = file.length();
            if (this.length == 0) {
                throw new AudioException("Generated file '" + outputFile + "' has no content.'");
            }
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
        File tempFile;
        try {
            tempFile = File.createTempFile(Integer.toString(text.hashCode()), ".wav");
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new AudioException("Unable to create temp file.", e);
        }
        return tempFile.getAbsolutePath();
    }

    /**
     * Gets the command used to generate an audio file {@code outputFile}
     *
     * @param outputFile The absolute filename of the command's output
     * @return The command used to generate the audio file {@code outputFile}
     */
    private String getCommand(String outputFile) {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("say");

        stringBuffer.append(" --voice=" + this.voice.getLabel());
        stringBuffer.append(" --output-file=" + outputFile);
        stringBuffer.append(" --file-format=" + this.audioFormat.getContainer());
        stringBuffer.append(" --data-format=LEI" + audioFormat.getBitDepth() + "@" + audioFormat.getFrequency());
        stringBuffer.append(" --channels=1"); // Mono
        stringBuffer.append(" " + this.text);

        return stringBuffer.toString();
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
