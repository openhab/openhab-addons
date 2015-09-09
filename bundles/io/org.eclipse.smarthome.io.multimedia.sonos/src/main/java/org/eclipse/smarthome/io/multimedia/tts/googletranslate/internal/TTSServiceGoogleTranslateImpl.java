package org.eclipse.smarthome.io.multimedia.tts.googletranslate.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.smarthome.io.multimedia.tts.googletranslate.TextToSpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a TTS service using Google Translate service.
 *
 * A sample call to use Google translate is e.g.:
 *
 * <pre>
 * curl -o hello.mp3 -H "user-agent: XXX" "https://translate.google.com/translate_tts?tl=en&q=Hello"
 * </pre>
 *
 * The user-agent must be set, otherwise Google will deny the call.
 *
 * @see <a
 *      href="https://cloud.google.com/translate/">https://cloud.google.com/translate/</a>
 *
 * @author Jochen Hiller - Initial contribution
 * @author Karel Goderis - Fix google URL to bypass google Captchas
 */
public class TTSServiceGoogleTranslateImpl implements TextToSpeechService {

    private static final Logger logger = LoggerFactory.getLogger(TTSServiceGoogleTranslateImpl.class);

    /**
     * Base URL from Google translate. We will use http protocol (non secure),
     * as no critical data will be transferred.
     */
    private final String BASE_URL = "http://translate.google.com/translate_tts";

    @Override
    public String[] supportedLanguages() {
        return new String[] { LANGUAGE_ENGLISH, LANGUAGE_GERMAN, LANGUAGE_DUTCH };
    }

    @Override
    public InputStream textToSpeech(final String text, final String language) throws IOException {
        final File tmpFile = File.createTempFile("esh-tts-googletranslate", "mp3");
        tmpFile.deleteOnExit();
        internalTextToSpeech(text, language, tmpFile);
        final InputStream is = new FileInputStream(tmpFile);
        return is;
    }

    @Override
    public void textToSpeech(final String text, final String language, final File mp3) throws IOException {
        internalTextToSpeech(text, language, mp3);
    }

    // private methods

    /**
     * Call Google translate service. The text has to be encoded when containing
     * special characters.
     */
    private void internalTextToSpeech(final String text, final String language, final File mp3) throws IOException {
        final String url = BASE_URL + "?" + "tl=" + language + "&q=" + URLEncoder.encode(text, "UTF-8")
                + "&client=openhab";
        makeHttpCall(url, mp3);
    }

    @Override
    public boolean serviceIsAvailable() {
        try {
            String text = "test";
            final File tmpFile = File.createTempFile("esh-tts-googletranslate", "mp3");
            tmpFile.deleteOnExit();
            final String url = BASE_URL + "?" + "tl=" + "en" + "&q=" + URLEncoder.encode(text, "UTF-8")
                    + "&client=openhab";
            makeHttpCall(url, tmpFile);
            return true;
        } catch (IOException ex) {
            // ignore error
            return false;
        }
    }

    /**
     * Low level call to an HTTP service.
     */
    private void makeHttpCall(final String url, final File outputFile) throws MalformedURLException, IOException {
        logger.trace("call '{}' >{}", url, outputFile.getPath());
        final long startTime = System.currentTimeMillis();

        final URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("user-agent", "EclipseSmartHome/1.0");

        final int rc = ((HttpURLConnection) connection).getResponseCode();
        if (rc == HttpServletResponse.SC_OK) {
            final InputStream is = connection.getInputStream();
            final OutputStream os = new FileOutputStream(outputFile);
            copyStream(is, os);
            is.close();
            os.close();
            final long endTime = System.currentTimeMillis();
            logger.trace("call took {} ms for {} bytes.", (endTime - startTime), outputFile.length());
        } else {
            throw new IOException("call to '" + url + "' failed: ResponseCode=" + rc);
        }
    }

    private void copyStream(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        final byte[] bytes = new byte[4096];
        int read = inputStream.read(bytes, 0, bytes.length);
        while (read > 0) {
            outputStream.write(bytes, 0, read);
            read = inputStream.read(bytes, 0, bytes.length);
        }
    }

}
