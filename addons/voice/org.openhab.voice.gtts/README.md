# Google Cloud Text-to-Speech

The Google Cloud Text-to-Speech API converts text or Speech Synthesis Markup Language (SSML) input into audio data 
of natural human speech. It provides 30 voices, available in multiple languages and variants and applies DeepMind’s 
groundbreaking research in WaveNet and Google’s powerful neural networks to deliver the highest fidelity possible. 
This addon uses this API for implementation of TTSService. The implementation caches the converted texts under $userdata/
gtts/cache folder to reduce the load on the API and make the conversion faster. 

## Table of Contents

<!-- MarkdownTOC -->

- [Obtaining Credentials](#obtaining-credentials)
- [Service Configuration](#service-configuration)
- [Voice Configuration](#voice-configuration)

<!-- /MarkdownTOC -->

## Obtaining Credentials

Before you can integrate this service with your Google Cloud Text-to-Speech, you must have a Google API Console project.

* Follow the [instructions](https://cloud.google.com/text-to-speech/docs/quickstart-protocol) and set up a project 
* Download JSON service account key file.

## Service Configuration

After the Google TTS addon is installed it creates a **$USERDATA/gtts** folder. Place the downloaded key JSON file in this 
folder. Using your favourite configuration UI (e.g. PaperUI) edit **services/voice/Google Cloud TTS Service** settings and set 

* **Service Account Key Name** - Key file name located in service home folder.
* **Pitch** - The pitch of selected voice, up to 20 semitones
* **Volume Gain** - The volume of the output between 16dB and -96dB
* **Speaking Rate** - The speaking rate can be 4x faster or slower than the normal rate 

## Voice Configuration

Using your favourite configuration UI

* Edit **System** settings
* Edit **Voice** settings
* Set **Google Cloud TTS Service** as **Default Text-to-Speech**
* Choose default voice for the setup. **Wavenet** voices available for en-US location
