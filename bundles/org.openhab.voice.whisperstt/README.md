# Whisper Speech-to-Text

Whisper STT Service uses [whisper.cpp](https://github.com/ggerganov/whisper.cpp) to perform offline speech-to-text in openHAB.
It also uses [libfvad](https://github.com/dpirch/libfvad) for voice activity detection to isolate single command to transcribe, speeding up the execution.

[Whisper.cpp](https://github.com/ggerganov/whisper.cpp) is a high-optimized lightweight c++ implementation of [whisper](https://github.com/openai/whisper) that allows to easily integrate it in different platforms and applications.

Alternatively, if you do not want to perform speech-to-text on the computer hosting openHAB, this add-on can consume an OpenAI/Whisper compatible transcription API.

Whisper enables speech recognition for multiple languages and dialects:

english, chinese, german, spanish, russian, korean, french, japanese, portuguese, turkish, polish, catalan, dutch, arabic, swedish,
italian, indonesian, hindi, finnish, vietnamese, hebrew, ukrainian, greek, malay, czech, romanian, danish, hungarian, tamil, norwegian,
thai, urdu, croatian, bulgarian, lithuanian, latin, maori, malayalam, welsh, slovak, telugu, persian, latvian, bengali, serbian, azerbaijani,
slovenian, kannada, estonian, macedonian, breton, basque, icelandic, armenian, nepali, mongolian, bosnian, kazakh, albanian, swahili, galician,
marathi, punjabi, sinhala, khmer, shona, yoruba, somali, afrikaans, occitan, georgian, belarusian, tajik, sindhi, gujarati, amharic, yiddish, lao,
uzbek, faroese, haitian, pashto, turkmen, nynorsk, maltese, sanskrit, luxembourgish, myanmar, tibetan, tagalog, malagasy, assamese, tatar, lingala,
hausa, bashkir, javanese and sundanese.

## Local mode (offline)

### Supported platforms

This add-on uses some native binaries to work when performing offline recognition.
You can find here the used [whisper.cpp Java wrapper](https://github.com/GiviMAD/whisper-jni) and [libfvad Java wrapper](https://github.com/GiviMAD/libfvad-jni).

The following platforms are supported:

- Windows10 x86_64
- Debian GLIBC x86_64/arm64 (min GLIBC version 2.31 / min Debian version Focal)
- macOS x86_64/arm64 (min version v11.0)

The native binaries for those platforms are included in this add-on provided with the openHAB distribution.

### CPU compatibility

To use this binding it's recommended to use a device at least as powerful as the RaspberryPI 5 with a modern CPU.
The execution times on Raspberry PI 4 are x2, so just the tiny model can be run on under 5 seconds.

If you are going to use the binding in a `x86_64` host the CPU should support the flags: `avx2`, `fma`, `f16c`, `avx`.
You can check those flags on linux using the terminal with `lscpu`.
You can check those flags on Windows using a program like `CPU-Z`.

If you are going to use the binding in a `arm64` host the CPU should support the flags: `fphp`.
You can check those flags on linux using the terminal with `lscpu`.

### Transcription time

On a Raspberry PI 5, the approximate transcription times are:

| model      | exec time |
|------------|----------:|
| tiny.bin   |      1.5s |
| base.bin   |        3s |
| small.bin  |      8.5s |
| medium.bin |       17s |

### Configuring the model

Before you can use this service you should configure your model.

You can download them from the sources provided by the [whisper.cpp](https://github.com/ggerganov/whisper.cpp) author:

- <https://huggingface.co/ggerganov/whisper.cpp>
- <https://ggml.ggerganov.com>

You should place the downloaded .bin model in '\<openHAB userdata\>/whisper/' so the add-ons can find them.

Remember to check that you have enough RAM to load the model, estimated RAM consumption can be checked on the huggingface link.

### Using alternative whisper.cpp library

It's possible to use your own build of the whisper.cpp shared library with this add-on.

On `Linux/macOs` you need to place the `libwhisper.so/libwhisper.dydib` at `/usr/local/lib/`.

On `Windows` the `whisper.dll` file needs to be placed in any directory listed at the variable `$env:PATH`, for example `X:\\Windows\System32\`.

In the [Whisper.cpp](https://github.com/ggerganov/whisper.cpp) README you can find information about the required flags to enable different acceleration methods on the cmake build and other relevant information.

Note: You need to restart openHAB to reload the library.

### Grammar

The whisper.cpp library allows to define a grammar to alter the transcription results without fine-tuning the model.

Internally whisper works by inferring a matrix of possible tokens from the audio and then resolving the final transcription from it using either the Greedy or Bean Search algorithm.
The grammar feature allows you to modify the probabilities of the inferred tokens by adding a penalty to the tokens outside the grammar so that the transcription gets resolved in a different way.

It's a way to get the smallest models to perform better over a limited grammar.

The grammar should be defined using [BNF](https://en.wikipedia.org/wiki/Backus–Naur_form), and the root variable should resolve the full grammar.
It allows using regex and optional parts to make it more dynamic.

This is a basic grammar example:

```BNF
root ::= (light_switch | light_state | tv_channel) "."
light_switch ::= "turn the light " ("on" | "off")
light_state ::= "set light to " ("high" | "low")
tv_channel ::= ("set ")? "tv channel to " [0-9]+
```

You can provide the grammar and enable its usage using the binding configuration.

## API mode

You can also use this add-on with a remote API that is compatible with the 'transcription' API from OpenAI. Online services exposing such an API may require an API key (paid services, such as OpenAI).

You can host you own compatible service elsewhere on your network, with third-party software such as faster-whisper-server.

Please note that API mode also uses libvfad for voice activity detection, and that grammar parameters are not available.

## Configuration

Use your favorite configuration UI to edit the Whisper settings:

### Speech to Text Configuration

General options.

- **Mode : LOCAL or API** - Choose either local computation or remote API use.
- **Model Name** - Model name. The 'ggml-' prefix and '.bin' extension are optional here but required on the filename. (ex: tiny.en -> ggml-tiny.en.bin)
- **Preload Model** - Keep whisper model loaded.
- **Single Utterance Mode** - When enabled recognition stops listening after a single utterance.
- **Min Transcription Seconds** - Forces min audio duration passed to whisper, in seconds.
- **Max Transcription Seconds** - Max seconds for force trigger the transcription, without wait for detect silence.
- **Initial Silence Seconds** - Max seconds without any voice activity to abort the transcription.
- **Max Silence Seconds** - Max consecutive silence seconds to trigger the transcription.
- **Remove Silence** - Remove start and end silence from the audio to transcribe.

### Voice Activity Detection Configuration

Configure VAD options.

- **Audio Step** - Audio processing step in seconds for the voice activity detection.
- **Voice Activity Detection Mode** - Selected VAD Mode.
- **Voice Activity Detection Sensitivity** - Percentage in range 0-1 of voice activity in one second to consider it as voice.
- **Voice Activity Detection Step** - VAD detector internal step in ms (only allows 10, 20 or 30). (Audio Step / Voice Activity Detection Step = number of vad executions per audio step).

### Whisper Configuration

Configure whisper options.

- **Threads** - Number of threads used by whisper. (0 to use host max threads)
- **Sampling Strategy** - Sampling strategy used.
- **Beam Size** - Beam Size configuration for sampling strategy Bean Search.
- **Greedy Best Of** - Best Of configuration for sampling strategy Greedy.
- **Speed Up** - Speed up audio by x2. (Reduced accuracy)
- **Audio Context** - Overwrite the audio context size. (0 to use whisper default context size)
- **Temperature** - Temperature threshold.
- **Initial Prompt** - Initial prompt for whisper.
- **OpenVINO Device** - Initialize OpenVINO encoder. (built-in binaries do not support OpenVINO, this has no effect)
- **Use GPU** - Enables GPU usage. (built-in binaries do not support GPU usage, this has no effect)
- **Language** - If specified, speed up recognition by avoiding auto-detection. Default to system locale.

### API Configuration

- **API key** - Optional use of an API key for online services requiring it.
- **API url** - You may use your own service and define its URL here. Default set to OpenAI transcription API.
- **API model name** - Your hosted service may have other models. Default to OpenAI only model 'whisper-1'.

### Grammar Configuration

Configure the grammar options.

- **Grammar** - Grammar to use in GBNF format (whisper.cpp BNF variant).
- **Use Grammar** - Enable grammar usage.
- **Grammar penalty** - Penalty for non grammar tokens.

#### Grammar Example

```gbnf
# Grammar should define a root expression that should end with a dot.
root     ::= " " command "."
# Alternative command expression to expand into the root.
command  ::= "Turn " onoff " " (connector)? thing |
             put " " thing " to " state |
             watch " " show " at bedroom" |
             "Start " timer " minutes timer"

# You can use as many expressions as you need.

thing   ::= "light" | "bedroom light" | "living room light" | "tv"

put     ::= "set" | "put"

onoff  ::= "on" | "off"

watch   ::= "watch" | "play"

connector ::= "the"

state   ::= "low" | "high" | "normal"

show  ::= [a-zA-Z]+

timer ::= [0-9]+

```

### Messages Configuration

- **No Results Message** - Message to be told on no results.
- **Error Message** - Message to be told on exception.

### Developer Configuration

- **Create WAV Record** - Create wav audio file on each whisper execution, also creates a '.prop' file containing the transcription.
- **Record Sample Format** - Change the record sample format. (allows i16 or f32)
- **Enable Whisper Log** - Emit whisper.cpp library logs as add-on debug logs.

You can find information on how to fine-tune a model using the generated records at [givimad’s GitHub repository](https://github.com/givimad/whisper-finetune-oh).

### Configuration via a text file

In case you would like to set up the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `whisperstt.cfg`

Its contents should look similar to:

```ini
org.openhab.voice.whisperstt:mode=LOCAL
org.openhab.voice.whisperstt:modelName=tiny
org.openhab.voice.whisperstt:language=en
org.openhab.voice.whisperstt:initSilenceSeconds=0.3
org.openhab.voice.whisperstt:removeSilence=true
org.openhab.voice.whisperstt:stepSeconds=0.3
org.openhab.voice.whisperstt:vadStep=0.5
org.openhab.voice.whisperstt:singleUtteranceMode=true
org.openhab.voice.whisperstt:preloadModel=false
org.openhab.voice.whisperstt:vadMode=LOW_BITRATE
org.openhab.voice.whisperstt:vadSensitivity=0.1
org.openhab.voice.whisperstt:maxSilenceSeconds=2
org.openhab.voice.whisperstt:minSeconds=2
org.openhab.voice.whisperstt:maxSeconds=10
org.openhab.voice.whisperstt:threads=0
org.openhab.voice.whisperstt:audioContext=0
org.openhab.voice.whisperstt:samplingStrategy=GREEDY
org.openhab.voice.whisperstt:temperature=0
org.openhab.voice.whisperstt:noResultsMessage="Sorry, I didn't understand you"
org.openhab.voice.whisperstt:errorMessage="Sorry, something went wrong"
org.openhab.voice.whisperstt:createWAVRecord=false
org.openhab.voice.whisperstt:recordSampleFormat=i16
org.openhab.voice.whisperstt:speedUp=false
org.openhab.voice.whisperstt:beamSize=4
org.openhab.voice.whisperstt:enableWhisperLog=false
org.openhab.voice.whisperstt:greedyBestOf=4
org.openhab.voice.whisperstt:initialPrompt=
org.openhab.voice.whisperstt:openvinoDevice=""
org.openhab.voice.whisperstt:useGPU=false
org.openhab.voice.whisperstt:useGrammar=false
org.openhab.voice.whisperstt:grammarPenalty=80.0
org.openhab.voice.whisperstt:grammarLines=
org.openhab.voice.whisperstt:apiKey=mykeyaaaa
org.openhab.voice.whisperstt:apiUrl=https://api.openai.com/v1/audio/transcriptions
org.openhab.voice.whisperstt:apiModelName=whisper-1
```

### Default Speech-to-Text Configuration

You can select your preferred default Speech-to-Text in the UI:

- Go to **Settings**.
- Edit **System Services - Voice**.
- Set **Whisper** as **Speech-to-Text**.

In case you would like to set up these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```ini
org.openhab.voice:defaultSTT=whisperstt
```
