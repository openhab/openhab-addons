/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.marytts.internal;
/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;

/**
 * Implementation of the {@link AudioSource} interface for the {@link MaryTTSService}
 *
 * @author Kelly Davis - Initial contribution and API
 * @author Kai Kreuzer - Refactored to updated APIs and moved to openHAB
 */
class MaryTTSAudioStream extends AudioStream {

    /**
     * {@link AudioFormat} of this {@link AudioSource}
     */
    private final AudioFormat audioFormat;

    /**
     * {@link InputStream} of this {@link AudioSource}
     */
    private final AudioInputStream inputStream;

    /**
     * Constructs an instance with the passed properties
     *
     * @param inputStream The InputStream of this instance
     * @param audioFormat The AudioFormat of this instance
     */
    public MaryTTSAudioStream(AudioInputStream inputStream, AudioFormat audioFormat) {
        this.inputStream = inputStream;
        this.audioFormat = audioFormat;
    }

    @Override
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }
}
