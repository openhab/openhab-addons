# macOS Text-to-Speech

## Overview

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

## Supported Audio Formats

The MacTTS service produces audio streams using WAV containers and PCM (signed) codec with 16bit depth and 44.1kHz frequency.
