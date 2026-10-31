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
package org.openhab.voice.parakeetstt.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.voice.parakeetstt.internal.ParakeetSTTConstants.*;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.ByteArrayAudioStream;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.voice.stt.RecognitionStartEvent;
import org.openhab.core.voice.stt.RecognitionStopEvent;
import org.openhab.core.voice.stt.STTEvent;
import org.openhab.core.voice.stt.STTException;
import org.openhab.core.voice.stt.STTListener;
import org.openhab.core.voice.stt.SpeechRecognitionErrorEvent;
import org.openhab.core.voice.stt.SpeechRecognitionEvent;

import io.github.jvoiceproject.parakeet.internal.NativeLibraryLoader;
import io.github.jvoiceproject.parakeet.internal.ParakeetBinding;

/**
 * Unit tests for {@link ParakeetSTTService}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
class ParakeetSTTServiceTest {
    private static final AudioFormat RAW_PCM_FORMAT = new AudioFormat(AudioFormat.CONTAINER_NONE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, null, (long) SAMPLE_RATE, 1);

    private @TempDir @NonNullByDefault({}) Path tempDir;
    private @NonNullByDefault({}) FakeParakeetBinding fakeBinding;
    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) ParakeetSTTService service;

    @BeforeEach
    void setUp() {
        fakeBinding = new FakeParakeetBinding();
        NativeLibraryLoader.setInstance(fakeBinding);

        localeProvider = mock(LocaleProvider.class);
        when(localeProvider.getLocale()).thenReturn(Locale.ENGLISH);
        service = new ParakeetSTTService(localeProvider, tempDir);
    }

    @AfterEach
    void tearDown() {
        service.deactivate();
    }

    private Path createDummyModelFile(String filename) throws IOException {
        Path path = tempDir.resolve(filename);
        Files.writeString(path, "dummy-gguf");
        return path;
    }

    private static AudioStream createAudioStream(int numSamples) {
        byte[] bytes = new byte[numSamples * Short.BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < numSamples; i++) {
            buffer.putShort((short) 1000);
        }
        return new ByteArrayAudioStream(bytes, RAW_PCM_FORMAT);
    }

    @Test
    void getParameterOptionsScansParakeetFolderForGgufFiles() throws IOException {
        createDummyModelFile("parakeet-tdt-0.6b.gguf");
        createDummyModelFile("silero-vad-f16.gguf");
        createDummyModelFile("readme.txt");

        URI uri = URI.create(CONFIG_URI);
        Collection<ParameterOption> modelOptions = service.getParameterOptions(uri, PARAM_MODEL_NAME, null,
                Locale.ENGLISH);
        assertNotNull(modelOptions);
        List<String> values = modelOptions.stream().map(ParameterOption::getValue).toList();
        assertEquals(List.of("parakeet-tdt-0.6b.gguf", "silero-vad-f16.gguf"), values);

        Collection<ParameterOption> vadOptions = service.getParameterOptions(uri, PARAM_VAD_MODEL_NAME, null,
                Locale.ENGLISH);
        assertNotNull(vadOptions);
        assertEquals(List.of("parakeet-tdt-0.6b.gguf", "silero-vad-f16.gguf"),
                vadOptions.stream().map(ParameterOption::getValue).toList());

        assertNull(service.getParameterOptions(uri, "otherParam", null, Locale.ENGLISH));
        assertNull(service.getParameterOptions(URI.create("voice:other"), PARAM_MODEL_NAME, null, Locale.ENGLISH));
    }

    @Test
    void getSupportedLocalesAndFormats() {
        service.activate(Map.of());
        assertEquals(Set.of(Locale.ENGLISH), service.getSupportedLocales());

        service.modified(Map.of("language", "de"));
        assertEquals(Set.of(Locale.GERMAN), service.getSupportedLocales());

        assertEquals(2, service.getSupportedFormats().size());
        AudioFormat unsupported = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, false, 16, null,
                16000L, 1);
        STTListener listener = event -> {
        };
        assertThrows(STTException.class,
                () -> service.recognize(listener, new ByteArrayAudioStream(new byte[0], unsupported), Locale.ENGLISH));
    }

    @Test
    void streamingModeRecognizesAndFlushesOnEou() throws Exception {
        createDummyModelFile("streaming-model.gguf");
        fakeBinding.streamingSupported = true;
        fakeBinding.streamFeedJsonResponses.add("{\"text\":\"Turn on\",\"eou\":false,\"eob\":false,\"frame_sec\":0.1}");
        fakeBinding.streamFeedJsonResponses
                .add("{\"text\":\"the light\",\"eou\":true,\"eob\":false,\"frame_sec\":0.2}");
        fakeBinding.streamFinishJson = "{\"text\":\".\",\"eou\":true,\"eob\":false,\"frame_sec\":0.2}";

        service.activate(Map.of( //
                "modelName", "streaming-model.gguf", //
                "mode", "STREAMING", //
                "stepSeconds", 0.1, //
                "singleUtteranceMode", true, //
                "backends", 2, //
                "threads", 6));

        RecordingListener listener = new RecordingListener();
        // Provide 4 steps of audio (0.4s); singleUtteranceMode should stop after step 2 when eou=true
        try (AudioStream audioStream = createAudioStream(SAMPLE_RATE * 4 / 10)) {
            service.recognize(listener, audioStream, Locale.ENGLISH);
            assertTrue(listener.awaitTerminalEvent());
        }

        assertThat(listener.events.getFirst(), instanceOf(RecognitionStartEvent.class));
        assertThat(listener.events.get(1), instanceOf(RecognitionStopEvent.class));
        assertThat(listener.events.get(2), instanceOf(SpeechRecognitionEvent.class));
        SpeechRecognitionEvent speechEvent = (SpeechRecognitionEvent) listener.events.get(2);
        assertThat(speechEvent.getTranscript(), is("Turn on the light ."));
        assertThat(fakeBinding.streamFeedCount.get(), is(2));
        assertThat(fakeBinding.lastBackends, is(2));
        assertThat(fakeBinding.lastThreads, is(6));
    }

    @Test
    void offlineModeSegmentsWithVadAndBatchTranscribes() throws Exception {
        createDummyModelFile("offline-asr.gguf");
        createDummyModelFile("silero-vad.gguf");
        fakeBinding.streamingSupported = false;
        fakeBinding.vadFeedJsonResponses.add(
                "{\"frame_sec\":0.032,\"first_frame\":0,\"events\":[{\"type\":\"start\",\"time\":0.0}],\"probabilities\":[]}");
        fakeBinding.vadFeedJsonResponses.add(
                "{\"frame_sec\":0.032,\"first_frame\":3,\"events\":[{\"type\":\"end\",\"time\":0.2}],\"probabilities\":[]}");
        fakeBinding.batchTranscript = "Kitchen light off";

        service.activate(Map.of( //
                "modelName", "offline-asr", //
                "vadModelName", "silero-vad", //
                "mode", "OFFLINE", //
                "decoder", "TRANSDUCER", //
                "language", "en", //
                "stepSeconds", 0.1, //
                "singleUtteranceMode", true));

        RecordingListener listener = new RecordingListener();
        try (AudioStream audioStream = createAudioStream(SAMPLE_RATE * 3 / 10)) {
            service.recognize(listener, audioStream, Locale.ENGLISH);
            assertTrue(listener.awaitTerminalEvent());
        }

        assertThat(listener.events.size(), is(3));
        SpeechRecognitionEvent speechEvent = (SpeechRecognitionEvent) listener.events.get(2);
        assertThat(speechEvent.getTranscript(), is("Kitchen light off"));
        assertThat(fakeBinding.batchTranscribeCalls.get(), is(1));
        assertThat(fakeBinding.lastDecoder, is(2));
        assertThat(fakeBinding.lastTargetLang, is("en"));
    }

    @Test
    void autoModeFallsBackToOfflineAndUsesBundledVadComponent() throws Exception {
        createDummyModelFile("bundle-model.gguf");
        fakeBinding.streamingSupported = false;
        fakeBinding.bundleComponentsJson = "[{\"name\":\"asr\",\"kind\":\"asr\",\"offset\":0,\"size\":100},{\"name\":\"vad\",\"kind\":\"vad\",\"offset\":100,\"size\":50}]";
        fakeBinding.vadFeedJsonResponses.add(
                "{\"frame_sec\":0.032,\"first_frame\":0,\"events\":[{\"type\":\"start\",\"time\":0.0},{\"type\":\"end\",\"time\":0.1}],\"probabilities\":[]}");
        fakeBinding.batchTranscript = "Set temperature to twenty";

        service.activate(Map.of( //
                "modelName", "bundle-model.gguf", //
                "mode", "AUTO", //
                "stepSeconds", 0.1));

        RecordingListener listener = new RecordingListener();
        try (AudioStream audioStream = createAudioStream(SAMPLE_RATE / 10)) {
            service.recognize(listener, audioStream, Locale.ENGLISH);
            assertTrue(listener.awaitTerminalEvent());
        }

        assertThat(listener.events.size(), is(3));
        SpeechRecognitionEvent speechEvent = (SpeechRecognitionEvent) listener.events.get(2);
        assertThat(speechEvent.getTranscript(), is("Set temperature to twenty"));
        assertThat(fakeBinding.bundleComponentLoadCount.get(), is(1));
    }

    @Test
    void preloadModelReusesLoadedModelAcrossRecognitions() throws Exception {
        createDummyModelFile("preloaded.gguf");
        fakeBinding.streamingSupported = true;
        fakeBinding.streamFinishJson = "{\"text\":\"Hello\",\"eou\":true,\"eob\":false,\"frame_sec\":0.1}";

        service.activate(Map.of( //
                "modelName", "preloaded.gguf", //
                "preloadModel", true, //
                "mode", "STREAMING", //
                "stepSeconds", 0.1));

        assertThat(fakeBinding.modelInitCount.get(), is(1));
        assertThat(fakeBinding.modelFreeCount.get(), is(0));

        RecordingListener listener = new RecordingListener();
        try (AudioStream audioStream = createAudioStream(SAMPLE_RATE / 10)) {
            service.recognize(listener, audioStream, Locale.ENGLISH);
            assertTrue(listener.awaitTerminalEvent());
        }

        assertThat(fakeBinding.modelInitCount.get(), is(1));
        assertThat(fakeBinding.modelFreeCount.get(), is(0));

        service.deactivate();
        assertThat(fakeBinding.modelFreeCount.get(), is(1));
    }

    @Test
    void initialSilenceTimeoutEmitsErrorEvent() throws Exception {
        createDummyModelFile("silence-model.gguf");
        fakeBinding.streamingSupported = true;

        service.activate(Map.of( //
                "modelName", "silence-model.gguf", //
                "mode", "STREAMING", //
                "stepSeconds", 0.1, //
                "initSilenceSeconds", 0.2));

        RecordingListener listener = new RecordingListener();
        try (AudioStream audioStream = createAudioStream(SAMPLE_RATE / 2)) {
            service.recognize(listener, audioStream, Locale.ENGLISH);
            assertTrue(listener.awaitTerminalEvent());
        }

        assertThat(listener.events.getFirst(), instanceOf(RecognitionStartEvent.class));
        assertThat(listener.events.get(1), instanceOf(RecognitionStopEvent.class));
        assertThat(listener.events.get(2), instanceOf(SpeechRecognitionErrorEvent.class));
    }

    private static class RecordingListener implements STTListener {
        final List<STTEvent> events = new CopyOnWriteArrayList<>();
        final CountDownLatch terminalLatch = new CountDownLatch(1);

        @Override
        public void sttEventReceived(STTEvent sttEvent) {
            events.add(sttEvent);
            if (sttEvent instanceof SpeechRecognitionEvent || sttEvent instanceof SpeechRecognitionErrorEvent) {
                terminalLatch.countDown();
            }
        }

        boolean awaitTerminalEvent() throws InterruptedException {
            return terminalLatch.await(5, TimeUnit.SECONDS);
        }
    }

    @NonNullByDefault({})
    @SuppressWarnings("preview")
    private static class FakeParakeetBinding implements ParakeetBinding {
        private static final long ASR_CTX_ADDR = 0x1000L;
        private static final long VAD_CTX_ADDR = 0x2000L;
        private static final int MODEL_KIND_ASR = 1;
        private static final int MODEL_KIND_VAD = 5;

        final AtomicInteger modelInitCount = new AtomicInteger();
        final AtomicInteger modelFreeCount = new AtomicInteger();
        final AtomicInteger bundleComponentLoadCount = new AtomicInteger();
        final AtomicInteger streamFeedCount = new AtomicInteger();
        final AtomicInteger batchTranscribeCalls = new AtomicInteger();
        final List<String> streamFeedJsonResponses = new CopyOnWriteArrayList<>();
        final List<String> vadFeedJsonResponses = new CopyOnWriteArrayList<>();

        volatile boolean streamingSupported = true;
        volatile String streamFinishJson = "{\"text\":\"\",\"eou\":false,\"eob\":false,\"frame_sec\":0.0}";
        volatile String batchTranscript = "";
        volatile @Nullable String bundleComponentsJson = null;
        volatile int lastBackends = -1;
        volatile int lastThreads = -1;
        volatile int lastDecoder = -1;
        volatile @Nullable String lastTargetLang = null;

        @Override
        public int parakeet_capi_abi_version() {
            return 10;
        }

        @Override
        public String parakeet_version() {
            return "0.6.1-test";
        }

        @Override
        public MemorySegment parakeet_capi_load(String ggufPath) {
            modelInitCount.incrementAndGet();
            return MemorySegment.ofAddress(ggufPath.contains("vad") ? VAD_CTX_ADDR : ASR_CTX_ADDR);
        }

        @Override
        public MemorySegment parakeet_capi_load_component(String ggufPath, String component) {
            bundleComponentLoadCount.incrementAndGet();
            return MemorySegment.ofAddress("vad".equalsIgnoreCase(component) ? VAD_CTX_ADDR : ASR_CTX_ADDR);
        }

        @Override
        public @Nullable String parakeet_capi_bundle_components_json(String ggufPath) {
            return bundleComponentsJson;
        }

        @Override
        public void parakeet_capi_free(MemorySegment ctx) {
            modelFreeCount.incrementAndGet();
        }

        @Override
        public int parakeet_capi_model_kind(MemorySegment ctx) {
            return ctx.address() == VAD_CTX_ADDR ? MODEL_KIND_VAD : MODEL_KIND_ASR;
        }

        @Override
        public String parakeet_capi_last_error(MemorySegment ctx) {
            return "fake error";
        }

        @Override
        public String parakeet_capi_load_error() {
            return "fake load error";
        }

        @Override
        public int parakeet_capi_set_concurrency(MemorySegment ctx, int backends, int threadsEach) {
            this.lastBackends = backends;
            this.lastThreads = threadsEach;
            return backends;
        }

        @Override
        public @Nullable String parakeet_capi_transcribe_path_lang(MemorySegment ctx, String wavPath, int decoder,
                @Nullable String targetLang) {
            this.lastDecoder = decoder;
            this.lastTargetLang = targetLang;
            return batchTranscript;
        }

        @Override
        public @Nullable String parakeet_capi_transcribe_pcm_lang(MemorySegment ctx, float[] pcm, int nSamples,
                int sampleRate, int decoder, @Nullable String targetLang) {
            batchTranscribeCalls.incrementAndGet();
            this.lastDecoder = decoder;
            this.lastTargetLang = targetLang;
            return batchTranscript;
        }

        @Override
        public @Nullable String parakeet_capi_transcribe_path_json(MemorySegment ctx, String wavPath, int decoder) {
            return "{\"text\":\"" + batchTranscript + "\",\"words\":[]}";
        }

        @Override
        public MemorySegment parakeet_capi_stream_begin_lang(MemorySegment ctx, @Nullable String targetLang) {
            this.lastTargetLang = targetLang;
            if (!streamingSupported) {
                return MemorySegment.NULL;
            }
            return MemorySegment.ofAddress(0x3000L);
        }

        @Override
        public @Nullable String parakeet_capi_stream_feed_json(MemorySegment stream, float[] pcm, int nSamples) {
            int idx = streamFeedCount.getAndIncrement();
            if (idx < streamFeedJsonResponses.size()) {
                return streamFeedJsonResponses.get(idx);
            }
            return "{\"text\":\"\",\"eou\":false,\"eob\":false,\"frame_sec\":0.1}";
        }

        @Override
        public @Nullable String parakeet_capi_stream_finalize_json(MemorySegment stream) {
            return streamFinishJson;
        }

        @Override
        public void parakeet_capi_stream_free(MemorySegment stream) {
        }

        @Override
        public @Nullable String parakeet_capi_vad_pcm_json(MemorySegment ctx, float[] pcm, int nSamples, int sampleRate,
                String optionsJson) {
            return "{\"events\":[]}";
        }

        @Override
        public @Nullable String parakeet_capi_vad_path_json(MemorySegment ctx, String wavPath, String optionsJson) {
            return "{\"events\":[]}";
        }

        @Override
        public @Nullable String parakeet_capi_transcribe_path_json_vad_with(MemorySegment asrCtx, MemorySegment vadCtx,
                String wavPath, int decoder, String vadOptsJson) {
            return "{\"text\":\"" + batchTranscript + "\",\"words\":[]}";
        }

        @Override
        public MemorySegment parakeet_capi_vad_stream_begin(MemorySegment vadCtx, int sampleRate, String optionsJson) {
            return MemorySegment.ofAddress(0x4000L);
        }

        @Override
        public @Nullable String parakeet_capi_vad_stream_feed_json(MemorySegment vadStream, float[] pcm, int nSamples,
                int isLast) {
            if (!vadFeedJsonResponses.isEmpty()) {
                return vadFeedJsonResponses.removeFirst();
            }
            return "{\"frame_sec\":0.032,\"first_frame\":0,\"events\":[],\"probabilities\":[]}";
        }

        @Override
        public int parakeet_capi_vad_stream_reset(MemorySegment vadStream) {
            return 0;
        }

        @Override
        public void parakeet_capi_vad_stream_free(MemorySegment vadStream) {
        }
    }
}
