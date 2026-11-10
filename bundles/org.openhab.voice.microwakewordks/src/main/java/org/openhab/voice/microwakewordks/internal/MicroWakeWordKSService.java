/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.voice.microwakewordks.internal;

import static org.openhab.voice.microwakewordks.internal.MicroWakeWordKSConstants.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.utils.AudioWaveUtils;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.voice.dialog.trigger.ks.KSException;
import org.openhab.core.voice.dialog.trigger.ks.KSListener;
import org.openhab.core.voice.dialog.trigger.ks.KSService;
import org.openhab.core.voice.dialog.trigger.ks.KSServiceHandle;
import org.openhab.core.voice.dialog.trigger.ks.KSpottedEvent;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jvoiceproject.microwakeword.BuiltinModel;
import io.github.jvoiceproject.microwakeword.MicroWakeWord;
import io.github.jvoiceproject.microwakeword.WakeWordModel;

/**
 * The {@link MicroWakeWordKSService} spots wake words with microWakeWord models.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Keyword Spotter", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class MicroWakeWordKSService implements KSService {
    private static final Path DEFAULT_MODEL_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "microwakeword");
    private static final long SAMPLE_RATE = MicroWakeWord.SAMPLE_RATE;
    private static final Duration FRAME_DURATION = Duration.ofMillis(1000L * MicroWakeWord.FRAME_SIZE / SAMPLE_RATE);
    private static final AudioFormat FORMAT = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED,
            false, 16, null, SAMPLE_RATE, 1);
    private static final AudioFormat WAVE_FORMAT = new AudioFormat(AudioFormat.CONTAINER_WAVE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, null, SAMPLE_RATE, 1);
    private static final String BUILTIN_IDS = Arrays.stream(BuiltinModel.values()).map(BuiltinModel::getId)
            .collect(Collectors.joining(", "));

    private final Logger logger = LoggerFactory.getLogger(MicroWakeWordKSService.class);
    // each spotter needs its own thread to detect a keyword
    private final ExecutorService executor = Executors
            .newThreadPerTaskExecutor(new NamedThreadFactory("voice-microwakewordks"));
    private final Path modelFolder;
    /** Abort flags of the running spotting tasks. */
    private final Set<AtomicBoolean> running = ConcurrentHashMap.newKeySet();
    private volatile MicroWakeWordKSConfiguration config = new MicroWakeWordKSConfiguration();

    public MicroWakeWordKSService() {
        this(DEFAULT_MODEL_FOLDER);
    }

    MicroWakeWordKSService(Path modelFolder) {
        this.modelFolder = modelFolder;
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        try {
            Files.createDirectories(modelFolder);
        } catch (IOException e) {
            logger.warn("Unable to create the model folder {}: {}", modelFolder, e.getMessage());
        }
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        MicroWakeWordKSConfiguration newConfig = new Configuration(config).as(MicroWakeWordKSConfiguration.class);
        Double cutoff = newConfig.probabilityCutoff;
        if (cutoff != null && !(cutoff >= 0 && cutoff <= 1)) {
            logger.warn("Ignoring probability cutoff {}, it must be between 0 and 1", cutoff);
            newConfig.probabilityCutoff = null;
        }
        this.config = newConfig;
    }

    @Deactivate
    protected void deactivate() {
        running.forEach(aborted -> aborted.set(true));
        executor.shutdownNow();
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
        return Set.of();
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(FORMAT, WAVE_FORMAT);
    }

    @Override
    public KSServiceHandle spot(KSListener ksListener, AudioStream audioStream, Locale locale, String keyword)
            throws KSException {
        AudioFormat format = audioStream.getFormat();
        if (!isSupported(format)) {
            throw new KSException("Unsupported audio format " + format + ", expected 16 kHz 16-bit mono PCM");
        }
        WakeWordModel model = loadModel(keyword);
        logger.debug("Spotting '{}'", model.getWakeWord());
        AtomicBoolean aborted = new AtomicBoolean(false);
        running.add(aborted);
        boolean wave = AudioFormat.CONTAINER_WAVE.equals(format.getContainer());
        try {
            executor.execute(() -> spotKeyword(model, audioStream, wave, ksListener, aborted));
        } catch (RejectedExecutionException e) {
            running.remove(aborted);
            throw new KSException("Unable to spot '" + keyword + "', the service is stopped", e);
        }
        return () -> aborted.set(true);
    }

    /**
     * Finds the model of a keyword, either a manifest in the model folder named after the keyword, or a bundled model.
     */
    WakeWordModel loadModel(String keyword) throws KSException {
        String id = keyword.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", "_").replaceAll("^_+|_+$", "");
        String manifestName = id + ".json";
        try {
            // throws InvalidPathException when file names on this system cannot hold the letters of the keyword
            Path manifest = modelFolder.resolve(manifestName);
            if (!id.isEmpty() && Files.isRegularFile(manifest)) {
                return WakeWordModel.load(manifest);
            }
            for (BuiltinModel builtin : BuiltinModel.values()) {
                if (builtin.getId().equals(id)) {
                    return WakeWordModel.load(builtin);
                }
            }
        } catch (IOException | InvalidPathException e) {
            throw new KSException("Unable to load the model for '" + keyword + "': " + e.getMessage(), e);
        }
        throw new KSException("No model for keyword '" + keyword + "', expected " + manifestName + " in " + modelFolder
                + " or one of the bundled models: " + BUILTIN_IDS);
    }

    /**
     * Reads the stream until it ends, fails or spotting is aborted.
     */
    void spotKeyword(WakeWordModel model, AudioStream audioStream, boolean wave, KSListener ksListener,
            AtomicBoolean aborted) {
        try {
            if (wave) {
                AudioWaveUtils.removeFMT(audioStream);
            }
            byte[] bytes = new byte[MicroWakeWord.FRAME_SIZE * 2];
            short[] frame = new short[MicroWakeWord.FRAME_SIZE];
            MicroWakeWord detector = null;
            while (!aborted.get()) {
                if (audioStream.readNBytes(bytes, 0, bytes.length) < bytes.length) {
                    break;
                }
                double cutoff = cutoff(model);
                if (detector == null || detector.getProbabilityCutoff() != cutoff) {
                    detector = new MicroWakeWord(model, cutoff);
                }
                ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(frame);
                if (detector.process(frame) && !aborted.get()) {
                    Duration audioAfterWakeWord = FRAME_DURATION.multipliedBy(detector.getFramesAfterWakeWord());
                    logger.debug("Spotted '{}' with probability {}, about {} ms after it ended", model.getWakeWord(),
                            detector.getProbability(), audioAfterWakeWord.toMillis());
                    // process() stays true while the probability is high, so only report the first frame
                    detector.reset();
                    try {
                        // sent before reading on, so speech recognition can continue from where the wake word ended
                        ksListener.ksEventReceived(new KSpottedEvent(audioAfterWakeWord));
                    } catch (RuntimeException e) {
                        // the listener runs the dialog on this thread, so a failing dialog must not end spotting
                        logger.warn("The dialog failed after spotting '{}'", model.getWakeWord(), e);
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            // a subclass of InterruptedIOException, but the thread was not interrupted
            logger.debug("Audio stream timed out: {}", e.getMessage());
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // a source that goes away closes its stream, which shows up as a read error, so this only ends spotting
            logger.debug("Audio stream ended: {}", e.getMessage());
        } finally {
            running.remove(aborted);
            logger.debug("Stopped spotting '{}'", model.getWakeWord());
        }
    }

    private double cutoff(WakeWordModel model) {
        Double cutoff = config.probabilityCutoff;
        return cutoff != null ? cutoff : model.getProbabilityCutoff();
    }

    private static boolean isSupported(AudioFormat format) {
        Integer channels = format.getChannels();
        return AudioFormat.CODEC_PCM_SIGNED.equals(format.getCodec())
                && (AudioFormat.CONTAINER_NONE.equals(format.getContainer())
                        || AudioFormat.CONTAINER_WAVE.equals(format.getContainer()))
                && !Boolean.TRUE.equals(format.isBigEndian()) && Integer.valueOf(16).equals(format.getBitDepth())
                && Long.valueOf(SAMPLE_RATE).equals(format.getFrequency()) && (channels == null || channels == 1);
    }
}
