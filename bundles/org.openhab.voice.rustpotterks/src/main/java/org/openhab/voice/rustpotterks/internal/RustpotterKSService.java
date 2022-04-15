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
package org.openhab.voice.rustpotterks.internal;

import static org.openhab.voice.rustpotterks.internal.RustpotterKSConstants.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.voice.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.givimad.rustpotter_java.RustpotterJava;

/**
 * The {@link RustpotterKSService} is responsible for creating things and thing
 * handlers.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Keyword Spotter", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class RustpotterKSService implements KSService {
    private static final String RUSTPOTTER_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "rustpotter").toString();
    private final Logger logger = LoggerFactory.getLogger(RustpotterKSService.class);
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("OH-voice-porcupineks");
    private RustpotterKSConfiguration config = new RustpotterKSConfiguration();
    private boolean loop = false;
    private @Nullable BundleContext bundleContext;
    static {
        Logger logger = LoggerFactory.getLogger(RustpotterKSService.class);
        File directory = new File(RUSTPOTTER_FOLDER);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                logger.info("rustpotter dir created {}", RUSTPOTTER_FOLDER);
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
        this.config = new Configuration(config).as(RustpotterKSConfiguration.class);
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
        RustpotterJava.loadLibrary();
        var rustpotter = new RustpotterJava();
        var modelName = keyword.replaceAll("\\s", "_") + ".rpw";
        var modelPath = Path.of(RUSTPOTTER_FOLDER, modelName);
        if (!modelPath.toFile().exists()) {
            throw new KSException("Missing model " + modelName);
        }
        rustpotter.addModel(modelPath.toString());
        executor.submit(() -> processAudioStream(rustpotter, ksListener, audioStream));
        return new KSServiceHandle() {
            @Override
            public void abort() {
                logger.debug("stopping service");
                loop = false;
            }
        };
    }

    private void processAudioStream(RustpotterJava rustpotter, KSListener ksListener, AudioStream audioStream) {
        int numBytesRead;
        var frameSize = (int) rustpotter.getFrameSize();
        ByteBuffer captureBuffer = ByteBuffer.allocate(frameSize * 2);
        captureBuffer.order(ByteOrder.nativeOrder());
        short[] audioBuffer = new short[frameSize];
        this.loop = true;
        while (loop) {
            try {
                // read a buffer of audio
                numBytesRead = audioStream.read(captureBuffer.array(), 0, captureBuffer.capacity());
                if (!loop) {
                    break;
                }
                if (numBytesRead != frameSize * 2) {
                    Thread.sleep(100);
                    continue;
                }
                captureBuffer.asShortBuffer().get(audioBuffer);
                var result = rustpotter.processPCMSigned(audioBuffer);
                if (result.isPresent()) {
                    var detection = result.get();
                    logger.debug("keyword '{}' detected with score {}!", detection.getName(), detection.getScore());
                    ksListener.ksEventReceived(new KSpottedEvent());
                }
            } catch (IOException | InterruptedException e) {
                String errorMessage = e.getMessage();
                ksListener.ksEventReceived(new KSErrorEvent(errorMessage != null ? errorMessage : "Unexpected error"));
            }
        }
        rustpotter.delete();
        logger.debug("rustpotter stopped");
    }
}
