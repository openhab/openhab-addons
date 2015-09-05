package org.eclipse.smarthome.io.multimedia.tts.googletranslate.internal.tts;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TTSGoogleTranslateImplTest {

	private final static String EN = TTSGoogleTranslate.LANGUAGE_ENGLISH;
	private final static String DE = TTSGoogleTranslate.LANGUAGE_GERMAN;

	private final static File baseFolder = new File("./tts_cache");

	@Test
	public void testHello() throws IOException {
		TTSGoogleTranslate tts = new TTSGoogleTranslateImpl();

		tts.textToSpeech("Hello", EN, new File(baseFolder, "hello-en.mp3"));
		tts.textToSpeech("Hallo", DE, new File(baseFolder, "hello-de.mp3"));
	}

	@Test
	public void testHelloWorld() throws IOException {
		TTSGoogleTranslate tts = new TTSGoogleTranslateImpl();

		tts.textToSpeech("Hello Brave new World", EN, new File(baseFolder,
				"helloworld-en.mp3"));
		tts.textToSpeech("Hallo Schoene neue Welt", DE, new File(baseFolder,
				"helloworld-de.mp3"));
	}
}
