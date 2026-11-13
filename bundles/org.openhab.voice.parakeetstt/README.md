# Parakeet Speech-to-Text

The Parakeet Speech-to-Text service uses [parakeet.cpp](https://github.com/mudler/parakeet.cpp) via [parakeet-ffm](https://github.com/jvoice-project/parakeet-ffm) to perform local, offline speech-to-text in openHAB using NVIDIA Parakeet and NeMo models in GGUF format.

It supports both **streaming** transcription (for streaming-capable models such as Parakeet Realtime EOU, Nemotron Speech, Sortformer, Canary, and streaming TDT/CTC models) and **offline** VAD-segmented batch transcription using built-in Silero Voice Activity Detection (VAD).

## Supported Platforms and Runtime Requirements

### Supported Platforms

Native binaries for the following platforms are included in the add-on:

- Linux `x86_64` and `aarch64` (`arm64`)
- macOS `x86_64` and `arm64` (Apple Silicon)
- Windows `x86_64`

### Java Runtime Flags

This add-on uses the Java Foreign Function & Memory (FFM) API (`java.lang.foreign`) to invoke the native `parakeet.cpp` library:

- **Java 21**: FFM is a preview API in Java 21, so the openHAB JVM must be started with `--enable-preview --enable-native-access=ALL-UNNAMED` (for example, via `EXTRA_JAVA_OPTS` in `/etc/default/openhab`).
- **Java 22+**: FFM is finalized, and `--enable-native-access=ALL-UNNAMED` is recommended to avoid restricted native access warnings.

## Model Setup

Place your `.gguf` model files in `$OPENHAB_USERDATA/parakeet/` (for example `/var/lib/openhab/parakeet/` on Linux package installations).
The add-on automatically scans `$OPENHAB_USERDATA/parakeet/` for `.gguf` files and lists them as selectable options in the UI.

Pre-converted GGUF models (including Parakeet TDT, CTC, RNNT, Realtime EOU, Nemotron, Canary, and Silero VAD, as well as multi-component bundle GGUFs) can be downloaded from [HuggingFace](https://huggingface.co/mudler/parakeet-cpp-gguf) or converted using the conversion scripts in [parakeet.cpp](https://github.com/mudler/parakeet.cpp/blob/master/docs/conversion.md).

### VAD Model Setup

- **Offline Mode (`OFFLINE`)** requires a Silero VAD model to detect and segment speech utterances before batch transcription.
  - If your `modelName` is a **bundle GGUF** that already includes a `vad` component, the add-on automatically loads the bundled `vad` component when `vadModelName` is left empty.
  - If your `modelName` is a standalone ASR `.gguf` file, also place a Silero VAD `.gguf` file (for example `silero-vad-f16.gguf`) in `$OPENHAB_USERDATA/parakeet/` and select it in `vadModelName`.
- **Streaming Mode (`STREAMING`)** feeds audio chunks directly to the streaming ASR model and can stop on End-of-Utterance (`<EOU>`) events or silence timeouts, optionally enhanced by VAD if a VAD model is configured or bundled.
- **Automatic Mode (`AUTO`, default)** uses streaming transcription if the configured ASR model supports it and automatically falls back to offline VAD-segmented batch transcription otherwise.

## Configuration

### Model

| Parameter      | Type    | Default  | Description                                                                                                |
|----------------|---------|----------|------------------------------------------------------------------------------------------------------------|
| `modelName`    | text    | required | ASR model or bundle `.gguf` filename in `$OPENHAB_USERDATA/parakeet/` (the `.gguf` extension is optional). |
| `preloadModel` | boolean | `false`  | Keep the ASR and VAD models loaded in memory across recognition sessions instead of loading on each run.   |

### Transcription Behavior (Advanced)

| Parameter             | Type    | Default   | Description                                                                                                                               |
|-----------------------|---------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `mode`                | text    | `AUTO`    | Transcription mode: `AUTO` (streaming if supported, offline otherwise), `OFFLINE` (VAD + batch), or `STREAMING`.                          |
| `decoder`             | text    | `DEFAULT` | Decoder override for offline transcription (`DEFAULT`, `CTC`, `TRANSDUCER`).                                                              |
| `language`            | text    |           | Target language code (e.g. `en`, `de`, `es`, `fr`) for models supporting language selection. Defaults to the recognition locale if empty. |
| `singleUtteranceMode` | boolean | `true`    | Stop listening as soon as the first utterance finishes (`<EOU>` in streaming mode or speech end in VAD).                                  |

### Audio Stream & Stopping Limits (Advanced)

| Parameter            | Type    | Default | Description                                                                                |
|----------------------|---------|---------|--------------------------------------------------------------------------------------------|
| `stepSeconds`        | decimal | `0.1`   | Audio chunk duration in seconds read from the audio stream per step.                       |
| `maxSeconds`         | integer | `10`    | Maximum duration in seconds of audio processed in a single recognition session.            |
| `initSilenceSeconds` | decimal | `5.0`   | Maximum initial silence in seconds before aborting recognition when no speech has started. |
| `maxSilenceSeconds`  | decimal | `1.0`   | Silence duration in seconds after speech that marks the end of an utterance.               |

### Voice Activity Detection (Advanced)

| Parameter          | Type    | Default | Description                                                                                                                               |
|--------------------|---------|---------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `vadModelName`     | text    |         | Standalone Silero VAD `.gguf` filename in `$OPENHAB_USERDATA/parakeet/`. Optional if `modelName` is a bundle GGUF with a `vad` component. |
| `vadThreshold`     | decimal | `0.5`   | Speech probability threshold (`0.05` - `0.95`) for Silero VAD.                                                                            |
| `minSpeechSeconds` | decimal | `0.25`  | Minimum speech duration in seconds required to trigger a VAD speech start event.                                                          |
| `speechPadSeconds` | decimal | `0.03`  | Padding in seconds added around speech segments detected by VAD.                                                                          |

### CPU Performance (Advanced)

| Parameter  | Type    | Default | Description                                                                  |
|------------|---------|---------|------------------------------------------------------------------------------|
| `threads`  | integer | `0`     | Number of CPU threads per backend instance (`0` uses the library default).   |
| `backends` | integer | `1`     | Number of parallel CPU backend instances configured on the ASR model.        |

### Configuration via a Text File

To configure the service via a text file, create `$OPENHAB_CONF/services/parakeetstt.cfg`:

```ini
# Model
org.openhab.voice.parakeetstt:modelName=parakeet-tdt-0.6b-v2-q8_0.gguf
org.openhab.voice.parakeetstt:preloadModel=true

# Transcription Behavior
org.openhab.voice.parakeetstt:mode=AUTO
org.openhab.voice.parakeetstt:decoder=DEFAULT
org.openhab.voice.parakeetstt:language=
org.openhab.voice.parakeetstt:singleUtteranceMode=true

# Audio Stream & Stopping Limits
org.openhab.voice.parakeetstt:stepSeconds=0.1
org.openhab.voice.parakeetstt:maxSeconds=10
org.openhab.voice.parakeetstt:initSilenceSeconds=5.0
org.openhab.voice.parakeetstt:maxSilenceSeconds=1.0

# Voice Activity Detection
org.openhab.voice.parakeetstt:vadModelName=silero-vad-f16.gguf
org.openhab.voice.parakeetstt:vadThreshold=0.5
org.openhab.voice.parakeetstt:minSpeechSeconds=0.25
org.openhab.voice.parakeetstt:speechPadSeconds=0.03

# CPU Performance
org.openhab.voice.parakeetstt:threads=0
org.openhab.voice.parakeetstt:backends=1
```

### Default Speech-to-Text Configuration

You can select Parakeet as your default Speech-to-Text service in Main UI:

- Go to **Settings**.
- Edit **System Settings -> Voice**.
- Set **Parakeet** as **Speech-to-Text Service**.

Alternatively, configure it in `$OPENHAB_CONF/services/runtime.cfg`:

```ini
org.openhab.voice:defaultSTT=parakeetstt
```
