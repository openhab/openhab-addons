# Piper Text-to-Speech

This voice service allows you to use the open source library [Piper](https://github.com/rhasspy/piper) as your TTS service in openHAB.
[Piper](https://github.com/rhasspy/piper) is a fast, local neural text to speech system that sounds great and is optimized for the Raspberry Pi 4.

## Supported platforms

The add-on is compatible with the following platforms:

* linux (armv7l, aarch64, x86_64, min GLIBC version 2.31)
* macOS (x86_64 min version 11.0, aarch64 min version 13.0)
* win64 (x86_64 min version Windows 10).

## Configuration

## Setting up dependencies

The add-on will download the required dependencies at first activation.

If your openHAB installation does not have access to the Internet, you need to download the [piper-jni jar file](https://repo1.maven.org/maven2/io/github/givimad/piper-jni/1.2.0-a0f09cd/piper-jni-1.2.0-a0f09cd.jar) and place it at '<OPENHAB_USERDATA>/piper/'.

### Downloading Voice Model Files

You can find the link to the available voices at the [Piper README](https://github.com/rhasspy/piper).

Each voice model is composed of two files an onnx runtime model file with extension '.onnx' and a model config file with extension '.onnx.json'.
For the add-on to load your voices you need both to be named equal (obviously excluding their extensions).

You should place both voice files at '<OPENHAB_USERDATA>/piper/'.
After that the UI should display your available voices at 'Settings / System Settings / Voice'.

### Multi Speaker Voices

Models that support multiples speakers are shown as multiple voices in openHAB.

### Text to Speech Configuration

Use your favorite configuration UI to edit **Settings / Other Services - Piper Text-to-Speech**:

* **Preload model** - Keep last voice model used loaded in memory, these way it can be reused on next execution if the voice option matches.

### Configuration via a text file

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `pipertts.cfg`

Its contents should look similar to:

```text
org.openhab.voice.pipertts:preloadModel=true
```

### Default Text-to-Speech Configuration

You can setup your preferred default Speech-to-Text in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Piper** as **Text-to-Speech**.
* Set your **Default Voice**.

In case you would like to set up these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```text
org.openhab.voice:defaultTTS=pipertts
```
