# Rustpotter Keyword Spotter

This voice service allows you to use the open source library Rustpotter as your keyword spotter in openHAB.
[Rustpotter](https://github.com/GiviMAD/rustpotter) is a free and open-source keywords spotter written in rust.

Rustpotter provides personal on-device wake word detection. You need to generate a model for your keyword using audio samples.

You can test library in your browser using these web pages:

- [The spot demo](https://givimad.github.io/rustpotter-worklet-demo/), which include some example wakewords (but it's recommended to use your own).
- [The model creation demo](https://givimad.github.io/rustpotter-create-model-demo/), it allows you to record compatible wav files and generate a wakeword file that you can test on the previous page.

Important: No voice data listened by this service will be uploaded to the Cloud.
The voice data is processed offline, locally on your openHAB server by Rustpotter.

## Configuration

After installing, you will be able to access the service options through the openHAB configuration page in UI (**Settings / Other Services - Rustpotter Keyword Spotter**) to edit them:

- **Threshold** - Configures the detector threshold, is the min score (in range 0. to 1.) that some wake word template should obtain to trigger a detection. Defaults to 0.5.
- **Averaged Threshold** - Configures the detector averaged threshold, is the min score (in range 0. to 1.) that the audio should obtain against a combination of the wake word templates, the detection will be aborted if this is not the case. This way it can prevent to run the comparison of the current frame against each of the wake word templates which saves cpu. If set to 0 this functionality is disabled.
- **Score Mode** - Indicates how to calculate the final score.
- **Min Scores** - Minimum number of positive scores to consider a partial detection as a detection.
- **Comparator Ref** - Configures the reference for the comparator used to match the samples.
- **Comparator Band Size** - Configures the band-size for the comparator used to match the samples.
- **Gain Normalizer** - Enables an audio filter that intent to approximate the volume of the stream to a reference level.
- **Min Gain** - Min gain applied by the gain normalizer filter.
- **Max Gain** - Max gain applied by the gain normalizer filter.
- **Gain Ref** - The RMS reference used by the gain-normalizer to calculate the gain applied. If unset an estimation of the wakeword level is used.
- **Band Pass** - Enables an audio filter that attenuates frequencies outside the low cutoff and high cutoff range.
- **Low Cutoff** - Low cutoff for the band-pass filter.
- **High Cutoff** - High cutoff for the band-pass filter.

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `rustpotterks.cfg`

Its contents should look similar to:

```
org.openhab.voice.rustpotterks:threshold=0.5
org.openhab.voice.rustpotterks:averagedthreshold=0.2
org.openhab.voice.rustpotterks:scoreMode=max
org.openhab.voice.rustpotterks:minScores=5
org.openhab.voice.rustpotterks:comparatorRef=0.22
org.openhab.voice.rustpotterks:comparatorBandSize=5
org.openhab.voice.rustpotterks:gainNormalizer=true
org.openhab.voice.rustpotterks:minGain=0.5
org.openhab.voice.rustpotterks:maxGain=1
org.openhab.voice.rustpotterks:gainRef=
org.openhab.voice.rustpotterks:bandPass=true
org.openhab.voice.rustpotterks:lowCutoff=80
org.openhab.voice.rustpotterks:highCutoff=400
```

## Magic Word Configuration

The magic word to spot is gathered from your 'Voice' configuration. 

You can generate your own wakeword files using the [Rustpotter CLI](https://github.com/GiviMAD/rustpotter-cli).

You can also download the models used as examples on the [rustpotter web demo](https://givimad.github.io/rustpotter-worklet-demo/) from [this folder](https://github.com/GiviMAD/rustpotter-worklet-demo/tree/main/static).

To use a wake word model, you should place the file under '\<openHAB userdata\>/rustpotter' and configure your magic word to match the file name replacing spaces with '_' and adding the extension '.rpw'.
As an example, the file generated for the keyword "ok openhab" will be named 'ok_openhab.rpw'.

The service will only work if it's able to find the correct rpw for your magic word configuration.


## Default Keyword Spotter and Magic Word Configuration

You can setup your preferred default keyword spotter and default magic word in the UI:

- Go to **Settings**.
- Edit **System Services - Voice**.
- Set **Rustpotter Keyword Spotter** as **Default Keyword Spotter**.
- Choose your preferred **Magic Word** for your setup.
- Choose optionally your **Listening Switch** item that will be switch ON during the period when the dialog processor has spotted the keyword and is listening for commands.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultKS=rustpotterks
org.openhab.voice:keyword=hey openhab
org.openhab.voice:listeningItem=myItemForDialog
```
