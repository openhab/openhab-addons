/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.googletts.internal.protocol;

/**
 * Configuration to set up audio encoder.
 *
 * @author Wouter Born - Initial contribution
 */
public enum AudioEncoding {

    /**
     * Not specified.
     */
    AUDIO_ENCODING_UNSPECIFIED,

    /**
     * Uncompressed 16-bit signed little-endian samples (Linear PCM). Audio content returned as LINEAR16 also contains a
     * WAV header.
     */
    LINEAR16,

    /**
     * MP3 audio.
     */
    MP3,

    /**
     * Opus encoded audio wrapped in an ogg container. The result will be a file which can be played natively on
     * Android, and in browsers (at least Chrome and Firefox). The quality of the encoding is considerably higher than
     * MP3 while using approximately the same bitrate.
     */
    OGG_OPUS;
}
