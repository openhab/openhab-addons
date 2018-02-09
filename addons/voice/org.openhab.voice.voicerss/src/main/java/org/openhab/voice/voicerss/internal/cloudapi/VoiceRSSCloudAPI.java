/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.voicerss.internal.cloudapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;

/**
 * Interface which represents the functionality needed from the VoiceRSS TTS
 * service.
 *
 * @author Jochen Hiller - Initial contribution
 */
public interface VoiceRSSCloudAPI {

    /**
     * Get all supported locales by the TTS service.
     *
     * @return A set of @{link {@link Locale} supported
     */
    Set<Locale> getAvailableLocales();

    /**
     * Get all supported audio formats by the TTS service. This includes MP3,
     * WAV and more audio formats as used in APIs. About supported audio
     * formats, see {@link AudioFormat}
     *
     * @return A set of all audio formats supported
     */
    Set<String> getAvailableAudioFormats();

    /**
     * Get all supported voices.
     *
     * @return A set of voice names supported
     */
    Set<String> getAvailableVoices();

    /**
     * Get all supported voices for a specified locale.
     *
     * @param locale
     *            the locale to get all voices for
     * @return A set of voice names supported
     */
    Set<String> getAvailableVoices(Locale locale);

    /**
     * Get the given text in specified locale and auido format as input stream.
     *
     * @param apiKey
     *            the API key to use for the cloud service
     * @param text
     *            the text to translate into speech
     * @param locale
     *            the locale to use
     * @param audioFormat
     *            the audio format to use
     * @return an InputStream to the audio data in specified format
     * @throws IOException
     *             will be raised if the audio data can not be retrieved from
     *             cloud service
     */
    InputStream getTextToSpeech(String apiKey, String text, String locale, String audioFormat) throws IOException;
}
