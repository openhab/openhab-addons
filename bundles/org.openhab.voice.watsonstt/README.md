# IBM Watson Speech-to-Text

Watson STT Service uses the non-free IBM Watson Speech-to-Text API to transcript audio data to text. 
Be aware that using this service may incur cost on your IBM account.
You can find pricing information on [this page](https://www.ibm.com/cloud/watson-speech-to-text/pricing).

## Obtaining Credentials

Before you can use this add-on, you should create a Speech-to-Text instance in the IBM Cloud service. 

* Go to the following [link](https://cloud.ibm.com/catalog/services/speech-to-text) and create the instance in your desired region.
* After the instance is created you should be able to view its url and api key.

## Configuration

### Authentication Configuration

Use your favorite configuration UI to edit **Settings / Other Services - IBM Watson Speech-to-Text** and set:

* **Api Key** - Api key for Speech-to-Text instance created on IBM Cloud.
* **Instance Url** - Url for Speech-to-Text instance created on IBM Cloud.

### Speech to Text Configuration

Use your favorite configuration UI to edit **Settings / Other Services - IBM Watson Speech-to-Text**:

* **Prefer Multimedia Model** - Prefer multimedia to telephony [models](https://cloud.ibm.com/docs/speech-to-text?topic=speech-to-text-models-ng). Multimedia models are intended for audio that has a minimum sampling rate of 16 kHz, while telephony models are intended for audio that has a minimum sampling rate of 8 kHz.
* **Background Audio Suppression** - Use the parameter to suppress side conversations or background noise.
* **Speech Detector Sensitivity** - Use the parameter to suppress word insertions from music, coughing, and other non-speech events.
* **Single Utterance Mode** - When enabled recognition stops listening after a single utterance.
* **Max Silence Seconds** - The time in seconds after which, if only silence (no speech) is detected in the audio, the connection is closed.
* **Opt Out Logging** - By default, all IBM Watson™ services log requests and their results. Logging is done only to improve the services for future users. The logged data is not shared or made public.
* **No Results Message** - Message to be told when no results.
* **Smart Formatting** - If true, the service converts dates, times, series of digits and numbers, phone numbers, currency values, and internet addresses into more readable. (Not available for all locales)
* **Redaction** - If true, the service redacts, or masks, numeric data from final transcripts. (Not available for all locales)

### Configuration via a text file

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `watsonstt.cfg`

Its contents should look similar to:

```
org.openhab.voice.watsonstt:apiKey=******
org.openhab.voice.watsonstt:instanceUrl=https://api.***.speech-to-text.watson.cloud.ibm.com/instances/*****
org.openhab.voice.watsonstt:backgroundAudioSuppression=0.5
org.openhab.voice.watsonstt:speechDetectorSensitivity=0.5
org.openhab.voice.watsonstt:singleUtteranceMode=true
org.openhab.voice.watsonstt:maxSilenceSeconds=2
org.openhab.voice.watsonstt:optOutLogging=false
org.openhab.voice.watsonstt:smartFormatting=false
org.openhab.voice.watsonstt:redaction=false
org.openhab.voice.watsonstt:noResultsMessage="Sorry, I didn't understand you"
org.openhab.voice.watsonstt:errorMessage="Sorry, something went wrong"
```

### Default Speech-to-Text Configuration

You can setup your preferred default Speech-to-Text in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Watson** as **Speech-to-Text**.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultSTT=watsonstt
```
