# microWakeWord Keyword Spotter

This voice service spots wake words with [microWakeWord](https://github.com/OHF-Voice/micro-wake-word), the wake word detector of ESPHome voice devices.

microWakeWord uses small neural networks, each trained to detect a single wake word phrase.
The models run in pure Java through the [micro-wakeword4j](https://github.com/jvoice-project/micro-wakeword4j) library.

Important: No voice data listened by this service will be uploaded to the Cloud. The voice data is processed offline, locally on your openHAB server by this binding.

## Supported Wake Words

These models are included:

- Alexa, from the [ESPHome model collection](https://github.com/esphome/micro-wake-word-models)
- Hey Jarvis, from the [ESPHome model collection](https://github.com/esphome/micro-wake-word-models)
- Hey Jeeves, trained by the [micro-wakeword4j](https://github.com/jvoice-project/micro-wakeword4j) project
- Hey Mycroft, from the [ESPHome model collection](https://github.com/esphome/micro-wake-word-models)
- Hey openHAB, trained by the [micro-wakeword4j](https://github.com/jvoice-project/micro-wakeword4j) project

### Custom Wake Words

Any other microWakeWord model (version 2) can be used, for example one trained with the [microWakeWord training notebook](https://github.com/OHF-Voice/micro-wake-word) or downloaded from a community collection.
A model consists of a JSON manifest and a `.tflite` file.

Place both files in the `$OPENHAB_USERDATA/microwakeword` folder and name the manifest after your magic word.
Write the magic word in lowercase, replace each run of characters other than letters and digits with `_`, and drop any `_` at the start or end.
For example, the manifest for the magic word "Hey, Computer!" is `hey_computer.json`.
The `model` entry of the manifest names the `.tflite` file, which must be in the same folder.

A model in this folder takes precedence over an included model with the same name.

## Configuration

After installing, you will be able to access the service options through the openHAB configuration page in UI (**Settings / Other Services - microWakeWord Keyword Spotter**) to edit them:

- **Probability Cutoff** - Average probability above which the wake word counts as detected. Lower values detect the wake word more easily but cause more false detections. Leave empty to use the cutoff of the model, which its author tuned.

For file based configurations, create a new file in `$OPENHAB_ROOT/conf/services` named `microwakewordks.cfg`.

```ini
org.openhab.voice.microwakewordks:probabilityCutoff=0.9
```

## Default Keyword Spotter and Magic Word Configuration

You can set up your preferred default keyword spotter and default magic word in the UI:

- Go to **Settings**.
- Edit **System Services - Voice**.
- Set **microWakeWord** as **Default Keyword Spotter**.
- Set the **Magic Word** to one of the supported wake words, for example "Hey openHAB".
- Optionally, choose a **Listening Switch** item, which is switched ON while the dialog processor listens for a command after spotting the wake word.

In case you would like to set up these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```ini
org.openhab.voice:defaultKS=microwakewordks
org.openhab.voice:keyword=Hey openHAB
org.openhab.voice:listeningItem=myItemForDialog
```
