# Piper Text-to-Speech

This voice service allows you to use [Piper](https://github.com/OHF-Voice/piper1-gpl) as your TTS service in openHAB.
[Piper](https://github.com/OHF-Voice/piper1-gpl) is a open-source, fast, local neural text-to-speech system that sounds great and is optimized for the Raspberry Pi 4.

::: tip Note
This add-on depends on native libraries that cannot be included with the openHAB distribution due to their license.
The add-on will download these native dependencies automatically on first activation.
In case your openHAB server has no internet connection, you need to download the [piper-jni JAR file](https://repo1.maven.org/maven2/io/github/jvoice-project/piper-jni/1.4.1/piper-jni-1.4.1.jar) and place it into `<OPENHAB_USERDATA>/piper/`.
:::

## Supported platforms

The add-on is compatible with the following platforms:

- Linux (x86_64/aarch64, min. GLIBC version 2.31)
- macOS (x86_64/aarch64, min. version macOS 14 Sonoma)
- Windows (x86_64, min. version Windows 10).

## Configuration

### Downloading Voice Model Files

You can find an overview of the available voices on [GitHub](https://github.com/OHF-Voice/piper1-gpl/blob/main/docs/VOICES.md).

Each voice model is composed of two files: an ONNX runtime model file with extension `.onnx` and a model config file with extension `.onnx.json`.
For the add-on to load your voices, the two files must have the same base filename (differing only in their extensions).

You should place both voice files at '<OPENHAB_USERDATA>/piper/'.
After that, Main UI should display your available voices at _Settings / System Settings / Voice_.

#### Multi Speaker Voices

Models that support multiple speakers are shown as multiple voices in openHAB.

### Text to Speech Configuration

Use Main UI to edit **Settings / Add-on Settings / Piper Text-to-Speech**:

- **Preload model**: Keep the last-used voice model in memory, this way it can be reused on next execution if the voice option matches.

#### Configuration via a text file

In case you would like to set up the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `pipertts.cfg`

Its contents should look similar to:

```ini
org.openhab.voice.pipertts:preloadModel=true
```

### Default Text-to-Speech Configuration

You can setup your preferred default Speech-to-Text in the UI:

- Go to **Settings**.
- Edit **System Services - Voice**.
- Set **Piper** as **Text-to-Speech**.
- Set your **Default Voice**.

In case you would like to set up these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```ini
org.openhab.voice:defaultTTS=pipertts
```
