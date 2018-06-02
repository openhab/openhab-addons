# Mary Text-to-Speech

## Overview

The Mary Text-to-Speech (TTS) service is a pure Java implementation of a TTS service, which uses the [MaryTTS](http://mary.dfki.de/) project of DFKI.

While it provides good quality results, it must be noted that it is too heavy-weight for most embedded hardware like Raspberry Pis. When using this service, you should be running openHAB on some real server instead.

## Configuration

There is no need to configure anything for this service.

## Voices

MaryTTS comes with three packages voices, one for American English, two for German:

```
> voice voices
marytts:cmuslthsmm cmu-slt-hsmm (en_US)
marytts:bits3hsmm bits3-hsmm (de)
marytts:bits1hsmm bits1-hsmm (de)
```

## Supported Audio Formats

The MaryTTS service produces audio streams using WAV containers and PCM (signed) codec with 16bit depth.
The sample frequency depends on the chosen voice and ranges from 16kHz to 48kHz.

## Log files

The log messages of Mary TTS are not bundled with the openHAB log messages in the `openhab.log` file of your log directory but are stored in their own log file at `server.log` of your log directory.
