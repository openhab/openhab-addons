# Whisper Speech-to-Text

Whisper STT Service uses [whisper.cpp](https://github.com/ggerganov/whisper.cpp) to perform offline speech-to-text in openHAB.

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

As mentioned this add-on uses a native binary.
If interested you can check here the used [Java wrapper](https://github.com/GiviMAD/whisper-jni).

The following platforms are supported:

* debian-armv7l
* debian-arm64
* debian-x86_64
* macos-arm64 (+v11.0)
* macos-x86_64 (+v11.0)
* windows-x86_64

Note that the Raspberry PI 4 has bad performance running this add-on (the transcription time is similar to the audio duration using the tiny model).

For modern arm64 devices which cpu supports the flag fphp (Half Precision(16bit) Floating Point Data Processing Instructions) (armv8.2-a) the add-on will use a binary compiled with the [fp16 feature](https://gcc.gnu.org/onlinedocs/gcc/AArch64-Options.html) enabled offering a better performance. (This was tested on an Orange Pi 5).
Cpu flags can be checked on debian using `lscpu`.

## Configuring the model

Before you can use this service you should configure your model.

You can download them from the sources provided by the [whisper.cpp](https://github.com/ggerganov/whisper.cpp) author:

* https://huggingface.co/ggerganov/whisper.cpp
* https://ggml.ggerganov.com

You should place the downloaded .bin model in '\<openHAB userdata\>/whisper/' so the add-ons can find them.

Remember to check that you have enough RAM to load the model, estimated RAM consumption can be checked on the huggingface link.

## Operation

The add-on includes a very simple voice activity detector that you can use to avoid several calls to whisper.

When running without VAD whisper will be called each 'step' seconds, until the 'length' seconds is reached, in that moment the current transcription is stored and a new audio segment starts which keep the last 'keep' milliseconds of audio of the previous segment. This continues until the configured 'max transcription seconds' is reached or in case of having the 'single utterance mode' enabled until whisper end a phrase (a dot appears).
Note that if you introduce inconsistent values for the stepSeconds, lengthSeconds or keepMilliseconds configurations, those would be recalculated.

When running with VAD the audio will be cached until the detection of the configured 'max silence seconds' or the 'length' seconds is reached, then whisper will be called with that audio segment.
In case of having the 'single utterance mode' enabled, the transcription will end after this.
Otherwise it will continue transcribing segments until the 'max transcriptions seconds' is reached.

Note that time calculations are done in base to the audio time, without considering the execution time.

Recommended mode for voice commands is VAD with single utterance mode.

## Configuration

Use your favorite configuration UI to edit **Settings / Other Services - Whisper Speech-to-Text**:

### Speech to Text Configuration

* **Model Name** - Model name, 'ggml-' prefix and '.bin' extension are added by the binding if missing, so they are required on the filename (transformation example: tiny.en -> ggml-tiny.en.bin).
* **Preload Model** - Keep whisper model loaded.
* **Single Utterance Mode** - When enabled recognition stops listening after a single utterance.
* **Max Transcription Seconds** - Max seconds to wait to force stop the transcription.
* **Step Seconds** - Transcription step seconds. Does not apply when using VAD.
* **Length Seconds** - Transcription length seconds. Max audio to feed whisper with.
* **Keep Milliseconds** - Audio to keep after length seconds is reached and a new segment starts. Does not apply when using VAD.
* **Use VAD** - Use a voice activity detector to trigger transcription on silence.
* **VAD Threshold** - Threshold for voice activity detection.
* **VAD Max Silence Seconds** - Max silence seconds for triggering transcription.
* **Remove Specials** - Remove some characters from the transcription: ",", ".", "¿", "?", "¡", "!".
* **Create WAV File** - Create wav audio file on each transcription call, useful for debugging purposes or collecting samples.

### Whisper Configuration

* **Threads** - Number of threads used by whisper. (0 for default)
* **Speed Up** - Speed up audio by x2. (Reduced accuracy)
* **Audio Context** - Overwrite the audio context size. (0 for default)
* **Temperature** - Temperature threshold.
* **Sampling Strategy** - Sampling strategy used.
* **Beam Size** - Beam Size configuration for sampling strategy Bean Search.
* **Greedy Best Of** - Best Of configuration for sampling strategy Greedy.


### Messages Configuration

* **No Results Message** - Message to be told when no results.
* **Error Message** - Message to be told when an error has happened.

### Configuration via a text file

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `whisperstt.cfg`

Its contents should look similar to:

```
org.openhab.voice.whisperstt:modelName=tiny
org.openhab.voice.whisperstt:singleUtteranceMode=true
org.openhab.voice.whisperstt:preloadModel=false
org.openhab.voice.whisperstt:keepMs=500
org.openhab.voice.whisperstt:stepSeconds=5
org.openhab.voice.whisperstt:lengthSeconds=10
org.openhab.voice.whisperstt:maxSeconds=20
org.openhab.voice.whisperstt:useVAD=true
org.openhab.voice.whisperstt:vadThreshold=0.01f
org.openhab.voice.whisperstt:vadMaxSilenceSeconds=2
org.openhab.voice.whisperstt:threads=0
org.openhab.voice.whisperstt:audioContextSize=512
org.openhab.voice.whisperstt:samplingStrategy=GREEDY
org.openhab.voice.whisperstt:temperature=0
org.openhab.voice.whisperstt:singleUtteranceMode=true
org.openhab.voice.whisperstt:removeSpecials=true
org.openhab.voice.whisperstt:noResultsMessage="Sorry, I didn't understand you"
org.openhab.voice.whisperstt:errorMessage="Sorry, something went wrong"
org.openhab.voice.whisperstt:createWAVFile=false
```

### Default Speech-to-Text Configuration

You can select your preferred default Speech-to-Text in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Whisper** as **Speech-to-Text**.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultSTT=whisperstt
```
