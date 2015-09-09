package org.eclipse.smarthome.io.multimedia.tts.googletranslate.internal.tts;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TTSCacheImplTest {

	private final static String EN = TTSGoogleTranslate.LANGUAGE_ENGLISH;
	private final static String DE = TTSGoogleTranslate.LANGUAGE_GERMAN;

	private final static File baseFolder = new File("./tts_cache");
	private TTSCache cache;

	@Before
	public void setUp() {
		this.cache = new TTSCacheImpl(baseFolder);
	}

	@After
	public void tearDown() {
		this.cache = null;
	}

	@Test
	public void testHello() throws IOException {
		File f1 = cache.getAudioForText("Hello", EN);
		Assert.assertNotNull(f1);
		File f2 = cache.getAudioForText("Hallo", DE);
		Assert.assertNotNull(f2);
	}

	@Test
	public void testClearCache() throws IOException {
		cache.clearCache();
	}

	@Test
	public void testGetAudioForText() throws IOException {
		cache.getAudioForText("Hello", EN);
		cache.getAudioForText("Hello brave new world", EN);
		cache.getAudioForText("Hallo", DE);
		cache.getAudioForText("Hallo schoene neue Welt", DE);
	}

	@Test
	public void testFillCache() throws IOException {
		cache.fillCache(null, EN);
		cache.fillCache(new String[] {}, EN);
		cache.fillCache(new String[] { "Hello", "Hello brave new world" }, EN);
		cache.fillCache(new String[] { "Hallo", "Hallo schoene neue Welt" }, DE);
	}

	@Test
	public void testPreFillCache() throws IOException {
		String template1 = "It is {0} o'clock";
		for (int i = 0; i <= 24; i++) {
			String text = MessageFormat.format(template1, Integer.valueOf(i));
			cache.fillCache(new String[] { text }, EN);
		}
		String template2 = "Es ist {0} Uhr";
		for (int i = 0; i <= 24; i++) {
			String text = MessageFormat.format(template2, Integer.valueOf(i));
			cache.fillCache(new String[] { text }, DE);
		}
	}
}
