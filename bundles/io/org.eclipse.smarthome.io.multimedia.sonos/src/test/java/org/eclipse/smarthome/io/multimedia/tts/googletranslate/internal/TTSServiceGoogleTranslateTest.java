package org.eclipse.smarthome.io.multimedia.tts.googletranslate.internal;

import java.io.IOException;

import org.junit.Test;

public class TTSServiceGoogleTranslateTest {

	@Test
	public void testSayHelloEN() throws IOException {
		TTSServiceGoogleTranslate service = new TTSServiceGoogleTranslate();
		service.say("Hello", "en", null);
	}

	@Test
	public void testSayWindowIsOpenEN() throws IOException {
		TTSServiceGoogleTranslate service = new TTSServiceGoogleTranslate();
		service.say("Window in bedroom is open", "en", null);
	}

	@Test
	public void testSayWindowIsOpenDE() throws IOException {
		TTSServiceGoogleTranslate service = new TTSServiceGoogleTranslate();
		service.say("Das Fenster im Schlafzimmer ist noch offen", "de", null);
	}

}
