# Rustpotter Keyword Spotter

This voice service allows you to use the open source library Rustpotter as your keyword spotter in openHAB.
[Rustpotter](https://github.com/GiviMAD/rustpotter) is a free and open-source keywords spotter written in rust.

Rustpotter provides personal on-device wake word detection. You need to generate a model for your keyword using audio samples.

Important: No voice data listened by this service will be uploaded to the Cloud.
The voice data is processed offline, locally on your openHAB server by Rustpotter.

## Configuration

After installing, you will be able to access the service options through the openHAB configuration page in UI (**Settings / Other Services - Rustpotter Keyword Spotter**) to edit them:

* **Threshold** - Configures the detector threshold, is the min score (in range 0. to 1.) that some wake word template should obtain to trigger a detection. Defaults to 0.5.
* **Averaged Threshold** - Configures the detector averaged threshold, is the min score (in range 0. to 1.) that the audio should obtain against a combination of the wake word templates, the detection will be aborted if this is not the case. This way it can prevent to run the comparison of the current frame against each of the wake word templates which saves cpu. If set to 0 this functionality is disabled.
* **Eager mode** - Enables eager mode. End detection as soon as a result is over the score, instead of waiting to see if the next frame has a higher score.
* **Noise Detection Mode** - Use build-in noise detection to reduce computation on absence of noise. Configures the difficulty to consider a frame as noise (the required noise level).
* **Noise Detection Sensitivity** - Noise/silence ratio in the last second to consider noise is detected. Defaults to 0.5.
* **VAD Mode** - Use a voice activity detector to reduce computation in the absence of vocal sound.
* **VAD Sensitivity** - Voice/silence ratio in the last second to consider voice is detected.
* **VAD Delay** - Seconds to disable the vad detector after voice is detected. Defaults to 3.
* **Comparator Ref** - Configures the reference for the comparator used to match the samples.
* **Comparator Band Size** - Configures the band-size for the comparator used to match the samples.


In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `rustpotterks.cfg`

Its contents should look similar to:

```
org.openhab.voice.rustpotterks:threshold=0.5
org.openhab.voice.rustpotterks:averagedthreshold=0.2
org.openhab.voice.rustpotterks:comparatorRef=0.22
org.openhab.voice.rustpotterks:comparatorBandSize=6
org.openhab.voice.rustpotterks:eagerMode=true
org.openhab.voice.rustpotterks:noiseDetectionMode=hard
org.openhab.voice.rustpotterks:noiseDetectionSensitivity=0.5
org.openhab.voice.rustpotterks:vadMode=aggressive
org.openhab.voice.rustpotterks:vadSensitivity=0.5
org.openhab.voice.rustpotterks:vadDelay=3
```

## Magic Word Configuration

The magic word to spot is gathered from your 'Voice' configuration. 

You can generate your own wake word model by using the [Rustpotter CLI](https://github.com/GiviMAD/rustpotter-cli).

You can also download the models used as examples on the [rustpotter web demo](https://givimad.github.io/rustpotter-worklet-demo/) from [this folder](https://github.com/GiviMAD/rustpotter-worklet-demo/tree/main/static).

To use a wake word model, you should place the file under '\<openHAB userdata\>/rustpotter' and configure your magic word to match the file name replacing spaces with '_' and adding the extension '.rpw'.
As an example, the file generated for the keyword "ok openhab" will be named 'ok_openhab.rpw'.

The service will only work if it's able to find the correct rpw for your magic word configuration.


## Default Keyword Spotter and Magic Word Configuration

You can setup your preferred default keyword spotter and default magic word in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Rustpotter Keyword Spotter** as **Default Keyword Spotter**.
* Choose your preferred **Magic Word** for your setup.
* Choose optionally your **Listening Switch** item that will be switch ON during the period when the dialog processor has spotted the keyword and is listening for commands.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultKS=rustpotterks
org.openhab.voice:keyword=hey openhab
org.openhab.voice:listeningItem=myItemForDialog
```
