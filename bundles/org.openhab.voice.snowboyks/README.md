# Snowboy Keyword Spotter

This voice service allows you to use the Snowboy as your keyword spotter in openHAB.

Snowboy provides on-device wake word detection powered by deep learning.

Important: No voice data listened by this service will be uploaded to the Cloud.
The voice data is processed offline, locally on your openHAB server by Snowboy.

# Supported platforms

This binding includes the pre-compiled snowboy binaries for the following platforms:

* macos
* debian x86_64
* debian armv7l
* debian aarch64

## Configuration

After installing, you will be able to access the service options through the openHAB configuration page in UI (**Settings / Other Services - Snowboy Keyword Spotter**) to edit them:

* **Apply Frontend** - Enables Audio Frontend.

* **Sensitivities** - Spot sensitivity, a higher sensitivity reduces miss rate at cost of increased false alarm rate.

* **Audio Gain** - Change audio gain, values less than one decrease and higher increase.

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `snowboyks.cfg`

Its contents should look similar to:

```
org.openhab.voice.snowboyks:apiKey=KEY
org.openhab.voice.snowboyks:sensitivity=0.5
```

## Magic Word Configuration

The magic word to spot is gathered from your 'Voice' configuration.

You need to , you should place the generated file under '\<openHAB userdata\>/snowboy' and configure your magic word to match the file name replacing spaces with '_' and adding the extension '.umdl'  or '.pmdl'.
As an example, the file generated for the keyword "ok openhab" will be named 'ok_openhab.umdl'.

The service will only work if it's able to find the correct umdl for your magic word configuration.

#### Build-in keywords

* computer
* jarvis
* snowboy
* smart mirror

#### Build your own personal models

As described (here)[https://github.com/seasalt-ai/snowboy#new-build-your-own-personal-models-ubuntu-1604-and-macos] you can build your own models.

This is a quick summary on how to do it with docker:

* record 3 wav files containing your wake word (or phrase) called record1.wav, record2.wav and record3.wav. (rec -r 16000 -c 1 -b 16 -e signed-integer -t wav record<N>.wav)
* move them to a folder called 'model'.
* run this docker command in the parent 'model' directory: 'docker run -it -v $(pwd)/model:/snowboy-master/examples/Python/model givi/snowboy-pmdl'.
* rename and move the generated file from the 'model' folder to '\<openHAB userdata\>/snowboy'.

## Language support

The service assumes your configured wake work matches the openHAB configured language.

## Default Keyword Spotter and Magic Word Configuration

You can setup your preferred default keyword spotter and default magic word in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Snowboy Keyword Spotter** as **Default Keyword Spotter**.
* Choose your preferred **Magic Word** for your setup.
* Choose optionally your **Listening Switch** item that will be switch ON during the period when the dialog processor has spotted the keyword and is listening for commands.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultKS=snowboyks
org.openhab.voice:keyword=computer
org.openhab.voice:listeningItem=myItemForDialog
```
