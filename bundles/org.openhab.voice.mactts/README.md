# macOS Text-to-Speech

The macOS Text-to-Speech (TTS) service uses the macOS "say" command for producing spoken text.

Obviously, this service only works on a host that is running macOS.

## Configuration

There is no need to configure anything for this service.

## Voices

It automatically scans all available voices and registers them, see e.g.

```
> voice voices
mactts:Alex Alex (en_US)
mactts:Ioana Ioana (ro_RO)
mactts:Moira Moira (en_IE)
mactts:Sara Sara (da_DK)
mactts:Ellen Ellen (nl_BE)
mactts:Thomas Thomas (fr_FR)
mactts:Zosia Zosia (pl_PL)
mactts:Steffi Steffi (de_DE)
mactts:Amelie Amelie (fr_CA)
mactts:Veena Veena (en_IN)
mactts:Luciana Luciana (pt_BR)
mactts:Mariska Mariska (hu_HU)
mactts:Sinji Sin-ji (zh_HK)
mactts:Markus Markus (de_DE)
mactts:Zuzana Zuzana (cs_CZ)
mactts:Kyoko Kyoko (ja_JP)
mactts:Satu Satu (fi_FI)
mactts:Yuna Yuna (ko_KR)
...
```

### Default Text-to-Speech and Voice Configuration

You can setup your preferred default Text-to-Speech and default voice in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **macOS TTS** as **Default Text-to-Speech**.
* Choose your preferred **Default Voice** for your setup.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultTTS=mactts
org.openhab.voice:defaultVoice=mactts:Alex
```

## Supported Audio Formats

The MacTTS service produces audio streams using WAV containers and PCM (signed) codec with 16bit depth and 44.1kHz frequency.

## Caching

The macOS TTS service uses the openHAB TTS cache to cache audio files produced from the most recent queries in order to reduce traffic, improve performance and reduce number of requests.
