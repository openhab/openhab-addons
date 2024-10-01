/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.givimad.rustpotter_java.Endianness;
import io.github.givimad.rustpotter_java.Rustpotter;
import io.github.givimad.rustpotter_java.RustpotterConfig;
import io.github.givimad.rustpotter_java.RustpotterDetection;
import io.github.givimad.rustpotter_java.SampleFormat;
import io.github.givimad.rustpotter_java.ScoreMode;
import io.github.givimad.rustpotter_java.VADMode;

/**
 * The {@link RustpotterKSService} is a keyword spotting implementation based on rustpotter.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Keyword Spotter", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class RustpotterKSService implements KSService {
    private static final Path RUSTPOTTER_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "rustpotter");
    private static final Path RUSTPOTTER_RECORDS_FOLDER = RUSTPOTTER_FOLDER.resolve("records");
    private final Logger logger = LoggerFactory.getLogger(RustpotterKSService.class);
    private final ExecutorService executor = ThreadPoolManager.getPool("voice-rustpotterks");
    private RustpotterKSConfiguration config = new RustpotterKSConfiguration();
    private final List<RustpotterMutex> runningInstances = new ArrayList<>();

    @Activate
    protected void activate(Map<String, Object> config) {
        logger.debug("Loading library");
        tryCreateDir(RUSTPOTTER_FOLDER);
        tryCreateDir(RUSTPOTTER_RECORDS_FOLDER);
        try {
            Rustpotter.loadLibrary();
        } catch (IOException e) {
            logger.warn("Unable to load rustpotter native library: {}", e.getMessage());
        }
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = new Configuration(config).as(RustpotterKSConfiguration.class);
        asyncUpdateActiveInstances();
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
        return Set.of(
                new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 16000L),
                new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, null, 16, null, null),
                new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, null, 32, null, null),
                new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 16000L),
                new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, null, 16, null, null),
                new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, null, 32, null, null));
    }

    @Override
    public KSServiceHandle spot(KSListener ksListener, AudioStream audioStream, Locale locale, String keyword)
            throws KSException {
        var audioFormat = audioStream.getFormat();
        var frequency = audioFormat.getFrequency();
        var bitDepth = audioFormat.getBitDepth();
        var channels = audioFormat.getChannels();
        var isBigEndian = audioFormat.isBigEndian();
        if (frequency == null || bitDepth == null || channels == null || isBigEndian == null) {
            throw new KSException(
                    "Missing stream metadata: frequency, bit depth, channels and endianness must be defined.");
        }
        var endianness = isBigEndian ? Endianness.BIG : Endianness.LITTLE;
        logger.debug("Audio wav spec: sample rate {}, {} bits, {} channels, {}", frequency, bitDepth, channels,
                isBigEndian ? "big-endian" : "little-endian");
        var wakewordName = keyword.replaceAll("\\s", "_") + ".rpw";

        var wakewordPath = RUSTPOTTER_FOLDER.resolve(wakewordName);
        if (!Files.exists(wakewordPath)) {
            throw new KSException("Missing wakeword file: " + wakewordPath);
        }
        Rustpotter rustpotter;
        try {
            rustpotter = initRustpotter(frequency, bitDepth, channels, endianness);
        } catch (Exception e) {
            throw new KSException("Unable to start rustpotter: " + e.getMessage(), e);
        }
        try {
            rustpotter.addWakewordFile("w", wakewordPath.toString());
        } catch (Exception e) {
            throw new KSException("Unable to load wakeword file: " + e.getMessage());
        }
        logger.debug("Wakeword '{}' loaded", wakewordPath);
        AtomicBoolean aborted = new AtomicBoolean(false);
        int bufferSize = (int) rustpotter.getBytesPerFrame();
        long bytesPerMs = frequency / 1000 * (long) bitDepth;
        RustpotterMutex rustpotterMutex = new RustpotterMutex(rustpotter);
        synchronized (this.runningInstances) {
            this.runningInstances.add(rustpotterMutex);
        }
        executor.submit(
                () -> processAudioStream(rustpotterMutex, bufferSize, bytesPerMs, ksListener, audioStream, aborted));
        return () -> {
            logger.debug("Stopping service");
            aborted.set(true);
        };
    }

    private Rustpotter initRustpotter(long frequency, int bitDepth, int channels, Endianness endianness)
            throws Exception {
        var rustpotterConfig = initRustpotterConfig();
        // audio format config just need to be set for initializing the instance, is ignored on config updates
        rustpotterConfig.setSampleFormat(getIntSampleFormat(bitDepth));
        rustpotterConfig.setSampleRate(frequency);
        rustpotterConfig.setChannels(channels);
        rustpotterConfig.setEndianness(endianness);
        // init the detector
        var rustpotter = new Rustpotter(rustpotterConfig);
        rustpotterConfig.delete();
        return rustpotter;
    }

    private RustpotterConfig initRustpotterConfig() {
        var rustpotterConfig = new RustpotterConfig();
        // detector configs
        rustpotterConfig.setThreshold(config.threshold);
        rustpotterConfig.setAveragedThreshold(config.averagedThreshold);
        rustpotterConfig.setScoreMode(getScoreMode(config.scoreMode));
        rustpotterConfig.setMinScores(config.minScores);
        rustpotterConfig.setEager(config.eager);
        rustpotterConfig.setScoreRef(config.scoreRef);
        rustpotterConfig.setBandSize(config.bandSize);
        rustpotterConfig.setVADMode(getVADMode(config.vadMode));
        rustpotterConfig.setRecordPath(config.record ? RUSTPOTTER_RECORDS_FOLDER.toString() : null);
        // filter configs
        rustpotterConfig.setGainNormalizerEnabled(config.gainNormalizer);
        rustpotterConfig.setMinGain(config.minGain);
        rustpotterConfig.setMaxGain(config.maxGain);
        rustpotterConfig.setGainRef(config.gainRef);
        rustpotterConfig.setBandPassFilterEnabled(config.bandPass);
        rustpotterConfig.setBandPassLowCutoff(config.lowCutoff);
        rustpotterConfig.setBandPassHighCutoff(config.highCutoff);

        return rustpotterConfig;
    }

    private void processAudioStream(RustpotterMutex rustpotter, int bufferSize, long bytesPerMs, KSListener ksListener,
            AudioStream audioStream, AtomicBoolean aborted) {
        int numBytesRead;
        byte[] audioBuffer = new byte[bufferSize];
        int remaining = bufferSize;
        boolean hasFailed = false;
        while (!aborted.get()) {
            try {
                numBytesRead = audioStream.read(audioBuffer, bufferSize - remaining, remaining);
                if (aborted.get() || numBytesRead == -1) {
                    break;
                }
                if (numBytesRead != remaining) {
                    remaining = remaining - numBytesRead;
                    try {
                        Thread.sleep(remaining / bytesPerMs);
                    } catch (InterruptedException ignored) {
                        logger.warn("Thread interrupted while waiting for audio, aborting execution");
                        aborted.set(true);
                    }
                    if (aborted.get()) {
                        break;
                    }
                    continue;
                }
                remaining = bufferSize;
                var result = rustpotter.processBytes(audioBuffer);
                hasFailed = false;
                if (result.isPresent()) {
                    var detection = result.get();
                    if (logger.isDebugEnabled()) {
                        ArrayList<String> scores = new ArrayList<>();
                        var scoreNames = detection.getScoreNames().split("\\|\\|");
                        var scoreValues = detection.getScores();
                        for (var i = 0; i < Integer.min(scoreNames.length, scoreValues.length); i++) {
                            scores.add("'" + scoreNames[i] + "': " + scoreValues[i]);
                        }
                        logger.debug("Detected '{}' with: Score: {}, AvgScore: {}, Count: {}, Gain: {}, Scores: {}",
                                detection.getName(), detection.getScore(), detection.getAvgScore(),
                                detection.getCounter(), detection.getGain(), String.join(", ", scores));
                    }
                    detection.delete();
                    ksListener.ksEventReceived(new KSpottedEvent());
                }
            } catch (IOException e) {
                String errorMessage = e.getMessage();
                ksListener.ksEventReceived(new KSErrorEvent(errorMessage != null ? errorMessage : "Unexpected error"));
                if (hasFailed) {
                    logger.warn("Multiple consecutive errors, stopping service");
                    break;
                }
                hasFailed = true;
            }
        }
        synchronized (this.runningInstances) {
            this.runningInstances.remove(rustpotter);
        }
        rustpotter.delete();
        logger.debug("Rustpotter stopped");
    }

    private void asyncUpdateActiveInstances() {
        int nInstances;
        synchronized (this.runningInstances) {
            nInstances = this.runningInstances.size();
        }
        if (nInstances == 0) {
            return;
        }
        var rustpotterConfig = initRustpotterConfig();
        executor.submit(() -> {
            logger.debug("Updating running instances");
            synchronized (this.runningInstances) {
                for (RustpotterMutex rustpotter : this.runningInstances) {
                    rustpotter.updateConfig(rustpotterConfig);
                }
                logger.debug("{} running instances updated", this.runningInstances.size());
            }
            rustpotterConfig.delete();
        });
    }

    private static SampleFormat getIntSampleFormat(int bitDepth) throws IOException {
        return switch (bitDepth) {
            case 8 -> SampleFormat.I8;
            case 16 -> SampleFormat.I16;
            case 32 -> SampleFormat.I32;
            default -> throw new IOException("Unsupported audio bit depth: " + bitDepth);
        };
    }

    private ScoreMode getScoreMode(String mode) {
        return switch (mode) {
            case "average" -> ScoreMode.AVG;
            case "median" -> ScoreMode.MEDIAN;
            case "p25" -> ScoreMode.P25;
            case "p50" -> ScoreMode.P50;
            case "p75" -> ScoreMode.P75;
            case "p80" -> ScoreMode.P80;
            case "p90" -> ScoreMode.P90;
            case "p95" -> ScoreMode.P95;
            default -> ScoreMode.MAX;
        };
    }

    private @Nullable VADMode getVADMode(String mode) {
        return switch (mode) {
            case "easy" -> VADMode.EASY;
            case "medium" -> VADMode.MEDIUM;
            case "hard" -> VADMode.HARD;
            default -> null;
        };
    }

    private void tryCreateDir(Path rustpotterFolder) {
        if (!Files.exists(rustpotterFolder) || !Files.isDirectory(rustpotterFolder)) {
            try {
                Files.createDirectory(rustpotterFolder);
                logger.info("Folder {} created", rustpotterFolder);
            } catch (IOException e) {
                logger.warn("Unable to create folder {}", rustpotterFolder);
            }
        }
    }

    private record RustpotterMutex(Rustpotter rustpotter) {

        public Optional<RustpotterDetection> processBytes(byte[] bytes) {
            synchronized (this.rustpotter) {
                return this.rustpotter.processBytes(bytes);
            }
        }

        public void updateConfig(RustpotterConfig config) {
            synchronized (this.rustpotter) {
                this.rustpotter.updateConfig(config);
            }
        }

        public void delete() {
            synchronized (this.rustpotter) {
                this.rustpotter.delete();
            }
        }
    }
}
