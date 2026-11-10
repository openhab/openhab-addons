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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.ByteArrayAudioStream;
import org.openhab.core.voice.dialog.trigger.DTEvent;
import org.openhab.core.voice.dialog.trigger.ks.KSEvent;
import org.openhab.core.voice.dialog.trigger.ks.KSException;
import org.openhab.core.voice.dialog.trigger.ks.KSListener;
import org.openhab.core.voice.dialog.trigger.ks.KSServiceHandle;
import org.openhab.core.voice.dialog.trigger.ks.KSpottedEvent;

import io.github.jvoiceproject.microwakeword.MicroWakeWord;
import io.github.jvoiceproject.microwakeword.WakeWordModel;

/**
 * Tests for {@link MicroWakeWordKSService}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MicroWakeWordKSServiceTest {
    private static final AudioFormat RAW_FORMAT = new AudioFormat(AudioFormat.CONTAINER_NONE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 16000L, 1);
    private static final AudioFormat WAVE_FORMAT = new AudioFormat(AudioFormat.CONTAINER_WAVE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 16000L, 1);
    /** The size of the header of the test file. */
    private static final int WAV_HEADER_SIZE = 44;
    private static final int FRAME_BYTES = MicroWakeWord.FRAME_SIZE * 2;
    private static final Duration FRAME_DURATION = Duration.ofMillis(10);

    private @TempDir Path modelFolder = Path.of("");
    private @NonNullByDefault({}) MicroWakeWordKSService service;
    private final EventRecorder listener = new EventRecorder();

    @BeforeEach
    public void setUp() {
        service = new MicroWakeWordKSService(modelFolder);
        service.activate(Map.of());
    }

    @AfterEach
    public void tearDown() {
        service.deactivate();
    }

    @Test
    public void spotsTheWakeWord() throws Exception {
        service.spot(listener, wave(), Locale.ENGLISH, "Hey Jarvis");

        assertTrue(listener.spotted.await(30, TimeUnit.SECONDS));
    }

    @Test
    public void reportsEachDetectionOnce() throws Exception {
        WakeWordModel model = service.loadModel("hey jarvis");

        service.spotKeyword(model, wave(), true, listener, new AtomicBoolean(false));

        assertEquals(List.of(KSpottedEvent.class), listener.eventTypes());
    }

    @Test
    public void tellsHowMuchAudioWasReadAfterTheWakeWord() throws Exception {
        service.spotKeyword(service.loadModel("hey_jarvis"), wave(), true, listener, new AtomicBoolean(false));

        assertEquals(List.of(KSpottedEvent.class), listener.eventTypes());
        Duration audioAfterWakeWord = ((KSpottedEvent) listener.events.getFirst()).getAudioAfterKeyword();
        assertNotNull(audioAfterWakeWord);
        // a detection comes shortly after the end of the wake word
        assertTrue(audioAfterWakeWord.isPositive() && audioAfterWakeWord.compareTo(Duration.ofMillis(500)) < 0,
                audioAfterWakeWord.toString());
    }

    @Test
    public void reportsTheWakeWordBeforeReadingOn() throws Exception {
        WakeWordModel model = service.loadModel("hey_jarvis");
        // a detector fed the same audio shows on which frame the wake word is detected
        MicroWakeWord detector = new MicroWakeWord(model);
        ShortBuffer samples = ByteBuffer.wrap(pcmBytes()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] frame = new short[MicroWakeWord.FRAME_SIZE];
        int frames = 0;
        do {
            samples.get(frame);
            frames++;
        } while (!detector.process(frame));
        Duration expectedAudioAfterWakeWord = FRAME_DURATION.multipliedBy(detector.getFramesAfterWakeWord());

        CountingAudioStream stream = new CountingAudioStream(wave());
        List<Long> bytesReadWhenSpotted = new CopyOnWriteArrayList<>();
        EventRecorder recorder = new EventRecorder() {
            @Override
            public void ksEventReceived(KSEvent ksEvent) {
                bytesReadWhenSpotted.add(stream.bytesRead);
                super.ksEventReceived(ksEvent);
            }
        };

        service.spotKeyword(model, stream, true, recorder, new AtomicBoolean(false));

        // the stream is read up to the frame with the detection, and no further
        assertEquals(List.of(WAV_HEADER_SIZE + (long) frames * FRAME_BYTES), bytesReadWhenSpotted);
        assertEquals(expectedAudioAfterWakeWord, ((KSpottedEvent) recorder.events.getFirst()).getAudioAfterKeyword());
    }

    @Test
    public void keepsSpottingWhenTheDialogFails() throws Exception {
        EventRecorder failingDialog = new EventRecorder() {
            @Override
            public void ksEventReceived(KSEvent ksEvent) {
                super.ksEventReceived(ksEvent);
                throw new IllegalStateException("The dialog failed");
            }
        };
        byte[] pcm = pcmBytes();
        byte[] wakeWordTwice = Arrays.copyOf(pcm, 2 * pcm.length);
        System.arraycopy(pcm, 0, wakeWordTwice, pcm.length, pcm.length);
        AudioStream stream = new ByteArrayAudioStream(wakeWordTwice, RAW_FORMAT);

        service.spotKeyword(service.loadModel("hey_jarvis"), stream, false, failingDialog, new AtomicBoolean(false));

        assertEquals(List.of(KSpottedEvent.class, KSpottedEvent.class), failingDialog.eventTypes());
    }

    @Test
    public void readsRawAudio() throws Exception {
        AudioStream stream = new ByteArrayAudioStream(pcmBytes(), RAW_FORMAT);

        service.spotKeyword(service.loadModel("hey_jarvis"), stream, false, listener, new AtomicBoolean(false));

        assertEquals(List.of(KSpottedEvent.class), listener.eventTypes());
    }

    @Test
    public void silenceIsNotSpotted() throws Exception {
        AudioStream stream = new ByteArrayAudioStream(new byte[5 * 16000 * 2], RAW_FORMAT);

        service.spotKeyword(service.loadModel("hey_jarvis"), stream, false, listener, new AtomicBoolean(false));

        assertEquals(List.of(), listener.eventTypes());
    }

    @Test
    public void configuredCutoffReplacesTheModelCutoff() throws Exception {
        service.modified(Map.of("probabilityCutoff", BigDecimal.ONE));

        service.spotKeyword(service.loadModel("hey_jarvis"), wave(), true, listener, new AtomicBoolean(false));

        assertEquals(List.of(), listener.eventTypes());
    }

    @Test
    public void ignoresInvalidCutoffs() throws Exception {
        service.modified(Map.of("probabilityCutoff", new BigDecimal("1.5")));

        service.spotKeyword(service.loadModel("hey_jarvis"), wave(), true, listener, new AtomicBoolean(false));

        assertEquals(List.of(KSpottedEvent.class), listener.eventTypes());
    }

    @Test
    public void abortedSpottingReportsNothing() throws Exception {
        service.spotKeyword(service.loadModel("hey_jarvis"), wave(), true, listener, new AtomicBoolean(true));

        assertEquals(List.of(), listener.eventTypes());
    }

    @Test
    public void abortingStopsSpotting() throws Exception {
        SilenceAudioStream stream = new SilenceAudioStream();
        KSServiceHandle handle = service.spot(listener, stream, Locale.ENGLISH, "hey jarvis");
        Thread reader = stream.awaitReader();

        handle.abort();

        reader.join(TimeUnit.SECONDS.toMillis(30));
        assertFalse(reader.isAlive());
    }

    @Test
    public void deactivatingStopsSpotting() throws Exception {
        SilenceAudioStream stream = new SilenceAudioStream();
        service.spot(listener, stream, Locale.ENGLISH, "hey jarvis");
        Thread reader = stream.awaitReader();

        service.deactivate();

        reader.join(TimeUnit.SECONDS.toMillis(30));
        assertFalse(reader.isAlive());
        assertThrows(KSException.class, () -> service.spot(listener, wave(), Locale.ENGLISH, "hey jarvis"));
    }

    @Test
    public void closedStreamsEndSpottingQuietly() throws Exception {
        AudioStream closed = new ClosedAudioStream();

        service.spotKeyword(service.loadModel("hey_jarvis"), closed, false, listener, new AtomicBoolean(false));

        assertEquals(List.of(), listener.eventTypes());
    }

    @Test
    public void findsBundledModelsByWakeWord() throws Exception {
        assertEquals("Hey Jarvis", service.loadModel(" Hey, Jarvis! ").getWakeWord());
        assertEquals("Alexa", service.loadModel("ALEXA").getWakeWord());
        // only letters and digits of the keyword are used, so it can't name a path outside the model folder
        assertEquals("Hey Mycroft", service.loadModel("../hey_mycroft").getWakeWord());
    }

    @Test
    public void prefersModelsInTheModelFolder() throws Exception {
        writeModel("hey_jarvis", "Custom Jarvis");

        assertEquals("Custom Jarvis", service.loadModel("Hey Jarvis").getWakeWord());
    }

    @Test
    public void findsModelsOfWakeWordsInAnyScript() throws Exception {
        writeModel("привет_джарвис", "Привет, Джарвис");

        assertEquals("Привет, Джарвис", service.loadModel("Привет, Джарвис!").getWakeWord());
    }

    @Test
    public void rejectsUnknownKeywords() {
        assertThrows(KSException.class, () -> service.loadModel("ok openhab"));
        assertThrows(KSException.class, () -> service.loadModel(""));
    }

    @Test
    public void rejectsInvalidModels() throws Exception {
        Files.writeString(modelFolder.resolve("broken.json"), "{");

        assertThrows(KSException.class, () -> service.loadModel("broken"));
    }

    @Test
    public void rejectsUnsupportedFormats() {
        AudioFormat format = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null,
                8000L, 1);
        AudioStream stream = new ByteArrayAudioStream(new byte[0], format);

        assertThrows(KSException.class, () -> service.spot(listener, stream, Locale.ENGLISH, "hey jarvis"));
    }

    /**
     * Writes a model named {@code id} to the model folder, using the network of a bundled model.
     */
    private void writeModel(String id, String wakeWord) throws IOException {
        try (InputStream model = WakeWordModel.class.getResourceAsStream("models/hey_mycroft.tflite")) {
            assertNotNull(model);
            Files.write(modelFolder.resolve(id + ".tflite"), model.readAllBytes());
        }
        Files.writeString(modelFolder.resolve(id + ".json"), """
                {
                  "type": "micro",
                  "wake_word": "%s",
                  "model": "%s.tflite",
                  "version": 2,
                  "micro": {"probability_cutoff": 0.9, "sliding_window_size": 5}
                }
                """.formatted(wakeWord, id));
    }

    private static AudioStream wave() throws IOException {
        return new ByteArrayAudioStream(wavBytes(), WAVE_FORMAT);
    }

    private static byte[] pcmBytes() throws IOException {
        byte[] wav = wavBytes();
        return Arrays.copyOfRange(wav, WAV_HEADER_SIZE, wav.length);
    }

    private static byte[] wavBytes() throws IOException {
        try (InputStream in = MicroWakeWordKSServiceTest.class.getResourceAsStream("/hey_jarvis.wav")) {
            assertNotNull(in);
            return in.readAllBytes();
        }
    }

    private static class EventRecorder implements KSListener {
        private final List<KSEvent> events = new CopyOnWriteArrayList<>();
        private final CountDownLatch spotted = new CountDownLatch(1);

        @Override
        public void ksEventReceived(KSEvent ksEvent) {
            events.add(ksEvent);
            if (ksEvent instanceof KSpottedEvent) {
                spotted.countDown();
            }
        }

        @Override
        public void dtEventReceived(DTEvent dtEvent) {
            if (dtEvent instanceof KSEvent ksEvent) {
                ksEventReceived(ksEvent);
            }
        }

        List<Class<?>> eventTypes() {
            return events.stream().<Class<?>> map(Object::getClass).toList();
        }
    }

    private static class ClosedAudioStream extends AudioStream {
        @Override
        public AudioFormat getFormat() {
            return RAW_FORMAT;
        }

        @Override
        public int read() throws IOException {
            throw new IOException("Pipe closed");
        }
    }

    /**
     * Endless silence, which remembers the thread that reads it.
     */
    private static class SilenceAudioStream extends AudioStream {
        private final CountDownLatch reading = new CountDownLatch(1);
        private volatile @Nullable Thread reader;

        @Override
        public AudioFormat getFormat() {
            return RAW_FORMAT;
        }

        @Override
        public int read() {
            reader = Thread.currentThread();
            reading.countDown();
            return 0;
        }

        Thread awaitReader() throws InterruptedException {
            assertTrue(reading.await(30, TimeUnit.SECONDS));
            return Objects.requireNonNull(reader);
        }
    }

    /**
     * Counts the bytes read from another stream.
     */
    private static class CountingAudioStream extends AudioStream {
        private final AudioStream stream;
        private long bytesRead;

        CountingAudioStream(AudioStream stream) {
            this.stream = stream;
        }

        @Override
        public AudioFormat getFormat() {
            return stream.getFormat();
        }

        @Override
        public int read() throws IOException {
            int value = stream.read();
            if (value >= 0) {
                bytesRead++;
            }
            return value;
        }
    }
}
