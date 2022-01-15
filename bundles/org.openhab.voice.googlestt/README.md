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
  * From the "Create credentials" drop-down list, select "OAuth client ID.
  * Select application type "TV and Limited Input" and enter a name into the "Name" field.
  * Click Create. A pop-up appears, showing your "client ID" and "client secret".

## Service Authentication Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Google Cloud Speech-to-Text** and set:

* **Client Id** - Google Cloud Platform OAuth 2.0-Client Id.
* **Client Secret** - Google Cloud Platform OAuth 2.0-Client Secret.
* **Oauth Code** - The oauth code is a one-time code needed to retrieve the necessary access-codes from Google Cloud Platform.**Please go to your browser ...**[https://accounts.google.com/o/oauth2/auth?client_id=<clientId>&redirect_uri=urn:ietf:wg:oauth:2.0:oob&scope=https://www.googleapis.com/auth/cloud-platform&response_type=code](https://accounts.google.com/o/oauth2/auth?client_id=<clientId>&redirect_uri=urn:ietf:wg:oauth:2.0:oob&scope=https://www.googleapis.com/auth/cloud-platform&response_type=code) (replace `<clientId>` by your Client Id)**... to generate an auth-code and paste it here**. After initial authorization, this code is not needed anymore.

## Service Speech to Text Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Google Cloud Speech-to-Text**:

* **Single Utterance Mode** - When enabled Google Cloud Platform is responsible for detect when to stop listening after a single utterance. (Recommended)
* **Max Transcription Seconds** - Max seconds to wait to force stop the transcription.
* **Max Silence Seconds** - Only works when singleUtteranceMode is disabled, max seconds without get new transcriptions to stop listening.
* **Refresh Supported Locales** - Try to load supported locales from the documentation page.

## Service Messages Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Google Cloud Speech-to-Text**:

* **No Results Message** - Message to be told when no results. (Empty for disabled)
* **Error Message** - Message to be told when an error has happened. (Empty for disabled)
