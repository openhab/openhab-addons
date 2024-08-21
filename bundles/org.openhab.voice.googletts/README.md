# Google Cloud Text-to-Speech

Google Cloud TTS Service uses the non-free Google Cloud Text-to-Speech API to convert text or Speech Synthesis Markup Language (SSML) input into audio data of natural human speech. 
It provides multiple voices, available in different languages and variants and applies DeepMind’s groundbreaking research in WaveNet and Google’s powerful neural networks. 
The Google Cloud TTS service uses the openHAB TTS cache to cache audio files produced from the most recent queries in order to reduce traffic, improve performance and reduce number of requests.
Be aware, that using this service may incur cost on your Google Cloud account.
You can find pricing information on the [documentation page](https://cloud.google.com/text-to-speech/#pricing-summary).

## Table of Contents

<!-- MarkdownTOC -->

* [Obtaining Credentials](#obtaining-credentials)
* [Configuration](#configuration)
* [Voice Configuration](#default-text-to-speech-and-voice-configuration)

<!-- /MarkdownTOC -->

## Obtaining Credentials

Before you can integrate this service with your Google Cloud Text-to-Speech, you must have a Google API Console project:

1. Select or create a Google Cloud project ([link](https://console.cloud.google.com/cloud-resource-manager))
2. Make sure that billing is enabled for your project ([link](https://cloud.google.com/billing/docs/how-to/modify-project))
3. Enable the `Text-to-Speech API` ([link](https://console.cloud.google.com/apis/dashboard))
  * Don't mistake with the `Speech-to-Text API`
4. Set up authentication:
  1. Go to the `APIs & Services` -> `Credentials` page in the GCP Console and your project ([link](https://console.cloud.google.com/apis/credentials))
  2. From the `Create credentials` drop-down list, select `OAuth client ID`.
  3. Select application type `Web application` and enter a name into the `Name` (e.g. `OpenHAB TTS`) field.
  4. Add `https://www.google.com` to the `Authorized redirect URIs`.
    * Watch out for trailing spaces in the URL! E.g. when using the copy url feature in the context menu of Google Chrome it appends a slash (`https://www.google.com` becomes `https://www.google.com`) and the steps afterwards fail.
  5. Click Create. A pop-up appears, showing your "client ID" and "client secret".

## Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Google Cloud Text-to-Speech** and set:

* **Client Id** - Google Cloud Platform OAuth 2.0-Client Id.
* **Client Secret** - Google Cloud Platform OAuth 2.0-Client Secret.
* **Authorization Code** - This code is used once for retrieving the Google Cloud Platform access and refresh tokens.
  1. Go to `OAuth consent screen` ([link](https://console.cloud.google.com/apis/credentials/consent)) and set the `publishing status` to `production` to activate the created project.
  2. Wait a few minutes. The step before takes some time to go live. Sometimes it is necessary to wait a few hours, so you can try after some minutes, if it doesn't work, try again the next day.
  3. Go to your browser and access the following URL to generate an authorization code
[https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/cloud-platform&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=<clientId>](https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/cloud-platform&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=CLIENTID) (replace `CLIENTID` at the end by your Client Id)
  4. After your browser has been redirected to https://www.google.com, the authorization code will be set in the browser URL as value of the "code" URL query parameter (e.g. `https://www.google.com/?code=XXXXXXXXXX&scope=https://www.googleapis.com/auth/cloud-platform` --> `XXXXXXXXXX`)
  5. After initial authorization, this code is not needed anymore. It is recommended to clear this configuration parameter afterwards.
* **Pitch** - The pitch of selected voice, up to 20 semitones.
* **Volume Gain** - The volume of the output between 16dB and -96dB.
* **Speaking Rate** - The speaking rate can be 4x faster or slower than the normal rate.

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `googletts.cfg`

Its contents should look similar to:

```
org.openhab.voice.googletts:clientId=ID
org.openhab.voice.googletts:clientSecret=SECRET
org.openhab.voice.googletts:authcode=XXXXX
org.openhab.voice.googletts:pitch=0
org.openhab.voice.googletts:volumeGain=0
org.openhab.voice.googletts:speakingRate=1
```

### Default Text-to-Speech and Voice Configuration

You can setup your preferred default Text-to-Speech and default voice in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Google Cloud** as **Default Text-to-Speech**.
* Choose your preferred **Default Voice** for your setup.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultTTS=googletts
org.openhab.voice:defaultVoice=googletts:XXX
```
You can see the available voices by entering `voice voices` in the karaf console
```shell
openhab> voice voices
  ...
  Google Cloud - Deutsch (Deutschland) - de-DE-Standard-C (googletts:deDEStandardC)
  ...
  Google Cloud - Englisch (Vereinigte Staaten) - en-US-Neural2-A (googletts:enUSNeural2A)
  ...
```
