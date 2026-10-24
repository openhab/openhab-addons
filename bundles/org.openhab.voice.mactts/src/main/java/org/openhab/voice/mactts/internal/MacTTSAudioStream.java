/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.common.Disposable;
import org.openhab.core.voice.tts.Voice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link AudioStream} interface for the {@link MacTTSService}
 *
 * @author Kelly Davis - Initial contribution and API
 * @author Kai Kreuzer - Refactored to use AudioStream and fixed audio format to produce
 * @author Laurent Garnier - Add dispose method to delete the temporary file
 */
class MacTTSAudioStream extends FixedLengthAudioStream implements Disposable {

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

    /**
     * Timeout for the 'say' process before it is considered hung and destroyed.
     */
    private static final int SYNTHESIS_TIMEOUT_SECONDS = 60;

    /**
     * Grace period allowed for the process to terminate after being destroyed.
     */
    private static final int GRACE_PERIOD_SECONDS = 5;

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    private InputStream createInputStream() throws AudioException {
        String outputFile = generateOutputFilename();
        String[] command = getCommand(outputFile);
        logger.debug("Executing on command line: {}", (Object) command);

        try {
            Process process = new ProcessBuilder(command).redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD).start();
            int exitCode = runWithTimeout(process, SYNTHESIS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (exitCode != 0) {
                throw new AudioException("The 'say' command exited with non-zero code " + exitCode
                        + " while generating '" + outputFile + "'");
            }
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
            Thread.currentThread().interrupt();
            throw new AudioException("The '" + command + "' has been interrupted", e);
        }
    }

    /**
     * Waits for the given process to terminate and returns its exit code.
     *
     * If the process does not terminate within the given timeout, it is destroyed (forcibly, if necessary) and an
     * {@link AudioException} is thrown. This protects against hung 'say' processes blocking synthesis forever.
     *
     * If the current thread is interrupted while waiting, the process is forcibly destroyed before the interruption is
     * propagated, so no hung process is left behind.
     *
     * @param process the process to wait for
     * @param timeout the maximum time to wait for the process
     * @param unit the time unit of the timeout argument
     * @return the exit code of the terminated process
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws AudioException if the process did not terminate within the timeout
     */
    static int runWithTimeout(Process process, long timeout, TimeUnit unit)
            throws InterruptedException, AudioException {
        try {
            if (process.waitFor(timeout, unit)) {
                return process.exitValue();
            }
            process.destroy();
            if (!process.waitFor(GRACE_PERIOD_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(GRACE_PERIOD_SECONDS, TimeUnit.SECONDS);
            }
            throw new AudioException("The 'say' process did not finish within " + timeout + " " + unit);
        } catch (InterruptedException e) {
            process.destroyForcibly();
            throw e;
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
            tempFile = Files.createTempFile(Integer.toString(text.hashCode()), ".wav").toFile();
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
    private String[] getCommand(String outputFile) {
        return new String[] { //
                "say", //
                "--voice=" + this.voice.getLabel(), //
                "--output-file=" + outputFile, //
                "--file-format=" + this.audioFormat.getContainer(), //
                "--data-format=LEI" + audioFormat.getBitDepth() + "@" + audioFormat.getFrequency(), //
                "--channels=1", //
                this.text };
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

    @Override
    public void dispose() throws IOException {
        if (file != null && file.exists()) {
            try {
                if (!file.delete()) {
                    logger.warn("Failed to delete the file {}", file.getAbsolutePath());
                }
            } catch (SecurityException e) {
                logger.warn("Failed to delete the file {}: {}", file.getAbsolutePath(), e.getMessage());
            }
        }
    }
}
