# Google Cloud Text-to-Speech

Google Cloud TTS Service uses Google's Cloud Text-to-Speech API to convert text or Speech Synthesis Markup Language 
(SSML) input into audio data of natural human speech. 
It provides 30 voices, available in multiple languages and variants and applies DeepMind’s groundbreaking research in 
WaveNet and Google’s powerful neural networks. 
The implementation caches the converted texts to reduce the load on the API and make the conversion faster.

## Table of Contents

<!-- MarkdownTOC -->

- [Prerequisites](#prerequisites)
- [Obtaining Credentials](#obtaining-credentials)
- [Service Configuration](#service-configuration)
- [Voice Configuration](#voice-configuration)

<!-- /MarkdownTOC -->

## Prerequisites

Please make sure your installation runs on x86_64 Linux/Windows/Mac. 
Dependencies of Google Cloud Java support only these platforms. 
For details please visit [Google Cloud Java](https://github.com/GoogleCloudPlatform/google-cloud-java/blob/master/README.md)/Supported platforms.

## Obtaining Credentials

Before you can integrate this service with your Google Cloud Text-to-Speech, you must have a Google API Console project.

* Select or create a GCP project. [link](https://console.cloud.google.com/cloud-resource-manager)
* Make sure that billing is enabled for your project. [link](https://cloud.google.com/billing/docs/how-to/modify-project)
* Enable the Cloud Text-to-Speech API. [link](https://console.cloud.google.com/apis/dashboard)
* Set up authentication:
  * Go to the Create service account key page in the GCP Console.[link](https://console.cloud.google.com/apis/credentials/serviceaccountkey)
  * From the Service account drop-down list, select New service account.
  * Enter a name into the Service account name field.
  * Don't select a value from the Role drop-down list. No role is required to access this service.
  * Click Create. A note appears, warning that this service account has no role.
  * Click Create without role. A JSON file that contains your key downloads to your computer.

## Service Configuration

Using your favourite configuration UI (e.g. PaperUI) edit **Services/Voice/Google Cloud Text-to-Speech** settings and set 

* **Service Account Key** - Copy-paste the content of the downloaded key file.
* **Pitch** - The pitch of selected voice, up to 20 semitones
* **Volume Gain** - The volume of the output between 16dB and -96dB
* **Speaking Rate** - The speaking rate can be 4x faster or slower than the normal rate 

## Voice Configuration

Using your favourite configuration UI

* Edit **System** settings
* Edit **Voice** settings
* Set **Google Cloud** as **Default Text-to-Speech**
* Choose default voice for the setup.
