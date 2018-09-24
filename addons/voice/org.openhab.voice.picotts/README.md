# Pico Text-to-Speech

## Overview

The Pico Text-to-Speech (TTS) service uses the TTS binary from SVOX for producing spoken text.

You manually need to install the pico2wave binary in order for this service to work correctly. You can,
e.g., install it with apt-get on an Ubuntu system:

```
sudo apt-get install libttspico-utils
```

In Arch Linux the pico2wave binaries are available in an Arch User repository (AUR) under
https://aur.archlinux.org/packages/svox-pico-bin/

## Configuration

There is no need to configure anything for this service.

## Voices

The following list are the only supported languages (as these are the languages supported by
pico2wave, see also [the documentation of the debian package](https://packages.debian.org/de/wheezy/libttspico-utils)):

```
German (de-DE)
English, US (en-US)
English, GB (en-GB)
Spanish (es-ES)
French (fr-FR)
Italian (it-IT)
```

## Supported Audio Formats

The Pico service produces audio streams using WAV containers and PCM (signed) codec with 16bit depth.
