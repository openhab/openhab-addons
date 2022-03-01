/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.voice.snowboyks.internal;

import static org.openhab.voice.snowboyks.internal.SnowboyKSConstants.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.rest.LocaleService;
import org.openhab.core.voice.KSException;
import org.openhab.core.voice.KSListener;
import org.openhab.core.voice.KSService;
import org.openhab.core.voice.KSServiceHandle;
import org.openhab.core.voice.KSpottedEvent;
import org.openhab.voice.snowboyks.internal.generated.SnowboyDetect;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SnowboyKSService} is a keyword spotting implementation based on Snowboy.
 * sent to one of the channels.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME, description_uri = SERVICE_CATEGORY + ":"
        + SERVICE_ID)

public class SnowboyKSService implements KSService {
    private static final String SNOWBOY_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "snowboy").toString();
    private static final String EXTRACTION_FOLDER = Path.of(SNOWBOY_FOLDER, "extracted").toString();
    private static final Set<String> INCLUDED_KEYWORDS = Set.of("computer", "jarvis", "snowboy", "smart_mirror");
    private static final String WAKE_WORD_EXT = ".umdl";
    private static final String WAKE_WORD_ALT_EXT = ".pmdl";
    static {
        Logger logger = LoggerFactory.getLogger(SnowboyKSService.class);
        File directory = new File(SNOWBOY_FOLDER);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                logger.info("snowboy dir created {}", SNOWBOY_FOLDER);
            }
        }
        File childDirectory = new File(EXTRACTION_FOLDER);
        if (!childDirectory.exists()) {
            if (childDirectory.mkdir()) {
                logger.info("snowboy extraction file dir created {}", EXTRACTION_FOLDER);
            }
        }
    }
    private final Logger logger = LoggerFactory.getLogger(SnowboyKSService.class);
    private final LocaleService localeService;
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("OH-voice-snowboyks");
    private @Nullable BundleContext bundleContext;
    private SnowboyKSConfiguration config = new SnowboyKSConfiguration();

    @Activate
    public SnowboyKSService(@Reference LocaleService localeService) {
        this.localeService = localeService;
    }

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> config) {
        this.config = new Configuration(config).as(SnowboyKSConfiguration.class);
        this.bundleContext = componentContext.getBundleContext();
    }

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_NAME;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        // as it is not possible to determine the language of the model that was downloaded and setup by the user, it is
        // assumed the language of the model is matching the locale of the openHAB server
        return Set.of(localeService.getLocale(null));
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set
                .of(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 16000L));
    }

    @Override
    public KSServiceHandle spot(KSListener ksListener, AudioStream audioStream, Locale locale, String wakeWord)
            throws KSException {
        // Sets up Snowboy.
        try {
            String wakeWordFileName = wakeWord.toLowerCase().replaceAll("\\s", "_");
            prepareLibs(wakeWordFileName);
            var wakeWorkModelPath = getKeywordResourcePath(wakeWordFileName);
            if (wakeWorkModelPath == null) {
                throw new KSException("Unable to locale wake word model '" + wakeWordFileName + WAKE_WORD_EXT + "'");
            }
            SnowboyDetect detector = new SnowboyDetect(Path.of(EXTRACTION_FOLDER, "common.res").toString(),
                    wakeWorkModelPath);
            detector.SetSensitivity(config.sensitivitiesString);
            detector.SetAudioGain(config.audioGain);
            detector.ApplyFrontend(config.applyFrontend);
            AtomicBoolean aborted = new AtomicBoolean(false);
            executor.submit(() -> startProcessing(audioStream, detector, ksListener, aborted));
            return () -> aborted.set(true);
        } catch (IOException e) {
            throw new KSException(e);
        }
    }

    private void prepareLibs(String wakeWordFileName) throws IOException {
        var platformName = getPlatformName();
        if (platformName == null) {
            throw new IOException("Unable to detect platform, make sure it's supported");
        }
        var context = bundleContext;
        if (context == null) {
            throw new IOException("Missing bundle context");
        }
        prepareLib(context, "common.res");
        if (INCLUDED_KEYWORDS.contains(wakeWordFileName)) {
            prepareLib(context, "models/" + wakeWordFileName + WAKE_WORD_EXT);
        }
        String libraryPath;
        String libraryName;
        switch (platformName) {
            case "macos":
                libraryName = "libsnowboy-detect-java.dylib";
                libraryPath = Path.of("macos", libraryName).toString();
                break;
            case "linux":
                libraryName = "libsnowboy-detect-java.so";
                libraryPath = Path.of("debian", libraryName).toString();
                break;
            case "linux-arm":
                libraryName = "libsnowboy-detect-java.so";
                libraryPath = Path.of("debian-arm", libraryName).toString();
                break;
            case "linux-arm64":
                libraryName = "libsnowboy-detect-java.so";
                libraryPath = Path.of("debian-arm64", libraryName).toString();
                break;
            default:
                throw new IOException("Unsupported platform");
        }
        prepareLib(context, libraryPath);
        System.load(Path.of(EXTRACTION_FOLDER, libraryName).toString());
    }

    private void startProcessing(AudioStream audioStream, SnowboyDetect detector, KSListener ksListener,
            AtomicBoolean aborted) {
        // Reads 0.1 second of audio in each call.
        byte[] targetData = new byte[3200];
        short[] snowboyData = new short[1600];
        int numBytesRead;

        while (!aborted.get()) {
            try {
                numBytesRead = audioStream.read(targetData, 0, targetData.length);
                if (numBytesRead == -1) {
                    logger.warn("Fails to read audio data.");
                    break;
                }
                // don't pass if we don't have a full buffer
                if (numBytesRead != 3200) {
                    Thread.sleep(100);
                    continue;
                }
                // Converts bytes into int16 that Snowboy will read.
                ByteBuffer.wrap(targetData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(snowboyData);
                // Detection.
                int result = detector.RunDetection(snowboyData, snowboyData.length);
                if (result > 0) {
                    logger.debug("keyword detected!");
                    ksListener.ksEventReceived(new KSpottedEvent());
                }
            } catch (IOException | InterruptedException e) {
                logger.error("{} while spotting: {}", e.getClass().getName(), e.getMessage());
            }
        }
    }

    private @Nullable String getPlatformName() {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (os.contains("mac") || os.contains("darwin")) {
            return "macos";
        }
        if (os.contains("linux")) {
            String arch = System.getProperty("os.arch");
            if ("armv7l".equals(arch)) {
                return "debian-arm";
            } else if ("aarch64".equals(arch)) {
                return "debian-arm64";
            } else {
                return "debian";
            }
        }
        return null;
    }

    private @Nullable String getKeywordResourcePath(String keyWordFileName) throws IOException {
        Path keywordPath = Path.of(SNOWBOY_FOLDER, keyWordFileName + WAKE_WORD_EXT);
        Path altKeywordPath = Path.of(SNOWBOY_FOLDER, keyWordFileName + WAKE_WORD_ALT_EXT);
        Path buildInKeywordPath = Path.of(EXTRACTION_FOLDER, keyWordFileName + WAKE_WORD_EXT);
        if (Files.exists(keywordPath)) {
            return keywordPath.toString();
        }
        if (Files.exists(altKeywordPath)) {
            return altKeywordPath.toString();
        }
        if (Files.exists(buildInKeywordPath)) {
            return buildInKeywordPath.toString();
        }
        return null;
    }

    private String prepareLib(BundleContext bundleContext, String bundlePath) throws IOException {
        File localFile = new File(EXTRACTION_FOLDER, bundlePath.substring(bundlePath.lastIndexOf(File.separator) + 1));
        if (!localFile.exists()) {
            if (File.separator.equals("\\")) {
                // bundle requires unix path separator
                logger.debug("use unix path separator");
                bundlePath = bundlePath.replace("\\", "/");
            }
            URL resourceUrl = bundleContext.getBundle().getEntry(bundlePath);
            logger.debug("extracting binary {} from bundle to extraction folder", bundlePath);
            if (resourceUrl == null) {
                throw new IOException("Missing bundle file: " + bundlePath);
            }
            extractBundleURL(resourceUrl, localFile);
        } else {
            logger.debug("binary {} already extracted", bundlePath);
        }
        return localFile.toString();
    }

    private void extractBundleURL(URL resourceUrl, File outFile) throws IOException {
        InputStream in = new BufferedInputStream(resourceUrl.openStream());
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
        byte[] buffer = new byte[1024];
        int lengthRead;
        while ((lengthRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, lengthRead);
            out.flush();
        }
        in.close();
        out.close();
    }
}
