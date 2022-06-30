# Mimic Text-to-Speech

Mimic (version 3 and above) is an offline open source Text-To-speech engine designed by Mycroft A.I. for the eponym Vocal Assistant.
It provides multiple voices, available in different languages and variants.
Its neural network is built upon some very good and some not-so-good models. Try some to be sure you get the best one for your need.
Mimic3 doesn't need Mycroft, it can be run standalone as a service or command line utility.
When launched as a web server, it exposes its capability through a web API. This TTS bundle make use of these feature, so please take note : this openHAB TTS bundle is NOT a standalone ! it requires mimic web server to run somewhere (on your openHAB computer, or your network)

It supports a subset of SSML. If you want to use SSML, be sure to start your text with `<speak>`.

## Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Mimic Text-to-Speech** and set:

* **url** - Mimic URL. default to `http://localhost:59125`
* **speakingRate** - Controls how fast the voice speaks the text. A value of 1 is the speed of the training dataset. Less than 1 is faster, and more than 1 is slower.
* **audioVolatility** - The amount of noise added to the generated audio (0-1). Can help mask audio artifacts from the voice model. Multi-speaker models tend to sound better with a lower amount of noise than single speaker models.
* **phonemeVolatility ...** - The amount of noise used to generate phoneme durations (0-1). Allows for variable speaking cadance, with a value closer to 1 being more variable. Multi-speaker models tend to sound better with a lower amount of phoneme variability than single speaker models.

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `mimictts.cfg`

Its contents should look similar to:

```
org.openhab.voice.mimictts:url=http://localhost:59125
org.openhab.voice.mimictts:speakingRate=1
org.openhab.voice.mimictts:audioVolatility=0.667
org.openhab.voice.mimictts:phonemeVolatility=0.8
```

### Default Text-to-Speech and Voice Configuration

You can setup your preferred default Text-to-Speech and default voice in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Mimic** as **Default Text-to-Speech**.
* Choose your preferred **Default Voice** for your setup.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultTTS=mimictts
org.openhab.voice:defaultVoice=mimictts:XXX
```
