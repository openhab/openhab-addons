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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
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
import org.osgi.service.component.annotations.Deactivate;
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
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME, description_uri = SERVICE_CATEGORY + ":"
        + SERVICE_ID)
public class PorcupineKSService implements KSService {
    private final Logger logger = LoggerFactory.getLogger(PorcupineKSService.class);
    private static final String PORCUPINE_FOLDER = Path.of(OpenHAB.getUserDataFolder(), ".porcupine").toString();
    private static final String EXTRACTION_FOLDER = Path.of(OpenHAB.getUserDataFolder(), ".porcupine", "extracted")
            .toString();
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("audio");
    private PorcupineKSConfiguration config = new PorcupineKSConfiguration();
    private boolean loop = false;
    private @Nullable BundleContext bundleContext;
    private @Nullable Porcupine porcupine = null;
    private @Nullable Future<?> scheduledTask;

    static {
        var logger = LoggerFactory.getLogger(PorcupineKSService.class);
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
        var serviceConfig = new org.openhab.core.config.core.Configuration(config).as(PorcupineKSConfiguration.class);
        this.config = serviceConfig;
        this.bundleContext = componentContext.getBundleContext();
        if (serviceConfig.apiKey.isBlank()) {
            logger.warn("Missing pico voice api key to use Porcupine Keyword Spotter");
        }
    }

    @Deactivate
    protected void deactivate() {
        var scheduledTask = this.scheduledTask;
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }
        var porcupine = this.porcupine;
        if (porcupine != null) {
            porcupine.delete();
        }
        loop = false;
        bundleContext = null;
    }

    private String prepareLib(BundleContext bundleContext, String path) throws IOException {
        var relativePath = path.substring(path.indexOf("porcupine/"));
        var porcupineResource = bundleContext.getBundle().getEntry(relativePath);
        File localFile = new File(EXTRACTION_FOLDER, relativePath.substring(relativePath.lastIndexOf("/") + 1));
        if (!localFile.exists()) {
            logger.debug("extracting binary {} from bundle to extraction folder", path);
            extractFromBundle(porcupineResource, localFile);
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
        return Set.of(Locale.ENGLISH, new Locale("es"), Locale.FRANCE, Locale.GERMAN);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set
                .of(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 16000L));
    }

    @Override
    public KSServiceHandle spot(KSListener ksListener, AudioStream audioStream, Locale locale, String s)
            throws KSException {
        Porcupine porcupine;
        if (config.apiKey.isBlank()) {
            throw new KSException("Missing pico voice api key");
        }
        var bundleContext = this.bundleContext;
        if (bundleContext == null) {
            throw new KSException("Missing bundle context");
        }
        try {
            porcupine = initPorcupine(bundleContext, locale, s);
            this.porcupine = porcupine;
        } catch (PorcupineException | IOException e) {
            throw new KSException(e);
        }
        var scheduledTask = executor.submit(() -> processInBackground(porcupine, ksListener, audioStream));
        this.scheduledTask = scheduledTask;
        return () -> {
            loop = false;
            scheduledTask.cancel(true);
            porcupine.delete();
        };
    }

    private Porcupine initPorcupine(BundleContext bundleContext, Locale locale, String keyword)
            throws IOException, PorcupineException {
        var libraryPath = prepareLib(bundleContext, Porcupine.LIBRARY_PATH);
        var modelPath = getModelPath(bundleContext, locale);
        var keywordPath = getKeywordResourcePath(bundleContext, keyword);
        logger.debug("Porcupine library path: {}", libraryPath);
        logger.debug("Porcupine model path: {}", modelPath);
        logger.debug("Porcupine keyword path: {}", keywordPath);
        logger.debug("Porcupine sensitivity: {}", config.sensitivity);
        return new Porcupine(config.apiKey, libraryPath, modelPath, new String[] { keywordPath },
                new float[] { config.sensitivity });
    }

    private String getPorcupineEnv() {
        // extract library detected env from default library path
        var searchTerm = "lib" + File.separator + "java" + File.separator;
        var env = Porcupine.LIBRARY_PATH.substring(Porcupine.LIBRARY_PATH.indexOf(searchTerm) + searchTerm.length());
        env = env.substring(0, env.indexOf(File.separator));
        return env;
    }

    private String getModelPath(BundleContext bundleContext, Locale locale) throws IOException {
        String modelPath = null;
        if (locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
            var dePath = Path.of(PORCUPINE_FOLDER, "porcupine_params_de.pv");
            if (Files.exists(dePath)) {
                modelPath = dePath.toString();
            } else {
                logger.warn(
                        "You can provide a specific model for de language in {}, english language model will be used",
                        PORCUPINE_FOLDER);
            }
        } else if (locale.getLanguage().equals(Locale.FRENCH.getLanguage())) {
            var frPath = Path.of(PORCUPINE_FOLDER, "porcupine_params_fr.pv");
            if (Files.exists(frPath)) {
                modelPath = frPath.toString();
            } else {
                logger.warn(
                        "You can provide a specific model for fr language in {}, english language model will be used",
                        PORCUPINE_FOLDER);
            }
        } else if (locale.getLanguage().equals("es")) {
            var esPath = Path.of(PORCUPINE_FOLDER, "porcupine_params_es.pv");
            if (Files.exists(esPath)) {
                modelPath = esPath.toString();
            } else {
                logger.warn(
                        "You can provide a specific model for es language in {}, english language model will be used",
                        PORCUPINE_FOLDER);
            }
        }
        if (modelPath == null) {
            modelPath = prepareLib(bundleContext, Porcupine.MODEL_PATH);
        }
        return modelPath;
    }

    private String getKeywordResourcePath(BundleContext bundleContext, String keyWord) throws IOException {
        var localKeywordFile = keyWord.toLowerCase().replace(" ", "_") + ".ppn";
        var localKeywordPath = Path.of(PORCUPINE_FOLDER, localKeywordFile);
        if (Files.exists(localKeywordPath)) {
            return localKeywordPath.toString();
        }
        String env = getPorcupineEnv();
        var keywordPath = "porcupine/resources/keyword_files/" + env + "/" + keyWord.toLowerCase() + "_" + env + ".ppn";
        return prepareLib(bundleContext, keywordPath);
    }

    private void processInBackground(Porcupine porcupine, KSListener ksListener, AudioStream audioStream) {
        int numBytesRead;
        // buffers for processing audio
        int frameLength = porcupine.getFrameLength();
        ByteBuffer captureBuffer = ByteBuffer.allocate(frameLength * 2);
        captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
        short[] porcupineBuffer = new short[frameLength];
        this.loop = true;
        while (loop) {
            try {
                // read a buffer of audio
                numBytesRead = audioStream.read(captureBuffer.array(), 0, captureBuffer.capacity());
                // don't pass to porcupine if we don't have a full buffer
                if (numBytesRead != frameLength * 2) {
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
            } catch (IOException | PorcupineException e) {
                var errorMessage = e.getMessage();
                ksListener.ksEventReceived(new KSErrorEvent(errorMessage != null ? errorMessage : "Unexpected error"));
            }
        }
    }
}
