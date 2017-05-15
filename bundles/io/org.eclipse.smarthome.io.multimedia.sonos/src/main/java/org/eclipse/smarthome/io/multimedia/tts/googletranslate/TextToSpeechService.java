package org.eclipse.smarthome.io.multimedia.tts.googletranslate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for a common TextToSpeech service.
 *
 * @author Jochen Hiller - Initial contribution
 */
public interface TextToSpeechService {

    // language codes

    final static String LANGUAGE_ENGLISH = "en";
    final static String LANGUAGE_GERMAN = "de";
    final static String LANGUAGE_DUTCH = "nl";

    /**
     * Returns the list of supported languages.
     *
     * @return an array of language codes
     */
    String[] supportedLanguages();

    /**
     * Creates an mp3 file for the text in specified language.
     *
     * @param text
     *            the text to translate to speech
     * @param language
     *            the language to use. See supported languages
     * @param mp3
     *            the output file for mp3 format
     * @throws IOException
     *             will be raised for any IO problems with files or Internet
     *             access
     */
    void textToSpeech(String text, String language, File mp3) throws IOException;

    /**
     * Creates an mp3 file for the text in specified language.
     *
     * @param text
     *            the text to translate to speech
     * @param language
     *            the language to use. See supported languages
     * @return an input stream to audio resource
     * @throws IOException
     *             will be raised for any IO problems with files or Internet
     *             access
     */
    InputStream textToSpeech(String text, String language) throws IOException;

    boolean serviceIsAvailable();

}
