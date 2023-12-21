# Whisper Speech-to-Text

Whisper STT Service uses [whisper.cpp](https://github.com/ggerganov/whisper.cpp) to perform offline speech-to-text in openHAB.
It also uses [libfvad](https://github.com/dpirch/libfvad) for voice activity detection to isolate single command to transcribe, speeding up the execution.

[Whisper.cpp](https://github.com/ggerganov/whisper.cpp) is a high-optimized lightweight c++ implementation of [whisper](https://github.com/openai/whisper) that allows to easily integrate it in different platforms and applications.

Whisper enables speech recognition for multiple languages and dialects:

english, chinese, german, spanish, russian, korean, french, japanese, portuguese, turkish, polish, catalan, dutch, arabic, swedish, 
italian, indonesian, hindi, finnish, vietnamese, hebrew, ukrainian, greek, malay, czech, romanian, danish, hungarian, tamil, norwegian, 
thai, urdu, croatian, bulgarian, lithuanian, latin, maori, malayalam, welsh, slovak, telugu, persian, latvian, bengali, serbian, azerbaijani, 
slovenian, kannada, estonian, macedonian, breton, basque, icelandic, armenian, nepali, mongolian, bosnian, kazakh, albanian, swahili, galician, 
marathi, punjabi, sinhala, khmer, shona, yoruba, somali, afrikaans, occitan, georgian, belarusian, tajik, sindhi, gujarati, amharic, yiddish, lao, 
uzbek, faroese, haitian, pashto, turkmen, nynorsk, maltese, sanskrit, luxembourgish, myanmar, tibetan, tagalog, malagasy, assamese, tatar, lingala, 
hausa, bashkir, javanese and sundanese.

## Supported platforms

This add-on uses some native binaries to work.
You can find here the used [whisper.cpp Java wrapper](https://github.com/GiviMAD/whisper-jni) and [libfvad Java wrapper](https://github.com/GiviMAD/libfvad-jni).

The following platforms are supported:

* debian-armv7l
* debian-arm64
* debian-x86_64
* macos-arm64 (+v11.0)
* macos-x86_64 (+v11.0)
* windows-x86_64

The add-on has been tested on a Raspberry PI 5 with execution times:

* tiny model ~=1.5s
* base model ~=3.5s
* small model ~=8s

## Configuring the model

Before you can use this service you should configure your model.

You can download them from the sources provided by the [whisper.cpp](https://github.com/ggerganov/whisper.cpp) author:

* https://huggingface.co/ggerganov/whisper.cpp
* https://ggml.ggerganov.com

You should place the downloaded .bin model in '\<openHAB userdata\>/whisper/' so the add-ons can find them.

Remember to check that you have enough RAM to load the model, estimated RAM consumption can be checked on the huggingface link.

## Using alternative whisper.cpp library

It's possible to use your own build of the whisper.cpp shared library with this add-on.

On `Linux/macOs` you need to place the `libwhisper.so/libwhisper.dydib` at `/usr/local/lib/`.

On `Windows` the `whisper.dll` file can be placed at in any directory listed at the variable `$env:PATH`, for example `X:\\Windows\System32\`.

Here is a sample script for building a device specific whisper.cpp shared library using cmake in debian:

```bash
#!/bin/bash
sudo apt update
sudo apt install -y git build-essential
git clone https://github.com/ggerganov/whisper.cpp
cd whisper.cpp
# Tells the compiler to optimize the build for our device cpu.
COMMON_FLAGS="-mcpu=native -march=native"

cmake -B build \
-DCMAKE_C_FLAGS="$COMMON_FLAGS" \
-DCMAKE_CXX_FLAGS="$COMMON_FLAGS"

cmake --build build -j --config Release

sudo mv build/libwhisper.so /usr/local/lib/
```

In the [Whisper.cpp](https://github.com/ggerganov/whisper.cpp) README you can find information about the required flags to enable different acceleration methods on the cmake build and other relevant information.

Note: You need to restart OpenHAB to reload the library.

## Configuration

Use your favorite configuration UI to edit the Whisper settings:

### Speech to Text Configuration

General options.

* **Model Name** - Model name. The 'ggml-' prefix and '.bin' extension are optional here but required on the filename. (ex: tiny.en -> ggml-tiny.en.bin)
* **Preload Model** - Keep whisper model loaded.
* **Single Utterance Mode** - When enabled recognition stops listening after a single utterance.
* **Max Transcription Seconds** - Max seconds for force trigger the transcription, without wait for detect silence.
* **Initial Silence Seconds** - Max seconds without any voice activity to abort the transcription.
* **Max Silence Seconds** - Max consecutive silence seconds to trigger the transcription.
* **Remove Silence** - Remove start and end silence from the audio to transcribe.

### Voice Activity Detection Configuration

Configure VAD options.

* **Audio Step** - Audio processing step in seconds for the voice activity detection.
* **Voice Activity Detection Mode** - Selected VAD Mode.
* **Voice Activity Detection Sensitivity** - Percentage in range 0-1 of voice activity in one second to consider it as voice.
* **Voice Activity Detection Step** - VAD detector internal step in ms (only allows 10, 20 or 30). (Audio Step / Voice Activity Detection Step = number of vad executions per audio step).

### Whisper Configuration

Configure whisper options.

* **Threads** - Number of threads used by whisper. (0 for default)
* **Sampling Strategy** - Sampling strategy used.
* **Beam Size** - Beam Size configuration for sampling strategy Bean Search.
* **Greedy Best Of** - Best Of configuration for sampling strategy Greedy.
* **Speed Up** - Speed up audio by x2. (Reduced accuracy)
* **Audio Context** - Overwrite the audio context size. (0 for default)
* **Temperature** - Temperature threshold.
* **Initial Prompt** - Initial prompt for whisper.

### Grammar Configuration

Configure the grammar options.

* **Grammar** - Grammar to use in GBNF format (whisper.cpp BNF variant).
* **Use Grammar** - Enable grammar usage.
* **Grammar penalty** - Penalty for non grammar tokens.

#### Simple Grammar Example:


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

* **Error Message** - Message to be told on exception.

### Developer Configuration

* **Create WAV Record** - Create wav audio file on each whisper execution, also creates a '.prop' file containing the transcription.
* **Record Sample Format** - Change the record sample format. (allows i16 or f32)

You can find [here](https://github.com/givimad/whisper-finetune-oh) information on how to fine-tune a model using the generated records.

### Configuration via a text file

In case you would like to set up the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `whisperstt.cfg`

Its contents should look similar to:

```
org.openhab.voice.whisperstt:modelName=tiny
org.openhab.voice.whisperstt:step=0.5
org.openhab.voice.whisperstt:singleUtteranceMode=true
org.openhab.voice.whisperstt:preloadModel=false
org.openhab.voice.whisperstt:vadMode=LOW_BITRATE
org.openhab.voice.whisperstt:vadSentivity=0.1
org.openhab.voice.whisperstt:maxSilenceSeconds=2
org.openhab.voice.whisperstt:minSeconds=2
org.openhab.voice.whisperstt:maxSeconds=10
org.openhab.voice.whisperstt:threads=0
org.openhab.voice.whisperstt:audioContextSize=0
org.openhab.voice.whisperstt:samplingStrategy=GREEDY
org.openhab.voice.whisperstt:temperature=0
org.openhab.voice.whisperstt:singleUtteranceMode=true
org.openhab.voice.whisperstt:errorMessage="Sorry, something went wrong"
org.openhab.voice.whisperstt:createWAVRecord=false
org.openhab.voice.whisperstt:recordSampleFormat=i16
```

### Default Speech-to-Text Configuration

You can select your preferred default Speech-to-Text in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Whisper** as **Speech-to-Text**.

In case you would like to set up these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultSTT=whisperstt
```
