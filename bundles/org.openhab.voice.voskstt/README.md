# Vosk Speech-to-Text

Vosk STT Service uses [vosk-api](https://github.com/alphacep/vosk-api) to perform offline speech-to-text in openHAB.

[Vosk](https://alphacephei.com/vosk/) is an offline open source speech recognition toolkit.
It enables speech recognition for 20+ languages and dialects - English, Indian English, German, French, Spanish, Portuguese, Chinese, Russian, Turkish, Vietnamese, Italian, Dutch, Catalan, Arabic, Greek, Farsi, Filipino, Ukrainian, Kazakh, Swedish, Japanese, Esperanto.
More to come.

## Supported platforms

This add-on uses an underling binary to work.
The following platforms are supported:

* linux-aarch64
* linux-armv7l
* linux-x86_64
* osx
* win64

**On Linux this binary requires the package libatomic to be installed (apt install libatomic1).**

## Configuring the model

Before you can use this service you should configure your language model.
You can download it from [here](https://alphacephei.com/vosk/models).
You should unzip the contained folder into '\<openHAB userdata\>/vosk/' and rename it to model for the add-on to work.

## Configuration

### Speech to Text Configuration

Use your favorite configuration UI to edit **Settings / Other Services - Vosk Speech-to-Text**:

* **Preload Model** - Keep language model loaded.
* **Single Utterance Mode** - When enabled recognition stops listening after a single utterance.
* **Max Transcription Seconds** - Max seconds to wait to force stop the transcription.
* **Max Silence Seconds** - Only works when singleUtteranceMode is disabled, max seconds without getting new transcriptions to stop listening.

### Messages Configuration

Use your favorite configuration UI to edit **Settings / Other Services - Vosk Speech-to-Text**:

* **No Results Message** - Message to be told when no results.
* **Error Message** - Message to be told when an error has happened.

### Configuration via a text file

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `voskstt.cfg`

Its contents should look similar to:

```
org.openhab.voice.voskstt:preloadModel=false
org.openhab.voice.voskstt:singleUtteranceMode=true
org.openhab.voice.voskstt:maxTranscriptionSeconds=60
org.openhab.voice.voskstt:maxSilenceSeconds=5
org.openhab.voice.voskstt:noResultsMessage="Sorry, I didn't understand you"
org.openhab.voice.voskstt:errorMessage="Sorry, something went wrong"
```

### Default Speech-to-Text Configuration

You can setup your preferred default Speech-to-Text in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Vosk** as **Speech-to-Text**.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultSTT=voskstt
```
