package org.eclipse.smarthome.io.multimedia.tts.googletranslate.internal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.TextToSpeechCache;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.TextToSpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TTSCacheImpl implements TextToSpeechCache {

    private static final Logger logger = LoggerFactory.getLogger(TTSCacheImpl.class);

    private final TextToSpeechService translateService = new TTSServiceGoogleTranslateImpl();
    private final File cacheFolder;
    private final File prefillCacheFolder;

    public TTSCacheImpl(final File baseFolder) {
        this.cacheFolder = baseFolder;
        this.prefillCacheFolder = new File(baseFolder, "prefill");
        if (!this.prefillCacheFolder.exists()) {
            this.prefillCacheFolder.mkdirs();
        }
    }

    @Override
    public String[] supportedLanguages() {
        return translateService.supportedLanguages();
    }

    /**
     * Get a unique name for a text in given language.
     * 
     * Will create a MD5 hash for the text. The language will be used as prefix
     * for this name.
     */
    @Override
    public String getUniqueName(String text, String language) {
        try {
            final byte[] bytesOfMessage = text.getBytes("UTF-8");
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] md5Hash = md.digest(bytesOfMessage);
            final BigInteger bigInt = new BigInteger(1, md5Hash);
            String hashtext = bigInt.toString(16);
            // Now we need to zero pad it if you actually want the full 32
            // chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            final String uniqueName = language + "-" + hashtext;
            return uniqueName;
        } catch (UnsupportedEncodingException ex) {
            // should not happen, some misconfiguration?
            logger.error("Could not create MD5 hash for '" + text + "'", ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {
            // should not happen, some misconfiguration?
            logger.error("Could not create MD5 hash for '" + text + "'", ex);
            return null;
        }
    }

    @Override
    public File textToSpeech(final String text, final String language) throws IOException {
        final String fileName = getUniqueName(text, language);
        if (fileName == null) {
            return null;
        }
        try {
            // check prefilled cache, otherwise use normal cache
            File mp3 = new File(this.prefillCacheFolder, fileName + ".mp3");
            if (!mp3.exists()) {
                mp3 = new File(this.cacheFolder, fileName + ".mp3");
                if (!mp3.exists()) {
                    createAudioForText(text, language, this.cacheFolder.getPath() + File.separator + fileName);
                }
            }
            // check of text of mp3 file matches
            // just for consistency check, remove later
            File txtFile = new File(mp3.getPath().replace("mp3", "txt"));
            String textInFile = FileUtils.readFileToString(txtFile, "UTF-8");
            if (!text.equals(textInFile)) {
                logger.error("Uups, text in file '{}' does not match expected text '{}'", textInFile, text);
            }
            return mp3;
        } catch (IOException ex) {
            logger.error("Could not create audio file for text '" + text + "'", ex);
        }
        return null;
    }

    @Override
    public File textToSpeech(String uniqueName) throws IOException {
        File f = new File(prefillCacheFolder + File.separator + uniqueName);
        if (f.exists()) {
            return f;
        } else {
            f = new File(cacheFolder + File.separator + uniqueName);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

    @Override
    public void clearCache() {
        logger.info("clearCache");
        for (final File file : this.cacheFolder.listFiles()) {
            if (file.getName().endsWith(".mp3") && (!file.isDirectory())) {
                logger.trace("clearCache: delete file {}", file.getName());
                file.delete();
                // remove txt file too if existing
                final String fileName = file.getPath();
                final File txtFile = new File(fileName.substring(0, fileName.length() - 3) + "txt");
                if (txtFile.exists()) {
                    logger.trace("clearCache: delete file {}", txtFile.getName());
                    txtFile.delete();
                }
            }
        }
    }

    @Override
    public void fillCache(List<String> listOfText, String language) {
        logger.info("fillCache");
        if ((listOfText == null) || (listOfText.size() == 0)) {
            return;
        }
        if (!translateService.serviceIsAvailable()) {
            logger.warn("Could not prefill cache, Google Translate Service not available");
            return;
        }
        for (Iterator<String> iter = listOfText.iterator(); iter.hasNext();) {
            String text = iter.next();

            String uniqueName = getUniqueName(text, language);

            // store in prefill cache
            String baseFileName = this.prefillCacheFolder.getPath() + File.separator + uniqueName;
            logger.info("fillCache: '{}' ({}) ==> {}", text, language, baseFileName);
            try {
                createAudioForText(text, language, baseFileName);
            } catch (IOException ex) {
                logger.error("Could not create audio file for text '" + text + "'", ex);
            }
        }
    }

    // internal methods

    /**
     * Create an mp3 file for given text and language using the Google translate
     * service.
     */
    protected void createAudioForText(final String text, final String language, final String baseFileName)
            throws IOException {
        final File mp3 = new File(baseFileName + ".mp3");
        this.translateService.textToSpeech(text, language, mp3);
        // write text to file for transparency too
        final File txtFile = new File(baseFileName + ".txt");
        FileUtils.writeStringToFile(txtFile, text, "UTF-8");
    }
}
