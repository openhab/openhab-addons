/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.cmusphinx.internal;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.STTException;
import org.eclipse.smarthome.core.voice.STTListener;
import org.eclipse.smarthome.core.voice.STTService;
import org.eclipse.smarthome.core.voice.STTServiceHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

/**
 * This is a STT service implementation using CMU Sphinx.
 *
 * @author Yannick Schaus - Initial contribution and API
 *
 */
public class CMUSphinxSTTService implements STTService {
    private Logger logger = LoggerFactory.getLogger(CMUSphinxSTTService.class);
    private Locale locale;

    private Configuration configuration;
    private StreamSpeechRecognizer speechRecognizer;

    @Override
    public String getId() {
        return "cmusphinx";
    }

    @Override
    public String getLabel(Locale locale) {
        return "CMU Sphinx";
    }

    /**
     * Set of supported locales
     */
    private HashSet<Locale> locales = new HashSet<>();

    /**
     * Set of supported audio formats
     */
    private final HashSet<AudioFormat> audioFormats = initAudioFormats();

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Locale> getSupportedLocales() {
        return this.locales;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return this.audioFormats;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public STTServiceHandle recognize(STTListener sttListener, AudioStream audioStream, Locale locale,
            Set<String> grammars) throws STTException {
        // Validate arguments
        if (null == sttListener) {
            throw new IllegalArgumentException("The passed STTListener is null");
        }
        if (null == audioStream) {
            throw new IllegalArgumentException("The passed AudioSource is null");
        }
        boolean isAudioFormatValid = false;
        AudioFormat audioFormat = audioStream.getFormat();
        for (AudioFormat currentAudioFormat : this.audioFormats) {
            if (currentAudioFormat.isCompatible(audioFormat)) {
                isAudioFormatValid = true;
                break;
            }
        }
        if (!isAudioFormatValid) {
            throw new IllegalArgumentException("The passed AudioSource's AudioFormat is unsupported");
        }
        if (null == audioFormat.getBitRate()) {
            throw new IllegalArgumentException("The passed AudioSource's AudioFormat's bit rate is not set");
        }
        if (!this.locale.equals(locale)) {
            throw new IllegalArgumentException("The passed Locale is unsupported");
        }

        // Start recognition
        STTServiceCMUSphinxRunnable sttServiceCMUSphinxRunnable = new STTServiceCMUSphinxRunnable(this.speechRecognizer,
                sttListener, audioStream);
        Thread thread = new Thread(sttServiceCMUSphinxRunnable);
        thread.start();

        // Return STTServiceHandleKaldi
        return new STTServiceHandleCMUSphinx(sttServiceCMUSphinxRunnable);

    }

    /**
     * Initializes this.audioFormats
     *
     * @return The audio formats of this instance
     */
    private final HashSet<AudioFormat> initAudioFormats() {
        HashSet<AudioFormat> audioFormats = new HashSet<AudioFormat>();

        audioFormats.add(new AudioFormat("WAV", "PCM_SIGNED", false, 16, null, 16000L));
        // audioFormats.add(new AudioFormat("WAV", "PCM_SIGNED", false, 16, null, 8000L));

        return audioFormats;
    }

    public void activate(Map<String, Object> properties) {
        modified(properties);
    }

    public void deactivate(Map<String, Object> properties) {
    }

    public void modified(Map<String, Object> properties) {
        if (properties == null) {
            return;
        }

        Object locale = properties.get("locale");
        if (locale == null) {
            logger.error("Please set the locale in settings");
            return;
        }
        this.locale = new Locale((String) locale);
        this.locales.clear();
        this.locales.add(this.locale);

        Object acousticModelPath = properties.get("acousticModelPath");
        if (acousticModelPath == null) {
            logger.error("Please provide an acoustic model");
            return;
        }
        Object dictionaryPath = properties.get("dictionaryPath");
        if (dictionaryPath == null) {
            logger.error("Please provide a dictionary");
            return;
        }
        Object languageModelPath = properties.get("languageModelPath");
        Object grammarPath = properties.get("grammarPath");
        Object grammarName = properties.get("grammarName");
        if (languageModelPath == null && grammarPath == null) {
            logger.error("Please provide either a language model or a grammar path and name");
            return;
        }

        this.configuration = new Configuration();
        configuration.setAcousticModelPath("file:" + (String) acousticModelPath);
        configuration.setDictionaryPath("file:" + (String) dictionaryPath);
        if (languageModelPath != null) {
            configuration.setLanguageModelPath("file:" + (String) languageModelPath);
        } else {
            if (grammarName == null) {
                logger.error("Please provide the grammar name (.gram file without the extension)");
            }
            configuration.setGrammarPath("file:" + (String) grammarPath);
            configuration.setGrammarName((String) grammarName);
            configuration.setUseGrammar(true);
        }

        try {
            this.speechRecognizer = new StreamSpeechRecognizer(configuration);
        } catch (IOException e) {
            logger.error("Error during CMU Sphinx speech recognizer initialization", e);
        }

        logger.info("CMU Sphinx speech recognizer initialized");
    }
}
