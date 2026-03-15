# Mary Text-to-Speech

The Mary Text-to-Speech (TTS) service is a pure Java implementation of a TTS service, which uses the [MaryTTS](http://mary.dfki.de/) project of DFKI.

While it provides good quality results, it must be noted that it is too heavy-weight for most embedded hardware like a Raspberry Pi. When using this service, you should be running openHAB on some real server instead.

## Configuration

There is no need to configure anything for this service.

## Voices

MaryTTS comes with three packages voices, one for American English, two for German:

```shell
> voice voices
marytts:cmuslthsmm cmu-slt-hsmm (en_US)
marytts:bits3hsmm bits3-hsmm (de)
marytts:bits1hsmm bits1-hsmm (de)
```

### Default Text-to-Speech and Voice Configuration

You can setup your preferred default Text-to-Speech and default voice in the UI:

- Go to **Settings**.
- Edit **System Services - Voice**.
- Set **MaryTTS** as **Default Text-to-Speech**.
- Choose your preferred **Default Voice** for your setup.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```ini
org.openhab.voice:defaultTTS=marytts
org.openhab.voice:defaultVoice=marytts:cmuslthsmm
```

## Supported Audio Formats

The MaryTTS service produces audio streams using WAV containers and PCM (signed) codec with 16bit depth.
The sample frequency depends on the chosen voice and ranges from 16kHz to 48kHz.

## Log files

The log messages of Mary TTS are not bundled with the openHAB log messages in the `openhab.log` file of your log directory but are stored in their own log file at `server.log` of your log directory.

## Caching

The MaryTTS service uses the openHAB TTS cache to cache audio files produced from the most recent queries in order to reduce traffic, improve performance and reduce number of requests.
