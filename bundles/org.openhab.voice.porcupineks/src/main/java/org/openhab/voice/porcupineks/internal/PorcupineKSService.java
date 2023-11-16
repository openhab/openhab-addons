/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.voice.porcupineks.internal;

import static org.openhab.voice.porcupineks.internal.PorcupineKSConstants.SERVICE_CATEGORY;
import static org.openhab.voice.porcupineks.internal.PorcupineKSConstants.SERVICE_ID;
import static org.openhab.voice.porcupineks.internal.PorcupineKSConstants.SERVICE_NAME;
import static org.openhab.voice.porcupineks.internal.PorcupineKSConstants.SERVICE_PID;

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
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.voice.KSErrorEvent;
import org.openhab.core.voice.KSException;
import org.openhab.core.voice.KSListener;
import org.openhab.core.voice.KSService;
import org.openhab.core.voice.KSServiceHandle;
import org.openhab.core.voice.KSpottedEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;

/**
 * The {@link PorcupineKSService} is a keyword spotting implementation based on porcupine.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Keyword Spotter", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class PorcupineKSService implements KSService {
    private static final String PORCUPINE_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "porcupine").toString();
    private static final String EXTRACTION_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "porcupine", "extracted")
            .toString();
    private final Logger logger = LoggerFactory.getLogger(PorcupineKSService.class);
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("OH-voice-porcupineks");
    private PorcupineKSConfiguration config = new PorcupineKSConfiguration();
    private @Nullable BundleContext bundleContext;

    static {
        Logger logger = LoggerFactory.getLogger(PorcupineKSService.class);
        File directory = new File(PORCUPINE_FOLDER);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                logger.info("porcupine dir created {}", PORCUPINE_FOLDER);
            }
        }
        File childDirectory = new File(EXTRACTION_FOLDER);
        if (!childDirectory.exists()) {
            if (childDirectory.mkdir()) {
                logger.info("porcupine extraction file dir created {}", EXTRACTION_FOLDER);
            }
        }
    }

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> config) {
        this.bundleContext = componentContext.getBundleContext();
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = new Configuration(config).as(PorcupineKSConfiguration.class);
        if (this.config.apiKey.isBlank()) {
            logger.warn("Missing pico voice api key to use Porcupine Keyword Spotter");
        }
    }

    private String prepareLib(BundleContext bundleContext, String path) throws IOException {
        if (!path.contains("porcupine" + File.separator)) {
            // this should never happen
            throw new IOException("Path is not pointing to porcupine bundle files " + path);
        }
        // get a path relative to the porcupine bundle folder
        String relativePath;
        if (path.startsWith("porcupine" + File.separator)) {
            relativePath = path;
        } else {
            relativePath = path.substring(path.lastIndexOf(File.separator + "porcupine" + File.separator) + 1);
        }
        File localFile = new File(EXTRACTION_FOLDER,
                relativePath.substring(relativePath.lastIndexOf(File.separator) + 1));
        if (!localFile.exists()) {
            if ("\\".equals(File.separator)) {
                // bundle requires unix path separator
                logger.debug("use unix path separator");
                relativePath = relativePath.replace("\\", "/");
            }
            URL porcupineResource = bundleContext.getBundle().getEntry(relativePath);
            logger.debug("extracting binary {} from bundle to extraction folder", relativePath);
            if (porcupineResource == null) {
                throw new IOException("Missing bundle file: " + relativePath);
            }
            extractFromBundle(porcupineResource, localFile);
        } else {
            logger.debug("binary {} already extracted", relativePath);
        }
        return localFile.toString();
    }

    private void extractFromBundle(URL resourceUrl, File targetFile) throws IOException {
        InputStream in = new BufferedInputStream(resourceUrl.openStream());
        OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));
        byte[] buffer = new byte[1024];
        int lengthRead;
        while ((lengthRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, lengthRead);
            out.flush();
        }
        in.close();
        out.close();
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
        return Set.of(Locale.ENGLISH, new Locale("es"), Locale.FRENCH, Locale.GERMAN);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set
                .of(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 16000L));
    }

    @Override
    public KSServiceHandle spot(KSListener ksListener, AudioStream audioStream, Locale locale, String keyword)
            throws KSException {
        Porcupine porcupine;
        if (config.apiKey.isBlank()) {
            throw new KSException("Missing pico voice api key");
        }
        BundleContext bundleContext = this.bundleContext;
        if (bundleContext == null) {
            throw new KSException("Missing bundle context");
        }
        try {
            porcupine = initPorcupine(bundleContext, locale, keyword);
        } catch (PorcupineException | IOException e) {
            throw new KSException(e);
        }
        final AtomicBoolean aborted = new AtomicBoolean(false);
        Future<?> scheduledTask = executor
                .submit(() -> processInBackground(porcupine, ksListener, audioStream, aborted));
        return new KSServiceHandle() {
            @Override
            public void abort() {
                logger.debug("stopping service");
                aborted.set(true);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                scheduledTask.cancel(true);
            }
        };
    }

    private Porcupine initPorcupine(BundleContext bundleContext, Locale locale, String keyword)
            throws IOException, PorcupineException {
        // Suppress library logs
        java.util.logging.Logger globalJavaLogger = java.util.logging.Logger
                .getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
        Level currentGlobalLogLevel = globalJavaLogger.getLevel();
        globalJavaLogger.setLevel(java.util.logging.Level.OFF);
        String bundleLibraryPath = Porcupine.LIBRARY_PATH;
        if (bundleLibraryPath == null) {
            throw new PorcupineException("Unsupported environment, ensure Porcupine is supported by your system");
        }
        String libraryPath = prepareLib(bundleContext, bundleLibraryPath);
        String alternativeModelPath = getAlternativeModelPath(bundleContext, locale);
        String modelPath = alternativeModelPath != null ? alternativeModelPath
                : prepareLib(bundleContext, Porcupine.MODEL_PATH);
        String keywordPath = getKeywordResourcePath(bundleContext, keyword, alternativeModelPath == null);
        logger.debug("Porcupine library path: {}", libraryPath);
        logger.debug("Porcupine model path: {}", modelPath);
        logger.debug("Porcupine keyword path: {}", keywordPath);
        logger.debug("Porcupine sensitivity: {}", config.sensitivity);
        try {
            return new Porcupine(config.apiKey, libraryPath, modelPath, new String[] { keywordPath },
                    new float[] { config.sensitivity });
        } finally {
            // restore log level
            globalJavaLogger.setLevel(currentGlobalLogLevel);
        }
    }

    private String getPorcupineEnv() {
        // get porcupine env from resolved library path
        String searchTerm = "lib" + File.separator + "java" + File.separator;
        String env = Porcupine.LIBRARY_PATH.substring(Porcupine.LIBRARY_PATH.indexOf(searchTerm) + searchTerm.length());
        env = env.substring(0, env.indexOf(File.separator));
        return env;
    }

    private @Nullable String getAlternativeModelPath(BundleContext bundleContext, Locale locale) throws IOException {
        String modelPath = null;
        if (locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
            Path dePath = Path.of(PORCUPINE_FOLDER, "porcupine_params_de.pv");
            if (Files.exists(dePath)) {
                modelPath = dePath.toString();
            } else {
                logger.warn(
                        "You can provide a specific model for de language in {}, english language model will be used",
                        PORCUPINE_FOLDER);
            }
        } else if (locale.getLanguage().equals(Locale.FRENCH.getLanguage())) {
            Path frPath = Path.of(PORCUPINE_FOLDER, "porcupine_params_fr.pv");
            if (Files.exists(frPath)) {
                modelPath = frPath.toString();
            } else {
                logger.warn(
                        "You can provide a specific model for fr language in {}, english language model will be used",
                        PORCUPINE_FOLDER);
            }
        } else if ("es".equals(locale.getLanguage())) {
            Path esPath = Path.of(PORCUPINE_FOLDER, "porcupine_params_es.pv");
            if (Files.exists(esPath)) {
                modelPath = esPath.toString();
            } else {
                logger.warn(
                        "You can provide a specific model for es language in {}, english language model will be used",
                        PORCUPINE_FOLDER);
            }
        }
        return modelPath;
    }

    private String getKeywordResourcePath(BundleContext bundleContext, String keyWord, boolean allowBuildIn)
            throws IOException {
        String localKeywordFile = keyWord.toLowerCase().replace(" ", "_") + ".ppn";
        Path localKeywordPath = Path.of(PORCUPINE_FOLDER, localKeywordFile);
        if (Files.exists(localKeywordPath)) {
            return localKeywordPath.toString();
        }
        if (allowBuildIn) {
            try {
                Porcupine.BuiltInKeyword.valueOf(keyWord.toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Unable to find model file for configured wake word neither is build-in. Should be at "
                                + localKeywordPath);
            }
            String env = getPorcupineEnv();
            String keywordPath = Path
                    .of("porcupine", "resources", "keyword_files", env, keyWord.replace(" ", "_") + "_" + env + ".ppn")
                    .toString();
            return prepareLib(bundleContext, keywordPath);
        } else {
            throw new IllegalArgumentException(
                    "Unable to find model file for configured wake word; there are no build-in wake words for your language. Should be at "
                            + localKeywordPath);
        }
    }

    private void processInBackground(Porcupine porcupine, KSListener ksListener, AudioStream audioStream,
            AtomicBoolean aborted) {
        int numBytesRead;
        // buffers for processing audio
        int frameLength = porcupine.getFrameLength();
        ByteBuffer captureBuffer = ByteBuffer.allocate(frameLength * 2);
        captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
        short[] porcupineBuffer = new short[frameLength];
        while (!aborted.get()) {
            try {
                // read a buffer of audio
                numBytesRead = audioStream.read(captureBuffer.array(), 0, captureBuffer.capacity());
                if (aborted.get()) {
                    break;
                }
                // don't pass to porcupine if we don't have a full buffer
                if (numBytesRead != frameLength * 2) {
                    Thread.sleep(100);
                    continue;
                }
                // copy into 16-bit buffer
                captureBuffer.asShortBuffer().get(porcupineBuffer);
                // process with porcupine
                int result = porcupine.process(porcupineBuffer);
                if (result >= 0) {
                    logger.debug("keyword detected!");
                    ksListener.ksEventReceived(new KSpottedEvent());
                }
            } catch (IOException | PorcupineException | InterruptedException e) {
                String errorMessage = e.getMessage();
                ksListener.ksEventReceived(new KSErrorEvent(errorMessage != null ? errorMessage : "Unexpected error"));
            }
        }
        porcupine.delete();
        logger.debug("Porcupine stopped");
    }
}
