package org.eclipse.smarthome.io.multimedia.tts.googletranslate;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface TextToSpeechCache {

	/**
	 * Returns the list of supported languages.
	 * 
	 * @return an array of language codes
	 */
	String[] supportedLanguages();

	/**
	 * Get a unique name for a text in given language.
	 * 
	 * Will create a MD5 hash for the text. The language will be used as prefix
	 * for this name.
	 */
	String getUniqueName(String text, String language);

	/**
	 * Creates an mp3 file for the text in specified language.
	 * 
	 * @param text
	 *            the text to translate to speech
	 * @param language
	 *            the language to use. See supported languages
	 * @return an file to audio resource
	 * @throws IOException
	 *             will be raised for any IO problems with files or Internet
	 *             access
	 */
	File textToSpeech(String text, String language) throws IOException;

	File textToSpeech(String uniqueName) throws IOException;

	void clearCache();

	void fillCache(List<String> listOfText, String language);
}
