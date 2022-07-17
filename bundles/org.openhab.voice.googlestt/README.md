# Google Cloud Speech-to-Text

Google Cloud STT Service uses the non-free Google Cloud Speech-to-Text API to transcript audio data to text. 
Be aware, that using this service may incur cost on your Google Cloud account.
You can find pricing information on the [documentation page](https://cloud.google.com/speech-to-text#section-12).

## Obtaining Credentials

Before you can integrate this service with your Google Cloud Speech-to-Text, you must have a Google API Console project:

* Select or create a GCP project. [link](https://console.cloud.google.com/cloud-resource-manager)
* Make sure that billing is enabled for your project. [link](https://cloud.google.com/billing/docs/how-to/modify-project)
* Enable the Cloud Speech-to-Text API. [link](https://console.cloud.google.com/apis/dashboard)
* Set up authentication:
  * Go to the "APIs & Services" -> "Credentials" page in the GCP Console and your project. [link](https://console.cloud.google.com/apis/credentials)
  * From the "Create credentials" drop-down list, select "OAuth client ID".
  * Select application type "Web application" and enter a name into the "Name" field.
  * Add "https://www.google.com" to the "Authorized redirect URIs".
  * Click Create. A pop-up appears, showing your "client ID" and "client secret".

## Configuration

### Authentication Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Google Cloud Speech-to-Text** and set:

* **Client Id** - Google Cloud Platform OAuth 2.0-Client Id.
* **Client Secret** - Google Cloud Platform OAuth 2.0-Client Secret.
* **Authorization Code** - This code is used once for retrieving the Google Cloud Platform access and refresh tokens.
**Please go to your browser ...**
[https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/cloud-platform&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=<clientId>](https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/cloud-platform&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=<clientId>) (replace `<clientId>` by your Client Id)
**... to generate an authorization code and paste it here**.
After your browser has been redirected to https://www.google.com, the authorization code will be set in the browser URL as value of the "code" URL query parameter.
After initial authorization, this code is not needed anymore.
It is recommended to clear this configuration parameter afterwards.

### Speech to Text Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Google Cloud Speech-to-Text**:

* **Single Utterance Mode** - When enabled Google Cloud Platform is responsible for detecting when to stop listening after a single utterance. (Recommended)
* **Max Transcription Seconds** - Max seconds to wait to force stop the transcription.
* **Max Silence Seconds** - Only works when singleUtteranceMode is disabled, max seconds without getting new transcriptions to stop listening.
* **Refresh Supported Locales** - Try loading supported locales from the documentation page.

### Messages Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Google Cloud Speech-to-Text**:

* **No Results Message** - Message to be told when no results. (Empty for disabled)
* **Error Message** - Message to be told when an error has happened. (Empty for disabled)

### Configuration via a text file

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `googlestt.cfg`

Its contents should look similar to:

```
org.openhab.voice.googlestt:clientId=ID
org.openhab.voice.googlestt:clientSecret=SECRET
org.openhab.voice.googlestt:authcode=XXXXX
org.openhab.voice.googlestt:singleUtteranceMode=true
org.openhab.voice.googlestt:maxTranscriptionSeconds=60
org.openhab.voice.googlestt:maxSilenceSeconds=5
org.openhab.voice.googlestt:refreshSupportedLocales=false
org.openhab.voice.googlestt:noResultsMessage="Sorry, I didn't understand you"
org.openhab.voice.googlestt:errorMessage="Sorry, something went wrong"
```

### Default Speech-to-Text Configuration

You can setup your preferred default Speech-to-Text in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Google Cloud** as **Speech-to-Text**.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultSTT=googlestt
```
