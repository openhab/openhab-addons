# Polly Text-to-Speech

PollyTTS is a voice service utilizing the Internet based text-to-speech (TTS) service [Amazon Polly](https://aws.amazon.com/polly/).
The service generates speech from both plain text input and text with Speech Synthesis Markup Language (SSML) [tags](https://docs.aws.amazon.com/polly/latest/dg/supported-ssml.html).
There are servers set in various geographic [regions](https://docs.aws.amazon.com/general/latest/gr/rande.html#pol_region).
API keys provided by Amazon are required to get access to the service.
Amazon Polly has a wide selection of [voices and languages](https://aws.amazon.com/polly/features/#Wide_Selection_of_Voices_and_Languages).
Be aware, that using this service may incur costs on your AWS account.
You can find pricing information on the [documentation page](https://aws.amazon.com/polly/pricing/).

## Obtaining Credentials

* Sign up for Amazon Web Services (AWS). [link](https://portal.aws.amazon.com/billing/signup)

When you sign up for AWS, your account is automatically signed up for all services in AWS, including Amazon Polly. 

* Create an IAM User. [link](https://docs.aws.amazon.com/polly/latest/dg/setting-up.html)

Services in AWS, such as Amazon Polly, require that you provide credentials when you access them so that the service can determine whether you have permissions to access the resources owned by that service.
Within the AWS console, you can create access keys for your AWS account to access the Polly API.

To use the service you will need the **access key**, **secret key** and **server region**.

## Configuration

The following settings can be edited in UI (**Settings / Other Services - Polly Text-to-Speech**):

* **Access Key** - The AWS credentials access key (required).
* **Secret Key** - The AWS credentials secret key (required).
* **Service Region** - The service region used for accessing Polly (required). To reduce latency select the region closest to you. E.g. "eu-west-1" (see [regions](https://docs.aws.amazon.com/general/latest/gr/rande.html#pol_region))

* **Cache Expiration** - Cache expiration in days.

The PollyTTS service caches audio files from previous requests.
This reduces traffic, improves performance, reduces the number of requests and provides offline functionality.
When cache files are used their time stamps are updated, unused files are purged if their time stamp exceeds the specified age.
The default value of 0 disables this functionality.
A value of 365 removes files that have been unused for a year.

* **Audio Format** - Allows for overriding the system default audio format.
 
Use "default" to select the system default audio format.
The default audio format can be overriden with the value "mp3" or "ogg".


In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `pollytts.cfg`

Its contents should look similar to:

```
org.openhab.voice.pollytts:accessKey=ACCESS_KEY
org.openhab.voice.pollytts:secretKey=SECRET_KEY
org.openhab.voice.pollytts:serviceRegion=eu-west-1
org.openhab.voice.pollytts:cacheExpiration=0
org.openhab.voice.pollytts:audioFormat=default
```

### Default Text-to-Speech and Voice Configuration

You can setup your preferred default Text-to-Speech and default voice in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **PollyTTS** as **Default Text-to-Speech**.
* Choose your preferred **Default Voice** for your setup.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultTTS=pollytts
org.openhab.voice:defaultVoice=pollytts:Joanne
```

## Rule Examples

```
say("Hello there")  
say("Hello there", "pollytts:Joanne", "enhancedjavasound")  
say("" + item.state, "pollytts:Joey", "enhancedjavasound")  
say("<speak>Children, come to dinner <prosody volume='x-loud'>Right now!</prosody></speak>")  
```
